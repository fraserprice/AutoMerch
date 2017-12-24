package state.ai.item_strategies;

import state.ai.Actionable;
import state.ai.agents.Agent;
import state.ge.items.Item;
import state.ge.Margin;

class StaticPriceItemStrategy extends ItemStrategy implements Actionable {

    public StaticPriceItemStrategy(Agent agent, Item item, Margin fixedMargin) {
        super(agent, item);
        fixedMargin.setMarginTimeout(-1);
        this.itemMargin = fixedMargin;
    }

    @Override
    protected boolean checkUpperMargin() {
        return false;
    }

    @Override
    protected boolean checkLowerMargin() {
        return false;
    }

    @Override
    protected boolean commenceFlip() {
        return placeOffer();
    }

    // Shouldn't be timeout for fixed price flips --> return false as no action taken.
    @Override
    protected boolean buyTimeout() {
        return false;
    }

    // Shouldn't be timeout for fixed price flips --> return false as no action taken.
    @Override
    protected boolean sellTimeout() {
        return false;
    }
}
