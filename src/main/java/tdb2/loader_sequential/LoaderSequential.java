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

package tdb2.loader_sequential;

import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.atlas.lib.ProgressMonitor.Output;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.slf4j.Logger;
import tdb2.BulkLoader;
import tdb2.BulkLoaderException;
import tdb2.LoaderBase;
import tdb2.LoaderOps;

/** Simple bulk loader. Algorithm: Parser to dataset. */ 
public class LoaderSequential extends LoaderBase {
    
    private static int DataTickPoint = 100_000;
    private static int DataSuperTick = 10;
    
    private final LoaderNodeTupleTable triplesLoader;
    private final LoaderNodeTupleTable quadsLoader;
    private final Output outputToLog = outputToLog(BulkLoader.LOG);
    private final DatasetGraphTDB dsgtdb;
    private final ProgressMonitor monitor; 

    protected LoaderSequential(DatasetGraph dsg, Node graphName, ProgressMonitor.Output output, boolean showProgress) {
        super(dsg, graphName, showProgress);
        
        if ( ! TDBInternal.isBackedByTDB(dsg) )
            throw new BulkLoaderException("Not a TDB2 database");
        
        this.monitor = /*ProgressMonitor.*/create(outputToLog, "data", BulkLoader.DataTickPoint, BulkLoader.DataSuperTick);
        
        this.dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        this.triplesLoader = new LoaderNodeTupleTable(dsgtdb.getTripleTable().getNodeTupleTable(), "Triples", monitor);
        this.quadsLoader = new LoaderNodeTupleTable(dsgtdb.getQuadTable().getNodeTupleTable(), "Quads", monitor);
    }
    
    // -> ProgressMonitor
    /** ProgressMonitor that outputs to a {@link Logger} */ 
    public static ProgressMonitor create(Output output, String label, long tickPoint, int superTick) {
        return new ProgressMonitor(label, tickPoint, superTick, output) ;
    }
    
    static Output outputToLog(Logger log) { 
        return (fmt, args)-> {
            System.out.printf(fmt, args);
            System.out.println();
//            if ( log != null && log.isInfoEnabled() ) {
//                String str = String.format(fmt, args);
//                log.info(str);
//            }
        } ;
    }

    @Override
    protected StreamRDF createDest(DatasetGraph dsg, Node graphName) {
        StreamRDF s = StreamRDFLib.dataset(dsg);
        s = new StreamRDFWrapper(s) {
            // XXX Tidy up!
            @Override
            public void triple(Triple triple) {
                triplesLoader.load(triple.getSubject(), triple.getPredicate(), triple.getObject());
            }

            @Override
            public void quad(Quad quad) {
                quadsLoader.load(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
            }

            @Override
            public void base(String base) {}

//            @Override
//            public void prefix(String prefix, String iri) {
//                dsg.getPrefixes().getPrefixMapping().setNsPrefix(prefix, iri);
//            }
        };    
        
        
        return LoaderOps.toNamedGraph(s, graphName);
    }
    
    @Override
    public void startBulk() {
        super.startBulk();
        //dsgtdb.getTxnSystem().getTxnMgr().startExclusiveMode();
        monitor.startMessage("START");
        monitor.start();
        triplesLoader.loadDataStart();
        quadsLoader.loadDataStart();
    }

    @Override
    public void finishBulk() {
        triplesLoader.loadDataFinish();
        quadsLoader.loadDataFinish();
        monitor.finish();
        monitor.finishMessage();
        super.finishBulk();
        //dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }

    @Override
    public void finishException() {
        super.finishException();
        //dsgtdb.getTxnSystem().getTxnMgr().finishExclusiveMode();
    }

    @Override
    protected void loadOne(StreamRDF dest, String filename) {
        LoaderOps.inputFile(dest, filename, showProgress, DataTickPoint, DataSuperTick);
    }

    @Override
    public boolean bulkUseTransaction() {
        return true;
    }
}
