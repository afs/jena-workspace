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

package tuple;

import static java.lang.String.format;

import java.util.ArrayList ;
import java.util.Arrays;
import java.util.Collections ;
import java.util.List;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.atlas.lib.StrUtils;

/**
 * General descriptor of a reordering (mapping) of slots in tuples
 * <p>
 * Naming: map is convert to the reordered form, unmap is get back.
 * <p>
 * 
 * <pre>
 * map(tuple) is equivalent to
 *   create tuple(getSlotIdx(0) , getSlotIdx(1), ... getSlotIdx(n-1)) ;    
 * </pre>
 * 
 * A {@code TupleMap} holds twp maps: the "getTransform" and the "putTransform".
 * The "getTransform" is here to get the item from in the mapper Tuple.
 * In the case is {@code SPO->POS} this is 
 * {@code 0<-1, 1<-2, 2<-0} 
 * and the "putTransform" is where to place the items: {@code 0->2, 1->0, 2->1}.
 */
final
public class TupleMap {
    /*
     * Naming.  getTransform (from src), putTransform(into dst)
     * And these are mutual inverses: unmap process is to swap use of getTransform and putTransform
     * See getSlotIdx and putSlotIdx
     * 
     * These are then equivalent
     * 
     * int j = getTransform[i] ; elts[i] = src.get(j) ;
     * int j = putTransform[i] ; elts[j] = src.get(i) ;
     * 
     * The code tends to use this style (getTransform)
     *     int j = getTransform[i] ;
     *     dst[i] = src[j] ;
     * 
     * See apply and applyArray
     * 
     * Warning : map and unmap here do not correspond to fetch and map in
     * ColumnMap. That has confusing/inconsistent usage.
     */
    
    // SPO->POS: get:{0<-1, 1<-2, 2<-0} put:{0->2, 1->0, 2->1}
    
    // Map by where to fetch from source.
    // For SPO -> POS, get from 1 to go into 0 so (0->, 1->0 2->   
    // POS->SPO, is (0->1, 1->2, 2->0)
    // i.e. the location to fetch the mapped element from.
    private final int[]  getTransform ;
    
    // Map by insertion into destination.
    // So SPO->POS is (0->2, 1->0, 2->1)
    // i.e. the location of the element after mapping.
    private final int[]  putTransform ; // putTransform, insertOrder

    private final int len ;
    private final String label;

    /**
     * Construct a mapping that maps the input (one col, one char) to the output
     */
    public static TupleMap create(String input, String output) {
        return new TupleMap(input + "->" + output, compileMapping(input, output));
    }

    /**
     * Construct a mapping, with label, that maps the input (one col, one char) to the output
     */
    public static TupleMap create(String label, String input, String output) {
        return new TupleMap(label, compileMapping(input, output));
    }

    /**
     * Construct a mapping that maps the input to the output
     */
    public static <T> TupleMap create(String label, List<T> input, List<T> output) {
        return new TupleMap(label, compileMapping(input, output));
    }

    /**
     * Construct a mapping that maps the input to the output
     */
    public static <T> TupleMap create(String label, T[] input, T[] output) {
        return new TupleMap(label, compileMapping(input, output));
    }
    
    /**
     * Construct a mapping - the elements are the mappings of a tuple
     * originally in the order 0,1,2,... so SPO->POS is 2,0,1 (SPO->POS so S->2,
     * P->0, O->1) and not 1,2,0 (which is the extraction mapping). The label is
     * just a label and is not interpretted here.
     */
    private TupleMap(String label, int... elements) {
        this.len = elements.length ; 
        this.label = label;

        this.putTransform = new int[elements.length];
        Arrays.fill(putTransform, -1);

        this.getTransform = new int[elements.length];
        Arrays.fill(getTransform, -1);

        for ( int i = 0 ; i < elements.length ; i++ ) {
            int x = elements[i];
            if ( x < 0 || x >= elements.length )
                throw new IllegalArgumentException("Out of range: " + x);
            // Checking
            if ( putTransform[i] != -1 || getTransform[x] != -1 )
                throw new IllegalArgumentException("Inconsistent: " + ListUtils.str(elements));

            putTransform[i] = x;    // The elements are the putTransform.
            getTransform[x] = i;
        }
    }

    /** Length of mapping */
    public int length() {
        return len;
    }

    /** 
     * Get the index of the i'th slot as it appears from a mapping : for
     * SPO->POS : 0'th slot is P so 0 returns 1 (the location in the tuple before mapping)
     * The 0'th mapped slot is {@code tuple.get(tupleMap.getSlotIdx(0))}.
     */
    public int getSlotIdx(int idx) {
        return getTransform[idx];
    }

    /**
     * Get the index of the i'th slot as it appears after unmapping : SPO->POS :
     * 0'th slot is S from POS so 0 returns 2
     */
    public int putSlotIdx(int idx) {
        return putTransform[idx]; 
    }
    
    /** 
     * Get the index of the i'th slot as it appears from a mapping : for
     * SPO->POS : 0'th slot is P so 0 returns 1 (the location in the tuple before mapping)
     * Equivalent to {@link #getSlotIdx}.
     * The 0'th mapped slot is {@code tuple.get(tupleMap.mapIdx(0))}.
     */
    public int mapIdx(int idx) {
        return getSlotIdx(idx) ;
    }

    /**
     * Get the index of the i'th slot as it appears after unmapping : SPO->POS :
     * 0'th slot is S from POS so 0 returns 2
     * Equivalent to {@link #putSlotIdx}.
     * The 0'th unmapped slot is {@code tuple.get(tupleMap.unmapIdx(0))}.
     */
    public int unmapIdx(int idx) {
        return putSlotIdx(idx) ; 
    }
    
    /** Apply to an <em>unmapped</em> tuple to get a tuple with the tuple mapping applied.
     */
    public <T> Tuple<T> map(Tuple<T> src) {
        return apply(src, getTransform) ;
    }

    /** Apply to a <em>mapped</em> tuple to get a tuple with the tuple mapping reverse-applied. */
    public <T> Tuple<T> unmap(Tuple<T> src) {
        return apply(src, putTransform) ;
    }

    // Does not work (java8) - assigning the return causes a runtime case exception 
//    /** Apply to an <em>unmapped</em> tuple to get a tuple with the tuple mapping applied */
//    public <T> T[] map(T[] src) {
//        @SuppressWarnings("unchecked")
//        T[]dst = (T[])new Object[src.length] ;
//        applyArray(src, dst, getTransform) ;
//        return dst ;
//    }

    /** Apply to an <em>unmapped</em> tuple to get a tuple with the tuple mapping applied.
     * Returns the destination array.
     */
    public <T> T[] map(T[] src, T[] dst) {
        if ( src == dst )
            throw new IllegalArgumentException("Source and destination are the same array") ;
        applyArray(src, dst, getTransform) ;
        return dst ;
    }

    // Does not work (java8) - assigning the return causes a runtime case exception 
//    /** Apply to a <em>mapped</em> tuple to get a tuple with the tuple mapping reverse-applied */
//    public <T> T[] unmap(T[] src) {
//        @SuppressWarnings("unchecked")
//        T[]dst = (T[])new Object[src.length] ;
//        applyArray(src, dst, putTransform) ;
//        return dst ;
//    }

    /** Apply to a <em>mapped</em> tuple to get a tuple with the tuple mapping reverse-applied.
     * Returns the destination array.
     */
    public <T> T[] unmap(T[] src, T[] dst) {
        if ( src == dst )
            throw new IllegalArgumentException("Source and destination are the same array") ;
        applyArray(src, dst, putTransform) ;
        return dst ;
    }

    /** Apply an index transformation
     */
    private static <T> Tuple<T> apply(Tuple<T> src, int[] getTransform) {
        if ( src.len() != getTransform.length )
            throw new IllegalArgumentException("Lengths do not match: Tuple:"+src.len()+"; transform:"+getTransform.length) ;
        // Fast-track 1,2,3,4 ?
//        // All this to avoid the temp array.
//        switch(src.len()) {
//            case 0: return src ;
//            case 1: return src ;
//            case 2: {
//                T x1 = src.get(getTransform[0]);
//                T x2 = src.get(getTransform[1]);
//                return TupleFactory.create2(x1, x2) ;
//            }
//            case 3: {
//                T x1 = src.get(getTransform[0]);
//                T x2 = src.get(getTransform[1]);
//                T x3 = src.get(getTransform[2]);
//                return TupleFactory.create3(x1, x2, x3) ;
//            }
//            case 4: {
//                T x1 = src.get(getTransform[0]);
//                T x2 = src.get(getTransform[1]);
//                T x3 = src.get(getTransform[2]);
//                T x4 = src.get(getTransform[3]);
//                return TupleFactory.create4(x1, x2, x3, x4) ;
//            }
//        }
        
        @SuppressWarnings("unchecked")
        T[] elts = (T[])new Object[src.len()] ;
        
        for ( int i = 0 ; i < src.len() ; i++ ) {
            int j = getTransform[i] ;
            elts[i] = src.get(j) ;
        }
        return TupleFactory.create(elts) ;
    }
    
    /** Apply an index transformation */
    private <T> void applyArray(T[] src, T[] dst, int[] transform) {
        for ( int i = 0 ; i < src.length ; i++ ) {
            int j = transform[i] ;
            dst[i] = src[j] ;
        }
    }
    
    /**
     * Apply to an <em>unmapped</em> tuple to get the i'th slot after mapping :
     * SPO->POS : 0'th slot is P from SPO
     */
    public <T> T mapSlot(int idx, Tuple<T> tuple) {
        checkLength(tuple) ;
        idx = getSlotIdx(idx) ;
        return tuple.get(idx) ;
    }

    /**
     * Apply to a <em>mapped</em> tuple to get the i'th slot as it appears after
     * mapping : SPO->POS : 0'th slot is S from POS
     */
    public <T> T unmapSlot(int idx, Tuple<T> tuple) {
        checkLength(tuple) ;
        idx = putSlotIdx(idx) ;
        return tuple.get(idx);
    }

    /**
     * Apply to an <em>unmapped</em> tuple to get the i'th slot after mapping :
     * SPO->POS : 0'th slot is P from SPO
     */
    public <T> T mapSlot(int idx, T[] tuple) {
        return tuple[getSlotIdx(idx)] ;
    }

    /**
     * Apply to a <em>mapped</em> tuple to get the i'th slot as it appears after
     * mapping : SPO->POS : 0'th slot is S from POS
     */
    public <T> T unmapSlot(int idx, T[] tuple) {
        return tuple[putSlotIdx(idx)] ;
    }

    /** Compile a mapping encoded as single charcaters e.g. "SPO", "POS" */
    private static int[] compileMapping(String domain, String range) {
        List<Character> input = StrUtils.toCharList(domain);
        List<Character> output = StrUtils.toCharList(range);
        return compileMapping(input, output);
    }

    /**
     * Compile a mapping, encoded two list, the domain and range of the mapping
     * function
     */
    private static <T> int[] compileMapping(T[] domain, T[] range) {
        return compileMapping(Arrays.asList(domain), Arrays.asList(range));
    }

    /** Compile a mapping */
    private static <T> int[] compileMapping(List<T> domain, List<T> range) {
        if ( domain.size() != range.size() )
            throw new AtlasException("Bad mapping: lengths not the same: " + domain + " -> " + range);

        int[] cols = new int[domain.size()];
        boolean[] mapped = new boolean[domain.size()];
        // Arrays.fill(mapped, false) ;

        for ( int i = 0 ; i < domain.size() ; i++ ) {
            T input = domain.get(i);
            int j = range.indexOf(input);
            if ( j < 0 )
                throw new AtlasException("Bad mapping: missing mapping: " + domain + " -> " + range);
            if ( mapped[j] )
                throw new AtlasException("Bad mapping: duplicate: " + domain + " -> " + range);
            cols[i] = j;
            mapped[j] = true;
        }
        return cols;
    }

    /** Access to the getTransform */
    /*package-testing*/ List<Integer> transformGet() {
        return arrayToList(getTransform) ;
    }

    /** Access to the putTransform */
    /*package-testing*/ List<Integer> transformPut() {
        return arrayToList(putTransform) ;
    }

    private List<Integer> arrayToList(int[] array) {
        List<Integer> list = new ArrayList<>(array.length) ;
        for ( int x : array ) 
            list.add(x) ;
        return  Collections.unmodifiableList(list) ;
    }

    @Override
    public String toString() {
        // return label ;
        return format("%s:%s:%s", label, mapStr(putTransform, "->"), mapStr(getTransform, "<-"));
    }

    private Object mapStr(int[] map, String arrow) {
        StringBuilder buff = new StringBuilder();
        String sep = "{";

        for ( int i = 0 ; i < map.length ; i++ ) {
            buff.append(sep);
            sep = ", ";
            buff.append(format("%d%s%d", i, arrow, map[i]));
        }
        buff.append("}");

        return buff.toString();
    }

    public String getLabel() {
        return label;
    }

    private static boolean CHECKING = true ; 
    private final void checkLength(Tuple<?> tuple) {
        if ( CHECKING ) {
            if ( tuple.len() != length() )
                throw new IllegalArgumentException("Tuple length "+tuple.len()+": not of length "+length()) ;
        }
    }
}
