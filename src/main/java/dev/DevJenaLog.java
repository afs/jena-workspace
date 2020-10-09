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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.stream.JenaIOEnvironment;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileManagerImpl;


public class DevJenaLog {

    // [1] log4j2.properties changes
    // [1.1] filter.threshold.level = ALL
    // [1.2] %c to -15.
    // [] Desktop/LOGGING

    // [2] StreamManager logging
    // [2.1] StreamManager multiple map.
    //   Map to test; map per locator.

    // RDFParser.openTypedInputStream

    static {
        //System.setProperty("log4j.configurationFile", "file:log4j2.properties");
        LogCtl.setLog4j2();
        LogCtl.setLevel(FileManager.class, "debug");
        LogCtl.setLevel(org.apache.jena.riot.system.stream.LocationMapper.class, "debug");

        LogCtl.setLevel(JenaIOEnvironment.class, "debug");

        LogCtl.setLevel(FileManagerImpl.class, "debug");
        LogCtl.setLevel(StreamManager.class, "debug");

        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {
        System.err.println("read model");
        Model model = ModelFactory.createDefaultModel();
        model.read("/home/afs/tmp/D.ttl");

//        RDFDataMgr.loadGraph("/home/afs/tmp/D.ttl");

        System.out.println("DONE");
    }
}
