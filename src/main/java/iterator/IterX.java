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

package iterator;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

// Extracts from the development to Jena-1414.
public class IterX
{
    private static final int CMP_GREATER = 1;
    private static final int CMP_EQUAL   = 0;
    private static final int CMP_LESS    = -1;
    private static final int CMP_UNKNOWN = -2;
    // compare (dst,src) : One src is "small", other side unknown size.
    private static final int CMP_SRC_SMALL = -3;

//    // Iter.step
//    public static int step(Iterator<?> iter, int steps) {
//        for ( int i = 0 ; i < steps; i++) {
//            if ( ! iter.hasNext() )
//                return i;
//            iter.next();
//        }
//        return steps;
//    }

    /** Compare the size of a graph to {@code size}, without calling Graph.size
     *  by iterating on {@code graph.find()} as necessary.
     *  <p>
     *  Return -1 , 0, 1 for the comparison.
     */
    /*package*/ static int compareSizeTo(Graph graph, int size) {
        ExtendedIterator<Triple> it = graph.find();
        try {
            int stepsTake = Iter.step(it, size);
            if ( stepsTake < size )
                // Iterator ran out.
                return CMP_LESS;
            if ( !it.hasNext())
                // Finished at the same timne.
                return CMP_EQUAL;
            // Still more to go
            return CMP_GREATER;
        } finally {
            it.close();
        }
    }

    /** Compare graph sizes (size arg2 : size arg1) by co-iteration.
     */
    /*package*/ static int compareSizeByFind(Graph dstGraph, Graph srcGraph, int minSize, int ratio, int maxCompare) {
        if ( dstGraph == srcGraph )
            return CMP_EQUAL;

        // Phase 1:
        //    iteration on srcGraph if less than MIN_SIZE
        // Phase 2:
        //    step dst the scaled number of steps
        //    compare by scaled length

        ExtendedIterator<Triple> srcIter = srcGraph.find();
        ExtendedIterator<Triple> dstIter = dstGraph.find();
        try {
            if ( minSize >= 0 ) {

                //  |find(src)| <= MIN_SRC_SIZE
                int x1 = Iter.step(srcIter, minSize);
                if ( ! srcIter.hasNext() )
                    return CMP_SRC_SMALL;

                // Now move the dstIter the same amount, in proportion to the ratio.
                int x2 = Iter.step(dstIter, ratio*minSize);
                if ( x2 < x1 )
                    // dst was short after all.
                    return CMP_LESS;
                maxCompare -= minSize;
            }
            int compareLimit = ( minSize < 0 ) ? maxCompare : maxCompare-minSize;

            // Test remainder of the iterators.
            //   |find(src)| * DST_SRC_RATIO  <=  |find(dst)|
            // Step srcIter by "ratio" steps each time.
            return compareByLength(dstIter, srcIter, 1, ratio, compareLimit);
        } finally {
            dstIter.close();
            srcIter.close();
        }
    }

//    /** Compare the length of two iterators.
//     *  <b>This operation is destructive on the iterators<b>.
//     */
//    /*package*/ static int compareByLength(Iterator<?> iter1, Iterator<?> iter2) {
//        return compareByLength(iter1, iter2, 1, 1, Integer.MAX_VALUE);
//    }

    /** Compare the length of two iterators.
     * By using the "scale" arguments, the caller can compare ratios of sizes.
     * <p>
     * <b>This operation is destructive on the iterators<b>.
     */
    /*package*/ static int compareByLength(Iterator<?> iter1, Iterator<?> iter2, int scale1, int scale2, int maxSteps) {
        if ( scale1 <= 0 )
            throw new IllegalArgumentException("step size 'step1' is not positive: "+scale1);
        if ( scale2 <= 0 )
            throw new IllegalArgumentException("step size 'step2' is not positive: "+scale2);

        int x = 0;
        int scaledSize1 = 0 ;
        int scaledSize2 = 0 ;
        // At nearly equal (the length are within one ratio unit), the stepping algothm isn't precise.
        // Exact for ratio = 1.
        for ( ;; ) {
            x++ ;
            boolean ended1 = ! iter1.hasNext();
            boolean ended2 = ! iter2.hasNext();

            //System.out.printf("x=%d scaledSize1=%d scaledSize2=%d  ended1=%s ended2=%s\n", x, scaledSize1, scaledSize2, ended1, ended2);

            if ( ended1 )
                return ended2 ? CMP_EQUAL : CMP_LESS ;
            if ( ended2 )
                return CMP_GREATER;
            if ( x > maxSteps )
                return CMP_UNKNOWN;
            // Yes, opposite scales : iter1 moves scale2 steps.
            int x1 = Iter.step(iter1, scale2);
            int x2 = Iter.step(iter2, scale1);
            scaledSize1 += x1;
            scaledSize2 += x2;
        }
    }
}
