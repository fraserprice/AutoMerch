package services;

import javafx.util.Pair;
import state.ge.items.Item;

public class PriceChecker {
    private PriceCheckerEndpoint endpoint;
    private String endpointUrl = "";

    public PriceChecker(PriceCheckerEndpoint endpoint) {
        this.endpoint = endpoint;
        this.endpointUrl = endpoint.getEndpointUrl();
    }

    public int getCurrentPriceEstimate(Item item) {
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
