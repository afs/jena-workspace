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

package io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.TypedInputStream;
import org.eclipse.jetty.util.IO;

public class IOZ {
    /** Read a {@link TypedInputStream} into a string */
    public static String slurp(TypedInputStream typedInputStream) {
        String charsetStr = typedInputStream.getCharset() ;
        Charset cs = charsetFor(charsetStr) ;
        try ( Reader r = new InputStreamReader(typedInputStream, cs) ) {
            return readAll(r) ;
        } catch (IOException ex) { return null ; } // Does not happen - readAll deals with it.
    }
    
    private static final int BUFFER_SIZE = 128*1024 ; 
    
    /** read all (or as much as possible) of a reader */
    private static String readAll(Reader r) {
        StringWriter sw = new StringWriter(BUFFER_SIZE) ;
        try { 
            char buff[] = new char[BUFFER_SIZE];
            for (;;) {
                int l = r.read(buff);
                if (l < 0)
                    break;
                sw.write(buff, 0, l);
            }
            sw.close() ;
            //r.close() ;
        } catch (IOException ex) {
            Log.warn(IO.class, "faled to read all of the reader : "+ex.getMessage(), ex) ;
        }
        return sw.toString();
    }

    /** Convert a string name for a charset into a {@link Charset}.
     *  Default to UTF-8.
     */ 
    private static Charset charsetFor(String charsetStr) {
        if ( charsetStr == null )
            return StandardCharsets.UTF_8 ;
        // Use a build-in if possible.
        Charset cs = tryForCharset(charsetStr, StandardCharsets.UTF_8) ;
        if ( cs == null )
            cs = tryForCharset(charsetStr, StandardCharsets.ISO_8859_1) ;
        if ( cs == null )
            cs = tryForCharset(charsetStr, StandardCharsets.US_ASCII) ;
        if ( cs == null ) {
            try {
                cs = Charset.forName(charsetStr) ;
            } catch (IllegalCharsetNameException ex) {
                Log.warn(IO.class, "No such charset: "+ex.getCharsetName()+ " : defaulting to UTF-8") ;
                cs = StandardCharsets.UTF_8 ;
            }
        }
        return cs ;
    }

    private static Charset tryForCharset(String ct, Charset cs) {
        if ( ct.equalsIgnoreCase(cs.name()) )
            return cs ;
        return null ;
    }

}
