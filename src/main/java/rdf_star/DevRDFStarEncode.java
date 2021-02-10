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

package rdf_star;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

public class DevRDFStarEncode {
    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {
//      TurtleJCC.register();
//      org.apache.jena.riot.lang.extra.TurtleJCC.register();
        JenaSystem.init();
        mainEncode() ;
    }

    public static void mainEncode(String...a) {
        String str = StrUtils.strjoinNL
            ("BASE <http://base/>"
            ,"PREFIX :     <http://example/>"
            ,"PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>"
            ,""
            ,"<<:s :p :o >> :q :z ."
            );

        Graph g = data(str);
        Graph g1 = RDFX.encodeRDFStar(g);


        //Reification missing base triple!
        Graph g2 = data(str);
        RDFX.encodeRDFStarInPlace(g2);

        //System.out.println("-- Mismatch");
        RDFDataMgr.write(System.out, g, RDFFormat.TURTLE_BLOCKS);
        System.out.println("-- Encode");
        RDFDataMgr.write(System.out, g1, RDFFormat.TURTLE_BLOCKS);
        System.out.println("-- Encode in place");
        RDFDataMgr.write(System.out, g2, RDFFormat.TURTLE_BLOCKS);

//        Graph g3 = RDFX.decodeRDFStar(g);
//        System.out.println("-- Decode");
//        RDFDataMgr.write(System.out, g3, RDFFormat.TURTLE_BLOCKS);
        System.out.println("-----");
    }

    static Graph data(String dataStr) {
        Graph g = SSE.parseGraph(dataStr);
        g.getPrefixMapping().setNsPrefix("", "http://example/");
        g.getPrefixMapping().setNsPrefix("rdf", RDF.getURI());
        return g;
    }

}
