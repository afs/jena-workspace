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

import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestNumberParsing {

    @Test public void parse_02() { positiveTest("0"); }
    @Test public void parse_03() { positiveTest("123"); }
    @Test public void parse_04() { positiveTest("+1"); }
    @Test public void parse_05() { positiveTest("-2"); }
    @Test public void parse_06() { positiveTest("+0"); }
    @Test public void parse_07() { positiveTest("-0"); }
    @Test public void parse_08() { positiveTest("-000"); }
    @Test public void parse_09() { positiveTest("+000"); }
    @Test public void parse_10() { positiveTest("0123"); }
    @Test public void parse_11() { negativeTest("0123A"); }
    @Test public void parse_12() { negativeTest("012A3"); }
    @Test public void parse_13() { negativeTest("A012A3"); }

    @Test public void parse_20() { positiveTest("0123.5"); }
    @Test public void parse_21() { positiveTest(".9"); }
    @Test public void parse_22() { positiveTest("-1."); }
    @Test public void parse_23() { positiveTest("00.00"); }
    @Test public void parse_24() { negativeTest("0123.A"); }

    @Test public void parse_30() { positiveTest("0123E0"); }
    @Test public void parse_31() { positiveTest("0123e+1"); }
    @Test public void parse_32() { positiveTest("-123.9e+1"); }
    @Test public void parse_33() { negativeTest("-123.9e"); }
    @Test public void parse_34() { negativeTest("-123.9e+"); }
    @Test public void parse_35() { negativeTest("3.9e-"); }

    @Test public void parse_80() { positiveTest("NaN"); }
    @Test public void parse_81() { positiveTest("-INF"); }
    @Test public void parse_82() { positiveTest("+INF"); }
    @Test public void parse_83() { positiveTest("INF"); }
    @Test public void parse_84() { negativeTest("+NaN"); }
    @Test public void parse_85() { negativeTest("nan"); }
    @Test public void parse_86() { negativeTest("inf"); }

    @Test public void parse_90() { negativeTest("."); }
    @Test public void parse_91() { negativeTest(""); }
    @Test public void parse_92() { negativeTest(" "); }

    // The regular expression from XSD Datatypes.
    private static Pattern regex = Pattern.compile("(\\+|-)?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([Ee](\\+|-)?[0-9]+)?|(\\+|-)?INF|NaN");

    public static boolean numberByRegex(String string, boolean isLegal) {
        Matcher matcher = regex.matcher(string);
        boolean actual = matcher.matches();
        if ( isLegal == actual )
            return actual;
        if ( actual ) {
            System.out.print("Regex on: |"+string+"| :: ");
            for ( int i = 0; i < matcher.groupCount(); i++ ) {
                System.out.printf("  Group %d = %s", i, printStr(matcher.group(i)));
            }
            System.out.println();
        }
        return actual;
    }

    private static String printStr(String str) {
        return str == null ? "_" : "'"+str+"'";
    }

    private static void positiveTest(String str) { test(str, true); }

    private static void negativeTest(String str) { test(str, false); }

    private static void test(String str, boolean isLegal) {
        numberByRegex(str, isLegal);
        boolean result = NumberParser.check(str);
        assertEquals("Number: "+str, isLegal, result);
    }
}
