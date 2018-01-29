package state.ai.agents.item_margin_agents;

import org.dreambot.api.script.AbstractScript;
import state.ge.items.ItemRestrictions;
import state.ge.utils.Margin;
import utils.OSBPriceChecker;
import state.ge.items.Item;
import utils.ScriptData;

import static state.ai.agents.item_margin_agents.MarginAgent.ItemState.BUY_QUEUED;

public class OSBPriceCheckerMarginAgent extends MarginAgent {


    public OSBPriceCheckerMarginAgent(ScriptData scriptData, Item item, int undercutPercentage) {
        super(scriptData, item, undercutPercentage);
    }

    @Override
    protected boolean handlePCQueued() {
        Margin itemMargin = OSBPriceChecker.getCurrentMarginEstimate(item);

        if(itemMargin.areBothValid()) {
            this.itemMargin = itemMargin;
            state = BUY_QUEUED;
        }
        return true;
    }
}
