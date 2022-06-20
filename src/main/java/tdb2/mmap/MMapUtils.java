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

package tdb2.mmap;

import java.io.IOException;

public class MMapUtils {
    /*
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-misc</artifactId>
    <version>...</version>
</dependency>

 and locally compiled
 NativePosixUtil.cpp

/usr/java/packages/lib:/usr/lib/x86_64-linux-gnu/jni:/lib/x86_64-linux-gnu:/usr/lib/x86_64-linux-gnu:/usr/lib/jni:/lib:/usr/lib

     */

    public static void main(String... args) throws IOException {
        try {
//            int RANDOM = org.apache.lucene.store.NativePosixUtil.RANDOM;
//            org.apache.lucene.store.NativePosixUtil.madvise(null, RANDOM);
        } catch (java.lang.UnsatisfiedLinkError ule) {
            System.err.println(ule.getMessage());
        }
    }
}
