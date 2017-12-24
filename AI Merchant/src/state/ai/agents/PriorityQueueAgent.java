package state.ai.agents;

import org.dreambot.api.methods.MethodProvider;
import services.PriceChecker;
import state.ai.Actionable;
import state.ai.item_strategies.ItemStrategy;
import state.ge.*;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;

import java.util.Map;
import java.util.Queue;

public class PriorityQueueAgent extends Agent {


    public PriorityQueueAgent(GrandExchangeInterface ge, Queue<Item> itemQueue, Map<Item, ItemRestrictions> itemRestrictionsMap,
                              PriceChecker pc, Map<Item, ItemStrategy> itemStrategies) {
        super(ge, itemQueue, itemRestrictionsMap, pc, itemStrategies);
    }

    @Override
    public boolean performAction() {
        MethodProvider.log("");
        for(Item item : getWaitingItemQueue()) {
            Actionable itemStrategy = itemStrategies.get(item);
            if(itemStrategy.performAction()) {
                return true;
            }
        }
        return false;
    }

}
