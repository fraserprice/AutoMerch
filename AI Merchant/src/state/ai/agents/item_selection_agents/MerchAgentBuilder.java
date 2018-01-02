package state.ai.agents.item_selection_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.item_margin_agents.ItemStrategy;
import state.ge.GrandExchangeAPI;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;

import java.util.*;

public abstract class MerchAgentBuilder<T extends MerchAgentBuilder<T>> {
    protected AbstractScript abstractScript;

    protected GrandExchangeAPI ge;

    protected Queue<Item> itemQueue;
    protected Map<Item, ItemStrategy> itemStrategies = new HashMap<>();
    protected Map<Item, ItemRestrictions> itemRestrictions = new HashMap<>();

    protected abstract T getThis();

    public T abstractScript(AbstractScript abstractScript) {
        this.abstractScript = abstractScript;
        return getThis();
    }

    public T ge(GrandExchangeAPI ge) {
        this.ge = ge;
        return getThis();
    }

    public T itemQueue(Queue<Item> itemQueue) {
        this.itemQueue = itemQueue;
        return getThis();
    }

    public T itemRestrictionsMap(Map<Item, ItemRestrictions> itemRestrictionsMap) {
        this.itemRestrictions = itemRestrictionsMap;
        for(Item item : itemQueue) {
            if(itemRestrictionsMap.containsKey(item)) {
                this.itemRestrictions.put(item, itemRestrictionsMap.get(item));
            } else {
                this.itemRestrictions.put(item, new ItemRestrictions());
            }
        }
        return getThis();
    }

    public T itemStrategies(Map<Item, ItemStrategy> itemStrategies) {
        this.itemStrategies = itemStrategies;
        return getThis();
    }
}
