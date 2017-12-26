package state.ai.agents.item_strategies;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.merch_node_agents.MerchAgent;
import state.ge.*;
import state.ge.items.Item;

import static org.dreambot.api.methods.MethodProvider.log;
import static state.ai.agents.item_strategies.ItemStrategy.ItemState.*;
import static state.ge.PlaceOfferResult.FAILED_TO_PLACE_OFFER;
import static state.ge.PlaceOfferResult.OFFER_PLACED;

public class PriceCheckerItemStrategy extends ItemStrategy {

    private int priceEstimate = -1;
    private int currentPCValue = -1;

    public PriceCheckerItemStrategy(AbstractScript abstractScript, MerchAgent merchAgent, Item item,
                                    double buyRatioLimit, double sellRatioLimit) {
        super(abstractScript, merchAgent, item);
    }

    @Override
    protected boolean handlePCBuyQueued() {
        log("--- Attempting to begin price check for " + item.getItemName() + " ---");
        if(currentPCValue > priceEstimate * 2) {
            notifyBadItem();
            return false;
        }
        PriceCheckResults result = ge.placePCBuyOffer(item, merchAgent.getAvailableGold(), currentPCValue);
        log(result.getOfferResult().getMessage());
        if(result.getOfferResult() == OFFER_PLACED) {
            slot = result.getSlot();
            currentPCValue = result.getPrice();
            if(priceEstimate == -1) {
                priceEstimate = (int) (currentPCValue / 1.1);
            }
            state = ItemState.PC_BUYING;
            return true;
        } else if(result.getOfferResult() == FAILED_TO_PLACE_OFFER) {
            return true;
        } else {
            this.restrictions.notifyBadFlip();
            log("------------------------");
            return false;
        }
    }

    @Override
    protected boolean handlePCBuying() {
        if(ge.offerIsCompleted(slot)) {
            int collectionValue = ge.collectPCBuyOffer(slot, currentPCValue);
            if(collectionValue != -1) {
                itemMargin.setMaximum(collectionValue);
                slot = -1;
                currentPCValue = (int) (0.9 * priceEstimate);
                state = ItemState.PC_BOUGHT;
            }
        } else {
            if(ge.cancelPCBuyOffer(slot)) {
                currentPCValue = Math.max((int) (1.1 * currentPCValue), currentPCValue + 1);
                slot = -1;
                state = PC_BUY_QUEUED;
            }
        }
        return true;
    }

    @Override
    protected boolean handlePCBought() {
        log("--- Attempting to begin price check for " + item.getItemName() + " ---");
        if(currentPCValue < priceEstimate * 0.5) {
            notifyBadItem();
            return false;
        }
        PriceCheckResults result = ge.placePCSellOffer(item, currentPCValue);
        log(result.getOfferResult().getMessage());
        if(result.getOfferResult() == OFFER_PLACED) {
            slot = result.getSlot();
            currentPCValue = result.getPrice();
            state = ItemState.PC_SELLING;
        }
        return true;
    }

    @Override
    protected boolean handlePCSelling() {
        if(ge.offerIsCompleted(slot)) {
            int collectionValue = ge.collectPCBuyOffer(slot, currentPCValue);
            if(collectionValue != -1) {
                itemMargin.setMinimum(collectionValue);
                slot = -1;
                currentPCValue = -1;
                state = ItemState.BUY_QUEUED;
            }
        } else {
            if(ge.cancelPCBuyOffer(slot)) {
                currentPCValue = Math.min((int) (0.9 * currentPCValue), currentPCValue - 1);
                slot = -1;
                state = PC_BOUGHT;
            }
        }
        return true;
    }

    @Override
    protected boolean handleBuyQueued() {
        if(ge.getAvailableItemAmount(item) > 0) {
            if(placeBuyOffer()) {
                state = BUYING;
            }
        } else {
            notifyBadItem();
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleBuying() {
        if(ge.offerIsCompleted(slot)) {
            if(collectBuyOffer()) {
                state = BOUGHT;
            }
        } else if(System.currentTimeMillis() > flip.getBuyOfferPlacedAt() + flip.getMaxOfferTime()) {
            // TODO: Handle timeout
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleBought() {
        if(placeSellOffer()) {
            state = SELLING;
        }
        return true;
    }

    @Override
    protected boolean handleSelling() {
        if(ge.offerIsCompleted(slot)) {
            if(collectSellOffer()) {
                state = SOLD;
            }
        } else if(System.currentTimeMillis() > flip.getSellOfferPlacedAt() + flip.getMaxOfferTime()) {
            // TODO: Handle timeout
            return false;
        }
        return true;
    }

    @Override
    protected boolean handleSold() {
        merchAgent.addCompletedFlip(flip);
        resetItem();
        return true;
    }

    private void resetItem() {
        slot = -1;
        priceEstimate = -1;
        currentPCValue = -1;
        flip = null;
        state = IDLE;
    }

    private void notifyBadItem() {
        restrictions.notifyBadFlip();
        resetItem();
    }

}
