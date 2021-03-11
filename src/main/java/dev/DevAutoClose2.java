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

package dev;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class DevAutoClose2 {
    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    // == ExtendedIterator.forEach.
    public static <T> void forEach( ExtendedIterator<T> iter, Consumer<T> action ) {
        try {
            while(iter.hasNext())
                action.accept(iter.next());
        } finally { iter.close(); }
    }

    public static <T, X> X iterRtn( ExtendedIterator<T> iter,
                                    Function<ExtendedIterator<T>, X> action ) {
        try {
            return action.apply(iter);
        } finally { iter.close(); }
    }


    public static void main(String...a) {
        ExtendedIterator<String> iter = WrappedIterator.create(Arrays.asList("A", "B", "C").iterator());
        //forEach(iter, System.out::println);
        //iter.forEach(System.out::println);

//        // ----
//        Graph graph = null;
//        Node s = null;
//        Node p = null;
//        Node o = G.getOneSP(graph, s, p);

        // ----

        String x = Iter.first(iter, s->s.equals("B"));
        System.out.println(x);
    }
}
