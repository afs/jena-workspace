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

package a_techready;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.lib.BitsInt ;
import org.apache.jena.atlas.lib.Chars ;


// See JSWriter.outputQuotesString.

// This code here is the extracted JSON quoting mechanism.
// ideally put back into atlas.json
public class JSON2 {
    
    /** Escape a string according to the JSON quoting rules. 
     * This is a general purpose, out of context, quoting mechanism
     * and is not sensitive to JSON format.
     * "/" is escaped as "\/" if and only if it comes after '<' 
     */
    public static String escape(String string) {
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        escape(string, out) ;
        return out.asString() ;
    }
        
    /** Escape a string according to the JSON quoting rules. 
     * This is a general purpose, out of context, quoting mechanism
     * and is not sensitive to JSON format.
     * "/" is escaped as "\/" if and only if it comes after '<' 
     */
    public static void escape(String string, AWriter out) {   
        // Some overlap with JSWriter but different.
        int len = string.length() ;
        for (int i = 0; i < len; i++) {
            char ch = string.charAt(i) ;
            switch (ch) {
                case '"':
                case '\'':
                case '\\' :
                    esc(out, ch) ;
                case '/' :
                    // Avoid </ which confuses if it's in HTML (this is from json.org)
                    if ( i > 0 && string.charAt(i - 1) == '<' )
                        esc(out, ch) ;
                    else
                        out.print(ch);
                    break ;
                case '\b' :
                case '\f' :
                case '\n' :
                case '\r' :
                case '\t' :
                    esc(out, ch) ;
                    break ;
                default :
                    // Character.isISOControl(ch) ; //00-1F, 7F-9F
                    // This is more than Character.isISOControl
                    if ( ch < ' ' || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch < '\u2100') ) {
                        out.write("\\u") ;
                        int x = ch ;
                        x = oneHex(out, x, 3) ;
                        x = oneHex(out, x, 2) ;
                        x = oneHex(out, x, 1) ;
                        x = oneHex(out, x, 0) ;
                        break ;
                    }

                    out.write(ch) ;
                    break ;
            }
        }
    }
    
    private static void esc(AWriter out, char ch) {
        out.print('\\') ;
        out.print(ch) ;
    }
    
    private static int oneHex(AWriter out, int x, int i) {
        int y = BitsInt.unpack(x, 4*i, 4*i+4) ;
        char charHex = Chars.hexDigitsUC[y] ;
        out.write(charHex) ;
        return BitsInt.clear(x, 4 * i, 4 * i + 4) ;
    }
}
