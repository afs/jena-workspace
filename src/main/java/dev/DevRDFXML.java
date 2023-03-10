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

import jena.riot;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfxml.xmlinput.RDFXMLReader;
import org.apache.jena.rdfxml.xmlinput0.RDFXMLReader0;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sys.JenaSystem;

public class DevRDFXML {
    static {
        LogCtl.setLog4j2();
        JenaSystem.init();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // ****
    //   test with a bad-by-ARP URI.
    //   Add test case / new suite
    // ****
    // Lost ARP0 and setting the context.

    // Nothing catches the IRIException.

    // ** URIReference.resolve
    //   SWITCH for exception.

    // xmlinput0 - the "z z" is caught later (in RIOT).

    public static void main(String...args) {

        String file = "file:///home/afs/ASF/jena-workspace/xmlinput2/E.rdf";

        parse0(file);
        System.out.println();

        parse1(file);
        System.out.println();

        parseRIOT(file);
        System.out.println();

        System.out.println("DONE");
        System.exit(0);

        // ****
        // test with a bad-by-ARP URI.
        // Add test case / new suite
        // ****
        // Lost ARP0 and setting the context.

        // Nothing catches the IRIException.
        // AbsXMLContext.resolveAsURI - what does other jena do?
        // Other resolve?
        // URIReference.resolve(
        // xmlinput0 - the "z z" is caught later (in RIOT).

        //RIOT.getContext().set(RIOT.symRDFXML0, "true");
        riot.main("/home/afs/ASF/jena-development/E.rdf");
        System.out.println("DONE");
        System.exit(0);
        // Direct
        Model m = ModelFactory.createDefaultModel();
        new RDFXMLReader0().read(m, "file:///home/afs/ASF/jena-development/E.rdf");
        RDFWriter.source(m).lang(Lang.NT).output(System.out);

        System.out.println("DONE");
        System.exit(0);
    }

    private static void parse0(String file) {
        System.out.flush();
        System.err.flush();
        System.out.println("** Direct0");
        Model m = ModelFactory.createDefaultModel();
        RDFXMLReader0 r = new RDFXMLReader0();
        try {
            r.read(m, file);
            System.out.println("   Direct0");
        } catch (Throwable th) {
            System.out.flush();
            System.err.println("++ Direct0: "+th.getMessage());
            th.printStackTrace();
        }
        //RDFWriter.source(m).lang(Lang.NT).output(System.out);
    }

    private static void parse1(String file) {
        System.out.flush();
        System.err.flush();
        System.out.println("** Direct1");
        Model m = ModelFactory.createDefaultModel();
        RDFXMLReader r = new RDFXMLReader();
        try {
            r.read(m, file);
            System.out.println("   Direct1");
        } catch (Throwable th) {
            System.err.println("++ Direct1: "+th.getMessage());
            th.printStackTrace();
        }
        //RDFWriter.source(m).lang(Lang.NT).output(System.out);
    }

    private static void parseRIOT(String file) {
        System.out.flush();
        System.err.flush();
        System.out.println("** RIOT");
        riot.main("/home/afs/ASF/jena-development/E.rdf");
    }

}
