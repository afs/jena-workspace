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

public class DevParseNumber {

    // Decimal32 etc: Densely packed decimal (DPD)
    //https://en.wikipedia.org/wiki/Densely_packed_decimal

    // ?? Parse record (index, length) separate from results

    public static void main(String...a) {

        // Trim (XML whitespace facet rules.
        // XMLChar.trim(value);
        // FloatingDecimal.readJavaFormatString

//        ParserNumber.byRegex("+012.4560e+1");
//        ParserNumber.byRegex("012.0");

        // To do -
        // [ ] Leading and trailing zeros.
        // [ ] Remove "_" handling.

        // Underscore - tricky because "next char" may be several charcater -> index in the parser object, not passed into charAt(idx)
        // => "nextChar()"

        if ( false ) {
            dwimNumber("0123.5", true);
        } else {
            dwimNumber("0123", true);
            dwimNumber("0123.5", true);
            dwimNumber("0123E0", true);
            dwimNumber("0123e+1", true);
            dwimNumber("-123.9e+1", true);
            dwimNumber("-123.9e", false);
            dwimNumber("-123.9e+", false);
            dwimNumber("3.9e-", false);

            dwimNumber(".9", true);
            dwimNumber("-1.", true);
            dwimNumber("0123.A", false);
            dwimNumber("0123A", false);
            dwimNumber(".", false);
            dwimNumber("", false);
            dwimNumber(" ", false);


            dwimNumber("NaN",  true);
            dwimNumber("-INF", true);
            dwimNumber("+INF", true);
            dwimNumber("INF",  true);
            dwimNumber("+NaN", false);
            dwimNumber("nan", false);
            dwimNumber("inf", false);

            // +0
            // -0
        }

        System.out.println("DONE");
    }

    public static void dwimNumber(String string, boolean isLegal) {
        try { NumberParser.checkByRegex(string); } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        Matcher matcher = NumberParser.regex.matcher(string);
        boolean b = matcher.matches();
        if ( b != isLegal )
            System.err.println("Mismatch on regex: "+string);
        //MatchResult matchResult = matcher.toMatchResult();

        try {
            NumberParser r = new NumberParser(string, false, false);
            if ( isLegal )
                System.out.print("   ");
            else
                System.out.print("** ");
            System.out.println("\""+string+"\" ");
        } catch (Exception ex) {
            if ( ! isLegal )
                System.out.println("-- \""+string+"\"");
            else
                System.out.println("** "+"\""+string+"\" "+ex.getMessage());
        }
    }


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
}
