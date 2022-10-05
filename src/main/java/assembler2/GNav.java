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

package assembler2;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.other.RDFDataException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.sparql.util.graph.GraphUtils;


/**
 * Functions for navigating a graph.
 * Taken from the model-resource level {@link GraphUtils}.
 * This set of functions covers the access requirements of assemblers/constructors.
 */
public class GNav {
    // Default values.

    /**
     * Produce a string for a node. Converts non-literals to a string form.
     */
    private static String anyToString(Node node) {
        Objects.requireNonNull(node, "node anyToString");
        if ( node.isURI() ) return node.getURI();
        if ( node.isLiteral() ) return node.getLiteralLexicalForm();
        if ( node.isBlank()) return "_:"+NodeFmtLib.encodeBNodeLabel(node.getBlankNodeLabel());
        if ( node.isVariable()) return "?"+node.getName();
        throw new RDFDataException("Can't turn node '"+node+"' into a string");
    }

    /**
     * Produce a string for a node which is a plain string or a URI.
     */
    private static String stringOrURI(Node node) {
        Objects.requireNonNull(node, "node stringOrURI");
        if ( node.isURI() )
            return node.getURI();
        if ( NodeUtils.isSimpleString(node) )
            return node.getLiteralLexicalForm();
        throw new RDFDataException("Not a simple string or URI:" +node);
    }


    /**
     * Get a string from a simple literal node, otherwise throw {@link RDFDataException}.
     */
    private static String asString(Node node) {
        Objects.requireNonNull(node, "node asString");
        if ( node.isURI() )     throw new RDFDataException("URI where string expected: "+node);
        if ( node.isBlank())    throw new RDFDataException("Blank node where string expected:" +node);
        if ( node.isVariable()) throw new RDFDataException("Variable node where string expected:" +node);
        if ( NodeUtils.hasLang(node) )
            throw new RDFDataException("Language string node where simple string expected:" +node);
        if ( ! NodeUtils.isSimpleString(node) )
            throw new RDFDataException("Datatype literals where string expected:" +node);
        return node.getLiteralLexicalForm();
    };

    /**
     * Produce strings for nodes which are string or URIs.
     */
    private static List<String> asStrings(List<Node> nodes) {
        return Iter.iter(nodes.iterator()).map(GNav::stringOrURI).toList();
    };

    /**
     * Get a list of the URIs (as strings) and strings
     * @see #getAsStringValue
     */
    public static List<String> multiValueAsString(Graph graph, Node subject, Node predicate) {
        List<Node> nodes = multiValue(graph, subject, predicate);
        return asStrings(nodes);
    }

    public static List<Node> multiValue(Graph graph, Node subject, Node predicate) {
        return G.listSP(graph, subject, predicate);
    }

    public static List<Node> multiValueURI(Graph graph, Node subject, Node predicate) {
        List<Node> nodes = multiValue(graph, subject, predicate);
        check(nodes, Node::isURI, n->errorMsg(graph, subject, predicate, "Not a URI"));
        return nodes;
    }

    /** Return all object of S-P nodes, checking a condition (if null, then the condition is anything) */
    private static List<Node> multiValue(Graph graph, Node subject, Node predicate,
                                         Predicate<Node> condition, Function<Node, String> errorMessage) {
        List<Node> nodes = multiValue(graph, subject, predicate);
        check(nodes, condition, errorMessage);
        return nodes;
    }

    private static String errorMsg(Graph graph, Node subject, Node predicate, String msg) {
        PrefixMap pmap = PrefixMapFactory.create(graph.getPrefixMapping());
        String subj = NodeFmtLib.str(subject, pmap);
        String pred = NodeFmtLib.str(predicate, pmap);
        return String.format("%s : %s %s", msg, subj, pred);
    }

    private static String errorMsg(Graph graph, Node node, String msg) {
        PrefixMap pmap = PrefixMapFactory.create(graph.getPrefixMapping());
        String x = NodeFmtLib.str(node, pmap);
        return String.format("%s : %s", msg, x);
    }

    private static boolean isString(Node node) {
        if ( ! node.isLiteral() )
            return false;
        if ( ! node.getLiteralDatatype().equals(XSDDatatype.XSDboolean) )
            return false;
        return true;
    }

    private static boolean isBoolean(Node node) {
        if ( ! node.isLiteral() )
            return false;
        if ( ! node.getLiteralDatatype().equals(XSDDatatype.XSDboolean) )
            return false;
        return true;
    }

    private static void check(Collection<Node> nodes, Predicate<Node> condition, Function<Node, String> errorMessage) {
        if ( condition == null )
            return;
        Iter.forEach(nodes.iterator(), n->{
            if ( !condition.test(n) )
                throw new RDFDataException(errorMessage.apply(n));
        });
    }

    public static boolean exactlyOne(Graph graph, Node subject, Node predicate) {
        return G.hasOneSP(graph, subject, predicate);
    }

    public static boolean atmostOne(Graph graph, Node subject, Node predicate) {
        // Add G operations for this
        List<Node> x = G.listSP(graph, subject, predicate);
        return x.isEmpty() || x.size() == 1;
    }

    private static Node getExactlyOne(Graph graph, Node subject, Node predicate) {
        return G.getOneSP(graph, subject, predicate);
    }

    public static boolean atLeastOne(Graph graph, Node subject, Node predicate) {
        return G.contains(graph, subject, predicate, null);
    }

    public static boolean getBooleanValue(Graph graph, Node subject, Node predicate) {
        Node x = getExactlyOne(graph, subject, predicate);
        if ( ! x.isLiteral() )
            throw new RDFDataException(errorMsg(graph, subject, predicate, "Not a literal"));
        if ( ! x.getLiteralDatatype().equals(XSDDatatype.XSDboolean) )
            throw new RDFDataException(errorMsg(graph, subject, predicate, "Not a boolean"));
        return (Boolean)x.getLiteralValue();
    }

    /** Get a string literal. */
    public static String getStringValue(Graph graph, Node subject, Node predicate) {
        Node x = G.getOneSP(graph, subject, predicate);
        if ( ! x.isLiteral() )
            throw new RDFDataException(errorMsg(graph, subject, predicate, "Not a literal"));
        if ( ! x.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
            throw new RDFDataException(errorMsg(graph, subject, predicate, "Not a string"));
        return (String)x.getLiteralValue();
    }

    /** Get a string literal or a URI as a string. */
    public static String getAsStringValue(Graph graph, Node subject, Node predicate) {
        Node x = getExactlyOne(graph, subject, predicate);
        return asString(x);
    }

    public static String string(Node node) {
        return asString(node);
    }

    /** Get exactly one (no absent, no duplicates) */
    public static Node getAsNode(Graph graph, Node subject, Node predicate) {
        return G.getOneSP(graph, subject, predicate);
    }

    /** Get one, no duplicates; default value if absent */
    public static Node getAsNode(Graph graph, Node subject, Node predicate, Node defaultValue) {
        Node x = G.getZeroOrOneSP(graph, subject, predicate);
        return ( x != null ) ? x : defaultValue;
    }

    /**
     * URI or blank node
     */
    public static Node getResourceValue(Graph graph, Node subject, Node predicate) {
        Node x = getExactlyOne(graph, subject, predicate);
        if ( ! x.isBlank() && ! x.isURI() )
            throw new RDFDataException(errorMsg(graph, subject, predicate, "Not a URI or a blank node"));
        return x;
    }

    public static List<Node> listResourcesByType(Graph graph, Node type) {
        return G.nodesOfTypeAsList(graph, type);
    }

    /**
     * Return a node which as the given type. Must be unique.
     */
    public static Node getNodeByType(Graph graph, Node type) {
        List<Node> x = G.nodesOfTypeAsList(graph, type);
        isUnique(x, ()->errorMsg(graph, type, "getNodeByType : not unique"));
        return x.get(0);
    }

    private static void isUnique(List<Node> x, Supplier<String> errorMessage) {
        if ( x.isEmpty() )
            throw new RDFDataException("Empty: "+errorMessage.get());
        if ( x.size() != 1 )
            throw new RDFDataException("Not unique: "+errorMessage.get());
    }

    /** Includes RDFS */
    public static Node findRootByType(Graph graph, Node type) {
        List<Node> x = G.listNodesOfTypeRDFS(graph, type);
        isUnique(x, ()->errorMsg(graph, type, "findRootByType : not unique"));
        return x.get(0);
    }

    /** Includes RDFS */
    public static List<Node> findRootsByType(Graph graph, Node type) {
        List<Node> x = G.listNodesOfTypeRDFS(graph, type);
        return x;
    }
}
