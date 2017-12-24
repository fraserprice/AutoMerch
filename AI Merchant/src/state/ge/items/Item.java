package state.ge.items;

import state.ge.Margin;

public class Item {
    private String itemName;
    private int itemId;

    public Item(String itemName) {
        this.itemName = itemName;
    }

    public Item(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemId() {
        return itemId;
    }
}
