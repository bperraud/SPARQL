import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

public class Main {

    public static void main(String[] args) {

        String query = "requests.sparql";
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, "foaf.n3");
        runSelectQuery(query, model);
    }

    public static void runSelectQuery(String qfilename, Model model) {
        Query query = QueryFactory.read(qfilename);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet r = queryExecution.execSelect();
        ResultSetFormatter.out(System.out, r);
        queryExecution.close();
    }


    /* Base distante */
//    public static void runSelectQuery(String qfilename, Model model) {
//        Query query = QueryFactory.read(qfilename);
//
//        System.out.println("Query sent");
//
//        QueryExecution queryExecution =
//                QueryExecutionFactory.sparqlService("http://linkedgeodata.org/sparql", query);
//        ResultSet r = queryExecution.execSelect();
//        ResultSetFormatter.out(System.out, r);
//        queryExecution.close();
//    }

}