import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class SPARQLMain {

    public static void main(String[] args) {

        Runtime rt = Runtime.instance();
        ProfileImpl p;

        try {

            String SECONDARY_PROPERTIES_FILE = "secondary_container.txt";
            p = new ProfileImpl(SECONDARY_PROPERTIES_FILE);
            ContainerController cc = rt.createAgentContainer(p);

            AgentController ac = cc.createNewAgent("KB", "KBAgent", null);
            ac.start();

            ac = cc.createNewAgent("Manager", "ManagerAgent", null);
            ac.start();

            ac = cc.createNewAgent("PropagateSparql", "PropagateSparqlAgent", null);
            ac.start();

            ac = cc.createNewAgent("Geodata", "GeodataAgent", null);
            ac.start();

            ac = cc.createNewAgent("PropagateGeoSparql", "PropagateGeoSparqlAgent", null);
            ac.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}