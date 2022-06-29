package reports;

import java.io.FileNotFoundException;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;



public class SortingBug {

    private static final String queryStr = "SELECT ?label WHERE { ?object "
            + "<http://www.w3.org/2000/01/rdf-schema#label> ?label ."
            + "} "
            + "ORDER BY ASC(?label)";

    public static void main(String[] args) throws FileNotFoundException {

//        FileInputStream fis = new FileInputStream(new File("bad_data_out.n3"));
//        Model m = ModelFactory.createDefaultModel();
//
//        m.read(fis, null, "N3");
        //String DATA = "bad-small-19.ttl";
        String DATA = "bad_data_out.ttl";
        Model m = RDFDataMgr.loadModel(DATA);

        /*
         * try { Thread.sleep(3000); } catch (InterruptedException e) {
         * e.printStackTrace(); }
         */

        int counter = 0;
        for (int i=0;i<1000; i++) {
            System.out.println("Round " + i);

            boolean result = isReproduced(queryStr, m);
            if (result) {
                counter++;
            }
        }
        System.out.println("finished " + counter);
    }

    private static boolean isReproduced(String queryStr, Model m) {
        boolean result = true;
        @SuppressWarnings("resource")
        QueryExecution qe = QueryExecutionFactory.create(queryStr, m);
        try {
            ResultSet rs = qe.execSelect();
            if(rs.hasNext()) {
                //System.out.println(rs.next().toString());
                result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            qe.close();
        }
        return result;
    }

}
