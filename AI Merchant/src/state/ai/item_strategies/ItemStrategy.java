package state.ai.item_strategies;

import state.ai.Actionable;
import state.ai.agents.Agent;
import state.ge.*;

public abstract class ItemStrategy implements Actionable {

    protected final Agent agent;
    protected Item item;
    protected GrandExchange ge;
    protected Flip currentFlip;

    public ItemStrategy(Agent agent, Item item) {
        this.agent = agent;
        this.item = item;
        this.ge = agent.getGe();
        this.currentFlip = ge.getOngoingFlip(item);
    }

    /*
     * This default framework should be ok for vast majority of strategies. Can be overridden if we do not want this
     * behaviour. If so, all methods below should still be used for clarity + consistency; there shouldn't really be a
     * case where we need different methods.
     */
    @Override
    public boolean performAction() {
        currentFlip = ge.getOngoingFlip(item);

        if(currentFlip != null) {
            FlipStatus flipStatus = currentFlip.getStatus();
            if(ge.offerIsCompleted(currentFlip)) {
                if(flipStatus == FlipStatus.SELLING) {
                    return collectFinishedSell();
                } else if(flipStatus == FlipStatus.BUYING) {
                    return collectFinishedBuy();
                }
            } else {
                if(flipStatus == FlipStatus.SELLING) {
                    long maxAllowedTime = currentFlip.getSellOfferPlacedAt() + currentFlip.getMaxOfferTime();
                    if(maxAllowedTime > System.currentTimeMillis()) {
                        return sellTimeout();
                    }
                } else if(flipStatus == FlipStatus.BUYING) {
                    long maxAllowedTime = currentFlip.getBuyOfferPlacedAt() + currentFlip.getMaxOfferTime();
                    if(maxAllowedTime > System.currentTimeMillis()) {
                        return buyTimeout();
                    }
                }
            }
        } else if(ge.availableSlotCount() > 0 && ge.getAvailableItemAmount(item) > 0) {
            return commenceFlip();
        }


        return false;
    }

    /*
     * Utility functions to reduce duplication
     */

    protected boolean placeOffer(Margin margin) {
        int availableGoldPerFlip = agent.getAvailableGold() / ge.availableSlotCount();
        if(margin.getMinimum() < availableGoldPerFlip) {
            ItemSet itemSet = new ItemSet(item, availableGoldPerFlip / margin.getMinimum());
            Flip newFlip = new Flip(itemSet, margin.getMinimum(), margin.getMaximum());
            agent.placeFlipBuyOffer(newFlip);
            return true;
        }
        return false;
    }

    /*
     * Below methods are all sub-strategies for our item strategy. They should all return a boolean value if action is
     * actually performed. This should be false in the case of failure rather than because conditions are not right for
     * the given sub-strategy (e.g. cannot find suitable margins and so do not commence flip).
     */

    // Place buy offer for item. Margins should be decided by specific policy.
    protected abstract boolean commenceFlip();

    // Cancel sell offer due to timeout. Policy should then deal with any items which were not successfully sold.
    protected abstract boolean sellTimeout();

    // Cancel buy offer due to timeout and sell successfully bought items.
    protected boolean buyTimeout() {
        agent.placeFlipSellOffer(agent.cancelOffer(currentFlip));
        return true;
    }


    // Collect finished buy offer and place sell offer. Can be overridden in case of any non-default behaviour.
    protected boolean collectFinishedBuy() {
        agent.collectFinishedBuyingFlip(currentFlip);
        agent.placeFlipSellOffer(currentFlip);
        return true;
    }

    // Collect finished sell offer.
    protected boolean collectFinishedSell() {
        agent.collectFinishedSellingFlip(currentFlip);
        return true;
    }

}
