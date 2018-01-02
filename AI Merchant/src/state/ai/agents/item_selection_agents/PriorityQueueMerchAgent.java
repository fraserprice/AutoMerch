package state.ai.agents.item_selection_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.AgentNode;
import state.ai.agents.item_margin_agents.ItemStrategy;
import state.ge.*;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;

import java.util.Map;
import java.util.Queue;

public class PriorityQueueMerchAgent extends MerchAgent {


    public PriorityQueueMerchAgent(AbstractScript abstractScript, GrandExchangeAPI ge, Queue<Item> itemQueue,
                                   Map<Item, ItemRestrictions> itemRestrictionsMap, Map<Item,
            ItemStrategy> itemStrategies) {
        super(abstractScript, ge, itemQueue, itemRestrictionsMap, itemStrategies);
    }

    @Override
    public boolean performAction() {
        for(Item item : getWaitingItemQueue()) {
            AgentNode itemStrategy = itemStrategies.get(item);
            if(itemStrategy.performAction()) {
                return true;
            }
        }
        return false;
    }

}
