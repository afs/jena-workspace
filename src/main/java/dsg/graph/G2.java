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

package dsg.graph;

import static org.apache.jena.graph.Node.ANY;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.other.G;
import org.apache.jena.util.IteratorCollection;
import org.apache.jena.util.iterator.ExtendedIterator;

public class G2 {

    public static void findExec(Graph graph, Consumer<Triple> action, Node s, Node p , Node o) {
        ExtendedIterator<Triple> eIter = G.find(graph, s, p, o);
        try {
            eIter.forEach(action);
        } finally { eIter.close(); }
    }

    // From GraphUtils.

    /** Add triples into the destination (arg 1) from the source (arg 2)*/
    public static void addInto(Graph dstGraph, Graph srcGraph ) {
        if ( dstGraph == srcGraph && ! dstGraph.getEventManager().listening() )
            return ;
        dstGraph.getPrefixMapping().setNsPrefixes(srcGraph.getPrefixMapping()) ;
        findExec(srcGraph, dstGraph::add, ANY, ANY, ANY);
        dstGraph.getEventManager().notifyAddGraph( dstGraph, srcGraph );
    }

    private static void addIteratorWorker( Graph graph, Iterator<Triple> it ) {
        List<Triple> s = IteratorCollection.iteratorToList( it );
        addIteratorWorkerDirect(graph, s.iterator());
    }

    private static void addIteratorWorkerDirect( Graph graph, Iterator<Triple> it ) {
        it.forEachRemaining(graph::add);
//        if ( OldStyle && graph instanceof GraphWithPerform ) {
//            GraphWithPerform g = (GraphWithPerform)graph;
//            it.forEachRemaining(g::performAdd);
//        } else {
//            it.forEachRemaining(graph::add);
//        }
    }

    // Abbreviations

    public static void execTxn(Graph graph, Runnable action) {
        graph.getTransactionHandler().executeAlways(action);
    }

    public static <X> X calcTxn(Graph graph, Supplier<X> action) {
        return graph.getTransactionHandler().calculateAlways(action);
    }
}
