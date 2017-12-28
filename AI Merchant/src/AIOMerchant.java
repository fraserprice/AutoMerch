import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import state.Actionable;
import state.ai.agents.idle_node_agents.AfkIdleAgent;
import state.ai.agents.merch_node_agents.MerchAgent;
import state.ai.agents.merch_node_agents.PriorityQueueMerchAgentBuilder;
import state.ge.GrandExchangeAPI;
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
        GrandExchangeAPI ge = new GrandExchangeAPI(this, 3);
        Item item1 = new Item("Bronze dagger");
        Item item2 = new Item("Iron warhammer");
        Queue<Item> itemQueue = new LinkedList<>();
        itemQueue.add(item1);
        //itemQueue.add(item2);
        MerchAgent merchAgent = new PriorityQueueMerchAgentBuilder()
                .abstractScript(this)
                .ge(ge)
                .itemQueue(itemQueue)
                .build();
        agentNodes.add(merchAgent);
        agentNodes.add(new AfkIdleAgent(this));
    }

    @Override
    public int onLoop() {
        log("--------------------");
        for(Actionable agentNode : agentNodes) {
            if(agentNode.performAction()) {
                return 250;
            }
        }
        return 10000;
    }

    @Override
    public void onExit() {
        System.out.println("Thanks for using 123Flip!");
    }

}

