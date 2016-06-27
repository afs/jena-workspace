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

import log_dsg.StreamChangesApply ;
import log_dsg.StreamChangesReader ;
import log_dsg.Txn ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.tdb.TDBFactory ;

public class ClientFetcher {

    public static void main(String... args) throws IOException {
        Dataset ds = TDBFactory.createDataset() ;
        
        for ( int i = 1 ;i < 5 ; i++ ) {
            StreamChangesReader reader = LibPatchFetcher.fetch1(DP.FetchService, i) ;
            if ( reader == null )
                break ;
            System.out.println("Apply : "+i);
            StreamChangesApply sca = new StreamChangesApply(ds.asDatasetGraph()) ;
            reader.apply1(sca) ;
        }
        System.out.println("----");
        Txn.execRead(ds, ()->RDFDataMgr.write(System.out, ds, Lang.TRIG)) ;
        System.out.println("----");
        Txn.execRead(ds, ()->RDFDataMgr.write(System.out, ds, Lang.NQ)) ;
        System.out.println("----");
    }

}
