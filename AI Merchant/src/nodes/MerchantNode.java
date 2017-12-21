package nodes;

import state.MerchantState;
import org.dreambot.api.script.TaskNode;

public abstract class MerchantNode extends TaskNode {

    protected MerchantState state;

    public MerchantNode(MerchantState state) {
        this.state = state;
    }
}