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

/** A class to create and recreate UUIDs.
 * http://www.opengroup.org/onlinepubs/009629399/apdxa.htm
 */

package uuid.jena_original ;

import java.util.Locale ;

import org.apache.jena.atlas.lib.BitsLong ;
import org.slf4j.LoggerFactory ;

// TO DO
// + Comments and renaming.
// ? Move to/from string code here (string <=> pair of longs).
//   OK but unparse code makes explicit what goes where in the structures
//   parse/unparseV4 is the generic code.

// UUID and factory

/**
    <a ref="https://tools.ietf.org/html/rfc4122">RFC 4122</a> UUID.
*/
public abstract class JenaUUID
{
    // Variants: we only support RFC 4122 whic is variant 2.
    // The code also assumes the variant is stored in 2 bits.
    //static final int Var_NCS      = 0 ;
    static final int Var_Std      = 2 ;     // Two bits
    //static final int Var_DCE      = 2 ;   // Same as above
    //static final int Var_MS_GUID  = 6 ;   // Reserved. 3 bits
    //static final int Var_Reserved = 7 ;   // Reserved for future definition. 3 bits.
    private final long mostSignificantBits;
    private final long leastSignificantBits;

    public int getVersion() { return _getVersion(mostSignificantBits, leastSignificantBits); }

    private int _getVersion(long mostSigBits, long leastSigBits) {
        return (int)BitsLong.unpack(mostSigBits, 12, 16);
    }

    public int getVariant() {return _getVariant(mostSignificantBits, leastSignificantBits); }

    private int _getVariant(long mostSigBits, long leastSigBits) {
        // This could be sensitive to the variant encoding.
        // https://tools.ietf.org/html/rfc4122#page-6
//        if ( true )
//            return (int)BitsLong.unpack(leastSigBits, 62, 64);
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


    public long getMostSignificantBits() { return mostSignificantBits; }

    public long getLeastSignificantBits() { return leastSignificantBits; }

    protected JenaUUID(long mostSigBits, long leastSigBits) {
        checkArgs(mostSigBits, leastSigBits);
        this.mostSignificantBits = mostSigBits;
        this.leastSignificantBits = leastSigBits;
    }

    private void checkArgs(long mostSigBits, long leastSigBits) {
        int _variant = _getVariant(mostSigBits, leastSigBits);
        int _version = _getVersion(mostSigBits, leastSigBits);

        if ( _variant == 0 && _version == 0 ) {
            if ( mostSigBits == 0 && leastSigBits == 0 )
                return;
            String msg = String.format("Version = 0 : Expected: most and least significant to be 0: Got: %016x %016x",
                                       mostSigBits, leastSigBits);
            throw new IllegalArgumentException(msg);
        }

        if ( _variant == getImplVariant() && _version == getImplVersion() )
            return;
        String msg = String.format("Version/variant error: Expected: (%d,%d); Got (%d,%d)",
                                   getImplVersion(), getImplVariant(), _version, _variant);
        throw new IllegalArgumentException(msg);
    }

    protected abstract int getImplVersion();
    protected int getImplVariant() { return  JenaUUID.Var_Std ; }

    /** Format as a string - no URI scheme **/
    public String asString() {
        return toString() ;
    }

    /** Format as a URI - that is uuid:ABCD */
    public String asURI() {
        return "uuid:" + toString() ;
    }

    /** Format as a URN - that is urn:uuid:ABCD */
    public String asURN() {
        return "urn:uuid:" + toString() ;
    }

    /** Return a {@link java.util.UUID} for this Jena-generated UUID */
    public java.util.UUID asUUID() {
        return new java.util.UUID(getMostSignificantBits(), getLeastSignificantBits()) ;
    }

    @Override
    public String toString() {
        return toString(this) ;
    }

    // Time low - which includes the incremental count.
    @Override
    public int hashCode() { return (int) BitsLong.unpack(getMostSignificantBits(), 32, 64) ; }

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( other == null ) return false ;
        if ( ! ( other instanceof JenaUUID ) )
            return false ;
        JenaUUID x = (JenaUUID)other ;
        return this.getMostSignificantBits() == x.getMostSignificantBits() &&
               this.getLeastSignificantBits() == x.getLeastSignificantBits() ;
    }

    // ----------------------------------------------------
    // Factory

    static UUIDFactory factory = new UUID_V1_Gen() ;

    public static void setFactory(UUIDFactory factory) {
        JenaUUID.factory = factory ;
    }

    public static UUIDFactory getFactory() {
        return factory ;
    }

    /** Create a UUID */
    public static JenaUUID generate() {
        return factory.generate() ;
    }

    public static void reset() {
        factory.reset() ;
    }

    /** The nil UUID */
    public static JenaUUID nil() {
        return UUID_nil.getNil() ;
    }

    public static String strNil() {
        return UUID_nil.getNilString() ;
    }

    public boolean isNil() {
        return this.equals(nil()) ;
    } // Or this == UUID_nil.nil because it's a singleton.

    /** Recreate a UUID from string */
    public static JenaUUID parse(String s) {
        if ( s.equals(strNil()) )
            return nil() ;

        // Canonical: this works in conjunction with .equals
        s = s.toLowerCase(Locale.ENGLISH) ;

        if ( s.startsWith("urn:") )
            s = s.substring(4) ;
        if ( s.startsWith("uuid:") )
            s = s.substring(5) ;

        if ( s.length() != 36 )
            throw new UUIDFormatException("UUID string is not 36 chars long: it's " + s.length() + " [" + s + "]") ;

        if ( s.charAt(8) != '-' || s.charAt(13) != '-' || s.charAt(18) != '-' || s.charAt(23) != '-' )
            throw new UUIDFormatException("String does not have dashes in the right places: " + s) ;

        // The UUID broken up into parts.
        //       00000000-0000-0000-0000-000000000000
        //       ^        ^    ^    ^    ^
        // Byte: 0        4    6    8    10
        // Char: 0        9    14   19   24  including hyphens
        int x = (int)BitsLong.unpack(s, 19, 23) ;
        int variant = (x >>> 14) ;
        int version = (int)BitsLong.unpack(s, 14, 15) ;

        if ( variant == Var_Std ) {
            switch (version) {
                case UUID_V1.UUID_ImplVersion:
                    return UUID_V1_Gen.parse$(s) ;
                case UUID_V4.UUID_ImplVersion:
                    return UUID_V4_Gen.parse$(s) ;
            }
            LoggerFactory.getLogger(JenaUUID.class).warn(s + " : Unsupported version: " + version) ;
            throw new UnsupportedOperationException("String specifies unsupported UUID version: " + version) ;
        }
        throw new UnsupportedOperationException("String specifies unsupported UUID variant: " + variant) ;
    }

    public static String toString(JenaUUID uuid) {
        return toString(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()) ;
    }

    /** Format using two longs - assumed valid for an UUID of some kind */
    public static String toString(long mostSignificantBits, long leastSignificantBits) {
        StringBuffer sb = new StringBuffer(36) ;
        JenaUUID.toHex(sb, BitsLong.unpack(mostSignificantBits, 32, 64), 4) ;
        sb.append('-') ;
        JenaUUID.toHex(sb, BitsLong.unpack(mostSignificantBits, 16, 32), 2) ;
        sb.append('-') ;
        JenaUUID.toHex(sb, BitsLong.unpack(mostSignificantBits, 0, 16), 2) ;
        sb.append('-') ;
        JenaUUID.toHex(sb, BitsLong.unpack(leastSignificantBits, 48, 64), 2) ;
        sb.append('-') ;
        JenaUUID.toHex(sb, BitsLong.unpack(leastSignificantBits, 0, 48), 6) ;
        return sb.toString() ;
    }

    // ----------------------------------------------------
    // Worker functions

    static void toHex(StringBuffer sBuff, long value, int lenBytes) {
        // Insert in high-low order, by nibble
        for (int i = 2 * lenBytes - 1; i >= 0; i--) {
            int shift = 4 * i ;
            int x = (int)(value >>> shift & 0xF) ;
            sBuff.append(Character.forDigit(x, 16)) ;
        }
    }

    static public class UUIDFormatException extends RuntimeException
    {
        public UUIDFormatException() {
            super() ;
        }

        public UUIDFormatException(String msg) {
            super(msg) ;
        }
    }

}
