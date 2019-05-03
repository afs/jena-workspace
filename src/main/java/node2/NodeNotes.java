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

package node2;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.sparql.expr.NodeValue;

public class NodeNotes {
    Node node;
    Node_Literal lit;
    Node_Blank blank;
    NodeValue nv;
/*
 * label : in Node: only hashCode and toString. 
 *         else push down slot.
 * THRESHOLD - unused.        
 */
    
/*
 * Node_literal: value field -> via subclass.
 *    No LiteralLabel held - inline the 
 *    Value only Node_Liteal (delayed lexical)
 *    Maybe only one kind of LiteralLabel
 * NodeValue: more library?
 *    becomes an interface + defaults?
 *    Factory: "NVal" 
 *    
 * Node_Blank: Holds two longs. Label?
 * Node_URI: OK
 * Node_Variable : Var.
 */
}
