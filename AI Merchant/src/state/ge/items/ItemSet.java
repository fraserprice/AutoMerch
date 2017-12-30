package state.ge.items;

public class ItemSet {
    private final Item item;
    private final int itemAmount;

    public ItemSet(Item item, int itemAmount) {

        this.item = item;
        this.itemAmount = itemAmount;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public Item getItem() {
        return item;
    }
}
