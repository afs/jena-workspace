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

package tdb2;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.ProgressStreamRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;

/** Simple bulk loader. Algorithm: Parser to dataset. */ 
public class LoaderSequential implements Loader {

    private final DatasetGraph dsg;
    private final StreamRDF dest;
    
    public LoaderSequential(DatasetGraph dsg) {
        this.dsg = dsg;
        this.dest = StreamRDFLib.dataset(dsg);
    }
    
    @Override
    public void startBulk() {
        dsg.begin(TxnType.WRITE);
    }

    @Override
    public void finishBulk() {
        dsg.commit();
        dsg.end();
    }

    @Override
    public void finishException() {
        dsg.abort();
        dsg.end();
    }

    @Override
    public boolean inTransaction() {
        return false;
    }

    @Override
    public void loadOne(String filename) {
        loadOne(dest, filename, true);
    }

//    @Override
//    public void load(List<String> filenames) {
//        load(dsg, filenames, true, false);
//    }
//    
//    private static void load(DatasetGraph dsg, List<String> urls, boolean showProgress, boolean generateStats) {
//        StreamRDF dest = StreamRDFLib.dataset(dsg);
//        Txn.executeWrite(dsg, ()->urls.forEach( (x)->loadOne(dest, x, showProgress) ));
//    }

//    public static void load(Graph graph, List<String> urls, boolean showProgress) {
//        StreamRDF dest = StreamRDFLib.graph(graph);
//        graph.getTransactionHandler().execute(()->urls.forEach((x) -> loadOne(dest, x, showProgress)));
//    }
    
    private static void loadOne(StreamRDF dest, String filename, boolean showProgress) {
        StreamRDF sink = dest;
        ProgressMonitor monitor = null;
        if ( showProgress ) { 
            String basename = FileOps.splitDirFile(filename).get(1);
            monitor = ProgressMonitor.create(BulkLoader.LOG, basename, BulkLoader.DataTickPoint, BulkLoader.DataSuperTick); 
            sink = new ProgressStreamRDF(sink, monitor);
        }
        if ( monitor!= null )
            monitor.start();
        sink.start();
        RDFDataMgr.parse(sink, filename);
        sink.finish();
        if ( monitor!= null ) {
            monitor.finish();
            monitor.finishMessage();
        }
    }

}
