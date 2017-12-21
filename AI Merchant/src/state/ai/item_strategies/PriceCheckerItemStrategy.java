package state.ai.item_strategies;

import state.ai.Actionable;
import state.ai.agents.Agent;
import state.ge.*;

class PriceCheckerItemStrategy extends ItemStrategy implements Actionable {

    public PriceCheckerItemStrategy(Agent agent, Item item) {
        super(agent, item);
    }

    @Override
    protected boolean commenceFlip() {
        Margin itemMargin = agent.checkMarginsOnGe(item);
        return placeOffer(itemMargin);
    }

    @Override
    protected boolean sellTimeout() {
        Flip remainingFlip = agent.cancelOffer(currentFlip);
        if(ge.getAvailableItemAmount(item) > 0) {
            Margin newMargin = agent.checkMarginsOnGe(item);
            Flip newFlip = new Flip(remainingFlip.getItemSet(), currentFlip.getBuyPrice(), newMargin.getMaximum());
            newFlip.setStatus(FlipStatus.SELLING);
            agent.placeFlipSellOffer(newFlip);
        } else {
            remainingFlip.setSellPrice(currentFlip.getBuyPrice());
            agent.placeFlipSellOffer(remainingFlip);
        }
        return true;
    }
}
