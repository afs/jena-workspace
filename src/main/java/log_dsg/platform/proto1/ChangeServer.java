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

import java.io.IOException ;
import java.io.InputStream ;
import java.net.ServerSocket ;
import java.net.Socket ;

import log_dsg.* ;
import log_dsg.platform.DPS ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.slf4j.Logger ;

public class ChangeServer {
    static { LogCtl.setLog4j(); }
    
    private static Logger log = DPS.LOG ; 
    
    public static void main(String ...args) {
        int port = 7707 ;
        Dataset ds = DatasetFactory.createTxnMem() ; 
        StreamChanges sc = new StreamChangesApply(ds.asDatasetGraph()) ;
        if ( true )
            sc = new StreamChanges2(sc, new StreamChangesWriter(System.out)) ;
        log.info("Starting ...");
        
        
        try ( ServerSocket serverSocket = new ServerSocket(port) ) {
            Socket clientSocket = serverSocket.accept();
            log.info("Client "+clientSocket.getPort());
            InputStream in = clientSocket.getInputStream() ;
            //clientSocket.getOutputStream() ;
            for (;;) {
                StreamChangesReader scr = new StreamChangesReader(in) ;
                boolean ongoing = scr.apply1(sc) ;
                if ( ! ongoing )
                    break ;
                System.out.println("<<<<-----------------") ;
                Txn.execRead(ds, ()-> RDFDataMgr.write(System.out, ds, Lang.TRIG)) ;
                System.out.println(">>>>-----------------") ;
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        log.info("... Finished");
    }
}
