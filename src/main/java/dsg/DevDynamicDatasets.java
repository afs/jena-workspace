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

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.* ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.DatasetDescription ;
import org.apache.jena.sparql.core.DynamicDatasets ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.apache.jena.tdb.TDBFactory ;

public class DevDynamicDatasets
{
    static { LogCtl.setCmdLogging() ; }
    
    public static void main(String ... a) {

        // FROM and datasets.
        Dataset ds ;
        if ( false )    ds = DatasetFactory.createTxnMem() ;
        if ( false )    ds = TDBFactory.createDataset() ;
        if ( true  )    ds = DatasetFactory.create() ;
        
        RDFDataMgr.read(ds, "D.trig") ;

        String qs = "SELECT * FROM <http://example/g1> { ?s ?p ?o }" ;
        Query q = QueryFactory.create(qs) ;
        Dataset ds1 = ds ;
        if ( true ) {
            DatasetDescription desc = DatasetDescription.create(q) ;
            ds1 = DynamicDatasets.dynamicDataset(desc, ds, false) ;
        }

        
        System.out.println("-----------");
        RDFDataMgr.write(System.out, ds1, Lang.TRIG);
        System.out.println("-----------");
        
        QueryExecution qExec = QueryExecutionFactory.create(q, ds1) ;
        QueryExecUtils.executeQuery(qExec);
        
        
        System.exit(0) ;
    }
}
