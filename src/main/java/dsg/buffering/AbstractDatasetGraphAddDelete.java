/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package dsg.buffering;

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.util.Context;


// DatasetGraphWrapper like functionality.
// Use interface default methods?
/**
 * Catch all changes to calls and reduce to a few methods.
 * <ul>
 * <li>{@link #actionAdd}</li>
 * <li>{@link #actionDelete}</li>
 * </ul>
 * This includes implementing {@link #deleteAny} as a
 * loop that uses a array to record then delete from the dataset.
 */
public abstract class AbstractDatasetGraphAddDelete extends DatasetGraphBaseFind {

    private final DatasetGraph dsg;
    private Context context;

    /** Return the DatasetGraph being wrapped. */
    public final DatasetGraph getWrapped() {
        return get();
    }

    /** The dataset to use for redirection - can be overridden.
     *  It is also guaranteed that this is called only once per
     *  delegated call.  Changes to the wrapped object can be
     *  made based on that contract.
     */
    protected DatasetGraph get() { return dsg; }


    public AbstractDatasetGraphAddDelete(DatasetGraph dsg) {
        super() ;
        this.dsg = dsg;
    }

    // No op implementation.
//    protected void actionAdd(Node g, Node s, Node p, Node o) {
//        super.add(g, s, p, o);
//    }
//    protected  void actionDelete(Node g, Node s, Node p, Node o) {
//        super.delete(g, s, p, o);
//    }


    protected abstract void actionAdd(Node g, Node s, Node p, Node o);
    protected abstract void actionDelete(Node g, Node s, Node p, Node o);

    @Override
    public void add(Quad quad) {
        actionAdd(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void delete(Quad quad) {
        actionDelete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        actionAdd(g,s,p,o);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        actionDelete(g, s, p, o);
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        removeGraph(graphName);
        graph.find(null, null, null)
            .forEachRemaining((t) -> this.add(graphName, t.getSubject(), t.getPredicate(), t.getObject()));
    }

    @Override
    public void removeGraph(Node graphName)
    { deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY) ; }

    @Override
    public void setDefaultGraph(Graph graph) {
        graph.find(null, null, null)
             .forEachRemaining((t) -> this.add(Quad.defaultGraphNodeGenerated, t.getSubject(), t.getPredicate(), t.getObject()));
    }

    @Override
    public void clear()
    { deleteAny(Node.ANY, Node.ANY, Node.ANY, Node.ANY) ; }

    @Override
    public Graph getDefaultGraph()
    { return GraphView.createDefaultGraph(this) ; }

    @Override
    public Graph getGraph(Node graphNode)
    { return GraphView.createNamedGraph(get(), graphNode) ; }

    // Unbundle deleteAny
    private static final int DeleteBufferSize = 10000;
    @Override
    /** Simple implementation but done without assuming iterator.remove() */
    public void deleteAny(Node g, Node s, Node p, Node o) {
        // TODO DRY This code.
        // This is duplicated: see DatasetGraphBase.
        // We need to do the conversion here.
        // DRY => DSGUtils
        // Convert deleteAny to deletes.
        Quad[] buffer = new Quad[DeleteBufferSize];
        while (true) {
            Iterator<Quad> iter = find(g, s, p, o);
            // Get a slice
            int len = 0;
            for ( ; len < DeleteBufferSize ; len++ ) {
                if ( !iter.hasNext() )
                    break;
                buffer[len] = iter.next();
            }
            // Delete them.
            for ( int i = 0 ; i < len ; i++ ) {
                delete(buffer[i]);
                buffer[i] = null;
            }
            // Finished?
            if ( len < DeleteBufferSize )
                break;
        }
    }
}
