package state.ai.item_strategies;

import state.ai.Actionable;
import state.ai.agents.Agent;
import state.ge.*;
import state.ge.items.Item;
import state.ge.items.ItemSet;

public class PriceCheckerItemStrategy extends ItemStrategy implements Actionable {

    private PriceCheck upperPriceCheck;
    private PriceCheck lowerPriceCheck;

    public PriceCheckerItemStrategy(Agent agent, Item item, double buyRatioLimit, double sellRatioLimit) {
        super(agent, item);
        upperPriceCheck = new PriceCheck(MarginCheckType.UPPER, buyRatioLimit);
        lowerPriceCheck = new PriceCheck(MarginCheckType.LOWER, sellRatioLimit);
    }

    @Override
    protected boolean checkUpperMargin() {
        int upperMargin = upperPriceCheck.tryToObtainPrice();
        upperMarginCheckInProgress = upperPriceCheck.inProgress;
        if(!lowerMarginCheckInProgress) {
            itemMargin.setMaximum(upperMargin);
        }
        return true;
    }

    @Override
    protected boolean checkLowerMargin() {
        int lowerMargin = lowerPriceCheck.tryToObtainPrice();
        upperMarginCheckInProgress = upperPriceCheck.inProgress;
        if(!lowerMarginCheckInProgress) {
            itemMargin.setMinimum(lowerMargin);
        }
        return true;
    }

    @Override
    protected boolean sellTimeout() {
        Flip remainingFlip = agent.cancelOffer(currentFlip);
        if(ge.getAvailableItemAmount(item) > 0) {
            int upperMargin = upperPriceCheck.tryToObtainPrice();
            ItemSet itemSet = new ItemSet(item, remainingFlip.getItemAmount() + 1);
            Flip newFlip = new Flip(itemSet, currentFlip.getBuyPrice(), upperMargin);
            newFlip.setStatus(FlipStatus.SELLING);
            agent.placeFlipSellOffer(newFlip);
        } else {
            remainingFlip.setSellPrice(currentFlip.getBuyPrice());
            agent.placeFlipSellOffer(remainingFlip);
        }
        return true;
    }

    private class PriceCheck {
        private boolean inProgress = false;
        private int slot = -1;
        private MarginCheckType checkType;
        private double currentRatio;
        private double ratioLimit;

        public PriceCheck(MarginCheckType checkType, double ratioLimit) {
            this.checkType = checkType;
            this.ratioLimit = ratioLimit;
            currentRatio = getStartRatio();
        }

        private double getStartRatio() {
            return checkType == MarginCheckType.UPPER ? 1.1 : 0.9;
        }

        private double getNextRatio() {
            double nextRatio = currentRatio + (checkType == MarginCheckType.UPPER ? 0.1 : -0.1);
            boolean isValidRatio = checkType == MarginCheckType.UPPER ? nextRatio < ratioLimit : nextRatio > ratioLimit;
            return isValidRatio ? nextRatio : -1.0;
        }

        private void resetCheck() {
            currentRatio = getStartRatio();
            inProgress = false;
            slot = -1;
        }

        public int tryToObtainPrice() {
            inProgress = true;
            PriceCheckResults priceCheckResults;
            if(checkType == MarginCheckType.LOWER) {
                priceCheckResults = ge.checkLowerItemPriceIteration(item, currentRatio, slot);
            } else {
                priceCheckResults = ge.checkUpperItemPriceIteration(item, currentRatio, slot);
            }
            if(priceCheckResults.successfullyGotPrice()) {
                inProgress = false;
                return priceCheckResults.getPrice();
            } else {
                double nextRatio = getNextRatio();
                if(nextRatio == -1.0) {
                    agent.getItemRestrictions(item).notifyBadFlip();
                    resetCheck();
                } else {
                    currentRatio = nextRatio;
                }
                return -1;
            }
        }
    }

    private enum MarginCheckType {
        UPPER, LOWER
    }

}
