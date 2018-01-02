package state.ai.agents.item_margin_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.item_selection_agents.MerchAgent;
import state.ge.items.Item;

/*
 *
 * This strategy should choose to either perform lookup via ge or osb based on the following metrics:
 *
 *  - Item trade volume, price/price variance, and buying limit
 *  - Time of last osb price update
 *
 * In general:
 *
 *  - Guaranteed high volume -> GE lookup; conversely guaranteed low volume -> OSB lookup
 *  - Med volume ->
 *
 * TODO: Implement machine learning based on statistics from script data sent to webserver by all bots. Would probably
 * TODO: have to work in conjunction with machine learning merch node
 *
 */
class AIOItemStrategy extends ItemStrategy {

    public AIOItemStrategy(AbstractScript abstractScript, MerchAgent merchAgent, Item item) {
        super(abstractScript, merchAgent, item);
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
