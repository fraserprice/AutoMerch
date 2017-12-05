package ai_merchant.ge;

/**
 * Class designed to deal with stacks of the same item bought for specific price
 */
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
