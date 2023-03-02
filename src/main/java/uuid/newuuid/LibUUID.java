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

import java.util.Locale;
import java.util.UUID;

import org.apache.jena.atlas.lib.BitsLong;

/**
 * Library code for UUIDs.
 */
public class LibUUID {

    public static String str(UUID uuid) {
        return LibUUID.strUC(uuid);
    }

    /*package*/ static long setVersion(long mostSignificantBits, int val) {
        return BitsLong.pack(mostSignificantBits, val&0xF, 12, 16);
    }

    /*package*/ static long setVariant(long leastSignificantBits, int val) {
        return BitsLong.pack(leastSignificantBits, val&0x3, 62, 64);
    }

    /** Prefer {@link UUID#getVersion()} */
    /*package*/ static int getVersion(long mostSignificantBits, long leastSignificantBits) {
        return (int)BitsLong.unpack(mostSignificantBits, 12, 16);
    }

    /** Prefer {@link UUID#getVariant()} */
    /*package*/ static long getVariant(long leastSignificantBits, int val) {
        return (int)BitsLong.pack(leastSignificantBits, val&0x3, 62, 64);
    }

    // General variant decode with variable length variant of 1,2,3 bits
    /*
        Msb0  Msb1  Msb2  Description
        0     x     x    Reserved, NCS backward compatibility.
        1     0     x    The variant specified in this document.
        1     1     0    Reserved, Microsoft Corporation backward compatibility
        1     1     1    Reserved for future definition.
     */

    private int _getVariant(long mostSigBits, long leastSigBits) {
        // This could be sensitive to the variant encoding.
        // https://tools.ietf.org/html/rfc4122#section-4.1.1
        if ( true ) {
            // Do the expected case as a fast path.
            int x = (int)BitsLong.unpack(leastSigBits, 62, 64);
            if ( x == 2 )
                return x;
        }
        //Variable length decoding.
        int b0 = (int)BitsLong.unpack(leastSigBits, 63, 64);
        if ( b0 == 0 )
            // Bit pattern 0xx
            return 0;
        int b1 = (int)BitsLong.unpack(leastSigBits, 62, 63);
        if ( b1 == 0 )
            // Bit pattern 10x - the normal UUID variant.
            return 2;
        int b2 = (int)BitsLong.unpack(leastSigBits, 61, 62);
        if ( b2 == 0 )
            // 110
            return 0x4;
        else
            // Bit pattern 1111
            return 0x7;
    }

    /** As a string (uppercase) */
    static String strLC(UUID uuid) { return uuid.toString().toUpperCase(Locale.ROOT); }

    static String strUC(UUID uuid) {
        /** As a string (uppercase) */
        //Long.toUnsignedString(i, 16);
        long mostSignificantBits = uuid.getMostSignificantBits();
        long leastSignificantBits = uuid.getLeastSignificantBits();
        StringBuffer sb = new StringBuffer(36) ;
        toHex(sb, BitsLong.unpack(mostSignificantBits, 32, 64), 4) ;
        sb.append('-') ;
        toHex(sb, BitsLong.unpack(mostSignificantBits, 16, 32), 2) ;
        sb.append('-') ;
        toHex(sb, BitsLong.unpack(mostSignificantBits, 0, 16), 2) ;
        sb.append('-') ;
        toHex(sb, BitsLong.unpack(leastSignificantBits, 48, 64), 2) ;
        sb.append('-') ;
        toHex(sb, BitsLong.unpack(leastSignificantBits, 0, 48), 6) ;
        return sb.toString() ;
    }

    // ----------------------------------------------------
    // Worker functions

    /** Hex digits : upper case **/
    final public static char[] hexDigitsUC = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
        '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F' };

    /** Hex digits : lower case **/
    final public static char[] hexDigitsLC = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
        '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' };

    static void toHex(StringBuffer sBuff, long value, int lenBytes) {
        // Insert in high-low order, by nibble
        for (int i = 2 * lenBytes - 1; i >= 0; i--) {
            int shift = 4 * i ;
            int x = (int)(value >>> shift & 0xF) ;
            sBuff.append(hexDigitsUC[x]);
        }
    }

}
