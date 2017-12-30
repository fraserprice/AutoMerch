package state.ai.agents.item_strategies;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.merch_node_agents.MerchAgent;
import state.ge.items.Item;

class MachineLearningItemStrategy extends ItemStrategy {

    public MachineLearningItemStrategy(AbstractScript abstractScript, MerchAgent merchAgent, Item item) {
        super(abstractScript, merchAgent, item);
    }

    @Override
    protected boolean handlePCQueued() {
        return false;
    }
    @Override
    protected boolean handleBuyQueued() {
        return false;
    }

    @Override
    protected boolean handleBuying() {
        return false;
    }

    @Override
    protected boolean handleBought() {
        return false;
    }

    @Override
    protected boolean handleSelling() {
        return false;
    }

    @Override
    protected boolean handleSold() {
        return false;
    }

}
