package state.ai.item_strategies;

import state.ai.Actionable;
import state.ai.agents.Agent;
import state.ge.Item;
import state.ge.Margin;

class StaticPriceItemStrategy extends ItemStrategy implements Actionable {

    private Margin fixedMargin;

    public StaticPriceItemStrategy(Agent agent, Item item, Margin fixedMargin) {
        super(agent, item);
        this.fixedMargin = fixedMargin;
    }

    @Override
    protected boolean commenceFlip() {
        return placeOffer(fixedMargin);
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
