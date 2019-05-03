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

package fuseki;

public class Other {
    @FunctionalInterface
    interface Call2<T1, T2> {
        void call(T1 x1, T2 x2);
    }

    @FunctionalInterface
    interface Call3<T1, T2, T3> {
        void call(T1 x1, T2 x2, T3 x3);
    }

    @FunctionalInterface
    interface Call4<T1, T2, T3, T4> {
        void call(T1 x1, T2 x2, T3 x3, T4 x4);
    }

    @FunctionalInterface
    interface Function2<T1, T2, R> {
        R call(T1 x1, T2 x2);
    }

    @FunctionalInterface
    interface Function3<T1, T2, T3, R> {
        R call(T1 x1, T2 x2, T3 x3);
    }

    @FunctionalInterface
    interface Function4<T1, T2, T3, T4, R> {
        R call(T1 x1, T2 x2, T3 x3, T4 x4);
    }
}
