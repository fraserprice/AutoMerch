package state.ge.items;

public class ItemStatistics {

    private final long timestamp;
    private final int buyingPrice;
    private final int buyingCompleted;
    private final int sellingPrice;
    private final int sellingCompleted;
    private final int overallPrice;
    private final int overallCompleted;

    public ItemStatistics(long timestamp, int buyingPrice, int buyingCompleted, int sellingPrice, int sellingCompleted,
                          int overallPrice, int overallCompleted) {

        this.timestamp = timestamp;
        this.buyingPrice = buyingPrice;
        this.buyingCompleted = buyingCompleted;
        this.sellingPrice = sellingPrice;
        this.sellingCompleted = sellingCompleted;
        this.overallPrice = overallPrice;
        this.overallCompleted = overallCompleted;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getBuyingPrice() {
        return buyingPrice;
    }

    public int getBuyingCompleted() {
        return buyingCompleted;
    }

    public int getSellingPrice() {
        return sellingPrice;
    }

    public int getSellingCompleted() {
        return sellingCompleted;
    }

    public int getOverallPrice() {
        return overallPrice;
    }

    public int getOverallCompleted() {
        return overallCompleted;
    }

}
