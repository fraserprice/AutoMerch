package services;

public enum PriceCheckerEndpoint {
    OSRS, OSBUDDY, GE_TRACKER;

    public String getEndpointUrl() {
        switch(this) {
            case OSRS:
                return "";
            case GE_TRACKER:
                return "";
            case OSBUDDY:
                return "";
            default:
                return "";
        }
    }
}
