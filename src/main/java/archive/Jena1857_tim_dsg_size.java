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

package archive;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;

public class Jena1857_tim_dsg_size {

    public static void main(String ... a) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        System.out.println(dsg.size());

        Quad q1 = SSE.parseQuad("(:g :s :p :o1)");
        Quad q2 = SSE.parseQuad("(:g :s :p :o2)");
        dsg.add(q1);
        dsg.add(q2);
        System.out.println(dsg.size());
    }
}
