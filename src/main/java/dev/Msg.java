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

package dev;

import java.io.PrintStream ;

public class Msg {
    private static final boolean SILENT = true ;
    // Or logger.
    private static PrintStream out = System.out ;
    private static String marker = "** ** " ;
    
    public static void msg(String msg) {
        if ( SILENT )
            return ;
        out.print(marker);
        out.print(msg);
        if ( ! msg.endsWith("\n") )
            out.println();
    }
    
    public static void msg(String who, String fmt, Object...args) {
        if ( SILENT )
            return ;
        out.print(marker);
        out.printf("%-20s ", who);
        if ( fmt == null ) {
            out.println();
            return ;
        }
        if ( args.length == 0 )
            out.print(fmt) ;
        else {
            try { out.printf(fmt, args) ; }
            catch (java.util.IllegalFormatException ex) {
                out.print("ERR: "+fmt) ;
            }
        }
        if ( ! fmt.endsWith("\n") )
            out.println();
    }
}
