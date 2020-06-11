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

package glib;

public class NoteGLib {
    // Single class of functions for working with graph/triples/nodes.
    // Streaming!

    // Mostly in G
    // Divide into sections.
    // -- basic graph : find, contains, hasProperty
    // -- getSP, getPO, getOne, getOneOrZero,
    // -- tests: absentOrOne, containsOne
    // -- typesOfNode, nodesofType
    // -- subclass, subproperty
    // -- Bulk: G2

    // -- listSubjects, listObjects, listPredicates: GLib

    // -- Quad and Triple transforms.

    // Check coverage

    // rename "setSP" as "allSP" or ? No sets - only iterSP().toSet()

    // Categories:
    //   Bulk operations - copy, delete, clear


    // Replace ExtendedIterator with Iter for Graph, not API.

    //interface ExtendedIterator<T> extends ClosableIterator<T>
    // AutoCloseable

    //    ExtendedIterator<T>
    //    ExtendedIterator.removeNext()   :: ??
    //    ExtendedIterator.nextOptional() :: Add to Iter.

    //    ExtendedIterator.andThen(Iterator<X>) :: Iter.concat

    //    ExtendedIterator.filterKeep(Predicate<T>) :: Iter.filter
    //    ExtendedIterator.filterDrop(Predicate<T>) :: Iter:notFilter

    //    ExtendedIterator.mapWith(Function<T, U>) :: Iter.map
    //    ExtendedIterator.toList() :: Iter.toList
    //    ExtendedIterator.toSet() :: Iter.toSet

    // Close?


}

