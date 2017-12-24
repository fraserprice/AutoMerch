import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import state.ai.Actionable;
import state.ai.agents.Agent;
import state.ai.agents.PriorityQueueAgentBuilder;
import state.ge.GrandExchangeInterface;
import state.ge.items.Item;

import java.util.LinkedList;
import java.util.Queue;

@ScriptManifest(
        author = "XD123",
        name = "123Flip",
        version = 1.0,
        description = "AIO Flipping on the GE for profit",
        category = Category.MONEYMAKING)
public class AIOMerchant extends AbstractScript {

    private Queue<Actionable> agentNodes = new LinkedList<>();

    @Override
    public void onStart() {
        // TODO: GUI
        GrandExchangeInterface ge = new GrandExchangeInterface(3);
        Item bronzeDagger = new Item("Bronze dagger");
        Item ironArrow = new Item("Iron arrow");
        Queue<Item> itemQueue = new LinkedList<>();
        itemQueue.add(bronzeDagger);
        itemQueue.add(ironArrow);
        Agent agent = new PriorityQueueAgentBuilder()
                .ge(ge)
                .itemQueue(itemQueue)
                .build();
        agentNodes.add(agent);
        //agentNodes.add(/*TODO: Idle agent*/agent);
        //agentNodes.add(/*TODO: Exit agent*/agent);
    }

    @Override
    public int onLoop() {
        for(Actionable agent : agentNodes) {
            if(agent.performAction()) {
                return 0;
            }
        }
        return 500;
    }

    @Override
    public void onExit() {
        System.out.println("Thanks for using 123Flip!");
    }

}

