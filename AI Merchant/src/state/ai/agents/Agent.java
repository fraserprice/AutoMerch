package state.ai.agents;

import org.dreambot.api.methods.MethodProvider;

import services.PriceChecker;
import state.ai.Actionable;
import state.ge.*;

import java.util.*;

/*
 * Agent's performAction() method should iterate over waiting item queue, calling performAction() for each item until an
 * action is performed or until waiting items is exhausted.
 */
public abstract class Agent implements Actionable {

    protected GrandExchange ge;

    Map<Item, Actionable> itemStrategies;

    private int availableGold;
    private PriceChecker pc;
    private Queue<Item> itemQueue;
    private Set<Flip> buyingFlips = new HashSet<>();
    private Set<Flip> sellingFlips = new HashSet<>();
    private Set<Flip> completedFlips = new HashSet<>();

    public Agent(GrandExchange ge, Queue<Item> itemList, int availableGold, PriceChecker pc) {
        this.ge = ge;
        this.itemQueue = itemList;
        this.availableGold = availableGold;
        this.pc = pc;
    }

    // Get queue of items waiting to be flipped
    protected Queue<Item> getWaitingItemQueue() {
        Queue<Item> waitingItems = new LinkedList<>(itemQueue);
        for(Flip flip : buyingFlips) {
            waitingItems.remove(flip.getItemSet().getItem());
        }
        for(Flip flip : sellingFlips) {
            waitingItems.remove(flip.getItemSet().getItem());
        }
        for(Item waitingItem : waitingItems) {
            if(ge.getAvailableItemAmount(waitingItem) == 0) {
                waitingItems.remove(waitingItem);
            }
        }
        return waitingItems;
    }


    // Below methods should be utilised by strategies in order to interact with ge

    /* Checks margin of item by buying on ge for high and selling for low.
     * This should (in general) only be done for either low value or high volume items; can be very risky for
     * high value/low volume items.
     * User can specify which items they would like to check price of, along with a maximum and minimum check
     * value.
    */
    public Margin checkMarginsOnGe(Item item) {
        int priceEstimate = pc.getCurrentPriceEstimate(item);
        double buyMultiplier = 1.05;
        double sellMultiplier = 0.95;
        Flip marginTestFlip = new Flip(new ItemSet(item, 1), (int) (priceEstimate * 1.05), (int) (priceEstimate * 0.95));
        do {
            ge.placeBuyOffer(marginTestFlip);
            MethodProvider.sleep(2000); // TODO: Implement properly lol
            buyMultiplier += 0.05;
            marginTestFlip.setBuyPrice((int) (priceEstimate * buyMultiplier));
        } while(!ge.offerIsCompleted(marginTestFlip));
        OfferCollection buyCollection = ge.collectOffer(marginTestFlip);
        do {
            ge.placeSellOffer(marginTestFlip);
            MethodProvider.sleep(2000); // TODO: Implement properly lol
            sellMultiplier -= 0.05;
            marginTestFlip.setSellPrice((int) (priceEstimate * sellMultiplier));
        } while(!ge.offerIsCompleted(marginTestFlip));
        OfferCollection sellCollection = ge.collectOffer(marginTestFlip);
        // TODO: Implement
        return null;
    }

    // Place new buy offer for given flip.
    public void placeFlipBuyOffer(Flip flip) {
        ge.placeBuyOffer(flip);
        flip.setBuyOfferPlacedAt(System.currentTimeMillis());
        flip.setStatus(FlipStatus.BUYING);
        buyingFlips.add(flip);
        availableGold -= flip.getBuyPrice();
    }

    // Place new sell offer for given flip.
    public void placeFlipSellOffer(Flip flip) {
        ge.placeSellOffer(flip);
        flip.setSellOfferPlacedAt(System.currentTimeMillis());
        flip.setStatus(FlipStatus.SELLING);
        sellingFlips.add(flip);
    }

    // Collect finished buying flip. Assumes offer is finished.
    public void collectFinishedBuyingFlip(Flip flip) {
        OfferCollection collection = ge.collectOffer(flip);
        flip.setBuyPrice(flip.getBuyPrice() - collection.getGold() / collection.getItems().getItemAmount());
        flip.setStatus(FlipStatus.BOUGHT);
        buyingFlips.remove(flip);
        availableGold += collection.getGold();
    }

    // Collect finished selling flip. Assumes offer is finished.
    public void collectFinishedSellingFlip(Flip flip) {
        OfferCollection collection = ge.collectOffer(flip);
        flip.setSellPrice(collection.getGold() / collection.getItems().getItemAmount());
        flip.setFlipCompletedAt(System.currentTimeMillis());
        flip.setStatus(FlipStatus.COMPLETED);
        sellingFlips.remove(flip);
        completedFlips.add(flip);
        availableGold += collection.getGold();
    }

    /*
     * Cancel given offer slot. We have two cases:
     *
     * 1. Buy offer:
     *     - Create new flip containing only the bought number of items with correct buy price
     *     - In all agent cases we would like to immediately sell the item rather than persist in buying, hence why we
     *       simply reduce the amount of items bought. This lets us continue to either try to directly sell item, or
     *       alternatively re-check price and sell item.
     *
     * 2. Sell offer:
     *     - Create two new flips.
     *     - i.  Flip containing number of items successfully sold. Add this flip to completed flip list. TODO: Alter buy time?
     *     - ii. Flip containing number of items still unsold. We return this flip so agent can decide what action to
     *           take next.
     */
    public Flip cancelOffer(Flip flip) {
        OfferCollection collection = ge.collectOffer(flip);
        if(buyingFlips.contains(flip)) {
            flip.setItemSet(collection.getItems());
            int offerPrice = flip.getBuyPrice() * flip.getItemSet().getItemAmount();
            int actualBuyValue = offerPrice - collection.getGold();
            flip.setBuyPrice((offerPrice - actualBuyValue) / collection.getItems().getItemAmount());
            flip.setStatus(FlipStatus.BOUGHT);

            buyingFlips.remove(flip);

            return flip;
        } else if(sellingFlips.contains(flip)) {
            int sellPrice = (flip.getItemSet().getItemAmount() - collection.getItems().getItemAmount()) / collection.getGold();
            Flip completedFlip = new Flip(collection.getItems(), flip.getBuyPrice(), sellPrice);
            completedFlip.setFlipCompletedAt(System.currentTimeMillis());
            completedFlip.setStatus(FlipStatus.COMPLETED);

            Flip unsuccessfulFlip = new Flip(collection.getItems(), flip.getBuyPrice(), flip.getSellPrice());
            completedFlip.setStatus(FlipStatus.BOUGHT);

            completedFlips.add(completedFlip);
            sellingFlips.remove(flip);

            return unsuccessfulFlip;
        }
        return null;
    }

    public int getAvailableGold() {
        return availableGold;
    }

    public void setAvailableGold(int availableGold) {
        this.availableGold = availableGold;
    }

    public Set<Flip> getBuyingFlips() {
        return buyingFlips;
    }

    public Set<Flip> getSellingFlips() {
        return sellingFlips;
    }

    public Set<Flip> getCompletedFlips() {
        return completedFlips;
    }

    public Map<Item, Actionable> getItemStrategies() {
        return itemStrategies;
    }

    public PriceChecker getPc() {
        return pc;
    }

    public GrandExchange getGe() {
        return ge;
    }

}
