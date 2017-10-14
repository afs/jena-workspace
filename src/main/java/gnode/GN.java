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

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Node ;

/**
 * Node-centric traversing of {@link Nav}s, going node to node.
 */
public class GN { 
   
    private final Nav links ;
    private final Node node ;
    
    public GN(Nav findable, Node node) {
        this.links = findable;
        this.node = node ;
    }
    
    private GN wrap(Node n) {
        return new GN(this.links, n); 
    }
    
    private Iter<GN> wrap(Iterator<Node> iter) {
        return Iter.iter(iter).map(this::wrap); 
    }
    
    /**
     * Traverse forwards - returns null (no triple with matching
     * subject-property) or some arbitrary {@link GN} if there are several
     * triples with the subject-property. May not return the same one each
     * time.
     * 
     * @see #fwd1 Return a GN if theer is exactly one subject-property triple.
     */
    public GN fwd(Node property) {
        return wrap(links.fwd(node, property));
    }
    
    /**
     * Traverse forwards - returns null (no triple with matching
     * subject-property) or a {@link GN}.  If there are several
     * triples with the subject-property, throws {@link MultipleNodeException}.
     * 
     * @see #fwdN Return an iterator of GN
     * @throws MultipleNodeException
     */
    public GN fwd1(Node property) {
        return wrap(links.fwd1(node, property));
    }
    
    /**
     * Traverse forwards - returns an iterator of {@link GN} which are objects of 
     * triples matching the subject-property. The iterator may be empty.
     * 
     * @see #fwd1 Return a GN if there is exactly one subject-property triple.
     */
    public Iter<GN> fwdN(Node property) { return wrap(links.fwdN(node, property));} 
    
    /**
     * Traverse backwards - returns null (no triple with matching
     * subject-property) or some arbitrary {@link GN} if there are several
     * triples with the property-object. May not return the same one each
     * time.
     * 
     * @see #bkd1 Return a GN if there is exactly one property-object triple.
     * @see #bkdN Return an iterator of GN
     */
    public GN bkd(Node property) {
        return wrap(links.bkd(node, property));
    }
    
    /**
     * Traverse backwards - returns null (no triple with matching
     * property-object) or a {@link GN}.  If there are several
     * triples with the property-object, throws {@link MultipleNodeException}.
     * 
     * @see #bkdN Return an iterator of GN
     * @throws MultipleNodeException
     */
    public GN bkd1(Node property) {
        return wrap(links.bkd1(node, property));
    }
    
    /**
     * Traverse backwards - returns an iterator of {@link GN} which are subjects of 
     * triples matching the property-object. The iterator may be empty.
     * 
     * @see #bkd1 Return a GN if there is exactly one property-object triple.
     */
    public Iter<GN> bkdN(Node property) { return wrap(links.bkdN(node, property));}
}
