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

package rdf_star;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.*;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;

/** Library for RDF* translation to and from reification form. */
public class RDFX {

    private static final Node rdfSubject   = RDF.Nodes.subject;
    private static final Node rdfPredicate = RDF.Nodes.predicate;
    private static final Node rdfObject    = RDF.Nodes.object;

    /**
     * Returns a copy of the argument graph with any triple terms encoded as
     * reification.
     * <p>
     * See {@link #decodeRDFStar(Graph)} for the reverse operation.
     * <p>
     * See {@link #encodeRDFStarInPlace(Graph)} {@link #decodeRDFStarInPlace(Graph)}
     * for operations that alters the argument graph in-place.
     * <p>
     * Returns a new graph with triples involving triple terms replaced with
     * reification.
     */
    public static Graph encodeRDFStar(Graph graph) {
        Graph output = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(output);
        StreamRDF process = encodeRDFStar(dest);
        StreamRDFOps.graphToStream(graph, process);
        output.getPrefixMapping().samePrefixMappingAs(graph.getPrefixMapping());
        output.getPrefixMapping().setNsPrefix("rdf", RDF.getURI());
        return output;
    }

    /**
     * Copy to a {@link StreamRDF}, encoding RDF* Triple terms by replacing them with
     * RDF Reification.
     */
    public static void encodeRDFStar(Graph graph, StreamRDF dest) {
        StreamRDFOps.sendPrefixesToStream(graph.getPrefixMapping(), dest);
        // Ensure set because this process uses vocabulary from RDF.
        dest.prefix("rdf", RDF.getURI());
        StreamRDF process = encodeRDFStar(dest);
        StreamRDFOps.graphToStream(graph, process);
    }

    /**
     * Returns a copy of the argument graph with any reifications for triple terms
     * translated to triple terms.
     * <p>
     * See {@link #decodeRDFStar(Graph)} for the reverse operation.
     * <p>
     * See {@link #encodeRDFStarInPlace(Graph)} {@link #decodeRDFStarInPlace(Graph)}
     * for operations that alters the argument graph in-place.
     */
    public static Graph decodeRDFStar(Graph graph) {
        Graph gx = GraphFactory.createDefaultGraph();
        decodeRDFStar(graph, StreamRDFLib.graph(gx));
        gx.getPrefixMapping().setNsPrefixes(graph.getPrefixMapping());
        return gx;

    }

    /**
     * Copy the argument graph to a {@link StreamRDF}, replacing reifications with for triple terms.
     * Caution: this operation uses space proportional to the number of triple terms present.
     */
    public static void decodeRDFStar(Graph graph, StreamRDF dest) {
        // Two pass
        // 1: Generate <<>>
        // 2: Process graph

        Map<Node, Node> map = decodeBuildTerms(graph);
        decodeProcessGraph(graph, map, dest);
    }

    /** Return a {@link StreamRDF} that encodes RDF* triples as reification. */
    private static StreamRDF encodeRDFStar(StreamRDF dest) {
        return new ConvertToReified(dest);
    }

    private static class ConvertToReified extends StreamRDFWrapper {

            public ConvertToReified(StreamRDF other) {
                super(other);
            }

            // Cache of the Node of the reification of seen Node_Triple.
            // Reduce the number of triples in repeated reifications.
            //  <<>> :p 1 ; :q 2 ; ....
            private Cache<Node_Triple, Node> cache = CacheFactory.createCache(1000);

            @Override
            public void triple(Triple triple) {
                Triple replacement = encode(triple, cache, other);
                if ( replacement == null )
                    other.triple(triple);
                else
                    other.triple(replacement);
            }

    //        @Override
    //        public void quad(Quad quad) {
    //            boolean b = encode(quad, cache, other);
    //            if ( ! b )
    //                other.quad(quad);
    //        }

        }

    /**
     * Encode RDF* Triple terms by replacing them with RDF Reification.
     * <p>
     * Changes the argument graph in-place.
     * @see #decodeRDFStar
     */
    public static Graph encodeRDFStarInPlace(Graph graph) {
        Graph gx = graph;

        // Accumulate changes so that ConcurrentModificationExceptions don't happen.
        // graph and gx being the same will work.
        List<Triple> inserts = new ArrayList<>();
        List<Triple> deletes = new ArrayList<>();

        StreamRDF insertStream = new StreamRDFApply(inserts::add, null);

        StreamRDF process = new StreamRDFWrapper(insertStream) {
            private Cache<Node_Triple, Node> cache = CacheFactory.createCache(1000);
            @Override
            public void triple(Triple triple) {
                Triple triple2 = encode(triple, cache, insertStream);
                if ( triple2 != null ) {
                    insertStream.triple(triple2);
                    deletes.add(triple);
                }
            }
        };

        StreamRDFOps.sendTriplesToStream(graph, process);
        update(gx, deletes, inserts);
        return gx;
    }

    // Assuming good encoding as reification, reverse the process on a graph.
    /**
     * Replace reification encoding of RDF* terms with RDF* triples.
     * <p>
     * This function assuming any reification use in the graph is for RDF* terms.
     */

    // [RDF*] Streamify. Buffering Graph
    public static Graph decodeRDFStarInPlace(Graph graph) {
        Graph gx = copyGraph(graph);

        graph.find(null, rdfPredicate, null).toList().forEach((t)->{
            List<Triple> inserts = new ArrayList<>();
            List<Triple> deletes = new ArrayList<>();
            decode(gx, t, deletes, inserts);
            update(gx, deletes, inserts);
        });
        return gx;
    }

    // XXX
//    /**
//     * Generate reification triples the input triple has an {@link Node_Triple} term
//     * (i.e. RDF*, triple used as subject or object).
//     *
//     * Return true if this triple was converted.
//     *
//     * @param triple
//     * @param cache used to suppress duplicate reifications
//     * @param stream output stream for reification triples.
//     * @return boolean
//     */
//    private static boolean encode(Quad quad, Cache<Node_Triple, Node> cache, StreamRDF stream) {
//        Node g = quad.getGraph();
//        Node s = quad.getSubject();
//        Node p = quad.getPredicate();
//        Node o = quad.getObject();
//        Node s1 = null;
//        Node o1 = null;
//
//        // Replace terms.
//        if ( s.isNodeTriple() )
//            s1 = addReif((Node_Triple)s, cache, g, stream);
//        if ( o.isNodeTriple() )
//            o1 = addReif((Node_Triple)o, cache, g, stream);
//
//        // Replace triple if changes.
//        if ( s1 == null && o1 == null ) {
//            // No change - do nothing
//            //stream.triple(triple);
//            return false;
//        }
//        // Change. Replace original.
//        if ( s1 == null )
//            s1 = s ;
//        if ( o1 == null )
//            o1 = o ;
//        stream.quad(Quad.create(g, s1, p, o1));
//        return true;
//    }

    /**
     * Generate reification triples the input triple has an {@link Node_Triple} term
     * (i.e. RDF*, triple used as subject or object).
     *
     * Return true if this triple was converted.
     *
     * @param triple
     * @param cache used to suppress duplicate reifications
     * @param stream output stream for reification triples.
     * @return boolean
     */
    private static boolean encodeTop(Triple triple, Cache<Node_Triple, Node> cache, StreamRDF stream) {

        Triple t = encode(triple, cache, stream);
        if ( t == null )
            return false;
        stream.triple(t);
        return true;
    }

    // Encode a triple - do not emit the triple replacement but return it, or null if no change.
    private static Triple encode(Triple triple, Cache<Node_Triple, Node> cache, StreamRDF stream) {
        Node s = triple.getSubject();
        Node p = triple.getPredicate();
        Node o = triple.getObject();
        Node s1 = nodeReif(s, cache, stream);
        Node o1 = nodeReif(o, cache, stream);

        // Replace triple if changes.
        if ( s1 == s && o1 == o ) {
            // No change - do nothing
            //stream.triple(triple);
            return null;
        }
        // Change. Replace original.
        if ( s1 == null )
            s1 = s ;
        if ( o1 == null )
            o1 = o ;
        return Triple.create(s1, p, o1);
    }

    private static Node nodeReif(Node x, Cache<Node_Triple, Node> cache, StreamRDF output) {
        if ( ! x.isNodeTriple() )
            return x;
        Triple t = Node_Triple.triple(x);
        // Reify any nested triple terms. Reifications sent to stream.
        Triple t2 = encode(t, cache, output);
        // If its a new triple, this node is based on the replacement t2.
        Node_Triple nt = ( t2 == null )
            ? Node_Triple.cast(x)
            : (Node_Triple)NodeFactory.createTripleNode(t2);
        return cache.getOrFill(nt, ()->genReif(nt, output));
    }

    /** Build a mapping of reification terms to RDF*triple terms. */
    private static Map<Node, Node> decodeBuildTerms(Graph graph) {
        Map<Node, Node> map = new HashMap<>();
        StreamRDF builder = new StreamRDFBase() {
            @Override
            public void triple(Triple triple) {
                // Vocabulary.
                if ( ! rdfPredicate.equals(triple.getPredicate()) )
                    return;
                Node reif = triple.getSubject();
                Node s = G.getOneSP(graph, reif, rdfSubject);
                Node p = G.getOneSP(graph, reif, rdfPredicate);
                Node o = G.getOneSP(graph, reif, rdfObject);
                Node tripleTerm = NodeFactory.createTripleNode(s,p,o);
                map.put(reif, tripleTerm);
            }
        };
        StreamRDFOps.sendTriplesToStream(graph, builder);
        return map;
    }

    /** Given a mapping of reification terms, convert graph to RDF* graph */
    private static void decodeProcessGraph(Graph graph, Map<Node, Node> map, StreamRDF dest) {
        StreamRDF translate = new StreamRDFWrapper(dest) {
            @Override
            public void triple(Triple triple) {
              //Node g = quad.getGraph();
              Node s = triple.getSubject();
              Node p = triple.getPredicate();
              Node o = triple.getObject();
              // Filter out reifications
              if ( p.equals(rdfSubject) || p.equals(rdfPredicate) || p.equals(rdfObject) )
                  return;

              Node s1 = translate(s, map);
              Node o1 = translate(o, map);
              if ( s == s1 && o == o1 ) {
                  // No change.
                  super.triple(triple);
              }
              else {
                  Triple t = Triple.create(s1, p, o1);
                  super.triple(t);
              }
            }

            /** Recursively search for the replacement for a Node */
            private Node translate(Node x, Map<Node, Node> map) {
                // x is in the map if it is a reification URI.
                Node x1 = map.get(x);
                if ( x1 == null )
                    return x;
                // Recursively translate
                if ( x1.isNodeTriple() ) {
                    Triple triple = Node_Triple.triple(x1);
                    Node s = triple.getSubject();
                    Node p = triple.getPredicate();
                    Node o = triple.getObject();
                    Node s1 = translate(s, map);
                    Node o1 = translate(o, map);
                    if ( s == s1 && o == o1 )
                        return x1;
                    x1 = NodeFactory.createTripleNode(s1, p, o1);
                }
                return x1;
            }
        };
        // Send triples to translater
        StreamRDFOps.sendTriplesToStream(graph, translate);
    }

    /**
     * Test whether a triple has an triple term as one of its components.
     */
    static boolean tripleHasNodeTriple(Triple triple) {
        return triple.getSubject().isNodeTriple()
               /*|| triple.getPredicate().isNodeTriple()*/
               || triple.getObject().isNodeTriple();
    }

    private static Graph copyGraph(Graph graph) {
        if ( false )
            return graph;
        Graph gx = GraphFactory.createDefaultGraph();
        gx.getPrefixMapping().setNsPrefixes(graph.getPrefixMapping());
        graph.find().forEachRemaining(gx::add);
        return gx;
    }

    private static void update(Graph graph, List<Triple> deletes, List<Triple> inserts) {
        deletes.forEach(graph::delete);
        inserts.forEach(graph::add);
    }

    /**
     * Generate the reification for a Node_Triple. Return the subject of the
     * reification triples.
     */
    private static Node genReif(Node_Triple nt, StreamRDF output) {
        Triple t = nt.get();
        Node n = reificationSubject(nt);
        output.triple(Triple.create(n, rdfSubject, t.getSubject()));
        output.triple(Triple.create(n, rdfPredicate, t.getPredicate()));
        output.triple(Triple.create(n, rdfObject, t.getObject()));
        return n;
    }

    /**
     * Calculate and accumulate changes to the graph for one reification.
     * Does not make the changes.
     */
    private static void decode(Graph graph, Triple pReifTriple, List<Triple> deletes, List<Triple> inserts) {
        // Get all triples?
        Node reif = pReifTriple.getSubject();
        System.out.println("    reif = "+NodeFmtLib.str(reif));

        Triple sReifTriple = G.getOne(graph, reif, rdfSubject, null);
        Triple oReifTriple = G.getOne(graph, reif, rdfObject, null);

        Node s = sReifTriple.getObject();
        Node p = pReifTriple.getObject();
        Node o = oReifTriple.getObject();

        Node_Triple nodeTriple = new Node_Triple(s, p, o);
        if ( false )
            inserts.add(Triple.create(s, p, o));

        deletes.add(sReifTriple);
        deletes.add(pReifTriple);
        deletes.add(oReifTriple);

        // What about <<reif1>> :p <<reif2>>
        // [RDF*] BUG : Does not see <<reif1>> changes

        // Find mentions, accumulate deletions and insertions.
        // BOTH!! Uncommon (??!) so don't worry about using a list for "both"
        G.find(graph, reif, null, reif).forEachRemaining(t->{
            Triple tx = Triple.create(nodeTriple, t.getPredicate(), nodeTriple);
            deletes.add(t);
            inserts.add(tx);
        });

        G.find(graph, null, null, reif).forEachRemaining(t->{
            if (! t.getSubject().equals(reif) ) {
                Triple tx = Triple.create(t.getSubject(), t.getPredicate(), nodeTriple);
                deletes.add(t);
                inserts.add(tx);
            }
        });

        // Will find reification triples.
        G.find(graph, reif, null, null).forEachRemaining(t->{
            Node pred = t.getPredicate();
            if ( ! pred.equals(rdfSubject) && ! pred.equals(rdfPredicate) && ! pred.equals(rdfObject) ) {
                if ( ! t.getObject().equals(reif) ) {
                    Triple tx = Triple.create(nodeTriple, pred, t.getObject());
                    deletes.add(t);
                    inserts.add(tx);
                }
            }
        });
    }

    private static final boolean USE_REIF_URIS = true;
    private static String URN_TRIPLE = "urn:triple:";
    private static String BNODE_TRIPLE = "_:T";

//    public static boolean isReificationSubject(Node node) {
//        if ( USE_REIF_URIS )
//            return node.isURI() && node.getURI().startsWith(URN_TRIPLE);
//        else
//            return node.isBlank() && node.getBlankNodeLabel().startsWith(BNODE_TRIPLE);
//    }

    /**
     * Calculate a reification subject node for a {@link Node_Triple}
     * This must be the same node (same by value) when ever called with
     * a {@link Node_Triple} with the same s/p/o.
     */
    public static Node reificationSubject(Node_Triple nodeTriple) {
        Triple t = nodeTriple.get();
        String x = reifStr(t);
        x = Lib.murmurHashHex(x);
        if ( USE_REIF_URIS )
            return NodeFactory.createURI(URN_TRIPLE+x);
        else
            return NodeFactory.createBlankNode(BNODE_TRIPLE+x);
    }

    // Assumes no loops!
    private static String reifStr(Triple triple) {
        return reifStr(triple.getSubject())+reifStr(triple.getPredicate())+reifStr(triple.getObject());
    }

    private static String reifStr(Node node) {
        if ( node.isURI() )
            return node.getURI();
        if ( node.isBlank() )
            return node.getBlankNodeLabel();
        if ( node.isLiteral() ) {
            // Non-URI character to separate the URI, in case we start using the string without hashing.
            return node.getLiteralLexicalForm()+" "+node.getLiteralDatatypeURI();
        }
        Triple t = Node_Triple.tripleOrNull(node);
        if ( t != null )
            return reifStr(t);
        throw new JenaException("Node type not supported in Node_Triple: "+node);
    }

}