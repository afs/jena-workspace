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

import java.io.StringReader;

import junit.framework.TestCase;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphPrefixesProjection;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb.sys.TDBInternal;

// JENA-81 report.

public class Report_Prefixes2 extends TestCase {
    static final String GRAPH_NAME = "file:pop-wales.ttl";

    static final String TEST_URI = "http://statistics.data.wales.gov.uk/def/dimension/region";

    static final String DATA = 
        "@prefix sw-dim: <http://statistics.data.wales.gov.uk/def/dimension/> .\n" +
        "@prefix qb:      <http://purl.org/linked-data/cube#> .\n" +
        "<http://statistics.data.wales.gov.uk/dataset/pca-population/2011-02-25/2008NAWAC44> a qb:Observation; \n" +
        "<http://statistics.data.wales.gov.uk/def/dimension/region>  <http://data.ordnancesurvey.co.uk/id/7000000000041309> .";

    // Delete GraphPrefixesProjection
    // GraphPrefixesProjectionTDB -> org.apache.jena.sparql.core.DatasetPrefixMapping
    // 
    
    public static void main(String[] args) {
        Dataset ds = TDBFactory.createDataset();
        Txn.executeWrite(ds, ()->{
            ds.getDefaultModel().setNsPrefix("ex", "http://example/");    
        });
        
        
        Txn.executeRead(ds, ()->{
            DatasetGraphTDB dsg = TDBInternal.getDatasetGraphTDB(ds);
            
            PrefixMapping pmap = new GraphPrefixesProjection("",  dsg.getPrefixes());
            //PrefixMapping pmap = new GraphPrefixesProjection("", dsg.getPrefixes());
            String URI = "http://example/foo";
            // JENA-81
            System.out.println(pmap.qnameFor(URI)) ;
            System.out.println(pmap.shortForm(URI)) ;
        });
    }
    
    public static void main1(String[] args) {
        String DB = "DB" ;
        
        if ( false )
        {
            // This creates the test data.
            FileOps.ensureDir(DB);
            FileOps.clearDirectory(DB) ;

            DatasetGraph tdb = TDBFactory.createDatasetGraph(DB);
            Dataset src = DatasetFactory.wrap(tdb);
            Model m = src.getNamedModel(GRAPH_NAME);
            m.read(new StringReader(DATA), null, "Turtle");

            assertEquals("sw-dim:region", m.shortForm(TEST_URI));
            TDB.sync(src);
            tdb.close();
            System.out.println("BUILT");
            return;
        }
        
        DatasetGraph tdb = TDBFactory.createDatasetGraph(DB);
        Dataset src = DatasetFactory.wrap(tdb);
        Model m = src.getNamedModel(GRAPH_NAME);

        if ( false )
            // Flushes everything into the PrefixMappingImpl cache.
            m.getNsPrefixMap() ;
        
        // Alternative.
//        // With this uncommented the lookup works and the list shows all expected prefixes
//        for (Map.Entry<String, String> binding : m.getNsPrefixMap().entrySet()) {
//            System.out.println(binding.getKey() + " = " + binding.getValue());
//        }

        System.out.println(m.qnameFor(TEST_URI)) ;
        System.out.println(m.shortForm(TEST_URI)) ;
        
        //assertEquals("sw-dim:region", m.shortForm(TEST_URI));
        tdb.close();
        
        System.out.println("DONE");
    }
}

