package state.ge.utils;

public enum PlaceOfferResult {
    MARKET_PRICE_CHECKED("Checked item's market price"),
    OFFER_PLACED("Successfully placed offer!"),
    FAILED_TO_PLACE_OFFER("Failed to place offer"),
    TOO_EXPENSIVE("Could not place offer; item too expensive");

    private String message;

    PlaceOfferResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
