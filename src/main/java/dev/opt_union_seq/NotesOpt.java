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

package dev.opt_union_seq;

public class NotesOpt {
/*
-- Required
op = TransformScopeRename.transform(op) ;
    // Prepare expressions.
    OpWalker.walk(op, new OpVisitorExprPrepare(context)) ;
    
    // Convert paths to triple patterns if possible.
    if ( context.isTrueOrUndef(ARQ.optPathFlatten) ) {
        op = apply("Path flattening", new TransformPathFlattern(), op) ;
        // and merge adjacent BGPs (part 1)
        if ( context.isTrueOrUndef(ARQ.optMergeBGPs) )
        op = apply("Merge BGPs", new TransformMergeBGPs(), op) ;
    }
-- Basics

    // Property functions
    transformPropertyFunctions :: TransformPropertyFunction

-- Expressions
    // Expression constant folding
    transformExprConstantFolding :: ExprTransformConstantFold
    
    // Expand (A&&B) to two filter (A), (B) so that they can be placed independently.
    transformFilterConjunction :: TransformFilterConjunction

    // Expand IN and NOT IN which then allows other optimizations to be applied.
    transformFilterExpandOneOf :: TransformExpandOneOf

-- ??
    // Eliminate/Inline assignments where possible
    transformInlineAssignments :: TransformEliminateAssignments

    // Apply some general purpose filter transformations
    transformFilterImplicitJoin :: TransformFilterImplicitJoin
    transformFilterImplicitLeftJoin :: TransformImplicitLeftJoin

    // (filter (|| (= ?x <x>) (!= ?x <y>)) (bgp ( ?s ?p ?x)) )",
    // (disjunction  (assign ((?x <x>)) (bgp ( ?s ?p <x>)))
    //           (filter (!= ?x <y>) (bgp ( ?s ?p ?x))) )
    transformFilterDisjunction :: TransformFilterDisjunction
   
-- Sorting   
    // TopN
    transformTopNSorting :: TransformTopN
    
    // ORDER BY+DISTINCT optimizations
    transformOrderByDistinctApplication

    // Transform some DISTINCT to REDUCED, slightly more liberal transform that ORDER BY+DISTINCT application
    transformDistinctToReduced :: TransformDistinctToReduced

-- Joins    
    // Join strategy : (leftjoin) and (join)
    transformJoinStrategy :: TransformJoinStrategy
        
    // Place filters
    transformFilterPlacement :: TransformFilterPlacement
    
    // Apply (possibly a second time) after FILTER placement
    transformFilterEquality :: TransformFilterEquality
        
    // Replace suitable FILTER(?x != TERM) with (minus (original) (table))
    // Off by default due to minimal performance difference
    transformFilterInequality :: TransformFilterInequality

-- Final
    // Promote table empty as late as possible since this will only be produced by other 
    // optimizations and never directly from algebra generation
    transformPromoteTableEmpty :: TransformPromoteTableEmpty

    transformMergeBGPs :: TransformMergeBGPs

    // Normally, leave to the specific engines.
    transformReorder :: TransformReorder BGPs
    
    // Merge (extend) and (assign) stacks
    transformExtendCombine :: TransformExtendCombine
 
 */
}
