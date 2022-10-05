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

package tools;

public class Notes {
    /*
     * TupleLib.triple
     *   Very long literals.
     *   Long : 165910 (over 200 chars)
     *   Longest : 2081
     *   Thrift does: new String(readBinary(length), "UTF-8");
     *   TDB1 does NodecSSE but is faster (!!!)
     *   
     * An inline cache would be a good thing. global?
     * Share, not share, with NodeId cache.
     */

    // NodeIdInline::
//    public static Cache<NodeId, Node> id2node_Cache = CacheFactory.createCache(2_000_000) ;
//    
//    /** Decode an inline nodeID, return null if not an inline node */
//    public static Node extract(NodeId nodeId) {
//        if ( nodeId == NodeId.NodeDoesNotExist )
//            return null;
//        
//        NodeIdType type = nodeId.type();
//        if ( type == PTR || type == NodeIdType.SPECIAL)
//            return null;
//        
//        Node x = id2node_Cache.getOrFill(nodeId, ()->extract$(nodeId, type));
////        Node x = id2node_Cache.getIfPresent(nodeId);
////        if ( x != null )
////            return x ;
////        x = extract$(nodeId, type);
////        id2node_Cache.put(nodeId, x);
//        return x;
//    }
//        
//    private static Node extract$(NodeId nodeId, NodeIdType type) {
//        
//        switch (type) {

    
    // TDB1:

    // TDB2:
}
