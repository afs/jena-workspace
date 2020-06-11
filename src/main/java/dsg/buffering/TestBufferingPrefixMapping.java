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

package dsg.buffering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.junit.Test;

public class TestBufferingPrefixMapping {

    @Test public void buffering_prefix_01() {
        PrefixMapping base = new PrefixMappingImpl();
        PrefixMapping pmap = new BufferingPrefixMapping(base);
        assertEquals(0, pmap.numPrefixes());
        assertTrue(pmap.hasNoMappings());
    }

    @Test public void buffering_prefix_02() {
        PrefixMapping base = new PrefixMappingImpl();
        base.setNsPrefix("x", "http://example/");
        PrefixMapping pmap = new BufferingPrefixMapping(base);
        assertEquals(1, pmap.numPrefixes());
        assertFalse(pmap.hasNoMappings());
    }

    @Test public void buffering_prefix_03() {
        PrefixMapping base = new PrefixMappingImpl();
        PrefixMapping pmap = new BufferingPrefixMapping(base);
        pmap.setNsPrefix("x", "http://example/");

        assertFalse(pmap.hasNoMappings());
        assertTrue(base.hasNoMappings());

        assertEquals(1, pmap.numPrefixes());
        assertEquals(0, base.numPrefixes());
    }

    @Test public void buffering_prefix_04() {
        PrefixMapping base = new PrefixMappingImpl();
        base.setNsPrefix("x", "http://example/");
        PrefixMapping pmap = new BufferingPrefixMapping(base);
        pmap.removeNsPrefix("x");

        assertTrue(pmap.hasNoMappings());
        assertFalse(base.hasNoMappings());

        assertEquals(0, pmap.numPrefixes());
        assertEquals(1, base.numPrefixes());
    }

    // XXX More

}
