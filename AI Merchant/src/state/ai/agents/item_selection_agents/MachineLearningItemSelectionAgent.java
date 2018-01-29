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
 * Should select items based upon the recent successes of other bots
 *
 * TODO: ----- REQUIRES WEBSERVER -----
 * TODO: All bots going for same item will increase flip demand -> decrease profit. Take number of bots on every item
 * TODO: into account?
 * TODO: ----- REQUIRES WEBSERVER -----
 */
public class MachineLearningItemSelectionAgent extends ItemSelectionAgent {

    public MachineLearningItemSelectionAgent(ScriptData scriptData) {
        super(scriptData);
    }

    @Override
    public boolean performAction() {
        return false;
    }
}
