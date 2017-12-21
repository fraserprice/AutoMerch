package state.ge.slots;

import state.ge.Flip;
import state.ge.Item;
import state.ge.ItemSet;
import state.ge.OfferCollection;

public abstract class OfferSlot extends Slot {
    private final Flip flip;

    public OfferSlot(int position, Flip flip) {
        super(position);
        this.flip = flip;
    }

    public OfferCollection collectOffer() {
        return new OfferCollection(1, new ItemSet(new Item("xd"), 2));
    }

    public void cancelOffer() {

    }

    public boolean isFinished() {
        return true;
    }

    public boolean isCancelled() {
        return true;
    }

    public Flip getFlip() {
        return flip;
    }
}
