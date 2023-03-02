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

package lib.number;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.seaborne.rfc3986.Chars3986;

/**
 * Parse a number, which may be in floating point form.
 * This class does not account for the exact datatype
 */
    public class NumberParser {
        /* Facets: https://www.w3.org/TR/xmlschema11-2/#rf-facets
         * length
         * minLength
         * maxLength
         * pattern
         * enumeration
         * whiteSpace
         * maxInclusive
         * maxExclusive
         * minExclusive
         * minInclusive
         * totalDigits
         * fractionDigits
         * Assertions
         * explicitTimezone
         */

        static final char unset = 0x0000;   //Character.MIN_VALUE;
        static final char EOF = 0xFFFF;     //Character.MAX_VALUE;

        private char sign = unset;
        // Mantissa.
        private int countLeadingZeros = 0;  // Integer part
        private int countTrailingZeros = 0; // Decimal part
        private int decimalPoint = -1;
        private int exponent = -1;  // The "E/e" introducing the exponent.
        private char expSign = unset;
        private boolean isNaN = false;
        private boolean isInf = false;

        private final int start;
        private final int N;
        private final String str;
        // Character being processed
        private int idx;
        // Only leading and trailing whitespace considered.
        private final boolean whitespace;
        //private final boolean underscore;

        public static boolean check(String string) {
            try {
                NumberParser result = new NumberParser(string, false, false);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        NumberParser(String string, boolean whitespace, boolean underscore) {
            // Adjust for whitespace facet.
            // Bit numbers do no allow internal white space so that just trim (inc newlines).
            this.start = 0 ;
            this.N = string.length();
            this.str = string;
            this.whitespace = whitespace;
            // Allow _ for readability. No semantics.
            // this.underscore = underscore;
            parse(string);
        }

        // Slices

        boolean matchesInteger() { return decimalPoint == -1 && exponent == -1; }
        boolean matchesDouble()  { return exponent != -1; }
        boolean matchesDecimal() { return decimalPoint != -1 && exponent == -1; }

        boolean isNaN() { return isNaN; }
        boolean isInf() { return isInf; }

        /**
         * The regular expression for numbers from XSD Datatypes.
         * <br/>
         * <a href="https://www.w3.org/TR/xmlschema11-2/#double">XSD double regex</a>,
         * <br/>
         * <a href="https://www.w3.org/TR/xsd-precisionDecimal/#pD-lexical-mapping">xsd:precisionDecimal regex</a>.
         */
        public static final Pattern regexNumber = Pattern.compile("(\\+|-)?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([Ee](\\+|-)?[0-9]+)?|(\\+|-)?INF|NaN");

        // Groups (if a number and not NaN or INF)
        // 1 : sign, maybe empty
        // 2 : mantissa,/significand
        // 4 : exponent.
        // 6 : NaN, INF part
        //                                            1     1 2      3         3           24    5     5       4 |67     7        6
        //From XSD Datatypes, add() for NaN/INF
        //static final Pattern regex = Pattern.compile("(\\+|-)?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([Ee](\\+|-)?[0-9]+)?|((\\+|-)?INF|NaN)");

        static final Pattern regex = regexNumber;

        /**
         * Apply the general-purpose regex (from XSD datatypes).
         * It does not require leading digits.
         * Legal: {@code 1.}, {@code .1}
         *
         * @param string -- inoput, without leading or trailing whitespace.
         * @throws NumberFormatException
         */

        static void checkByRegex(String string) {
            Matcher matcher = regex.matcher(string);
            if ( ! matcher.matches() )
                error("Does not match regex: "+string);
        }

        static void byRegexPrint(String string) {
            Matcher matcher = regex.matcher(string);
            if ( ! matcher.matches() )
                error("Does not match regex: "+string);

            System.out.print("Regex on: |"+string+"| :: ");
            for ( int i = 0; i < matcher.groupCount(); i++ ) {
                System.out.printf("  Group %d = %s", i, printStr(matcher.group(i)));
            }
            System.out.println();
//            String strSign = matcher.group(1);
//            String strInteger = matcher.group(2);
//            String strFraction = matcher.group(3);
//            String strExponent = matcher.group(4);
//            System.out.printf("Sign     = %-3s", printStr(strSign));
//            System.out.printf("  Integer  = %-8s", printStr(strInteger));
//            System.out.printf("  Fraction = %-8s", printStr(strFraction));
//            System.out.printf("  Exponent = %s", printStr(strExponent));
//            System.out.println();
        }

        private static String printStr(String str) {
            return str == null ? "_" : "'"+str+"'";
        }

        // Accepts zero digits after the decimal point.
        // Check that "." is not accepted.

        // What about E1?

        void parse(String string) {
            if ( string.isEmpty() )
                error("Empty string");
            switch(string) {
                case "NaN":
                    isNaN = true; return;
                case "INF":
                case "-INF":
                case "+INF":
                    isInf = true;
                    return;
                case "nan": case "NAN":
                case "inf":
                case "-inf":
                case "+inf":
                    error("INF and NaN are case sensitive. Found: "+string);
            }

            // sign?
            // digits*
            // ( decimal point ; digits+)
            // exponent.
            // expSign
            // digits+

            int idx = 0;
            if ( false )
                idx = skipWhitespace(0);

            idx = optionalSign(str, idx);

            int idx0 = parseZeros(idx);
            this.countLeadingZeros = (idx0-idx);
            int idx1 = parseDigits(idx0);

            // Was there an integer part?
            // If so fractional part can be empty, else it must be at least one charaater.
            boolean zeroIntegerDigits = (idx == idx1);
            idx = idx1;

            if ( idx >= N ) {
                if ( zeroIntegerDigits ) {
                    // Non-empty, no integer, end of string => sign and nothing else
                    if ( this.sign != unset )
                        error("No number, only a sign");
                    // No sign, no number was handled by "isEmpty"
                    throw new InternalErrorException("Inconsistent 1");
                }
                return;
            }

            char ch1 = charAt(idx);
            if ( ch1 == '.' ) {
                this.decimalPoint = idx;
                idx++;
                // Accept zero digits.
                int idx2 = parseDigits(idx);
                boolean zeroDecimalDigits = (idx == idx2);
                idx = idx2;

//                if ( zeroDecimalDigits )
//                    // If not allowing "1."
//                    error("No digits in decimal fraction: "+str);
//
                if ( zeroIntegerDigits && zeroDecimalDigits )
                    error("No integer or fractional digits.");
            }

            // Is there an exponent?
            if ( idx >= N)
                return;

            char ch2 = charAt(idx);
            if ( ch2 == 'E' || ch2 == 'e' ) {
                this.decimalPoint = idx;
                idx++;
            } else
                error("Bad number at posn "+idx+" '"+Character.valueOf(ch2)+"' after significand (mantissa) "+str);

            // Exponent.
            idx = optionalSign(str, idx);
            int idx2 = parseDigits(idx);
            if ( idx == idx2 )
                error("No digits in exponent: "+str);
            idx = idx2;

            // End?
            if ( false )
                idx = skipWhitespace(idx);
            if ( idx != N )
                error("Trailing characters: "+string);
        }

        /** String.charAt except with an EOF character, not an exception. */
        private char charAt(int x) {
            if ( x < 0 )
                System.err.println("BUG");
            if ( x >= N )
                return EOF;
            for ( ;; ) {
                char ch = str.charAt(x);
//                if ( this.underscore && ch == '_' )
//                    continue;
                return ch;
            }
        }


        private int optionalSign(CharSequence str, int i) {
            char ch = charAt(i);
            if ( ch == '-' || ch == '+' ) {
                this.sign = ch;
                return i+1;
            }
            return i;
        }

        private int skipWhitespace(int start) {
            int p = start;
            for (int i = start ; i < N ; i++ ) {
                char ch = charAt(p+i);
                // Other whitespace
                if ( ch != ' ' )
                    break;
            }
            return p;
        }

        // Move over 0-9.
        private int parseDigits(int start) {
            for ( int i = start; i < N ; i++ ) {
                char ch = charAt(i);
                if ( ! Chars3986.isDigit(ch) )
                    return i;
            }
            return N;
        }

        // Move over 0.
        private int parseZeros(int start) {
            for ( int i = start; i < N ; i++ ) {
                char ch = charAt(i);
                if ( ch != '0' )
                    return i;
            }
            return N;
        }

        private static void error(String msg) {
            throw new NumberFormatException(msg);
        }

//        public String toString() {
//            StringBuilder sb = new StringBuilder();
//            String SEP = "  ";
//            sb.append("Sign = "+(int)sign);
//            sb.append(SEP);
//            sb.append("Integer  = %-8s", printStr(strInteger));
//            System.out.printf("  Fraction = %-8s", printStr(strFraction));
//            System.out.printf("  Exponent = %s", printStr(strExponent));
//        }
    }