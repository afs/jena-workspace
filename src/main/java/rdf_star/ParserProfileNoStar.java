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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.ParserProfileWrapper;

public class ParserProfileNoStar extends ParserProfileWrapper {

    public ParserProfileNoStar(ParserProfile other) {
        super(other);
    }

    /** Create a triple node (RDF*) */
    @Override
    public Node createTripleNode(Node subject, Node predicate, Node object, long line, long col) {
        throw new RiotParseException("RDF* Triple Term not permitted", line, col);
    }

    /** Create a triple node (RDF*) */
    @Override
    public Node createTripleNode(Triple triple, long line, long col) {
        throw new RiotParseException("RDF* Triple Term not permitted", line, col);
    }


}

