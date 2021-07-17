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

import java.io.StringReader;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;

public class Jena1864_Turtle_base {
    static { LogCtl.setLog4j2(); }
    //static { LogCtl.setCmdLogging(); }

    // JENA-1862 Turtle and TriG
    public static void main(String...a) {
        // Log.info(Report.class, "A log message");
        //System.exit(0);

        String str = "BASE <http://base/> PREFIX : <http://example/>  <urn:a> <b#c> :d .";
        Graph graph = GraphFactory.createDefaultGraph();
        Context context = RIOT.getContext().copy();
        //context.set(RIOT.symTurtlePrefixStyle, "sparql");

        RDFParser.create()
            .lang(Lang.TTL)
            .source(new StringReader(str))
            .parse(graph);

//        RDFWriter.create()
//            .base("http://base/")
//            .format(RDFFormat.NTRIPLES)
//            .source(graph)
//            .context(context)
//            .output(System.out);

        System.out.println("----------");

        RDFWriter.create()
            .base("http://base/")
            .format(RDFFormat.TURTLE_FLAT)
            .source(graph)
            .context(context)
            .output(System.out);

        System.out.println("----------");

        RDFWriter.create()
            .base("http://base/")
            .format(RDFFormat.TURTLE_PRETTY)
            .source(graph)
            .context(context)
            .output(System.out);
        System.exit(0);
    }

    // QueryIterGroup
    // https://github.com/eclipse/rdf4j/issues/1978
    public static void groupAgg() {
        dwim("SELECT ?s (COUNT(?o) AS ?count) { ?s ?p ?o } GROUP BY ?s");
        dwim("SELECT (COUNT(?o) AS ?count) { ?s ?p ?o } GROUP BY (1) ");
        dwim("SELECT ?x (COUNT(?o) AS ?count) { ?s ?p ?o } GROUP BY (1 AS ?x) ");
        dwim("SELECT (COUNT(?o) AS ?count) { ?s ?p ?o }");
    }

    private static void dwim(String queryString) {
        Query query = QueryFactory.create(queryString);
        Model model = ModelFactory.createDefaultModel();
        // System.out.println(queryString);
        System.out.println(query);
        try (QueryExecution qExec = QueryExecutionFactory.create(query, model)) {
            QueryExecUtils.executeQuery(qExec);
        }
        System.out.println();
    }
}
