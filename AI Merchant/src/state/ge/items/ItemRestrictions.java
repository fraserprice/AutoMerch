package state.ge.items;

public class ItemRestrictions {
    private long badFlipTimeout = 1800000; // Timeout of this item if bad flip
    private long nextValidTime = -1; // Next valid time if we have timeout. -1 if no timeout.

    public void notifyBadFlip() {
        nextValidTime = System.currentTimeMillis() + badFlipTimeout;
    }

    public void setBadFlipTimeout(long badFlipTimeout) {
        this.badFlipTimeout = badFlipTimeout;
    }

    public long getNextValidTime() {
        return nextValidTime;
    }

    public boolean isBadItem() {
        return nextValidTime != -1 && System.currentTimeMillis() > nextValidTime;
    }
}
