package ai_merchant.agents;

import ai_merchant.ge.*;
import ai_merchant.ge.slots.BuyOfferSlot;
import ai_merchant.ge.slots.SellOfferSlot;
import javafx.util.Pair;

import java.util.Queue;

public class PriceCheckerAgent extends Agent {

    public PriceCheckerAgent(GrandExchange ge, Queue<Item> itemList, int availableGold, PriceChecker pc, int usableSlots) {
        super(ge, itemList, availableGold, pc, usableSlots);
    }

    private Item getNextItemToFlip() {
        for(Item i : getWaitingItems()) {
            if(pc.getCurrentPriceEstimate(i) < availableGold) {
                return i;
            }
        }
        return null;
    }

    @Override
    public void performAction() {
        // Collect finished sell offer
        SellOfferSlot finishedSellOffer = ge.getNextFinishedSellOffer();
        if(finishedSellOffer != null) {
            collectFinishedSellOffer(finishedSellOffer);
            return;
        }

        // Collect finished buy offer + place sell offer
        BuyOfferSlot finishedBuyOffer = ge.getNextFinishedBuyOffer();
        if(finishedBuyOffer != null) {
            collectFinishedBuyOfferAndSell(finishedBuyOffer);
            return;
        }

        // Check item buy/sell prices + place buy offer
        Item nextItemToFlip = getNextItemToFlip();
        if(ge.availableSlotCount() > 0 && getWaitingItems().size() > 0 && nextItemToFlip != null) {
            Pair<Integer, Integer> itemMargins = checkMarginsOnGe(nextItemToFlip);
            Flip newFlip = new Flip()
            return;
        }

        // Cancel sell offer, re-check price and place sell offer for lower price
        if() {
            return;
        }

        // Cancel buy offer + place sell offer if any items bought
        if() {
            return;
        }
    }

    @Override
    public boolean actionWaiting() {
        return true;
    }
}
