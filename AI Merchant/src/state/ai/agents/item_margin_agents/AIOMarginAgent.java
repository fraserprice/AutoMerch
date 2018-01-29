package state.ai.agents.item_margin_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.item_selection_agents.ItemSelectionAgent;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;
import utils.ScriptData;

/*
 *
 * This strategy should choose to either perform lookup via ge or osb based on the following metrics:
 *
 *  - Item trade volume, price/price variance, and buying limit
 *  - Availability of recent OSB data
 *
 * In general:
 *
 *  - Guaranteed high volume -> GE lookup; conversely guaranteed low volume -> OSB lookup
 *  - Med volume -> Use OSB if price update is recent or price is stable; otherwise use GE lookup
 *
 * TODO: Implement machine learning based on statistics from script data sent to webserver by all bots. Would probably
 * TODO: have to work in conjunction with machine learning merch node
 *
 */
public class AIOMarginAgent extends MarginAgent {

    private final Item item;

    private GEPriceCheckerMarginAgent gepc;
    private OSBPriceCheckerMarginAgent osbpc;

    public AIOMarginAgent(ScriptData scriptData, Item item, int undercutPercentage) {
        super(scriptData, item, undercutPercentage);
        this.item = item;
        gepc = new GEPriceCheckerMarginAgent(scriptData, item, undercutPercentage);
        osbpc = new OSBPriceCheckerMarginAgent(scriptData, item, undercutPercentage);
    }

    @Override
    protected boolean handlePCQueued() {
        return false;
    }
    @Override
    protected boolean handleBuyQueued() {
        return false;
    }

    @Override
    protected boolean handleBuying() {
        return false;
    }

    @Override
    protected boolean handleBought() {
        return false;
    }

    @Override
    protected boolean handleSelling() {
        return false;
    }

    @Override
    protected boolean handleSold() {
        return false;
    }

}
