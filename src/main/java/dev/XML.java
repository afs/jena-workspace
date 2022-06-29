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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sys.JenaSystem;

public class XML {
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

        //JenaXMLInput.allowLocalDTDs = true;

        try {
            System.out.println("1: RDF/XML");
            Model model = ModelFactory.createDefaultModel();
            model.read("file:CVE/rdfxml-bad1.rdf");
            System.out.println("1: model.read(RDF/XML) returned normally");
        } catch (Throwable ex) {
            System.out.println("1: Exception: "+ex.getClass().getSimpleName()+" : "+ex.getMessage());
        }
        System.out.println();

        try {
            System.out.println("2: RDF/XML");
            Model model = ModelFactory.createDefaultModel();
            model.read("file:CVE/rdfxml-bad2.rdf");
            System.out.println("2: model.read(RDF/XML) returned normally");
        } catch (Throwable ex) {
            System.out.println("2: Exception: "+ex.getClass().getSimpleName()+" : "+ex.getMessage());
        }
        System.out.println();

        try {
            System.out.println("3: TriX");
            Model model = ModelFactory.createDefaultModel();
            model.read("file:CVE/trix-bad1.trix");
            System.out.println("3: model.read(TriX) returned normally");
        } catch (Throwable ex) {
            System.out.println("3: Exception: "+ex.getClass().getSimpleName()+" : "+ex.getMessage());
        }
        System.out.println();

        try {
            System.out.println("4: TriX");
            Model model = ModelFactory.createDefaultModel();
            model.read("file:CVE/trix-bad2.trix");
            System.out.println("4: model.read(TriX) returned normally");
        } catch (Throwable ex) {
            System.out.println("4: Exception: "+ex.getClass().getSimpleName()+" : "+ex.getMessage());
        }
        System.out.println();

        try {
            System.out.println("5: SRX");
            ResultSetFactory.load("file:CVE/bad1-srx.srx");
            System.out.println("5: ResultSetFactory.load returned normally");
        } catch (Throwable ex) {
            System.out.println("5: "+ex.getMessage());
        }
        System.out.println();

        try {
            System.out.println("6: SRX");
            ResultSetFactory.load("file:CVE/bad2-srx.srx");
            System.out.println("6: ResultSetFactory.load returned normally");
        } catch (Throwable ex) {
            System.out.println("6: "+ex.getMessage());
        }
    }
}
