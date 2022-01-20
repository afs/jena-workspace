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

package dsg.union;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/*package*/ class DSG_ImplLib {
    /*package*/ static boolean isWildcard(Node g) {
        return g == null || g == Node.ANY;
    }


    protected static void unsupportedMethod(Object object, String method) {
        throw new UnsupportedOperationException(Lib.className(object)+"."+method) ;
    }

    protected static Iterator<Quad> findAny(TraitDSG_Find dsg,Node s, Node p, Node o) {
        // Default graph
        Iterator<Quad> iter1 = dsg.findInDftGraph(s, p, o);
        if ( ! iter1.hasNext() )
            iter1 = null;
        Iterator<Quad> iter2 = dsg.findInAnyNamedGraphs(s, p, o);
        if ( ! iter2.hasNext() )
            iter2 = null;
        // Copes with null in either or both positions.
        return Iter.append(iter1, iter2);
    }



}
