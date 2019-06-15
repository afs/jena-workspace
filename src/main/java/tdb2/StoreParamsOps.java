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

package tdb2;

import static java.util.Arrays.stream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.tdb2.params.StoreParams;

public class StoreParamsOps {

    public static void main(String ...args) {
        StoreParams params = StoreParams.getDftStoreParams();
        
        params.getPrimaryIndexTriples();
        params.getTripleIndexes();

        params.getPrimaryIndexQuads();
        params.getQuadIndexes();
        
        tripleIndexes = stream(params.getTripleIndexes())
            .map(StoreParamsOps::canonical)
            .collect(Collectors.toSet());
        quadIndexes = stream(params.getQuadIndexes())
            .map(StoreParamsOps::canonical)
            .collect(Collectors.toSet());
        
        for ( String idxName : params.getTripleIndexes() ) {
            idxName = canonical(idxName);
            tripleIndexes.add(idxName);
        }
        
        }

    static Set<String> tripleIndexes = new HashSet<>();
    static Set<String> quadIndexes = new HashSet<>();
    
    static String canonical(String idxName) { return idxName.toUpperCase(Locale.ROOT) ; }
    
    static boolean isIndex(String idxName) {
        return isIndexTriples(idxName) || isIndexQuads(idxName);
    }

    private static boolean isIndexTriples(String idxName) {
        return tripleIndexes.contains(canonical(idxName));
    }
    
    private static boolean isIndexQuads(String idxName) {
        return quadIndexes.contains(canonical(idxName));
    }

    private static <X> Set<X> toSet(Stream<X> stream) {
        return stream.collect(Collectors.toSet());
    }
    
}
