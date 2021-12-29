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

package z_archive;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ref.QueryEngineRef;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;

public class Jena2150_eval_quads_bind {

    // JENA-2150 - DONE - Delete this file.

    static {
        JenaSystem.init();
        FusekiLogging.setLogging();
        //LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    static String DIR = "/home/afs/tmp/Diff-13-15/";

    public static void main(String...a) {
        investigate();
    }

    // With filter - rewrite to ?*g form.
    // Without filter - do not rename apart.
    private static void investigate() {
        String qs = StrUtils.strjoinNL
                ("SELECT * WHERE {"
                ,"  GRAPH ?g {"
                ,"    ?s ?p ?o ."
                ,"    FILTER(?g != 123)"
                ,"    BIND(str(?g) AS ?g1)"
                ,"  }"
                ,"}"
                );
        String opStr = StrUtils.strjoinNL
                ("(graph ?g"
                //,"  (filter (!= ?g 123)"
                ,"    (extend ((?g1 ?g))"
                ,"      (bgp (triple ?s ?p ?o))"
                ,"    )"
                //,"  )"
                ,")");

        // 1: Rewrite graph node if touched.
        // 2: (filter) triggers in ?
        // 3: Not (extend), (assign)

        Query query = QueryFactory.create(qs);
        System.out.println(query);
        Op opq = Algebra.compile(query);
        Op opq2 = Algebra.toQuadForm(opq);
        System.out.println(opq);
        System.out.println(opq2);
        System.exit(0);
//        Op op = Algebra.compile(query);
        Op op = SSE.parseOp(opStr);
        System.out.println(op);
        Op op1 = Algebra.toQuadForm(op);
        System.out.println(op1);

        System.exit(0);

        System.out.println("Quad block");
        Op op1a = Algebra.toQuadBlockForm(op);
        System.out.println(op1a);
        Op op2a = Algebra.optimize(op1a);
        System.out.println(op2a);
    }

    public static void test() throws FileNotFoundException {
        DatasetGraph dsg1 = DatasetGraphFactory.createTxnMem();
        DatasetGraph dsg2 = TDBFactory.createDatasetGraph();

        //DatasetGraph dsg3 = DatasetGraphFactory.createGeneral();
        //DatasetGraph dsg4 = org.apache.jena.tdb2.DatabaseMgr.createDatasetGraph();

//        update(dsg1);
//        update(dsg2);
//        //update(dsg3);
//        //update(dsg4);


        String qs = StrUtils.strjoinNL
                ("SELECT (count(?g) AS ?C) WHERE {"
                ,"  GRAPH ?g {"
                ,"    ?s ?p ?o ."
                ,"    BIND(?g AS ?g1)"
                //,"    FILTER(?g != 123)"
                ,"  }"
                ,"}"
                );
        Query query = QueryFactory.create(qs);

        query("TIM/triples", dsg1, query);
        query("TDB1/quads",  dsg2, query);
        QueryEngineRef.register();
        query("TDB1/ref",    dsg2, query);
    }

    private static void query(String label, DatasetGraph dsg, Query query) {
        System.out.println(label);
        Txn.executeWrite(dsg, ()->{
            RDFDataMgr.read(dsg, DIR+"data1.trig");
            QueryExecution qExec = QueryExecutionFactory.create(query, dsg);
            QueryExecUtils.executeQuery(qExec);
        });
        System.out.println();
    }

    public static void xmlDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream in = new FileInputStream("/home/afs/tmp/XML-tunnel/data.rdf");
        //builder.parse("file:///home/afs/tmp/XML-tunnel/data.rdf");
        builder.parse(in);
    }
}
