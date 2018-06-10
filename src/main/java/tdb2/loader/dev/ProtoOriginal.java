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

package tdb2.loader.dev;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

public class ProtoOriginal {
    
    public static void main(String[] args) {
        JenaSystem.init();
        // parallel:input1,([SPO],[POS,OSP]),([GSPO],[GPOS,GOSP],[SPOG,PSOG,OSPG])
        
//        String DATA = "/home/afs/Datasets/BSBM/bsbm-200m.nt.gz";
//        if ( args.length > 0 )
//            DATA = args[0];
//        
//        //String DB = "home/afs/disk1/tmp/DB3";
//        String DB = "DB3";
//        
//        LoaderDevTools.reset(DB);
//        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(DB);
//        
//        
//        
//        
//        MonitorOutput output = LoaderOps.outputTo(System.out);  
//        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
//        LoaderPlan loaderPlan = LoaderPlans.loaderPlanPhased;
//        Map<String, TupleIndex> indexMap = indexMap(dsgtdb);
//        
//        // LoaderRecord object?
//        Timer t = new Timer();
//        t.startTimer();
//        
//        /* ******************** */
//        Pair<Long, Long> p = execute(DATA, loaderPlan, dsgtdb, indexMap, output);
//        /* ******************** */
//        
//        long totalElapsed = t.endTimer();
//        if ( output != null ) {
//            long count = p.getLeft(); 
//            String label = "Triples";
//            double seconds = totalElapsed/1000.0;
//            if ( seconds > 1 )
//                output.print("Time = %,.3f seconds : %s = %,d : Rate = %,.0f /s", seconds, label, count, count/seconds);  
//        }
//        
//        // Check answers!
//        Txn.execute(dsg, ()->{
//            LoaderDevTools.query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ; 
//            LoaderDevTools.query("SELECT (count(*) AS ?C) { ?s ?p 1 }", dsg) ;
//        });
    }
    
    /** Indexes to a list of mappings suitable as a label  */
    private static String indexMappings(TupleIndex[] indexes) {
        StringJoiner sj = new StringJoiner(", ");
        Arrays.stream(indexes).map(x->x.getMappingStr()).forEach(str->sj.add(str));
        return sj.toString();
    }

    private static <X> void arrayApply2(X[][] array, Consumer<X> action) {
        if ( array == null )
            return;
        for ( X[] lines : array ) {
            for ( X item : lines ) {
                action.accept(item);
            }
        }
    }
    
    private static <X> void arrayApply1(X[] array, Consumer<X> action) {
        if ( array == null )
            return;
        for ( X item : array ) {
            action.accept(item);
        }
    }

    private static <X> X[] flatten(X[][] array, IntFunction<X[]> generator) {
        return flatten(array).toArray(generator);
    }
    
    private static <X> Stream<X> flatten(X[][] array) {
        if ( array == null )
            return null;
        return Arrays.stream(array).flatMap(Arrays::stream);
//        // Or this arbitrary depth version. 
//        return Arrays.stream(array)
//            .flatMap(obj -> obj instanceof Object[]? flatten((Object[])obj): Stream.of(obj));
//        }
    }

//    //-------------
//    // Generics version.
//    //   Less specific error message
//    //   Fully streamed version does not compile in Eclipse.     
//    private static TupleIndex[][] indexSetsFromNames$(String[][] indexNames, Map<String, TupleIndex> indexMap) {
//        return deepMap(indexNames, indexMap::get, TupleIndex[]::new, TupleIndex[][]::new);  
//    }
//    
//    private static <X,Z> Z[][] deepMap(X[][] array, Function<X, Z> action, IntFunction<Z[]> generator1, IntFunction<Z[][]> generator2) {
//        // Can't get Eclipse to reliably accept the "toArray(generator2)" (problem with generics and [][]?) 
//        //Z[][] z1 = Arrays.stream(array).map(vec->deepMap1(vec, action, generator1)).toArray(generator2);
//        Z[][] result = generator2.apply(array.length);
//        for ( int i = 0 ; i < array.length ; i++ ) {
//            result[i] = deepMap1(array[i], action, generator1);
//        }
//        return result;
//    }
//
//    private static <X,Z> Z[] deepMap1(X[] array, Function<X, Z> action, IntFunction<Z[]> generator) {
//        return Arrays.stream(array)
//            .map(item -> mapItem(item, action))
//            .toArray(generator);
//    }
//    
//    private static <X,Z> Z[] deepMap1(X[] array, Function<X, Z> action, IntFunction<Z[]> generator) {
//        Z[] result = generator.apply(array.length);
//        for ( int i = 0 ; i < array.length ; i++ ) {
//            Z z = mapItem(array[i], action);
//            if ( z == null )
//                throw new IllegalArgumentException("Not found: "+array[i]);
//            result[i] = z;
//        }
//        return result;
//    }
//
//    private static <X, Z> Z mapItem(X x, Function<X, Z> action) {
//        Z z = action.apply(x);
//        if ( z == null )
//            throw new IllegalArgumentException("Not found: "+x);
//        return z;
//    }
    
//    //-------------
}
