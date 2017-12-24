package state.ge.items;

public class ItemRestrictions {
    private double maxPriceCheckBuyMultiplier = 1.5;
    private double maxPriceCheckSellMultiplier = 0.5;
    private long badFlipTimeout = 1800000; // Timeout of this item if bad flip
    private long nextValidTime = -1; // Next valid time if we have timeout. -1 if no timeout.

    public void notifyBadFlip() {
        nextValidTime = System.currentTimeMillis() + badFlipTimeout;
    }

    public double getMaxPriceCheckBuyMultiplier() {
        return maxPriceCheckBuyMultiplier;
    }

    public void setMaxPriceCheckBuyMultiplier(double maxPriceCheckBuyMultiplier) {
        this.maxPriceCheckBuyMultiplier = maxPriceCheckBuyMultiplier;
    }

    public double getMaxPriceCheckSellMultiplier() {
        return maxPriceCheckSellMultiplier;
    }

    public void setMaxPriceCheckSellMultiplier(double maxPriceCheckSellMultiplier) {
        this.maxPriceCheckSellMultiplier = maxPriceCheckSellMultiplier;
    }

    public void setBadFlipTimeout(long badFlipTimeout) {
        this.badFlipTimeout = badFlipTimeout;
    }

    public long getNextValidTime() {
        return nextValidTime;
    }

    public boolean isBadItem() {
        return System.currentTimeMillis() < nextValidTime;
    }
}
