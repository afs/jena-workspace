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

package dataset;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.sys.SystemTDB;

public class DevLocal {
    public static void main(String...a) {
        LogCtl.setLog4j();
        try { secure(); } 
        catch (Exception ex) { ex.printStackTrace(); }
        finally { System.exit(0); }
    }
    
    public static void secure() {
        Dataset ds = TDB2Factory.createDataset();
        Txn.executeWrite(ds, ()->RDFDataMgr.read(ds, "D.trig"));
        SecurityFilterTDB2 sf = FiltersTDB2.securityFilter(ds.asDatasetGraph(), NodeUtils.convertToNodes("http://example/g1"));
        //SecurityFilter sf = FiltersTDB2.securityFilter(ds.asDatasetGraph(), Quad.defaultGraphIRI);
        Txn.executeRead(ds, ()->{
            // DO NOT PUT IT ON THE DATASET - CONCURRENT EXECUTION -- ds.getContext().set(SystemTDB.symTupleFilter, sf);
            // No filter
            if ( true ) {
                System.out.println("SPARQL 0");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }", ds);
                QueryExecUtils.executeQuery(qExec);
            }
            // Filter
            if ( true ) {
                System.out.println("SPARQL 1");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }", ds);
                qExec.getContext().set(SystemTDB.symTupleFilter, sf);
                QueryExecUtils.executeQuery(qExec);
            }
            // Filter + unionDefaultGraph
            if ( true ) {
                System.out.println("SPARQL 2");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", ds);
                qExec.getContext().set(TDB2.symUnionDefaultGraph, true);
                FiltersTDB2.withFilter(qExec.getContext(), sf);
                QueryExecUtils.executeQuery(qExec);
            }
            if ( false ) {
                // Filter + Model
                System.out.println("SPARQL 3");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", ds.getDefaultModel());
                // ******* Dataset stripped off.
                // Transfer context:
                //    Wrapped by DatasetGraphFactory.wrap
                //    or in QueryExecutionFactory - a few cases
                
                //qExec.getContext().set(SystemTDB.symTupleFilter, f);
                qExec.getContext().putAll(ds.getContext());
                FiltersTDB2.withFilter(qExec.getContext(), sf);
                QueryExecUtils.executeQuery(qExec);
            }
        });

        System.out.println("DONE");
    }
}
