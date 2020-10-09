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

package query;

public class NotesSkolemization {
/*
 * [1]
 *  NodeFmtLib.encodeBNodeLabel, decodeBNodeLabel not applied consistently.
 *
 *  Query printing :: ==> bnodes to <_:..> ??
 *  Query parsing  :: ==> <_:..> to bnodes always??
 *
 *  No ARQ.constantBNodeLabels, only strict mode.
 *
 * ----
 * [2] Centralize:
 *    RiotLib.createIRIorBNode does not decode.
 *    ParameterizedQuery.nodeEnc does encode.
 *
 *  Currently:
 *    If ARQ.constantBNodeLabels == false,
 *    still puts in blank node in patterns.
 * ----
 * [3] TestSkolemization for Turtle and SPARQL.
 *
 * ----
 *   Data. Real blank node.
 *
 *    Sparql.
 *    Pattern vs data??

 *    Query pattern:
 *    Not anon variable.
 *    Real bnode, exact match.
 *    Syntax bnode to anon var in parser.
 *    So skolem to bnode.
 *
 *    Query data:
 *    Real bnode.
 *
 *    Skolemize printing? Always.
 *    Bnode to skolem.
 *    Anon var to bnode.
 *
 *    Query parsing:
 *    Real bnodes normally.
 *    Leave alone if strict. No warning.
 *
 *    Printing.
 *    Data: optional skolemize.
 *    Query: Always skolemize (otherwise illegal).
 *
 *    Substitute:
 *    As skolem.
 *    As bnode.
 *
 */
}

