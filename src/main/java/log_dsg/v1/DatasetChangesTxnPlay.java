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

package log_dsg.v1;

import log_dsg.L ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.QuadAction ;
import org.apache.jena.sparql.core.Transactional ;

public class DatasetChangesTxnPlay implements DatasetChangesTxn {

    public DatasetChangesTxnPlay(Transactional transactional, DatasetGraph dsg) {
    }
    
    @Override
    public void change(QuadAction qaction, Node g, Node s, Node p, Node o) {
        L.print("%-2s  %s %s %s %s", 
              qaction.label, L.str(g), L.str(s), L.str(p), L.str(o)) ;
    }

    @Override
    public void start() {
        L.print("start") ;
    }

    @Override
    public void finish() {
        L.print("finish") ;
    }

    @Override
    public void reset() {
        L.print("reset") ;
    }

    @Override
    public void begin1(ReadWrite readWrite) {
        L.print("begin1:"+readWrite) ;
    }

    @Override
    public void begin2(ReadWrite readWrite) {
        L.print("begin2:"+readWrite) ;
    }

    @Override
    public void commit1() {
        L.print("commit1") ;
    }

    @Override
    public void commit2() {
        L.print("commit2") ;
    }

    @Override
    public void abort1() {
        L.print("abort1") ;
    }

    @Override
    public void abort2() {
        L.print("abort2") ;
    }

    @Override
    public void end1() {
        L.print("end1") ;
    }

    @Override
    public void end2() {
        L.print("end2") ;
    }
}
