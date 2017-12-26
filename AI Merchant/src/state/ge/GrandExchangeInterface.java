package state.ge;

import org.dreambot.api.Client;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;

import org.dreambot.api.methods.grandexchange.Status;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import state.ge.items.Item;
import state.ge.items.ItemSet;
import state.ge.limit.LimitTracker;

import java.util.*;

import static org.dreambot.api.methods.MethodProvider.sleepUntil;
import static state.ge.PlaceOfferResult.FAILED_TO_PLACE_OFFER;
import static state.ge.PlaceOfferResult.OFFER_PLACED;
import static state.ge.PlaceOfferResult.TOO_EXPENSIVE;

// All public methods should be safe to perform alone (i.e. correctly handle ge interfaces/widgets). No widget/interface
// checking should be required by caller; high level abstraction es bueno!
public class GrandExchangeInterface {
    private AbstractScript abstractScript;

    private static final int COINS_ID = 995;

    private final Set<Integer> enabledSlots = new HashSet<>();
    private final LimitTracker limitTracker = new LimitTracker();
    private final GrandExchange dreambotGe = new GrandExchange(Client.getClient());

    public GrandExchangeInterface(AbstractScript abstractScript, int numberOfSlots) {
        this.abstractScript = abstractScript;
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
            sleepUntil(geOpen(), 5000);
            if(geOpen().verify()) {
                for(GrandExchangeItem grandExchangeItem : dreambotGe.getItems()) {
                    if(enabledSlots.contains(grandExchangeItem.getSlot())) {
                        grandExchangeItems.add(grandExchangeItem);
                    }
                }
                return grandExchangeItems;
            }
        }
        sleepUntil(exchangeInterfaceOpen(), 3000);
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

    public int placeBuyOffer(String itemName, int amount, int price) {
        if(availableSlotCount() > 0) {
            int slot = dreambotGe.getFirstOpenSlot();
            dreambotGe.buyItem(itemName, amount, price);
            sleepUntil(exchangeInterfaceOpen(), 3000);
            if(slotContainsItem(slot)) {
                return slot;
            }
        }
        return -1;
    }

    // Place buy offer for a given slot, and return slot number. -1 if unsuccessful
    public int placeBuyOffer(Flip flip) {
        int slot = placeBuyOffer(flip.getItemName(), flip.getItemAmount(), flip.getBuyPrice());
        if(slot != -1) {
            flip.setBuyOfferPlacedAt(System.currentTimeMillis());
        }
        return slot;
    }

    public int placeSellOffer(String itemName, int amount, int price) {
        if(availableSlotCount() > 0) {
            int slot = dreambotGe.getFirstOpenSlot();
            dreambotGe.sellItem(itemName, amount, price);
            sleepUntil(exchangeInterfaceOpen(), 3000);
            if(slotContainsItem(slot)) {
                return slot;
            }
        }
        return -1;
    }

    // Place sell offer for a given slot, and return slot number. -1 if unsuccessful
    public int placeSellOffer(Flip flip) {
        int slot = placeSellOffer(flip.getItemName(), flip.getItemAmount(), flip.getSellPrice());
        if(slot != -1) {
            flip.setSellOfferPlacedAt(System.currentTimeMillis());
        }
        return slot;
    }

    // Cancel offer and collect items. Returns null if unsuccessful
    public OfferCollection cancelOffer(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        if(grandExchangeItem != null && grandExchangeItem.getStatus() != Status.EMPTY) {
            dreambotGe.cancelOffer(slot);
            sleepUntil(offerCompletedInOfferInterface(), 3000);
            if(offerIsCompletedInOfferInterface()) {
                return collectFromOpenInterface(grandExchangeItem.getName());
            }
        }
        return null;
    }

    // Collect any items in offer slot; does not require offer to be finished. Returns null if unsuccessful
    public OfferCollection collectOffer(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        if(grandExchangeItem != null && grandExchangeItem.getStatus() != Status.EMPTY) {
            openSlotInterface(slot);
            sleepUntil(currentOfferInterfaceOpen(), 3000);
            if(isCurrentOfferInterfaceOpen()) {
                return collectFromOpenInterface(grandExchangeItem.getName());
            }
        }
        return null;
    }

    // Returns state + price of offer placed at
    public PriceCheckResults placePCBuyOffer(Item item, int availableGold, int buyPrice) {
        int slot = dreambotGe.getFirstOpenSlot();
        if(!dreambotGe.isBuyOpen() && !slotContainsItem(slot)) {
            dreambotGe.openBuyScreen(slot);
            sleepUntil(buyInterfaceOpen(), 3000);
        }

        if(dreambotGe.isBuyOpen() && dreambotGe.getCurrentChosenItem() == null) {
            dreambotGe.addBuyItem(item.getItemName());
            sleepUntil(itemChosen(item), 3000);
        }

        if(dreambotGe.isBuyOpen() && dreambotGe.getCurrentChosenItem() != null
                && dreambotGe.getCurrentChosenItem().getName().equals(item.getItemName())) {
            if(buyPrice == -1) {
                int priceEstimate = dreambotGe.getCurrentPrice();
                buyPrice = Math.max((int) (priceEstimate * 1.1), priceEstimate + 1);
            }
            if(buyPrice > availableGold) {
                return new PriceCheckResults(TOO_EXPENSIVE, -1, -1);
            }
            dreambotGe.buyItem(item.getItemName(), 1, buyPrice);
            sleepUntil(exchangeInterfaceOpen(), 3000);
        }

        boolean success = isExchangeInterfaceOpen() && dreambotGe.slotContainsItem(slot);
        return new PriceCheckResults(success ? OFFER_PLACED : FAILED_TO_PLACE_OFFER, buyPrice, slot);
    }

    // TODO: get buyprice from widget
    public int collectPCBuyOffer(int slot, int buyPrice) {
        OfferCollection collection = collectOffer(slot);
        return collection == null ? -1 : buyPrice - collection.getGold();
    }

    public boolean cancelPCBuyOffer(int slot) {
        return cancelOffer(slot) != null;
    }

    // Returns state + price of offer placed at
    public PriceCheckResults placePCSellOffer(Item item, int sellPrice) {
        int slot = dreambotGe.getFirstOpenSlot();

        if(!dreambotGe.isSellOpen() && !slotContainsItem(slot)) {
            dreambotGe.openSellScreen(slot);
            sleepUntil(sellInterfaceOpen(), 3000);
        }

        if(dreambotGe.isSellOpen() && dreambotGe.getCurrentChosenItem() == null) {
            dreambotGe.addSellItem(item.getItemName());
            sleepUntil(itemChosen(item), 3000);
        }

        if(dreambotGe.isSellOpen() && dreambotGe.getCurrentChosenItem() != null
                && dreambotGe.getCurrentChosenItem().getName().equals(item.getItemName())) {
            if(sellPrice == -1) {
                int priceEstimate = dreambotGe.getCurrentPrice();
                sellPrice = Math.min((int) (priceEstimate * 0.9), priceEstimate + 1);
                sellPrice =  sellPrice < 1 ? 1 : sellPrice;
            }
            dreambotGe.sellItem(item.getItemName(), 1, sellPrice);
            sleepUntil(exchangeInterfaceOpen(), 3000);
        }

        boolean success = isExchangeInterfaceOpen() && dreambotGe.slotContainsItem(slot);
        return new PriceCheckResults(success ? OFFER_PLACED : FAILED_TO_PLACE_OFFER, sellPrice, slot);
    }

    public int collectPCSellOffer(int slot) {
        OfferCollection collection = collectOffer(slot);
        return collection == null ? -1 : collection.getGold();
    }

    public boolean cancelPCSellOffer(int slot) {
        return cancelOffer(slot) != null;
    }

    public int getFirstAvailableSlot() {
        return dreambotGe.getFirstOpenSlot();
    }

    private OfferCollection collectFromOpenInterface(String itemName) {
        boolean isBuy = isCurrentBuyOfferInterfaceOpen();
        if(isCurrentOfferInterfaceOpen()) {
            WidgetChild itemStack = GrandExchangeWidgets.COLLECTION_SQUARE_1.getWidgetChild(abstractScript);
            WidgetChild coins = GrandExchangeWidgets.COLLECTION_SQUARE_2.getWidgetChild(abstractScript);
            if(itemStack != null && itemStack.isVisible()) {
                int previousAmountOfItems = getAmountInInventory(itemName);
                int previousNumberOfCoins = getAmountInInventory("Coins");
                int numberOfCollectedItems = 0;
                int numberOfCollectedCoins = 0;
                if(itemStack.getItemId() != COINS_ID && itemStack.getActions() != null) {
                    numberOfCollectedItems = itemStack.getItemStack();
                    List<String> actions = Arrays.asList(itemStack.getActions());
                    if(actions.contains("Collect-note")) {
                        itemStack.interact("Collect-note");
                    } else if(actions.contains("Collect-notes")) {
                        itemStack.interact("Collect-notes");
                    } else {
                        itemStack.interact("Collect");
                    }
                    if(coins != null && coins.getActions() != null && coins.isVisible()) {
                        numberOfCollectedCoins = coins.getItemStack();
                        coins.interact("Collect");
                    }
                } else {
                    numberOfCollectedCoins = itemStack.getItemStack();
                    itemStack.interact("Collect");
                }

                OfferCollection offerCollection = new OfferCollection(numberOfCollectedCoins,
                        new ItemSet(new Item(itemName), numberOfCollectedItems));

                sleepUntil(exchangeInterfaceOpen(), 5000);

                if(getAmountInInventory(itemName) == previousAmountOfItems + numberOfCollectedItems
                        && getAmountInInventory("Coins") == previousNumberOfCoins + numberOfCollectedCoins) {
                    if(isBuy) {
                        limitTracker.addBuyTransaction(offerCollection.getItems());
                    }
                    return offerCollection;
                }
            }
        }
        return null;
    }

    private boolean slotContainsItem(int slot) {
        if(openExchangeInterface()) {
            return dreambotGe.slotContainsItem(slot);
        }
        return slotContainsItem(slot);
    }

    // Reduction of duplication
    private boolean cancelSlot(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        if(grandExchangeItem != null && grandExchangeItem.getStatus() != Status.EMPTY && !dreambotGe.isReadyToCollect(slot)) {
            cancelOffer(slot);
            sleepUntil(slotEmpty(slot), 3000);
        }
        return slotEmpty(slot).verify();

    }

    private boolean openExchangeInterface() {
        if(!isExchangeInterfaceOpen()) {
            if(dreambotGe.isOpen()) {
                if(isNewOfferInterfaceOpen()) {
                    WidgetChild backButton = GrandExchangeWidgets.BACK_BUTTON.getWidgetChild(abstractScript);
                    backButton.interact("Back");
                    sleepUntil(exchangeInterfaceOpen(), 2000);
                    return isExchangeInterfaceOpen();
                } else if(dreambotGe.close()) {
                    dreambotGe.open();
                    sleepUntil(exchangeInterfaceOpen(), 2000);
                    return isExchangeInterfaceOpen();
                } else {
                    return openExchangeInterface();
                }
            } else {
                dreambotGe.open();
                sleepUntil(exchangeInterfaceOpen(), 2000);
                return isExchangeInterfaceOpen();
            }
        } else {
            return true;
        }
    }

    private int getAmountInInventory(String itemName) {
        return abstractScript.getInventory().count(itemName);
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

    private boolean isExchangeInterfaceOpen() {
        WidgetChild collectWidget = GrandExchangeWidgets.COLLECT_BUTTON.getWidgetChild(abstractScript);
        return collectWidget != null && collectWidget.isVisible();
    }

    private boolean isNewOfferInterfaceOpen() {
        WidgetChild newOfferType = GrandExchangeWidgets.NEW_OFFER_INTERFACE_TYPE.getWidgetChild(abstractScript);
        return newOfferType != null && newOfferType.isVisible();
    }

    private boolean isCurrentOfferInterfaceOpen() {
        WidgetChild newOfferType = GrandExchangeWidgets.CURRENT_OFFER_INTERFACE_TYPE.getWidgetChild(abstractScript);
        return newOfferType != null && newOfferType.isVisible();
    }

    private boolean isCurrentSellOfferInterfaceOpen() {
        WidgetChild offerType = GrandExchangeWidgets.CURRENT_OFFER_INTERFACE_TYPE.getWidgetChild(abstractScript);
        return offerType != null && offerType.isVisible() && offerType.getText().equals("Sell offer");
    }

    private boolean isCurrentBuyOfferInterfaceOpen() {
        WidgetChild offerType = GrandExchangeWidgets.CURRENT_OFFER_INTERFACE_TYPE.getWidgetChild(abstractScript);
        return offerType != null && offerType.isVisible() && offerType.getText().equals("Buy offer");
    }

    private boolean offerIsCompletedInOfferInterface() {
        boolean offerInterfaceOpen = dreambotGe.isBuyOpen() || dreambotGe.isSellOpen();
        boolean offerIsCompleted = GrandExchangeWidgets.COLLECTION_SQUARE_1.getWidgetChild(abstractScript).getActions() != null;
        return offerInterfaceOpen && offerIsCompleted;
    }

    public boolean offerIsCompleted(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        return grandExchangeItem != null && grandExchangeItem.isReadyToCollect();
    }

    private Condition exchangeInterfaceOpen() {
        WidgetChild collectWidget = GrandExchangeWidgets.COLLECT_BUTTON.getWidgetChild(abstractScript);
        return () -> collectWidget != null && collectWidget.isVisible();
    }

    private Condition geOpen() {
        return dreambotGe::isOpen;
    }

    private Condition currentOfferInterfaceOpen() {
        return this::isCurrentOfferInterfaceOpen;
    }

    private Condition offerCompletedInOfferInterface() {
        boolean offerInterfaceOpen = dreambotGe.isBuyOpen() || dreambotGe.isSellOpen();
        boolean offerIsCompleted = GrandExchangeWidgets.COLLECTION_SQUARE_1.getWidgetChild(abstractScript).getActions() != null;
        return () -> offerInterfaceOpen && offerIsCompleted;
    }

    private Condition slotEmpty(int slot) {
        return () -> !slotContainsItem(slot);
    }

    private Condition offerCompleted(int slot) {
        return () -> offerIsCompleted(slot);
    }

    private Condition buyInterfaceOpen() {
        return dreambotGe::isBuyOpen;
    }

    private Condition sellInterfaceOpen() {
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
        COLLECTION_SQUARE_2(465, 23, 3),
        NEW_OFFER_INTERFACE_TYPE(465, 24, 18),
        CURRENT_OFFER_INTERFACE_TYPE(465, 15, 4);

        private int[] identifiers;

        GrandExchangeWidgets(int... identifiers) {
            this.identifiers = identifiers;
        }

        WidgetChild getWidgetChild(AbstractScript abstractScript) {
            return abstractScript.getWidgets().getWidgetChild(identifiers);
        }
    }

}

