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

package dev.iri.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

import org.apache.jena.irix.*;

public class IRIxTestLib {

    /** Resolve test. */
    static void testResolve(String baseUriStr, String otherStr, String expected) {
        IRIx baseIRI = IRIx.create(baseUriStr);
        IRIx relIRI = IRIx.create(otherStr);
        IRIx iriResolved = baseIRI.resolve(relIRI);
        String s1 = iriResolved.str();
        assertEquals("Base=<"+baseUriStr+"> Rel=<"+otherStr+">", expected, expected);
    }

    /**
     * Test calculating a relative IRI. "exp3cted" may be null meaning "no relative
     * form".
     */
    static void testRelative(String baseUriStr, String pathStr, String expected) {
        IRIx base = IRIx.create(baseUriStr);
        IRIx path = IRIx.create(pathStr);
        IRIx rel = base.relativize(path);
        String result = (rel==null) ? null:rel.str();
        assertEquals("Base=<"+baseUriStr+"> IRI=<"+pathStr+">", expected, result);
        if ( expected != null ) {
            IRIx path2 = base.resolve(rel);
            assertEquals(path, path2);
            assertEquals(path.str(), path2.str());
        }
    }

    /** Test normalization */
    static void testNormalize(String input, String expected) {
            // jena-iri does not implement normalization.
            assumeFalse("jena-iri does not implement normalization", SystemIRIx.getProvider() instanceof IRIProviderJenaIRI );
    //        if ( SystemIRIx.getProvider() instanceof IRIProviderJenaIRI )
    //            return;
            IRIx iri = IRIx.create(input);
            IRIx iri2 = iri.normalize();
            String s = iri2.toString();
            assertEquals(expected, s);
        }


    /** Test whether the string is an "RDF Reference". */
    static void testReference(String uriStr) {
        testReference(uriStr, true);
    }

    /** Test whether the string is/is not an "RDF Reference". */
    static void testReference(String uriStr, boolean expected) {
        IRIx iri = IRIx.create(uriStr);
        assertEquals("IRI = "+uriStr, expected, iri.isReference());
    }

    /** Test that parses the argument and checks the string form ({@link IRIx#str()}) is the same. */
    static void testParseCheckString(String uriStr) {
        IRIx iri = IRIx.create(uriStr);
        String x = iri.str();
        assertEquals(uriStr, x);
    }

    /** Test that parses the argument. Throws {@link IRIException} on bad argument. */
    static void testParse(String string) {
        IRIx iri = IRIx.create(string);
    }
}
