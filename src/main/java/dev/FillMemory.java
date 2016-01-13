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

package dev;

import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.system.StreamRDFWrapper ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;

public class FillMemory {
    public static void main(String ... argv) {
        String filename = null ; 
        DatasetGraph dsg = null ;
        
        StreamRDF s1 = StreamRDFLib.dataset(dsg) ;
        StreamRDF s2 = new StreamRDFProgress(s1) ;
        
        RDFDataMgr.parse(s2, filename); 
        System.out.println("DONE") ;
    }
    
    
    /** Wrap another StreamRDF and provide counts of items */
    public static class StreamRDFProgress extends StreamRDFWrapper implements StreamRDF, StreamRDFCounting {
        private long countTriples  = 0 ;
        private long countQuads    = 0 ;
        private long countBase     = 0 ;
        private long countPrefixes = 0 ;

        public StreamRDFProgress(StreamRDF other) {
            super(other) ;
        }

        @Override
        public void triple(Triple triple) {
            countTriples++ ;
            super.triple(triple) ;
        }

        @Override
        public void quad(Quad quad) {
            countQuads++ ;
            super.quad(quad) ;
        }

        @Override
        public long count() {
            return countTriples + countQuads ;
        }

        @Override
        public long countTriples() {
            return countTriples ;
        }

        @Override
        public long countQuads() {
            return countQuads ;
        }
    }
}
