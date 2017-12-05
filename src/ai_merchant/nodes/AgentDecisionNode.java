package ai_merchant.nodes;

import ai_merchant.nodes.MerchantNode;
import ai_merchant.state.MerchantState;
import ai_merchant.state.MerchantTask;

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

