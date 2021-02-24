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

package dev.iri.test;

public class TS_IRIx2 {}

//import org.apache.jena.irix.SystemIRIx;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.runner.RunWith;
//import org.junit.runners.Suite;
//
//@RunWith(Suite.class)
//@Suite.SuiteClasses( {
//    // IRIx tests with matrix of providers.
//    TestIRIx2.class,
//    // Controlled by lib
//    TestRFC3986_2.class,
//    TestResolve2.class,
//    TestNormalize2.class,
//    TestRelative2.class,
//    TestMiscIRI.class
//} )
//
//public class TS_IRIx2 {
//    static boolean strictHTTPx;
//    static boolean strictURN;
//    static boolean strictFILE;
//
//    @BeforeClass public static void classSetup() {
//        strictHTTPx = SystemIRIx.isStrictMode("http");
//        strictURN = SystemIRIx.isStrictMode("urn");
//        strictFILE = SystemIRIx.isStrictMode("file");
//        SystemIRIx.strictMode("http", true);
//        SystemIRIx.strictMode("urn", true);
//        SystemIRIx.strictMode("file", true);
//    }
//
//    @AfterClass public static void classReset() {
//        SystemIRIx.strictMode("http", strictHTTPx);
//        SystemIRIx.strictMode("urn", strictURN);
//        SystemIRIx.strictMode("file", strictFILE);
//    }
//}
