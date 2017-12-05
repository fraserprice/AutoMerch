package ai_merchant.ge.slots;

import ai_merchant.ge.Flip;
import ai_merchant.ge.ItemSet;
import ai_merchant.ge.OfferCollection;

public abstract class OfferSlot extends Slot {
    private final Flip flip;

    public OfferSlot(int position, Flip flip) {
        super(position);
        this.flip = flip;
    }

    public OfferCollection collectOffer() {
        return new OfferCollection(1, new ItemSet("xd", 2));
    }

    public void cancelOffer() {

    }

    public boolean isFinished() {
        return true;
    }

    public boolean isCancelled() {
        return true;
    }
}
