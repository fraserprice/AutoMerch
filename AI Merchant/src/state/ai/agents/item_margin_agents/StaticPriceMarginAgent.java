package state.ai.agents.item_margin_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.item_selection_agents.ItemSelectionAgent;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;
import state.ge.utils.Margin;
import utils.ScriptData;

public class StaticPriceMarginAgent extends MarginAgent {

    public StaticPriceMarginAgent(ScriptData scriptData, Item item, Margin fixedMargin) {
        super(scriptData, item, 0);
        fixedMargin.setMarginTimeout(-1);
        itemMargin = fixedMargin;
    }

    @Override
    protected boolean handlePCQueued() {
        state = ItemState.BUY_QUEUED;
        return true;
    }

    public Margin getMargin() {
        return itemMargin;
    }
}
