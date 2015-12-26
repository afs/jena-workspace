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

package tdb;

import org.apache.jena.query.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;

public class DevTDBCache {

    public static void main(String[] args) {
        Dataset ds = TDBFactory.createDataset() ;
        RDFDataMgr.read(ds, "D.ttl") ;
        
        TupleIndex[] indexes = ((DatasetGraphTransaction)ds.asDatasetGraph()).get().getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        for ( int i = 0 ; i < indexes.length ; i++ ) {
            TupleIndex idx = indexes[i] ;
            System.out.println(idx.getName());
            // Cache only 2 fixed. 
            int len = (idx.getName().startsWith("P")) ? 2 : 2 ;
            TupleIndex idx2 = new TupleIndexCache(idx, len) ;
            indexes[i] = idx2 ;
        }
        
        String prefix = "PREFIX : <http://example/> " ;
        String qs = "SELECT * { ?s :p ?o OPTIONAL { ?o :q ?v } }" ;   
        
        Query query = QueryFactory.create(prefix+qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        QueryExecUtils.executeQuery(qExec);
        
        
    }

}
