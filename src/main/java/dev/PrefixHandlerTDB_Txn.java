package dev;

import java.util.Map ;

import static org.apache.jena.system.Txn.* ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.core.GraphView ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;

/** Protect all the core operations within a transaction. */ 
public class PrefixHandlerTDB_Txn extends PrefixMappingImpl {
    private final DatasetGraphTransaction dsgtxn ;
    private final GraphView graph ;
    private final String name ;

    public PrefixHandlerTDB_Txn(DatasetGraphTransaction dsgtxn, GraphView graph, /*dev aid*/String name) {
        this.dsgtxn = dsgtxn ;
        this.graph = graph ;
        this.name = name ;
        executeWrite(dsgtxn, ()->{
            // Load the in-memory cache of prefixes.
            PrefixMapping pmap = getRawGraph().getPrefixMapping() ;
            pmap.getNsPrefixMap().forEach((p,u) -> super.set(p,u)) ;
        }) ;
    }
    
    @Override
    public PrefixMapping setNsPrefixes( PrefixMapping other ) {
        Map<String, String> map = other.getNsPrefixMap() ;
        // Make a txn safe call.
        return setNsPrefixes(map) ;
    }
    
    @Override
    public PrefixMapping setNsPrefixes( Map<String, String> map ) {
        executeWrite(dsgtxn, ()-> { 
            map.forEach((p,u)-> {
                // Add if and only if not defined.
                if ( super.get(p) == null )
                    setWorker(p,u) ;
            }) ;
        } ) ;
        return this ;
    }

    @Override
    protected void set(String prefix, String uri) {
        executeWrite(dsgtxn, ()->{
            setWorker(prefix, uri) ;
        }) ;
    }

    // Must be inside a transaction to use this.
    private void setWorker(String prefix, String uri) {
        getRawGraph().getPrefixMapping().setNsPrefix(prefix, uri) ;
        super.set(prefix, uri) ;
    }
    
    // Must be inside a transaction to use this.
    private Graph getRawGraph() {
        if ( graph.getGraphName() == null )
            return dsgtxn.getDefaultGraph() ;
        return dsgtxn.getGraph(graph.getGraphName()) ;
    }
    
    @Override
    protected String get(String prefix) {
        String uri = super.get(prefix) ;
        if ( uri != null )
            return uri ;
        return calculateRead(dsgtxn, ()-> getRawGraph().getPrefixMapping().getNsPrefixURI(prefix) ) ;
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        executeWrite(dsgtxn, ()->{
            getRawGraph().getPrefixMapping().removeNsPrefix(prefix) ;
            super.removeNsPrefix(prefix) ;
        }) ;
        return this ; 
    }
}
