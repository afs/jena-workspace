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

package dsg.union;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;

/** Union default graph for any dataset*/
public class DevUnionDSG {
    //DatasetGraphWrapperView

    // Imperfect for things that dive inside implementation.
    // *** What about Context setting? Universal.
    // Push down to storage, but also in OpExecutor.
    
    // Symbol:  tdb:unionDefaultGraph, tdb2:unionDefaultGraph
    //  arq:unionDefaultGraph : isUnionDftQuery.
    // Build into OpExecutor.
    //   OpExecutor.executeUnion(OpBGP opBGP, QueryIterator input).
    //   And/Or rewrite to GRAPH <union> quads
    //   Default to quadded, not graph?
    
    /* TDB1:
     * Quads: 
     * OpExecutorTDB.decideGraphNode
     * BGP:
     * decideGraphNode => Union graph -> Node.ANY
     * 
     * 
     * 
     * SolverLib:
     * public executes: 
     *     graph.getNodeTupleTable()  or ds.chooseNodeTupleTable
     * private SolverLib.execute/NodeTupleTable
     *  if ( Quad.isUnionGraph(graphNode) )
     *       graphNode = Node.ANY ;
     *   if ( Quad.isDefaultGraph(graphNode) )
     *       graphNode = null ;
     * 
     * boolean anyGraph = (graphNode==null ? false : (Node.ANY.equals(graphNode))) ;
     * GRAPH <union>
     */
    
    /* TDB2: same in TDB1 OpExecutor2.
     */
    
    // QueryEngineFactoryWrapper.accept
    static class DatasetGraphMorph extends DatasetGraphWrapper implements DatasetGraphWrapperView {
        public DatasetGraphMorph(DatasetGraph dsg) {
            super(dsg);
        }
    }
    
    // JENA-1668.
    public static void main(String...args) {
        
        // This is Morph.
        
        // DatasetGraphMapLink
        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
        RDFDataMgr.read(dsg, "D.trig");
        //RDFDataMgr.write(System.out, dsg, Lang.NQ);
        
//        DatasetGraphMapLink dsgm = (DatasetGraphMapLink)dsg;
//        
//        // BUT NOT UPDATEABLE.
//        dsgm.setDefaultGraph(dsg.getUnionGraph());

        // Change the read-default graph.
        DatasetGraph dsg1 = new DatasetGraphMorph(dsg) {
            @Override public Graph getDefaultGraph() {
                return new GraphWrapper(super.getDefaultGraph()) {
                    @Override public ExtendedIterator<Triple> find(Triple t) {
                        return getUnionGraph().find(t);
                    }
                    @Override public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
                        return getUnionGraph().find(s, p, o);
                    }
                };
            }
        };
        
        RDFDataMgr.write(System.out, dsg1, Lang.NQ);
        
        try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(dsg1))) {
            conn.queryResultSet("SELECT * { ?s ?p ?o }",
                rs->ResultSetFormatter.out(rs));
            conn.update("INSERT DATA { <x:s> <x:p> 'new'}");
        }
        
        RDFDataMgr.write(System.out, dsg, Lang.NQ);
    }
}
