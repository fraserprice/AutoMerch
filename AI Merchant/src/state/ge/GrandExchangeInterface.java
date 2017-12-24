package state.ge;

import org.dreambot.api.Client;
import org.dreambot.api.methods.MethodContext;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.MethodProvider;

import org.dreambot.api.methods.grandexchange.Status;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import state.ge.items.Item;
import state.ge.items.ItemSet;
import state.ge.limit.LimitTracker;

import java.util.*;

// All public methods should be safe to perform alone (i.e. correctly handle ge interfaces/widgets). No widget/interface
// checking should be required by caller; high level abstraction es bueno!
public class GrandExchangeInterface {

    private static final int COINS_ID = 995;

    private final Set<Integer> enabledSlots = new HashSet<>();
    private final LimitTracker limitTracker = new LimitTracker();
    private final GrandExchange dreambotGe = new GrandExchange(Client.getClient());
    private final Widgets widgets = new Widgets(Client.getClient());

    public GrandExchangeInterface(int numberOfSlots) {
        constructGe(numberOfSlots);
    }

    private void constructGe(int numberOfSlots) {
        if(openExchangeInterface()) {
            List<GrandExchangeItem> geItems = Arrays.asList(dreambotGe.getItems());
            int availableSlotCount = 0;
            for(GrandExchangeItem geItem : geItems) {
                if(dreambotGe.isSlotEnabled(geItem.getSlot()) && geItem.getStatus() == Status.EMPTY) {
                    enabledSlots.add(geItem.getSlot());
                    availableSlotCount++;
                    if(availableSlotCount == numberOfSlots) {
                        break;
                    }
                }
            }
        } else {
            constructGe(numberOfSlots);
        }

    }

    // Open general ge interface and return all items in form of GrandExchangeItems
    private List<GrandExchangeItem> getGrandExchangeItems() {
        if(openExchangeInterface()) {
            List<GrandExchangeItem> grandExchangeItems = new ArrayList<>();
            MethodProvider.sleepUntil(geOpen(), 5000);
            if(geOpen().verify()) {
                for(GrandExchangeItem grandExchangeItem : dreambotGe.getItems()) {
                    if(enabledSlots.contains(grandExchangeItem.getSlot())) {
                        grandExchangeItems.add(grandExchangeItem);
                    }
                }
                return grandExchangeItems;
            }
        }
        return getGrandExchangeItems();
    }

    // As above but return single item in specified slot
    private GrandExchangeItem getGrandExchangeItem(int slot) {
        for(GrandExchangeItem grandExchangeItem : getGrandExchangeItems()) {
            if(grandExchangeItem.getSlot() == slot) {
                return grandExchangeItem;
            }
        }
        return null;
    }

    // Return current max buying quantity for a given item. -1 for no-limit items
    public int getAvailableItemAmount(Item item) {
        return limitTracker.getAvailableAmount(item);
    }

    // Returns first slot containing item; -1 if ge does not contain offer for item
    public int getItemSlot(Item item) {
        for(GrandExchangeItem grandExchangeItem : getGrandExchangeItems()) {
            if(grandExchangeItem.getStatus() != Status.EMPTY
                    && grandExchangeItem.getName().equals(item.getItemName())) {
                return grandExchangeItem.getSlot();
            }
        }
        return -1;
    }

    // Count available slots
    public int availableSlotCount() {
        return (int) getGrandExchangeItems().stream().filter(i -> i.getStatus() == Status.EMPTY).count();
    }

    // Place buy offer for a given slot, and return slot number. -1 if unsuccessful
    public int placeBuyOffer(Flip flip) {
        if(availableSlotCount() > 0) {
            int slotNumber = dreambotGe.getFirstOpenSlot();
            if(dreambotGe.buyItem(flip.getItemSet().getItem().getItemName(), flip.getItemSet().getItemAmount(), flip.getBuyPrice())) {
                MethodProvider.sleepUntil(exchangeScreenOpen(), 1000);
                return slotNumber;
            }
        }
        return -1;
    }

    // Place sell offer for a given slot, and return slot number. -1 if unsuccessful
    public int placeSellOffer(Flip flip) {
        if(availableSlotCount() > 0) {
            int slotNumber = dreambotGe.getFirstOpenSlot();
            if(dreambotGe.sellItem(flip.getItemSet().getItem().getItemName(), flip.getItemSet().getItemAmount(), flip.getSellPrice())) {
                MethodProvider.sleepUntil(exchangeScreenOpen(), 1000);
                return slotNumber;
            }
        }
        return -1;
    }

    // Cancel offer and collect items. Returns null if unsuccessful
    public OfferCollection cancelOffer(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        if(grandExchangeItem != null && grandExchangeItem.getStatus() != Status.EMPTY) {
            if(dreambotGe.cancelOffer(slot)) {
                return collectOffer(grandExchangeItem.getSlot());
            }
        }
        return null;
    }

    // Collect any items in offer slot; does not require offer to be finished. Returns null if unsuccessful
    public OfferCollection collectOffer(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        if(grandExchangeItem != null && grandExchangeItem.getStatus() != Status.EMPTY) {
            if(openSlotInterface(slot)) {
                WidgetChild itemStack = GrandExchangeWidgets.COLLECTION_SQUARE_1.getWidgetChild(widgets);
                WidgetChild coins = GrandExchangeWidgets.COLLECTION_SQUARE_2.getWidgetChild(widgets);
                if(itemStack != null) {
                    int numberOfItems = 0;
                    int numberOfCoins = 0;
                    if(itemStack.getItemId() != COINS_ID && itemStack.getActions() != null) {
                        numberOfItems = itemStack.getItemStack();
                        if(Arrays.asList(itemStack.getActions()).contains("Collect-note")) {
                            itemStack.interact("Collect-note");
                        } else {
                            itemStack.interact("Collect");
                        }
                        if(coins != null && coins.getActions() != null) {
                            numberOfCoins = coins.getItemStack();
                            coins.interact("Collect");
                        }
                    } else {
                        numberOfCoins = itemStack.getItemStack();
                        itemStack.interact("Collect");
                    }

                    OfferCollection offerCollection = new OfferCollection(numberOfCoins,
                            new ItemSet(new Item(grandExchangeItem.getName()), numberOfItems));

                    if(grandExchangeItem.getStatus() == Status.BUY || grandExchangeItem.getStatus() == Status.BUY_COLLECT) {
                        limitTracker.addBuyTransaction(offerCollection.getItems());
                    }
                    return offerCollection;
                }
                return null;
            }
        }
        return null;
    }

    public PriceCheckResults checkUpperItemPriceIteration(Item item, double buyMultiplier, int slot) {
        int buyPrice = -1;
        MethodProvider.log("STARTING BUY LOOP");
        slot = cancelSlotIfOfferExists(slot);

        if(!dreambotGe.isBuyOpen() && !dreambotGe.slotContainsItem(slot)) {
            dreambotGe.openBuyScreen(slot);
            MethodProvider.sleepUntil(buyScreenOpen(), 3000);
        }

        if(dreambotGe.isBuyOpen() && dreambotGe.getCurrentChosenItem() == null) {
            dreambotGe.addBuyItem(item.getItemName());
            MethodProvider.sleepUntil(itemChosen(item), 3000);
        }

        if(dreambotGe.isBuyOpen() && dreambotGe.getCurrentChosenItem() != null
                && dreambotGe.getCurrentChosenItem().getName().equals(item.getItemName())) {
            int priceEstimate = dreambotGe.getCurrentPrice();
            buyPrice = Math.max((int) (priceEstimate * buyMultiplier), (int) (priceEstimate + (buyMultiplier - 1) / 0.1));
            dreambotGe.buyItem(item.getItemName(), 1, buyPrice);
            MethodProvider.sleepUntil(exchangeScreenOpen(), 3000);
        }
        MethodProvider.sleepUntil(offerCompleted(slot), 3000);

        if(offerIsCompleted(slot)) {
            OfferCollection collection = collectOffer(slot);
            MethodProvider.sleepUntil(exchangeScreenOpen(), 2000);
            return new PriceCheckResults(slot, collection == null ? -1 : buyPrice - collection.getGold());
        } else {
            return new PriceCheckResults(slot, -1);
        }
    }

    public PriceCheckResults checkLowerItemPriceIteration(Item item, double sellMultiplier, int slot) {
        slot = cancelSlotIfOfferExists(slot);

        if(!dreambotGe.isSellOpen() && !dreambotGe.slotContainsItem(slot)) {
            dreambotGe.openSellScreen(slot);
            MethodProvider.sleepUntil(sellScreenOpen(), 3000);
        }

        if(dreambotGe.isSellOpen() && dreambotGe.getCurrentChosenItem() == null) {
            dreambotGe.addSellItem(item.getItemName());
            MethodProvider.sleepUntil(itemChosen(item), 3000);
        }

        if(dreambotGe.isSellOpen() && dreambotGe.getCurrentChosenItem() != null
                && dreambotGe.getCurrentChosenItem().getName().equals(item.getItemName())) {
            int priceEstimate = dreambotGe.getCurrentPrice();
            int sellPrice = Math.min((int) (priceEstimate * sellMultiplier), (int) (priceEstimate + (sellMultiplier - 1) / 0.1));
            sellPrice =  sellPrice < 1 ? 1 : sellPrice;
            dreambotGe.sellItem(item.getItemName(), 1, sellPrice);
            MethodProvider.sleepUntil(exchangeScreenOpen(), 3000);
        }
        MethodProvider.sleepUntil(offerCompleted(slot), 3000);

        if(offerIsCompleted(slot)) {
            OfferCollection collection = collectOffer(slot);
            MethodProvider.sleepUntil(exchangeScreenOpen(), 2000);
            return new PriceCheckResults(slot, collection == null ? -1 : collection.getGold());
        } else {
            return new PriceCheckResults(slot, -1);
        }
    }

    // Reduction of duplication
    private int cancelSlotIfOfferExists(int slot) {
        if(slot != -1) {
            GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
            if(grandExchangeItem != null && grandExchangeItem.getStatus() != Status.EMPTY && !dreambotGe.isReadyToCollect(slot)) {
                cancelOffer(slot);
                MethodProvider.sleepUntil(slotEmpty(slot), 3000);
            }
            return slot;
        } else {
            return dreambotGe.getFirstOpenSlot();
        }

    }

    private boolean openExchangeInterface() {
        if(!isExchangeScreenOpen()) {
            if(dreambotGe.isOpen()) {
                if(isOfferScreenOpen()) {
                    WidgetChild backButton = GrandExchangeWidgets.BACK_BUTTON.getWidgetChild(widgets);
                    backButton.interact("Back");
                    MethodProvider.sleepUntil(exchangeScreenOpen(), 2000);
                    return isExchangeScreenOpen();
                } else if(dreambotGe.close()) {
                    return dreambotGe.open();
                } else {
                    return openExchangeInterface();
                }
            } else {
                return dreambotGe.open();
            }
        } else {
            return true;
        }
    }

    private boolean openSlotInterface(int slot) {
        if(!dreambotGe.openSlotInterface(slot)) {
            if(openExchangeInterface()) {
                return dreambotGe.openSlotInterface(slot);
            } else {
                return openSlotInterface(slot);
            }
        } else {
            return true;
        }
    }

    private boolean isExchangeScreenOpen() {
        WidgetChild collectWidget = GrandExchangeWidgets.COLLECT_BUTTON.getWidgetChild(widgets);
        return collectWidget != null && collectWidget.isVisible();
    }

    private boolean isOfferScreenOpen() {
        WidgetChild backButton = GrandExchangeWidgets.BACK_BUTTON.getWidgetChild(widgets);
        return backButton != null && backButton.isVisible();
    }

    public boolean offerIsCompleted(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        return grandExchangeItem != null && grandExchangeItem.isReadyToCollect();
    }

    private Condition exchangeScreenOpen() {
        WidgetChild collectWidget = GrandExchangeWidgets.COLLECT_BUTTON.getWidgetChild(widgets);
        return () -> collectWidget != null && collectWidget.isVisible();
    }

    private Condition geOpen() {
        return dreambotGe::isOpen;
    }

    private Condition slotEmpty(int slot) {
        return () -> !dreambotGe.slotContainsItem(slot);
    }

    private Condition offerCompleted(int slot) {
        return () -> offerIsCompleted(slot);
    }

    private Condition buyScreenOpen() {
        return dreambotGe::isBuyOpen;
    }

    private Condition sellScreenOpen() {
        return dreambotGe::isSellOpen;
    }

    private Condition itemChosen(Item item) {
        return () -> dreambotGe.getCurrentChosenItem() != null
                && dreambotGe.getCurrentChosenItem().getName().equals(item.getItemName());
    }

    private enum GrandExchangeWidgets {
        BACK_BUTTON(465, 4),
        COLLECT_BUTTON(465, 6, 0),
        COLLECTION_SQUARE_1(465, 23, 2),
        COLLECTION_SQUARE_2(465, 23, 3);

        private int[] identifiers;

        GrandExchangeWidgets(int... identifiers) {
            this.identifiers = identifiers;
        }

        WidgetChild getWidgetChild(Widgets widgets) {
            return widgets.getWidgetChild(identifiers);
        }
    }

}

