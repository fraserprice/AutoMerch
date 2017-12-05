package ai_merchant.ge;

import javafx.util.Pair;

public class PriceChecker {
    private final PriceCheckerEndpoint endpoint;
    private final String endpointUrl;

    public PriceChecker(PriceCheckerEndpoint endpoint) {
        this.endpoint = endpoint;
        this.endpointUrl = endpoint.getEndpointUrl();
    }

    public int getCurrentPriceEstimate(Item item) {
        // TODO: Make call to endpoint
        return 0;
    }

    public Pair<Integer, Integer> getCurrentMarginEstimate(Item item) {
        if(this.endpoint == PriceCheckerEndpoint.GE_TRACKER) {
            return new Pair<>(0, 0);
        }

        // Return margin containing avg for upper + lower if we are not using ge tracker
        int priceEstimate = getCurrentPriceEstimate(item);
        return new Pair<>(priceEstimate, priceEstimate);
    }
}
