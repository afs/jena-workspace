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

package tdb2.early;

import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.atlas.lib.ProgressMonitor.Output;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.ProgressStreamRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.slf4j.Logger;
import tdb2.BulkLoader;
import tdb2.loader_parallel.BulkStreamRDF;
import tdb2.loader_sequential.LoaderNodeTupleTable;

/** 
 * TDB2 Bulk loader - pure java, TDB1 style.
 */
public class BulkLoaderJava {
    public static BulkStreamRDF create(DatasetGraphTDB dsg) {
        if ( dsg.isEmpty() ) {
            return new LoaderEmpty(dsg);
        } else {
            return new LoaderIncremental(dsg);
        }
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
    
    // -> ProgressMonitor
    /** ProgressMonitor that outputs to a {@link Logger} */ 
    public static ProgressMonitor create(Output output, String label, long tickPoint, int superTick) {
        return new ProgressMonitor(label, tickPoint, superTick, output) ;
    }
    
    // Load from empty
    static class LoaderEmpty implements BulkStreamRDF {

        private final DatasetGraphTDB dsg;
        private final LoaderNodeTupleTable triplesLoader;
        private final LoaderNodeTupleTable quadsLoader;
        private final Output outputToLog = outputToLog(BulkLoader.LOG); 
        private final ProgressMonitor monitor;

        public LoaderEmpty(DatasetGraphTDB dsg) {
            this.dsg = dsg;
            
            monitor = /*ProgressMonitor.*/create(outputToLog, "data", BulkLoader.DataTickPoint, BulkLoader.DataSuperTick);
            triplesLoader = new LoaderNodeTupleTable(dsg.getTripleTable().getNodeTupleTable(), "Triples", monitor);
            quadsLoader = new LoaderNodeTupleTable(dsg.getQuadTable().getNodeTupleTable(), "Quads", monitor);
                                                    
//            dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes();
//            dsg.getTripleTable().getNodeTupleTable().getNodeTable();
//
//            dsg.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes();
//            dsg.getQuadTable().getNodeTupleTable().getNodeTable();
        }
        
        @Override
        public void start() {
            monitor.startMessage();
            monitor.start();
            triplesLoader.loadDataStart();
            quadsLoader.loadDataStart();
        }

        @Override
        public void finish() {
            triplesLoader.loadDataFinish();
            quadsLoader.loadDataFinish();
            monitor.finish();
            monitor.finishMessage();
        }

        @Override
        public void startBulk() {}

        @Override
        public void finishBulk() {}

        // Batch?
        
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

        @Override
        public void prefix(String prefix, String iri) {
            dsg.getPrefixes().getPrefixMapping().setNsPrefix(prefix, iri);
        }
    }
    
    // Load into a databse which already has data in it.
    static class LoaderIncremental implements BulkStreamRDF {
        private final StreamRDF stream;
        private final DatasetGraph dsg;
        private final ProgressMonitor monitor;

        LoaderIncremental(DatasetGraphTDB dsg) {
            this.dsg = dsg;
            StreamRDF s = StreamRDFLib.dataset(dsg);
            ProgressMonitor progress = ProgressMonitor.create(BulkLoader.LOG, "TDB2", BulkLoader.DataTickPoint, BulkLoader.DataSuperTick); 
            StreamRDF s2 = new ProgressStreamRDF(s, progress);
            this.stream = s2;
            this.monitor = progress;
        }

        // Delayed setup StreamRDFWrapper.
        
        @Override
        public void start() {
            monitor.startMessage();
            monitor.start();
            stream.start();
        }

        @Override
        public void finish() {
            stream.finish();
            monitor.finish();
            monitor.finishMessage();
        }

        @Override
        public void startBulk() {}

        @Override
        public void finishBulk() {}

        @Override
        public void triple(Triple triple) {
            stream.triple(triple);
        }

        @Override
        public void quad(Quad quad) {
            stream.quad(quad);
        }

        @Override
        public void base(String base) {
            stream.base(base);
        }

        @Override
        public void prefix(String prefix, String iri) {
            stream.prefix(prefix, iri);
        }
    }
}
