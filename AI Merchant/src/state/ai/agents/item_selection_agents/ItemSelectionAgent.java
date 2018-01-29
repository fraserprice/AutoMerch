package state.ai.agents.item_selection_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.AgentNode;
import state.ai.agents.item_margin_agents.GEPriceCheckerMarginAgent;
import state.ai.agents.item_margin_agents.MarginAgent;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;
import state.ge.utils.Flip;
import utils.ScriptData;

import java.util.*;

/*
 * ItemSelectionAgent's performAction() method should iterate over waiting item queue, calling performAction() for each item until an
 * action is performed or until waiting items is exhausted.
 */
public abstract class ItemSelectionAgent extends AgentNode {

    protected ScriptData scriptData;

    // TODO: Create builder
    public ItemSelectionAgent(ScriptData scriptData) {
        super(scriptData);

        this.scriptData = scriptData;
        scriptData.setItemSelectionAgent(this);
        scriptData.setCompletedFlips(new HashSet<>());
        scriptData.setItemMarginAgents(new HashMap<>());

        for(Item item : scriptData.getItemQueue()) {
            ItemRestrictions restrictions
                    = scriptData.getItemRestrictions().containsKey(item)
                            ? scriptData.getItemRestrictions().get(item)
                            : new ItemRestrictions();
            MarginAgent marginAgent
                    = scriptData.getItemMarginAgents().containsKey(item)
                    ? scriptData.getItemMarginAgents().get(item)
                    : new GEPriceCheckerMarginAgent(scriptData, item, 5);

            scriptData.getItemRestrictions().put(item, restrictions);
            scriptData.getItemMarginAgents().put(item, marginAgent);
        }
    }

    public void addItem(Item newItem) {
        scriptData.getItemQueue().add(newItem);
        scriptData.getItemMarginAgents().put(newItem,
                new GEPriceCheckerMarginAgent(scriptData, newItem, 0));
        scriptData.getItemRestrictions().put(newItem, new ItemRestrictions());
    }

    public void removeItem(Item oldItem) {
        scriptData.getItemQueue().remove(oldItem);
        scriptData.getItemMarginAgents().remove(oldItem);
        scriptData.getItemRestrictions().remove(oldItem);
    }

    public void addItemMarginAgent(MarginAgent marginAgent) {
        scriptData.getItemMarginAgents().put(marginAgent.getItem(), marginAgent);
    }

    public void addCompletedFlip(Flip flip) {
        scriptData.getCompletedFlips().add(flip);
    }

    // Get queue of items waiting to be flipped
    public Queue<Item> getWaitingItemQueue() {
        Queue<Item> waitingItems = new LinkedList<>();
        for(Item item : scriptData.getItemQueue()) {
            if(scriptData.getItemMarginAgents().get(item).isWaiting()) {
                waitingItems.add(item);
            }
        }
        return waitingItems;
    }

}
