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

package archive;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.Violation;
import org.apache.jena.iri.ViolationCodes;
import org.apache.jena.iri.impl.AbsIRIImpl;
import org.apache.jena.iri.impl.PatternCompiler;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.*;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;


public class Jena1924_ucschar {
    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {


//        Tokenizer tok = //TokenizerFactory.makeTokenizerString(" 123\n   456");
//            TokenizerText.create()
//                .fromString("<x:\\uFFFC> 123\n   456")
//                .errorHandler(ErrorHandlerFactory.errorHandlerWarn)
//                .build();
//
//        while(tok.hasNext()) {
//            Token t = tok.next();
//            System.out.printf("[%d, %d] %s\n", t.getLine(), t.getColumn(), t);
//        }
//        System.out.println("DONE");
//        System.exit(0);

        turtle();

        // See also JENA1924_ucschar_extra
        // JENA-1924 TokenizerText
        // TokenizerFactory
        // Fixup RiotParsers

        //TokenizerText.Builder.


        // Conclusion jena-iri is broken - does see non-chars. Thinks it is XML-legal?
        // error(IRI_CHAR) does not seem to happen. rule(4)
        // Possibly because not followed by error(ILLEGAL_CHARACTER)

        //iriParser();        System.exit(0);
        //parserProfile();
        token();
        //turtle();
        //chars();
    }

    public static void token() {
        //System.out.println("** Token");
        //String iriStr = "<x:\\U000E01EF>";
        // Good
        String[] iriStrings = { "<x:\\uFFEF>"/*good*/, "<x:\\uFFFC>"/*bad*/, "<x:\\U000E01EF>"/*bad*/, "<x:\\U000E1000>"/*good*/ };
        for ( String iriStr : iriStrings ) {
            System.out.println("** Token: "+iriStr);
            byte[] bytes2 = iriStr.getBytes(StandardCharsets.UTF_8);
            InputStream in = new ByteArrayInputStream(bytes2);
            //Tokenizer tok = TokenizerFactory.makeTokenizerUTF8(in);
            Tokenizer tok = TokenizerText.fromString(iriStr);
            try {
                Token t = tok.next();
                Node n1 = t.asNode();
                System.out.println("node for token: "+NodeFmtLib.displayStr(n1));
            }
            catch(RiotParseException ex) {
                System.out.println("ERROR: "+ex.getMessage());
            }
        }
    }

    public static void turtle() {
        System.out.println("** Turtle");
        //String s = "<http://pl.dbpedia.org/resource/A_\uFFFC_Z> <x:p> 123 .";
        String s = "<x:AA\uFFFCZZ> <x:p> 123 .";
        //String s = "<x:AA\\U000E01EF> <x:p> 123 .";

        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(bytes);
        System.out.println("Turtle");

        try {
            RDFParser.create()
                .lang(Lang.TTL)
                .strict(true)
                //.checking(true)
                .source(in)
                .parse(StreamRDFLib.writer(System.out));
        }
        catch(RiotException ex) {
            System.out.println("ERROR: "+ex.getMessage());
        }
        System.out.println();
    }

    public static void iriParser() {
        String s = "http://ex/\uFFFC";

        System.out.println("IRI Parser");
        System.err.println("Re-enable");
//        try {
//            Parser.devParse(s);
//            //NON_URI_CHARACTER
//        } catch (IOException e) {
//            System.out.flush();
//            e.printStackTrace();
//        }

    }


    public static void chars() {
        System.out.printf("** Chars");

//        char[] chars = {
//            //'\uFDD0',
//            '\uFFF0',
//            '\uFFFB',
//            '\uFFFC',
//            '\uFFFD',
//            '\uFFFE',
//            '\uFFFF'
//        };
        //char[] chars = { '\uFFFB', '\uFFFC', '\uFFFD' };
        char[] chars = {
            '\uFFEF', '\uFFFC'
            //Yes, errors, '\uF980' , '\uF8FF' // -- private chars
//            , '\uFDD0' , '\uFDEF'
//            , '\uFFF0' , '\uFFFF'


        };
        for ( char ch : chars ) {
            if ( ch == 0 )
                continue;
            //AbsLexer.difficultCodePoint
            // error(IRI_CHAR)
            //ViolationCodes.COMPATIBILITY_CHARACTER;
            // ucschar        = %xA0-D7FF / %xF900-FDCF / %xFDF0-FFEF
            System.out.printf("Character = 0x%02X", (int)ch);
            String s = "http://ex/#"+ch;
            IRIFactory iriFactory = new IRIFactory();
            setup(iriFactory);
            IRI iri = iriFactory.create(s);

            //System.out.println(NodeFmtLib.displayStr(NodeFactory.createURI(iri.toString())));
            System.out.printf(" ; Violations = %s", iri.hasViolation(true)) ;
            System.out.println();

            //((AbsIRIImpl) iri).allViolations();
            //for ( Iterator<Violation> vIter = iri.violations(true) ;

            for ( Iterator<Violation> vIter = ((AbsIRIImpl) iri).allViolations();
                  vIter.hasNext() ; )
            {
                Violation v = vIter.next();
                if ( v.isError() )
                    System.out.print("ERROR ");
                else
                    System.out.print("WARN  ");
                System.out.println(v.getShortMessage()) ;
            }
            //System.out.println();
        }


        System.out.print("DONE");
        System.exit(0);
    }

    public static void parserProfile() {
        System.out.println("** Parser Profile");
        String iriStr = "x:AA\uFFFCZZ";
        FactoryRDF factory = new FactoryRDFStd();
        IRIResolver resolver = IRIResolver.create("http://example/");

        ParserProfile profile = new ParserProfileStd(factory,
            ErrorHandlerFactory.errorHandlerStd,
            resolver,
            PrefixMapFactory.create(),
            ARQ.getContext(),
            true/*checking*/,
            true/*strict*/);
        Node node = profile.createURI(iriStr, -1, -1);
        System.out.println(NodeFmtLib.displayStr(node));
        String n1 = node.getURI();
        // [120, 58, 65, 65, 92, 117, 70, 70, 70, 67, 90, 90]
        //    x   :   A   A  .......................   Z   Z
        System.out.println();
    }

    public static void main1() throws IOException {
        if ( false ) {
            String str = "\uFFFC";
            System.out.println(str.length());
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            for ( byte b : bytes )
                System.out.printf(" %02X", b);
            System.out.println();
            String x = Bytes.asHex(bytes);
            System.out.println(x);
            System.out.println();
        }

        //System.exit(0);

        //String iriStr = "x:AA\\uFFFCZZ";
        if ( false ) {
            String iriStr = "<x:AA\uFFFCZZ>";
            byte[] bytes = iriStr.getBytes(StandardCharsets.UTF_8);
        }

        if ( true ) {
            System.out.println("Direct");
            String iriStr = "<x:AA\uFFFCZZ>";
            FactoryRDF factory = new FactoryRDFStd();
            IRIResolver resolver = IRIResolver.create("http://example/");

            ParserProfile profile = new ParserProfileStd(factory,
                                                         ErrorHandlerFactory.errorHandlerStd,
                                                         resolver,
                                                         PrefixMapFactory.create(),
                                                         ARQ.getContext(),
                                                         true/*checking*/,
                                                         true/*strict*/);
            Node node = profile.createURI(iriStr, -1, -1);
            System.out.println(NodeFmtLib.displayStr(node));
            String n1 = node.getURI();
            // [120, 58, 65, 65, 92, 117, 70, 70, 70, 67, 90, 90]
            //    x   :   A   A  .......................   Z   Z
            System.out.println();
        }

        if ( false ) {
            System.out.println("Character");

            //char[] chars = { '\uFFFA', '\uFFFB', '\uFFFC', '\uFFFD', '\uFFFE', '\uFFFF' };
            //char[] chars = { '\uFFFB', '\uFFFC', '\uFFFD' };
            char[] chars = { '\uFFFC' };
            for ( char ch : chars ) {
                System.out.printf("(int) _%02X_\n", (int)ch);
                Character character = Character.valueOf(ch);
                String s = Character.toString(ch);
                System.out.printf("charAt _%02X_\n", (int)s.charAt(0));
                System.out.println("CH:_"+character+"_");

                StringBuilder buffer = new StringBuilder();
                char xch = ch ;
                buffer.append(xch);
                String s1 = buffer.toString();
                System.out.printf("charAt _%02X_\n", (int)s1.charAt(0));
            }

            //System.exit(0);
        }

        if ( true ) {
            System.out.println("Token");
            String iriStr2 = "<x:\uFFFC>";
            byte[] bytes2 = iriStr2.getBytes(StandardCharsets.UTF_8);
            InputStream in = new ByteArrayInputStream(bytes2);
            //Tokenizer tok = TokenizerFactory.makeTokenizerString(iriStr2);

            // *** TokenizerText.Checking = false;

//            Tokenizer tok = TokenizerFactory.makeTokenizerUTF8(in);
//            Token t = tok.next();
//            Node n = t.asNode();
            Node n2 = NodeFactory.createURI(iriStr2);
            String s2 = n2.getURI();
            // [120, 0, 58, 0, 65, 0, 65, 0, -4, -1, 90, 0, 90, 0]
            //    x      :      A      A     ......   Z      Z
            System.out.println(NodeFmtLib.displayStr(n2));
            System.out.println();
        }

        // ----
        if ( false ) {
            String str = "\uFFFC";
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            InputStream in = new ByteArrayInputStream(bytes);
            Reader r = IO.asUTF8(in) ;
            for(;;) {
                int x = r.read();
                if ( x == -1 )
                    break;
                System.out.printf("0x%02X\n", x);
            }
            System.out.println("--");
            in = new ByteArrayInputStream(bytes);
            PeekReader preader = PeekReader.makeUTF8(in);
            for(;;) {
                int x = preader.read();
                if ( x == -1 )
                    break;
                System.out.printf("0x%02X\n", x);
            }

        }
        //PeekReader preader = PeekReader


    }

    private static void printEx(IRI iri) {
        System.out.println(NodeFmtLib.displayStr(NodeFactory.createURI(iri.toString())));
        Iterator<Violation> vIter = iri.violations(false) ;
        for ( ; vIter.hasNext() ; )
        {
            Violation v = vIter.next();
            if ( v.isError() )
                System.out.print("ERROR ");
            else
                System.out.print("WARN  ");
            System.out.println(v.getShortMessage()) ;
        }
    }

    private static boolean         showExceptions    = true;

    private static final boolean   ShowResolverSetup = false;

    public static void setup(IRIFactory iriFactoryInst) {
     // These two are from IRIFactory.iriImplementation() ...
        iriFactoryInst.useSpecificationIRI(true);
        iriFactoryInst.useSchemeSpecificRules("*", true);

        // Allow relative references for file: URLs.
        iriFactoryInst.setSameSchemeRelativeReferences("file");

        // Convert "SHOULD" to warning (default is "error").
        // iriFactory.shouldViolation(false,true);

        if ( ShowResolverSetup ) {
            System.out.println("---- Default settings ----");
            printSetting(iriFactoryInst);
        }

        setErrorWarning(iriFactoryInst, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);
        setErrorWarning(iriFactoryInst, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, true, true);
        setErrorWarning(iriFactoryInst, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER, true, true);
        setErrorWarning(iriFactoryInst, ViolationCodes.ILLEGAL_CHARACTER, true, true);
        // IRI vs URI
        //setErrorWarning(iriFactoryInst, ViolationCodes.NON_URI_CHARACTER, true, true);

        // ------------
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

//        // ** Applies to various unicode blocks.
//        setErrorWarning(iriFactoryInst, ViolationCodes.COMPATIBILITY_CHARACTER, false, false);

        setErrorWarning(iriFactoryInst, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, false);
//        // The set of legal characters depends on the Java version.
//        // If not set, this causes test failures in Turtle and Trig eval tests.
        setErrorWarning(iriFactoryInst, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER, false, false);

        if ( ShowResolverSetup ) {
            System.out.println("---- After initialization ----");
            printSetting(iriFactoryInst);
        }
    }
    private static void setErrorWarning(IRIFactory factory, int code, boolean isError, boolean isWarning) {
        factory.setIsWarning(code, isWarning);
        factory.setIsError(code, isError);
    }

    private static void printSetting(IRIFactory factory) {
        PrintStream ps = System.out;
        printErrorWarning(ps, factory, ViolationCodes.UNREGISTERED_IANA_SCHEME);
        printErrorWarning(ps, factory, ViolationCodes.NON_INITIAL_DOT_SEGMENT);
        printErrorWarning(ps, factory, ViolationCodes.NOT_NFC);
        printErrorWarning(ps, factory, ViolationCodes.NOT_NFKC);
        printErrorWarning(ps, factory, ViolationCodes.UNWISE_CHARACTER);
        printErrorWarning(ps, factory, ViolationCodes.UNDEFINED_UNICODE_CHARACTER);
        printErrorWarning(ps, factory, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER);
        printErrorWarning(ps, factory, ViolationCodes.COMPATIBILITY_CHARACTER);
        printErrorWarning(ps, factory, ViolationCodes.LOWERCASE_PREFERRED);
        printErrorWarning(ps, factory, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE);
        printErrorWarning(ps, factory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED);
        ps.println();
    }

    private static void printErrorWarning(PrintStream ps, IRIFactory factory, int code) {
        String x = PatternCompiler.errorCodeName(code);
        ps.printf("%-40s : E:%-5s W:%-5s\n", x, factory.isError(code), factory.isWarning(code));
    }


}
