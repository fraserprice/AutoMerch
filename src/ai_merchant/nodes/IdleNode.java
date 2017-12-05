package ai_merchant.nodes;

import ai_merchant.nodes.MerchantNode;
import ai_merchant.state.MerchantState;
import ai_merchant.state.MerchantTask;

public class IdleNode extends MerchantNode {

    public IdleNode(MerchantState state) {
        super(state);
    }

    @Override
    public boolean accept() {
        return this.state.getCurrentTask() == MerchantTask.IDLE;
    }

    @Override
    public int execute() {
        return 0;
    }
}

