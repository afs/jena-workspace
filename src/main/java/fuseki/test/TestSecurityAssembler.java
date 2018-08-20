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

package fuseki.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import fuseki.security.DataAccessControlledFuseki;
import fuseki.security.SecurityRegistry;
import fuseki.security.VocabSecurity;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.junit.Test;

public class TestSecurityAssembler {
    // If this set of tests is run on its own ...
    static { LogCtl.setLog4j("log4j-testing.properties"); }
    
    static final String DIR = "testing/FusekiEmbedded/";
    
    @Test public void assembler1() { 
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem-security.ttl", VocabSecurity.tAccessControlledDataset);
    }
    
    @Test public void assembler2() { 
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem-security.ttl", VocabSecurity.tAccessControlledDataset);
        SecurityRegistry securityRegistry = ds.getContext().get(VocabSecurity.symSecurityRegistry);
        // XXX check registry
    }
    
    @Test public void assembler3() {
        VocabSecurity.init();
        int port = FusekiLib.choosePort();
        AtomicReference<String> user = new AtomicReference<>();
        
        FusekiServer server = 
            FusekiServer.create().port(port).parseConfigFile(DIR+"assem-security.ttl").build();
        // User via access AtomicReference
        DataAccessControlledFuseki.enable(server, (a)->user.get());
        
        //Add data
        DatasetGraph dsg = server.getDataAccessPointRegistry().get("/database").getDataService().getDataset();
        Txn.executeWrite(dsg,  ()->{
            dsg.add(SSE.parseQuad("(<http://host/graphname1> :s1 :p :o)"));
            dsg.add(SSE.parseQuad("(<http://host/graphname3> :s3 :p :o)"));
            dsg.add(SSE.parseQuad("(<http://host/graphname9> :s9 :p :o)"));
        });
        server.start();
        
        try {
            String url = "http://localhost:"+port+"/database";
            
            Node s1 = SSE.parseNode(":s1"); 
            Node s3 = SSE.parseNode(":s3");
            Node s9 = SSE.parseNode(":s9"); 
            
            user.set("user1");
            try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
                Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
                assertSeen(visible, s1, s3);
            }
            
            user.set("userX"); // No such user in the registry
            try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
                Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
                assertSeen(visible);
            }
            user.set(null);
            try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
                Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
                assertSeen(visible);
            }
            
            user.set("user2");
            try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
                Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
                assertSeen(visible, s9);
            }
            
            user.set("userZ"); // No graphs with data.
            try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
                Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
                assertSeen(visible);
            }
            
            // Access the uncontrolled dataset.
            user.set(null);
            String plainUrl = "http://localhost:"+port+"/plain";
            try(RDFConnection conn = RDFConnectionFactory.connect(plainUrl)) {
                conn.update("INSERT DATA { <x:s> <x:p> 123 , 456 }");
                conn.queryResultSet("SELECT * { ?s ?p ?o }",
                    rs->{
                        int x = ResultSetFormatter.consume(rs);
                        assertEquals(2, x);
                    });
            }
        } finally { server.stop(); }
    }

    private static void assertSeen(Set<Node> visible, Node ... expected) {
        Set<Node> expectedNodes = new HashSet<>(Arrays.asList(expected));
        assertEquals(expectedNodes, visible);
    }
    
    private Set<Node> query(RDFConnection conn, String queryString) {
        Set<Node> results = new HashSet<>();
        conn.queryResultSet(queryString, rs->{
            List<QuerySolution> list = Iter.toList(rs);
            list.stream()
            .map(qs->qs.get("s"))
            .filter(Objects::nonNull)
            .map(RDFNode::asNode)
            .forEach(n->results.add(n));
        });
        return results;
    }
}
