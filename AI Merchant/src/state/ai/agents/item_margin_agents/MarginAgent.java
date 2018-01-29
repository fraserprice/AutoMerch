package state.ai.agents.item_margin_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.AgentNode;
import state.ai.agents.item_selection_agents.ItemSelectionAgent;
import state.ge.*;
import state.ge.utils.Flip;
import state.ge.utils.Margin;
import state.ge.utils.OfferCollection;
import state.ge.items.Item;
import state.ge.items.ItemSet;
import utils.ScriptData;
import utils.ScriptStatus;

import static org.dreambot.api.methods.MethodProvider.log;
import static state.ai.agents.item_margin_agents.MarginAgent.ItemState.*;

/*
 * Margin agents should handle the buying/selling of selected items, as well as decide upon the margin of each item, and
 * in some cases how to determine this margin (e.g. AIOMarginAgent)
 */
public abstract class MarginAgent extends AgentNode {

    protected final ScriptData scriptData;
    protected final ItemSelectionAgent itemSelectionAgent;
    protected final GrandExchangeAPI ge;

    protected final Item item;

    protected final int undercutPercentage;
    protected ItemState state = IDLE;
    protected int slot = -1;
    protected Flip flip = null;
    protected Margin itemMargin = new Margin();

    private final int STARTING_QUANTITY_OF_ITEM_HELD;

    // TODO: Constructor for static
    public MarginAgent(ScriptData scriptData, Item item, int undercutPercentage) {
        super(scriptData);
        this.item = item;

        itemSelectionAgent = scriptData.getItemSelectionAgent();
        ge = scriptData.getGe();

        org.dreambot.api.wrappers.items.Item inventoryItem = scriptData.getAbstractScript()
                .getInventory().get(item.getItemName());
        this.STARTING_QUANTITY_OF_ITEM_HELD = inventoryItem == null ? 0 : inventoryItem.getAmount();
        this.scriptData = scriptData;
        this.undercutPercentage = undercutPercentage;
    }

    public ItemState getState() {
        return state;
    }

    public Flip getFlip() {
        return flip;
    }

    protected int getQuantityOfItemsHeld() {
        org.dreambot.api.wrappers.items.Item inventoryItem = scriptData.getAbstractScript().getInventory()
                .get(item.getItemName());
        return inventoryItem == null ? 0 : inventoryItem.getAmount() - STARTING_QUANTITY_OF_ITEM_HELD;
    }

    public boolean isWaiting() {
        return (state == IDLE
                && (ge.getAvailableItemAmount(item) > 0 || ge.getAvailableItemAmount(item) == -1)
                && !scriptData.getItemRestrictions().get(item).isBadItem())
                || state != IDLE;
    }

    /*
     * MarginAgent represented by finite state machine where states are defined in ItemState and transitions
     * represented by protected object methods. This model should be fine for vast majority of item strategies; can
     * override methods in children if we need different behaviour.
     */
    @Override
    public boolean performAction() {
        log(item.getItemName() + ": " + state.getMessage());
        boolean actionTaken;
        switch(state) {
            case IDLE:
                log(Integer.toString(ge.availableSlotCount()));
                if(isWaiting() && ge.availableSlotCount() > 0) {
                    scriptData.setStatus(ScriptStatus.IDLE);
                    if(itemMargin.isMinimumValid() && itemMargin.isMaximumValid()) {
                        state = BUY_QUEUED;
                        return true;
                    } else {
                        state = PC_QUEUED;
                        return true;
                    }
                }
                return false;
            case PC_QUEUED:
                actionTaken = handlePCQueued();
                if(actionTaken) {
                    scriptData.setStatus(ScriptStatus.PC_QUEUED);
                }
                break;
            case BUY_QUEUED:
                actionTaken = handleBuyQueued();
                if(actionTaken) {
                    scriptData.setStatus(ScriptStatus.BUY_QUEUED);
                }
                break;
            case BUYING:
                actionTaken = handleBuying();
                if(actionTaken) {
                    scriptData.setStatus(ScriptStatus.BUYING);
                }
                break;
            case BOUGHT:
                actionTaken = handleBought();
                if(actionTaken) {
                    scriptData.setStatus(ScriptStatus.BOUGHT);
                }
                break;
            case SELLING:
                actionTaken = handleSelling();
                if(actionTaken) {
                    scriptData.setStatus(ScriptStatus.SELLING);
                }
                break;
            case SOLD:
                actionTaken = handleSold();
                if(actionTaken) {
                    scriptData.setStatus(ScriptStatus.SOLD);
                }
                break;
            default:
                return false;
        }
        return actionTaken;
    }

    /*
     * Utility functions to reduce duplication + for abstraction
     */

    private boolean createNewFlip() {
        int availableSlots = ge.availableSlotCount();
        int waitingQueueSize = itemSelectionAgent.getWaitingItemQueue().size();
        int availableGold = getAvailableGold();
        int goldPerSlot = availableSlots == 0 ? 0 : availableGold / availableSlots;
        int goldPerQueueItem = waitingQueueSize == 0 ? 0 : availableGold / waitingQueueSize;
        int availableGoldPerFlip = Math.max(goldPerQueueItem, goldPerSlot);
        int undercutValue = (int) ((itemMargin.getMaximum() - itemMargin.getMinimum()) * undercutPercentage / 100.0);
        int buyPrice = itemMargin.getMinimum() - undercutValue;
        if(buyPrice < availableGoldPerFlip && buyPrice > 0) {
            int availableQuantity = Math.min(availableGoldPerFlip / buyPrice,
                    ge.getAvailableItemAmount(item));
            ItemSet itemSet = new ItemSet(item, availableQuantity);
            flip = new Flip(itemSet, itemMargin.getMinimum() + undercutValue,
                    itemMargin.getMaximum() - undercutValue);
        }
        return true;
    }

    private boolean placeFlipBuyOffer() {
        if(flip == null) {
            return createNewFlip() && (slot = ge.placeBuyOffer(flip)) != -1;
        }
        return false;
    }

    private boolean placeFlipSellOffer() {
        flip.setItemAmount(getQuantityOfItemsHeld());
        return (slot = ge.placeSellOffer(flip)) != -1;
    }

    private boolean collectBuyOffer() {
        OfferCollection collection = ge.collectOffer(slot);
        if(collection == null) {
            return false;
        }
        flip.setBuyPrice(flip.getBuyPrice() - collection.getGold() / collection.getItems().getItemAmount());
        flip.setBuyOfferFinishedAt(System.currentTimeMillis());
        return true;
    }

    private boolean collectFlipSellOffer() {
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
     *     - In all itemSelectionAgent cases we would like to immediately sell the item rather than persist in buying, hence why we
     *       simply reduce the amount of items bought. This lets us continue to either try to directly sell item, or
     *       alternatively re-check price and sell item.
     *
     * 2. Sell offer: TODO: Alter buy time for each flip to avoid inaccuracy?
     *     - Create two new utils.
     *     - i.  Flip containing number of items successfully sold. Add this flip to completed flip list.
     *     - ii. Flip containing number of items still unsold. We return this flip so itemSelectionAgent can decide what action to
     *           take next.
     */
    private boolean cancelFlipBuyOffer() {
        OfferCollection collection = ge.cancelOffer(slot);
        if (collection == null) {
            return false;
        }
        flip.setItemSet(collection.getItems());
        int offerPrice = flip.getBuyPrice() * flip.getItemAmount();
        int actualBuyValue = offerPrice - collection.getGold();
        int collectedItems = collection.getItems().getItemAmount();
        if(collectedItems > 0) {
            flip.setBuyPrice((offerPrice - actualBuyValue) / collection.getItems().getItemAmount());
        }

        return true;
    }

    private boolean cancelFlipSellOffer() {
        OfferCollection collection = ge.collectOffer(slot);
        if(collection == null) {
            return false;
        }

        int sellPrice = (flip.getItemAmount() - collection.getItems().getItemAmount()) / collection.getGold();
        Flip completedFlip = new Flip(collection.getItems(), flip.getBuyPrice(), sellPrice);

        completedFlip.copyFlipTimes(flip);
        completedFlip.setFlipCompletedAt(System.currentTimeMillis());
        itemSelectionAgent.addCompletedFlip(completedFlip);

        Flip newflip = new Flip(collection.getItems(), flip.getBuyPrice(), flip.getSellPrice());
        newflip.copyFlipTimes(flip);
        flip = newflip;

        return true;
    }


    /*
     * Below methods are all sub-strategies for our item strategy. They should all return a boolean value if action is
     * actually performed. This should be false in the case of failure rather than because conditions are not right for
     * the given sub-strategy (e.g. cannot find suitable margins and so do not commence flip).
     *
     * Can be overridden for non-default behaviour.
     */

    protected abstract boolean handlePCQueued();

    protected void resetItem() {
        slot = -1;
        flip = null;
        state = IDLE;
    }

    protected boolean handleBuyQueued() {
        if(ge.getAvailableItemAmount(item) > 0 || ge.getAvailableItemAmount(item) == -1) {
            if(placeFlipBuyOffer()) {
                state = ItemState.BUYING;
            }
        } else {
            notifyBadItem();
            return false;
        }
        return true;
    }

    protected boolean handleBuying() {
        if(ge.isOfferCompleted(slot)) {
            if(collectBuyOffer()) {
                state = ItemState.BOUGHT;
            }
            return true;
        } else if((flip.getMaxOfferTime() != -1
                && System.currentTimeMillis() > flip.getBuyOfferPlacedAt() + flip.getMaxOfferTime()
                || !itemMargin.isMinimumValid())
                && cancelFlipBuyOffer()) {
            if(getQuantityOfItemsHeld() > 0) {
                if(itemMargin.isMaximumValid()) {
                    state = BOUGHT;
                    return true;
                }
            }
            state = PC_QUEUED;
            return true;
        }
        return false;
    }

    protected boolean handleBought() {
        if(placeFlipSellOffer()) {
            state = ItemState.SELLING;
        }
        return true;
    }

    protected boolean handleSelling() {
        if(ge.isOfferCompleted(slot)) {
            if(collectFlipSellOffer()) {
                state = SOLD;
            }
        } else if((flip.getMaxOfferTime() != -1
                && System.currentTimeMillis() > flip.getSellOfferPlacedAt() + flip.getMaxOfferTime()
                || !itemMargin.isMinimumValid())
                && cancelFlipSellOffer()) {
            state = PC_QUEUED;
            return true;
        }
        return false;
    }

    protected boolean handleSold() {
        itemSelectionAgent.addCompletedFlip(flip);
        resetItem();
        return true;
    }

    void notifyBadItem() {
        scriptData.getItemRestrictions().get(item).notifyBadFlip();
        resetItem();
    }

    // Represents all possible flip states for each item. PC = price check
    protected enum ItemState {
        IDLE("Idle"),
        PC_QUEUED("Attempting to perform price check"),
        BUY_QUEUED("Attempting to place buy offer for new flip"),
        BUYING("Buying for flip in progress"),
        BOUGHT("Attempting to sell bought items for flip"),
        SELLING("Selling for flip in progress"),
        SOLD("Finished flip!");

        private String message;

        ItemState(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public Item getItem() {
        return item;
    }

    public int getAvailableGold() {
        return scriptData.getAbstractScript().getInventory().count(995);
    }

    public int getUndercutPercentage() {
        return undercutPercentage;
    }
}
