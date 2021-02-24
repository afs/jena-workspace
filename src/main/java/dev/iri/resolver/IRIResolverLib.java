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

package dev.iri.resolver;

import java.io.PrintStream;

import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.impl.PatternCompiler;

public class IRIResolverLib {


    /** Set the error/warning state of a violation code.
     * @param factory   IRIFactory
     * @param code      ViolationCodes constant
     * @param isError   Whether it is to be treated an error.
     * @param isWarning Whether it is to be treated a warning.
     */
    static void setErrorWarning(IRIFactory factory, int code, boolean isError, boolean isWarning) {
        factory.setIsWarning(code, isWarning);
        factory.setIsError(code, isError);
    }

    static void printSettings(IRIFactory iriFactory) {
        for ( int code = 0 ; code < 128; code++) {
            printErrorWarning(System.out, iriFactory, code);
        }
    }

//    static void printSetting(IRIFactory factory) {
//        PrintStream ps = System.out;
//        printErrorWarning(ps, factory, ViolationCodes.UNREGISTERED_IANA_SCHEME);
//        printErrorWarning(ps, factory, ViolationCodes.NON_INITIAL_DOT_SEGMENT);
//        printErrorWarning(ps, factory, ViolationCodes.NOT_NFC);
//        printErrorWarning(ps, factory, ViolationCodes.NOT_NFKC);
//        printErrorWarning(ps, factory, ViolationCodes.UNWISE_CHARACTER);
//        printErrorWarning(ps, factory, ViolationCodes.UNDEFINED_UNICODE_CHARACTER);
//        printErrorWarning(ps, factory, ViolationCodes.UNASSIGNED_UNICODE_CHARACTER);
//        printErrorWarning(ps, factory, ViolationCodes.COMPATIBILITY_CHARACTER);
//        printErrorWarning(ps, factory, ViolationCodes.LOWERCASE_PREFERRED);
//        printErrorWarning(ps, factory, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE);
//        printErrorWarning(ps, factory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED);
//        ps.println();
//    }

    static void printErrorWarning(PrintStream ps, IRIFactory factory, int code) {
        String x = PatternCompiler.errorCodeName(code);
        ps.printf("%-40s : E:%-5s W:%-5s\n", x, factory.isError(code), factory.isWarning(code));
    }
}
