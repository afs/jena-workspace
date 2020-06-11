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

public class NotesRDFStar {

    // https://github.com/RDFstar/RDFstarTools
    // Revisit RDF* docs

    // https://rdf4j.org/documentation/programming/rdfstar/

    // ** Test and Migrate RDFX to org.apache.jena.system (in jena-arq) JENA-1903

    /*
     * https://arxiv.org/abs/1406.3399
     * https://blog.liu.se/olafhartig/2019/01/10/position-statement-rdf-star-and-sparql-star/
     */

    // Migrate X.java?
    // G Glib, G2

    // REPORT
    // Terms in result sets mean that free triple terms exist naturally.
    //
    // BIND->FIND
    //
    // FIND to use code QueryIterBlockTriplesStar.rdfStarTriple - needs right entry
    // point. RX
    // QueryIterBlockTriplesStar.matchTripleStar(QueryIterator chain, Var var, Triple
    // triple, ExecutionContext execContext)
    // RX.rdfStarTriple -> QueryIterator

    // 3/ BIND
    // 4/ application/sparql-results+json, application/sparql-results+xml
    // 5/ N-triple: the one line, one triple feature is important. Using <<>> in NT
    // should not result in anything more than the triple described.
    // 6/ Datasets and compound graphs
    // - one graph refers to another
    // - one graph is a subset of another.
    // - datasets, esp default graph = union of named graphs.
    // 7/ Update
    // Weirdness DELETE DATA { :s :p :o } ; INSERT DATA { :s :p :o }

    // SHOULD
    // PG mode - StreamRDF.
    // Look [RDF*] (only in RDFX)
    // PG mode. All deletes cause looking for the terms in S and O position.
    // RDFX and quads.

    // ALSO
    // Deprecate ResultSetApply etc.

    // Other
    // Clear up PrintSerializableBase implements PrintSerializable extends Printable

    // Node_Triple.triple -> library: G, GLib
    // Node.getTriple()
    // Node.getGraph()
}
