package ai_merchant.ge;

import ai_merchant.ge.slots.*;

import java.util.*;
import java.util.stream.Collectors;

public class GrandExchange {

    private List<Slot> slots;

    public GrandExchange() {
        // TODO: initialise slots
    }

    private List<OfferSlot> getFinishedSlots() {
        List<OfferSlot> finishedSlots = new ArrayList<>();
        for(Slot s : slots) {
            if(s.getClass().isAssignableFrom(OfferSlot.class) && ((OfferSlot) s).isFinished()) {
                finishedSlots.add((OfferSlot) s);
            }
        }
        return finishedSlots;
    }

    private AvailableSlot getAvailableSlot() {
        for(Slot s : slots) {
            if(s.getClass() == AvailableSlot.class) {
                return (AvailableSlot) s;
            }
        }
        return null;
    }

    // Return first finished buy offer; null if no offers finished
    public BuyOfferSlot getNextFinishedBuyOffer() {
        for(Slot s : slots) {
            if(s.getClass() == BuyOfferSlot.class && ((BuyOfferSlot) s).isFinished()) {
                return (BuyOfferSlot) s;
            }
        }
        return null;
    }

    // Return first finished sell offer; null if no offers finished
    public SellOfferSlot getNextFinishedSellOffer() {
        for(Slot s : slots) {
            if(s.getClass() == SellOfferSlot.class && ((SellOfferSlot) s).isFinished()) {
                return (SellOfferSlot) s;
            }
        }
        return null;
    }

    public int availableSlotCount() {
        return (int) slots.stream().filter(s -> s.getClass() == AvailableSlot.class).count();
    }

    // TODO: Remove duplication
    public BuyOfferSlot placeBuyOffer(Flip flip) {
        if(availableSlotCount() > 0) {
            AvailableSlot as = getAvailableSlot();
            BuyOfferSlot bs = new BuyOfferSlot(as.getPosition(), flip);
            // TODO: Place offer
            slots.remove(as);
            slots.add(bs);
            return bs;
        }
        return null;
    }

    public SellOfferSlot placeSellOffer(Flip flip) {
        if(availableSlotCount() > 0) {
            AvailableSlot as = getAvailableSlot();
            SellOfferSlot ss = new SellOfferSlot(as.getPosition(), flip);
            // TODO: Place offer
            slots.remove(as);
            slots.add(ss);
            return ss;
        }
        return null;
    }

    // Collects all finished offers
    public Map<OfferSlot, OfferCollection> collectFinishedOffers() {
        List<OfferSlot> finishedSlots = getFinishedSlots();
        List<OfferCollection> collections = finishedSlots.stream()
                .map(this::collectOffer).collect(Collectors.toList());
        Iterator<OfferCollection> collectionsIterator = collections.iterator();
        Map<OfferSlot, OfferCollection> slotCollections = new HashMap<>();
        for(OfferSlot slot : finishedSlots) {
            slotCollections.put(slot, collectionsIterator.next());
            AvailableSlot emptiedSlot = new AvailableSlot(slot.getPosition());
            slots.remove(slot);
            slots.add(emptiedSlot);
        }
        return slotCollections;
    }

    // Collect the desired offer; does not cancel unfinished offers
    public OfferCollection collectOffer(OfferSlot offer) {
        for(Slot s : slots) {
            if(s.equals(offer)) {
                if(offer.isFinished()) {
                    // TODO: Collect offer
                } else {
                    // TODO: Collect offer
                    return new OfferCollection(1, new ItemSet("xd", 20));
                }
            }
        }
        return new OfferCollection(1, new ItemSet("xd", 20));
    }
}
