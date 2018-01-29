package state.ge;

import org.dreambot.api.Client;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;

import org.dreambot.api.methods.grandexchange.Status;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import state.ge.utils.Flip;
import state.ge.utils.OfferCollection;
import state.ge.utils.PriceCheckResults;
import state.ge.items.Item;
import state.ge.items.ItemSet;
import state.ge.items.ItemLimitTracker;

import java.util.*;

import static org.dreambot.api.methods.MethodProvider.sleep;
import static org.dreambot.api.methods.MethodProvider.sleepUntil;
import static state.ge.utils.PlaceOfferResult.*;

// All public methods should be safe to perform alone (i.e. correctly handle ge interfaces/widgets). No widget/interface
// checking should be required by caller; high level abstraction es bueno!
public class GrandExchangeAPI {
    private AbstractScript abstractScript;

    private static final int COINS_ID = 995;
    private static final int COMPLETED_PROGRESS_COLOUR = 0X005F00;
    private static final int CANCELLED_PROGRESS_COLOUR = 0x8F0000;
    private static final int IN_PROGRESS_PROGRESS_COLOUR = 0xD88020;
    private static final int SLOT_N_INTERFACE_OPEN = 0x10; // OPEN_SLOT varp is value N * 0x10 when slot N open

    private static final String BUY_OFFER_TEXT = "Buy offer";
    private static final String SELL_OFFER_TEXT = "Sell offer";

    private final Set<Integer> enabledSlots = new HashSet<>();
    private final ItemLimitTracker limitTracker = new ItemLimitTracker();
    private final GrandExchange dreambotGe = new GrandExchange(Client.getClient());

    public GrandExchangeAPI(AbstractScript abstractScript, int numberOfSlots) {
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
        List<GrandExchangeItem> grandExchangeItems = new ArrayList<>();
        for(GrandExchangeItem grandExchangeItem : dreambotGe.getItems()) {
            if(enabledSlots.contains(grandExchangeItem.getSlot())) {
                grandExchangeItems.add(grandExchangeItem);
            }
        }
        return grandExchangeItems;
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

    // Count available slots
    public int availableSlotCount() {
        return (int) getGrandExchangeItems().stream().filter(i -> i.getStatus() == Status.EMPTY).count();
    }

    public int placeBuyOffer(String itemName, int amount, int price) {
        if(availableSlotCount() > 0 && openExchangeInterface()) {
            int slot = dreambotGe.getFirstOpenSlot();
            dreambotGe.buyItem(itemName, amount, price);
            sleepUntil(this::isExchangeInterfaceOpen, 2000);
            if(slotContainsItem(slot)) {
                return slot;
            }
        }
        return -1;
    }

    public int placeBuyOffer(Flip flip) {
        int slot = -1;
        if(flip != null) {
            slot = placeBuyOffer(flip.getItemName(), flip.getItemAmount(), flip.getBuyPrice());
            if(slot != -1) {
                flip.setBuyOfferPlacedAt(System.currentTimeMillis());
            }
        }
        return slot;
    }

    public int placeSellOffer(String itemName, int amount, int price) {
        if(availableSlotCount() > 0 && openExchangeInterface()) {
            int slot = dreambotGe.getFirstOpenSlot();
            dreambotGe.sellItem(itemName, amount, price);
            sleepUntil(this::isExchangeInterfaceOpen, 2000);
            if(slotContainsItem(slot)) {
                return slot;
            }
        }
        return -1;
    }

    public int placeSellOffer(Flip flip) {
        int slot = -1;
        if(flip != null) {
            slot = placeSellOffer(flip.getItemName(), flip.getItemAmount(), flip.getSellPrice());
            if(slot != -1) {
                flip.setSellOfferPlacedAt(System.currentTimeMillis());
            }
        }
        return slot;
    }

    public OfferCollection cancelOffer(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        if(grandExchangeItem != null && grandExchangeItem.getStatus() != Status.EMPTY && openSlotCurrentOfferInterface(slot)) {
            dreambotGe.cancelOffer(slot);
            sleepUntil(() -> isOfferCancelled(slot), 2000);
            if(isOfferCancelled(slot)) {
                return collectFromOpenInterface(grandExchangeItem.getName());
            }
        }
        return null;
    }

    // Collect any items in offer slot; does not require offer to be finished. Returns null if unsuccessful
    public OfferCollection collectOffer(int slot) {
        GrandExchangeItem grandExchangeItem = getGrandExchangeItem(slot);
        if(grandExchangeItem != null && grandExchangeItem.getStatus() != Status.EMPTY && openSlotCurrentOfferInterface(slot)) {
            return collectFromOpenInterface(grandExchangeItem.getName());
        }
        return null;
    }

    // Returns state + price of offer placed at. If buy interface already open, slot returns -1
    public PriceCheckResults placePCBuyOffer(Item item, int availableGold, int buyPrice) {
        int slot = -1;
        if(!isExchangeInterfaceOpen() && !dreambotGe.isBuyOpen()) {
            openExchangeInterface();
            return placePCBuyOffer(item, availableGold, buyPrice);
        }
        if(isExchangeInterfaceOpen()) {
            slot = dreambotGe.getFirstOpenSlot();
            if(slot != -1) {
                dreambotGe.openBuyScreen(slot);
                sleepUntil(dreambotGe::isBuyOpen, 2000);
            }
        }
        if(dreambotGe.isBuyOpen()) {
            if(!isItemSelected(item)) {
                dreambotGe.addBuyItem(item.getItemName());
                sleepUntil(() -> isItemSelected(item), 2000);
            }

            if(isItemSelected(item)) {
                // Return market price
                if(buyPrice == -1) {
                    return new PriceCheckResults(MARKET_PRICE_CHECKED, dreambotGe.getCurrentPrice(), slot);
                }
                if(buyPrice > availableGold) {
                    return new PriceCheckResults(TOO_EXPENSIVE, -1, -1);
                }
                dreambotGe.buyItem(item.getItemName(), 1, buyPrice);
                sleepUntil(this::isExchangeInterfaceOpen, 2000);
            }
            return new PriceCheckResults(isExchangeInterfaceOpen() ? OFFER_PLACED : FAILED_TO_PLACE_OFFER, buyPrice, slot);
        }
        return new PriceCheckResults(FAILED_TO_PLACE_OFFER, -1, slot);
    }

    // TODO: get buyprice from widget
    public int collectPCBuyOffer(int slot) {
        OfferCollection collection = collectOffer(slot);
        return collection == null ? -1 : collection.getGold();
    }

    public boolean cancelPCBuyOffer(int slot) {
        return cancelOffer(slot) != null;
    }

    // Returns state + price of offer placed at
    public PriceCheckResults placePCSellOffer(Item item, int sellPrice) {
        int slot = -1;
        if(!isExchangeInterfaceOpen() && !dreambotGe.isSellOpen()) {
            openExchangeInterface();
            return placePCSellOffer(item, sellPrice);
        }
        if(isExchangeInterfaceOpen()) {
            slot = dreambotGe.getFirstOpenSlot();
            if(slot != -1) {
                dreambotGe.openSellScreen(slot);
                sleepUntil(dreambotGe::isSellOpen, 4000);
            }
        }
        if(dreambotGe.isSellOpen()) {
            if(!isItemSelected(item)) {
                dreambotGe.addSellItem(item.getItemName());
                sleepUntil(() -> isItemSelected(item), 4000);
            }

            if(isItemSelected(item)) {
                // Return market price
                dreambotGe.sellItem(item.getItemName(), 1, sellPrice);
                sleepUntil(this::isExchangeInterfaceOpen, 4000);
            }
            return new PriceCheckResults(isExchangeInterfaceOpen() ? OFFER_PLACED : FAILED_TO_PLACE_OFFER, sellPrice, slot);
        }

        return new PriceCheckResults(FAILED_TO_PLACE_OFFER, -1, slot);
    }

    public int collectPCSellOffer(int slot) {
        OfferCollection collection = collectOffer(slot);
        return collection == null ? -1 : collection.getGold();
    }

    public boolean cancelPCSellOffer(int slot) {
        return cancelOffer(slot) != null;
    }

    // Open buy interface and return slot number. -1 if unsuccessful, -2 if interface already open; no way of finding
    // slot number if already open unless we close interface.
    private int openBuyInterface() {
        if(dreambotGe.isBuyOpen()) {
            return -2;
        } else if(openExchangeInterface()) {
            int slot = dreambotGe.getFirstOpenSlot();
            if(slot != -1 && !slotContainsItem(slot)) {
                dreambotGe.openBuyScreen(slot);
                sleepUntil(dreambotGe::isBuyOpen, 4000);
                if(dreambotGe.isBuyOpen()) {
                    return slot;
                }
            } else {
                return -1;
            }
        }
        return openBuyInterface();
    }

    // Open sell interface and return slot number. -1 if unsuccessful, -2 if interface already open; no way of finding
    // slot number if already open unless we close interface.
    private int openSellInterface() {
        if(dreambotGe.isSellOpen()) {
            return -2;
        } else if(openExchangeInterface()) {
            int slot = dreambotGe.getFirstOpenSlot();
            if(slot != -1 && !slotContainsItem(slot)) {
                dreambotGe.openSellScreen(slot);
                sleepUntil(dreambotGe::isSellOpen, 4000);
                if(dreambotGe.isSellOpen()) {
                    return slot;
                }
            } else {
                return -1;
            }
        }
        return openSellInterface();

    }

    // Select item to buy; assumes buy interface is open. Return item market price if successful, else -1.
    private int selectBuyItem(Item item) {
        if(dreambotGe.isBuyOpen()) {
            if (!isItemSelected(item)) {
                dreambotGe.addBuyItem(item.getItemName());
                sleepUntil(() -> isItemSelected(item), 4000);
            }
            if(isItemSelected(item)) {
                int price = dreambotGe.getCurrentPrice();
                if(price != -1) {
                    return price;
                }
            }
            return selectBuyItem(item);
        }
        return -1;
    }

    // Select item to sell; assumes sell interface is open. Return item market price if successful, else -1.
    private int selectSellItem(Item item) {
        if(dreambotGe.isSellOpen()) {
            if (!isItemSelected(item)) {
                dreambotGe.addSellItem(item.getItemName());
                sleepUntil(() -> isItemSelected(item), 4000);
            }
            if(isItemSelected(item)) {
                int price = dreambotGe.getCurrentPrice();
                if(price != -1) {
                    return price;
                }
            }
            return selectSellItem(item);
        }
        return -1;
    }

    // Ew. Fix me.
    private OfferCollection collectFromOpenInterface(String itemName) {
        boolean isBuy = isCurrentBuyOfferInterfaceOpen();
        if(isCurrentOfferInterfaceOpen()) {
            WidgetChild itemStack = GrandExchangeWidget.COLLECTION_SQUARE_1.getWidgetChild(abstractScript);
            WidgetChild coins = GrandExchangeWidget.COLLECTION_SQUARE_2.getWidgetChild(abstractScript);
            sleep(1000, 1500);
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

                sleepUntil(this::isExchangeInterfaceOpen, 5000);

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
        return dreambotGe.slotContainsItem(slot);
    }

    private boolean openExchangeInterface() {
        if(!isExchangeInterfaceOpen()) {
            if(dreambotGe.isOpen()) {
                if(isNewOfferInterfaceOpen() || isCurrentOfferInterfaceOpen()) {
                    WidgetChild backButton = GrandExchangeWidget.BACK_BUTTON.getWidgetChild(abstractScript);
                    backButton.interact("Back");
                    sleepUntil(this::isExchangeInterfaceOpen, 4000);
                    return isExchangeInterfaceOpen();
                } else if(dreambotGe.close()) {
                    dreambotGe.open();
                    sleepUntil(this::isExchangeInterfaceOpen, 4000);
                    return isExchangeInterfaceOpen();
                } else {
                    return openExchangeInterface();
                }
            } else {
                dreambotGe.open();
                sleepUntil(this::isExchangeInterfaceOpen, 4000);
                return isExchangeInterfaceOpen();
            }
        } else {
            return true;
        }
    }

    private boolean openSlotCurrentOfferInterface(int slot) {
        if(!isSlotCurrentOfferInterfaceOpen(slot)) {
            if(openExchangeInterface()) {
                dreambotGe.openSlotInterface(slot);
                sleepUntil(() -> isSlotCurrentOfferInterfaceOpen(slot), 5000);
            }
            return openSlotCurrentOfferInterface(slot);
        }
        return true;
    }

    private int getAmountInInventory(String itemName) {
        return abstractScript.getInventory().count(itemName);
    }

    private boolean isExchangeInterfaceOpen() {
        WidgetChild collectWidget = GrandExchangeWidget.COLLECT_BUTTON.getWidgetChild(abstractScript);
        return collectWidget != null && collectWidget.isVisible();
    }

    private boolean isCurrentOfferInterfaceOpen() {
        WidgetChild progressBar = GrandExchangeWidget.OFFER_INTERFACE_PROGRESS.getWidgetChild(abstractScript);
        return progressBar != null && progressBar.isVisible();
    }

    private boolean isCurrentBuyOfferInterfaceOpen() {
        if(isCurrentOfferInterfaceOpen()) {
            WidgetChild offerType = GrandExchangeWidget.CURRENT_OFFER_INTERFACE_TYPE.getWidgetChild(abstractScript);
            return offerType != null && offerType.isVisible() && offerType.getText().equals(BUY_OFFER_TEXT);
        }
        return false;
    }

    public boolean isSlotCurrentOfferInterfaceOpen(int slot) {
        return isCurrentOfferInterfaceOpen()
                && GrandExchangeVarps.OPEN_SLOT.getVarp(abstractScript) == SLOT_N_INTERFACE_OPEN * (slot + 1);
    }

    private boolean isNewOfferInterfaceOpen() {
        WidgetChild newOfferType = GrandExchangeWidget.OFFER_TYPE.getWidgetChild(abstractScript);
        WidgetChild progressBar = GrandExchangeWidget.OFFER_INTERFACE_PROGRESS.getWidgetChild(abstractScript);
        return newOfferType != null && newOfferType.isVisible() && (progressBar == null || !progressBar.isVisible());
    }
    public boolean isOfferCompleted(int slot) {
        return getProgressBarColour(slot) == COMPLETED_PROGRESS_COLOUR;
    }

    public boolean isOfferCancelled(int slot) {
        return getProgressBarColour(slot) == CANCELLED_PROGRESS_COLOUR;
    }

    private boolean isItemSelected(Item item) {
        return dreambotGe.getCurrentChosenItem() != null
                && dreambotGe.getCurrentChosenItem().getName().equals(item.getItemName());
    }

    private int getProgressBarColour(int slot) {
        WidgetChild progressBar;
        if(isExchangeInterfaceOpen()) {
            progressBar = GrandExchangeWidget.values()[slot].getWidgetChild(abstractScript);
        } else if(isSlotCurrentOfferInterfaceOpen(slot)) {
            // TODO: Hackey. Make a proper static map maybe
            progressBar = GrandExchangeWidget.OFFER_INTERFACE_PROGRESS.getWidgetChild(abstractScript);
        } else {
            openExchangeInterface();
            return getProgressBarColour(slot);
        }
        if(progressBar != null) {
            return progressBar.getTextColor();
        }
        return -1;
    }

    private enum GrandExchangeWidget {
        SLOT_0_PROGRESS(465, 7, 22),
        SLOT_1_PROGRESS(465, 8, 22),
        SLOT_2_PROGRESS(465, 9, 22),
        SLOT_3_PROGRESS(465, 10, 22),
        SLOT_4_PROGRESS(465, 11, 22),
        SLOT_5_PROGRESS(465, 12, 22),
        SLOT_6_PROGRESS(465, 13, 22),
        SLOT_7_PROGRESS(465, 14, 22),
        OFFER_INTERFACE_PROGRESS(465, 22, 4),
        BACK_BUTTON(465, 4),
        COLLECT_BUTTON(465, 6, 0),
        COLLECTION_SQUARE_1(465, 23, 2),
        COLLECTION_SQUARE_2(465, 23, 3),
        OFFER_TYPE(465, 24, 18),
        CURRENT_OFFER_INTERFACE_TYPE(465, 15, 4);

        private int[] identifiers;

        GrandExchangeWidget(int... identifiers) {
            this.identifiers = identifiers;
        }


        WidgetChild getWidgetChild(AbstractScript abstractScript) {
            return abstractScript.getWidgets().getWidgetChild(identifiers);
        }
    }

    private enum GrandExchangeVarps {
        OPEN_SLOT(375);

        private int varpId;

        GrandExchangeVarps(int varpId) {
            this.varpId = varpId;
        }

        public int getVarp(AbstractScript abstractScript) {
            return abstractScript.getPlayerSettings().getConfig(varpId);
        }
    }

}

