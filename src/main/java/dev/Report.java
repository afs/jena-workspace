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

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Locale;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import arq.qexpr;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.*;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.out.NodeFormatterTTL_MultiLine;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.DateTimeStruct;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Report {
    static {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
        } catch (Throwable th) {}

        JenaSystem.init();
        FusekiLogging.setLogging();
        //LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // [ ] Iter.first ; Leave the iterator usable. -- change to terminating.
	//       findFirst - remove?

    // GRAPH ( iri() | DEFAULT | NAMED | ALL | UNION ) pattern
    //  --> Target in modify.

    // tdbloader2
    // Data load phase
    //   CmdNodeTableBuilder --> ProcNodeTableBuilder
    // Tree write phase
    //    CmdIndexBuild --> ProcIndexBuild
    // New data phase.

    /*
     * gYear, gYearMonth,
     * xs:dateTime template
     *   "asDateTime"
     * Leave APi as-is and change SPARQL
     *
     * [x] Extract XMLChar and XML11Char
     * [ ] Xerces Regex
     *
     * [ ] Year 0 and calculations
     * [ ] New test suite
     * [ ] gYear difference = years
     */

    public static void main(String... args) {

        RDFConnectionRemote.service("").build();

        UpdateExecutionHTTPBuilder b = UpdateExecutionHTTP.service("");
        b.httpHeader("", "");

        QueryExecutionHTTP.service("").httpHeader("", "");

        //RDFConnectionRemote.service("").

        Locale.setDefault(Locale.FRANCE);
//        Locale.setDefault(Locale.GERMANY);

        NodeValue nv = NodeValue.makeFloat(1.23f);
        System.out.println(nv.toString());
        System.out.println(NodeValue.toNode(nv));
        System.out.printf("%,d\n", 10000);

        DecimalFormat myFormatter = new DecimalFormat("#,###.###");
        System.out.println(myFormatter.format(1234.67e0));
        System.exit(0);

        String P = "PREFIX : <http://example/>";
        DatasetGraph dsg = TDB2Factory.createDataset().asDatasetGraph();
        dsg.executeWrite(()->
            RDFParser.fromString(P+ ":x :p :o . :o :q :z .").lang(Lang.TTL).parse(dsg)
                );
        dsg.executeRead(()->{
            RowSet rowSet = QueryExec.dataset(dsg).query(P+"SELECT * { ?x :p ?o . ?o :q ?z .}").select();
            RowSetOps.out(rowSet);
        });
    }

    public static void mainLang(String... args) {
        Lang lang = Lang.TTL;
        String s1 = lang.getContentType().getContentTypeStr();

        //lang.getHeaderString() should be
        //public String getHeaderString()
        // { return contentType.getContentTypeStr() ; }
        // Not .toHeaderStr() which includes "q="

        //String s2 = lang.getHeaderString();
        String s2 = lang.getContentType().toHeaderString(); // q factors.

        System.out.println(s1);
        System.out.println(s2);
    }


    public static void mainDataTimeCompare() {
        /*
        Values of the date/time datatypes xs:time, xs:gMonthDay, xs:gMonth, and xs:gDay, can be considered
        to represent a sequence of recurring time instants or time periods.
        An xs:time occurs every day. An xs:gMonth occurs every year.
        Comparison operators on these datatypes compare the starting instants of equivalent occurrences in the recurring series.
        These xs:dateTime values are calculated as described below.

        Comparison operators on xs:date, xs:gYearMonth and xs:gYear compare their starting instants.
        These xs:dateTime values are calculated as described below.
         */
        /*
         * XMLGregorianDateTime works well including indeterminate for overlaps.
         * e.g.
         */

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
        System.out.println("nv2.month: "+nv2.getDateTime().getMonth());
        System.out.println("nv2.day:   "+nv2.getDateTime().getDay());
        System.out.println();
        System.out.println(Integer.MIN_VALUE);

        //SystemARQ.StrictDateTimeFO = true;

        System.out.println("Cmp: java: "+nv1.getDateTime().compare(nv2.getDateTime()));

        System.out.println("Cmp: XSD: "+XSDFuncOp.compareDateTime(nv1, nv2));

        //System.out.println(NodeValue.compare(nv1, nv2));
        System.exit(0);
    }

    public static void mainNodeFormatter() {
        PrefixMap prefixMap = PrefixMapFactory.create();
        prefixMap.add(":", "http://example/");

        NodeFormatter nFmt1 = new NodeFormatterTTL("http://base/", prefixMap);
        NodeFormatter nFmt2 = new NodeFormatterTTL_MultiLine("http://base/", prefixMap);
        AWriter aOut = IO.wrapUTF8(System.out);

        String x[] = {
            "'xyz\\ndef'^^:datatype",
            "'123'^^:datatype",
            "123",
//            "'1abc'^^:datatype",
//            "'2ab\"c'^^:datatype" ,
//            "'3ab\\'c'^^:datatype" ,
//            "\"4abc\\\"'\"^^:datatype"
        };
        for (String str : x) {
            Node n = SSE.parseNode(str);
            nFmt1.format(aOut, n);
            aOut.println();
            nFmt2.format(aOut, n);
            aOut.println();
            aOut.println();
            aOut.flush();
        }

        System.exit(0);
    }

    static void foo() throws DatatypeConfigurationException {

        OntModel data = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
        data.read("http://purl.org/iso25964/skos-thes");
        System.out.println(data.size());
        System.out.println("DONE");
        System.exit(0);

        Node n1 = NodeFactory.createLiteralByValue(42, XSDDatatype.XSDbyte);
        Object obj1 = n1.getLiteral().getIndexingValue();
        System.out.println(obj1);
        if ( obj1 != null )
            System.out.println(obj1.getClass().getSimpleName());


        Node n2 = NodeFactory.createLiteral("42", XSDDatatype.XSDbyte);
        Object obj2 = n1.getLiteral().getIndexingValue();
        System.out.println(obj2);
        if ( obj2 != null )
            System.out.println(obj2.getClass().getSimpleName());

        System.out.println("DONE");
        System.exit(0);

        //String x = "'3500000000-01-01T00:00:00Z'^^xsd:dateTime";
        String lex = "3500000000";
        String x = "'"+lex+"'^^xsd:gYear";

        qexpr.main("'0001-01-01'^^xsd:date - '-0001-01-01'^^xsd:date");
        qexpr.main("'0001-01-01'^^xsd:date - '0000-01-01'^^xsd:date");
        System.exit(0);

//        Node n = SSE.parseNode(x);
//        Checker.check(n, ErrorHandlerFactory.errorHandlerStd, -1, -1);

        DateTimeStruct dts1 = DateTimeStruct.parseDateTime("3500000000-01-01T00:00:00Z");
        DateTimeStruct dts2 = DateTimeStruct.parseGMonth("--03Z");

        XMLGregorianCalendar cal1 = DatatypeFactory.newInstance().newXMLGregorianCalendar("2021");
        XMLGregorianCalendar cal2 = DatatypeFactory.newInstance().newXMLGregorianCalendar("2021Z");

        System.out.println(cal1.equals(cal2));
        System.out.println(cal1.compare(cal2));
        System.exit(0);

        // Invalid for partial
        ZonedDateTime zdt = cal1.toGregorianCalendar().toZonedDateTime();
        System.out.println(zdt);
//        boolean b = XSDDatatype.XSDgYear.isValid("3500000000");
//        System.out.println(b);

//        dwim("TDB1", TDBFactory.createDataset());
//        dwim("TDB2", TDB2Factory.createDataset());
//        dwim("Dft", DatasetFactory.create());
//        dwim("TIM", DatasetFactory.createTxnMem());
        System.out.println("DONE");
        System.exit(0);
    }

    public static void dwim(String label, Dataset dataset) {
        System.out.println("== "+label);
        dataset.executeWrite(()->{
            String updateQuery = "PREFIX  mygraph:  <http://example.org#mygraph> " +
                    "WITH mygraph: " +
                    "INSERT { <x:s> <x:p> 'abc' } WHERE { BIND('x' AS ?x) }";

            UpdateRequest update = UpdateFactory.create(updateQuery);
            UpdateProcessor processor = UpdateExecutionFactory.create(update, dataset);
            processor.execute();

            RDFDataMgr.write(System.out,  dataset, Lang.NQ);
        });
    }

    public static void main0000() throws Exception {
        XMLGregorianCalendar cal =
                NodeValue.xmlDatatypeFactory.
                //DatatypeFactory.newInstance().
                // gYear.
                newXMLGregorianCalendar("0000-01-01T01:02:03");
        System.out.println(cal);
        System.out.println(cal.getYear());

        System.out.println();

//        XMLGregorianCalendar cal1 = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar("0001-01-01T00:00:00Z");
//        XMLGregorianCalendar cal2 = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar("0000-01-01T00:00:00Z");
//        XMLGregorianCalendar cal3 = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar("-0001-01-01T00:00:00Z");

        //TemporalAccessor cal1 = java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse("0001-01-01T00:00:00Z");
        //TemporalAccessor cal2 = java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse("0000-01-01T00:00:00Z");
        //TemporalAccessor cal3 = java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse("-0001-01-01T00:00:00Z");

//        ZonedDateTime zdt1 = ZonedDateTime.parse("0001-01-01T00:00:00Z");
//        ZonedDateTime zdt2 = ZonedDateTime.parse("0000-01-01T00:00:00Z");
//
//        System.out.println(zdt1.get(ChronoField.YEAR)+" :: "+zdt1);
//        System.out.println(zdt2.get(ChronoField.YEAR)+" :: "+zdt2);
//
//        // In hours/minutes/seconds.
//        System.out.println("zdt1-zdt2 = "+Duration.between(zdt1, zdt2));

        LocalDateTime dt1 = LocalDateTime.parse("0001");
        LocalDateTime dt2 = LocalDateTime.parse("0000");

        System.out.println(dt1.get(ChronoField.YEAR)+" :: "+dt1);
        System.out.println(dt2.get(ChronoField.YEAR)+" :: "+dt2);

        System.out.println("dt1-dt2 = "+Duration.between(dt1, dt2));

//        System.out.println("YEAR");
//        qexpr.main("YEAR('-0000-01-01'^^xsd:date)");
        //qexpr.main("'-0001'^^xsd:gYear + 'P2Y'^^xsd:yearMonthDuration");

        // One year -> XSD 1.0
        qexpr.main("'0001-01-01'^^xsd:date - '-0001-01-01'^^xsd:date");

        System.out.println("DONE");
        if ( true ) return ;
        Graph graph = GraphFactory.createDefaultGraph();
        RDFParser
            .fromString("<x> <p> '0000'^^<http://www.w3.org/2001/XMLSchema#gYear> .")
            .checking(false)
            .lang(Lang.TTL).parse(graph);
        System.out.println("DONE");

//        DatasetGraph dsg0 = TDBFactory.createDatasetGraph();
//        RDFDataMgr.write(System.out, dsg0.getDefaultGraph(), Lang.TTL);
    }

    public static void ttlFormat() {

        /*
        :s :p [] .
        :s :p1 [ :q :r ] .
        :s :p2 [ :q1 :r ; :q2 :r ] .
        :s :p3 (1 2 3) .

        becomes
        :s      :p      []  ;
                :p1     [ :q      :r ] ;
                ## This one!
                :p2     [ :q1     :r ;
                          :q2     :r
                        ] ;
                :p3     ( 1 2 3 ) .

        :s      :p      []  ;
                :p1     [ :q      :r ] ; ?? This one ??
                :p1 [
                      :q      :r
                  ] ;
                ## This one!
                :p2 [
                      :q1     :r ;
                      :q2     :r
                  ] ;
                :p3     ( 1 2 3 ) .
                */
        /*
         Writing complex objects:
              TurtleShell.ShellTurtle.writeNestedObject
              Don't indent after predicate when it's a blank to nest ("complex")
            ShellTurtle.writePredicate - need to delay until
            writePredicateObjectList*(cluster)

            // Yes - OK - gap.
            //  writePredicate at start of writePredicateObjectList/4
            if ( ! rdfLiterals.isEmpty() ) {
                    writePredicateObjectList(p, rdfLiterals, predicateMaxWidth, first) ;
                    first = false ;
                }
            // Yes - OK - gap.
                if ( ! rdfSimpleNodes.isEmpty() ) {
                    writePredicateObjectList(p, rdfSimpleNodes, predicateMaxWidth, first) ;
                    first = false ;
                }
            // NO
            // Both lists and []
                for ( Node o : rdfComplexNodes ) {
                    ==> write short.
                    writePredicateObject(p, o, predicateMaxWidth, first) ;
                       Calls writePredicate -- with indent
                       Calls writeNestedObject compact, non-compact
                       WANT non-compact to write, not indent, then " [\n";

                    first = false ;
                }

            ----
            writePredicate : becomes writePredicate (no gap) and writePredicateObjectGap
                 if ( wPredicate > LONG_PREDICATE )
                    println() ;
                else {
                    out.pad(predicateMaxWidth) ;
                    gap(GAP_P_O) ;
                }

            writeNestedObject
              Split to move "isCompact" test out.

            writeSimpleObject
            if ( isCompact(node) )
               // writeSimpleObject
               Finish indent.
               Write node.
            else
              // Write nested object
              "[\n"
              "]"
         */

        Graph graph = GraphFactory.createDefaultGraph();
        RDFParser.source("/home/afs/tmp/shapes.ttl").parse(graph);
        RDFWriter.create(graph).format(RDFFormat.TURTLE_BLOCKS).output(System.out);
        System.exit(0);
    }
}
