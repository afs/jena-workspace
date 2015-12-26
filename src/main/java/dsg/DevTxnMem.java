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

package dsg;

import java.util.Iterator ;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Transactional ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.TDBFactory ;

public class DevTxnMem {

    public static void main(String[] args) {
        //DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;
        DatasetGraph dsg = TDBFactory.createDatasetGraph() ;
        Transactional t = (Transactional)dsg ;
        
        dsg.add(SSE.parseQuad("(_ :s1 :p :o1)")) ;
        dsg.add(SSE.parseQuad("(_ :s2 :p :o1)")) ;
        dsg.add(SSE.parseQuad("(_ :s3 :p :o1)")) ;
        dsg.add(SSE.parseQuad("(_ :s4 :p :o2)")) ;
//        dsg.add(SSE.parseQuad("(_ :s5 :p :o2)")) ;
//        dsg.add(SSE.parseQuad("(_ :s6 :p :o2)")) ;

        System.out.println("READ") ;
        t.begin(ReadWrite.READ) ;
        Iterator<Quad> iter = dsg.find() ;
        System.out.println(iter.next()) ;
        System.out.println("END") ;
        t.end() ;
        System.out.println(iter.next()) ;
        System.out.println(iter.next()) ;
        System.out.println(iter.next()) ;
        System.out.println(iter.next()) ;
        System.out.println("DONE") ;
    }

}
