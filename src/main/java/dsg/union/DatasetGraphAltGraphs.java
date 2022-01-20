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

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.*;

/**
 * DatasetGraph which provides alternative special graphs : default graph or union graph.
 * Updates go to the special graphs or named graphs.
 */
public class DatasetGraphAltGraphs extends DatasetGraphWrapper implements DatasetGraphWrapperView {

    public static DatasetGraph defaultUnionGraph(DatasetGraph dsg) {
        Graph ug = dsg.getUnionGraph();
        return new DatasetGraphAltGraphs(ug, ug, dsg);
    }

    public static DatasetGraph altDefaultGraph(DatasetGraph dsg, Graph graph) {
        return new DatasetGraphAltGraphs(graph, null, dsg);
    }

    private final Graph dftGraph;
    private final Graph unionGraph;

    /** null means "defer to dataset */
    /*package*/ DatasetGraphAltGraphs(Graph dftGraph, Graph unionGraph, DatasetGraph dsg) {
        super(dsg);
        this.dftGraph = dftGraph;
        this.unionGraph = unionGraph;
    }

    @Override
    public Graph getDefaultGraph() {
        if ( dftGraph != null )
            return dftGraph;
        return super.getDefaultGraph();
    }

    @Override
    public Graph getUnionGraph() {
        if ( unionGraph != null )
            return unionGraph;
        return super.getUnionGraph();
    }

    @Override
    public Iterator<Quad> find(Quad quad) {
        if ( quad.isUnionGraph() ) {}
        if ( quad.isDefaultGraph() ) {}
        return getR().find(quad);
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        if ( Quad.isUnionGraph(g) ) {}
        if ( Quad.isDefaultGraph(g) ) {}
        return getR().find(g, s, p, o);
    }

    @Override
    public boolean contains(Quad quad) {
        if ( quad.isUnionGraph() ) {}
        if ( quad.isDefaultGraph() ) {}
        return getR().contains(quad);
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        if ( Quad.isUnionGraph(g) ) {}
        if ( Quad.isDefaultGraph(g) ) {}
        return getR().contains(g, s, p, o);
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        if ( Quad.isUnionGraph(g) ) {}
        if ( Quad.isDefaultGraph(g) ) {}
        return getR().findNG(g, s, p, o);
    }
}