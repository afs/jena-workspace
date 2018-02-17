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

package rdfconnection;

import java.util.stream.Stream;

import org.apache.http.client.HttpClient;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/** 
 * Implementation of the {@link RDFConnection} interface for connecting to an Apache Jena Fuseki.
 * <p>
 * This adds the ability to work with blank nodes across the network.
 */
public class RDFConnectionFuseki extends RDFConnectionRemote2 {
    // Upgrade QueryEngineHTTP "Accept" setting.
    
    
    // Format for RDF data.
    private static Lang RDFLlang = Lang.RDFTHRIFT;
    // Format for SPARQL Result Sets
    private static Lang RSLang = ResultSetLang.SPARQLResultSetThrift;

    /** Create connection that will use the {@link HttpClient} using URL of the dataset and default service names */
    public RDFConnectionFuseki(HttpClient httpClient, String destination) {
        super(httpClient, destination);
        init();
    }

    /** Create connection, using URL of the dataset and default service names */
    public RDFConnectionFuseki(String destination) {
        super(destination);
        init();
    }

    /** Create connection, using full URLs for services. Pass a null for "no service endpoint". */
    public RDFConnectionFuseki(String sQuery, String sUpdate, String sGSP) {
        super(sQuery, sUpdate, sGSP);
        init();
    }
    
    /** Create connection, using URL of the dataset and short names for the services */
    public RDFConnectionFuseki(String destination, String sQuery, String sUpdate, String sGSP) {
        super(destination, sQuery, sUpdate, sGSP);
        init();
    }
    
    /** Create connection, using URL of the dataset and short names for the services */
    public RDFConnectionFuseki(HttpClient httpClient, String destination, String sQuery, String sUpdate, String sGSP) {
        super(httpClient, destination, sQuery,  sUpdate, sGSP);
        init();
    }
    
    private void init() {
        //Validate the far end.
        
        //See XXX in super
//        setAcceptRDF();
//        setContentTypeRDF(RDFLang.getContentType());
//        setAcceptResultSet
//        setContentTypeResultSet(RSLang.getContentType());
        super.outputQuads = RDFFormat.RDF_THRIFT;
        super.outputTriples = RDFFormat.RDF_THRIFT;
        
        String ctRDFThrift = Lang.RDFTHRIFT.getContentType().getContentType();// XXX getHeaderString
        
        super.acceptGraph = ctRDFThrift;
        super.acceptDataset = ctRDFThrift;
        super.acceptSelectHeader = ResultSetLang.SPARQLResultSetThrift.getHeaderString();
        //super.acceptAskHeader = QueryEngineHTTP.defaultAskHeader();
        super.acceptRDFHeader = ctRDFThrift;
    }

//    // XXX
//    @Override
//    public QueryExecution query(Query query) {
//        checkQuery();
//        return super.query(query);
//    }
//
//    // XXX
//    @Override
//    public void update(UpdateRequest update) {
//        checkUpdate();
//        super.update(update);
//    }
//    
//    @Override
//    protected void doPutPost(String url, String file, Lang lang, boolean replace) {
//        super.doPutPost(url, file, lang, replace);
//    }
//
//    @Override
//    protected void doPutPost(Model model, String name, boolean replace) {
//        super.doPutPost(model, name, replace);
//    }
    
    // Fuseki specific

    /**
     * Return a {@link Model} that is proxy for a remote model in a Fuseki server. This
     * support the model operations of accessing statements and changing the model.
     * <p>
     * This provide low level access to the remote data. The application will be working
     * with and manipulating the remote model directly which may involve a significant
     * overhead for every {@code Model} API operation.
     * <p>
     * <b><em>Warning</em>:</b> This is <b>not</b> performant for bulk changes. 
     * <p>
     * Getting the model, using {@link #fetch()}, which copies the whole model into a local
     * {@code Model} object, maniupulating it and putting it back with {@link #put(Model)}
     * provides another way to work with remote data.
     * 
     * @return Model
     */
    public Model getModelProxy() { return null; }
    public Model getModelProxy(String graphName) { return null; }
    
    public Graph getGraphProxy() { return null; }
    public Graph getGraphProxy(String graphName) { return null; }

    public Dataset getDatasetProxy() { return null; }
    public DatasetGraph getDatasetGraphProxy() { return null; }

    // Or remote RDFStorage?
    public Stream<Triple> findStream(Node s, Node p , Node o) { return null; }
    public Stream<Quad> findStream(Node g, Node s, Node p , Node o) { return null; }

    // Send Patch 
}

