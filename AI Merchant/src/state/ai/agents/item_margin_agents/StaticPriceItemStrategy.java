package state.ai.agents.item_margin_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.item_selection_agents.MerchAgent;
import state.ge.items.Item;
import state.ge.utils.Margin;

class StaticPriceItemStrategy extends ItemStrategy {

    public StaticPriceItemStrategy(AbstractScript abstractScript, MerchAgent merchAgent, Item item, Margin fixedMargin) {
        super(abstractScript, merchAgent, item);
        fixedMargin.setMarginTimeout(-1);
        this.itemMargin = fixedMargin;
    }

    @Override
    protected boolean handlePCQueued() {
        state = ItemState.BUY_QUEUED;
        return true;
    }
}
