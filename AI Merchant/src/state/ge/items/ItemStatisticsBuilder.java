package state.ge.items;

public class ItemStatisticsBuilder {

    private long ts;
    private int buyingPrice = -1;
    private int buyingCompleted = -1;
    private int sellingPrice = -1;
    private int sellingCompleted = -1;
    private int overallPrice = -1;
    private int overallCompleted = -1;

    public ItemStatistics build() {
        return new ItemStatistics(ts, buyingPrice, buyingCompleted, sellingPrice, sellingCompleted, overallPrice, overallCompleted);
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public void setBuyingPrice(int buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public void setBuyingCompleted(int buyingCompleted) {
        this.buyingCompleted = buyingCompleted;
    }

    public void setSellingPrice(int sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setSellingCompleted(int sellingCompleted) {
        this.sellingCompleted = sellingCompleted;
    }

    public void setOverallPrice(int overallPrice) {
        this.overallPrice = overallPrice;
    }

    public void setOverallCompleted(int overallCompleted) {
        this.overallCompleted = overallCompleted;
    }
}
