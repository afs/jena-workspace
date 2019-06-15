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

import java.io.StringReader;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.main.cmds.FusekiMainCmd;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class Dev {
    static {
        LogCtl.setLog4j();
    }

    public static void main(String[] argv) throws Exception {
        FusekiMainCmd.main("--config=/home/afs/DIR/example.ttl");
    }

    public static void mainJena1710(String[] argv) {
        String data = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
            , "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            , ""
            ,":a :p _:b0 ."
            , "_:b0 rdf:first 1 ."
            , "_:b0 rdf:rest _:b1 ."
            , "_:b1 rdf:first ( _:b0 ) ."
            , "_:b1 rdf:rest rdf:nil ."
            );

        Model m = ModelFactory.createDefaultModel();

        if ( false ) {
            RDFDataMgr.read(m, new StringReader(data), null, Lang.TTL);
        } else {
            m.setNsPrefix("rdf", RDF.getURI());
            m.setNsPrefix("owl", OWL.getURI());
            m.setNsPrefix("rdfs", RDFS.getURI());

            Resource b0 = m.createResource();
            Resource b1 = m.createResource();

            // add 4 statements:

            b0.addProperty(OWL.unionOf, b1).addProperty(RDF.type, OWL.Class);
            // b0.addProperty(RDFS.label, "B0");
            b1.addProperty(RDF.first, b0).addProperty(RDF.rest, RDF.nil);
            // b1.addProperty(RDFS.label, "B1");
        }

        RDFDataMgr.write(System.out, m, Lang.TTL);
        System.out.println("# --------");
        RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_FLAT);
// System.out.println("# --------");
// RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_BLOCKS);
// System.out.println("# --------");
// RDFDataMgr.write(System.out, m, RDFFormat.NT);
    }

}
