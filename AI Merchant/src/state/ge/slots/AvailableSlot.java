package state.ge.slots;

import state.ge.Flip;

public class AvailableSlot extends Slot {
    public AvailableSlot(int position) {
        super(position);
    }

    public BuyOfferSlot placeBuyOffer(Flip flip) {
        return new BuyOfferSlot(position, flip);
    }

    public SellOfferSlot placeSellOffer(Flip flip) {
        return new SellOfferSlot(position, flip);
    }
}
