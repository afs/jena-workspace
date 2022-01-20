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

package dsg.union;

import java.util.List;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.sse.SSE;

public class DevUnionDSG {

    public static void data(DatasetGraph dsg) {
        String s = StrUtils.strjoinNL("PREFIX : <http://example/>"
                                      ,":g1 { :s :p :o }"
                                      ,":g2 { :s :p :o }");
        RDFParser.fromString(s).lang(Lang.TRIG).parse(dsg);
    }

    // JENA-1668.
    public static void main(String... args) {
        mainTIM();
    }
    public static void mainTIM() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        data(dsg);

        Graph g = dsg.getUnionGraph();
        Node x = SSE.parseNode(":s");

        // Does not hit indexes?
        List<Triple> list =
                g.find(x, null, null)
                .toList();

        System.exit(0);
    }

    public static void mainAltDft() {
        // DatasetGraphMapLink
        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
        Graph ug = dsg.getUnionGraph();

        // Change the read-default graph.
        // Union default graph + named graphs
        DatasetGraph dsg1 = new DatasetGraphAltGraphs(ug, ug, dsg);
        // Union default graph only.
        DatasetGraph dsg2 = DatasetGraphFactory.wrap(ug);

        RDFDataMgr.write(System.out, dsg1, Lang.NQ);

        try(RDFLink conn = RDFLink.connect(dsg1)) {
            conn.queryRowSet("SELECT * { ?s ?p ?o }",
                             rs->RowSetOps.out(rs));
            conn.update("INSERT DATA { <x:s> <x:p> 'new'}");
        }

        RDFDataMgr.write(System.out, dsg, Lang.NQ);
    }
}
