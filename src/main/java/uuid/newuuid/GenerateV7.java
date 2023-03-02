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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import org.apache.jena.atlas.lib.BitsLong;
import org.apache.jena.atlas.lib.Bytes;

/**
 * Generate version 7 UUIDs. Instances of this class are not thread safe. Multiple
 * generators; they do not interfere with one
 * another. Applications may create one {@link GenerateV7} per thread.
 *
 * <pre>
     Version 7 layout:
     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                           unix_ts_ms                          |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |          unix_ts_ms           |  ver  |       rand_a          |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |var|                        rand_b                             |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                            rand_b                             |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 */
final class GenerateV7 implements UUIDGenerator {
    // Generate BLOCK UUIDs between resetting the timestamp.
    private static final int BLOCK = 100;
    private long millseconds = -1;
    private int allocCount = BLOCK;
    // A non-blocking RNG in the JDK: "NativePRNGNonBlocking"
    private final Random random;
    //private Random random = new SecureRandom();

    GenerateV7() {
        Random random0;
        try {
            random0 = SecureRandom.getInstance("NativePRNGNonBlocking");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Filed to find NativePRNGNonBlocking - using default SecureRandom");
            random0 = new SecureRandom();
        }
        random = random0;
    }

    private long getMilliseconds() {
        allocCount++;
        if ( allocCount >= BLOCK ) {
            millseconds = System.currentTimeMillis();
            allocCount = 0;
        }
        return millseconds;
    }

    @Override
    public UUID generate() {
        long unix_ts_ms = getMilliseconds();
        return constructUUID7(unix_ts_ms, random);
    }

    // http://www0.cs.ucl.ac.uk/staff/d.jones/GoodPracticeRNG.pdf
    // Other random number generators may be found in:
    // org.apache.commons.math3.random

    // The algorithm; inputs millisecond time and random number generator.
    static UUID constructUUID7(long unix_ts_ms, Random random) {
        long mostSignificantBits = 0;
        long leastSignificantBits = 0;

        byte[] bytes = new byte[2 + 8];
        random.nextBytes(bytes);
        long rand_a = bytes[0] << 8 | bytes[1]; // 16 bits, unsigned.
        long rand_b = Bytes.getLong(bytes, 2);  // When used, 2 bits are lost to the
                                                // variant.

        mostSignificantBits = BitsLong.pack(mostSignificantBits, unix_ts_ms, 16, 64);

        mostSignificantBits = BitsLong.pack(mostSignificantBits, rand_a, 0, 12);
        leastSignificantBits = BitsLong.pack(leastSignificantBits, rand_b, 0, 62);

        // UUID version and variant.
        mostSignificantBits = LibUUID.setVersion(mostSignificantBits, 7);
        leastSignificantBits = LibUUID.setVariant(leastSignificantBits, 2);

        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    /* Version 7 0 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9
     * 0 1 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
     * unix_ts_ms | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * | unix_ts_ms | ver | rand_a |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |var| rand_b
     * | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | rand_b |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ As longs:
     * mostSignificantBits: 64 down to 0 leastSignificantBits: 64 down to 0 */
/* B.2. Example of a UUIDv7 Value
 *
 * This example UUIDv7 test vector utilizes a well-known 32 bit Unix epoch with
 * additional millisecond precision to fill the first 48 bits
 *
 * rand_a and rand_b are filled with random data.
 *
 * The timestamp is Tuesday, February 22, 2022 2:22:22.00 PM GMT-05:00 represented as
 * 0x17F22E279B0 or 1645557742000
 *
 * ------------------------------- field bits value -------------------------------
 * unix_ts_ms 48 0x17F22E279B0 var 4 0x7 rand_a 12 0xCC3 var 2 b10 rand_b 62
 * 0x18C4DC0C0C07398F ------------------------------- total 128
 * ------------------------------- final: 017F22E2-79B0-7CC3-98C4-DC0C0C07398F
 *
 * Figure 12: UUIDv7 Example Test Vector */

}