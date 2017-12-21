package state.ge;

import state.ge.limit.LimitTracker;
import state.ge.slots.*;

import java.util.*;

public class GrandExchange {

    // TODO: Refactor me
    private Set<AvailableSlot> availableSlots;
    private Set<BuyOfferSlot> buyOfferSlots;
    private Set<SellOfferSlot> sellOfferSlots;
    private Set<OfferSlot> allOfferSlots;
    private int numberOfSlots;
    private LimitTracker limitTracker;

    public GrandExchange(LimitTracker limitTracker, int numberOfSlots) {
        this.limitTracker = limitTracker;
        this.numberOfSlots = numberOfSlots;
        // TODO: initialise slots
    }

    public int getAvailableItemAmount(Item item) {
        return limitTracker.getAvailableAmount(item);
    }

    // Return BuyOfferSlot of given item; null if item is not being bought currently
    public Flip getOngoingFlip(Item item) {
        for(OfferSlot offerSlot : allOfferSlots) {
            if(offerSlot.getFlip().getItemSet().getItem().equals(item)) {
                return offerSlot.getFlip();
            }
        }
        return null;
    }

    public boolean offerIsCompleted(Flip flip) {
        OfferSlot slot = getFlipOfferSlot(flip);
        if(slot != null) {
            return slot.isFinished();
        }
        return false;
    }

    public int availableSlotCount() {
        return availableSlots.size();
    }

    public void placeBuyOffer(Flip flip) {
        if(availableSlotCount() > 0) {
            AvailableSlot as = getAvailableSlot();
            BuyOfferSlot bs = new BuyOfferSlot(as.getPosition(), flip);
            // TODO: Place offer
            availableSlots.remove(as);
            buyOfferSlots.add(bs);
            allOfferSlots.add(bs);
        }
    }

    public void placeSellOffer(Flip flip) {
        if(availableSlotCount() > 0) {
            AvailableSlot as = getAvailableSlot();
            SellOfferSlot ss = new SellOfferSlot(as.getPosition(), flip);
            // TODO: Place offer
            availableSlots.remove(as);
            sellOfferSlots.add(ss);
            allOfferSlots.add(ss);
        }
    }

    // Collect flip offer if offer is completed, otherwise cancel offer and collect
    public OfferCollection collectOffer(Flip flip) {
        OfferSlot flipOfferSlot = getFlipOfferSlot(flip);
        if(flipOfferSlot != null) {
            OfferCollection offerCollection;
            if(flipOfferSlot.isFinished()) {
                //TODO: Collect offer
                offerCollection = new OfferCollection(1, new ItemSet(new Item("xd"), 20));
            } else {
                // TODO: Cancel offer
                offerCollection = new OfferCollection(1, new ItemSet(new Item("xd"), 20));
            }

            boolean isBuyOffer = isBuySlot(flipOfferSlot);
            allOfferSlots.remove(flipOfferSlot);
            if(isBuyOffer) {
                buyOfferSlots.remove(flipOfferSlot);
            } else {
                sellOfferSlots.remove(flipOfferSlot);
            }
            availableSlots.add(new AvailableSlot(flipOfferSlot.getPosition()));

            if(isBuyOffer) {
                limitTracker.addBuyTransaction(offerCollection.getItems());
            }
            return offerCollection;
        }
        return null;
    }

    private boolean isBuySlot(OfferSlot offerSlot) {
        return offerSlot.getClass().equals(BuyOfferSlot.class);
    }

    private AvailableSlot getAvailableSlot() {
        for(AvailableSlot availableSlot : availableSlots) {
            return availableSlot;
        }
        return null;
    }

    private OfferSlot getFlipOfferSlot(Flip flip) {
        for(OfferSlot offerSlot : allOfferSlots) {
            if(offerSlot.getFlip().equals(flip)) {
                return offerSlot;
            }
        }
        return null;
    }

}
