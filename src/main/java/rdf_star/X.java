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

package rdf_star;

import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

public class X {

    /*package*/ static final boolean DEBUG = false;

    static int counter = 0 ;

    static QueryIterator debug(QueryIterator qIter, String label) {
        label = "["+(++counter)+"] "+label;
        return new QueryIteratorDebug(qIter, label, X.DEBUG);
    }

    static QueryIterator print(QueryIterator qIter, String label, ExecutionContext execCxt) {
        List<Binding> x = Iter.toList(qIter);
        if ( DEBUG ) {
            System.out.println(label);
            x.forEach(b->System.out.println("  "+b));
        }
        qIter = new QueryIterPlainWrapper(x.iterator(), execCxt);
        return qIter ;
    }
}
