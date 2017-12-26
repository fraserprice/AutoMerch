package state.ai.agents.item_strategies;

import org.dreambot.api.script.AbstractScript;
import state.Actionable;
import state.ai.agents.merch_node_agents.MerchAgent;
import state.ge.items.Item;
import state.ge.Margin;

class StaticPriceItemStrategy extends ItemStrategy implements Actionable {

    public StaticPriceItemStrategy(AbstractScript abstractScript, MerchAgent merchAgent, Item item, Margin fixedMargin) {
        super(abstractScript, merchAgent, item);
        fixedMargin.setMarginTimeout(-1);
        this.itemMargin = fixedMargin;
    }

    @Override
    protected boolean handlePCBuyQueued() {
        return false;
    }

    @Override
    protected boolean handlePCBuying() {
        return false;
    }

    @Override
    protected boolean handlePCBought() {
        return false;
    }

    @Override
    protected boolean handlePCSelling() {
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
