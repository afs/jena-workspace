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

package prefixes;

import junit.framework.TestCase;
import org.apache.jena.query.Dataset;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphPrefixesProjection;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb.sys.TDBInternal;

public class Report_Prefixes extends TestCase {
    
    public static void main(String[] args) {
        Dataset ds = TDBFactory.createDataset();
        Txn.executeWrite(ds, ()->{
            ds.getDefaultModel().setNsPrefix("ex", "http://example/");   
        });
        
        // Hard to test for because setup puts the prefixes in the prefix mapping caches.  
        
        Txn.executeRead(ds, ()->{
            DatasetGraphTDB dsg = TDBInternal.getDatasetGraphTDB(ds);
            
            PrefixMapping pmap1 = new GraphPrefixesProjection("",  dsg.getPrefixes());
            PrefixMapping pmap2 = new GraphPrefixesProjectionX("", dsg.getPrefixes());
            String URI = "http://example/foo";
            // JENA-81 sensitive
            System.out.println("pmap1.qnameFor   = "+pmap1.qnameFor(URI)) ;
            System.out.println("pmap1.shortForm  = "+pmap1.shortForm(URI)) ;
            System.out.println();
            
            System.out.println("pmap2.qnameFor   = "+pmap2.qnameFor(URI)) ;
            System.out.println("pmap2.shortForm  = "+pmap2.shortForm(URI)) ;
            // Works because it calls get().
            //System.out.println("pmap2.expandPrefix  = "+pmap2.expandPrefix("ex:a"));
            System.out.println();
            
            pmap2.getNsPrefixMap();
            System.out.println("pmap2a.qnameFor  = "+pmap1.qnameFor(URI)) ;
            System.out.println("pmap2a.shortForm = "+pmap1.shortForm(URI)) ;
        });
    }
}

