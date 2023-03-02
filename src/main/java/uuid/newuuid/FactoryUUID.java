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

package uuid.newuuid;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import org.apache.jena.atlas.lib.BitsLong;

public class FactoryUUID {

    private static UUID nilUUID = new UUID(0, 0);
    private static UUID maxUUID = new UUID(-1L, -1L);

    public static UUID nil() { return nilUUID; }
    public static UUID max() { return maxUUID; }

    // RFC4122 : https://www.rfc-editor.org/rfc/rfc4122.html
    // Type 0 : unallocated
    // Type 1 : Gregorian time-based UUID from in [RFC4122], Section 4.1.3
    // Type 2 : DCE Security version, [RFC4122], Section 4.1.3
    // Type 3 : The name-based, MD5 hashing.
    // Type 4 : The randomly or pseudo-randomly generated version
    // Type 5 : The name-based, SHA-1 hashing.

    // New (2022) : https://datatracker.ietf.org/doc/html/draft-peabody-dispatch-new-uuid-format
    // Version and variant in the sameplace. Variant = 2.
    // Type 6 : Reordered Gregorian time-based UUID
    // Type 7 : Unix Epoch time-based UUID
    // Type 8 : Reserved for custom UUID formats

    /**
     * Compare two UUIDs as if 2 unsigned 128 bit numbers, which is lexical order.
     * {@code F0xx...} &gt; {@code 80xx...} &gt; {@code 70xx...}.
     * <p>
     * {@link java.util.UUID#compareTo} uses signed comparison of the most and least
     * significant bits. This gives an ordering but can be surprising.
     * <p>
     * In the example, the ordering would be reversed because the examples with
     * "F" and "8" are negative numbers.
     */
    public static int compare(UUID uuid1, UUID uuid2) {
        int x = Long.compareUnsigned(uuid1.getMostSignificantBits(), uuid2.getMostSignificantBits());
        if ( x != 0 )
            return x;
        return Long.compareUnsigned(uuid1.getLeastSignificantBits(), uuid2.getLeastSignificantBits());
    }
    /**
     * Bit layout.
     * <p>
     * The two longs of a UUID are the mostSignifcant 64 bits, which is the top of
     * the usual diagram, and the least significant 64 bits, the bottom of the usual
     * diagram.
     * <p>
     * Fro access, the long bits are reversed as used in {@link BitsLong} which is
     * value centric. In {@link BitsLong}, bit zero is the least significant bit (the
     * 0/1 value) and bit 63 the sign bit.
     * <p>
     * In UUIDs, we are not interested in the numeric values. We are treating the two
     * longs as a way to work with a 128 bits in collation order. See
     * {@link #compare(UUID, UUID)}.
     */

    // IF we want V1, take from project afs-lib
//    private static UUIDFactory factory1 = new UUID_V1_Gen();
//    public static UUID generateUUID1() { return factory1.generate().asUUID(); }

    public static UUID generateUUID4() { return UUID.randomUUID(); }

    /**
     * Return a fresh version 7 UUID.
     * This function is thread safe.
     */
    public static UUID generateUUID7() {
        return globalGeneratorUUID7().generate();
    }
    /**
     * Per-thread generator for version 7 UUIDs.
     * This is slightly more efficient than the global generator because it does
     * not get the millisecond time every call.
     * <p>
     * Not thread safe.
     * <p>
     * Separate instances of this class can be used by separate threads.
     */
    public static UUIDGenerator threadGeneratorUUID7() {
        return new GenerateV7();
    }

    private static UUIDGenerator globalGeneratorV7 = new UUIDGenerator() {
        @Override
        public UUID generate() {
            long milliseconds = System.currentTimeMillis();
            return GenerateV7.constructUUID7(milliseconds, random);
        }
    };

    /**
     * Return a thread-safe UUIDGenerator for type 7 UUIDs.
     * See also {@link #threadGeneratorUUID7} that may be more efficient but is not thread safe.
     */
    public static UUIDGenerator globalGeneratorUUID7() {
        return globalGeneratorV7;
    }

    private static Random random = new SecureRandom();
}
