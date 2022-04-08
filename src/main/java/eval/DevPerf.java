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

package eval;

import java.io.IOException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.tdb2.params.StoreParams;

public class DevPerf {
    public static void main(String... args) throws IOException {
        StoreParams params = StoreParams.getDftStoreParams();
        params = StoreParams.builder(params).nodeId2NodeCacheSize(2*params.getNodeId2NodeCacheSize()).build();
        String DIR = "/home/afs/Projects/BSBM/Local/";

        // StageGeneratorGeneric, OpExecutorTDB1, OpExecutorTDB2


        String query = "Q5b.rq";
        //dwim(DIR+"Q5a.rq"); // Fast
        //dwim(DIR+"Q5b.rq"); // Slow filter : ?product <http://www.w3.org/2000/01/rdf-schema#label> ?productLabel)

        //DatabaseConnection.connectCreate(Location.create(DIR+"DB-bsbm"), params);
        System.out.println("Query = "+query);
        dwim(DIR+query);
        tdb2.tdbquery.main("--time", "--loc="+DIR+"DB-bsbm", "--query="+DIR+query);
        System.out.println("Waiting...");
        System.in.read();
        tdb2.tdbquery.main("--time","--repeat=25", "--loc="+DIR+"DB-bsbm", "--query="+DIR+query, "--results=none");
    }

    private static void dwim(String query) {
        Query q = QueryFactory.read(query);
        Op op = Algebra.compile(q);
        Op op1 = Algebra.optimize(op);
        System.out.println(op1);
    }
}
