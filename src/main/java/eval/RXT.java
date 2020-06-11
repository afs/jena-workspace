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

package eval;

import static org.apache.jena.graph.Node_Triple.triple;

import java.util.function.Consumer;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFind;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.util.Context;

/**
 * Transform library for RDF*.
 * <p>
 * There are two entry points.
 * <p>
 * Function {@link #rdfStarTriple} for matching a single triple pattern in a basic
 * graph pattern that may involve RDF* terms.
 * <p>
 * Function {@link #matchTripleStar} for matches a triple term and assigning the
 * triple matched to a variable. It is used within {@link #rdfStarTriple} for nested
 * triple term and a temporary allocated variable as well can for
 * {@code FIND(<<...>> AS ?t)}.
 * <p>
 * In each case, the call takes a {@code Consumer<Op> acc}. Each op needed (OpTriple, OpFind) is sent to this consumer.
 * <p>
 * {@code RXT} is a "before quad transform" step.
 */
public class RXT {

    /**
     * Match a single triple pattern that may involve RDF* terms.
     * This is the top level function for matching triples.
     *
     * The function {@link #matchTripleStar} matches a triple term and assigns the triple matched to a variable.
     * It is used within {@link #rdfStarTriple} for nested triple term and a temporary allocated variable
     * as well can for {@code FIND(<<...>> AS ?t)}.
     *
     * @apiNote
     *     Use the test {@link #tripleHasNodeTriple(Triple)} to see if
     *     processing is required. If this function is called on a triple without any
     *     RDF Term patterns, the code simply emits the triple - a a plain call of
     *     {@link #matchData} which is simply:
     *
     *     <pre>
     *     acc.accept(new OpTriple(pattern));
     *     </pre>
     */
    public static void rdfStarTriple(Consumer<Op> acc, Triple triple, Context cxt) {
//        // Should all work without this trap for plain RDF.
//        // Better to call tripleHasNodeTriple first to avoid the object churn of emitting an OpTriple.
//        if ( ! tripleHasNodeTriple(triple) ) {
//            // No RDF* : direct to data.
//            matchData(acc, triple);
//        }
        rdfStarTripleSub(acc, triple, cxt);
    }

    private static VarAlloc varAlloc(Context context) {
        VarAlloc varAlloc = VarAlloc.get(context, ARQConstants.sysVarAllocRDFStar);
        if ( varAlloc == null ) {
            varAlloc = new VarAlloc(ARQConstants.allocVarTripleTerm);
            context.set(ARQConstants.sysVarAllocRDFStar, varAlloc);
        }
        return varAlloc;
    }

    /**
     * Insert the stages necessary for a triple with triple pattern term inside it.
     * If the triple pattern has a triple term, possibly with variables, introduce
     * an iterator to solve for that, assign the matching triple term to a hidden
     * variable, and put allocated variable in to main triple pattern. Do for subject
     * and object positions, and also any nested triple pattern terms.
     */
    private static void rdfStarTripleSub(Consumer<Op> acc, Triple triple, Context cxt) {
        Triple rewrite = preprocessForTripleTerms(acc, triple, cxt);
        matchData(acc, rewrite);
    }

    /**
     * Match a triple pattern (which may have nested triple terms in it).
     * Any matched triples are added as triple terms bound to the supplied variable.
     */
    public static void matchTripleStar(Consumer<Op> acc, Var var, Triple triple, Context cxt) {
        if ( tripleHasNodeTriple(triple) ) {
            Triple rewrite = preprocessForTripleTerms(acc, triple, cxt);
            triple = rewrite;
        }
        // Match to data and assign to var in each binding, based on the triple pattern grounded by the match.
        bindTripleTerm(acc, var, triple, cxt);
    }

    /**
     * Process a triple for triple terms.
     * <p>
     * This creates additional matchers for triple terms in the pattern triple recursively.
     */
    private static Triple preprocessForTripleTerms(Consumer<Op> acc, Triple patternTriple, Context cxt) {
        Node s = patternTriple.getSubject();
        Node p = patternTriple.getPredicate();
        Node o = patternTriple.getObject();
        Node s1 = null;
        Node o1 = null;

        // Recurse.
        if ( s.isNodeTriple() && ! s.isConcrete() ) {
            Triple t2 = triple(s);
            Var var = varAlloc(cxt).allocVar();
            Triple tripleTerm = Triple.create(t2.getSubject(), t2.getPredicate(), t2.getObject());
            matchTripleStar(acc, var, tripleTerm, cxt);
            s1 = var;
        }
        if ( o.isNodeTriple() && ! o.isConcrete() ) {
            Triple t2 = triple(o);
            Var var = varAlloc(cxt).allocVar();
            Triple tripleTerm = Triple.create(t2.getSubject(), t2.getPredicate(), t2.getObject());
            matchTripleStar(acc, var, tripleTerm, cxt);
            o1 = var;
        }

        // Because of the test in rdfStarTriple,
        // This code only happens when there is a triple term.

        // No triple term in this triple.
        if ( s1 == null && o1 == null )
            return patternTriple;

        // Change. Replace original.
        if ( s1 == null )
            s1 = s ;
        if ( o1 == null )
            o1 = o ;
        Triple triple1 = Triple.create(s1, p, o1);
        return triple1;
    }

    /**
     * Add a binding to each row with triple grounded by the current row.
     * If the triple isn't concrete, then just return the row as-is.
     */
    private static void bindTripleTerm(Consumer<Op> acc, Var var, Triple pattern, Context cxt) {
        // Part of execution of OpFind is to match the data : matchData(acc, pattern);
        Op op = new OpFind(pattern, var);
        acc.accept(op);
    }

    /**
     * Match the graph with a triple pattern.
     * This is the accessor to the graph.
     * It assumes any triple terms have been dealt with.
     */
    private static void matchData(Consumer<Op> acc, Triple pattern) {
        acc.accept(new OpTriple(pattern));
    }

    /**
     * Test whether a triple has an triple term as one of its components.
     */
    public static boolean tripleHasNodeTriple(Triple triple) {
        return triple.getSubject().isNodeTriple()
               /*|| triple.getPredicate().isNodeTriple()*/
               || triple.getObject().isNodeTriple();
    }
}

