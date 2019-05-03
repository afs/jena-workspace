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

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.optimize.TransformJoinStrategy;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;

public class Report {


    public static void main(String[] args) {
        mainTDB2();
    }

    // Test TDB2 with faked exception
    public static void mainTDB2() {
        String DIR = "/home/afs/tmp/DB2";
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);
        DatasetGraph dsg = DatabaseMgr.connectDatasetGraph(DIR);
//        System.out.println("Before");
//        Txn.executeRead(dsg, ()->RDFDataMgr.write(System.out, dsg, Lang.NQ));
//        System.out.println("Init");

        Quad q = SSE.parseQuad("(:g :s :p :o)");
        System.out.println("START");
        try {
            Txn.executeWrite(dsg,  ()->{
                dsg.add(q);
                //throw new RuntimeException();
                //dsg.abort();
            });
        } catch (Exception ex) {

            System.out.println("isInTransaction = "+dsg.isInTransaction());

            System.out.println("------------");
            ex.printStackTrace(System.out);
            System.out.println("------------");
        }

        if ( dsg.isInTransaction() ) {
            System.out.println("************");
            dsg.isInTransaction();
        }

        // Look for "// [HACK]"
        // Fails to truly abort.

        // But now  TarsnactionCoordinator.rollback calls Trasnaction.end.

        // txn.isInTransaction() remains true.
        // TransactionalBase.commitExec calls _end, clears transaction.
        // TransactionalBase.abort calls _end
        // TransactionalBase.end calls _end
        // TarsnactionCoordinator has count > 0
        // TarsnactionCoordinator.executeAbort?

        // HACK: executeAbort

        System.out.println("Outcome");
        Txn.executeRead(dsg, ()->RDFDataMgr.write(System.out, dsg, Lang.NQ));
        System.out.println("END");

    }

    // Need JIRA
    public static void mainTDB1() {
        String DIR = "/home/afs/tmp/DB";
        FileOps.ensureDir(DIR);
        FileOps.clearAll(DIR);
        DatasetGraph dsg = TDBFactory.createDatasetGraph(DIR);
        System.out.println("Before");
        Txn.executeRead(dsg, ()->RDFDataMgr.write(System.out, dsg, Lang.NQ));
        System.out.println("Init");

        Quad q = SSE.parseQuad("(:g :s :p :o)");
        System.out.println("START");
        try {
            Txn.executeWrite(dsg,  ()->{
                dsg.add(q);
                //dsg.abort();
            });
        } catch (Exception ex) {
            System.out.println("------------");
            ex.printStackTrace(System.out);
            System.out.println("------------");
        }
        System.out.println("Outcome");
        Txn.executeRead(dsg, ()->RDFDataMgr.write(System.out, dsg, Lang.NQ));
        System.out.println("END");
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
