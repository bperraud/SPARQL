import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Scanner;

public class ManagerAgent extends Agent {

    class HandleRequestsBehaviour extends CyclicBehaviour {

        private void processRequest(ACLMessage message) {

            Scanner sc = new Scanner(message.getContent());
            String[] triplet = new String[3];

            for (int i = 0; i < triplet.length; ++i) {
                String next = sc.next();
                triplet[i] = next;
            }

            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.setContent(String.join(";", triplet));
            AID receiver = getAID("KB");
            request.addReceiver(receiver);
            send(request);
        }


        private void setModelSrc(ACLMessage message) {
            ACLMessage request = new ACLMessage(ACLMessage.INFORM_REF);
            request.setContent(message.getContent());
            AID receiver = getAID("KB");
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
                else if (performative == ACLMessage.INFORM_REF)
                    setModelSrc(message);

            } else
                block();

        }

    }

    protected void setup() {
        addBehaviour(new HandleRequestsBehaviour());
    }
}
