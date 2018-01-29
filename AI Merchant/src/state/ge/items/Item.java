package state.ge.items;

public class Item {
    private String itemName;
    private int itemId;

    public Item(String itemName) {
        this.itemName = itemName;
        this.itemId = ItemIdLookup.getId(itemName);
    }

    public Item(int itemId) {
        this.itemId = itemId;
        this.itemName = ItemIdLookup.getItemName(itemId);
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemId() {
        return itemId;
    }

    @Override
    public String toString() {
        return getItemName();
    }
}
