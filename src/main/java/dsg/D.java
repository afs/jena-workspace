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

package dsg;

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.RDFDataException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.NodeConst;

public class D {
    private D() {}

    private static Node rdfType = NodeConst.nodeRDFType;

    /** Return the subject of a quad, or null if the quad is null. */
    public static Node subject(Quad quad) {
        return quad == null ? null : quad.getSubject();
    }

    /** Return the predicate of a quad, or null if the quad is null. */
    public static Node predicate(Quad quad) {
        return quad == null ? null : quad.getPredicate();
    }

    /** Return the object of a quad, or null if the quad is null. */
    public static Node object(Quad quad) {
        return quad == null ? null : quad.getObject();
    }

    /** Return the object of a quad, or null if the quad is null. */
    public static Node graph(Quad quad) {
        return quad == null ? null : quad.getGraph();
    }

//    isURI(Node)
//    isBlank(Node)
//    isLiteral(Node)
//    isResource(Node)
//    isNodeTriple(Node)
//    isNodeGraph(Node)
//    nullAsAny(Node)
//    nullAsDft(Node, Node)

    /** Does the graph match the s/p/o pattern? */
    public static boolean contains(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(dsg, "dataset");
        return dsg.contains(graph, subject, predicate, object);
    }

    /** Does the dataset use the node anywhere as a graph term: subject, predicate or object? */
    public static boolean containsNode(DatasetGraph dsg, Node node) {
        Objects.requireNonNull(dsg, "dataset");
        Objects.requireNonNull(node, "node");
        return // Order for looking : S-O-P
            contains(dsg, Node.ANY, node, Node.ANY, Node.ANY) ||
            contains(dsg, Node.ANY, Node.ANY, Node.ANY, node) ||
            contains(dsg, Node.ANY, Node.ANY, node, Node.ANY);
    }

//    isOfType(Graph, Node, Node)
//    hasType(Graph, Node, Node)
//    hasProperty(Graph, Node, Node)

    /** Contains exactly one. */
    public static boolean containsOne(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(dsg, "dataset");
        Iterator<Quad> iter = dsg.find(graph, subject, predicate, object);
        return exactlyOne(iter);
    }

    private static <X> boolean exactlyOne(Iterator<X> iter) {
        try {
            if ( ! iter.hasNext() )
                return false;
            iter.next();
            return !iter.hasNext();
        } finally { Iter.close(iter); }
    }

    /** Find one triple matching subject-predicate-object. Return quad or throw {@link RDFDataException}. */
    private static Quad findUniqueQuad(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        // Better stack trace and error messages if done explicitly.
        Iterator<Quad> iter = dsg.find(graph, subject, predicate, object);
        if ( ! iter.hasNext() )
            throw new RDFDataException("No match : "+matchStr(graph, subject, predicate, object));
        Quad x = iter.next();
        if ( iter.hasNext() )
            throw new RDFDataException("More than one match : "+matchStr(graph, subject, predicate, object));
        return x;
    }

    private static String matchStr(Node graph, Node subject, Node predicate, Node object) {
        return "("+NodeFmtLib.strNodesTTL(graph, subject, predicate, object)+")";
    }
}
