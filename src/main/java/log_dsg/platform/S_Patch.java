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

package log_dsg.platform;

import java.io.* ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.concurrent.atomic.AtomicInteger ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import log_dsg.OutputStream2 ;
import log_dsg.StreamChangesReader ;
import log_dsg.StreamChangesWriter ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Receive an incoming patch file and put on disk (safely : something else may try to read it while its being written. */ 
public class S_Patch extends ServletBase {
    
    // Push a file.
    static public Logger         LOG     = LoggerFactory.getLogger("Patch") ;
    static private AtomicInteger counter = new AtomicInteger(0) ;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // With or without parsing.
        // Need at least an end marker.
        
        // With, for robustness.
        // And to see end marker.
        
        InputStream in = req.getInputStream() ;
        for (;;) {
            StreamChangesReader scr = new StreamChangesReader(in) ;
            if ( ! scr.hasMore() )
                break ;
            String dst = DPS.nextPatchFilename() ;
            String s = DPS.tmpFilename() ;
            LOG.info("<<<<-----------------") ;
            LOG.info("# Patch = "+dst+"("+s+")") ;
            // read one.
            try ( OutputStream output = output(s) ) {
                // TODO Abrupt end?
                StreamChangesWriter scWriter = new StreamChangesWriter(output) ;
                boolean b = scr.apply1(scWriter) ;
            } // close flushes.
            move(s, dst) ;
            LOG.info(">>>>-----------------") ;
        }
        resp.setContentLength(0);
        resp.setStatus(HttpSC.NO_CONTENT_204) ;
    }
    
    // Must move a complete file into place
    private static void move(String src, String dst) throws IOException {
        //System.err.printf("move %s to %s\n", src, dst) ;
        Path pSrc = Paths.get(src) ;
        Path pDst = Paths.get(dst) ;
        try { Files.move(pSrc, pDst) ; }
        catch (IOException ex) {
            LOG.warn(String.format("IOException moving %s to %s", src, dst) , ex);
            throw ex ;
        }
    }

    static private OutputStream output(String s) throws FileNotFoundException {
        Path p = Paths.get(s) ;
        if ( Files.exists(p) ) 
            System.out.println("Overwriting file"); 
        
        OutputStream out = new FileOutputStream(s) ;
        out = new BufferedOutputStream(out) ;
        
        OutputStream out2 = new FilterOutputStream(System.out) { 
            @Override public void close() {}
        } ;
        
        return new OutputStream2(out2, out) ; 
    }
}
