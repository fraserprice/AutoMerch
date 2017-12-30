package state.ge.items;

public class ItemStatistics {

    private final long timestamp;
    private final int buyingPrice;
    private final int buyingCompleted;
    private final int sellingPrice;
    private final int sellingCompleted;
    private final int overallPrice;
    private final int overallCompleted;

    // Parameters must be of length 6 and be ordered as defined in ParameterIndices
    public ItemStatistics(long timestamp, int[] parameters) {
        this.timestamp = timestamp;
        this.buyingPrice = parameters[ParameterIndices.BP_INDEX.ordinal()];
        this.buyingCompleted = parameters[ParameterIndices.BC_INDEX.ordinal()];
        this.sellingPrice = parameters[ParameterIndices.SP_INDEX.ordinal()];
        this.sellingCompleted = parameters[ParameterIndices.SC_INDEX.ordinal()];
        this.overallPrice = parameters[ParameterIndices.OP_INDEX.ordinal()];
        this.overallCompleted = parameters[ParameterIndices.OC_INDEX.ordinal()];
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

    private enum ParameterIndices {
        BP_INDEX, BC_INDEX, SP_INDEX, SC_INDEX, OP_INDEX, OC_INDEX
    }

}
