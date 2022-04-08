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

import java.io.InputStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.JenaXMLInput;

public class Report {
    static {
        try {
            // GeoSPARQL
            //org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        } catch (Throwable th) {}
        // JenaSystem.DEBUG_INIT = true;
        JenaSystem.init();
        LogCtl.setLog4j2();
        FusekiLogging.setLogging();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...args) {
        // Integrate error handling. Or
        // RowSetReaderXML.init and others.

        JenaXMLInput.allowLocalDTDs = true;

        try {
            Model model = ModelFactory.createDefaultModel();
            model.read("file:CVE/malicious1.xml");
            System.out.println("1: model.read returned normally");
        } catch (Throwable ex) {
            System.out.println("1: "+ex.getMessage());
        }
        System.out.println();

        try {
            Model model = ModelFactory.createDefaultModel();
            model.read("file:CVE/malicious2.xml");
            System.out.println("2: model.read returned normally");
        } catch (Throwable ex) {
            System.out.println("2: "+ex.getMessage());
        }
        System.out.println();

        try {
            ResultSetFactory.load("file:CVE/bad1-srx.srx");
            System.out.println("3: ResultSetFactory.load returned normally");
        } catch (Throwable ex) {
            System.out.println("3: "+ex.getMessage());
        }
        System.out.println();

        try {
            ResultSetFactory.load("file:CVE/bad2-srx.srx");
            System.out.println("4: ResultSetFactory.load returned normally");
        } catch (Throwable ex) {
            System.out.println("4: "+ex.getMessage());
        }

    }


    // [ ] Iter.first ; Leave the iterator usable. -- change to terminating.
    // findFirst - remove?

    // GRAPH ( iri() | DEFAULT | NAMED | ALL | UNION ) pattern
    // --> Target in modify.

    // Big years.

    /* gYear, gYearMonth, xs:dateTime template "asDateTime" Leave API as-is and
     * change SPARQL
     *
     * [x] Extract XMLChar and XML11Char [ ] Xerces Regex
     *
     * [ ] Year 0 and calculations [ ] New test suite [ ] gYear difference = years
     */

    public static void mainSRJ(String...args) {
        {
            int x = Integer.MAX_VALUE+1;
            System.out.printf("%d 0x%08X\n", x , x );
            System.exit(0);
        }
        RowSet rowSet = QueryExec.service("http://localhost:3030/ds")
                .query("SELECT * { ?s ?p ?o }")
                .select();
        rowSet.forEachRemaining(b->{
           System.out.println(b);
        });
        System.exit(0);

        //arq.qparse.main("--print=opt", "--file=/home/afs/tmp/Q1.rq");
        Query query = QueryFactory.read("/home/afs/tmp/Q1.rq");
        Op op = Algebra.compile(query);
        System.out.println(op);

        InputStream in = IO.openFileBuffered("/home/afs/tmp/R.srj");
        SPARQLResult result = ResultSetReaderRegistry.getFactory(ResultSetLang.RS_JSON).create(ResultSetLang.RS_JSON).readAny(in, null);
        if ( result.isBoolean() )
            System.out.println("ASK => "+result.getBooleanResult());
        else
            ResultSetFormatter.out(result.getResultSet());
        System.exit(0);
    }

    public static void ttlFormat() {

        /* :s :p [] . :s :p1 [ :q :r ] . :s :p2 [ :q1 :r ; :q2 :r ] . :s :p3 (1 2 3)
         * .
         *
         * becomes :s :p [] ; :p1 [ :q :r ] ; // This one! :p2 [ :q1 :r ; :q2 :r ] ;
         * :p3 ( 1 2 3 ) .
         *
         * :s :p [] ; :p1 [ :q :r ] ; ?? This one ?? :p1 [ :q :r ] ; // This one! :p2
         * [ :q1 :r ; :q2 :r ] ; :p3 ( 1 2 3 ) . */
        /* Writing complex objects: TurtleShell.ShellTurtle.writeNestedObject Don't
         * indent after predicate when it's a blank to nest ("complex")
         * ShellTurtle.writePredicate - need to delay until
         * writePredicateObjectList*(cluster)
         *
         * // Yes - OK - gap. // writePredicate at start of
         * writePredicateObjectList/4 if ( ! rdfLiterals.isEmpty() ) {
         * writePredicateObjectList(p, rdfLiterals, predicateMaxWidth, first) ; first
         * = false ; } // Yes - OK - gap. if ( ! rdfSimpleNodes.isEmpty() ) {
         * writePredicateObjectList(p, rdfSimpleNodes, predicateMaxWidth, first) ;
         * first = false ; } // NO // Both lists and [] for ( Node o :
         * rdfComplexNodes ) { ==> write short. writePredicateObject(p, o,
         * predicateMaxWidth, first) ; Calls writePredicate -- with indent Calls
         * writeNestedObject compact, non-compact WANT non-compact to write, not
         * indent, then " [\n";
         *
         * first = false ; }
         *
         * ---- writePredicate : becomes writePredicate (no gap) and
         * writePredicateObjectGap if ( wPredicate > LONG_PREDICATE ) println() ;
         * else { out.pad(predicateMaxWidth) ; gap(GAP_P_O) ; }
         *
         * writeNestedObject Split to move "isCompact" test out.
         *
         * writeSimpleObject if ( isCompact(node) ) // writeSimpleObject Finish
         * indent. Write node. else // Write nested object "[\n" "]" */

        Graph graph = GraphFactory.createDefaultGraph();
        RDFParser.source("/home/afs/tmp/shapes.ttl").parse(graph);
        RDFWriter.source(graph).format(RDFFormat.TURTLE_BLOCKS).output(System.out);
        System.exit(0);
    }
}
