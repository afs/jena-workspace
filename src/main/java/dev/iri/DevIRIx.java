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

package dev.iri;

public class DevIRIx {}

//import java.io.PrintStream;
//
//import dev.iri.resolver.IRIResolver_3_17;
//import org.apache.jena.atlas.lib.IRILib;
//import org.apache.jena.atlas.logging.LogCtl;
//import org.apache.jena.graph.Graph;
//import org.apache.jena.iri.IRI;
//import org.apache.jena.iri.IRIFactory;
//import org.apache.jena.iri.impl.PatternCompiler;
//import org.apache.jena.irix.IRIProviderJenaIRI;
//import org.apache.jena.irix.IRIs;
//import org.apache.jena.irix.IRIx;
//import org.apache.jena.riot.Lang;
//import org.apache.jena.riot.RDFParser;
//import org.apache.jena.riot.RDFWriter;
//import org.apache.jena.riot.RIOT;
//import org.apache.jena.riot.system.IRIResolver;
//import org.apache.jena.sparql.graph.GraphFactory;
//import org.seaborne.rfc3986.IRI3986;
//import org.seaborne.rfc3986.RFC3986;
//
//public class DevIRIx {
//
//    // MORE
//    // [1] riot behaviour
//    //    CheckerIRI and IRIProviderJenaIRI jena-iri settings - different.
//    //    riot4 --check does not work.
//    //    riot3 --nocheck
//    //   Why do warnings come out? parser calls CheckerIRI.
//    // Some tests in "default mode" -- http, urn.
//
//    // Wikidata and jena4
//    // [2] Tests for wikidata
//    // [3] Prepare  "wikidata" special file.
//    //   What error messages occur?
//
//    // Handling of illegal %-encoding.
//    // [4] TestMiscIRI
//    // NTriples - "no error" URI provider? <.*>
//    // [5] Setup code (see print).
//
//    // [6] "riot -stream nt"
//    // [7] "riot" catch/suppress stacktraces for IRIException
//
//    // IRI3986: parse DNS names.
//
//    /*
//11:44:41 ERROR riot            :: [line: 4, col: 1 ] Bad IRI: <http://example/a#b#c> : <http://example/a#b#c> Code: 0/ILLEGAL_CHARACTER in FRAGMENT: The character violates the grammar rules for URIs/IRIs.
//org.apache.jena.irix.IRIException: <http://example/a#b#c> Code: 0/ILLEGAL_CHARACTER in FRAGMENT: The character violates the grammar rules for URIs/IRIs.
//    at org.apache.jena.irix.IRIProviderJenaIRI.exceptions(IRIProviderJenaIRI.java:234)
//    at org.apache.jena.irix.IRIProviderJenaIRI.create(IRIProviderJenaIRI.java:141)
//    at org.apache.jena.irix.IRIx.create(IRIx.java:54)
//    at org.apache.jena.riot.system.ParserProfileStd.internalMakeIRI(ParserProfileStd.java:103)
//     */
//    // internalMakeIRI -- caught something then croaked - Need "IRIx.createAny"
//
//    // ------------------ PREVIOUS
//
//    // ** IRIResolver.iriFactor vs IRIResolver.iriCheckerFactory
//
//    // N-triples: IRIProviderAny?
//
//    // ==== Error Handler support for IRIx
//    //  Long term
//    //  pass in handlers.
//
//    // Warnings and errors.
//    //   Warning only if --check. // RDFParserBuilder.checking(true);
//    //   Need tests.
//
//    // "TestIRIs"
//
//    // ==== Part 1 [DONE]
//    // == Goal: IRIx in RIOT, using jena-iri.
//    // [y] NodeFunctions.java      /jena-arq/src/main/java/org/apache/jena/sparql/expr/nodevalue   line 406
//    // [x] Checker.
//    // [x] RDFParserBuilder.resolver(IRIxResolver)
//
//    // ---- Enough ----
//    /* Import jena.iri ==>
//     *   iri.java                     jena-cmds/src/main/java/arq (3 matches)
//     *   IRIValidatorHTML.java        jena-fuseki-core/src/main/java/org/apache/jena/fuseki/validation/html (3 matches)
//     *   IRIValidatorJSON.java        jena-fuseki-core/src/main/java/org/apache/jena/fuseki/validation/json (3 matches)
//     */
//
//    /*
//     * IRIResolver.iriFactory
//     *   [x] FmtUtils.java abbrevByBase
//     *   [Leave] ReaderRIOTRDFXML.java          -- line 165
//     *
//     *  Fuseki services.
//     *    IRIValidatorHTML.java
//     *    IRIValidatorJSON.java
//     */
//
//    // ==== Part 2
//    // == Goal: IRIx everywhere, based on IRI3986
//    // Investigate:
//
//    // [ ] Track down IRI again
//    // [ ] JENA-2029: IRILib and filename -> "/"
//    // [ ] IRIx.getErrors, getWarnings. Dummy "did not parser" IRIx class.
//
//    static { LogCtl.setLogging(); }
//
//    public static void main(String... a) {
//
//        riotcmd.riot.main("/home/afs/tmp/wikidata.nt");
//        System.exit(0);
//
////        print(IRIResolver.iriFactory());
////        print(IRIResolver.iriCheckerFactory());
////        print(IRIProviderJenaIRI.iriFactory());
//
//        // Matrix
//        //printSetupMatrix();
//        //print(IRIProviderJenaIRI.iriFactory());
//        System.exit(0);
//
//        Graph g = GraphFactory.createDefaultGraph();
//
//        RDFParser.create()
//            //.checking(true)
//            //.resolver(resolver)
//            .source("/home/afs/tmp/D.ttl")
//            .parse(g);
//
//        RDFWriter.create()
//            .lang(Lang.TTL)
//            .base("http://ex/")
//            .source(g)
//            .set(RIOT.symTurtleOmitBase, true)
//            .output(System.out);
//        //DataMgr.write(System.out, g,  "http://ex/", Lang.TTL);
//        System.exit(0);
//
//        {
//            String s = "http://example/abc#frag";
//            System.out.println("TEST: "+s);
//            System.out.println();
//            cmd.iri.CmdIRI.main(s);
//            System.out.println();
//            IRI3986 iri3 = RFC3986.create(s);
//
//            System.out.println("3986: Path?: "+iri3.hasPath());
//            System.out.println("3986: Path: '"+iri3.getPath()+"'");
//            System.out.println("3986: Abs: "+iri3.isAbsolute());
//            System.out.println("3986: Host: '"+iri3.getHost()+"'");
//            System.out.println("3986: Rel: "+iri3.isRelative());
//            System.out.println("3986: Root: "+iri3.isRootless());
//
//            System.out.println("3986: Norm: "+iri3.normalize());
//            System.out.println();
//
//            IRI iri = IRIResolver.iriFactory().create(s);
//            System.out.println("Errors?: "+iri.hasViolation(false));
//            System.out.println("Abs: "+iri.isAbsolute());
//            System.out.println("Rel: "+iri.isRelative());
//            System.out.println("Root: "+iri.isRootless());
//            System.out.println();
//            //iri.normalize(false);
//            IRIx irix = IRIx.create(s);
//            //irix = irix.normalize();
//            System.out.println("X: Abs: "+irix.isAbsolute());
//            System.out.println("X: Rel: "+irix.isRelative());
//            System.out.println("X: Ref: "+irix.isReference());
//
//            System.exit(0);
//        }
//
//        dwimFilename("abc");
//        dwimFilename(".");
//        dwimFilename("abc/def");
//    }
//
//    private static void printSetupMatrix() {
//        IRIFactory fResolver = IRIResolver.iriFactory();
//        IRIFactory fChecker = IRIResolver.iriCheckerFactory();
//
//        IRIFactory fProviderJenaIRI = IRIProviderJenaIRI.iriFactory();
//        IRIFactory fJena317 = IRIResolver_3_17.iriFactory();
//
//        PrintStream ps = System.out;
//
//        //"%-2d %-40s : E:%-5s W:%-5s\n"
//        ps.print("                                          ");
//        ps.print("  IRIProviderJenaIRI IRIResolver_3_17   iriCheckerFactory  iriFactory");
//        ps.println();
//        for ( int code = 0 ; code < 128; code++) {
//            String x = PatternCompiler.errorCodeName(code);
//            if ( x != null ) {
//                ps.printf("%-2d %-38s : ", code, x);
//                ps.printf("E:%-5s W:%-5s", b(fProviderJenaIRI.isError(code)), b(fProviderJenaIRI.isWarning(code)));
//                ps.print("    ");
//                ps.printf("E:%-5s W:%-5s", b(fJena317.isError(code)), b(fJena317.isWarning(code)));
//                ps.print("    ");
//                ps.printf("E:%-5s W:%-5s", b(fChecker.isError(code)), b(fChecker.isWarning(code)));
//                ps.print("    ");
//                ps.printf("E:%-5s W:%-5s", b(fResolver.isError(code)), b(fResolver.isWarning(code)));
//                ps.println();
//            }
//        }
//    }
//
//    private static String b(boolean b) {
//        return b ? "true" : "";
//    }
//
//    // CheckerIROI
////    // Ignore these.
////    if ( code == Violation.LOWERCASE_PREFERRED
////         || code == Violation.PERCENT_ENCODING_SHOULD_BE_UPPERCASE
////         || code == Violation.SCHEME_PATTERN_MATCH_FAILED )
////        continue ;
//
//
//    private static void print(IRIFactory iriFactory) {
//        for ( int code = 0 ; code < 128; code++) {
//            printErrorWarning(System.out, iriFactory, code);
//        }
//        for ( int code = 0 ; code < 128; code++) {
//            printSetCode(System.out, iriFactory, code);
//        }
//
////        private static void printSetting(IRIFactory factory) {
////            PrintStream ps = System.out;
////            printErrorWarning(ps, factory, ViolationCodes.UNREGISTERED_IANA_SCHEME);
////            printErrorWarning(ps, factory, ViolationCodes.NON_INITIAL_DOT_SEGMENT);
////            printErrorWarning(ps, factory, ViolationCodes.NOT_NFC);
////            printErrorWarning(ps, factory, ViolationCodes.NOT_NFKC);
////            printErrorWarning(ps, factory, ViolationCodes.UNWISE_CHARACTER);
////            printErrorWarning(ps, factory, ViolationCodes.UNDEFINED_UNICODE_CHARACTER);
////            printErrorWarning(ps, factory, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER);
////            printErrorWarning(ps, factory, ViolationCodes.COMPATIBILITY_CHARACTER);
////            printErrorWarning(ps, factory, ViolationCodes.LOWERCASE_PREFERRED);
////            printErrorWarning(ps, factory, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE);
////            printErrorWarning(ps, factory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED);
////            ps.println();
//    }
//
//    // Print as code.
//    private static void printSetCode(PrintStream ps, IRIFactory factory, int code) {
//        //static void setErrorWarning(IRIFactory factory, int code, boolean isError, boolean isWarning) {
//
//        String x = PatternCompiler.errorCodeName(code);
//        if ( x != null )
//            ps.printf("        setErrorWarning(factory, ViolationCodes.%-40s, %-5s,  %-5s);\n",
//                      x, (factory.isError(code)), (factory.isWarning(code)));
//    }
//    private static void printErrorWarning(PrintStream ps, IRIFactory factory, int code) {
//        String x = PatternCompiler.errorCodeName(code);
//        if ( x != null )
//            ps.printf("%-2d %-40s : E:%-5s W:%-5s\n", code, x, factory.isError(code), factory.isWarning(code));
//    }
//
//
//
//    private static void dwimFilename(String string) {
//        //String iri1 = IRIResolver.resolveFileURL(string); // Dir has "/"
//        String iri2 = IRILib.filenameToIRI(string);
//        String iri3 = IRIs.toBase(string);
//        //System.out.println("IRIResolver: "+iri1);
//        System.out.println("IRILib:      "+iri2);
//        System.out.println("SysRIOT:     "+iri3);
//        System.out.println();
//    }
//
////    0 ILLEGAL_CHARACTER                        : E:true  W:false
////    1 PERCENT_ENCODING_SHOULD_BE_UPPERCASE     : E:true  W:false
////    2 SUPERFLUOUS_NON_ASCII_PERCENT_ENCODING   : E:true  W:false
////    3 SUPERFLUOUS_ASCII_PERCENT_ENCODING       : E:true  W:false
////    4 UNWISE_CHARACTER                         : E:true  W:false
////    5 CONTROL_CHARACTER                        : E:true  W:false
////    8 NON_INITIAL_DOT_SEGMENT                  : E:true  W:false
////    9 EMPTY_SCHEME                             : E:true  W:false
////   10 SCHEME_MUST_START_WITH_LETTER            : E:true  W:false
////   11 LOWERCASE_PREFERRED                      : E:true  W:false
////   12 PORT_SHOULD_NOT_BE_EMPTY                 : E:true  W:false
////   13 DEFAULT_PORT_SHOULD_BE_OMITTED           : E:true  W:false
////   14 PORT_SHOULD_NOT_BE_WELL_KNOWN            : E:true  W:false
////   15 PORT_SHOULD_NOT_START_IN_ZERO            : E:true  W:false
////   16 BIDI_FORMATTING_CHARACTER                : E:true  W:false
////   17 WHITESPACE                               : E:true  W:false
////   18 DOUBLE_WHITESPACE                        : E:true  W:false
////   19 NOT_XML_SCHEMA_WHITESPACE                : E:true  W:false
////   25 IP_V6_OR_FUTURE_ADDRESS_SYNTAX           : E:true  W:false
////   26 IPv6ADDRESS_SHOULD_BE_LOWERCASE          : E:true  W:false
////   27 IP_V4_OCTET_RANGE                        : E:true  W:false
////   28 NOT_DNS_NAME                             : E:true  W:false
////   29 USE_PUNYCODE_NOT_PERCENTS                : E:true  W:false
////   30 ILLEGAL_PERCENT_ENCODING                 : E:true  W:false
////   33 DNS_LABEL_DASH_START_OR_END              : E:true  W:false
////   34 BAD_IDN_UNASSIGNED_CHARS                 : E:true  W:false
////   35 BAD_IDN                                  : E:true  W:false
////   36 HAS_PASSWORD                             : E:true  W:false
////   37 DISCOURAGED_IRI_CHARACTER                : E:true  W:false
////   38 BAD_BIDI_SUBCOMPONENT                    : E:true  W:false
////   44 UNREGISTERED_IANA_SCHEME                 : E:true  W:false
////   45 UNREGISTERED_NONIETF_SCHEME_TREE         : E:true  W:false
////   46 NOT_NFC                                  : E:true  W:false
////   47 NOT_NFKC                                 : E:true  W:false
////   48 DEPRECATED_UNICODE_CHARACTER             : E:true  W:false
////   49 UNDEFINED_UNICODE_CHARACTER              : E:true  W:false
////   50 PRIVATE_USE_CHARACTER                    : E:true  W:false
////   51 UNICODE_CONTROL_CHARACTER                : E:true  W:false
////   52 UNASSIGNED_UNICODE_CHARACTER             : E:true  W:false
////   55 UNICODE_WHITESPACE                       : E:true  W:false
////   56 COMPATIBILITY_CHARACTER                  : E:true  W:false
//
//}
