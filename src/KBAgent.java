import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

public class KBAgent extends Agent {

    private String modelUrl = "abox.n3";

    class HandleRequestsBehaviour extends CyclicBehaviour {

        private String getInfoFromPerson(String id) {

            Model model = ModelFactory.createDefaultModel();
            FileManager.get().readModel(model, modelUrl, "TURTLE");

            String nstd5 = model.getNsPrefixURI("td5");
            Resource rsrc = model.getResource(nstd5 + id);

            Selector selectTypes = new SimpleSelector(rsrc, null, (Resource) null);
            StmtIterator iterator = model.listStatements(selectTypes);

            StringBuilder answer = new StringBuilder();

            while (iterator.hasNext()) {
                Statement st = iterator.nextStatement();
                Property predicate = st.getPredicate();
                RDFNode obj = st.getObject();

                switch (predicate.getLocalName()) {
                    case "name":
                        answer.append("La personne ").append(id).append(" s'appelle ").append(obj.toString()).append(".\n");
                        break;
                    case "knows":
                        answer.append("La personne ").append(id).append(" connaît ").append(obj.asResource().getLocalName()).append(".\n");
                        break;
                    case "topic_interest":
                        answer.append("La personne ").append(id).append(" s'intéresse à ").append(obj.asResource().getLocalName()).append("\n");
                        break;
                }
            }

            return answer.toString();
        }

        private String getInfoFromPersonName(String personName) {

            Model model = ModelFactory.createDefaultModel();
            FileManager.get().readModel(model, modelUrl, "TURTLE");

            String nsfoaf = model.getNsPrefixURI("foaf");
            Property name = model.getProperty(nsfoaf + "name");

            Selector selectTypes = new SimpleSelector(null, name, personName);
            StmtIterator iterator = model.listStatements(selectTypes);

            System.out.println("name : " + personName);

            Statement st = iterator.nextStatement();
            return getInfoFromPerson(st.getSubject().asResource().getLocalName());
        }

        private String getAcquaintancesFromPerson(String id) {
            Model model = ModelFactory.createDefaultModel();
            FileManager.get().readModel(model, modelUrl, "TURTLE");

            String nstd5 = model.getNsPrefixURI("td5");
            String nsfoaf = model.getNsPrefixURI("foaf");
            Resource rsrc = model.getResource(nstd5 + id);
            Property knows = model.getProperty(nsfoaf + "knows");

            Selector selectTypes = new SimpleSelector(rsrc, knows, (Resource) null);
            StmtIterator iterator = model.listStatements(selectTypes);

            StringBuilder answer = new StringBuilder();

            while (iterator.hasNext()) {
                Statement st = iterator.nextStatement();
                RDFNode obj = st.getObject();
                answer.append("La personne ").append(id).append(" connaît ").append(obj.asResource().getLocalName()).append(".").append("\n");
            }

            return answer.toString();
        }

        private void processTurtleRequest(ACLMessage message) throws Exception {
            String[] triplet = message.getContent().split(";");
            String
                    subject = triplet[0].substring(0, 1).equals("?") ? "" : triplet[0],
                    predicate = triplet[1].substring(0, 1).equals("?") ? "" : triplet[1],
                    obj = triplet[2].substring(0, 1).equals("?") ? "" : triplet[2];

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            String content = "";

            if (predicate.isEmpty()) {

                if (subject.isEmpty())
                    throw new Exception("NO ID GIVEN!!");

                content = getInfoFromPerson(subject);

            } else {
                switch (predicate) {
                    case "foaf:name":
                        content = getInfoFromPersonName(obj);
                        break;
                    case "foaf:knows":
                        content = getAcquaintancesFromPerson(subject);
                        break;
                }
            }

            reply.setContent(content);
            send(reply);
        }

        private void setModelSrc(ACLMessage message) {
            modelUrl = message.getContent();
        }

        private void processSparqlRequest(ACLMessage message) {
            String query = message.getContent();
            Model model = ModelFactory.createDefaultModel();
            FileManager.get().readModel(model, modelUrl);
            String content = runSelectQuery(query, model);

            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(content);
            send(reply);
        }

        private String runSelectQuery(String queryString, Model model) {
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
            ResultSet resultSet = queryExecution.execSelect();
//            ResultSetFormatter.out(System.out, resultSet);
//            QuerySolution querySolution = resultSet.next();
//            querySolution.
//            for (String s : resultSet.getResultVars()) {
//                System.out.println(s);
//            }

//            while (resultSet.hasNext()) {
//                QuerySolution querySolution = resultSet.nextSolution();
//                RDFNode node = querySolution.get("p");
//                System.out.println(node.asResource().getLocalName());
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
                AID sender = message.getSender();
                if (sender.equals(getAID("Manager"))) {
                    try {
                        int performative = message.getPerformative();

                        if (performative == ACLMessage.REQUEST)
                            processTurtleRequest(message);
                        else if (performative == ACLMessage.INFORM_REF)
                            setModelSrc(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (sender.equals(getAID("PropagateSparql"))) {
                    if (message.getPerformative() == ACLMessage.REQUEST)
                        processSparqlRequest(message);
                }
            } else
                block();

        }

    }

    protected void setup() {
        addBehaviour(new HandleRequestsBehaviour());
    }
}
