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

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.atlas.lib.Chars;
import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.riot.system.RiotChars;

public class Encode {
    // URL encoding.
    // [ ] Does the query text get %-encoded for a queryString?
    //      Params::httpString
    //              formatEncodeHTTP
    //              encode
    //      HttpLib.urlEncodeQueryString
    //      IRILib.encodeUriQueryFrag
    //      StrUtils.encodeHex
    //        No %-binary

    //      Params::urlEncodeQueryString
    // [ ] QueryExecHTTP::executeQueryPostBody
    // Space as %20 or +

    // Encoders:
    //   IRILib.encodeUriComponent
    //      ENCODE_FOR_URI = fn:encode-for-uri - include ASCII-ization.
    //      encodeUriComponent, not non-ASCII (wrong)
    //   IRILib.encodeUriQueryFrag
    //      encodeUriQueryFrag
    //      encodeFileURL
    //      encodeUriPath (i.e URI segment).
    //  All leave nonASCII alone.

   // Order is do reservered, then do nonASCII; preserves %

    // PLAN
    //   HttpLib ->

    // Where is the "to UTF8, then encode two step?
    //   ** IRILib.strEncodeForURI
    //      ==> IRILib.encodeNonASCII

    // Current:
    // Params.httpString
    // HttpLib.urlEncodeQueryString(name);
    //   => IRILib.encodeUriQueryFrag(str);
    //   => StrUtils.encodeHex(string,'%', charsQueryFrag) ;

    // PLAN
    // [x]  Must fix:
    //      HttpLib.urlEncodeQueryString <-- Two pass fix
    //      Params.httpString -> HttpLib.urlEncodeQueryString
    // [ ] Improve TestIRILib
    // [ ] Routines: IRILib.isUriReserved, isPathComponentChar, isUriChar
    // [ ] Add IRILib.encodeNonASCII step (after) HttpLib.urlEncodeQueryString
    //     And space and control characters.
    // [ ] fn:iri-to-uri
    // [ ] All in one encoder : Encode.
    // [ ] Convert to bitmaps?

    // [ ] Fix: encode nonASCII after encoding for query string.
    // [ ]  Tests
    // [ ] Encode.encodePercent , rename escapees as non_Chars


    // BitSet possible.
    // ==> RiotChars
    // Unreserved
    //  unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
    public static boolean uri_unreservedAscii(int ch) {
        if ( RiotChars.isA2ZN(ch) )
            return true;
        switch(ch) {
            case '-': case '_': case '.': case '~':
                return true;
        }
        return false;
    }

    public static boolean uri_gen_delims(int ch) {

        switch(ch) {
            case ':': case '/': case '?': case '#': case '[': case ']': case '@':
                return true;
        }
        return false;
    }

    public static boolean uri_sub_delims(int ch) {
        switch(ch) {
            case '!': case '$': case '&': case '\'': case '(': case ')': case '*': case '+': case ',': case ';': case '=':
                return true;
        }
        return false;
    }


    // RFC 3986:
    //
    // pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
    // query         = *( pchar / "/" / "?" )
    // fragment      = *( pchar / "/" / "?" )
    // pct-encoded   = "%" HEXDIG HEXDIG
    // unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
    // reserved      = gen-delims / sub-delims
    // gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
    // sub-delims    = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="

    // RFC 2396
    //
    // query         = *uric
    // fragment      = *uric
    // uric          = reserved | unreserved | escaped
    // reserved      = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" | "$" | ","
    // unreserved    = alphanum | mark
    // mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"

    // RFC 2396: "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"

    public static boolean unreservedIChar(int ch) {
        if ( RiotChars.isAlphaNumeric(ch) )
            return true;
        switch(ch) {
            case '-': case '_': case '.': case '~':
                return true;
        }
        return false;
    }

    // In Params.encode
    // StrUtils.encodeHex
    public static String stringEsc(String s) {
        AWriter w = new StringWriterI() ;
        EscapeStr.stringEsc(w, s, Chars.CH_QUOTE2, true, CharSpace.ASCII) ;
        return w.toString() ;
    }

    // From StrUtils
    // Used by IRIlib.encodeUriQueryFrag
//    /**
//     * Encode a string using hex values e.g. %20.
//     * Encoding only deals with single byte codepoints.
//     *
//     * @param str String to encode
//     * @param marker Marker character
//     * @param escapees Characters to encode (must include the marker)
//     * @param charSpace UTF-8 or ASCII output.
//     * @return Encoded string (returns input object if no change)
//     */
//    public static String encodePercent(String str, char marker, char[] escapees, CharSpace charSpace) {
//        boolean asciiOnly = (charSpace != null) ? CharSpace.isAscii(charSpace) : false;
//        int N = str.length();
//        int idx = 0;
//        // Scan stage until first encodeable character
//        for ( ; idx < N ; idx++ ) {
//            char ch = str.charAt(idx);
//            if ( escapees != null && Chars.charInArray(ch, escapees) )
//                break;
//            if ( asciiOnly && !isAsciiPrintable(ch) )
//                break;
//        }
//        if ( idx == N )
//            return str;
//
//        // At least one char to convert
//        StringBuilder buff = new StringBuilder();
//        buff.append(str, 0, idx);  // Insert first part.
//        for ( ; idx < N ; idx++ ) {
//            char ch = str.charAt(idx);
//            if ( asciiOnly && !isAsciiPrintable(ch) ) {
//                Chars.encodeAsHex(buff, marker, ch);
//                continue;
//            }
//            if ( escapees != null && Chars.charInArray(ch, escapees) ) {
//                Chars.encodeAsHex(buff, marker, ch);
//                continue;
//            }
//            buff.append(ch);
//        }
//        return buff.toString();
//    }

    // See also EscapeStr
    // IRILib

    // Fix StrUtil.encodeHaex- take a "is" function.
    // IRILib: char[] -> functions.

    // [ ] ?? IRILib.encodeNonASCII

    // string to UTF-8
    // UTF-8 to %-encoding

    /**
     * Encode a string IRI with URL encoding
     * ' ' (space) is encoded as '+', not %20.
     *
     * @param str String to encode
     * @return Encoded string (returns input object if no change)
     */
    public static String encodeURL(String str) {
        return encodePercentURL(str, true);
    }

    /**
     * Encode a string IRI as a URI using %-encoding.
     * ' ' (space) is encoded as '%20', not '+'.
     *
     * @param str String to encode
     * @return Encoded string (returns input object if no change)
     */
    public static String encodePercent(String str) {
        return encodePercentURL(str, false);
    }

    private static String encodePercentURL(String str, boolean urlSpace) {
        int N = str.length();
        int idx = 0;
        // Scan stage until first encodeable character
        for ( ; idx < N ; idx++ ) {
            char ch = str.charAt(idx);
            if ( ch > 127 || !uri_unreservedAscii(ch) )
                break;
        }
        // Did not find an unsuitable character.
        if ( idx == N )
            return str;

        // At least one char to convert
        StringBuilder buff = new StringBuilder(str.length()+12);
        buff.append(str, 0, idx);  // Insert first part.

        String s = str.substring(idx);
        //StandardCharsets.UTF_8.newEncoder().encode(in, out, endOfInput)
        byte[] bytes = Bytes.asUTF8bytes(s);
        for ( int i = 0 ; i < bytes.length ; i++ ) {
            byte b = bytes[i];
            char x = (char)(b&0xFF);    // &FF to make this a unsigned extend.
            if ( urlSpace && x == ' ') {
                buff.append('+');
                continue;
            }

            if ( !uri_unreservedAscii(x) ) {
                Chars.encodeAsHex(buff, '%', x);
                continue;
            }
            buff.append(x);
        }
        return buff.toString();
   }

    private static String encodePercentURL_0(String str, boolean urlSpace) {
        int N = str.length();
        int idx = 0;
        // Scan stage until first encodeable character
        for ( ; idx < N ; idx++ ) {
            char ch = str.charAt(idx);
            if ( ch > 127 || !uri_unreservedAscii(ch) )
                break;
        }
        // Did not find an unsuitable character.
        if ( idx == N )
            return str;

        // At least one char to convert
        StringBuilder buff = new StringBuilder(str.length()+12);
        buff.append(str, 0, idx);  // Insert first part.
        for ( ; idx < N ; idx++ ) {
            char ch = str.charAt(idx);
            if ( urlSpace && ch == ' ') {
                buff.append('+');
                continue;
            }
            // UTF-8.
            if ( ch > 127 ) {
                // Convert to UTF8, and %-encode.
                System.err.println("ch > 127 - ProcUTF8.convert");
                //ProcUTF8.convert(ch, x->Chars.encodeAsHex(buff, '%', (char)x));
                continue;
            }

            if ( !uri_unreservedAscii(ch) ) {
                Chars.encodeAsHex(buff, '%', ch);
                continue;
            }
            buff.append(ch);
        }
        return buff.toString();
    }
}
