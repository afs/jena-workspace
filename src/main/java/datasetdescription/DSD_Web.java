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

package datasetdescription;

import org.apache.jena.sparql.core.DatasetDescription ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.DatasetGraphReadOnly ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.DatasetUtils ;
import org.apache.jena.system.Txn ;

/** Create a read-only {@link DatasetGraph} that is formed from the 
 *  graphs of the {@link DatasetDescription} read from the web. 
 */
public class DSD_Web implements DatasetDescriptionProcessor {

    @Override
    public DatasetGraph process(DatasetDescription description, DatasetGraph _dsg, Context context) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;
        Txn.executeWrite(dsg, ()->{
            DatasetUtils.addInGraphs(dsg, 
                                     description.getDefaultGraphURIs(),
                                     description.getNamedGraphURIs(),
                                     null) ;
        }) ;
        DatasetGraph dsg2 = new DatasetGraphReadOnly(dsg) ;
        return dsg2 ;
    }
}
