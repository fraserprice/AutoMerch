package state.ai.agents.item_selection_agents;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.item_margin_agents.MarginAgent;
import state.ge.GrandExchangeAPI;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;
import utils.ScriptData;

import java.util.Map;
import java.util.Queue;


/*
 * Should select items to flip based upon estimated profits from OSB data. Takes the following into account:
 *
 *  - Item's volume, buy/sell demand, margins, and price + price variance
 *  - Player's available gold
 *  - Availability of recent OSB data
 *
 * TODO: ----- REQUIRES WEBSERVER -----
 * TODO: All bots going for same item will increase flip demand -> decrease profit. Take number of bots on every item
 * TODO: into account?
 * TODO: ----- REQUIRES WEBSERVER -----
 */
public class OSBItemSelectionAgent extends ItemSelectionAgent {

    public OSBItemSelectionAgent(ScriptData scriptData) {
        super(scriptData);
    }

    @Override
    public boolean performAction() {
        return false;
    }
}
