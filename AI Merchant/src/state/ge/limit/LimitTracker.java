package state.ge.limit;

import state.ge.Item;
import state.ge.ItemSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LimitTracker {
    private Map<String, TransactionHistory> itemsBuyHistory;
    private Map<String, Integer> itemLimits;

    public LimitTracker(List<Item> items) {
        this.itemsBuyHistory = items.stream().collect(Collectors.toMap(Item::getItemName, i -> (new TransactionHistory())));
        try {
            BufferedReader br = new BufferedReader(new FileReader("Limits.csv"));
            String line;
            while((line = br.readLine()) != null) {
                String[] itemLimit = line.split(",");
                itemLimits.put(itemLimit[0], Integer.parseInt(itemLimit[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void trackNewItem(Item item) {
        trackNewItem(item.getItemName());
    }

    public void trackNewItem(String itemName) {
        itemsBuyHistory.put(itemName, new TransactionHistory());
    }

    public int getAvailableAmount(Item item) {
        return getAvailableAmount(item.getItemName());
    }

    public int getAvailableAmount(String itemName) {
        long MILLIS_IN_4H = 14400000;
        int amountBoughtIn4h = itemsBuyHistory.get(itemName).getAmountBought(MILLIS_IN_4H);
        int itemLimit = itemLimits.get(itemName);
        return itemLimit - amountBoughtIn4h;
    }

    public void addBuyTransaction(ItemSet items) {
        itemsBuyHistory.get(items.getItem().getItemName()).addTransaction(
                new Transaction(System.currentTimeMillis(), items.getItemAmount()));
    }

    private class Transaction {
        private final long time;
        private final int amount;

        public Transaction(long time, int amount) {

            this.time = time;
            this.amount = amount;
        }

        public long getTime() {
            return time;
        }

        public int getAmount() {
            return amount;
        }
    }

    private class TransactionHistory {
        private List<Transaction> transactions = new ArrayList<>();

        public void addTransaction(Transaction transaction) {
            transactions.add(transaction);
        }

        public int getAmountBought(long timePeriod) {
            return transactions.stream().filter(t -> t.getTime() > System.currentTimeMillis() - timePeriod)
                    .mapToInt(Transaction::getAmount).sum();
        }
    }
}

