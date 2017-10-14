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

package gnode;

import static org.apache.jena.graph.Node.ANY ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.util.graph.Findable ;
import org.apache.jena.vocabulary.RDF ;

/** Navigate a collection of triples, be it a {@code Graph} or a {@code Collection<Triple>}.
 * @see GN for a node-centric view of navigating a collection of triples (uses a {@code Nav} internally).  
 */
public class Nav {
    // Better name - "Links" "LinksSet", "Arcs", "Edges"
    // Nav<T>
    // findable<T>
    private final Findable findable ;

    public Nav(Findable findable) {
        this.findable = findable ; 
    }
    
    public Iter<Triple> find(Node s, Node p, Node o) {
        return find(findable, s, p, o) ;
    }

    public Node fwd(Node s, Node p) {
        return object(first(findable, s, p, ANY)) ;
    }

    public Node fwd1(Node s, Node p) {
        return object(exactlyOne(findable, s, p, ANY)) ;
    }

    public Iter<Node> fwdN(Node s, Node p) {
        return find(findable, s, p, ANY).map(triple -> triple.getObject()) ;
    }

    public Node bkd(Node o, Node p) {
        return subject(first(findable, ANY, p, o)) ;
    }

    public Node bkd1(Node o, Node p) {
        return subject(exactlyOne(findable, ANY, p, o)) ;
    }

    public Iter<Node> bkdN(Node o, Node p) {
        return find(findable, ANY, p, o).map(triple -> triple.getSubject()) ;
    }

    public boolean matches(Node s, Node p, Node o) {
        return findable.contains(s, p, o);
    }
    
//    // Backtrack?
//    public Node path(Node x, Node ... path) {
//        Node n = x ;
//        for(Node p : path ) {
//            Node n1 = fwd(x,p);
//            if ( n1 == null )
//                return null;
//            n = n1;
//        }
//        return n;
//    }

    /** Follow an exact path.
     * 
     * @param node
     * @param path
     * @return Node, or null if no such path.
     * @throws MultipleNodeException
     */
    public Node path1(Node node, Node ... path) {
        Node n = node ;
        for(Node p : path ) {
            Node n1 = fwd1(n, p);
            if ( n1 == null )
                return null;
            n = n1;
        }
        return n;
    }

    public int count(Node s, Node p, Node o) {
        return (int)Iter.count(find(s, p, o));
//        return findable.count(s, p, o);
    }

    private static final Node RDF_first     = RDF.first.asNode() ;
    private static final Node RDF_rest      = RDF.rest.asNode() ;
    private static final Node RDF_nil       = RDF.nil.asNode() ;
    
    /**
     * Is {@code node} a list cell? The test is whether {@code node}, as a
     * subject, has exactly one {@code rdf:first} and one {@code rdf:rest}
     * properties and no other others.
     *
     * @see #isListHeadCell
     */
    public boolean isListCell(Node node) {
        return isListCell(node, false);
    }

    /**
     * Is {@code node} the head of a list? The test is whether {@code node}, as a
     * subject, has one {@code rdf:first} and one {@code rdf:rest}
     * properties but may have others (this happens if this is the subject of a triple).
     * <p>
     * If the list is known not to be a subject, use {@link #isListCell}.
     *
     * @see #isListCell
     */
    public boolean isListHeadCell(Node node) {
        return isListCell(node, true);
    }
        
    private boolean isListCell(Node node, boolean head) {
        boolean seenFirst = false;
        boolean seenRest = false;
        Iterator<Triple> iter = find(node, ANY, ANY);
        while ( iter.hasNext() ) {
            Triple triple = iter.next();
            Node predicate = triple.getPredicate();
            if ( RDF_first.equals(predicate) ) {
                if ( seenFirst )
                    return false;
                seenFirst = true;
                continue;
            }
            if ( RDF_rest.equals(predicate) ) {
                if ( seenRest )
                    return false;
                seenRest = true;
                continue;
            }
            if ( !head )
                return false ;
        }
        return seenFirst && seenRest; 
    }
    
    public boolean isNil(Node n) { return RDF_nil.equals(n); }

    /** Return the elements of the well-formed list, in order, or null.
     * @throws BadListException
     */  
    public List<Node> members(Node n) {
        Node x = n;
        List<Node> elements = new ArrayList<>();
        for(;;) {
            if ( isNil(x) )
                break;
            Node elt = fwd1(RDF_first, ANY);
            if ( elt == null )
                throw new BadListException();
            x = fwd1(RDF_rest, ANY);
            if ( x == null )
                throw new BadListException();
            // ???
            if ( count(n, ANY, ANY) != 2 )
                throw new BadListException();
            elements.add(elt);
        }
        return elements;
    }
    
    private static Triple first(Findable findable, Node s, Node p, Node o) {
        Iterator<Triple> iter = find(findable, s, p, o);
        if ( ! iter.hasNext() )
            return null;
        return iter.next();
    }
    
    private static Triple exactlyOne(Findable findable, Node s, Node p, Node o) {
        Iterator<Triple> iter = find(findable, s, p, o);
        if ( ! iter.hasNext() )
            return null;
        Triple triple = iter.next();
        if ( iter.hasNext() )
            throw new MultipleNodeException();
        return triple;
    }
    
    private Node subject(Triple triple) {
        if ( triple == null )
            return null ;
        return triple.getSubject();
    }

    private Node property(Triple triple) {
        if ( triple == null )
            return null ;
        return triple.getPredicate();
    }

    private Node object(Triple triple) {
        if ( triple == null )
            return null ;
        return triple.getObject();
    }
    
    private static Iter<Triple> find(Findable findable, Node s, Node p, Node o) { return Iter.iter(findable.find(s, p, o)); }
}
