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

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.engine.main.VarFinder;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;

public class Dev
{
    static { LogCtl.setLog4j(); }
    
    public static void mainFind(String[] args) {
        Triple t1 = Triple.ANY;
        Triple t2 = SSE.parseTriple("(:s :p :o)"); 
        System.out.println(t1);
        System.out.println(t2);
        System.out.println(t1.matches(t2));
        System.out.println(t2.matches(t1));
        
        Triple t3 = SSE.parseTriple("(?s ?p ?o)");
    }
        
    // RDFConnection?
    public static void main(String[] args) {
        
        FusekiServer.create().setPort(3333).add("/ds", DatasetGraphFactory.createTxnMem()).build().start();
        
        String qs = "JSON { 'abc': ?X } WHERE {BIND (now() As ?X)}";
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ);
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService("http://localhost:3333/ds", query)) {
            QueryExecUtils.executeQuery(qExec);
        }
        
        try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:3333/ds") ) {
            QueryExecUtils.executeQuery(conn.query(query));
        }
        
        System.exit(0);
    }
    
    // And remove assumeTrue is jena-text tests.
    // QueryExecutionBase takes care of dsg==null.
    /* In tests:
     * If the query has FROM etc and no dataset,
     *   loaded in QueryExecutionFactory.create(query) ->
     *       -> QueryExecutionBase.getPlan
     *         -> QueryExecutionBase.prepareDataset
     *           ->DatasetUtils.createDatasetGraph
     *           Not in QueryEngineBase
     */
    
    public static void mainFROM(String[] args) {
        
        
//        TestSuite ts1 = new TestSuite("DevFROM") ;
//        ts1.addTest(ScriptTestSuiteFactory.make("/home/afs/ASF/afs-jena/jena-arq/testing/DAWG-Final/dataset/manifest.ttl")) ;
//        SimpleTestRunner.runAndReport(ts1);
//        System.exit(0);
        
        // Plain DSG + FROM.
        DatasetGraph dsgPlain = DatasetGraphFactory.createTxnMem();
        DatasetGraph dsgTIM = DatasetGraphFactory.create();
        DatasetGraph dsgTDB1 = TDBFactory.createDatasetGraph();
        DatasetGraph dsgTDB2 = DatabaseMgr.createDatasetGraph();

        
        dwim0("null");
        dwim("Plain", dsgPlain);
        dwim("TIM",  dsgTIM);
        dwim("TDB1", dsgTDB1);
        dwim("TDB2", dsgTDB2);
    }
     
    static void dwim(String label, DatasetGraph dsg) {
        System.out.println(label);
        Quad quad = SSE.parseQuad("(:g :s rdf:label 'abc')");
        Txn.executeWrite(dsg, ()->dsg.add(quad)); 
        String qs = StrUtils.strjoinNL(
            "PREFIX     : <http://example/>", 
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>", 
            "PREFIX text: <http://jena.apache.org/text#>",
            "",
            "SELECT * ",
            "FROM :g",
            "{", 
            "    { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } }",
            "}"); 

        Dataset ds = DatasetFactory.wrap(dsg);
        Query q = QueryFactory.create(qs);
//        if ( q.hasDatasetDescription()  )
//            ds = DynamicDatasets.dynamicDataset(q.getDatasetDescription(), ds, false ) ;

        Txn.executeRead(dsg, () -> {
            try (QueryExecution qExec = QueryExecutionFactory.create(q, ds)) {
                QueryExecUtils.executeQuery(qExec);
            }
        });
    }
    
    static void dwim0(String label) {
        System.out.println(label);
        String qs = StrUtils.strjoinNL(
            "PREFIX     : <http://example/>", 
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>", 
            "PREFIX text: <http://jena.apache.org/text#>",
            "",
            "SELECT * ",
            "{", 
            "    VALUES ?x { 1 2 }",
            "}"); 

        Query q = QueryFactory.create(qs);
        try (QueryExecution qExec = QueryExecutionFactory.create(q)) {
            QueryExecUtils.executeQuery(qExec);
        }
    }
    
    private static void dwim(String str) {
        Op op = SSE.parseOp(str);
        System.out.println(op);
        JoinClassifier.print = true;
        boolean b = JoinClassifier.isLinear((OpJoin)op);
        System.out.println(b);
    }

    public static void main1(String ... a) {     
        Op op1 = SSE.parseOp("(filter (notexists (bgp (triple ?x <my:property> ?z))) (bgp (?x rdf:type :Class)) )");
        //Op op = SSE.parseOp("(filter (> ?x 1) (table unit) )");
        Op op2 = SSE.parseOp("(filter (exprlist (notexists (bgp (triple ?x <my:property> ?z))) (coalesce true ?y)) (bgp (?x rdf:type :Class)))");
        
        VarFinder vf1 = VarFinder.process(op1);
        vf1.print(System.out) ;
        VarFinder vf2 = VarFinder.process(op2);
        vf2.print(System.out) ;
        
//        Expr expr = SSE.parseExpr("(notexists (bgp (triple ?x <my:property> ?z)))");
//        //Expr expr = SSE.parseExpr("(> ?x 1)");
//        
//        Set<Var> vars = ExprVars.getNonOpVarsMentioned(expr);
//        vars = ExprVars.getVarsMentioned(expr);
//        System.out.println(vars);
    }

   
}
