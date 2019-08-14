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

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.optimize.TransformJoinStrategy;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.engine.main.JoinClassifier;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.UpdateAction;

public class Report {


    public static void main(String[] args) throws Exception {
        String filename = "/home/afs/tmp/data.tar.gz";

        TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(filename)));
        TarArchiveEntry currentEntry;

        Graph graph = GraphFactory.createDefaultGraph();

        while ((currentEntry = tarInput.getNextTarEntry()) != null) {
            String currentFile = currentEntry.getName();
            System.out.println(currentFile);
            Lang lang = RDFLanguages.filenameToLang(currentFile);
            InputStream in = tarInput;
            in = new CloseShieldInputStream(in);

            RDFParser parser = RDFParserBuilder
                .create()
                .errorHandler(ErrorHandlerFactory.errorHandlerDetailed())
               //.source(new StringReader(CharStreams.toString(new InputStreamReader(tarInput))))
                .source(in)
                .lang(lang)
                .build();
            parser.parse(graph);
            System.out.println("Done: "+currentFile);
        }

    }

    //2019-07-31
    public static void main1(String[] args) throws Exception {
        Dataset dataset = TDB2Factory.createDataset();

      //wrap the dataset with a DatasetGraphMonitor and obtain a DatasetGraph
      DatasetGraph datasetGraph1 = new DatasetGraphMonitor(dataset.asDatasetGraph(), new DatasetChanges() {
        @Override
        public void start() {
        }
        @Override
        public void reset() {
        }
        @Override
        public void finish() {
        }
        @Override
        public void change(QuadAction qaction, Node g, Node s, Node p, Node o) {
          System.out.println("Dataset change: "+qaction);
        }
      });

      DatasetGraph datasetGraph2 = new DatasetGraphWrapper(dataset.asDatasetGraph()) {
          @Override public void add(Node g, Node s, Node p, Node o) {
              System.out.println("Dataset change: ADD/4");
          }
          @Override public void add(Quad q) {
              System.out.println("Dataset change: ADD/1");
          }

      };

      DatasetGraph datasetGraph = datasetGraph1;

      //create a model using the DatasetGraphMonitor as underlying graph
      //run an insert sparql query to add new triples to the triplestore (this really is in a write transaction, maybe I'm oversimplifying here)
      Txn.executeWrite(datasetGraph, ()->{
          //Model model = ModelFactory.createModelForGraph(datasetGraph.getDefaultGraph());
          UpdateAction.parseExecute("INSERT DATA { <x:s> <x:p> <x:o> }", datasetGraph);
      });
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
