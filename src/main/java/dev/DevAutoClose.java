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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.iterator.ExtendedIterator;

public class DevAutoClose {
    static {
        JenaSystem.init();
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    interface AutoCloseableExtendedIterator<X> extends AutoCloseable, ExtendedIterator<X> {
        @Override
        void close();
    }

    static AutoCloseableExtendedIterator<Triple> foo2() { return new AutoCloseableExtendedIterator<Triple>() {
        @Override public Triple removeNext() { return null; }
        @Override public <X extends Triple> ExtendedIterator<Triple> andThen(Iterator<X> other) { return null; }

        @Override
        public ExtendedIterator<Triple> filterKeep(Predicate<Triple> f) {
            return null;
        }

        @Override
        public ExtendedIterator<Triple> filterDrop(Predicate<Triple> f) {
            return null;
        }

        @Override
        public <U> ExtendedIterator<U> mapWith(Function<Triple, U> map1) {
            return null;
        }

        @Override
        public List<Triple> toList() {
            return null;
        }

        @Override
        public Set<Triple> toSet() {
            return null;
        }

        @Override
        public void close() {}

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Triple next() {
            return null;
        }
    };}


    static ExtendedIterator<Triple> foo() { return null; }

    public static void main(String...a) {
        ExtendedIterator<Triple> iter = foo();
        try {
        } finally { iter.close(); }
    }
}
