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

import static dev.iri.test.IRIxTestLib.testReference;

import dev.iri.resolver.IRIResolver_3_17;
import org.apache.jena.iri.IRI;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIProvider;
import org.apache.jena.riot.checker.CheckerIRI;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Collection of miscellaneous tests.
 * Real world situations.
 */

@RunWith(Parameterized.class)
public class TestMiscIRI extends AbstractTestIRIx2 {

    public TestMiscIRI(String name, IRIProvider provider) {
        super(name, provider);
    }

    @Test public void otherScheme_1()   { testReferenceX("wm:/abc"); }

    @Test public void wellKnownPort()   { testReferenceX("http://host:80/path"); }

    @Test public void emptyPort()       { testReferenceX("http://host:/path"); }

    @Test(expected=IRIException.class)
    public void user1()                 { testReferenceX("http://user@host/path"); }

    @Test
    public void user1a() {
        notStrict("http", () -> testReferenceX("http://user@host/path"));
    }

    @Test public void user2()          { testReferenceX("cvs://user@host/path"); }

    //@Test public void password1()        { testReferenceX("http://password:user@host/path"); }

    @Test
    public void password1a() {
        notStrict("http", () -> testReferenceX("http://password:user@host/path"));
    }

    @Test public void password2()        { testReferenceX("csv://password:user@host/path"); }

    // wikidata related.

    /* Surrogates
23:22:03 WARN  riot            :: [line: 5422552800, col: 120] Illegal character in IRI (Not a ucschar: 0xD83C): <https://en.wiktionary.org/wiki/[U+D83C]...>
23:22:03 WARN  riot            :: [line: 5422552800, col: 121] Illegal character in IRI (Not a ucschar: 0xDFAE): <https://en.wiktionary.org/wiki/?[U+DFAE]...>
23:22:04 WARN  riot            :: [line: 5422732425, col: 120] Illegal character in IRI (Not a ucschar: 0xD83D): <https://en.wiktionary.org/wiki/[U+D83D]...>
23:22:04 WARN  riot            :: [line: 5422732425, col: 121] Illegal character in IRI (Not a ucschar: 0xDE97): <https://en.wiktionary.org/wiki/?[U+DE97]...>

     */

    /*
    28 Code:0   ILLEGAL_CHARACTER
      2 fragments
      ":" in host -- http://:www.bjbcollege.in

   126 Code:12  PORT_SHOULD_NOT_BE_EMPTY        <http://http://www.jesusandmarycollegeofeducation.com>
      HTTP rule. Legal but.

   411 Code:13  DEFAULT_PORT_SHOULD_BE_OMITTED  <https://digital.lib.washington.edu:443/researchworks/bitstream/1773/33260/1/EmpiricalEvaluation_EDIT.pdf>
      HTTP rule. Legal but.

   1018 Code:14  PORT_SHOULD_NOT_BE_WELL_KNOWN   <http://212.122.187.196:84/Process.aspx?type=Fund&agid=41&flgid=5088046>
         HTTP rule. Legal but.

    89 Code:28  NOT_DNS_NAME                    <http://www.murlidharMAHAVIDYALAYA,.com>
         DNS with comma

    16 Code:29  USE_PUNYCODE_NOT_PERCENTS       <http://younggu-art.com%20>
        Now legal.

     8 Code:30  ILLEGAL_PERCENT_ENCODING        <http://www.v-r.de/de/magazine-25-25/die%5Fwelt%5%Fdes%5Forients-500045/>

     5 Code:35  BAD_IDN                         <http://‘ŠÙ.com/>
    16 Code:36  HAS_PASSWORD                    <cvs://:pserver:cvs:@cvs.cvsnt.org:/cvsnt>
     6 Code:5   CONTROL_CHARACTER               <http://kaminomachi.jp/ŽO“‡‘ŠÙ/>
     5 Code:55  UNICODE_WHITESPACE              U+2082 :: <http://sfi-cybium.fr/fr/inventaire-systématique-des-blenniidae\u2082des-côtes-tunisiennes>
    16 Code:57  REQUIRED_COMPONENT_MISSING      <https:///aircampus.co>
    74 Code:58  PROHIBITED_COMPONENT_PRESENT    <http://roytzo@tauex.tau.ac.il>

   */

    @Test//(expected=IRIException.class)
    public void badDNS_1()        { testReferenceX("http://host,machine/path"); }

    @Test(expected=IRIException.class)
    public void badDNS_2()        { testReferenceX("http://:www.bjbcollege.in"); }

    @Test(expected=IRIException.class)
    public void badCtlChar()      { testReferenceX("http://example/abc\u0009def"); }

    @Test//(expected=IRIException.class)
    // This "accidently passes the grammar with host=http and empty port.
    public void bad()             { testReferenceX("http://http://example/abc"); }



    static void  testReferenceX(String string) {
        testReference(string);
        jenaIRI(string);
    }

    static void jenaIRI(String string) {
        IRI iri = IRIResolver_3_17.iriFactory().create(string);
        if ( false && iri.hasViolation(true) )
            CheckerIRI.iriViolations(iri, ErrorHandlerFactory.errorHandlerStd);
    }
}
