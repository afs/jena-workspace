package txn_promote.txn;

import static org.junit.Assert.assertEquals ;

import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestTxnPromote {
    
    private static Logger logger = Logger.getLogger(SystemTDB.errlog.getName()) ;
    private static Level  level  = logger.getLevel() ;
    
    @BeforeClass static public void beforeClass() {
        logger.setLevel(Level.ERROR) ;
    }
    
    @AfterClass static public void afterClass() {
        logger.setLevel(level); 
    }
    
    private static Quad q1 = SSE.parseQuad("(_ :s :p1 1)") ;
    private static Quad q2 = SSE.parseQuad("(_ :s :p2 2)") ;
    private static Quad q3 = SSE.parseQuad("(_ :s :p3 3)") ;
    
    protected DatasetGraph create() { return TDBFactory.createDatasetGraph() ; } 
    
    protected static void assertCount(long expected, DatasetGraph dsg) {
        dsg.begin(ReadWrite.READ);
        long x = Iter.count(dsg.find()) ;
        dsg.end() ;
        assertEquals(expected, x) ;
    }
    
    @Test public void promote_01() {
        DatasetGraph dsg = create() ;
        dsg.begin(ReadWrite.READ); 
        dsg.add(q1) ;
        dsg.commit();
        dsg.end() ;
    }
    
    @Test public void promote_02() {
        DatasetGraph dsg = create() ;
        dsg.begin(ReadWrite.READ); 
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.commit();
        dsg.end() ;
        assertCount(2, dsg) ;
    }

    @Test public void promote_03() {
        DatasetGraph dsg = create() ;
        dsg.begin(ReadWrite.READ); 
        dsg.add(q1) ;
        // bad - forced abort.
        dsg.end() ;
        assertCount(0, dsg) ;
    }
    
    @Test public void promote_04() {
        DatasetGraph dsg = create() ;
        AtomicInteger a = new AtomicInteger(0) ;
        
        Semaphore sema = new Semaphore(0) ;
        Thread t = new Thread(()->{
            sema.release();
            Txn.executeWrite(dsg, ()->dsg.add(q3)) ;   
            sema.release();
        }) ;
        
        dsg.begin(ReadWrite.READ);
        // Promote
        dsg.add(q1) ;
        t.start(); 
        // First release.
        sema.acquireUninterruptibly();
        // Thread blocked. 
        dsg.add(q2) ;
        dsg.commit();
        dsg.end() ;
        
        // Until thread exits.
        sema.acquireUninterruptibly();
        assertCount(3, dsg) ;
    }
    
    @Test public void promote_05() {
        DatasetGraph dsg = create() ;
        // Start long running reader.
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
    }
}
