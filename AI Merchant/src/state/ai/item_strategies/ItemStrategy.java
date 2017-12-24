package state.ai.item_strategies;

import org.dreambot.api.methods.MethodProvider;
import state.ai.Actionable;
import state.ai.agents.Agent;
import state.ge.*;
import state.ge.items.Item;
import state.ge.items.ItemSet;

public abstract class ItemStrategy implements Actionable {

    protected final Agent agent;
    protected Item item;
    protected GrandExchangeInterface ge;
    protected Flip currentFlip;
    protected Margin itemMargin = new Margin();

    protected boolean upperMarginCheckInProgress = false;
    protected boolean lowerMarginCheckInProgress = false;

    public ItemStrategy(Agent agent, Item item) {
        this.agent = agent;
        this.item = item;
        this.ge = agent.getGe();
        this.currentFlip = agent.getFlipInSlot(ge.getItemSlot(item));
    }

    /*
     * This default framework should be ok for vast majority of strategies. Can be overridden if we do not want this
     * behaviour. If so, all methods below should still be used for clarity + consistency; there shouldn't really be a
     * case where we need different methods.
     */
    @Override
    public boolean performAction() {
        int itemSlot = ge.getItemSlot(item);
        currentFlip = agent.getFlipInSlot(itemSlot);

        if(currentFlip != null) {
            FlipStatus flipStatus = currentFlip.getStatus();
            if(ge.offerIsCompleted(itemSlot)) {
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
        } else if((ge.getAvailableItemAmount(item) > 0 || ge.getAvailableItemAmount(item) == -1)
                && !agent.getItemRestrictions(item).isBadItem()) {
            MethodProvider.log(Boolean.toString(itemMargin.maximumValid()));
            if(ge.availableSlotCount() > 0 && itemMargin.minimumValid() && itemMargin.maximumValid()) {
                return commenceFlip();
            }
            if(!itemMargin.maximumValid() && ge.availableSlotCount() > 0
                    || upperMarginCheckInProgress) {
                MethodProvider.log("CHECK UPPER");
                return checkUpperMargin();
            }
            if(!itemMargin.minimumValid() && ge.availableSlotCount() > 0
                    || lowerMarginCheckInProgress) {
                MethodProvider.log("CHECK LOWER");
                return checkLowerMargin();
            }
        }
        return false;
    }

    /*
     * Utility functions to reduce duplication
     */

    protected boolean placeOffer() {
        MethodProvider.log("Attempting to make new flip offer...");
        int availableSlots = ge.availableSlotCount();
        int waitingQueueSize = agent.getWaitingItemQueue().size();
        int availableGold = agent.getAvailableGold();
        int goldPerSlot = availableSlots == 0 ? 0 : availableGold / availableSlots;
        int goldPerQueueItem = waitingQueueSize == 0 ? 0 : availableGold / waitingQueueSize;
        int availableGoldPerFlip = Math.max(goldPerQueueItem, goldPerSlot);
        if(itemMargin.getMinimum() < availableGoldPerFlip && itemMargin.getMinimum() > 0) {
            ItemSet itemSet = new ItemSet(item, availableGoldPerFlip / itemMargin.getMinimum());
            Flip newFlip = new Flip(itemSet, itemMargin.getMinimum(), itemMargin.getMaximum());
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

    // Check upper margin if not currently valid
    protected abstract boolean checkUpperMargin();

    // Check lower margin if not currently valid
    protected abstract boolean checkLowerMargin();

    // Place buy offer for item. Margins should be decided by specific policy.
    protected boolean commenceFlip() {
        return placeOffer();
    }

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
