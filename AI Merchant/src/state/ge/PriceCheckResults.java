package state.ge;

public class PriceCheckResults {
    private int price;
    private int slot;
    private PlaceOfferResult result;

    public PriceCheckResults(PlaceOfferResult result, int price, int slot) {
        this.result = result;
        this.price = price;
        this.slot = slot;
    }

    public int getPrice() {
        return price;
    }

    public PlaceOfferResult getOfferResult() {
        return result;
    }

    public int getSlot() {
        return slot;
    }
}
