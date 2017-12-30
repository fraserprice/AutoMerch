package utils;

import state.ge.flips.Margin;
import state.ge.items.Item;
import state.ge.items.ItemStatistics;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OSBPriceChecker {
    private static final String OSBUDDY_API_URL = "https://api.rsbuddy.com/grandExchange?a=graph&g=30&i=";
    private static final int ITEM_STATISTICS_PARAMETER_COUNT = 6;

    public static Queue<ItemStatistics> getRecentItemStatistics(Item item) {
        int itemId = item.getItemId();
        if(itemId == -1) {
            return null;
        }
        String osbRequest = HttpGet.getRequest(OSBUDDY_API_URL + itemId);
        if(osbRequest == null) {
            return null;
        }
        return parseOsbHttpResponse(osbRequest);
    }

    public static ItemStatistics getCurrentItemStatistics(Item item) {
        Queue<ItemStatistics> itemStatistics = getRecentItemStatistics(item);
        if(itemStatistics != null && itemStatistics.size() > 0) {
            return getRecentItemStatistics(item).poll();
        }
        return null;
    }

    // TODO: Regression for prediction?
    public static Margin getCurrentMarginEstimate(Item item) {
        ItemStatistics currentStatistics = getCurrentItemStatistics(item);
        if(currentStatistics != null) {
            return new Margin(currentStatistics.getSellingPrice(), currentStatistics.getBuyingPrice());
        }
        return new Margin();
    }

    private static Queue<ItemStatistics> parseOsbHttpResponse(String response) {
        LinkedList<ItemStatistics> itemStatistics = new LinkedList<>();

        Scanner scanner = new Scanner(response);
        while(scanner.hasNextLine()) {
            itemStatistics.add(parseSingleStatistic(scanner.nextLine()));
        }
        scanner.close();

        Collections.reverse(itemStatistics);
        return itemStatistics;
    }

    private static ItemStatistics parseSingleStatistic(String json) {
        String[] variableNames = {
                "ts",
                "buyingPrice",
                "buyingCompleted",
                "sellingPrice",
                "sellingCompleted",
                "overallPrice",
                "overallCompleted"
        };
        List<Matcher> matchers = Arrays.stream(variableNames)
                .map(OSBPriceChecker::getJsonRegexPattern)
                .map(Pattern::compile)
                .map(pattern -> pattern.matcher(json))
                .collect(Collectors.toList());
        List<String> statisticValues = new LinkedList<>();
        for(Matcher m : matchers) {
            if(m.find()) {
                statisticValues.add(m.group(0));
            } else {
                statisticValues.add("-1");
            }
        }
        Iterator<String> i = statisticValues.iterator();
        Long ts = Long.parseLong(i.next());
        int[] params = new int[ITEM_STATISTICS_PARAMETER_COUNT];
        for(int index = 0; index < ITEM_STATISTICS_PARAMETER_COUNT; index++) {
            params[index] = Integer.parseInt(i.next());
        }
        return new ItemStatistics(ts, params);
    }

    private static String getJsonRegexPattern(String variableName) {
        return "\"" + variableName + "\":(\\d+),";
    }
}
