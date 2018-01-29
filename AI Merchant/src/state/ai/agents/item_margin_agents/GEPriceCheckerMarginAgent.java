package state.ai.agents.item_margin_agents;

import org.dreambot.api.script.AbstractScript;
import state.ge.utils.Margin;
import state.ge.utils.PriceCheckResults;
import state.ge.items.Item;
import utils.ScriptData;
import utils.ScriptStatus;

import java.util.Arrays;
import java.util.List;

import static org.dreambot.api.methods.MethodProvider.log;
import static org.dreambot.api.methods.MethodProvider.sleepUntil;
import static state.ai.agents.item_margin_agents.MarginAgent.ItemState.*;
import static state.ge.utils.PlaceOfferResult.OFFER_PLACED;

public class GEPriceCheckerMarginAgent extends MarginAgent {

    // TODO: Randomize/improve
    private final List<Double> FRACTION_GROWTH = Arrays.asList(0.0, 0.05, 0.15, 0.35, 0.6, 1.0);
    private PCState pcState = PCState.BUY_QUEUED;
    private int priceEstimate = -1;
    private int maxPCBuy = -1;
    private int minPCSell = -1;
    private int currentPCBuy = -1;
    private int currentPCSell = -1;
    private double currentPCFraction = 0.05;

    public GEPriceCheckerMarginAgent(ScriptData scriptData, Item item, int undercutPercentage) {
        super(scriptData, item, undercutPercentage);
    }


    @Override
    protected boolean handlePCQueued() {
        log(item.getItemName() + ": " + pcState.getMessage());
        switch(pcState) {
            case BUY_QUEUED:
                scriptData.setStatus(ScriptStatus.PC_BUY_QUEUED);
                return handlePCBuyQueued();
            case BUYING:
                scriptData.setStatus(ScriptStatus.PC_BUYING);
                return handlePCBuying();
            case BOUGHT:
                scriptData.setStatus(ScriptStatus.PC_BOUGHT);
                return handlePCBought();
            case SELLING:
                scriptData.setStatus(ScriptStatus.PC_SELLING);
                return handlePCSelling();
        }
        return true;
    }

    private boolean handlePCBuyQueued() {
        if(currentPCBuy != -1 && priceEstimate != -1 && currentPCBuy >= maxPCBuy) {
            notifyBadItem();
            return false;
        }
        PriceCheckResults result = ge.placePCBuyOffer(item, getAvailableGold(), currentPCBuy);
        log(result.getOfferResult().getMessage());
        if(result.getSlot() != -1) {
            slot = result.getSlot();
        }
        switch (result.getOfferResult()) {
            case OFFER_PLACED:
                currentPCBuy = result.getPrice();
                pcState = PCState.BUYING;
                return true;
            case MARKET_PRICE_CHECKED:
                currentPCBuy = result.getPrice();
                priceEstimate = currentPCBuy;
                setMaxPCBuyPrice();
                setNextPCBuyPrice();
                return true;
            case TOO_EXPENSIVE:
                notifyBadItem();
                return false;
            default:
                return true;
        }
    }

    private boolean handlePCBuying() {
        sleepUntil(() -> ge.isOfferCompleted(slot), 2000);
        if(ge.isOfferCompleted(slot)) {
            int collectionValue = ge.collectPCBuyOffer(slot);
            if(collectionValue != -1) {
                currentPCBuy -= collectionValue;
                itemMargin.setMaximum(currentPCBuy);
                setMinPCSellPrice();
                setNextPCSellPrice();
                slot = -1;
                currentPCFraction = 0.05;
                pcState = PCState.BOUGHT;
            }
        } else {
            if(ge.cancelPCBuyOffer(slot)) {
                setNextPCBuyPrice();
                slot = -1;
                pcState = PCState.BUY_QUEUED;
            }
        }
        return true;
    }

    private boolean handlePCBought() {
        if(currentPCSell <= minPCSell) {
            notifyBadItem();
            return false;
        }
        PriceCheckResults result = ge.placePCSellOffer(item, currentPCSell);
        log(result.getOfferResult().getMessage());
        if(result.getOfferResult() == OFFER_PLACED) {
            slot = result.getSlot();
            currentPCSell = result.getPrice();
            pcState = PCState.SELLING;
        }
        return true;
    }

    private boolean handlePCSelling() {
        if(ge.isOfferCompleted(slot)) {
            int collectionValue = ge.collectPCSellOffer(slot);
            if(collectionValue != -1) {
                itemMargin.setMinimum(collectionValue);
                slot = -1;
                currentPCBuy = -1;
                currentPCSell = -1;
                currentPCFraction = 0.05;
                pcState = PCState.BUY_QUEUED;
                state = ItemState.BUY_QUEUED;
            }
        } else {
            if(ge.cancelPCSellOffer(slot)) {
                setNextPCSellPrice();
                slot = -1;
                pcState = PCState.BOUGHT;
            }
        }
        return true;
    }


    @Override
    protected void resetItem() {
        slot = -1;
        priceEstimate = -1;
        currentPCBuy = -1;
        currentPCSell = -1;
        maxPCBuy = -1;
        minPCSell = -1;
        currentPCFraction = 0.05;
        itemMargin = new Margin();
        flip = null;
        state = IDLE;
    }

    /*
     * Heuristic for suitable price check values:
     *      - As item price increases, proportional max buy should decrease
     *      - As item price increases, proportional margin size should decrease
     *
     * E.g. We don't want to buy a godsword for 2x market value or sell for 0.5x market value as we may lose a lot of
     * money if item isn't being actively traded; however buying an iron warhammer for 10x market value isn't an issue
     * as we will never lose more than a few thousand if item isn't actively traded.
     *
     * This strategy should hold well for suitable ge price-check (i.e. high-volume) items; low-volume items should use
     * osbuddy or static price.
     *
     */

    // Returns double to multiply (max - current) or (current - min) by to obtain next PC price
    private void setNextPCFraction() {
        int nextIndex = FRACTION_GROWTH.indexOf(currentPCFraction) + 1;
        currentPCFraction = nextIndex >= FRACTION_GROWTH.size() ? -1 : FRACTION_GROWTH.get(nextIndex);
    }

    private void setNextPCBuyPrice() {
        currentPCBuy = (int) (priceEstimate + currentPCFraction * (maxPCBuy - priceEstimate));
        setNextPCFraction();
    }

    private void setNextPCSellPrice() {
        if(currentPCSell == -1) {
            currentPCSell = (int) (currentPCBuy - currentPCFraction * (currentPCBuy - minPCSell));
        } else {
            currentPCSell = Math.min(currentPCSell - 1, (int) (currentPCBuy - currentPCFraction * (currentPCBuy - minPCSell)));
        }
        setNextPCFraction();
    }

    private void setMaxPCBuyPrice() {
        maxPCBuy = (int) (
                - 5.078823051 * Math.pow(10,-26)  * Math.pow(priceEstimate, 4)
                + 5.669766257 * Math.pow(10, -17) * Math.pow(priceEstimate, 3)
                - 6.004089355 * Math.pow(10, -9)  * Math.pow(priceEstimate, 2)
                + 1.104658938                     * priceEstimate
                + 2500
        );
    }

    private void setMinPCSellPrice() {
        minPCSell = Math.max(currentPCBuy - (int) (
                - 1.945930527 * Math.pow(10, -25) * Math.pow(currentPCBuy, 4)
                + 2.048680473 * Math.pow(10, -16) * Math.pow(currentPCBuy, 3)
                - 9.367653320 * Math.pow(10, -9)  * Math.pow(currentPCBuy, 2)
                + 1.026246548 * Math.pow(10, -1)  * currentPCBuy
                + 2500
        ), 1);
    }

    private enum PCState {
        BUY_QUEUED("Attempting to buy item for price check"),
        BUYING("Buying for price check in progress"),
        BOUGHT("Attempting to sell item for price check"),
        SELLING("Selling for price check in progress");

        private String message;

        PCState(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

}
