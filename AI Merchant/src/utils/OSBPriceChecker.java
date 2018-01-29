package utils;

import state.ge.utils.Margin;
import state.ge.items.Item;
import state.ge.items.ItemStatistics;
import state.ge.items.ItemStatisticsBuilder;

import java.sql.Time;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OSBPriceChecker {
    private static final String OSBUDDY_API_URL = "https://api.rsbuddy.com/grandExchange?a=graph&g=30&i=";

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
            int min = Math.min(currentStatistics.getBuyingPrice(), currentStatistics.getSellingPrice());
            int max = Math.max(currentStatistics.getBuyingPrice(), currentStatistics.getSellingPrice());
            Margin margin = new Margin(min, max);
            margin.setValidUntil(TimeScheduler.getNextOSBUpdateTime());
            return margin;
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
        ItemStatisticsBuilder isb = new ItemStatisticsBuilder();
        String[] variableNames = {
                "ts",
                "buyingPrice",
                "buyingCompleted",
                "sellingPrice",
                "sellingCompleted",
                "overallPrice",
                "overallCompleted"
        };
        Map<StatisticParameter, String> matches = Arrays.stream(StatisticParameter.values())
                .collect(Collectors.toMap(
                        sp -> sp,
                        sp -> Pattern.compile(OSBPriceChecker.getJsonRegexPattern(sp.getKey())).matcher(json)))
                .entrySet().stream().filter(entry -> entry.getValue().find())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().group(1)));
        for(Map.Entry<StatisticParameter, String> match : matches.entrySet()) {
            StatisticParameter sp = match.getKey();
            String valueString = match.getValue();
            sp.setParameter(isb, valueString);
        }
        return isb.build();
    }

    private static String getJsonRegexPattern(String variableName) {
        return "\"" + variableName + "\":(\\d+),";
    }

    private enum StatisticParameter {
        TS("ts"), BUYING_PRICE("buyingPrice"), BUYING_COMPLETED("buyingCompleted"), SELLING_PRICE("sellingPrice"),
        SELLING_COMPLETED("sellingCompleted"), OVERALL_PRICE("overallPrice"), OVERALL_COMPLETD("overallCompleted");

        private String key;

        StatisticParameter(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setParameter(ItemStatisticsBuilder isb, String valueString) {
            switch (key) {
                case "ts":
                    isb.setTs(Long.parseLong(valueString));
                    break;
                case "buyingPrice":
                    isb.setBuyingPrice(Integer.parseInt(valueString));
                    break;
                case "buyingCompleted":
                    isb.setBuyingCompleted(Integer.parseInt(valueString));
                    break;
                case "sellingPrice":
                    isb.setSellingPrice(Integer.parseInt(valueString));
                    break;
                case "sellingCompleted":
                    isb.setSellingCompleted(Integer.parseInt(valueString));
                    break;
                case "overallPrice":
                    isb.setOverallPrice(Integer.parseInt(valueString));
                    break;
                case "overallCompleted":
                    isb.setOverallCompleted(Integer.parseInt(valueString));
            }

        }
    }
}
