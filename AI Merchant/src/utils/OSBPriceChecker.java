package utils;

import state.ge.flips.Margin;
import state.ge.items.Item;

public class OSBPriceChecker {
    private static final String OSBUDDY_API_URL = "";

    public static int getCurrentPriceEstimate(Item item) {
        return 0;
    }

    public static Margin getCurrentMarginEstimate(Item item) {
        // Return margin containing avg for upper + lower if we are not using ge tracker
        int priceEstimate = getCurrentPriceEstimate(item);
        return new Margin(priceEstimate, priceEstimate);
    }
}
