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

public class NotesFaster {

    // Filter-BGP reorder. JENA-2317.

    /*
     * [ ] Better BPT Iterator with jump ahead. "Cursor"
     * [ ] Bytes off disk faster?? (Better, avoid needing access)
     * [ ] ** Rocks For node tables
     * [ ] Co-fetch (?s (:p1 ?v1) (:p2 ?v2) (:p3 ?v3)) then expand.
     * [ ] All joins.
     * [ ] Push in filters to TDB2
     * [ ] (filter variable value)
     */

    // Possible ops: Decomposing bgp+filters
    // * (access s p o . filter depending on only the triple)
    // * "Inline filter" -- a filter except no subop.

    /*
     * ** (bgp s p o . filter of one var ) -- includes range.
     * Partial reorder - pull 2-term  first always then
     * 2-term triple patterns vs 1 term + filter.
     */
}
