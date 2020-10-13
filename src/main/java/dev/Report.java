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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import assembler1.JA;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.*;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.algebra.*;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.util.QueryExecUtils;
import riotcmd.riot;
import shacl.shacl_parse;

public class Report {
    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static class Foo{}

    public static void main(String...a) {


        //Object obj = AssemblerUtils.build("/home/afs/tmp/assem.ttl", JA.InfModel);
        //System.out.println(obj.getClass().getName());
        List<Rule> x = Rule.rulesFromURL("/home/afs/tmp/rules");
        x.forEach(System.out::println);

        System.exit(0);

        // tosh.ttl is RDF/XML
        // SPARQL parsing needs predefined prefixes.


        Graph shapesGraph = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM).read("/home/afs/tmp/SHACL/shapes.ttl", "TTL").getGraph();
        Graph dataGraph = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM).read("/home/afs/tmp/SHACL/data.ttl", "TTL").getGraph();
        ShaclValidator validator = ShaclValidator.get();
        ValidationReport report = validator.validate(shapesGraph, dataGraph);
        ShLib.printReport(report);
        System.exit(0);

        mainBase(); System.exit(0);
        mainJSONLD(); System.exit(0);

//        mainExistsStrict();
//        mainJena1945_defaultUnionGraph();   // Discuss
    }

    public static void mainBase(String...a) {
        riotcmd.riot.main(
                          //"--set=ttl:omitBase=true",
                          "--set=ttl:directiveStyle=sparql",
                          //"--pretty=TRIG",
                          "--petty=TTL",
                          "--base=http://basic/",
                          "/home/afs/tmp/D-base.ttl");
    }

    public static void mainJSONLD(String...a) {

        riot.main("/home/afs/tmp/D.jsonld");
        System.out.println("==");

        Model m = ModelFactory.createDefaultModel();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream("/home/afs/tmp/D.jsonld");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        String baseURI = "http://REL/";
        RDFParser.create().base(baseURI).source(inputStream).lang(RDFLanguages.JSONLD).parse(m);

        RDFWriter.create().base(baseURI).lang(Lang.TTL).source(m).output(System.out);
        System.out.println();
        RDFWriter.create().base(baseURI).lang(Lang.JSONLD).source(m).output(System.out);


    }

    public static void mainExistsStrict(String...a) {
        String DIR = "/home/afs/tmp/";
        arq.sparql.main(
            //"--set=arq:optFilterImplicitJoin=false",
            //"--strict",
            "--data="+DIR+"D-exists.ttl", "--query="+DIR+"Q-exists.rq");
        System.exit(0);
    }

    public static void mainJena1947_shacl(String...a) {
        String FILE = "/home/afs/tmp/allotrope.ttl";
        //String FILE = "/home/afs/tmp/shapes.ttl";
        shacl_parse.main(FILE);
        //shacl_validate.main(FILE);
        System.exit(0);
    }

    public static void mainJena1945_defaultUnionGraph(String...a) {
        /*
    @Override
    public Op transform(OpPath opPath)
    {
        Node current = currentGraph.peek();
        if ( current == Quad.defaultGraphNodeGenerated || current == Quad.unionGraph ) {
            Var v = Var.ANON ; //varAlloc.allocVar() ;
            Op op = new OpGraph(v, opPath) ;
            op = OpDistinct.create(op) ;
            return op;
        }
        return super.transform(opPath);
    }
         */

        if ( false ) {
            // Algebra.unionDefaultGraph
            Op op1 = Algebra.compile(QueryFactory.create("SELECT * { ?s  <http://example/p> ?z . ?z <http://example/q> ?o   }"));
            @SuppressWarnings("deprecation")
            Op op2 = Algebra.unionDefaultGraph(op1);
            /* Yields correct result:
            (distinct
              (graph ??_
                (bgp (triple ?s <urn:p> ?o))))
           */
            System.out.println(op2);

            DatasetGraph dsg = RDFDataMgr.loadDatasetGraph("/home/afs/tmp/D.trig");
            RDFDataMgr.write(System.out, dsg, Lang.TRIG);
            QueryExecUtils.execute(op2, dsg);
            QueryExecUtils.execute(op1, DatasetGraphFactory.wrap(dsg.getUnionGraph()));
            System.exit(0);
        }

        if ( true ) {
            // Quads, rename to union graph
            Op op1 = Algebra.compile(QueryFactory.create("SELECT * { ?s  <http://example/p> ?z . ?z <http://example/q> ?o   }"));
            Op op2 = Algebra.toQuadForm(op1);
            System.out.println(op2);
            Transform transform = new TransformGraphRename(Quad.defaultGraphNodeGenerated, Quad.unionGraph);
            Op op3 = Transformer.transform(transform, op2);
            System.out.println(op3);
            Query q = OpAsQuery.asQuery(op3);
            System.out.println(q);
            //System.exit(0);
        }

        if ( true ) {
            Op op1 = Algebra.compile(QueryFactory.create("SELECT * { ?s  <http://example/p> ?z . ?z <http://example/q> ?o   }"));
            Op op2 = new OpGraph(Quad.unionGraph, op1);
            Op op3 = Algebra.toQuadForm(op2);
            System.out.println(op3);
            Query q = OpAsQuery.asQuery(op3);
            System.out.println(q);
            System.exit(0);
        }

        Op op1 = Algebra.compile(QueryFactory.create("SELECT * { ?s <urn:q> ?z . ?s <urn:p1>/<urn:p2>/<urn:p3> ?o }"));
        @SuppressWarnings("deprecation")
        Op op2 = Algebra.unionDefaultGraph(op1);
        System.out.println(op2);

        DatasetGraph dsg = RDFDataMgr.loadDatasetGraph("/home/afs/tmp/D.trig");
    }
}
