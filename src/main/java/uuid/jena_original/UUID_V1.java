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

package uuid.jena_original;

import org.apache.jena.atlas.lib.BitsLong ;

/*
Version 1:
60 bits of time
48 bits of nodeId
12 bits of clock sequence
2  bits variant
4  bits version

   0                   1                   2                   3
   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                          time_low                             |   8 hex digits
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |       time_mid                |         time_hi_and_version   |   4-4
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |clk_seq_hi_res |  clk_seq_low  |         node (0-1)            |   4-
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                         node (2-5)                            |   12
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
*/

// https://tools.ietf.org/html/rfc4122
// http://www.opengroup.org/onlinepubs/009629399/apdxa.htm

/* java.util.UUID
0xFFFFFFFF00000000 time_low
0x00000000FFFF0000 time_mid
0x000000000000F000 version
0x0000000000000FFF time_hi

The least significant long consists of the following unsigned fields:

0xC000000000000000 variant
0x3FFF000000000000 clock_seq
0x0000FFFFFFFFFFFF node
*/

/**  * Version 1 Timebased UUIDs. */
public class UUID_V1 extends JenaUUID
{
    // Constants
    /*package*/ public static final int UUID_ImplVersion = 1 ;           // Version 1: time-based.

    /*package*/UUID_V1(long mostSigBits, long leastSigBits) {
        super(mostSigBits, leastSigBits);
    }

    // Accessors

    long getTimeHigh()  { return BitsLong.unpack(getMostSignificantBits(), 0,  12) ; } // Then 4 bits for the version.
    long getTimeMid()   { return BitsLong.unpack(getMostSignificantBits(), 16, 32) ; }
    long getTimeLow()   { return BitsLong.unpack(getMostSignificantBits(), 32, 64) ; }

    public long getTimestamp() {
        return getTimeLow() | getTimeMid()<<32 | getTimeHigh()<<48 ;
    }

    public long getClockSequence() {
        return BitsLong.unpack(getLeastSignificantBits(), 48, 62) ;
    }

    public long getNode() { return BitsLong.unpack(getLeastSignificantBits(), 0, 48) ; }

    @Override
    protected int getImplVersion() {
        return UUID_ImplVersion;
    }
}
