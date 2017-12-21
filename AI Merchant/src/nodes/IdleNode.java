package nodes;

import state.MerchantState;
import state.MerchantTask;

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

