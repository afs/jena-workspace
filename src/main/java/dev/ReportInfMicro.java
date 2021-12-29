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

import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.rdfs.SetupRDFS;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;

public class ReportInfMicro {
    static {
        JenaSystem.init();
        FusekiLogging.setLogging();
        //LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String... args) {
        System.out.println("Start: "+DateTimeUtils.nowAsString());

        DatasetGraph dsg0 = DatasetGraphFactory.create();
        RDFDataMgr.read(dsg0, "/home/afs/Desktop/ICD10CM.ttl.gz");

        System.out.println("BASE:  "+DateTimeUtils.nowAsString());
        System.out.println(dsg0.getDefaultGraph().size());
        Graph g = dsg0.getDefaultGraph();
        Model m = ModelFactory.createModelForGraph(g);

        System.out.println("RDFS");
        Reasoner reasoner1 = ReasonerRegistry.getRDFSReasoner();
        InfModel mRDFS = ModelFactory.createInfModel(reasoner1, m);
        //Model mRDFSx =  ModelFactory.createRDFSModel(m);
        mRDFS.listStatements();
        System.out.println("RDFS:  "+DateTimeUtils.nowAsString());

        if ( false ) {
            System.out.println("Micro");
            Reasoner reasoner2 = ReasonerRegistry.getOWLMicroReasoner();
            InfModel mMicro = ModelFactory.createInfModel(reasoner2, m);
            mMicro.listStatements();
            System.out.println("Micro: "+DateTimeUtils.nowAsString());
        }

        System.out.println("Data+RDFS");
        SetupRDFS setup = RDFSFactory.setupRDFS(dsg0.getDefaultGraph());
        System.out.println(setup.getSubClassHierarchy().size());

        DatasetGraph dsg = RDFSFactory.datasetRDFS(dsg0, setup);
        dsg.find();
        System.out.println("DRDFS: "+DateTimeUtils.nowAsString());

//        System.out.println(dsg0.stream(null, null, RDFS.Nodes.subClassOf, null).count());
//        System.out.println(dsg.stream(null, null, RDFS.Nodes.subClassOf, null).count());

        System.out.println("DONE");
    }
}
