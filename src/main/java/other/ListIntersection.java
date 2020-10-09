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

package other;

import static org.junit.Assert.assertEquals;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Test;

public class ListIntersection {

    private static boolean DEBUG = false;

    /* Java considerations.
     *   List<> is an interface that can backed by ArrayList, LinkedList, (Vector)
     *   Difference in access costs.
     *   ListIterator is always O(1) but stops probing or binary search lookup.
     * RandomAccess
     * Streams don't help much.
     *
     *  Assume comparator has been applied to the lists
     *  Assume no nulls
     */

    interface IntersectionCalc<X> { public List<X> intersection(List<X> list1, List<X> list2, Comparator<X> comparator); }

    /** Minimal code solution. Not cardinality preserving. Not good O. */
    public static <X> Collection<X> intersection_probe_search_min(List<X> list1, List<X> list2, Comparator<X> comparator) {
        // Distinct means the results are the same of the lists are swapped.
        return list1.stream().filter(list2::contains).distinct().collect(Collectors.toList());
    }

    /** Defensive minimal code solution */
    public static <X> List<X> intersection_probe_search(List<X> list1, List<X> list2, Comparator<X> comparator) {
        if ( list1.size() > list2.size() ) {
            List<X> tmp = list1;
            list1 = list2 ;
            list2 = tmp;
        }

        if ( list1.spliterator().hasCharacteristics(Spliterator.ORDERED) ) {
            // NB Assumes stream does not reorder!
            // List.stream() default to the spliterator and documents that it is ordered.
            return list1.stream().filter(list2::contains).collect(Collectors.toList());
        }
        else {
            List<X> result = new ArrayList<>(list1.size());
            for ( Iterator<X> iter = list1.iterator(); iter.hasNext(); ) {
                X elt = iter.next();
                if ( list2.contains(elt) )
                    result.add(elt);
            }
            return result;
        }
    }

    /** Binary search - not duplicates (not cardinality preserving) */
    public static <X> List<X> intersection_probe_binary_search_shortening(List<X> list1, List<X> list2, Comparator<X> comparator) {
        if ( list1.size() > list2.size() ) {
            List<X> tmp = list1;
            list1 = list2 ;
            list2 = tmp;
        }

        List<X> result = new ArrayList<>(list1.size());
        int j = 0 ; // Position in list2
        for ( Iterator<X> iter = list1.iterator(); iter.hasNext(); ) {
            X elt = iter.next();
            int j2 = binary_search_slice(list2, j, elt, comparator);
            if ( j2 >= 0 ) {
                j = j2+1;
                result.add(elt);
            }
        }
        return result;
    }

    private static <X> int binary_search_slice(List<X> list, int j, X elt, Comparator<X> comparator) {
        list = list.subList(j, list.size());
        return Collections.binarySearch(list, elt, comparator);
    }

    /** Find the intersection of two ordered lists, possibly with duplicates, any {@code List} implementation (array or linked list).
     * <p>
     * Algorithm: traverse both lists at the same time. (c.f. merge join on sorted inputs).
     * <p>
     * Result: Ordered list with element cardinality being the minimum cardinality of the element (multiset intersection).
     * <br/>
     * (1,2,2,3) and (1,1,2,2,2,5) => (1,2,2)
     * <p>
     * Complexity: O(N) time, O(1) temporary space
     * <p>
     * Assumes no nulls in the lists.
     * <p>
     * Costly for (1,1000) (1,2,3,4,5,6,7,....999) because it linearly walks list2 to find 1000
     * after the match at 1 but it works for lists that are not {@link RandomAccess}.
     *
     * @param <X>
     * @param list1
     * @param list2
     * @param comparator
     * @return The intersection
     */
    public static <X> List<X> intersection_bilinearsearch0(List<X> list1, List<X> list2, Comparator<X> comparator) {
        ListIterator<X> iter1 = list1.listIterator();
        ListIterator<X> iter2 = list2.listIterator();

        // Current elements.
        X elt1 = null;
        X elt2 = null;

        int len = Math.min(list1.size(), list2.size());

        // Worst case but it avoids any resizing alloc. Better as guess of len/2 ?
        List<X> result = new ArrayList<>(len);
        while(iter1.hasNext() && iter2.hasNext()) {
            // Move in lists
            if ( elt1 == null )
                elt1 = iter1.next();
            if ( elt2 == null )
                elt2 = iter2.next();

            int cmp = comparator.compare(elt1, elt2);
            if ( cmp < 0 ) {
                // elt1 < elt2 .
                elt1 = null;
            } else if ( cmp > 0 ) {
                // elt1 > elt2
                elt2 = null;
            } else {
                // elt1 == elt2
                result.add(elt1);
                elt1 = null;
                elt2 = null;
            }
        }
        return result;
    }

    /** See {@link #intersection_bilinearsearch0(List, List, Comparator)}.*/
    public static <X> List<X> intersection_bilinearsearch(List<X> list1, List<X> list2, Comparator<X> comparator) {

        /// Not true - works on LinkedList, alobeit not as well as a ArrayList.
//        if ( ! ( list1 instanceof RandomAccess ) || ! ( list2 instanceof RandomAccess ) ) {
//            throw new IllegalArgumentException("Need two RandomAccess lists");
//        }

        // Before the list.
        int idx1 = -1;
        int idx2 = -1;

        // Last elements processed.
        X prevElt1 = null;
        X prevElt2 = null;

        // Current elements.
        X currentElt1 = null;
        X currentElt2 = null;

        int len = Math.min(list1.size(), list2.size());

        // Worst case but it avoids resizing alloc. Better as guess of len/2 ?
        List<X> result = new ArrayList<>(len);
        for ( ; ; ) {
            if ( DEBUG ) {
                System.err.printf("Loop:  idx1=%s  idx2=%s  prevElt1=%s  prevElt1=%s  currentElt1=%s  currentElt2=%s  result=%s\n",
                    idx1, idx2, prevElt1, prevElt2, currentElt1, currentElt2, result);
                System.err.printf("Loop:  list1=%s\n", list1);
                System.err.printf("Loop:  list2=%s\n", list2);
            }

            // Move in lists
            if ( currentElt1 == null ) {
                // Init, currentElt1 < currentElt2 or match ( currentElt1 == currentElt2 )
                idx1 = move("list1", list1, idx1, prevElt2, comparator);
                if ( idx1 < 0 || idx1 >= list1.size() )
                    break;
                prevElt1 = currentElt1 = list1.get(idx1);
            }

            if ( currentElt2 == null ) {
                // Init, currentElt1 > currentElt2 or match
                idx2 = move("list2", list2, idx2, prevElt1, comparator);
                if ( idx2 < 0 || idx2 >= list2.size() )
                    break;
                prevElt2 = currentElt2 = list2.get(idx2);
            }

            // Process current
            int cmp = comparator.compare(currentElt1, currentElt2);
            if ( cmp < 0 ) {
                // elt1 < elt2 .
                currentElt1 = null;
            } else if ( cmp > 0 ) {
                // elt1 > elt2
                currentElt2 = null;
            } else {
                // elt1 == elt2
                result.add(currentElt1);
                currentElt1 = null;
                currentElt2 = null;
            }
        }
        return result;
    }

    /**
     * Find the index of the first value equal to or greater than {@code val}
     * in an ordered list, given the suggestion that index {@code start}
     * <p>
     * The algorithm is exponential probing, akin to binary search but modified for known starting state.
     * Only good for {@link RandomAccess} lists.
     * Returns an out of range index for no match.
     */
    private static <X> int move(String label, List<X> list, int start, X val, Comparator<X> comparator) {
        if ( !DEBUG )
            return move$(list, start, val, comparator);;
        System.err.println(label+ ":move: idx="+start+" val="+val);
        System.err.println(label+ ":move: "+list);
        int x = move$(list, start, val, comparator);
        System.err.println(label+ ":move: ==> "+x);
        return x;
    }

    private static <X> int move$(List<X> list, int start, X val, Comparator<X> comparator) {
        if ( start < 0 )
            // First element or out of range.
            return 0 ;

//        // Check that val is ahead in list.
//        if ( val != null ) {
//            X elt0 = list.get(start);
//            // elt < val
//            int x0 = comparator.compare(elt0, val);
//            if ( x0 > 0 )
//                throw new IllegalArgumentException("binaryProbe");
//        }

        if ( val == null ) {
            if ( list.isEmpty() )
                return -1;
            return start+1;
        }

        // -- Advance

        // Linear policy.
        //if ( false )
        {
        for( int idx = start+1 ; idx < list.size(); idx++ ) {
            X elt = list.get(idx);
            int x = comparator.compare(elt, val);
            if ( x >= 0 )
                return idx;
        }
        return list.size();
        }

//        // XXX
//
//        // Binary search - two sided - no guarantee of which element of duplicates is found.
//        // Maybe : pahse 1: linear scan on ezuality.
//
//        // linear on equality. Move at least one.
////        for( int idx = start+1 ; idx < list.size(); idx++ ) {
////            X elt = list.get(idx);
////            int x = comparator.compare(elt, val);
////            if ( x == 0 )
////                return idx;
////        }
//        // linear on equality. Move one.
//        X elt0 = list.get(start+1);
//        int x0 = comparator.compare(elt0, val);
//        if ( x0 == 0 )
//          return start+1;
//
//        // Different.
//
//        // Binary search. Finds random place in same value.
//        // So scan back to start (it's a new value).
//        int x = Collections.binarySearch(list, val, comparator);
//        if ( x < 0 )
//            // Not exact match, new value.
//            // "Insertion point"+1 - just after val.
//            return -x ;
//        // Found "val".
//        // Move backwards to start of "val" run
//        while( x > start ) {
//            int y = x-1;
//            X elt = list.get(y);
//            if ( ! elt.equals(val) )
//                return x;
//            x--;
//        }
//        return x+1;
    }

    /**
     * Ordered set - this code does not handle duplicates as a multiset.
     * It returns the cardinality in the longer list without distinct()
     *
     * @param <X>
     * @param list1
     * @param list2
     * @param comparator
     */
    public static <X> List<X> intersection_binarysearch_orderedSet(List<X> list1, List<X> list2, Comparator<X> comparator) {
        // list1 should be larger (if list1 is all unique, we have to try every list1 element, so better to  loop on list1)
        if ( list1.size() < list2.size() ) {
            List<X> tmp = list1;
            list1 = list2 ;
            list2 = tmp;
        }
        List<X> list2$ = list2;
        Predicate<X> test = x-> (Collections.binarySearch(list2$, x, comparator) >= 0);
        return list1.stream().filter(test)
                .distinct()
                .collect(Collectors.toList());
        // Collections.binarySearch does work on a linkedList but not as well as RandomAccess.
    }


    /** See {@link #intersection_bilinearsearch0(List, List, Comparator)}.*/
        public static <X> List<X> intersection_move_x(List<X> list1, List<X> list2, Comparator<X> comparator) {

            /// Not true - works on LinkedList, alobeit not as well as a ArrayList.
    //        if ( ! ( list1 instanceof RandomAccess ) || ! ( list2 instanceof RandomAccess ) ) {
    //            throw new IllegalArgumentException("Need two RandomAccess lists");
    //        }

            ListIterator<X> iter1 = list1.listIterator();
            ListIterator<X> iter2 = list2.listIterator();

            // Last elements processed.
            X prevElt1 = null;
            X prevElt2 = null;

            // Current elements.
            X currentElt1 = null;
            X currentElt2 = null;

            int len = Math.min(list1.size(), list2.size());

            // Worst case but it avoids resizing alloc. Better as guess of len/2 ?
            List<X> result = new ArrayList<>(len);
            for ( ; ; ) {
                // Move in lists
                if ( currentElt1 == null ) {
                    // Init, currentElt1 < currentElt2 or match ( currentElt1 == currentElt2 )
                    prevElt1 = currentElt1 = move_x(list1, iter1, prevElt2, comparator);
                    if ( prevElt1 == null  )
                        break;
                }

                if ( currentElt2 == null ) {
                    // Init, currentElt1 > currentElt2 or match
                    prevElt2 = currentElt2 = move_x(list2, iter2, prevElt1, comparator);
                    if ( prevElt2 == null  )
                        break;
                }

                // Process current
                int cmp = comparator.compare(currentElt1, currentElt2);
                if ( cmp < 0 ) {
                    // elt1 < elt2 .
                    currentElt1 = null;
                } else if ( cmp > 0 ) {
                    // elt1 > elt2
                    currentElt2 = null;
                } else {
                    // elt1 == elt2
                    result.add(currentElt1);
                    currentElt1 = null;
                    currentElt2 = null;
                }
            }
            return result;
        }

    private static <X> X move_x(List<X> list, ListIterator<X> iter, X val, Comparator<X> comparator) {
        // At least one place.
        if ( ! iter.hasNext() )
            return null ;
        iter.next();
        while(iter.hasNext()) {
            X elt = iter.next();
            int x = comparator.compare(elt, val);
            if ( x >= 0 )
                return elt;
        }
        return null;
    }

    // From Collections.iteratorBinarySearch
    private static <T> T get(ListIterator<? extends T> i, int index) {
        T obj = null;
        int pos = i.nextIndex();
        if (pos <= index) {
            do {
                obj = i.next();
            } while (pos++ < index);
        } else {
            do {
                obj = i.previous();
            } while (--pos > index);
        }
        return obj;
    }

    @Test public void inter01() {
        build().set1().set2(2,4,7).result().run();
    }

    @Test public void inter02() {
        build().set1(9).set2().result().run();
    }

    @Test public void inter03() {
        build().set1().set2().result().run();
    }

    @Test public void inter_card_01() {
        build().set1(1,1).set2(1,9,9,9).result(1)
            .run();
    }

    @Test public void inter_card_02() {
        build().set1(1,1).set2(1,9,9,9).result(1).run();
    }

    @Test public void inter11() {
            build().set1(1,2,3,5).set2(2,4,7).result(2).run();
    }

    @Test public void inter12() {
        build().set1(1,2,6,7).set2(3,6).result(6).run();
    }

    @Test public void inter13() {
        build().set1(1,2,6,7).set2(3,6,7,8).result(6,7).run();
    }

    @Test public void inter14() {
        build().set1(1,2,3,4).set2(5,6,7).result().run();
    }

    @Test public void inter15() {
        build().set1(1,2,2,3).set2(1,1,2,2,5).result(1,2,2).run();
    }

    @Test public void inter16() {
        build().set1(1,2,2,3).set2(1,1,2,2,5).result(1,2,2).run();
    }

    @Test public void inter17() {
        build().set1(1,2,3).set2(1,1,2,2,2,5).result(1,2).run();
    }


    static class TestBuilder {
        private List<Integer> list1 = new ArrayList<>();
        private List<Integer> list2 = new ArrayList<>();
        private List<Integer> results = new ArrayList<>();
        private boolean cardinalityPreserving = false;

        //private IntersectionCalc<Integer> function = Ha::intersection_bilinearsearch;
        //private IntersectionCalc<Integer> function = Ha::intersection_binarysearch_orderedSet;
        //private IntersectionCalc<Integer> function = Ha::intersection_move_x;
        private IntersectionCalc<Integer> function = ListIntersection::intersection_probe_binary_search_shortening;

        public TestBuilder set1(Integer... elts) {
            add(list1, elts);
            return this;
        }

//        public TestBuilder impl(IntersectionCalc<Integer> intersect) {
//            this.function = intersect;
//            return this;
//        }

        public TestBuilder cardinality(boolean cardinalityPreserving) {
            this.cardinalityPreserving = cardinalityPreserving;
            return this;
        }

        public void run() {
            Collection<Integer> expected = results;
            if ( ! cardinalityPreserving )
                expected = new HashSet<>(expected);

            Collection<Integer> r1 = function.intersection(list1, list2, Integer::compareTo);
            Collection<Integer> r2 = function.intersection(list2, list1, Integer::compareTo);
            if ( ! cardinalityPreserving ) {
                r1 = new HashSet<>(r1);
                r2 = new HashSet<>(r2);
            }

            assertEquals(expected, r1);
            assertEquals(expected, r2);
        }

        public TestBuilder set2(Integer... elts) {
            add(list2, elts);
            return this;
        }

        public TestBuilder result(Integer...elts) {
            add(results, elts);
            return this;
        }

        private static void add(List<Integer> list, Integer[] elts) {
            for (Integer x : elts)
                list.add(x);
        }
    }

    private TestBuilder build() {
        return new TestBuilder();
    }
}

