package state.ai.agents;

import org.dreambot.api.Client;
import org.dreambot.api.methods.MethodProvider;

import org.dreambot.api.methods.container.impl.Inventory;
import services.PriceChecker;
import services.PriceCheckerEndpoint;
import state.ai.Actionable;
import state.ai.item_strategies.ItemStrategy;
import state.ai.item_strategies.PriceCheckerItemStrategy;
import state.ge.*;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;

import java.util.*;

/*
 * Agent's performAction() method should iterate over waiting item queue, calling performAction() for each item until an
 * action is performed or until waiting items is exhausted.
 */
public abstract class Agent implements Actionable {

    protected GrandExchangeInterface ge;
    private PriceChecker pc = new PriceChecker(PriceCheckerEndpoint.GE_TRACKER);

    private Queue<Item> itemQueue;
    Map<Item, ItemStrategy> itemStrategies = new HashMap<>();
    private Map<Item, ItemRestrictions> itemRestrictions = new HashMap<>();

    private Map<Flip, Integer> buyingFlips = new HashMap<>();
    private Map<Flip, Integer> sellingFlips = new HashMap<>();
    private Set<Flip> completedFlips = new HashSet<>();

    public Agent(GrandExchangeInterface ge, Queue<Item> itemQueue, Map<Item, ItemRestrictions> itemRestrictions,
                 PriceChecker pc, Map<Item, ItemStrategy> itemStrategies) {
        this.ge = ge;
        this.itemQueue = itemQueue;
        this.pc = pc;
        for(Item item : itemQueue) {
            if(itemRestrictions.containsKey(item)) {
                this.itemRestrictions.put(item, itemRestrictions.get(item));
            } else {
                this.itemRestrictions.put(item, new ItemRestrictions());
            }
            if(itemStrategies.containsKey(item)) {
                this.itemStrategies.put(item, itemStrategies.get(item));
            } else {
                this.itemStrategies.put(item, new PriceCheckerItemStrategy(this, item, 1.5, 0.5));
            }
        }
    }

    // Get queue of items waiting to be flipped
    public Queue<Item> getWaitingItemQueue() {
        Queue<Item> waitingItems = new LinkedList<>(itemQueue);
        Map<Flip, Integer> allFlips = new HashMap<>(buyingFlips);
        allFlips.putAll(sellingFlips);
        for(Map.Entry<Flip, Integer> entry : allFlips.entrySet()) {
            if(!ge.offerIsCompleted(entry.getValue())) {
                waitingItems.remove(entry.getKey().getItemSet().getItem());
            }
        }
        for(Item waitingItem : waitingItems) {
            if(ge.getAvailableItemAmount(waitingItem) == 0 || itemRestrictions.get(waitingItem).isBadItem()) {
                waitingItems.remove(waitingItem);
            }
        }
        MethodProvider.log("Waiting item queue:");
        for(Item item : waitingItems) {
            MethodProvider.log(item.getItemName());
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
    // TODO: Majorly improve lol

    // Place new buy offer for given flip.
    public void placeFlipBuyOffer(Flip flip) {
        MethodProvider.log("Attempting to make flip buy offer...");
        int slot = ge.placeBuyOffer(flip);
        flip.setBuyOfferPlacedAt(System.currentTimeMillis());
        flip.setStatus(FlipStatus.BUYING);
        buyingFlips.put(flip, slot);
    }

    // Place new sell offer for given flip.
    public void placeFlipSellOffer(Flip flip) {
        int slot = ge.placeSellOffer(flip);
        flip.setSellOfferPlacedAt(System.currentTimeMillis());
        flip.setStatus(FlipStatus.SELLING);
        sellingFlips.put(flip, slot);
    }

    // Collect finished buying flip. Assumes offer is finished.
    public OfferCollection collectFinishedBuyingFlip(Flip flip) {
        OfferCollection collection = ge.collectOffer(buyingFlips.get(flip));
        flip.setBuyPrice(flip.getBuyPrice() - collection.getGold() / collection.getItems().getItemAmount());
        flip.setStatus(FlipStatus.BOUGHT);
        buyingFlips.remove(flip);
        return collection;
    }

    // Collect finished selling flip. Assumes offer is finished.
    public OfferCollection collectFinishedSellingFlip(Flip flip) {
        OfferCollection collection = ge.collectOffer(buyingFlips.get(flip));
        flip.setSellPrice(collection.getGold() / flip.getItemSet().getItemAmount());
        flip.setFlipCompletedAt(System.currentTimeMillis());
        flip.setStatus(FlipStatus.COMPLETED);
        sellingFlips.remove(flip);
        completedFlips.add(flip);
        return collection;
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
        if(buyingFlips.keySet().contains(flip)) {
            OfferCollection collection = ge.collectOffer(buyingFlips.get(flip));
            flip.setItemSet(collection.getItems());
            int offerPrice = flip.getBuyPrice() * flip.getItemSet().getItemAmount();
            int actualBuyValue = offerPrice - collection.getGold();
            flip.setBuyPrice((offerPrice - actualBuyValue) / collection.getItems().getItemAmount());
            flip.setStatus(FlipStatus.BOUGHT);

            buyingFlips.remove(flip);

            return flip;
        } else if(sellingFlips.keySet().contains(flip)) {
            OfferCollection collection = ge.collectOffer(sellingFlips.get(flip));
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

    public Flip getFlipInSlot(int slot) {
        Map<Flip, Integer> allFlips = new HashMap<>(buyingFlips);
        allFlips.putAll(sellingFlips);
        for(Map.Entry<Flip, Integer> entry : allFlips.entrySet()) {
            if(entry.getValue() == slot) {
                return entry.getKey();
            }
        }
        return null;
    }

    public ItemRestrictions getItemRestrictions(Item item) {
        return itemRestrictions.get(item);
    }

    public int getAvailableGold() {
        return new Inventory(Client.getClient()).count(995);
    }

    public GrandExchangeInterface getGe() {
        return ge;
    }


}
