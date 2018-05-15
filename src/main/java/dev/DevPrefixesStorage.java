package dev;

import java.util.Set;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb.store.DatasetPrefixesTDB;
import org.apache.jena.tdb.sys.TDBInternal;
import org.apache.jena.tdb2.DatabaseMgr;

public class DevPrefixesStorage {

    static { LogCtl.setLog4j(); }
    
    // ** Signal to GraphView across txn boundaries.
    
    public static void main(String[] args) {
        JenaSystem.init();
        Node gn1 = NodeFactory.createURI("http://example/gn1");
        Node gn2 = NodeFactory.createURI("http://example/gn2");
        boolean useTDB1 = true ;
        String DIR = "DB" ;
        FileOps.ensureDir(DIR);
        FileOps.clearDirectory(DIR);
        Triple t = SSE.parseTriple("(:s :p :o)");
        Quad q = SSE.parseQuad("(:gx :sx :px :ox)");
        
        final DatasetGraph dsg = useTDB1
            ? TDBFactory.createDatasetGraph(DIR)
            : DatabaseMgr.connectDatasetGraph(DIR);
            

        // Error occurs on first setNsPrefix after a txn block when prefix mapping got, not used.
        Txn.executeWrite(dsg, ()->System.out.println("Txn 0"));
        Graph g = dsg.getGraph(gn1);
        System.out.println(Lib.className(g));
        // If R txn, Read-only object file in "Txn 2"
        // GraphPrefixesProjection holds a NodeTupleTable Reference.
        Txn.executeWrite(dsg, ()->{
            System.out.println("Txn 1");
            Graph gz = g;//Factory.createDefaultGraph();
            
            // OK
            //Graph gz = dsg.getGraph(gn1);
                
            // Get, do not use. ** GraphBase caches **
            PrefixMapping pmap = g.getPrefixMapping();
            //pmap.setNsPrefix("x0", "http:/example/x0#");
        });

        Txn.executeWrite(dsg, ()-> {
            System.out.println("Txn 2");
            PrefixMapping pmap = g.getPrefixMapping();
            System.out.println(System.identityHashCode(pmap)+"  "+Lib.className(pmap));
            pmap.setNsPrefix("x0", "http:/example/x0#");
        });

        //printDsg("End", dsg);
        System.exit(0);
    }
    
    public static void printDsg(String label, DatasetGraph dsg) {
        Runnable r =  ()->{
            System.out.println("**** "+label);
            printPrefixes(dsg);
            RDFDataMgr.write(System.out, dsg, Lang.NQUADS);
        };
        
        if ( dsg.isInTransaction() )
            r.run();
        else 
            Txn.executeRead(dsg, r); 
    }
    
    public static void printPrefixesTxn(DatasetGraph dsg) {
        Txn.executeRead(dsg,  ()->printPrefixes(dsg));
    }
    
    public static void printPrefixes(DatasetGraph dsg) {
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
        DatasetPrefixesTDB prefixes = dsgtdb.getPrefixes();
        Set<String> gns = prefixes.graphNames();
        System.out.print("Graphnames/Prefixes:");
        gns.forEach(x->System.out.print("  \""+x+"\""));
        System.out.println();
        //System.out.printf("  %-20s : %s\n", "dft", prefixes.getPrefixMapping());
        gns.forEach((gn)-> {
            if ( gn.isEmpty() ) {
                gn = "default graph";
                System.out.printf("  %-20s : %s\n", "dft", prefixes.getPrefixMapping());
            }
            else
                // *** "" does not work.
                System.out.printf("  %-20s : %s\n", gn, prefixes.getPrefixMapping(gn));   
        });
    }
}
