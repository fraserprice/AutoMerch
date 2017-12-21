package state.ai.agents;

import services.PriceChecker;
import state.ai.Actionable;
import state.ge.*;

import java.util.Queue;

public class PriorityQueueAgent extends Agent implements Actionable {


    public PriorityQueueAgent(GrandExchange ge, Queue<Item> itemList, int availableGold, PriceChecker pc) {
        super(ge, itemList, availableGold, pc);
    }

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
