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
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;

/**
 * Generate a parse exception on seeing a RDF-star triple term.
 */
public class StreamRDFNoStar extends StreamRDFBase {

    // Better as ParserProfile to give line and column.
    // ?? A StreamRDF with line/column?

    public StreamRDFNoStar() { }

    @Override
    public void triple(Triple triple) {
        check(triple.getSubject());
        //check(triple.getPredicate());
        check(triple.getObject());

        super.triple(triple);
    }

    @Override
    public void quad(Quad quad) {
        //check(triple.getGraph());
        check(quad.getSubject());
        //check(triple.getPredicate());
        check(quad.getObject());
        super.quad(quad);
    }

    private void check(Node term) {
        if ( term.isNodeTriple() ) { //|| term.isNodeGraph() ) {}
            throw new RiotException("RDF-star Triple term: "+term);
        }
    }
}
