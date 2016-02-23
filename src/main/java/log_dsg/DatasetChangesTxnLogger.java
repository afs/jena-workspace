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

import static log_dsg.L.print ;
import static log_dsg.L.str ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.QuadAction ;

public class DatasetChangesTxnLogger implements DatasetChangesTxn {
    @Override
    public void change(QuadAction qaction, Node g, Node s, Node p, Node o) {
        print("%-2s  %s %s %s %s", 
              qaction.label, str(g), str(s), str(p), str(o)) ;
    }

    @Override
    public void start() {
        print("start") ;
    }

    @Override
    public void finish() {
        print("finish") ;
    }

    @Override
    public void reset() {
        print("reset") ;
    }

    @Override
    public void begin1(ReadWrite readWrite) {
        print("begin1:"+readWrite) ;
    }

    @Override
    public void begin2(ReadWrite readWrite) {
        print("begin2:"+readWrite) ;
    }

    @Override
    public void commit1() {
        print("commit1") ;
    }

    @Override
    public void commit2() {
        print("commit2") ;
    }

    @Override
    public void abort1() {
        print("abort1") ;
    }

    @Override
    public void abort2() {
        print("abort2") ;
    }

    @Override
    public void end1() {
        print("end1") ;
    }

    @Override
    public void end2() {
        print("end2") ;
    }
}
