package state.ge.utils;

import state.ge.items.ItemSet;

public class Flip {
    private ItemSet itemSet;
    private int buyPrice;
    private int sellPrice;

    private long maxOfferTime = -1;

    private long buyOfferPlacedAt = -1;
    private long buyOfferFinishedAt = -1;
    private long sellOfferPlacedAt = -1;
    private long flipCompletedAt = -1;

    public Flip(ItemSet itemSet, int buyPrice, int sellPrice) {
        this.itemSet = itemSet;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public Flip(ItemSet itemSet, int buyPrice, int sellPrice, long maxOfferTime) {
        this.itemSet = itemSet;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.maxOfferTime = maxOfferTime;
    }

    public void copyFlipTimes(Flip flip) {
        buyOfferPlacedAt = flip.getBuyOfferPlacedAt();
        buyOfferFinishedAt = flip.getBuyOfferFinishedAt();
        sellOfferPlacedAt = flip.getSellOfferPlacedAt();
        flipCompletedAt = flip.getFlipCompletedAt();
    }

    public void setItemAmount(int amount) {
        itemSet = new ItemSet(itemSet.getItem(), amount);
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

    public long getBuyOfferFinishedAt() {
        return buyOfferFinishedAt;
    }

    public void setBuyOfferFinishedAt(long buyOfferFinishedAt) {
        this.buyOfferFinishedAt = buyOfferFinishedAt;
    }
}
