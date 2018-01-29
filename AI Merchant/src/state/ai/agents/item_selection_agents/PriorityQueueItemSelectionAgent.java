package state.ai.agents.item_selection_agents;

import state.ai.AgentNode;
import state.ge.items.Item;
import utils.ScriptData;

public class PriorityQueueItemSelectionAgent extends ItemSelectionAgent {

    public PriorityQueueItemSelectionAgent(ScriptData scriptData) {
        super(scriptData);
    }

    @Override
    public boolean performAction() {
        for(Item item : getWaitingItemQueue()) {
            AgentNode itemStrategy = scriptData.getItemMarginAgents().get(item);
            if(itemStrategy.performAction()) {
                return true;
            }
        }
        return false;
    }

}
