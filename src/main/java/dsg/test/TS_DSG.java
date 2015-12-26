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

package dsg.test;

import org.apache.jena.sparql.core.TestDatasetGraphBaseFind_General ;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemoryFind ;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemoryFindPattern ;
import org.apache.jena.tdb.store.TestDatasetGraphTDBFind ;
import org.apache.jena.tdb.store.TestDatasetGraphTDBFindPattern ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.junit.runners.Suite.SuiteClasses ;

@RunWith(Suite.class)
@SuiteClasses(
{
    TestDatasetGraphBaseFind_General.class
    , TestDatasetGraphTDBFind.class
    , TestDatasetGraphTDBFindPattern.class
    
    , TestDatasetGraphInMemoryFind.class
    , TestDatasetGraphInMemoryFindPattern.class
    
    , TestDatasetGraphTDBFind.class
    , TestDatasetGraphTDBFindPattern.class

})
public class TS_DSG {

}
