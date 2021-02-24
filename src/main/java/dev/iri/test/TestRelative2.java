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

import org.apache.jena.irix.IRIProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestRelative2 extends AbstractTestIRIx2 {

    public TestRelative2(String name, IRIProvider provider) {
        super(name, provider);
    }

    @Test
    public void relative_01() { IRIxTestLib.testRelative("http://host/dir/", "http://host/dir/file", "file"); }

    @Test
    public void relative_02() { IRIxTestLib.testRelative("http://host/dir/", "http://elsewhere/dir/file", null); }

    @Test
    public void relative_03() { IRIxTestLib.testRelative("https://host/dir/", "http://host/dir/file", null); }

    @Test
    public void relative_04() { IRIxTestLib.testRelative("http://host:1234/dir/", "http://host:1234/dir/file", "file"); }

    @Test
    public void relative_05() { IRIxTestLib.testRelative("https://host:1234/dir/", "http://host:5678/dir/file", null); }

    @Test
    public void relative_06() { IRIxTestLib.testRelative("http://ex/path/?query", "http://ex/path/file", null); }

    @Test
    public void relative_07() { IRIxTestLib.testRelative("http://ex/path/#frag", "http://ex/path/file", "file"); }

    @Test
    public void relative_08() { IRIxTestLib.testRelative("http://ex/path/", "http://ex/path/file?q=x", "file?q=x"); }

    @Test
    public void relative_09() { IRIxTestLib.testRelative("http://ex/path/", "http://ex/path/file#frag", "file#frag"); }

    @Test
    public void relative_10() { IRIxTestLib.testRelative("http://example/ns#", "http://example/x", "x") ; }

    @Test
    public void relative_11() { IRIxTestLib.testRelative("http://example/ns#", "http://example/ns#x", "#x") ; }
}
