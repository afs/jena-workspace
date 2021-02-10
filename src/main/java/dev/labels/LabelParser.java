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

package dev.labels;

import static org.apache.jena.atlas.lib.Chars.CH_COLON;
import static org.apache.jena.atlas.lib.Chars.CH_DOT;
import static org.apache.jena.atlas.lib.Chars.CH_PERCENT;
import static org.apache.jena.atlas.lib.Chars.CH_UNDERSCORE;
import static org.apache.jena.riot.system.RiotChars.isHexChar;

import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.RiotChars;

public class LabelParser {
    // Prefix parser.
    // Returns the index of the first :
    // Blank node label parser.

    private ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd;

    private final String string;
    private final int length;

//    LabelParser() {
//        this(null);
//    }

    LabelParser(String string) {
        this.string = string;
        this.length = (string == null) ? -1 : string.length();
    }

    /**
     * Check the string for conformance to the Turtle/SPARQL "[137] PrefixedName"
     * rules. Note: this call does <i>not</i> process escapes (e.g. "x:a\$b") - it
     * processes the output of any parsing step when escapes will have been
     * processed (e.g. "x:a$b").
     */
    public static boolean checkPrefixName(String string) {
        LabelParser parser = new LabelParser(string);
        int x = parser.parsePrefixedName(0);
        return x == string.length();
    }

    /** Check the string for conformance to the Turtle/SPARQL "[142] BLANK_NODE_LABEL" rule. */
    public static boolean checkBlankNodeLabel(String string) {
        LabelParser parser = new LabelParser(string);
        int x = parser.parseBlankNodeLabel(0);
        return x == string.length();
    }

    // "The value FFFF is guaranteed not to be a Unicode character at all"
    private static final char EOF = 0xFFFF;
    private static final int ERR  = -1;
    //private boolean eof() { return true; }

    private char charAt(String string, int x) {
        if ( x >= length )
            return EOF;
        return string.charAt(x);
    }

    /*
     * The token rules from SPARQL and Turtle.
     *
     * PNAME_NS       ::=  PN_PREFIX? ':'
     * PNAME_LN       ::=  PNAME_NS PN_LOCAL
     *
     * PN_CHARS_BASE  ::=  [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF]
     *                   | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF]
     *                   | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD]
     *                   | [#x10000-#xEFFFF]
     *
     * PN_CHARS_U     ::=  PN_CHARS_BASE | '_'
     * PN_CHARS       ::=  PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
     *
     * PN_PREFIX      ::=  PN_CHARS_BASE ((PN_CHARS|'.')* PN_CHARS)?
     * PN_LOCAL       ::=  (PN_CHARS_U | ':' | [0-9] | PLX ) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX) )?
     *
     * PLX            ::=  PERCENT | PN_LOCAL_ESC
     * PERCENT        ::=  '%' HEX HEX
     * HEX            ::=  [0-9] | [A-F] | [a-f]
     * PN_LOCAL_ESC   ::=  '\' ( '_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')'
     *                   | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%' )
     *
     * When checking "post parsing," PN_LOCAL_ESC has been dealt with already.
     */

    /**
     * Parse a prefix name starting at index idx.
     * Return the index where parsing stops.
     */
    private int parsePrefixedName(int idx) {
        idx = parsePrefix(idx);
        if ( idx == ERR )
            return idx;
        int z = parseLocal(idx);
        return z;
    }

    /**
     * Parse a prefix of a prefix name (the first part, up to and including the
     * first ":" which is the separator).
     * <p>
     * Return the index of the character after the separator.
     */
    private int parsePrefix(int idx) {
        char ch = charAt(string, idx);
        if ( ch == EOF )
            return ERR;
        // -- First character. PN_CHARS_BASE
        if ( ch == CH_COLON )
            return idx+1;
        if ( ! RiotChars.isPNCharsBase(ch) )
            return ERR;
        // -- Rest: ((PN_CHARS|'.')* PN_CHARS)?
        // Do as ((PN_CHARS|'.')* then check the final character is not DOT

        while(true) {
            idx++;
            char ch2 = charAt(string, idx);
            if ( ch2 == EOF )
                return ERR;
            if ( ch2 == CH_COLON ) {
                // Must end in COLON
                break;
            }
            ch = ch2;
            if ( RiotChars.isPNChars(ch) || ch == CH_DOT )
                continue;
            return ERR;
        }

        // On exit, ch is the character before COLON.
        if ( ch == CH_DOT )
            return ERR;
        if ( idx != length )
            idx++;
        return idx;
    }

    // PN_LOCAL  ::=  (PN_CHARS_U | ':' | [0-9] | PLX ) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX) )?
    // Do as ((PN_CHARS | '.' | ':' | PLX)* and check for DOT
    /**
     * Parse a localname of a prefix name (the part after the ":").
     * <p>
     * Return the index of the character after the separator.
     */
    private int parseLocal(int idx) {
        char ch = charAt(string, idx);
        if ( ch == EOF )
            return idx;

        // -- First character (PN_CHARS_U | ':' | [0-9] | PLX )
        if ( ch == CH_PERCENT ) {
            idx++;
            idx = processPercent(idx);
            if ( idx == EOF || idx == length || idx == ERR )
                return idx;
        } else if ( ch == CH_COLON ) {
            return idx;
//        } else if ( ch == CH_RSLASH ) {
//          idx = processPLX_RSLASH(idx);
//          if ( idx == EOF || idx == length )
//              return idx;
//          idx--;
        } else if ( ! RiotChars.isPNChars_U_N(ch) && ! RiotChars.isPN_LOCAL_ESC(ch) ) {
            return ERR;
        }

        // --- Rest
        // ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX) )?
        //   Do as (PN_CHARS|'.'|':'|PLX)* then check the final character is not '.'

        while(true) {
            if ( idx == length )
                return idx;
            idx++;
            char ch2 = charAt(string, idx);
            if ( ch2 == EOF )
                break;
            ch = ch2;
            if ( RiotChars.isPNChars(ch) || ch == CH_DOT || ch == CH_COLON )
                continue;
            if ( ch == CH_PERCENT ) {
                idx++;
                idx = processPercent(idx);  // return end idx.
                if ( idx == EOF || idx == length  || idx == ERR )
                    return idx;
                // XXX Hmm.
                idx--;
                // Loop will increment
                continue;
            }
            // XXX Includes % so order matters.
            if ( RiotChars.isPN_LOCAL_ESC(ch) )
                continue;
            // XXX DRY
            // Not recognized.
            return ERR;
            // If processing \-escapes.
//            else if ( ch == CH_RSLASH ) {
//                idx = processPLX_RSLASH(idx);
//                if ( idx == EOF || idx == length )
//                    return idx;
//                idx--;
//                continue;
//            }
            // No CH_DOT test.

        }
        if ( ch == CH_DOT )
            return ERR;
        if ( idx != length )
            idx++;
        return idx;
    }

//    private int processPLX(char ch, int idx) {
//        switch(ch) {
//            case CH_PERCENT:
//                return processPLX_PERCENT(idx);
//            case CH_RSLASH:
//                return processPLX_RSLASH(idx);
//
//            default:
//                return ERR;
//        }
//    }

    // Percent sequence checking
    private int processPercent(int idx) {
        char ch1 = charAt(string, idx);
        if ( ch1 == EOF )
            return ERR;
        if ( ! isHexChar(ch1) )
            //fatal("Not a hex character: '%c'",ch1);
            return ERR;
        //stringBuilder.append((char)ch);
        idx++;
        char ch2 = charAt(string, idx);
        if ( ch2 == EOF )
            return ERR;
        if ( ! isHexChar(ch2) )
            //fatal("Not a hex character: '%c'",ch2);
            return ERR;
        idx++;
        return idx;
    }


    // Not for post parser checking.
    private int processEscapedChar(int idx) {
        char ch = charAt(string, idx);
        if ( ch == EOF )
            return ERR;
        idx++;
        if (! RiotChars.isPN_LOCAL_ESC(ch) )
            return ERR;
        return idx;
//        switch (ch) {
//            case '_': case '~': case '.':  case '-':  case '!':  case '$':  case '&':
//            case '\'': case '(':  case ')':  case '*':  case '+':  case ',':  case ';':
//            case '=':  case '/':  case '?':  case '#':  case '@':  case '%':
//                return idx;
//            default:
//                //fatal("illegal character escape value: \\%c", c);
//                return ERR;
//          }
    }

    // '_:' (PN_CHARS_U | [0-9]) ((PN_CHARS | '.')* PN_CHARS)?
    /**
     * Parse a as a blank nodee label 9teh part after "_:".
     * <p>
     * Return the index of the character after the end of parsing.
     */
    private int parseBlankNodeLabel(int idx) {
        char ch = charAt(string, idx);
        // -- Intro:  "_:"
        if ( ch == EOF || ch != CH_UNDERSCORE )
            return ERR;
        idx++;
        ch = charAt(string, idx);
        if ( ch == EOF || ch != CH_COLON )
            return ERR;
        idx++ ;
        // -- First character
        // (PN_CHARS_U | [0-9])
        ch = charAt(string, idx);
        if ( ch == EOF || ! RiotChars.isPNChars_U_N(ch) )
            return ERR;
        // -- Rest
        // ((PN_CHARS | '.')* PN_CHARS)?
        // (PN_CHARS_U | [0-9]), then check for CH_DOT
        while(true) {
            idx++;
            char ch2 = charAt(string, idx);
            if ( ch2 == EOF )
                break;
            ch = ch2;
            if ( ch == CH_DOT )
                continue;
            if ( RiotChars.isPNChars(ch) )
                continue;
            break;
        }
        if ( ch == CH_DOT )
            return ERR;
        if ( idx != length )
            idx++;
        return idx;
    }
}

