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

package tdb2;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import tdb2.xloader.CmdxLoader;

public class TestXLoader {
    private static String RUN_DIR = "target/xloader";
    private static String DATA_DIR = "testing/Data";

    @BeforeClass public static void beforeClass() {
        FileOps.ensureDir(RUN_DIR);
        FileOps.clearAll(RUN_DIR);
    }

    @AfterClass public static void afterClass() {
        FileOps.clearAll(RUN_DIR);
    }

    // Testing arguments.

    @Test public void xloader_1() {
        runTest("--loc="+RUN_DIR+"/DB2-1", DATA_DIR+"/data-xloader.ttl");
    }

    @Test public void xloader_2() {
        runTest("--loc="+RUN_DIR+"/DB2-2", "--threads=1", DATA_DIR+"/data-xloader.ttl");
    }

    @Test public void xloader_3() {
        runTest("--loc="+RUN_DIR+"/DB2-3", "--tmpdir="+RUN_DIR, DATA_DIR+"/data-xloader.ttl");
    }


    private void runTest(String...args) {
        // CmdxLoader is an all-java wrapper for TDB2 xloader.
        // It is slower - seems exiting the JVM and starting fresh is better (and
        // can vary the max heap size).
        silentAll(()->CmdxLoader.main(args));
    }

    private static String[] loggersXloader = {
//        BulkLoaderX.LOG_Data.getName(),
//        BulkLoaderX.LOG_Nodes.getName(),
//        BulkLoaderX.LOG_Terms.getName(),
//        BulkLoaderX.LOG_Index.getName()
    };

    private static void silentAll(Runnable action) {
        silent("OFF", loggersXloader, action);
    }

    private static void silent(String runLevel, String[] loggers, Runnable action) {
        Map<String, String> levels = new HashMap<>();
        for ( String logger : loggers ) {
            levels.put(logger, LogCtl.getLevel(logger));
            LogCtl.setLevel(logger, runLevel);
        }
        try {
            action.run();
        } finally {
            levels.forEach(LogCtl::setLevel);
        }
    }

}
