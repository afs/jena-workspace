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

package dev;

import java.util.* ;

import org.apache.jena.atlas.lib.SetUtils ;
import org.apache.jena.atlas.lib.Trie ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.shared.PrefixMapping ;

public class EVN752 {
    public static void main(String ...a) {
        Model m = RDFDataMgr.loadModel("D.ttl") ;
        RDFDataMgr.write(System.out, m, Lang.TTL) ;
        System.out.println("-----------------");
        
        // if any prefix does not end on /, # or : 
        
        fastMethod(m) ;
        System.out.println("-----------------");
        fullMethod(m);
        System.out.println("-----------------");
    }
    
    /** Assume that prefixes:localName are in the normal places (after/ or #).
     * i.e.  node.getNameSpace() ; makes sense.
     * @param m
     */
    private static void fastMethod(Model m) {    
        Map<String, String> pmap = m.getNsPrefixMap() ; // prefix -> URI.
        
        Map<String, String> pmapReverse = new HashMap<>() ;
        pmap.forEach((p,u)->pmapReverse.put(u, p)) ;

        // URIs in prfix mapping
        Set<String> prefixURIs = pmapReverse.keySet() ;
        // Prefix URIs in use.
        Set<String> inUse = new HashSet<>() ;
        
        Iterator<Triple> iter = m.getGraph().find(null, null, null) ;
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            process(prefixURIs, inUse, triple.getSubject()) ; 
            process(prefixURIs, inUse, triple.getPredicate()) ;
            process(prefixURIs, inUse, triple.getObject()) ; 
            if ( inUse.size() == prefixURIs.size() )
                // Fast exit.
                break ;
        }
        System.out.println("In use: "+inUse) ;
        inUse.forEach((u)->System.out.printf("    %s: -> <%s>\n", pmapReverse.get(u), u)) ;

        Set<String> s = SetUtils.difference(pmapReverse.keySet(), inUse) ;
        System.out.println("Not in use: "+s) ;
        s.forEach((u)->System.out.printf("    %s: -> <%s>\n", pmapReverse.get(u), u)) ;
        
    }

    private static void process(Set<String> prefixURIs, Set<String> inUse, Node node) {
        if ( ! node.isURI() )
            return ;
        //String uri = node.getURI() ;
        String ns = node.getNameSpace() ;
        if ( ns == null )
            return ;
        if ( prefixURIs.contains(ns) )
            inUse.add(ns) ;
    }
    
    private String namespace(String uri) {
        int idx = Util.splitNamespaceXML(uri) ;
        if ( idx <= 0 )
            // Unsplitable.
            return null ;
        return uri.substring(0, idx) ;
    }
    
    /** Check every URI as a possible use of a prefix */ 
    private static void fullMethod(Model m) {
        Trie<String> trie = new Trie<>() ;
        Map<String, String> map = m.getNsPrefixMap() ;
        map.forEach((p,u)-> trie.add(p, u)) ;
        
        
        PrefixMapping pmap = m ;
        
        Iterator<Triple> iter = m.getGraph().find(null, null, null) ;
        // Prefix URIs in use.
        Set<String> prefixesInUse = new HashSet<>() ;
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            processFull(trie, prefixesInUse, triple.getSubject()) ; 
            processFull(trie, prefixesInUse, triple.getPredicate()) ;
            processFull(trie, prefixesInUse, triple.getObject()) ; 
            //if ( pmap.size() == prefixURIs.size() )
        }
        System.out.println("In use: "+prefixesInUse) ;
        prefixesInUse.forEach((p)->System.out.printf("    %s: -> <%s>\n", p, pmap.getNsPrefixURI(p))) ;
//
//        Set<String> s = SetUtils.difference(pmapReverse.keySet(), inUse) ;
//        System.out.println("Not in use: "+s) ;
//        s.forEach((u)->System.out.printf("    %s: -> <%s>\n", pmapReverse.get(u), u)) ;
    }

    
    private static void processFull(Trie<String> trie, Set<String> prefixesInUse, Node node) {
        if ( ! node.isURI() )
            return ;
        String uri = node.getURI() ;
        // Shorten to "/" or "#" or ":"
        String pref = uri ;
        
        // Get all under the pref
        List<String> hits = trie.partialSearch(pref) ;
        if ( hits == null || hits.isEmpty() )
            return ;
        // ????
        prefixesInUse.addAll(hits) ;
    }
    
    
}
