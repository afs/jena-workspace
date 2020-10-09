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

package archive;

public class Jena1924_ucschar_extra {

//    [INFO] Running org.apache.jena.riot.Scripts_LangSuite
//    13:41:50 ERROR riot       :: [line: 1, col: 217] Illegal character in IRI (Not a UCSChar: 0xE01EF): <http://a.example/AZazÀÖØöø˿Ͱͽ΄῾‌‍⁰↉Ⰰ⿕、ퟻ﨎ﷇﷰ￯𐀀[U+E01EF]...>
//    13:41:50 ERROR riot       :: [line: 1, col: 217] Illegal character in IRI (Not a UCSChar: 0xE01EF): <http://a.example/AZazÀÖØöø˿Ͱͽ΄῾‌‍⁰↉Ⰰ⿕、ퟻ﨎ﷇﷰ￯𐀀[U+E01EF]...>
//    [ERROR] Tests run: 883, Failures: 2, Errors: 0, Skipped: 0, Time elapsed: 1.771 s <<< FAILURE! - in org.apache.jena.riot.Scripts_LangSuite
//    [ERROR] T-256: localName_with_assigned_nfc_PN_CHARS_BASE_character_boundaries  Time elapsed: 0.011 s  <<< FAILURE!
//    java.lang.AssertionError: Failed to read results: [line: 1, col: 217] Illegal character in IRI (Not a UCSChar: 0xE01EF): <http://a.example/AZazÀÖØöø˿Ͱͽ΄῾‌‍⁰↉Ⰰ⿕、ퟻ﨎ﷇﷰ￯𐀀[U+E01EF]...>
//
//    [ERROR] T-558: localName_with_assigned_nfc_PN_CHARS_BASE_character_boundaries  Time elapsed: 0.009 s  <<< FAILURE!
//    java.lang.AssertionError: Failed to read results: [line: 1, col: 217] Illegal character in IRI (Not a UCSChar: 0xE01EF): <http://a.example/AZazÀÖØöø˿Ͱͽ΄῾‌‍⁰↉Ⰰ⿕、ퟻ﨎ﷇﷰ￯𐀀[U+E01EF]...>

}
