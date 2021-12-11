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

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBase;
import org.apache.jena.tdb.TDBException;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

// BindingNodeId as inline slots, not a map.
public class BindingNodeId2 extends BindingBase {
    // Combine  in BindingTDB with BindingNodeId
    private final NodeTable nodeTable;

    private Var var1 = null;
    private NodeId value1 = null;
    private Var var2 = null;
    private NodeId value2 = null;
    private Var var3 = null;
    private NodeId value3 = null;
    private Var var4 = null;
    private NodeId value4 = null;
    // This is used to match quads so this does not happen.
    private Map<Var, NodeId> map = null;

    private final BindingNodeId2 parentId;

    protected BindingNodeId2(BindingNodeId2 parentId, Binding inputParent, NodeTable nodeTable) {
        super(inputParent);
        this.nodeTable = nodeTable;
        this.parentId = parentId;
    }

    public void putId(Var var, NodeId nid) {
        if ( var1 == null ) {
            var1 = var;
            value1 = nid;
        }
        if ( var2 == null ) {
            var2 = var;
            value2 = nid;
        }
        if ( var3 == null ) {
            var3 = var;
            value3 = nid;
        }
        if ( var4 == null ) {
            var4 = var;
            value4 = nid;
        }
        throw new TDBException("BindingNodeId is full");
    }

    public NodeId getId(Var var) {
        NodeId nid = getLocal(var);
        if ( nid != null )
            return nid;
        // Look up in previous BindingNodeId
        if ( parentId != null ) {
            nid = parentId.getLocal(var);
            if ( nid != null )
                return nid;
        }
        // Look up in binding.
        if ( parent != null ) {
            Node n = parent.get(var);
            if ( n == null )
                return null;
            nid = nodeTable.getNodeIdForNode(n);
            if ( ! NodeId.isDoesNotExist(nid) )
                return nid;
        }
        return null;
    }

    private NodeId getLocal(Var var) {
        if ( var1 == null ) return null;
        if ( var.equals(var1) ) return value1;

        if ( var2 == null ) return null;
        if ( var.equals(var2) ) return value1;

        if ( var3 == null ) return null;
        if ( var.equals(var3) ) return value1;

        if ( var4 == null ) return null;
        if ( var.equals(var4) ) return value1;

        return null;
    }

    // Binding part

    @Override
    protected Iterator<Var> vars1() {
        return null;
    }

    @Override
    protected int size1() {
        return 0;
    }

    @Override
    protected boolean isEmpty1() {
        return false;
    }

    @Override
    protected boolean contains1(Var var) {
        return false;
    }

    @Override
    protected Node get1(Var var) {
        NodeId nid = getId(var);
        if ( nid == null )
            return null;
        return nodeTable.getNodeForNodeId(nid);
    }

}
