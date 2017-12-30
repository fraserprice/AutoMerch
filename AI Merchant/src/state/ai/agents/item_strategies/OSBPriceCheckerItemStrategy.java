package state.ai.agents.item_strategies;

import org.dreambot.api.script.AbstractScript;
import utils.OSBPriceChecker;
import state.ai.agents.merch_node_agents.MerchAgent;
import state.ge.items.Item;

public class OSBPriceCheckerItemStrategy extends ItemStrategy {

    public OSBPriceCheckerItemStrategy(AbstractScript abstractScript, MerchAgent merchAgent, Item item) {
        super(abstractScript, merchAgent, item);
    }

    @Override
    protected boolean handlePCQueued() {
        itemMargin = OSBPriceChecker.getCurrentMarginEstimate(item);
        return false;
    }
}
