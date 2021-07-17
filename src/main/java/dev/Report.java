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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sys.JenaSystem;

public class Report {
    static {
        JenaSystem.init();
        FusekiLogging.setLogging();
        //LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String[] args){

        HttpRequest.Builder b = HttpRequest.newBuilder();
        HttpClient hc = HttpClient.newBuilder()
                .build();
        Authenticator authenticator = hc.authenticator().get();

        {
            URI uri = URI.create("http://example:28181/path?query#frag");

            uri.toString();

            if ( uri.getRawFragment()== null && uri.getRawQuery() == null ) {
                uri.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (uri.getScheme() != null) {
                    sb.append(uri.getScheme());
                    sb.append(':');
                }
            }

            // request URI
            //+uri.getRawAuthority()
            String x = uri.getScheme()+"//"+uri.getHost()+"/"+uri.getRawPath();

            System.exit(0);
        }
        // ----
        String URL = "http://localhost:3030/ds?default-graph-uri=urn:x-arq:DefaultGraph&default-graph-uri=urn:x-arq:UnionGraph";
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(URL, "SELECT * { ?s ?p ?o }") ) {
            QueryExecUtils.executeQuery(qExec);
        }
        System.exit(0);

        Model m = ModelFactory.createDefaultModel();
        m.read("http://someplace", "TTL");

        RDFParser.create().source("url").httpAccept("acceptHeader").parse(m);

        Graph g = m.getGraph();
        StreamRDF x = StreamRDFLib.graph(g);
        StreamRDF x2 = new StreamRDFBase() {
            int count = 0 ;
            @Override
            public void triple(Triple triple)
            {
                count++;
                if ( count%10_000 == 0 )
                    System.out.printf("Count = %d\n", count);
                super.triple(triple);
            }
        };

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

        riotcmd.riot.main("--pretty=TTL", "/home/afs/tmp/shapes.ttl");
        System.exit(0);
    }

    static public Reader asUTF8(InputStream in) {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        return new InputStreamReader(in, decoder);
    }

    public static void mainFixup(String[] args) {
        System.out.println("construct");
        IRIFactory.iriImplementation().construct(":::junk");
        System.out.println("create");
        IRIFactory.iriImplementation().create(":::junk");

        String qs =
                StrUtils.strjoinNL("PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>"
                                  ,"SELECT * {"
                                  ,"VALUES ?x { 1.2 }"
                                  ,"BIND(xsd:integer(?x) as ?y)"
                                  ,"}"
                               );
        arq.sparql.main(qs);
    }
}
