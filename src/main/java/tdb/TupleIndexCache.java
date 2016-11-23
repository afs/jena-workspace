/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tdb;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;
import java.util.concurrent.Callable ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;
import org.apache.jena.tdb.store.tupletable.TupleIndexWrapper ;

/** Caching wrapper for a TupleIndex.
 * The caching is by remembering previous {@link #find} by pattern.
 */
public class TupleIndexCache extends TupleIndexWrapper {
    // ARQ/TDB can end up making the same calls.
    // What about pushing this up to the solver? (Don't know pattern order.)
    
    // Update - drop all for now but can test patterns - expensive if many updates.

    private final Cache<Tuple<NodeId>, Collection<Tuple<NodeId>>> patternResultCache = CacheFactory.createCache(1000) ;
    private final int prefixCount;
    
    public TupleIndexCache(TupleIndex index, int prefixCount) {
        super(index);
        this.prefixCount = prefixCount ;
    }
    
    @Override
    public boolean add(Tuple<NodeId> tuple) {
        patternResultCache.clear() ;
        return super.add(tuple) ;
    }

    @Override
    public boolean delete(Tuple<NodeId> tuple) {
        patternResultCache.clear() ;
        return super.delete(tuple) ;
    }
    
    @Override
    public void clear() {
        patternResultCache.clear() ;
        super.clear() ;
    }


    @Override
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern) {
        return findCacheOnPattern(pattern) ; 
        //return findCacheOnPrefix(pattern) ;
    }
    
    private Iterator<Tuple<NodeId>> findCacheOnPattern(Tuple<NodeId> pattern) {
        // Easy case - same pattern.
        
        if ( ! acceptable(pattern) ) {
            System.err.printf("Unacceptable  [%s] : %s\n", getName(), pattern) ;
            return super.find(pattern) ;
        }
        
        Callable<Collection<Tuple<NodeId>>> c = () -> {
            return Iter.toList(super.find(pattern)) ;
        } ;
        
        if ( patternResultCache.containsKey(pattern) )
            System.err.printf("Hit  [%s] : %s\n", getName(), pattern) ;
        else
            System.err.printf("Miss [%s] : %s\n", getName(), pattern) ;
        
        Collection<Tuple<NodeId>> x = patternResultCache.getOrFill(pattern, c ) ;
        
        if ( x.size() > 100 ) {
            patternResultCache.remove(pattern); 
        }
        
        return x.iterator() ;
    }
    
    private Iterator<Tuple<NodeId>> findCacheOnPrefix(Tuple<NodeId> pattern) {
        // Assumption S* or PO* cache ie. small hits.
        
        if ( acceptable(pattern) ) {
            Tuple<NodeId> cacheKey = cacheKey(pattern) ;
            
            if ( patternResultCache.containsKey(cacheKey) )
                System.err.printf("Hit  [%s] : %s / %s\n", getName(), cacheKey, pattern) ;
            else
                System.err.printf("Miss [%s] : %s / %s\n", getName(), cacheKey, pattern) ;

            
            Callable<Collection<Tuple<NodeId>>> c = () -> Iter.toList(super.find(cacheKey)) ;
            Collection<Tuple<NodeId>> x = patternResultCache.getOrFill(cacheKey, c) ;
            if ( false ) {
                // If we assume list order which is true for prefix patterns with TDB.
                List<Tuple<NodeId>> z = (List<Tuple<NodeId>>)x ;
                Iterator<Tuple<NodeId>> iter = z.iterator() ;
                iter = Iter.takeWhile(iter, (item)->matchPattern(pattern, item)) ;
                return iter ;
            }
            
            //System.err.println(x) ;
            
            Iterator<Tuple<NodeId>> iter = x.iterator() ;
            // add filter for key wider than pattern.
            iter = scan(iter, pattern) ;
            return iter ;  
        }
     
        System.err.printf("Unacceptable  [%s] : %s\n", getName(), pattern) ;
    
        
        return super.find(pattern) ;
    }
    

    private Tuple<NodeId> cacheKey(Tuple<NodeId> pattern) {
        // Tuple.slice?
        NodeId[] k = new NodeId[pattern.len()] ; 
        for ( int i = 0 ; i < prefixCount ; i++ ) {
            k[i] = pattern.get(i) ;
        }
        // Rest are nulls.
        return TupleFactory.create(k) ;
    }

    private boolean acceptable(Tuple<NodeId> pattern) {
        for ( int i = 0 ; i < prefixCount ; i++ ) {
            NodeId slot = pattern.get(i) ;
            if ( slot == null || ( ! slot.isConcrete() && ! slot.isDirect() ) )
                return false ;
        }
        return true ;
    }    
    
    // Library code. Update TupleIndexRecord
    // Better - stop when not same
    // Streamization - JDK9 adds takeWhile. 
    public static Iterator<Tuple<NodeId>> scan(Iterator<Tuple<NodeId>> iter, Tuple<NodeId> pattern) {
        return TupleIndex.scan(iter, pattern) ;
    }
    
    public static boolean matchPattern(Tuple<NodeId> pattern, Tuple<NodeId> item) {
        int tupleLength = pattern.len() ; 
        for ( int i = 0 ; i < tupleLength ; i++ ) {
            NodeId n = pattern.get(i) ;
            // The pattern must be null/Any or match the tuple being tested.
            if ( ! NodeId.isAny(n) )
                if ( ! item.get(i).equals(n) ) 
                    return false ;
        }
        return true ;
    }
}
