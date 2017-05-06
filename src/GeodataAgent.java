import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.jena.query.*;

@SuppressWarnings("unused")
public class GeodataAgent extends Agent {

    class HandleRequestsBehaviour extends CyclicBehaviour {

        private void processGeoSparqlRequest(ACLMessage message) {
            String query = message.getContent();

            String content = runSelectQuery(query);

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(content);
            send(reply);
        }

        private String runSelectQuery(String queryString) {
            Query query = QueryFactory.create(queryString);

            QueryExecution queryExecution =
                    QueryExecutionFactory.sparqlService("http://linkedgeodata.org/sparql", query);

            ResultSet resultSet = queryExecution.execSelect();
//            ResultSetFormatter.out(System.out, resultSet);
//            QuerySolution querySolution = resultSet.next();
//            querySolution.
//            for (String s : resultSet.getResultVars()) {
//                System.out.println(s);
//            }

            String res = ResultSetFormatter.asText(resultSet);

//            System.out.println(ResultSetFormatter.asText(resultSet));
//            System.out.println("FIN");

            queryExecution.close();

            return res;
        }

        @SuppressWarnings("Duplicates")
        @Override
        public void action() {

            ACLMessage message = receive();

            if (message != null) {
                if (message.getPerformative() == ACLMessage.REQUEST)
                    processGeoSparqlRequest(message);
            } else
                block();

        }

    }

    protected void setup() {
        addBehaviour(new HandleRequestsBehaviour());
    }
}
