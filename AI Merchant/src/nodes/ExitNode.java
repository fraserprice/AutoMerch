package nodes;

import state.MerchantState;
import state.MerchantTask;

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

