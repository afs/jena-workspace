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

import arq.qexpr;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.iri.IRI;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.sparql.graph.GraphFactory;
import org.seaborne.rfc3986.IRI3986;
import org.seaborne.rfc3986.RFC3986;

public class DevIRIx {

   /*
    * URI: https://tools.ietf.org/html/rfc3986
    * HTTP:     https://tools.ietf.org/html/rfc7230
    * URN:      https://tools.ietf.org/html/rfc8141
    * UUID:     https://tools.ietf.org/html/rfc4122 (as urn:uuid:)
    * ORDiD:
    * DID:      https://www.w3.org/TR/did-core/ (-- not very constraining)
    * file:     https://tools.ietf.org/html/rfc8089#section-2
    *           Differences to https://tools.ietf.org/html/rfc8089#appendix-A
    *           https://tools.ietf.org/html/rfc1738
    *
    * Consequences:
    *   <urn:nid:> as base
    *   <urn:> as base
    */

    // wm:/foo
    // jena-arq-3.18.0-20210205.073020-31.jar

    // Outstanding:

    // ==== Part 1 [DONE]
    // == Goal: IRIx in RIOT, using jena-iri.
    // [y] NodeFunctions.java      /jena-arq/src/main/java/org/apache/jena/sparql/expr/nodevalue   line 406
    // [ ] Checker. Not used in "riot".
    //       RDFParserBuilder -> ParserProfileStd has the check flag. -> does not go to Checker.
    //       Add ParserProfileStd. Also a StreamRDF.
    // [ ] RDFParserBuilder.resolver(IRIxResolver)

    // ---- Enough ----
    /* Import jena.iri ==>
     *   iri.java                     jena-cmds/src/main/java/arq (3 matches)
     *   IRIValidatorHTML.java        jena-fuseki-core/src/main/java/org/apache/jena/fuseki/validation/html (3 matches)
     *   IRIValidatorJSON.java        jena-fuseki-core/src/main/java/org/apache/jena/fuseki/validation/json (3 matches)
     */

    /*
     * IRIResolver.iriFactory
     *   [x] FmtUtils.java abbrevByBase
     *   [Leave] ReaderRIOTRDFXML.java          -- line 165
     *
     *  Fuseki services.
     *    IRIValidatorHTML.java
     *    IRIValidatorJSON.java
     */

    // -----
    // old org.apache.jena.iri => org.apache.jena.iri0
    // new org.apache.jena.irix => org.apache.jena.iri

    // ==== Part 2
    // == Goal: IRIx everywhere, based on IRI3986
    // Investigate:

    // [ ] relative03

    // [ ] Track down IRI again
    // [ ] Extra IRIx: "IRIxError", "IRIxNull"

    // [ ] JENA-2029: IRILib and filename -> "/"

    // ==== Part 3
    // == Maturity
    // [ ] Other resolvers
    //     "error IRI"

    // OTHER

    // [ ] IRIx.getErrors, getWarnings. Dummy "did not parser" IRIx class.

    static { LogCtl.setLogging(); }

    public static void main(String... a) {

           qexpr.main("iri('_:hereByDragons')");

//        riotcmd.riot.main("--check", "/home/afs/tmp/D.ttl");
//        System.exit(0);
        {

//            SystemIRIx.setProvider(new IRIProviderJDK());
//            IRIx jdk = IRIx.create("wm:/abcde");
//            IRIx jdk2 = IRIs.getSystemBase().resolve(jdk);
//            System.out.println("JDK: "+jdk2);

            //SystemIRIx.setProvider(new IRIProvider3986());
//            String x = "@prefix msg:   <https://w3id.org/won/message#> . <wm:/jq15ga3aacsxbvl9nngw> a msg:Message .";
//            StreamRDF dest = StreamRDFLib.print(System.out);
//            RDFParser.create().fromString(x).lang(Lang.TTL).parse(dest);

            IRIx iri = IRIx.create("wm:/abcde#graph");
            IRIx iri2 = iri.resolve("#xyz");
            System.out.println("IRI2 = "+iri2);
            System.exit(0);
        }

        Graph g = GraphFactory.createDefaultGraph();

        RDFParser.create()
            //.checking(true)
            .source("/home/afs/tmp/D.ttl")
            .parse(g);

        RDFWriter.create()
            .lang(Lang.TTL)
            .base("http://ex/")
            .source(g)
            .set(RIOT.symTurtleOmitBase, true)
            .output(System.out);
        //DataMgr.write(System.out, g,  "http://ex/", Lang.TTL);
        System.exit(0);

        {
            String s = "http://example/abc#frag";
            System.out.println("TEST: "+s);
            System.out.println();
            cmd.iri.CmdIRI.main(s);
            System.out.println();
            IRI3986 iri3 = RFC3986.create(s);

            System.out.println("3986: Path?: "+iri3.hasPath());
            System.out.println("3986: Path: '"+iri3.getPath()+"'");
            System.out.println("3986: Abs: "+iri3.isAbsolute());
            System.out.println("3986: Host: '"+iri3.getHost()+"'");
            System.out.println("3986: Rel: "+iri3.isRelative());
            System.out.println("3986: Root: "+iri3.isRootless());

            System.out.println("3986: Norm: "+iri3.normalize());
            System.out.println();

            IRI iri = IRIResolver.iriFactory().create(s);
            System.out.println("Errors?: "+iri.hasViolation(false));
            System.out.println("Abs: "+iri.isAbsolute());
            System.out.println("Rel: "+iri.isRelative());
            System.out.println("Root: "+iri.isRootless());
            System.out.println();
            //iri.normalize(false);
            IRIx irix = IRIx.create(s);
            //irix = irix.normalize();
            System.out.println("X: Abs: "+irix.isAbsolute());
            System.out.println("X: Rel: "+irix.isRelative());
            System.out.println("X: Ref: "+irix.isReference());

            System.exit(0);
        }

        dwimFilename("abc");
        dwimFilename(".");
        dwimFilename("abc/def");
    }

    private static void dwimFilename(String string) {
        //String iri1 = IRIResolver.resolveFileURL(string); // Dir has "/"
        String iri2 = IRILib.filenameToIRI(string);
        String iri3 = SysRIOT.chooseBaseIRI(string);
        //System.out.println("IRIResolver: "+iri1);
        System.out.println("IRILib:      "+iri2);
        System.out.println("SysRIOT:     "+iri3);
        System.out.println();
    }

//    0 ILLEGAL_CHARACTER                        : E:true  W:false
//    1 PERCENT_ENCODING_SHOULD_BE_UPPERCASE     : E:true  W:false
//    2 SUPERFLUOUS_NON_ASCII_PERCENT_ENCODING   : E:true  W:false
//    3 SUPERFLUOUS_ASCII_PERCENT_ENCODING       : E:true  W:false
//    4 UNWISE_CHARACTER                         : E:true  W:false
//    5 CONTROL_CHARACTER                        : E:true  W:false
//    8 NON_INITIAL_DOT_SEGMENT                  : E:true  W:false
//    9 EMPTY_SCHEME                             : E:true  W:false
//   10 SCHEME_MUST_START_WITH_LETTER            : E:true  W:false
//   11 LOWERCASE_PREFERRED                      : E:true  W:false
//   12 PORT_SHOULD_NOT_BE_EMPTY                 : E:true  W:false
//   13 DEFAULT_PORT_SHOULD_BE_OMITTED           : E:true  W:false
//   14 PORT_SHOULD_NOT_BE_WELL_KNOWN            : E:true  W:false
//   15 PORT_SHOULD_NOT_START_IN_ZERO            : E:true  W:false
//   16 BIDI_FORMATTING_CHARACTER                : E:true  W:false
//   17 WHITESPACE                               : E:true  W:false
//   18 DOUBLE_WHITESPACE                        : E:true  W:false
//   19 NOT_XML_SCHEMA_WHITESPACE                : E:true  W:false
//   25 IP_V6_OR_FUTURE_ADDRESS_SYNTAX           : E:true  W:false
//   26 IPv6ADDRESS_SHOULD_BE_LOWERCASE          : E:true  W:false
//   27 IP_V4_OCTET_RANGE                        : E:true  W:false
//   28 NOT_DNS_NAME                             : E:true  W:false
//   29 USE_PUNYCODE_NOT_PERCENTS                : E:true  W:false
//   30 ILLEGAL_PERCENT_ENCODING                 : E:true  W:false
//   33 DNS_LABEL_DASH_START_OR_END              : E:true  W:false
//   34 BAD_IDN_UNASSIGNED_CHARS                 : E:true  W:false
//   35 BAD_IDN                                  : E:true  W:false
//   36 HAS_PASSWORD                             : E:true  W:false
//   37 DISCOURAGED_IRI_CHARACTER                : E:true  W:false
//   38 BAD_BIDI_SUBCOMPONENT                    : E:true  W:false
//   44 UNREGISTERED_IANA_SCHEME                 : E:true  W:false
//   45 UNREGISTERED_NONIETF_SCHEME_TREE         : E:true  W:false
//   46 NOT_NFC                                  : E:true  W:false
//   47 NOT_NFKC                                 : E:true  W:false
//   48 DEPRECATED_UNICODE_CHARACTER             : E:true  W:false
//   49 UNDEFINED_UNICODE_CHARACTER              : E:true  W:false
//   50 PRIVATE_USE_CHARACTER                    : E:true  W:false
//   51 UNICODE_CONTROL_CHARACTER                : E:true  W:false
//   52 UNASSIGNED_UNICODE_CHARACTER             : E:true  W:false
//   55 UNICODE_WHITESPACE                       : E:true  W:false
//   56 COMPATIBILITY_CHARACTER                  : E:true  W:false

}

