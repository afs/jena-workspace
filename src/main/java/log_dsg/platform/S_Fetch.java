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

import java.io.IOException ;
import java.io.OutputStream ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.riot.WebContent ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class S_Fetch extends ServletBase {
    static public Logger LOG = LoggerFactory.getLogger("Fetch") ;

    // Fetch a file
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String paramId = req.getParameter("id") ;
        int id = -999 ;
        try { id = Integer.parseInt(paramId) ; }
        catch (NumberFormatException ex) {
            resp.sendError(HttpSC.BAD_REQUEST_400, "Failed to parse the 'id' parameter" ) ;
            return ;
        }
        String filename = DPS.patchFilename(id) ;
        Path path = Paths.get(filename) ;

        LOG.info("patch = "+filename) ;
        resp.setContentType("application/rdf-patch+text"); 
        resp.setCharacterEncoding(WebContent.charsetUTF8);
        OutputStream out = resp.getOutputStream() ;

        if ( ! Files.exists(path) ) {
            resp.sendError(HttpSC.NOT_FOUND_404, "No such patch file: "+filename ) ;
            return ;
        }
            
        Files.copy(path, out) ;
        out.flush();
        resp.setStatus(HttpSC.OK_200);
    }
}
