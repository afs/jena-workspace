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

public class DevMod {
    // Have examples for test cases:
    //op:numeric-integer-divide

    // https://www.w3.org/TR/xpath-functions-3/#func-numeric-integer-divide
    // https://www.w3.org/TR/xpath-functions-3/#func-numeric-mod

    // """
    // Except in situations involving errors, loss of precision, or
    // overflow/underflow, the result of $a idiv $b is the same as ($a div $b) cast
    // as xs:integer.

    // """

    // Integer divide

    // [x] parser
    // [ ] Syntax tests (?)
    // [ ] Functions, check XSDFuncOp.numIntegerDivider for cheaper version.
    // [ ] Expression tests. See F&O sections for some examples.
    // [ ] Check idiv (RHS is not integer?)


    public static void main(String[] args) {


//      If $arg2 is INF or -INF, and $arg1 is not INF or -INF, then the result is zero.
//      A dynamic error is raised [err:FOAR0001] if the divisor is (positive or negative) zero.
//      A dynamic error is raised [err:FOAR0002] if either operand is NaN or if $arg1 is INF or -INF.

        System.out.println(156.7/Double.POSITIVE_INFINITY);
        System.exit(0);
        // Jena-2116
        // RDF4J: core/queryalgebra/evaluation/src/main/java/org/eclipse/rdf4j/query/algebra/evaluation/util/MathUtil.java

        // strict-> XSDFuncOp.numDivide

        // 1: strict-> XSDFuncOp.numDivide -- why strict?
        // NodeValueOps.divisionNV - checks and then calls  XSDFuncOp.numDivide - why?

        // 2: Delayed canonicalization
        // [x] Check everywhere

        // 3: asNode() vs getNode() -- need to check

        // ??: Avoid ArithmeticException?
        // ??: Much longer precision and truncate?

        // 6: MOD(a,b) : xsd:numeric-mod
        //(a idiv b)*b+(a mod b)

        // 14.9 mod 2.1  ::  0.2     xs:decimal

        dwim("fn:numeric-mod(3 , 2)");
        dwim("fn:numeric-mod(-3 , 2)");
        dwim("fn:numeric-mod(3 , -2)");
        dwim("fn:numeric-mod(-3 , -2)");

        dwim("fn:numeric-mod(3.4  , 1.3)");      // 1.3,2.6 and 0.8 mod.
        dwim("fn:numeric-mod(-3.4 , 1.3 )");
        dwim("fn:numeric-mod(3.4  , -1.3 )");
        dwim("fn:numeric-mod(-3.4 , -1.3 )");

        dwim("fn:numeric-mod(14.9 , 2.1)");     // 2.1, 14.7 -> 0.2 over

        dwim("IDIV(10,3)");
        dwim("10 idiv 3");
        dwim("IDIV(3.1E1,6)");
        dwim("IDIV(3.1E1,7)");

        dwim("1.23E2 mod 0.6E1");



/*
 * "e" vs "E"

    The expression op:numeric-integer-divide(10,3) returns 3.
    The expression op:numeric-integer-divide(3,-2) returns -1.
    The expression op:numeric-integer-divide(-3,2) returns -1.
    The expression op:numeric-integer-divide(-3,-2) returns 1.
    The expression op:numeric-integer-divide(9.0,3) returns 3.
    The expression op:numeric-integer-divide(-3.5,3) returns -1.
    The expression op:numeric-integer-divide(3.0,4) returns 0.
    The expression op:numeric-integer-divide(3.1E1,6) returns 5.
    The expression op:numeric-integer-divide(3.1E1,7) returns 4.

    The expression op:numeric-mod(10,3) returns 1.
    The expression op:numeric-mod(6,-2) returns 0.
    The expression op:numeric-mod(4.5,1.2) returns 0.9.
    The expression op:numeric-mod(1.23E2, 0.6E1) returns 3.0E0.

XXX XSDFuncOp extras
XSDFuncOp.numIntegerDivide => is a cast
    What about MOD(, not integer)
 */

        System.exit(0);

        System.out.println(4 % 2 );     // 0
        System.out.println(7 % 2 );     // 1
        System.out.println(3.5 % 2 );   // 1.5 - "repeated division"
        System.out.println(4 % 2.5 );   // 1.5 - "repeated division"

        // XSD: Sign of result - sign of dividend (left, top) agrees

        System.out.println( 7 % 3 );    //  1
        System.out.println(-7 % 3 );    // -1
        System.out.println(7 % -3 );    //  1
        System.out.println(-7 % -3 );   // -1
    }

    private static void dwim(String string) {
        System.out.println(string);
        arq.qexpr.main(string);
        System.out.println();
    }


}
