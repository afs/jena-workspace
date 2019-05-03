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

package fuseki_txn;

public class DevTransactions {
    // http://people.apache.org/~sallen/sparql11-transaction/
    // http://docs.rdf4j.org/rest-api/#_starting_transactions
    /*
Operation   HTTP Method     Endpoint            Description
Create      POST            /transaction        Create a transaction
Status      GET             /transaction/txid   Get the status of a transaction
Commit      PUT             /transaction/txid   Commit a transaction
Rollback    DELETE          /transaction/txid   Rollback a transaction 
     */
    
    // A transaction is (represented by) a JSON or RDF document. 
    //    { "status": "commit" ,
    //      "started":
    //      ...
    //    }
    // Changes are a patch:
    
    // POST changes (PATCH)
    /*
     * Commit :  POST /transaction/txid  { "status": "commit" }    
     * Abort  :  { "status": "abort" }
     */
}
