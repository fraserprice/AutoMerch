package state.ai.item_strategies;

import state.ai.Actionable;
import state.ai.agents.Agent;
import state.ge.Item;

class MachineLearningItemStrategy extends ItemStrategy implements Actionable {

    public MachineLearningItemStrategy(Agent agent, Item item) {
        super(agent, item);
    }

    @Override
    protected boolean commenceFlip() {

        return false;
    }

    @Override
    protected boolean sellTimeout() {

        return false;
    }
}
