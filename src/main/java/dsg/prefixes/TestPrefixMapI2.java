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

package dsg.prefixes;

class TestPrefixMapI2 {}

//import org.apache.jena.dboe.storage.StoragePrefixes;
//import org.apache.jena.dboe.storage.prefixes.*;
//import org.apache.jena.dboe.storage.simple.StoragePrefixesMem;
//import org.apache.jena.shared.PrefixMapping;
//import org.apache.jena.sparql.graph.AbstractTestPrefixMappingView;
//
//public class TestPrefixMapI2 extends AbstractTestPrefixMappingView {
//
//    private StoragePrefixMap spv = null;
//
//    @Override
//    protected PrefixMapping create() {
//        // Dataset storage.
//        StoragePrefixes sPrefixes = new StoragePrefixesMem();
//        // Storage oriented view for a single graph : StoragePrefixesView
//        spv = StoragePrefixesView.viewDefaultGraph(sPrefixes);
//        return new Impl_PrefixMapI2(spv);
//    }
//
//    @Override
//    protected PrefixMapping view() {
//        return new Impl_PrefixMapI2(spv);
//    }
//}
