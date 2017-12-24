package state.ge;

public class PriceCheckResults {
    private int slot;
    private int price;

    public PriceCheckResults(int slot, int price) {
        this.slot = slot;
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    public int getSlot() {
        return slot;
    }

    public boolean successfullyGotPrice() {
        return price != -1;
    }
}
