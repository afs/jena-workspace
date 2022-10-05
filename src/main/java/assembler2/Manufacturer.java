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

package assembler2;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

// Creator (already used)
// Fabricator?
// Manufacturer?
// Originator?

public interface Manufacturer<X> {

    /**
     * Return an {@code X}.
     * <p>
     * If a {@code X} has been built before for this reference node, return the
     * previous object (i.e. it is shared in the network of constructed objects).
     * Otherwise build a fresh object and also record it for later.
     */
    public default X construct(BuildContext cxt, Graph descriptionGraph, Node reference) {
        return cxt.computeIfAbsent(reference, (d)->newItem(cxt, descriptionGraph, d));
    }

    /**
     *  Create a fresh item every call.
     */
    public X newItem(BuildContext cxt, Graph descriptionGraph, Node reference);
}
