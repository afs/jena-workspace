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

package log_dsg.platform.proto1;

import java.io.* ;
import java.net.ServerSocket ;
import java.net.Socket ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.concurrent.atomic.AtomicInteger ;
import java.util.function.BiConsumer ;

import log_dsg.StreamChangesReader ;
import log_dsg.StreamChangesWriter ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class ChangeManager0 {
    
    static { LogCtl.setLog4j(); }
    
    private static Logger log = LoggerFactory.getLogger("Server") ; 

    static final String BASEPATTERN = "patch-%04d" ;
    static AtomicInteger counter = new AtomicInteger(0) ;

    public static void main(String ...args) {

        int port = 7707 ;
        log.info("Starting ...");

        try ( ServerSocket serverSocket = new ServerSocket(port) ) {
            for(;;) {
                Socket clientSocket = serverSocket.accept();
                log.info("Client "+clientSocket.getPort());
                forkReceiver(clientSocket) ;
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        log.info("... Finished");
    }

    private static void forkReceiver(Socket clientSocket) {
        try {
            InputStream in = clientSocket.getInputStream() ;
            BiConsumer<InputStream, OutputStream> r = actionPatchReceiver() ;
            Thread thread = new Thread(()->r.accept(in, null)) ;
            thread.start();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static BiConsumer<InputStream, OutputStream> actionPatchReceiver() {
        return (InputStream in, OutputStream out) -> {
            try {
                for (;;) {
                    StreamChangesReader scr = new StreamChangesReader(in) ;
                    if ( ! scr.hasMore() )
                        break ;
                    System.out.println("<<<<-----------------") ;
                    try ( OutputStream output = output() ) {
                        StreamChangesWriter scWriter = new StreamChangesWriter(output) ;
                        /*boolean b =*/ scr.apply1(scWriter) ;
                    } // close flushes.
                    System.out.println(">>>>-----------------") ;
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } 
        } ;
    }

    static private OutputStream output() {
        try {
            String s = String.format(BASEPATTERN, counter.incrementAndGet()) ;
            System.out.println("Patch = "+s) ;
            Path p = Paths.get(s) ;
            if ( Files.exists(p) ) 
                System.out.println("Overwriting file"); 
            OutputStream out = new FileOutputStream(s) ;
            out = new BufferedOutputStream(out) ;
            return out ;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            System.exit(1) ;
            return null ;
        }
    }
}