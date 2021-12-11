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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.atlas.io.InStreamUTF8;
import org.apache.jena.atlas.io.OutStreamUTF8;
import org.apache.jena.atlas.lib.Bytes;

public class NotesChars {

    /*

       RIOC in jena-base(core) or module/package or a few classs to copy.
          RiotChars
          Tokenizertext has it's own:
          isUcsChar
       iri4ld:Chars3986
       SplitIRI has RIOTChars copies.
     */

    // https://unicode.org/charts/PDF/UFFF0.pdf
    // 0xFFFE, 0xFFFF not a character

    // OBJECT REPLACEMENT CHARACTER
    // public static final char REPLACEMENT = 0xFFFC ;

    // REPLACEMENT CHARACTER
    public static final char REPLACEMENT = 0xFFFD;

/*
 * == blank node labels.
 *
 * == Prefix name.
 *    readSegment.
 *
 * == IRIs
 *   <> IRIs - change message.
 *   readIRI()
 *   insertCodePoint.
 *
 * == Strings
 *   readLongString()
 *   readString()
 *
 * insertCodepoint(StringBuilder, int) : void - org.apache.jena.riot.tokens.TokenizerText
 *    readIRI() : String - org.apache.jena.riot.tokens.TokenizerText
 *    readLongString(int, boolean) : String - org.apache.jena.riot.tokens.TokenizerText
 *    readString(int, int) : String - org.apache.jena.riot.tokens.TokenizerText
 *
 * For string: chars also read in:
 *   readLiteralEscape
 *   readUnicode4Escape
 *   readUnicode8Escape
 * OR in PeekReader which knows line / column
 */

    public static void main(String...a) throws IOException {
        // Unicode: df 2d

        byte[] bytes = {(byte)0xEF, (byte)0xBF, (byte)0xBD};
        //byte[] bytes = {(byte)0xDF, (byte)0x2D, (byte)0x20, (byte)0x20};

        // DF in ISO 8859-1 U+00DF is UTF-8 0xC39F : Uppercase is U+1E9E
        // DF in UTF8 needs encoding.

        String s = InStreamUTF8.decode(bytes);
        System.out.println(s);
        int ch = s.codePointAt(0);
        System.out.printf("0x%04X\n", ch);


        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] output = OutStreamUTF8.encode('ß');
        String x = Bytes.asHex(output);
        System.out.println(x);
//        System.out.println("Write bad binary");
//
//        try ( FileOutputStream outf = new FileOutputStream("DATA") ) {
//            PrintStream ps = new PrintStream(outf);
//            ps.println("PREFIX : <http://example>");
//            ps.print(":s :p ");
//            ps.flush();
//            outf.write('"');
//            outf.write(0xDF);
//            outf.write('"');
//            outf.flush();
//            ps.println(" .");
//        }
    }
}
