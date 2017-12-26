package state.ai.agents.merch_node_agents;

import org.dreambot.api.script.AbstractScript;
import services.PriceChecker;
import services.PriceCheckerEndpoint;
import state.AgentNode;
import state.ai.agents.item_strategies.ItemStrategy;
import state.ai.agents.item_strategies.PriceCheckerItemStrategy;
import state.ge.*;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;

import java.util.*;

/*
 * MerchAgent's performAction() method should iterate over waiting item queue, calling performAction() for each item until an
 * action is performed or until waiting items is exhausted.
 */
public abstract class MerchAgent extends AgentNode {

    protected GrandExchangeInterface ge;
    private PriceChecker pc = new PriceChecker(PriceCheckerEndpoint.GE_TRACKER);

    private Queue<Item> itemQueue;
    Map<Item, ItemStrategy> itemStrategies = new HashMap<>();
    private Map<Item, ItemRestrictions> itemRestrictions = new HashMap<>();

    private Set<Flip> completedFlips = new HashSet<>();

    // TODO: Create builder
    public MerchAgent(AbstractScript abstractScript, GrandExchangeInterface ge, Queue<Item> itemQueue, Map<Item,
            ItemRestrictions> itemRestrictions, PriceChecker pc, Map<Item, ItemStrategy> itemStrategies) {
        super(abstractScript);
        this.ge = ge;
        this.itemQueue = itemQueue;
        this.pc = pc;
        for(Item item : itemQueue) {
            ItemRestrictions restrictions = itemRestrictions.containsKey(item) ? itemRestrictions.get(item) : new ItemRestrictions();
            ItemStrategy strategy = itemStrategies.containsKey(item) ? itemStrategies.get(item)
                    : new PriceCheckerItemStrategy(abstractScript, this, item, 2.0, 0.3);
            strategy.setRestrictions(restrictions);
            this.itemStrategies.put(item, strategy);
        }
    }

    public void addCompletedFlip(Flip flip) {
        completedFlips.add(flip);
    }

    // Get queue of items waiting to be flipped
    public Queue<Item> getWaitingItemQueue() {
        Queue<Item> waitingItems = new LinkedList<>();
        for(Item item : itemQueue) {
            if(itemStrategies.get(item).isWaiting()) {
                waitingItems.add(item);
            }
        }
        return waitingItems;
    }

    public int getAvailableGold() {
        return abstractScript.getInventory().count(995);
    }

    public GrandExchangeInterface getGe() {
        return ge;
    }


}
