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

package tdb2.loader.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.sys.TDBInternal;
import tdb2.loader.Loader;
import tdb2.loader.base.LoaderBase;
import tdb2.loader.base.LoaderOps;
import tdb2.loader.base.MonitorOutput;

/**
 * The parallel Loader.
 * <p>
 * The process is:
 * <blockquote>
 * {@code DataBatcher -> DataToTuples -> Indexer}
 * <blockquote>
 * {@link DataBatcher} produces {@link DataBlock}s - grouping of triples and quads. It uses 
 * <br/>
 * {@link DataToTuples} processes {@link DataBlock} to create 2 outputs blocks of {@code Tuple<NodeId>}, one output for triples, oue for quads.
 * <br/>
 * {@link Indexer} processes blocks of {@code Tuple<NodeId>} (of the same tuple length) and writes them to a number of indexes.
 */
public class LoaderParallel extends LoaderBase implements Loader {
    public static final int DataTickPoint   = 100_000;
    public static final int DataSuperTick   = 10;
    public static final int IndexTickPoint  = 1_000_000;
    public static final int IndexSuperTick  = 10;
    
    private final DatasetGraphTDB dsgtdb;

    private StreamRDF stream;
    private DataBatcher dataBatcher;
    private DataToTuples dtt;
    private Indexer indexer3;
    private Indexer indexer4;
    private List<BulkStartFinish> process = new ArrayList<>();
    
    public LoaderParallel(DatasetGraph dsg, MonitorOutput output) {
        this(dsg, null, output);
    }
    
    public LoaderParallel(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        super(dsg, graphName, output);
        dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        
        BiConsumer<String, String> prefixHandler = (prefix, uristr) -> {
            // Transactions
            output.print("PREFIX %s: %s\n", prefix, uristr); 
        };
        
        

        // Onethread input stage
        // Contains a splitter of Tuples -> Tuples per index.
        indexer3 = new Indexer(output, dsgtdb.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes());
        indexer4 = new Indexer(output, dsgtdb.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes());

        process.add(indexer3);
        process.add(indexer4);
        
        Destination<Tuple<NodeId>> functionIndexer3 = indexer3.index();
        Destination<Tuple<NodeId>> functionIndexer4 = indexer4.index();
        
        dtt = new DataToTuples(dsgtdb, functionIndexer3, functionIndexer4, output);
        Destination<Triple> dest3 = dtt.dataTriples();
        Destination<Quad> dest4 = dtt.dataQuads();
        dataBatcher = new DataBatcher(dest3, dest4, output, prefixHandler);
        StreamRDF baseInput = dataBatcher;
        process.add(dtt);
        process.add(dataBatcher);
        
//      DataToTuplesInline dttInline = new DataToTuplesInline(dsgtdb, functionIndexer3, functionIndexer4, output);
//      StreamRDF baseInput = dttInline;
//        process.add(dttInline);
        
        stream = LoaderOps.toNamedGraph(baseInput, graphName);
    }
    
    @Override
    public StreamRDF stream() {
        return stream;
    }
    
    @Override
    public boolean bulkUseTransaction() {
        // Manipulate the transactions directly by component. 
        return false;
    }

    @Override
    public void startBulk() {
        // Lock everyone else out while we multithread.
        dsgtdb.getTxnSystem().getTxnMgr().startExclusiveMode();
        super.startBulk();
        BulkProcesses.start(process);
    }

    @Override
    public void finishBulk() {
        BulkProcesses.finish(process);
        super.finishBulk();
        dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }
    
    @Override
    public void finishException() {
        dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }
    
    @Override
    public long countTriples() {
        return dataBatcher.countTriples();
    }

    @Override
    public long countQuads() {
        return dataBatcher.countQuads();
    }

    @Override
    protected void loadOne(String filename) {
        LoaderOps.inputFile(stream, filename, output, DataTickPoint, DataSuperTick);
    }
}
