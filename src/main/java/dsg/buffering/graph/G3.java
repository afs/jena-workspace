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

package dsg.buffering.graph;

import java.util.function.Predicate;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.util.iterator.ExtendedIterator;

public class G3 {
    // Migrate to G. and NodeUtils?
    //And fix GraphPlain

    /** Contains by "same term", regardless of whether the graph implements "same value" or "same term". */
    public static boolean containsByEquals(Graph graph, Triple triple) {
        return containsByEquals(graph, triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    /** Contains by "same term", regardless of whether the graph implements "same value" or "same term". */
    public static boolean containsByEquals(Graph graph, Node s, Node p, Node o) {
        // Do direct for efficiency.
        if ( ! graph.contains(s,p,o) )
            return false;
        // May have matched by value.  Do a term test find to restrict to RDF terms.
        ExtendedIterator<Triple> iter = graph.find(s, p, o);
        // Unless generalized RDF, only need to test object.
        Predicate<Triple> predicate = (dataTriple) -> sameTermMatch(s, p, o, dataTriple);
        iter = iter.filterKeep(predicate);
        try {
            return iter.hasNext();
        } finally { iter.close(); }

//    // For reference - just object
//    if ( !o.isConcrete() || o.isLiteral() ) {
//        Predicate<Triple> predicate = (t) -> sameTermMatch(o, t.getObject()) ;
//        iter = iter.filterKeep(predicate) ;
//    }

    }

    /**
     * Match a ground triple (even ANY and variables are considered ground terms in the
     * data triple) with S/P/O which can be wildcards (ANY or null).
     */
    public static boolean sameTermMatch(Node matchSubj, Node matchPred, Node matchObj, Triple dataTriple) {
        return
            sameTermMatch(matchSubj, dataTriple.getSubject()) &&
            sameTermMatch(matchPred, dataTriple.getPredicate()) &&
            sameTermMatch(matchObj,  dataTriple.getObject());
    }

    /**
     * Match a ground RDF Term (ANY and variables are considered ground terms in the
     * data term) with a node which can be a wildcard (ANY or null).
     * Language tags compare case-insensitively.
     */
    public static boolean sameTermMatch(Node match, Node data) {
        if ( false ) {
            if ( match==null || Node.ANY.equals(match) )
                return true;
            // If lang tags are compared case-insensitively.
            return match.equals(data);
        }

        // Allow for case-insensitive language tag comparison.
        // Two literals.
        if ( ! Util.isLangString(data) || ! Util.isLangString(match) )
            // Not both strings with lang tags.
            return (match==null) || (match == Node.ANY) || match.equals(data) ;

        // Language tags compare case insensitively.
        String lex1 = data.getLiteralLexicalForm();
        String lex2 = data.getLiteralLexicalForm();
        String lang1 = data.getLiteralLanguage();
        String lang2 = data.getLiteralLanguage();
        return lex1.equals(lex2) && lang1.equalsIgnoreCase(lang2);
    }
}
