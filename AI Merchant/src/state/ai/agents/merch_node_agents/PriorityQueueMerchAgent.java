package state.ai.agents.merch_node_agents;

import org.dreambot.api.script.AbstractScript;
import services.PriceChecker;
import state.Actionable;
import state.ai.agents.item_strategies.ItemStrategy;
import state.ge.*;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;

import java.util.Map;
import java.util.Queue;

public class PriorityQueueMerchAgent extends MerchAgent {


    public PriorityQueueMerchAgent(AbstractScript abstractScript, GrandExchangeInterface ge, Queue<Item> itemQueue,
                                   Map<Item, ItemRestrictions> itemRestrictionsMap, PriceChecker pc, Map<Item,
            ItemStrategy> itemStrategies) {
        super(abstractScript, ge, itemQueue, itemRestrictionsMap, pc, itemStrategies);
    }

    @Override
    public boolean performAction() {
        for(Item item : getWaitingItemQueue()) {
            Actionable itemStrategy = itemStrategies.get(item);
            if(itemStrategy.performAction()) {
                return true;
            }
        }
        return false;
    }

}
