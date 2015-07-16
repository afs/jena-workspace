/**
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

package dev;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import rdfpatch.DatasetGraphPatchTransaction ;

public class MainTransactionByPatch
{
    static { LogCtl.setCmdLogging() ; }
    
    public static void main(String[] args) {
        DatasetGraph dsg1 = DatasetGraphFactory.createMem() ;
        DatasetGraphPatchTransaction dsg = new  DatasetGraphPatchTransaction(dsg1) ;
        Quad q = SSE.parseQuad("(:g <s> <p> _:a)") ;
        dsg.begin(ReadWrite.WRITE);
        dsg.add(q) ;
        SSE.write(dsg) ;
        dsg.abort() ;
        dsg.end();
        SSE.write(dsg) ;
        
        dsg.begin(ReadWrite.WRITE);
        dsg.add(q) ;
        dsg.commit() ;
        dsg.end();
        SSE.write(dsg) ;
        
        dsg.begin(ReadWrite.READ);
        Iter.toList(dsg.find()) ;
        dsg.end();
        System.out.println("DONE") ; 
    }
}

