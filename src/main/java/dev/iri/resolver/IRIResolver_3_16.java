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

package dev.iri.resolver;

import static dev.iri.resolver.IRIResolverLib.printSettings;
import static dev.iri.resolver.IRIResolverLib.setErrorWarning;

import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.ViolationCodes;

/** Setup of IRIResolver from jena 3.16.0 */
public class IRIResolver_3_16 {
    private static boolean         showExceptions    = true;

    private static final boolean   ShowResolverSetup = false;

    private static final IRIFactory iriFactoryInst = new IRIFactory();
    static {
        // These two are from IRIFactory.iriImplementation() ...
        iriFactoryInst.useSpecificationIRI(true);
        iriFactoryInst.useSchemeSpecificRules("*", true);

        // Allow relative references for file: URLs.
        iriFactoryInst.setSameSchemeRelativeReferences("file");

        // Convert "SHOULD" to warning (default is "error").
        // iriFactory.shouldViolation(false,true);

        if ( ShowResolverSetup ) {
            System.out.println("---- Default settings ----");
            printSettings(iriFactoryInst);
        }

        // Accept any scheme.
        setErrorWarning(iriFactoryInst, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);

        // These are a warning from jena-iri motivated by problems in RDF/XML and also internal processing by IRI
        // (IRI.relativize).
        // The IRI is valid and does correct resolve when relative.
        setErrorWarning(iriFactoryInst, ViolationCodes.NON_INITIAL_DOT_SEGMENT, false, false);

        // Turn off?? (ignored in CheckerIRI.iriViolations anyway).
        // setErrorWarning(iriFactory, ViolationCodes.LOWERCASE_PREFERRED, false, false);
        // setErrorWarning(iriFactory, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE, false, false);
        // setErrorWarning(iriFactory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED, false, false);

        // NFC tests are not well understood by general developers and these cause confusion.
        // See JENA-864

        // NFC is in RDF 1.1 so do test for that.
        // https://www.w3.org/TR/rdf11-concepts/#section-IRIs
        // Leave switched on as a warning.
        //setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFC,  false, false);

        // NFKC is not mentioned in RDF 1.1. Switch off.
        setErrorWarning(iriFactoryInst, ViolationCodes.NOT_NFKC, false, false);

        // ** Applies to various unicode blocks.
        setErrorWarning(iriFactoryInst, ViolationCodes.COMPATIBILITY_CHARACTER, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, false);
        // The set of legal characters depends on the Java version.
        // If not set, this causes test failures in Turtle and Trig eval tests.
        setErrorWarning(iriFactoryInst, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER, false, false);

        if ( ShowResolverSetup ) {
            System.out.println("---- After initialization ----");
            printSettings(iriFactoryInst);
        }
    }
}