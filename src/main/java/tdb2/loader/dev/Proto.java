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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.loader.BulkLoaderException;
import org.apache.jena.tdb2.loader.Loader;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.loader.base.ProgressMonitor;
import org.apache.jena.tdb2.loader.base.ProgressMonitorFactory;
import org.apache.jena.tdb2.loader.parallel.*;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.sys.TDBInternal;

public class Proto {
    
    static LoaderPlan loaderPlanPhased = new LoaderPlan(
        true,
        new String[]{ "SPO" },
        new String[]{ "GSPO" },
        new String[][]{ { "POS", "OSP" } },
        new String[][]{ {"GPOS", "GOSP"}, {"SPOG", "POSG", "OSPG"} }
        );
    
    static LoaderPlan loaderPlanAll = new LoaderPlan(
        true,
        new String[]{ "SPO", "POS", "OSP" },
        new String[]{ "GSPO", "GPOS", "GOSP", "SPOG", "POSG", "OSPG" },
        new String[][]{ },
        new String[][]{ }
        );
    
    public static void main(String[] args) {
        
        // parallel:input1,([SPO],[POS,OSP]),([GSPO],[GPOS,GOSP],[SPOG,PSOG,OSPG])
        
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        String DATA = "/home/afs/Datasets/BSBM/bsbm-50k.nt.gz";
        
        // Basic!
        Loader.read(dsg, DATA);
        
        MonitorOutput output = LoaderOps.outputTo(System.out);  
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        LoaderPlan loaderPlan = loaderPlanPhased;
        
        execute(loaderPlan, dsgtdb, output);
    }
    
    /** Execute a {@link LoaderPlan} */
    private static void execute(LoaderPlan loaderPlan, DatasetGraphTDB dsgtdb, MonitorOutput output) {
        Map<String, TupleIndex> indexMap = indexMap(dsgtdb);
        checkLoaderPlan(loaderPlan, indexMap);
        
        // XXX
        //executeData(loaderPlan, dsgtdb, output)
        // -> count of triples and quads.
        
        boolean doTriples = true;
        boolean doQuads = true;
        
        if ( doTriples ) {
            TupleIndex srcIdx3 = findInIndexMap(loaderPlan.primaryLoad3()[0], indexMap);
            TupleIndex[][] indexSets3 = indexSetsFromNames(loaderPlan.secondaryIndex3(), indexMap);
            executeSecondary(srcIdx3, indexSets3, dsgtdb, output);
        }
        
        if ( doQuads ) {
            TupleIndex srcIdx4 = findInIndexMap(loaderPlan.primaryLoad4()[0], indexMap);
            TupleIndex[][] indexSets4 = indexSetsFromNames(loaderPlan.secondaryIndex4(), indexMap);
            executeSecondary(srcIdx4, indexSets4, dsgtdb, output);
        }
    }
    
    /** Execute data ingestion and primary index building of a {@link LoaderPlan} */
    private static void executeData(LoaderPlan loaderPlan, DatasetGraphTDB dsgtdb, MonitorOutput output) {
        loaderPlan.primaryLoad3();
        loaderPlan.primaryLoad4();
        loaderPlan.mulithreadedInput();
        throw new NotImplemented();
    }
    
    /** Execute secondry index building of a {@link LoaderPlan} */
    private static void executeSecondary(TupleIndex srcIdx, TupleIndex[][] indexSets, DatasetGraphTDB dsgtdb, MonitorOutput output) {
        
        List<BulkStartFinish> processes = new ArrayList<>();
        output.print("Start replay index %s", srcIdx.getName());
        // For each phase.
        for ( TupleIndex[] indexes : indexSets ) {
            if ( indexes.length == 0 )
                // Nothing in this phase. 
                continue;
            indexPhase(processes, srcIdx, indexes, output);
            // processes - wait now or wait later?
        }
        // Now make sure they are flushed.
        BulkProcesses.finish(processes);
    }

    private static Map<String, TupleIndex> indexMap(DatasetGraphTDB dsgtdb) {
        Map<String, TupleIndex> indexMap = new HashMap<>();
        // All triple/quad indexes.
        Arrays.stream(dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes())
              .forEach(idx->indexMap.put(idx.getName(), idx));
        Arrays.stream(dsgtdb.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes())
              .forEach(idx->indexMap.put(idx.getName(), idx));
        return indexMap;
    }
    
    private static TupleIndex[][] indexSetsFromNames(String[][] indexNames, Map<String, TupleIndex> indexMap) {
        // Bad error message!
        //return deepMap(indexNames, indexMap::get, TupleIndex[]::new, TupleIndex[][]::new);  
        TupleIndex[][] z = Arrays.stream(indexNames)
            .map(indexSetNames->indexSetFromNames(indexSetNames, indexMap))
            .toArray(TupleIndex[][]::new);
        return z;
    }
    
    private static TupleIndex[] indexSetFromNames(String[] indexNames, Map<String, TupleIndex> indexMap) {
        return Arrays.stream(indexNames)
            .map(name-> findInIndexMap(name, indexMap))
            .toArray(TupleIndex[]::new);
    }

    private static TupleIndex findInIndexMap(String name,Map<String, TupleIndex> indexMap) {
        TupleIndex tIdx = indexMap.get(name);
        if ( tIdx == null )
            throw new IllegalArgumentException("No such index: "+name);
        return tIdx;
    }
    
    private static void indexPhase(List<BulkStartFinish> processes, TupleIndex srcIdx, TupleIndex[] indexes, MonitorOutput output) {
        String indexSetLabel = indexMappings(indexes);
        output.print("Index set:  %s => %s", srcIdx.getName(), indexSetLabel);
        Indexer indexer = new Indexer(output, indexes);
        Destination<Tuple<NodeId>> dest = indexer.index();
        indexer.startBulk();
        TransactionCoordinator coordinator = CoLib.newCoordinator();
        CoLib.add(coordinator, srcIdx);
        CoLib.start(coordinator);
        // READ transaction.
        Transaction transaction = coordinator.begin(TxnType.READ);
        // Add to processes - we can wait later if we do not touched indexes being built.
        processes.add(indexer);
        replay(srcIdx, dest, output);
        // End read tranaction on srcIdx
        transaction.end();
        output.print("End index set %s => %s", srcIdx.getName(), indexSetLabel);
    }
  
    /** Check the loader plan makes sense. */ 
    private static void checkLoaderPlan(LoaderPlan loaderPlan, Map<String, TupleIndex> indexMap) {
        Consumer<String> checker3 = name -> {
            if ( name == null ) throw new BulkLoaderException("Null index name");
            if ( name.length() != 3 ) throw new BulkLoaderException("Bad length (expected 3): "+name);
            if ( !indexMap.containsKey(name) ) throw new BulkLoaderException("No such index: "+name);
        };
        Consumer<String> checker4 = name -> {
            if ( name == null ) throw new BulkLoaderException("Null index name");
            if ( name.length() != 4 ) throw new BulkLoaderException("Bad length (expected 4): "+name);
            if ( !indexMap.containsKey(name) ) throw new BulkLoaderException("No such index: "+name);
        };
        
        // -- Checking for nulls and bad index names.
        arrayApply1(loaderPlan.primaryLoad3(), checker3);
        arrayApply1(loaderPlan.primaryLoad4(), checker4);
        
        arrayApply2(loaderPlan.secondaryIndex3(), checker3);
        arrayApply2(loaderPlan.secondaryIndex4(), checker4);
        
        // -- Checking for duplicates
        checkUnique("Primary triples",loaderPlan.primaryLoad3());
        checkUnique("Primary quads", loaderPlan.primaryLoad4());
        
        String[] secondary3 = flatten(loaderPlan.secondaryIndex3(), String[]::new);
        String[] secondary4 = flatten(loaderPlan.secondaryIndex4(), String[]::new);
        
        checkUnique("Secondary triples", secondary3);
        checkUnique("Secondary quads", secondary4);
    }

    private static <X> void checkUnique(String errorMessage, X[] array) {
        Set<X> set = new HashSet<>();
        for ( X x : array ) {
            if ( set.contains(x) )
                throw new BulkLoaderException(errorMessage+" : Not unique: "+x);
            set.add(x);
        }
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
    
    static final int TupleTickPoint = 1_000_000;
    static final int TupleSuperTick = 10;
    
    static void replay(TupleIndex srcIdx, Destination<Tuple<NodeId>> dest, MonitorOutput output) {
        ProgressMonitor monitor = 
            ProgressMonitorFactory.progressMonitor("Index", output, TupleTickPoint, TupleSuperTick);
        
        List<Tuple<NodeId>> block = null;

        int len = srcIdx.getTupleLength();
        
        monitor.start();
        Iterator<Tuple<NodeId>> iter = srcIdx.all();
        while (iter.hasNext()) {
            if ( block == null )
                block = new ArrayList<>(LoaderConst.ChunkSize);
            Tuple<NodeId> row = iter.next();
            block.add(row);
            monitor.tick();
            if ( block.size() == LoaderConst.ChunkSize ) {
                dest.deliver(block);
                block = null;
            }
        }
        if ( block != null )
            dest.deliver(block);
        dest.deliver(Collections.emptyList());
        monitor.finish();
        monitor.finishMessage("Tuples["+len+"]");
    }
}
