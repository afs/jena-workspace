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

package tdb2.tools;

import java.util.Iterator;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import tdb2.loader.base.ProgressMonitor2;

public class Tools {
    /** Copy a stream to several indexes (sequential version) */
    public static void copyIndex(Iterator<Tuple<NodeId>> srcIter, TupleIndex[] destIndexes, String label, ProgressMonitor2 monitor) {
        long counter = 0;
        for ( ; srcIter.hasNext() ; ) {
            counter++;
            Tuple<NodeId> tuple = srcIter.next();
            monitor.tick();
            for ( TupleIndex destIdx : destIndexes ) {
                if ( destIdx != null )
                    destIdx.add(tuple);
            }
        }
    }
}
