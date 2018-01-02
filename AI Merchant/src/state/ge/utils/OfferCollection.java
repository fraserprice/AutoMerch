package state.ge.utils;

import state.ge.items.ItemSet;

public class OfferCollection {
    private int gold;
    private ItemSet items;

    public OfferCollection(int gold, ItemSet items) {
        this.gold = gold;
        this.items = items;
    }

    public ItemSet getItems() {
        return items;
    }

    public void setItems(ItemSet items) {
        this.items = items;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
