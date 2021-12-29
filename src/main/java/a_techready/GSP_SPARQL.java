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

package a_techready;

public class GSP_SPARQL {}
//
//import org.apache.jena.fuseki.main.FusekiServer;
//import org.apache.jena.fuseki.system.FusekiLogging;
//import org.apache.jena.graph.Graph;
//import org.apache.jena.graph.Node;
//import org.apache.jena.graph.NodeFactory;
//import org.apache.jena.riot.Lang;
//import org.apache.jena.riot.RDFDataMgr;
//import org.apache.jena.sparql.core.DatasetGraph;
//import org.apache.jena.sparql.core.DatasetGraphFactory;
//import org.apache.jena.sparql.core.Quad;
//import org.apache.jena.sparql.exec.RowSet;
//import org.apache.jena.sparql.exec.http.*;
//import org.apache.jena.sparql.graph.GraphFactory;
//import org.apache.jena.sparql.modify.request.*;
//import org.apache.jena.sparql.sse.SSE;
//import org.apache.jena.update.Update;
//import org.apache.jena.update.UpdateRequest;
//
///**
// * GSP operations translated to SPARQL.
// * <p>
// * Use {@link GSP_SPARQL#service} to build a GSP request that will be translated to
// * SPARQL Update and SPARQL Query when executed.
// */
//public class GSP_SPARQL extends GSP {
//    public static void main(String ...args) {
//        try {
//            Graph graph = SSE.parseGraph("(graph (:s :p 123) (:s :q 456))");
//
//            {
//                DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
//                FusekiLogging.setLogging();
//                FusekiServer.create().add("/ds", dsg).build().start();
//            }
//            String graphName = "http://ex/gn";
//            String serviceURL = "http://localhost:3330/ds";
//
//            GSP_SPARQL.service(serviceURL).PUT(graph);
//            Graph graph2 = GSP_SPARQL.service(serviceURL).defaultGraph().GET();
//            RDFDataMgr.write(System.out, graph2, Lang.TTL);
//
//            GSP_SPARQL.service(serviceURL).clearDataset();
//
//            DatasetGraph data = SSE.parseDatasetGraph("(dataset (_ :s :p 123) (:g :s :q 456))");
//            GSP_SPARQL.service(serviceURL).putDataset(data);
//            DatasetGraph dsg2 = GSP_SPARQL.service(serviceURL).getDataset();
//            RDFDataMgr.write(System.out, dsg2,  Lang.NQ);
//
//        } catch (Throwable th) {
//            th.printStackTrace();
//        } finally { System.exit(0); }
//    }
//
//    // ----
//
//    public static GSP_SPARQL service(String service) {
//        GSP_SPARQL x = GSP_SPARQL.request();
//        x.endpoint(service);
//        return x;
//    }
//
//    public static GSP_SPARQL request() {
//        return new GSP_SPARQL();
//    }
//
//    @Override
//    public Graph GET() {
//        if ( isDefaultGraph() ) {
//
//        }
//
//        String qs = isDefaultGraph()
//                ? "SELECT REDUCED ?s ?p ?o WHERE { ?s ?p ?o }"
//                : "SELECT REDUCED ?s ?p ?o WHERE { GRAPH <"+graphName()+"> { ?s ?p ?o } }";
//
//
//
//         //QueryExec.service(service()).query(qs).select();
//        QueryExecHTTPBuilder builder = QueryExecHTTP.service(service()).query(qs);
//        if ( super.httpClient() != null )
//            builder.httpClient(super.httpClient());
//        httpHeaders(super::httpHeader);
//        RowSet rs = builder.select();
//
//
//        Graph graph = GraphFactory.createDefaultGraph();
//        rs.forEachRemaining(b->{
//            graph.add(b.get("s"), b.get("p"), b.get("o"));
//        });
//        return graph;
//        //        String qs = ( graphName == null )
//        //                ? "CONSTRUCT WHERE { ?s ?p ?o }"
//        //                : "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <"+graphName+"> { ?s ?p ?o } }";
//        //        return QueryExec.service(service()).query(qs).construct();
//    }
//
//    @Override
//    public void POST(String file) {
//        throw new UnsupportedOperationException("Directly sending data from a file is not supported.");
//    }
//
//    @Override
//    public void POST(Graph graph) {
//        graph_execPutPost(graph, false);
//    }
//
//    @Override
//    public void PUT(String file) {
//        throw new UnsupportedOperationException("Directly sending data from a file is not supported.");
//    }
//
//    @Override
//    public void PUT(Graph graph) {
//        graph_execPutPost(graph, true);
//    }
//
//    @Override
//    public void DELETE() {
//        UpdateRequest request = new UpdateRequest();
//        Node gn = null;
//        if ( isDefaultGraph() )
//            request.add(new UpdateDrop(Target.DEFAULT, true));
//        else
//            request.add(new UpdateClear(gn, true));
//        execUpdate(request);
//    }
//
//    @Override
//    public DatasetGraph getDataset() {
//        // Strict SPARQL.
//        String qs = "SELECT REDUCED ?g ?s ?p ?o WHERE { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }";
//        RowSet rs = QueryExecHTTP.newBuilder().endpoint(service())
//            //.httpClient(super.httpClient())
//            // Headers
//            .query(qs)
//            .select();
//
//        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
//        rs.forEachRemaining(b->{
//            if ( b.contains("g") ) {
//                dsg.add(b.get("g"), b.get("s"), b.get("p"), b.get("o"));
//            } else {
//                dsg.getDefaultGraph().add(b.get("s"), b.get("p"), b.get("o"));
//            }
//        });
//        return dsg;
//    }
//
//    @Override
//    public void postDataset(String file) {
//        throw new UnsupportedOperationException("Directly sending data from a file is not supported.");
//    }
//
//    @Override
//    public void postDataset(DatasetGraph dsg) {
//        dsg_execPutPost(dsg, false);
//    }
//
//    @Override
//    public void putDataset(String file) {
//        throw new UnsupportedOperationException("Directly sending data from a file is not supported.");
//    }
//
//    @Override
//    public void putDataset(DatasetGraph dsg) {
//        dsg_execPutPost(dsg, true);
//    }
//
//    @Override
//    public void clearDataset() {
//        UpdateRequest request = new UpdateRequest();
//        request.add(new UpdateClear(Target.ALL));
//        execUpdate(request);
//    }
//
//    private void graph_execPutPost(Graph graph, boolean incDrop) {
//        UpdateRequest request = new UpdateRequest();
//        Node gn = isDefaultGraph()
//                ? Quad.defaultGraphNodeGenerated
//                : NodeFactory.createURI(graphName());
//
//        if ( incDrop ) {
//            Update upEmpty = ( graphName() == null )
//                    ? new UpdateDrop(Target.DEFAULT, true)
//                    : new UpdateClear(gn, true);
//            request.add(upEmpty);
//        }
//
//        QuadDataAcc acc  = new  QuadDataAcc();
//        graph.stream().map(t->Quad.create(gn, t)).forEach(acc::addQuad);
//        request.add(new UpdateDataInsert(acc));
//        execUpdate(request);
//    }
//
//    private void dsg_execPutPost(DatasetGraph dsg, boolean incClear) {
//        UpdateRequest request = new UpdateRequest();
//        if ( incClear )
//            request.add(new UpdateClear(Target.ALL));
//        QuadDataAcc acc  = new  QuadDataAcc();
//        dsg.find().forEachRemaining(acc::addQuad);
//        request.add(new UpdateDataInsert(acc));
//        execUpdate(request);
//    }
//
//    private void execUpdate(UpdateRequest request) {
//        //.httpClient(super.httpClient()) + headers
//        UpdateExecHTTPBuilder builder = UpdateExecHTTP.service(service()).update(request);
//        if ( super.httpClient() != null )
//            builder.httpClient(super.httpClient());
//        httpHeaders(super::httpHeader);
//        builder.execute();
//    }
//}
