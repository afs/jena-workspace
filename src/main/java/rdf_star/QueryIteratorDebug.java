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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.serializer.SerializationContext;

/** See also {@link QueryIteratorLogger} */
class QueryIteratorDebug extends QueryIteratorWrapper {
    private final String label;

    private final boolean output;

    public QueryIteratorDebug(QueryIterator qIter, String label, boolean output) {
        super(qIter);
        this.output = output;
        this.label = label;
    }

    private void print(String fmt, Object...args) {
        if ( ! output )
            return;
        String msg = String.format(fmt, args);
        System.out.println(label+": "+msg);
    }

    @Override
    protected boolean hasNextBinding() {
        //print("hasNextBinding()");
        boolean b = super.hasNextBinding();
        if ( !b )
            print("hasNextBinding() -> %s", b);
        return b;
    }

    @Override
    protected Binding moveToNextBinding() {
        //print("moveToNextBinding()");
        Binding binding =  super.moveToNextBinding();
        print("moveToNextBinding() -> %s", binding);
        return binding;
    }

    @Override
    protected void closeIterator() {
        print("closeIterator()");
        super.closeIterator();
        //print("closeIterator() ->");
    }

    @Override
    public void output(IndentedWriter out) {
        super.iterator.output(out);
    }

    // Transparent.
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        super.iterator.output(out, sCxt);
    }
}