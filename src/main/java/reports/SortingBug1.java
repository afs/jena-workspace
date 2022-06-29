package reports;

import java.io.FileNotFoundException;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;



public class SortingBug1 {

    // 2022-06-18
    private static final String queryStr = "SELECT ?label WHERE { ?object "
            + "<http://www.w3.org/2000/01/rdf-schema#label> ?label ."
            + "} "
            + "ORDER BY ASC(?label)";

    public static void main(String[] args) throws FileNotFoundException {
        int counter = 0;
        String DATA = "../jena-workspace/"+"bad-small-19.ttl";
        for ( int i = 0 ; i < 100_000 ; i++ ) {
            Model m = RDFDataMgr.loadModel(DATA);

            boolean result = isReproduced(queryStr, m);
            if ( result ) {
                counter++;
            }
        }
        System.out.println("finished " + counter);
    }

    @SuppressWarnings("resource")
    private static boolean isReproduced(String queryStr, Model m) {
        boolean result = true;
        QueryExecution qe = QueryExecutionFactory.create(queryStr, m);
        try {
            ResultSet rs = qe.execSelect();
            if ( rs.hasNext() ) {
                result = false;
            }
        } catch (Exception e) {}
        finally {
            qe.close();
        }
        return result;
    }

}
