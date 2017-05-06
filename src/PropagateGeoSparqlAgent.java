import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("unused")
public class PropagateGeoSparqlAgent extends Agent {

    class HandleRequestsBehaviour extends CyclicBehaviour {

        private void processRequest(ACLMessage message) {

            String requestString = "" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX lgd: <http://linkedgeodata.org/>\n" +
                    "PREFIX lgdo: <http://linkedgeodata.org/ontology/>\n";

            requestString += message.getContent();

            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

            request.setContent(requestString);

            AID receiver = getAID("Geodata");
            request.addReceiver(receiver);
            send(request);
        }

        @SuppressWarnings("Duplicates")
        @Override
        public void action() {

            ACLMessage message = receive();

            if (message != null) {

                int performative = message.getPerformative();

                if (performative == ACLMessage.REQUEST)
                    processRequest(message);
                else if (performative == ACLMessage.INFORM)
                    System.out.println(message.getContent());
//                else if (performative == ACLMessage.INFORM_REF)
//                    setModelSrc(message);

            } else
                block();

        }

    }

    protected void setup() {
        addBehaviour(new HandleRequestsBehaviour());
    }
}
