package state;

import state.agents.AIOAgent;
import state.ge.GrandExchange;

public class MerchantState {

    private AIOAgent AIOAgent;
    private GrandExchange ge;

    public MerchantState(AIOAgent AIOAgent, GrandExchange ge) {
        this.AIOAgent = AIOAgent;
        this.ge = ge;
    }

    public MerchantTask getCurrentTask() {
        if(AIOAgent.actionWaiting()) {
            return MerchantTask.AGENT_DECISION;
        }
        return MerchantTask.IDLE;
    }

    public void performAgentAction() {
        this.AIOAgent.performAction();
    }

}
