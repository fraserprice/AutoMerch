package state.ge;

import state.ge.items.ItemSet;

public class Flip {
    private FlipStatus status;
    private ItemSet itemSet;
    private int buyPrice;
    private int sellPrice;
    private long maxOfferTime = -1;
    private long buyOfferPlacedAt = -1;
    private long sellOfferPlacedAt = -1;
    private long flipCompletedAt = -1;

    public Flip(ItemSet itemSet, int buyPrice, int sellPrice) {
        this.itemSet = itemSet;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.status = FlipStatus.INITIALISED;
    }

    public Flip(ItemSet itemSet, int buyPrice, int sellPrice, long maxOfferTime) {
        this.itemSet = itemSet;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.maxOfferTime = maxOfferTime;
        this.status = FlipStatus.INITIALISED;
    }

    public int getTotalBuyPrice() {
        return buyPrice * itemSet.getItemAmount();
    }

    public int getTotalSellPrice() {
        return sellPrice * itemSet.getItemAmount();
    }

    public String getItemName() {
        return this.itemSet.getItem().getItemName();
    }

    public int getItemAmount() {
        return this.itemSet.getItemAmount();
    }

    public int getProfit() {
        return getTotalSellPrice() - getTotalBuyPrice();
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(int sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(int buyPrice) {
        this.buyPrice = buyPrice;
    }

    public ItemSet getItemSet() {
        return itemSet;
    }

    public void setItemSet(ItemSet itemSet) {
        this.itemSet = itemSet;
    }

    public void setBuyOfferPlacedAt(long buyOfferPlacedAt) {
        this.buyOfferPlacedAt = buyOfferPlacedAt;
    }

    public long getBuyOfferPlacedAt() {
        return buyOfferPlacedAt;
    }

    public long getSellOfferPlacedAt() {
        return sellOfferPlacedAt;
    }

    public void setSellOfferPlacedAt(long sellOfferPlacedAt) {
        this.sellOfferPlacedAt = sellOfferPlacedAt;
    }

    public long getFlipCompletedAt() {
        return flipCompletedAt;
    }

    public void setFlipCompletedAt(long flipCompletedAt) {
        this.flipCompletedAt = flipCompletedAt;
    }

    public long getMaxOfferTime() {
        return maxOfferTime;
    }

    public FlipStatus getStatus() {
        return status;
    }

    public void setStatus(FlipStatus status) {
        this.status = status;
    }
}
