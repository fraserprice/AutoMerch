package state.ai.agents.item_strategies;

import org.dreambot.api.script.AbstractScript;
import state.ge.flips.Margin;
import utils.OSBPriceChecker;
import state.ai.agents.merch_node_agents.MerchAgent;
import state.ge.items.Item;

import static state.ai.agents.item_strategies.ItemStrategy.ItemState.BUY_QUEUED;

public class OSBPriceCheckerItemStrategy extends ItemStrategy {

    public OSBPriceCheckerItemStrategy(AbstractScript abstractScript, MerchAgent merchAgent, Item item) {
        super(abstractScript, merchAgent, item);
    }

    @Override
    protected boolean handlePCQueued() {
        Margin itemMargin = OSBPriceChecker.getCurrentMarginEstimate(item);
        if(itemMargin.areBothValid()) {
            this.itemMargin = itemMargin;
            state = BUY_QUEUED;
        }
        return true;
    }
}
