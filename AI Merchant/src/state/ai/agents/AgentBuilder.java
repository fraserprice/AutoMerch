package state.ai.agents;

import services.PriceChecker;
import services.PriceCheckerEndpoint;
import state.ai.item_strategies.ItemStrategy;
import state.ge.GrandExchangeInterface;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;

import java.util.*;

public abstract class AgentBuilder<T extends AgentBuilder<T>> {
    protected GrandExchangeInterface ge;
    protected PriceChecker pc = new PriceChecker(PriceCheckerEndpoint.GE_TRACKER);

    protected Queue<Item> itemQueue;
    protected Map<Item, ItemStrategy> itemStrategies = new HashMap<>();
    protected Map<Item, ItemRestrictions> itemRestrictions = new HashMap<>();

    protected abstract T getThis();

    public T ge(GrandExchangeInterface ge) {
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

    public T priceChecker(PriceChecker pc) {
        this.pc = pc;
        return getThis();
    }

    public T itemStrategies(Map<Item, ItemStrategy> itemStrategies) {
        this.itemStrategies = itemStrategies;
        return getThis();
    }
}
