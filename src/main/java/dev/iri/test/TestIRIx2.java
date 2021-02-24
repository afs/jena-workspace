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

import static dev.iri.test.IRIxTestLib.testReference;
import static dev.iri.test.IRIxTestLib.testRelative;
import static dev.iri.test.IRIxTestLib.testResolve;

import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestIRIx2 extends AbstractTestIRIx2 {

    public TestIRIx2(String name, IRIProvider provider) {
        super(name, provider);
    }

    // ---- RFC 3986 Grammar

    @Test public void uri_01()      { IRIxTestLib.testParseCheckString("http://example/abc"); }

    @Test public void uri_02()      { IRIxTestLib.testParseCheckString("http://example/αβγ"); }

    @Test public void uri_03()      { IRIxTestLib.testParseCheckString("http://example/Ẓ"); }

    @Test public void uri_04()      { IRIxTestLib.testParseCheckString("http://[::1]/abc"); }

    @Test public void uri_05()      { IRIxTestLib.testParseCheckString("http://reg123/abc"); }

    @Test public void uri_06()      { IRIxTestLib.testParseCheckString("http://1.2.3.4/abc"); }

    // ---- Compliance with HTTP RFC7230. https://tools.ietf.org/html/rfc7230#section-2.7
    @Test(expected=IRIException.class)
    public void http_01() { IRIxTestLib.testParse("http:"); }

    @Test(expected=IRIException.class)
    public void http_02() { IRIxTestLib.testParse("http:/"); }

    @Test(expected=IRIException.class)
    public void http_03() { IRIxTestLib.testParse("http://"); }

    @Test public void http_04() { IRIxTestLib.testParse("http://x"); }

    @Test(expected=IRIException.class)
    public void http_05()   { IRIxTestLib.testParse("http:abc"); }

    @Test(expected=IRIException.class)
    public void http_06()   { IRIxTestLib.testParse("http:///abc"); }

    @Test(expected=IRIException.class)
    // [] not in IPv6 address
    public void http_07()   { IRIxTestLib.testParse("http://h/ab[]"); }

    @Test public void http_08() { IRIxTestLib.testParse("http://example/~jena/file"); }

    // -- Compliance with URN scheme: https://tools.ietf.org/html/rfc8141

    @Test public void urn_01() { IRIxTestLib.testParse("urn:NID:NSS"); }

    @Test(expected=IRIException.class)
    public void urn_02() { IRIxTestLib.testParse("urn:x:abcd"); }

    @Test(expected=IRIException.class)
    public void urn_03() { IRIxTestLib.testParse("urn:ex:"); }

    @Test public void urn_04()  { notStrict("urn", ()->IRIxTestLib.testParse("urn:x:abc")); }

    @Test public void urn_05()  { notStrict("urn", ()->IRIxTestLib.testParse("urn:ex:")); }

    // -- Compliance with file scheme: https://tools.ietf.org/html/rfc8089

    @Test public void file_01() { IRIxTestLib.testParse("file:///path/name"); }

    @Test public void file_02() { IRIxTestLib.testParse("file:/path/name"); }

    @Test public void file_03() { IRIxTestLib.testParse("file:name"); }

    @Test public void file_04() { IRIxTestLib.testParse("file:/path/name"); }

    @Test public void file_05() { IRIxTestLib.testParse("file:name"); }

    @Test public void file_06() { IRIxTestLib.testParse("file:///c:/~user/file"); }

    // --- Use in RDF

    @Test public void reference_01() { testReference("http://example/", true); }

    @Test public void reference_02() { testReference("http://example/abcd", true); }

    @Test public void reference_03() { testReference("//example/", false); }

    @Test public void reference_04() { testReference("testRelative-uri", false); }

    @Test public void reference_05() { testReference("http://example/", true); }

    @Test public void reference_06() { testReference("http://example/", true); }

    @Test public void reference_07() { testReference("http://example/", true); }

    @Test public void reference_08() { testReference("file:///a:/~jena/file", true); }

    // -- Resolving
    @Test public void testResolve_http_01() { testResolve("http://example/", "path", "http://example/path"); }

    @Test public void testResolve_http_02() { testResolve("http://example/dirA/dirB/", "/path", "http://example/path"); }

    @Test public void testResolve_http_03() { testResolve("https://example/dirA/file", "path", "https://example/dirA/path"); }

    // <>
    @Test public void testResolve_http_04() { testResolve("http://example/doc", "", "http://example/doc"); }

    //<#>
    @Test public void testResolve_http_05() { testResolve("http://example/ns", "#", "http://example/ns#"); }

    @Test public void testResolve_http_06() { testResolve("http://example/ns", "#", "http://example/ns#"); }

    @Test public void testResolve_file_01() { testResolve("file:///dir1/dir2/", "path", "file:///dir1/dir2/path"); }

    @Test public void testResolve_file_02() { testResolve("file:///dir/file", "a/b/c", "file:///dir/a/b/c"); }

    @Test public void testResolve_file_03() { testResolve("file:///dir/file", "/a/b/c", "file:///a/b/c"); }

    @Test public void testResolve_file_04() { testResolve("file:///dir/file", "file:ABC", "file:///dir/ABC"); }

    @Test public void testResolve_file_05() { testResolve("file:///dir/file", "file:/ABC", "file:///ABC"); }

    // No trailing slash, not considered to be a "directory".
    @Test public void testRelative_http_01() { testRelative("http://example/dir", "http://example/dir/abcd", "dir/abcd"); }

    @Test public void testRelative_http_02() { testRelative("http://example/dir", "http://example/dir/abcd", "dir/abcd"); }

    @Test public void testRelative_http_03() { testRelative("http://example/dir/ab", "http://example/dir/abcd", "abcd"); }

    @Test public void testRelative_http_04() { testRelative("http://example/dir/", "http://example/dir/abcd#frag", "abcd#frag"); }

    @Test public void testRelative_http_05() { testRelative("http://example/abcd", "http://example/abcd#frag", "#frag"); }

    @Test public void testRelative_http_06() { testRelative("http://example/abcd", "http://example/abcd?query=qs", "?query=qs"); }

    @Test public void testRelative_http_07() { testRelative("http://example/abcd", "http://example/abcd?query=qs#f", "?query=qs#f"); }

    @Test public void testRelative_http_08() { testRelative("http://example/dir1/dir2/path", "http://example/otherDir/abcd", null); }

    @Test public void testRelative_http_09() { testRelative("http://example/path", "http://example/path", ""); }

    @Test public void testRelative_http_10() { testRelative("http://example/path", "http://example/path#", "#"); }

    @Test public void testRelative_file_01() { testRelative("file:///dir/", "file:///dir/abcd", "abcd"); }

    @Test public void testRelative_file_02() { testRelative("file:///", "file:///dir/abcd", "dir/abcd"); }
}
