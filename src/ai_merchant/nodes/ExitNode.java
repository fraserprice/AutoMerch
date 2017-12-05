package ai_merchant.nodes;

import ai_merchant.nodes.MerchantNode;
import ai_merchant.state.MerchantState;
import ai_merchant.state.MerchantTask;

public class ExitNode extends MerchantNode {

    public ExitNode(MerchantState state) {
        super(state);
    }

    @Override
    public boolean accept() {
        return this.state.getCurrentTask() == MerchantTask.EXIT;
    }

    @Override
    public int execute() {
        return 0;
    }
}

