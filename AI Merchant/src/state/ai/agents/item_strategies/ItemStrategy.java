package state.ai.agents.item_strategies;

import org.dreambot.api.script.AbstractScript;
import state.AgentNode;
import state.ai.agents.merch_node_agents.MerchAgent;
import state.ge.*;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;
import state.ge.items.ItemSet;

import static state.ai.agents.item_strategies.ItemStrategy.ItemState.*;

public abstract class ItemStrategy extends AgentNode {

    protected final MerchAgent merchAgent;
    protected final GrandExchangeInterface ge;

    protected final Item item;
    protected ItemRestrictions restrictions = new ItemRestrictions();


    protected ItemState state = IDLE;
    protected int slot = -1;
    protected Flip flip;
    protected Margin itemMargin = new Margin();

    public ItemStrategy(AbstractScript abstractScript, MerchAgent merchAgent, Item item) {
        super(abstractScript);
        this.merchAgent = merchAgent;
        this.item = item;
        this.ge = merchAgent.getGe();
        this.flip = null;
    }

    public ItemState getState() {
        return state;
    }

    public Flip getFlip() {
        return flip;
    }

    public void setRestrictions(ItemRestrictions restrictions) {
        this.restrictions = restrictions;
    }

    public boolean isWaiting() {
        return state == IDLE
                && (ge.getAvailableItemAmount(item) > 0 || ge.getAvailableItemAmount(item) == -1)
                && !restrictions.isBadItem();
    }

    /*
     * ItemStrategy represented by finite state machine where states are defined in ItemState and transitions
     * represented by protected object methods. This model should be fine for vast majority of item strategies; can
     * override methods in children if we need different behaviour.
     */
    @Override
    public boolean performAction() {
        switch(state) {
            case IDLE:
                if(isWaiting() && ge.availableSlotCount() > 0) {
                    if(itemMargin.isMinimumValid() && itemMargin.isMaximumValid()) {
                        state = BUY_QUEUED;
                        return true;
                    } else {
                        state = PC_BUY_QUEUED;
                        return true;
                    }
                }
                return false;
            case PC_BUY_QUEUED:
                return handlePCBuyQueued();
            case PC_BUYING:
                return handlePCBuying();
            case PC_BOUGHT:
                return handlePCBought();
            case PC_SELLING:
                return handlePCSelling();
            case BUY_QUEUED:
                return handleBuyQueued();
            case BUYING:
                return handleBuying();
            case BOUGHT:
                return handleBought();
            case SELLING:
                return handleSelling();
            case SOLD:
                return handleSold();
            default:
                return false;
        }
    }

    /*
     * Utility functions to reduce duplication + for abstraction
     */

    protected boolean placeBuyOffer() {
        if(flip == null) {
            int availableSlots = ge.availableSlotCount();
            int waitingQueueSize = merchAgent.getWaitingItemQueue().size();
            int availableGold = merchAgent.getAvailableGold();
            int goldPerSlot = availableSlots == 0 ? 0 : availableGold / availableSlots;
            int goldPerQueueItem = waitingQueueSize == 0 ? 0 : availableGold / waitingQueueSize;
            int availableGoldPerFlip = Math.max(goldPerQueueItem, goldPerSlot);
            if(itemMargin.getMinimum() < availableGoldPerFlip && itemMargin.getMinimum() > 0) {
                ItemSet itemSet = new ItemSet(item, availableGoldPerFlip / itemMargin.getMinimum());
                flip = new Flip(itemSet, itemMargin.getMinimum(), itemMargin.getMaximum());
            }
        }
        return (slot = ge.placeBuyOffer(flip)) != -1;
    }

    protected boolean placeSellOffer() {
        return (slot = ge.placeSellOffer(flip)) != -1;
    }

    protected boolean collectBuyOffer() {
        OfferCollection collection = ge.collectOffer(slot);
        if(collection == null) {
            return false;
        }
        flip.setBuyPrice(flip.getBuyPrice() - collection.getGold() / collection.getItems().getItemAmount());
        return true;
    }

    protected boolean collectSellOffer() {
        OfferCollection collection = ge.collectOffer(slot);
        if(collection == null) {
            return false;
        }
        flip.setSellPrice(collection.getGold() / flip.getItemSet().getItemAmount());
        flip.setFlipCompletedAt(System.currentTimeMillis());
        return true;
    }

    /*
     * Cancel given offer slot. We have two cases:
     *
     * 1. Buy offer:
     *     - Create new flip containing only the bought number of items with correct buy price
     *     - In all merchAgent cases we would like to immediately sell the item rather than persist in buying, hence why we
     *       simply reduce the amount of items bought. This lets us continue to either try to directly sell item, or
     *       alternatively re-check price and sell item.
     *
     * 2. Sell offer: TODO: Alter buy time for each flip to avoid inaccuracy?
     *     - Create two new flips.
     *     - i.  Flip containing number of items successfully sold. Add this flip to completed flip list.
     *     - ii. Flip containing number of items still unsold. We return this flip so merchAgent can decide what action to
     *           take next.
     */
    protected boolean cancelBuyOffer() {
        OfferCollection collection = ge.cancelOffer(slot);
        if (collection == null) {
            return false;
        }
        flip.setItemSet(collection.getItems());
        int offerPrice = flip.getBuyPrice() * flip.getItemAmount();
        int actualBuyValue = offerPrice - collection.getGold();
        flip.setBuyPrice((offerPrice - actualBuyValue) / collection.getItems().getItemAmount());

        return true;
    }

    protected boolean cancelSellOffer() {
        OfferCollection collection = ge.collectOffer(slot);
        if(collection == null) {
            return false;
        }

        int sellPrice = (flip.getItemAmount() - collection.getItems().getItemAmount()) / collection.getGold();
        Flip completedFlip = new Flip(collection.getItems(), flip.getBuyPrice(), sellPrice);

        completedFlip.copyFlipTimes(flip);
        completedFlip.setFlipCompletedAt(System.currentTimeMillis());
        merchAgent.addCompletedFlip(completedFlip);

        Flip newflip = new Flip(collection.getItems(), flip.getBuyPrice(), flip.getSellPrice());
        newflip.copyFlipTimes(flip);
        flip = newflip;

        return true;
    }


    /*
     * Below methods are all sub-strategies for our item strategy. They should all return a boolean value if action is
     * actually performed. This should be false in the case of failure rather than because conditions are not right for
     * the given sub-strategy (e.g. cannot find suitable margins and so do not commence flip).
     */
    // Place buy offer for item. Margins should be decided by specific policy.
    protected abstract boolean handlePCBuyQueued();

    protected abstract boolean handlePCBuying();

    protected abstract boolean handlePCBought();

    protected abstract boolean handlePCSelling();

    protected abstract boolean handleBuyQueued();

    protected abstract boolean handleBuying();

    protected abstract boolean handleBought();

    protected abstract boolean handleSelling();

    protected abstract boolean handleSold();

    // Represents all possible flip states for each item. PC = price check
    protected enum ItemState {
        IDLE,
        PC_BUY_QUEUED, PC_BUYING, PC_BOUGHT,
        PC_SELLING,
        BUY_QUEUED, BUYING, BOUGHT,
        SELLING, SOLD
    }

}
