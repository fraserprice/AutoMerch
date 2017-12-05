package ai_merchant.agents;

import ai_merchant.ge.*;
import ai_merchant.ge.slots.BuyOfferSlot;
import ai_merchant.ge.slots.SellOfferSlot;
import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/*
    Agent provides performAction() which returns implements an offer based upon the state of the GrandExchange and
    user's available gold.
    Agent should have a concrete strategy for flipping, and should keep track of available funds + items.
    performAction() should do one of the following:
        - Place buy offer
        - Place sell offer
        - Collect finished offer
        - Cancel + collect existing offer
 */
public abstract class Agent {
    protected GrandExchange ge;
    protected Queue<Item> itemList;
    protected int availableGold;
    protected PriceChecker pc;
    protected int usableSlots;
    protected Map<BuyOfferSlot, Flip> buyingFlips;
    protected Map<SellOfferSlot, Flip> sellingFlips;
    protected List<Flip> completedFlips;

    public Agent(GrandExchange ge, Queue<Item> itemList, int availableGold, PriceChecker pc, int usableSlots) {
        this.ge = ge;
        this.itemList = itemList;
        this.availableGold = availableGold;
        this.pc = pc;
        this.usableSlots = usableSlots;
    }

    public abstract void performAction();
    public abstract boolean actionWaiting();

    public int getAvailableGold() {
        return availableGold;
    }

    protected Queue<Item> getWaitingItems() {
        Queue<Item> waitingItems = new LinkedList<>(itemList);
        for(Flip flip : buyingFlips.values()) {
            waitingItems.remove(flip.getItemSet().getItem());
        }
        for(Flip flip : sellingFlips.values()) {
            waitingItems.remove(flip.getItemSet().getItem());
        }
        // TODO: Check ge buy limits
        return waitingItems;
    };

    protected Pair<Integer, Integer> checkMarginsOnGe(Item item) {
        return new Pair<>(0,0);
    }

    protected void placeFlipBuyOffer(Flip flip) {

    }

    // Collect finished buy offer and sell according to Flip object. Assumes offer is finished.
    protected void collectFinishedBuyOfferAndSell(BuyOfferSlot finishedBuyOffer) {
        OfferCollection collection = ge.collectOffer(finishedBuyOffer);
        Flip flip = buyingFlips.get(finishedBuyOffer);
        flip.setBuyPrice(flip.getBuyPrice() - collection.getGold() / flip.getItemSet().getItemAmount());
        SellOfferSlot sellOffer = ge.placeSellOffer(flip);
        flip.setSellOfferPlacedAt(System.currentTimeMillis());
        buyingFlips.remove(finishedBuyOffer);
        sellingFlips.put(sellOffer, flip);
        availableGold += collection.getGold();
    }

    protected OfferCollection collectFinishedSellOffer(SellOfferSlot finishedSellOffer) {
        OfferCollection collection = ge.collectOffer(finishedSellOffer);
        Flip flip = sellingFlips.get(finishedSellOffer);
        flip.setSellPrice(collection.getGold() / collection.getItems().getItemAmount());
        flip.setFlipCompletedAt(System.currentTimeMillis());
        sellingFlips.remove(finishedSellOffer);
        completedFlips.add(flip);
        availableGold += collection.getGold();
    }
}
