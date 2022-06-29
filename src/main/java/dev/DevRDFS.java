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

import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;

public class DevRDFS {

    public static void main(String[] args) {
        //Model data = RDFDataMgr.loadModel("file:/home/afs/tmp/RDFS/data.ttl");
        //Model data = RDFDataMgr.loadModel("file:/home/afs/tmp/RDFS/small.ttl");

        Model data;
        if ( false ) {
            data = TDBFactory.createDataset("/home/afs/tmp/RDFS/DB1a").getDefaultModel();
        }
        else {
            data = RDFDataMgr.loadModel("file:/home/afs/tmp/RDFS/small.ttl");
        }

        //RDFDataMgr.write(System.out,  data,  Lang.TTL);

        //Model data = RDFDataMgr.loadModel("file:/home/afs/tmp/RDFS/small.ttl");
        List<Rule> rules = Rule.rulesFromURL("/home/afs/tmp/RDFS/rules.rule");
        System.out.println(data.size());
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);

        InfModel inf = ModelFactory.createInfModel(reasoner, data);


        Model target = data;

        //
        String qs = """
                PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX  wd:   <http://www.wikidata.org/entity/>
                PREFIX  ex:   <http://example.com/>
                PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
                SELECT  (count(*) AS ?C)
                WHERE
                """;
        qs = qs + "{ ex:condition0 rdf:type ?type }";

        ResultSet rs = QueryExecution.create(qs, target).execSelect();
        ResultSetFormatter.out(rs);

    }

}
