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
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.optimize.TransformJoinStrategy;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;

public class DevTxnUnionNested {

    // AbstractStoreConnections store_1 and store_4.
    
    // In GraphView/TransactionHandlersView?
    
    // And also JENA-1667
    // and also GraphUnionRead
    // and shared internal datasets
    
    // Problem : two graphs sharing the same dataset/TDB2.
    // Nested transactions.
    
    // 1 - nested transactions
    // 2 - two DatasetGraphTransactions
    // 3 - Polyadic
    // 4 - GraphView graph Txn nesting.

    public static void main(String[] args) {
        tdb1();
    }
    
    // New/inner less than existing/outer.
    // READ < PROMOTE < WRITE ; PROMOTES must match (READ_COMMITTED_PROMOTE could be < READ_PROMOTE)
    // Could try to promote outer - See checkCompatible.
    
    public static void tdb1() {
        
        Quad quad = SSE.parseQuad("(:g :s :p :o)");
        
        DatasetGraph dsg1;
        DatasetGraph dsg2;
        if ( true ) {
            dsg1 = TDBFactory.createDatasetGraph(org.apache.jena.tdb.base.file.Location.mem("NAME")); 
            dsg2 = TDBFactory.createDatasetGraph(org.apache.jena.tdb.base.file.Location.mem("NAME"));
        } else {
            dsg1 = DatabaseMgr.connectDatasetGraph(org.apache.jena.dboe.base.file.Location.mem("NAME")); 
            dsg2 = DatabaseMgr.connectDatasetGraph(org.apache.jena.dboe.base.file.Location.mem("NAME"));
        }

        System.out.println("[1] "+dsg1.isInTransaction()+"/"+dsg2.isInTransaction());
        dsg1.begin(TxnType.READ);
        System.out.println("[2] "+dsg1.isInTransaction()+"/"+dsg2.isInTransaction());
        dsg2.begin(TxnType.WRITE);
        System.out.println("[3] "+dsg1.isInTransaction()+"/"+dsg2.isInTransaction());

        dsg2.add(quad);
        
        dsg2.commit();
        dsg1.commit();

//        TransactionHandler th1 = dsg1.getDefaultGraph().getTransactionHandler();
//        TransactionHandler th2 = dsg2.getDefaultGraph().getTransactionHandler();
//        th1.begin();
//        th2.begin();
//        th2.commit();
//        th1.commit();
        System.out.println("DONE");
        System.exit(0);
        
    }
    
    public static void run() {
        
        JenaSystem.init();
        // Needs tdb:GraphTDB rdfs:subClassOf  ja:Model .

        String file = "file:///home/afs/tmp/union/config1.ttl";
        Model m = RDFDataMgr.loadModel("/home/afs/tmp/union/config1.ttl");
        AssemblerUtils.addRegistered(m);
        
        Resource root = m.createResource(file+"#dataset1");
        
        FusekiLogging.setLogging();
        FusekiServer server = FusekiServer.create()
            //.add("/ds", DatasetGraphFactory.createTxnMem())
            .parseConfigFile("/home/afs/tmp/union/config1.ttl")
            .port(3030)
            //.verbose(true)
            .build()
            //.start()
            ;
        try { 
            String DS1 = "ds";
            String DS2 = "ds";

            DataAccessPoint dap = server.getDataAccessPointRegistry().get(DataAccessPoint.canonical(DS1));
            DataService dSvr = dap.getDataService();
            DatasetGraph dsg = dSvr.getDataset();

            TransactionHandler th = dsg.getDefaultGraph().getTransactionHandler();
            th.begin();
            th.begin();
            th.commit();

            th.commit();
            System.out.println("DONE");
            System.exit(0);


            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:3030/"+DS1) ) {
                conn.queryResultSet("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }", ResultSetFormatter::out);
            }

            if ( ! DS1.equals(DS2) ) {
                try ( RDFConnection conn = RDFConnectionFactory.connectFuseki("http://localhost:3030/"+DS2) ) {
                    conn.queryResultSet("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }", ResultSetFormatter::out);
                }
            }

        } finally { server.stop(); }
        System.out.println("DONE");
    }

    public static void mainJoin(String[] args) {
        if ( true ) {
            //arq.qparse.main("-print=op", "--query", "/home/afs/TQ/tmp/Q3.rq");
            JoinClassifier.print = true;
            arq.qparse.main("-print=opt", "--query", "/home/afs/TQ/tmp/F1.rq");
            return ;
        }

        if ( true ) {
            Op op = SSE.readOp("/home/afs/TQ/tmp/OP3");
            //System.out.println(op);
            Op opLeft = ((OpJoin)op).getLeft();
            Op opRight = ((OpJoin)op).getRight();
            JoinClassifier.print = true;
            //Op op1 = Transformer.transform(new TransformJoinStrategy(), op);
            Op op1 = new TransformJoinStrategy().transform((OpJoin)op, opLeft, opRight);
            //System.out.println(op1);
            return;
        }

        Query query = QueryFactory.read("/home/afs/TQ/tmp/Q3.rq");
        Op op = Algebra.compile(query);

        JoinClassifier.print = true;
        boolean b = JoinClassifier.isLinear((OpJoin)op);
        //JoinClassifier.print = false;

        Op op1 = Transformer.transform(new TransformJoinStrategy(), op);

        //Op op1 = Algebra.optimize(op);
        System.out.println(op);
        System.out.println(op1);
        System.out.println("DONE");

    }

    public static void mainPLAT1500(String[] args) {
        String x = StrUtils.strjoinNL
            ("PREFIX : <#>"
            ,"SELECT *"
            ,"WHERE {"
            ,"    ?a :p1 ?b"
            ,"    { ?a :p2 ?x } UNION { ?b :p3 ?y }"
            ,"}");

        Query query = QueryFactory.create(x);
        Op op = Algebra.compile(query);

        JoinClassifier.print = true;
        boolean b = JoinClassifier.isLinear((OpJoin)op);
        JoinClassifier.print = false;

        Op op1 = Algebra.optimize(op);
        System.out.println(op);
        System.out.println(op1);
    }
}
