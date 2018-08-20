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

package fuseki;

import fuseki.security.GraphFilter;
import fuseki.security.SecurityPolicy;
import fuseki.security.SecurityRegistry;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;

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
        
        // ---- Set up the registry.
        SecurityRegistry reg = new SecurityRegistry();
        reg.put("user1", new SecurityPolicy("http://example/g1", Quad.defaultGraphIRI.getURI()));
        reg.put("user2", new SecurityPolicy("http://example/g1", "http://example/g2"));

        SecurityPolicy sCxt = reg.get("user2");

        // Isolated context datasetgraph
        DatasetGraphWrapper dsg1 = new DatasetGraphWrapper(ds.asDatasetGraph()) {
            private Context cxt2 = ds.getContext().copy();
            @Override
            public Context getContext() {
                return cxt2;
            }
        };        

        // Hack
        GraphFilter<?> gf = sCxt.filterTDB2(ds.asDatasetGraph());
        dsg1.getContext().set(gf.getContextKey(), gf);
        
        
        //sCxt.filterTDB(dsg1, qExec);
        // Copy context.
        // Modify
        
        Dataset ds1 = DatasetFactory.wrap(dsg1);
        
        Txn.executeRead(ds, ()->{
            // No filter
            if ( true ) {
                System.out.println("SPARQL 0");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }", ds);
                QueryExecUtils.executeQuery(qExec);
            }
            // Filter execution
            if ( false ) {
                System.out.println("All: Filter execution");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }", ds);
                sCxt.filterTDB(ds.asDatasetGraph(), qExec);
                QueryExecUtils.executeQuery(qExec);
            }
            // Filtered dataset
            if ( false ) {
                System.out.println("All: Filtered dataset");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }", ds1);
                QueryExecUtils.executeQuery(qExec);
            }

            
            // Filter + unionDefaultGraph
            if ( true ) {
                System.out.println("Union: Filtered execution");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", ds);
                qExec.getContext().set(TDB2.symUnionDefaultGraph, true);
                sCxt.filterTDB(ds.asDatasetGraph(), qExec);
                QueryExecUtils.executeQuery(qExec);
            }
            // Filter + unionDefaultGraph
            if ( true ) {
                System.out.println("Union: Filtered dataset");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", ds1);
                qExec.getContext().set(TDB2.symUnionDefaultGraph, true);
                QueryExecUtils.executeQuery(qExec);
            }

            if ( true ) {
                // Filter + Model
                System.out.println("model 1 : filter execution");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", ds.getDefaultModel());
                // ******* Dataset stripped off.
                // Transfer context:
                //    Wrapped by DatasetGraphFactory.wrap
                //    or in QueryExecutionFactory - a few cases
                
                //qExec.getContext().set(SystemTDB.symTupleFilter, f);
                qExec.getContext().putAll(ds.getContext());
                sCxt.filterTDB(ds.asDatasetGraph(), qExec);
                QueryExecUtils.executeQuery(qExec);
            }
            if ( true ) {
                // Filter + Model
                System.out.println("model 1 : filter dataset");
                System.out.println("Filtered model 2/ wrapped ds");
                QueryExecution qExec = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", ds1.getDefaultModel());
                // ******* Dataset stripped off.
                qExec.getContext().putAll(ds1.getContext());
                QueryExecUtils.executeQuery(qExec);
            }
        });

        System.out.println("DONE");
    }
}
