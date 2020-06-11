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

package jena1815_var_type;

public class Design {
    // Assume rename apart - no same name variables, not projected, that are inside subqueries.
    // We can linearise IF:

    // All variables (define, optDefine, assign) in LHS are safe.
    // Safe means:
    //   Unused in RHS
    //   Not used in a "variable only position".
    //     Not assigned (i.e. VALUE ?X {} , BIND, LET and sub-query project)
    //     Not in BOUND(?X)
    //     Not used in MINUS
    //     Do not occur in expression-mention-only (filters or assignments)

    // For leftJoin:
    //   Not the double OPTIONAL case (see the LeftJoiClassifier).

    // ----

    // ---- Add "by location" information ?var -> s/p/o

    // Step 1 : blocking cases
    //   variables in the LHS, that are assigned in the RHS.
    //   variables in the LHS that are filter-only or filter-before define,

    // Step 2 : check whether any variables in the right are optional or
    //   filter vars which are also optional in the left side.
    //   because the nature of the "join" will deal with that.

    // Two cases to consider::
    // Case 1 : a variable in the RHS is optional (this is a join we are classifying).
    // Check no variables are optional on right if bound on the left (fixed
    // or optional)
    // Check no variables are optional on the left side, and optional on the
    // right.

    // --------------
}
