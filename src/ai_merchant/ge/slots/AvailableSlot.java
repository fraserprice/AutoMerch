package ai_merchant.ge.slots;

import ai_merchant.ge.ItemSet;

public class AvailableSlot extends Slot {
    public AvailableSlot(int position) {
        super(position);
    }

    public BuyOfferSlot placeBuyOffer(ItemSet itemSet) {
        return new BuyOfferSlot(position, itemSet);
    }

    public SellOfferSlot placeSellOffer(ItemSet itemSet) {
        return new SellOfferSlot(position, itemSet);
    }
}
