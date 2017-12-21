package nodes;

import state.MerchantState;
import state.MerchantTask;

public class AgentDecisionNode extends MerchantNode {

    public AgentDecisionNode(MerchantState state) {
        super(state);
    }

    @Override
    public boolean accept() {
        return this.state.getCurrentTask() == MerchantTask.AGENT_DECISION;
    }

    @Override
    public int execute() {
        this.state.performAgentAction();
        return 0;
    }
}

