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

package bcd;

// Mode dense:
// https://en.wikipedia.org/wiki/Chen%E2%80%93Ho_encoding
// https://en.wikipedia.org/wiki/Densely_packed_decimal
// Ten values in 4 bits (16 symbols)
// 8 bytes = 64/4 = 16 digits, no scale.
// whereas
// TDB2 : 7 bytes = 7*8/4 = 14 digits, no scale.

// TDB2 : DecimalNode56 : binary
// signed 8 bits of scale, signed 48 bits of value.
// Decimal precision is 47 bits (it's signed) or around 14 places.

public class BCD {
    static byte[] bcdZero = new byte[0];

    // ToDo.
//    private static boolean IncludeSign = true;
//    private static final byte Positive = 0xC;
//    private static final byte Negative = 0xD;
    private static boolean HiLo = true;

    public static byte[] encodeBCD(long value) {
        return HiLo ? encodeHiLo(value) : encodeLoHi(value) ;
    }

    public static long decodeBCD(byte[] value) {
        return HiLo ? decodeHiLo(value) : decodeLoHi(value) ;
    }

    // Byte layout. HiLo
    // byte[0] is high 2 digits hilo. Print as hex, the digits are in English-order
    private static byte[] encodeHiLo(long value) {
        boolean negative = (value < 0);
        if ( negative )
            value = -value;

        int digits = 0;
        if ( value == 0 )
            return bcdZero;
        else {
            long v = value;
            while (v > 0) {
                v = v / 10;
                digits++;
            }
        }
        // Encode value 0?
        int numBytes = (digits % 2 == 0) ? digits / 2 : digits / 2 + 1;

        byte[] array = new byte[numBytes];

        long v = value;
        int j = 0;

        for ( int i = numBytes - 1 ; i >= 0 ; i-- ) {
            long z1 = v % 10;
            v = v / 10;
            long z2 = v % 10;
            v = v / 10;
            byte x = (byte)(z2 << 4 | z1);
            array[i] = x;
        }
        return array;
    }

    private static long decodeHiLo(byte[] bytes) {
        long value = 0;
        for ( int i = 0 ; i < bytes.length ; i++ ) {
            int v = bytes[i];
            int lo = v & 0xF;
            int hi = (v >> 4) & 0xF;
            value = 100 * value + (lo + 10 * hi);
        }
        return value;
    }

    // --bytes lo-hi and hi-lo within a byte.

    // Byte layout. byte[0] is low 2 digits, and the byte is hi-lo.
    private static byte[] encodeLoHi(long value) {
        boolean negative = (value < 0);
        if ( negative )
            value = -value;

        int digits = 0;
        if ( value == 0 )
            return bcdZero;
        else {
            long v = value;
            while (v > 0) {
                v = v / 10;
                digits++;
            }
        }
        // Encode value 0?
        int numBytes = (digits % 2 == 0) ? digits / 2 : digits / 2 + 1;

        byte[] array = new byte[numBytes];

        long v = value;
        int j = 0;
        for ( int i = 0 ; i < numBytes ; i++ ) {
            long z1 = v % 10;
            v = v / 10;
            long z2 = v % 10;
            v = v / 10;
            byte x = (byte)(z2 << 4 | z1);
            array[i] = x;
        }
        return array;
    }

    private static long decodeLoHi(byte[] bytes) {
        long value = 0;
        for ( int i = bytes.length - 1 ; i >= 0 ; i-- ) {
            int v = bytes[i];
            int lo = v & 0xF;
            int hi = (v >> 4) & 0xF;
            value = 100 * value + (lo + 10 * hi);
        }
        return value;
    }

    private static String stringLoHi(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for ( int i = bytes.length - 1 ; i >= 0 ; i-- ) {
            int v = bytes[i];
            int lo = v & 0xF;
            int hi = (v >> 4) & 0xF;
            sb.append(Integer.toString(hi));
            sb.append(Integer.toString(lo));
        }
        return sb.toString();
    }
}
