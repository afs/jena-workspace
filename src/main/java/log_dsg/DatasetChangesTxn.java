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

package log_dsg;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetChanges ;

interface DatasetChangesTxn extends DatasetChanges {
    public void begin1(ReadWrite readWrite) ;
    public void begin2(ReadWrite readWrite) ;
    
    public void commit1() ;
    public void commit2() ;
    
    public void abort1() ;
    public void abort2() ;

    public void end1() ;
    public void end2() ;
}