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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import javax.xml.datatype.XMLGregorianCalendar;

import arq.qexpr;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;

public class DevDateTimeCompare {
    static {
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

    public static void mainDataTimeCompare() {
        /* Values of the date/time datatypes xs:time, xs:gMonthDay, xs:gMonth, and
         * xs:gDay, can be considered to represent a sequence of recurring time
         * instants or time periods. An xs:time occurs every day. An xs:gMonth occurs
         * every year. Comparison operators on these datatypes compare the starting
         * instants of equivalent occurrences in the recurring series. These
         * xs:dateTime values are calculated as described below.
         *
         * Comparison operators on xs:date, xs:gYearMonth and xs:gYear compare their
         * starting instants. These xs:dateTime values are calculated as described
         * below. */
        /* XMLGregorianDateTime works well including indeterminate for overlaps.
         * e.g. */

        Node n1 = SSE.parseNode("'2021'^^xsd:gYear");
        NodeValue nv1 = NodeValue.makeNode(n1);
        Node n2 = SSE.parseNode("'2021-02'^^xsd:gYearMonth");
        NodeValue nv2 = NodeValue.makeNode(n2);

        // Make the time point zeros? No prob - the "undef" value is negative.

        System.out.println(nv1);
        System.out.println(nv1.isDate());
        System.out.println(nv1.getDateTime());
        System.out.println();
        System.out.println(nv2);
        System.out.println(nv2.isDate());
        System.out.println(nv2.getDateTime());
        System.out.println();
        System.out.println("nv2.month: " + nv2.getDateTime().getMonth());
        System.out.println("nv2.day:   " + nv2.getDateTime().getDay());
        System.out.println();
        System.out.println(Integer.MIN_VALUE);

        // SystemARQ.StrictDateTimeFO = true;

        System.out.println("Cmp: java: " + nv1.getDateTime().compare(nv2.getDateTime()));

        System.out.println("Cmp: XSD: " + XSDFuncOp.compareDateTime(nv1, nv2));

        // System.out.println(NodeValue.compare(nv1, nv2));
        System.exit(0);
    }

    public static void main0000() throws Exception {
        XMLGregorianCalendar cal = NodeValue.xmlDatatypeFactory.
        // DatatypeFactory.newInstance().
        // gYear.
                                                               newXMLGregorianCalendar("0000-01-01T01:02:03");
        System.out.println(cal);
        System.out.println(cal.getYear());

        System.out.println();

// XMLGregorianCalendar cal1 =
// NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar("0001-01-01T00:00:00Z");
// XMLGregorianCalendar cal2 =
// NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar("0000-01-01T00:00:00Z");
// XMLGregorianCalendar cal3 =
// NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar("-0001-01-01T00:00:00Z");

        // TemporalAccessor cal1 =
        // java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse("0001-01-01T00:00:00Z");
        // TemporalAccessor cal2 =
        // java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse("0000-01-01T00:00:00Z");
        // TemporalAccessor cal3 =
        // java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse("-0001-01-01T00:00:00Z");

// ZonedDateTime zdt1 = ZonedDateTime.parse("0001-01-01T00:00:00Z");
// ZonedDateTime zdt2 = ZonedDateTime.parse("0000-01-01T00:00:00Z");
//
// System.out.println(zdt1.get(ChronoField.YEAR)+" :: "+zdt1);
// System.out.println(zdt2.get(ChronoField.YEAR)+" :: "+zdt2);
//
// // In hours/minutes/seconds.
// System.out.println("zdt1-zdt2 = "+Duration.between(zdt1, zdt2));

        LocalDateTime dt1 = LocalDateTime.parse("0001");
        LocalDateTime dt2 = LocalDateTime.parse("0000");

        System.out.println(dt1.get(ChronoField.YEAR) + " :: " + dt1);
        System.out.println(dt2.get(ChronoField.YEAR) + " :: " + dt2);

        System.out.println("dt1-dt2 = " + Duration.between(dt1, dt2));

// System.out.println("YEAR");
// qexpr.main("YEAR('-0000-01-01'^^xsd:date)");
// qexpr.main("'-0001'^^xsd:gYear + 'P2Y'^^xsd:yearMonthDuration");

        // One year -> XSD 1.0
        qexpr.main("'0001-01-01'^^xsd:date - '-0001-01-01'^^xsd:date");

        System.out.println("DONE");
        if ( true )
            return;
        Graph graph = GraphFactory.createDefaultGraph();
        RDFParser.fromString("<x> <p> '0000'^^<http://www.w3.org/2001/XMLSchema#gYear> .").checking(false).lang(Lang.TTL).parse(graph);
        System.out.println("DONE");

// DatasetGraph dsg0 = TDBFactory.createDatasetGraph();
// RDFDataMgr.write(System.out, dsg0.getDefaultGraph(), Lang.TTL);
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
