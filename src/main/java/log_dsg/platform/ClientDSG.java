package log_dsg.platform;

import java.io.IOException ;

import log_dsg.Txn ;
import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.update.UpdateAction ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;

public class ClientDSG {

    public static void main(String[] args) throws IOException {
        Dataset ds1 = TDBFactory.createDataset() ;
        DatasetGraph dsg = DP.managedDatasetGraph(ds1.asDatasetGraph(), DP.PatchContainer) ;
        Dataset ds = DatasetFactory.wrap(dsg) ;
        
        String template = "INSERT DATA { <http://example/s> <http://example/p> 'XXX'^^<"+XSDDatatype.XSDdateTime.getURI()+">} ";   
        
        for ( int i = 0 ; i < 2 ; i++ ) {
            System.out.println("Ready "+i);
            System.in.read() ;
            String s = template.replace("XXX", DateTimeUtils.nowAsString()) ;
            UpdateRequest req = UpdateFactory.create(s) ;
            
            Txn.execWrite(ds, ()-> { 
                DP.syncExecW(ds.asDatasetGraph(), () -> { 
                             UpdateAction.execute(req, ds) ;
                             RDFDataMgr.write(System.out, ds, Lang.TRIG) ;
                }) ;
            }) ;
        }
    }
}
