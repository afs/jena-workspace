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

import org.apache.jena.fuseki.cmd.FusekiCmd;
import org.apache.jena.fuseki.system.FusekiLogging;

public class Jena2055
{
    // ?? check all operations - what is happened with ActionErrorException?
    // [x] Query - raw but fails to close iterator?
    // [x] Update - needs fix - needs abort.
    // [ ] GSP_R - needs fix - needs abort.
    // [ ] GSP_RW - needs fix - needs abort.
    // [ ] Upload
    // [ ] Non-transactional permissions dataset.


    public static void main(String ... a) throws Exception {
        FusekiLogging.setLogging();
        String BASE = "/home/afs/tmp" ;
        //String BASE = "/home/afs/Desktop/JENA-1302";

        // For the UI files.
        String fusekiHome = "/home/afs/ASF/afs-jena/jena-fuseki2/jena-fuseki-webapp" ;
        String fusekiBase = "/home/afs/tmp/run" ;

        System.setProperty("FUSEKI_HOME", fusekiHome) ;
        System.setProperty("FUSEKI_BASE", fusekiBase) ;
        try {
            //FusekiMainCmd.main("--conf=/home/afs/Desktop/X2055/localData.ttl");
            FusekiCmd.main("--conf=/home/afs/Desktop/X2055/localData.ttl");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
