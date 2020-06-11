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

package archive.jena_1746_tdb2_cache;

import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;

public class Dev1746 {

    public static void main(String...a) {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        System.out.println("--READ/end--");
        dsg.begin(TxnType.READ);
        dsg.end();
        System.out.println("--READ/commit--");
        dsg.begin(TxnType.READ);
        dsg.commit();
        System.out.println("--READ/abort--");
        dsg.begin(TxnType.READ);
        dsg.abort();

        System.out.println();

        System.out.println("--WRITE/commit--");
        dsg.begin(TxnType.WRITE);
        dsg.commit();
        System.out.println("--WRITE/abort--");
        dsg.begin(TxnType.WRITE);
        dsg.abort();

        System.out.println("--Promote/commit--");
        dsg.begin(TxnType.READ_PROMOTE);
        dsg.promote();
        dsg.commit();


        System.out.println("--DONE--");

        System.exit(0);
    }
}
