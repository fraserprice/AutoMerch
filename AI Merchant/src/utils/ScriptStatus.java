package utils;

public enum ScriptStatus {
    IDLE("Idle"),
    PC_QUEUED("Attempting to perform price check"),
    PC_BUY_QUEUED("Attempting to buy item for price check"),
    PC_BUYING("Buying for price check in progress"),
    PC_BOUGHT("Attempting to sell item for price check"),
    PC_SELLING("Selling for price check in progress"),
    BUY_QUEUED("Attempting to place buy offer for new flip"),
    BUYING("Buying for flip in progress"),
    BOUGHT("Attempting to sell bought items for flip"),
    SELLING("Selling for flip in progress"),
    SOLD("Finished flip!");

    private String message;

    ScriptStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
