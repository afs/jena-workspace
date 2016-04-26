/*
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

package txn_promote;

import java.util.concurrent.Semaphore ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.TDBFactory ;
import txn_promote.txn.ThreadTxn ;
import txn_promote.txn.Txn ;

public class DevTxnPromote {
    static { LogCtl.setCmdLogging(); } 

    public static void main(String... args) throws Exception {
        dsgTxn() ;
    }
    
    public static void dsgTxn() throws Exception {
        DatasetGraph dsg = TDBFactory.createDatasetGraph() ;
        dsg.getDefaultGraph().getPrefixMapping().setNsPrefix("", "http://example/") ;
        Quad q1 = SSE.parseQuad("(_ :s :p1 1)") ;
        Quad q2 = SSE.parseQuad("(_ :s :p2 2)") ;
        Quad q3 = SSE.parseQuad("(_ :s :p3 3)") ;

        // Start reader.
        ThreadTxn tt = Txn.threadTxnRead(dsg, ()->{
            long x = Iter.count(dsg.find()) ;
            if ( x != 0 ) 
                throw new RuntimeException() ;
        }) ;
        
        // Start R->W here
        dsg.begin(ReadWrite.READ); 
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.commit();
        dsg.end() ;
        tt.run(); 
        System.out.println("DONE") ;
    }
    
    public static void dsgTxn1() throws Exception {
        DatasetGraph dsg = TDBFactory.createDatasetGraph() ;
        dsg.getDefaultGraph().getPrefixMapping().setNsPrefix("", "http://example/") ;
        Quad q1 = SSE.parseQuad("(_ :s :p1 1)") ;
        Quad q2 = SSE.parseQuad("(_ :s :p2 2)") ;
        Quad q3 = SSE.parseQuad("(_ :s :p3 3)") ;
        
        Semaphore sema = new Semaphore(0) ;
        Thread t = new Thread(()->{
            sema.release();
            System.err.println("Enter W2") ;
            Txn.executeWrite(dsg, ()->{ System.err.println("Elsewhere"); dsg.add(q3); }) ;   
            sema.release();
        }) ;
        
        dsg.begin(ReadWrite.READ); 
        System.err.println("READ") ;
        dsg.add(q1) ;
        System.err.println("added q1") ;
        //RDFDataMgr.write(System.err, dsg, Lang.TRIG) ;

        t.start(); 
        sema.acquireUninterruptibly();

        dsg.add(q2) ;
        System.err.println("added q2") ;
        dsg.commit();
        dsg.end() ;
        
        sema.acquireUninterruptibly();
        
        dsg.begin(ReadWrite.READ); 
        RDFDataMgr.write(System.err, dsg, Lang.TRIG) ;
        dsg.end() ;
        System.out.println("DONE") ;
    }
    
}