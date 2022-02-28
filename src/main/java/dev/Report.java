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

import static org.apache.jena.atlas.iterator.Iter.iter;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Report {
    static {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
        } catch (Throwable th) {}
        // JenaSystem.DEBUG_INIT = true;
        JenaSystem.init();
        FusekiLogging.setLogging();
        // LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // [ ] Iter.first ; Leave the iterator usable. -- change to terminating.
    // findFirst - remove?

    // GRAPH ( iri() | DEFAULT | NAMED | ALL | UNION ) pattern
    // --> Target in modify.

    // tdbloader2
    // Data load phase
    // CmdNodeTableBuilder --> ProcNodeTableBuilder
    // Tree write phase
    // CmdIndexBuild --> ProcIndexBuild
    // New data phase.

    /* gYear, gYearMonth, xs:dateTime template "asDateTime" Leave API as-is and
     * change SPARQL
     *
     * [x] Extract XMLChar and XML11Char [ ] Xerces Regex
     *
     * [ ] Year 0 and calculations [ ] New test suite [ ] gYear difference = years
     */

    // RDFS
    private static Iter<Quad> findOneGraph(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
        if ( ! g.isConcrete()  )
            throw new IllegalStateException();
        return iter(dsg.getGraph(g).find(s,p,o)).map(t->Quad.create(g, t));
    }

    public static void main(String...args) {
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
        RDFWriter.create(graph).format(RDFFormat.TURTLE_BLOCKS).output(System.out);
        System.exit(0);
    }
}
