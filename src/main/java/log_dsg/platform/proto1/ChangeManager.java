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
import java.net.InetSocketAddress ;
import java.net.ServerSocket ;
import java.net.Socket ;
import java.nio.channels.SelectionKey ;
import java.nio.channels.Selector ;
import java.nio.channels.ServerSocketChannel ;
import java.nio.channels.SocketChannel ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.Set ;
import java.util.concurrent.atomic.AtomicInteger ;
import java.util.function.BiConsumer ;

import log_dsg.StreamChangesReader ;
import log_dsg.StreamChangesWriter ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class ChangeManager {
static { LogCtl.setLog4j(); }
    
    private static Logger log = LoggerFactory.getLogger("HTTP") ; 

    static final String BASEPATTERN = "patch-%04d" ;
    static AtomicInteger counter = new AtomicInteger(0) ;

    static final int PORT_RECV = 7707 ;
    static final int PORT_FETCH = 7708 ;
    
    public static void main(String ...args) throws IOException {
        Selector selector = Selector.open();
        
        int[] ports = {PORT_RECV, PORT_FETCH};

        for (int port : ports) {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            server.register(selector, SelectionKey.OP_ACCEPT); 
        }
        
        while (selector.isOpen()) {
            selector.select();
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            for ( SelectionKey key : selector.selectedKeys() ) {
               if (key.isAcceptable()) {
                   ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                   SocketChannel socketChannel = serverSocketChannel.accept();
                   Socket socket = socketChannel.socket();
                   int port = socket.getLocalPort() ;
                   BiConsumer<InputStream, OutputStream> action ;
                   switch(port) {
                       case PORT_RECV: action = actionPatchReceiver() ; break ; 
                       case PORT_FETCH: action = actionPatchFetcher() ; break ;
                       default: action = null ;
                   }
                   InputStream in = socket.getInputStream() ;
                   OutputStream out = socket.getOutputStream() ;
                   Thread thread = new Thread(()->action.accept(in, out)) ;
                   thread.start();
               }
            }
        }
    }
    
    public static void main0(String ...args) {

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
    
    private static void forkCollecor(Socket clientSocket) {
        try {
            InputStream in = clientSocket.getInputStream() ;
            OutputStream out = clientSocket.getOutputStream() ;
            BiConsumer<InputStream, OutputStream> r = actionPatchReceiver() ;
            Thread thread = new Thread(()->r.accept(in, out)) ;
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
    
    static byte[] endMarker = Bytes.string2bytes(".\n") ;
    
    private static BiConsumer<InputStream, OutputStream> actionPatchFetcher() {
        return (InputStream in, OutputStream out) -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(in)) ; 
            try {
                for (;;) {
                    String s = input.readLine() ;
                    if ( s == null || s.isEmpty() )
                        break ;
                    int idx = Integer.parseInt(s) ;
                    String fn = String.format(BASEPATTERN, idx) ;
                    System.out.println("Request for "+fn) ;
                    InputStream data = IO.openFile(fn) ;
                    byte[] buffer = new byte[1024] ; 
                    
                    for (;;) {
                        int len = data.read(buffer) ;
                        if ( len < 0 )
                            break ;
                        out.write(buffer, 0, len);
                    }
                    out.write(endMarker) ;
                    out.flush() ;
                }
                out.close() ;
                in.close() ;
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } 
        } ;
    }

}