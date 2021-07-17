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

package chars;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import org.apache.jena.atlas.io.CharStreamBuffered;
import org.apache.jena.atlas.io.InStreamUTF8;
import org.apache.jena.atlas.io.OutStreamUTF8;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;

public class ReportSurrogatePairs {
//    static {
//        JenaSystem.init();
//        FusekiLogging.setLogging();
//        //LogCtl.setLog4j2();
//        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
//    }

    static String DIR = "SurrogatePairs/";
    static String BINARY_DATA = DIR+"SurrogatePairData.bin";
    static String RDF_DATA = "SurrogatePairs/";

    public static void main(String[] args) throws IOException {

//        showDifferent();
//        System.out.println();
//
//        showBytes();
//        System.out.println();
//
//        showJavaChars();
//        System.out.println();
//
//        // GOOD!
//        showInStreamUTF8();
//        System.out.println();
//
//        showCorrectString();
//        System.out.println();
//
//        showCorrectBytes();
//        System.out.println();
//
//        triples();
//        System.out.println();

        tokens();
        System.out.println();

        jenaIO();
        System.out.println();
    }

    private static void showDifferent() throws FileNotFoundException, IOException {
        System.out.println("== Decode");
        System.out.println("-- Decode (decoder)");
        try ( InputStream in = new FileInputStream(BINARY_DATA) ) {
            CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder();
            Reader r = new InputStreamReader(in, dec);
            int ch1 = r.read();
            int ch2 = r.read();
            System.out.printf("   %02X %02X\n", ch1, ch2);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("-- Decode (charset)");
        try ( InputStream in = new FileInputStream(BINARY_DATA) ) {
            Reader r = new InputStreamReader(in, StandardCharsets.UTF_8);
            int ch1 = r.read();
            int ch2 = r.read();
            System.out.printf("   %02X %02X\n", ch1, ch2);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void tokens() throws IOException {
        System.out.println("== RIOT tokens");

        // WRONG
        System.out.println("-- newDecoder");
        try ( InputStream in = new FileInputStream(DIR+"SurrogatePair.tok") ) {
            CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder();
            Reader r = new InputStreamReader(in, dec);
            Tokenizer tokenizer = TokenizerText.create().source(r).build();
            Token token = tokenizer.next();
            System.out.println(token.getImage());
        } catch (Throwable ex) {
            System.out.flush();
            ex.printStackTrace();
        }

        // RIGHT
        System.out.println("-- StandardCharsets");
        try ( InputStream in = new FileInputStream(DIR+"SurrogatePair.tok") ) {
            Reader r = new InputStreamReader(in, StandardCharsets.UTF_8);
            Tokenizer tokenizer = TokenizerText.create().source(r).build();
            Token token = tokenizer.next();
            System.out.println(token.getImage());
        }

        // Jena setup : input stream.
        System.out.println("-- Codebase:InputStream");
        try ( InputStream in = new FileInputStream(DIR+"SurrogatePair.tok") ) {
            Tokenizer tokenizer = TokenizerText.create().source(in).build();
            Token token = tokenizer.next();
            System.out.println(token.getImage());
        }
    }

    public static void jenaIO() throws IOException {
        System.out.println("== RIOT I/O");
        Reader r = new FileReader(DIR+"SurrogatePair.tok", StandardCharsets.UTF_8);
        // REPLACE
        // + CharStremaBuffered$SourceReader
        // + Reader is a FileReader (dft).

        try ( CharStreamBuffered cs = new CharStreamBuffered(r) ) {
            for(;;) {
                int x = cs.advance();
                if ( x == -1 )
                    break;
                System.out.printf(" %02X", x);
            }
            System.out.println();
        }
        System.out.println("== RIOT I/O - end");
    }

    public static void triples() throws IOException {
        System.out.println("== RIOT parser");
        // 3 byte surrogate pairs.
        Graph g = RDFDataMgr.loadGraph(DIR+"SurrogatePair.nt");
        Triple triple = g.find().next();
        String lex = triple.getObject().getLiteralLexicalForm();
        System.out.println("Lex='"+lex+"'");
    }

    public static void showBytes() throws IOException {
        // 3 byte surrogate pairs.
        // Rejected - java8->
        System.out.println("== Bytes");
        try ( InputStream in = new FileInputStream(BINARY_DATA) ) {
            // 6. 2*3 UTF-8 surrogate pairs.
            for(int i = 0 ; ; i++ ) {
                int x = in.read();
                if ( x == -1 )
                    break;
                System.out.printf(" %02X", x);
            }
        }
        System.out.println();
    }

    public static void showJavaChars() throws IOException {
        System.out.println("== Java chars");
        InputStream in = new FileInputStream(BINARY_DATA);
        try ( Reader r = new InputStreamReader(in,StandardCharsets.UTF_8) ) {
            int ch1 = r.read();
            int ch2 = r.read();
            System.out.printf("   %02X %02X\n", ch1, ch2);
            int ch3 = r.read();
            if ( ch3 != -1 )
                System.out.println("  BAD: Not EOF");
        }
    }

    public static void showInStreamUTF8() throws IOException {
        System.out.println("== InStreamUTF8 chars");
        try ( Reader r = new InStreamUTF8(new FileInputStream(BINARY_DATA)) ) {
            int ch1 = r.read();
            int ch2 = r.read();
            System.out.printf("   %02X %02X\n", ch1, ch2);
        }
    }

    public static void showCorrectString() {
        System.out.println("== Correct surrogate pair");
        String s = "\uD83D\uDD14";
        System.out.println(s);
        int codepoint = s.codePointAt(0);
        System.out.printf("   Codepoint %06X\n", codepoint);
    }

    public static void showCorrectBytes() throws IOException {
        System.out.println("== Correct encoding in bytes");
        int codepoint = 0x01F514;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutStreamUTF8.output(out, codepoint);
        byte[] bytes = out.toByteArray();
        System.out.printf("   Len = %d\n", bytes.length);
        System.out.print("  ");
        for ( int i = 0 ; i< bytes.length ; i++ ) {
            int x = bytes[i];
            x = x & 0xFF;
            System.out.printf(" %02X", x);
        }
        System.out.println();
    }

    static public Reader asUTF8(InputStream in) {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        return new InputStreamReader(in, decoder);
    }
}
