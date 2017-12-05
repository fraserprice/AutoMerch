package ai_merchant.state;

import ai_merchant.agents.Agent;
import ai_merchant.ge.GrandExchange;
import ai_merchant.ge.ItemSet;

import java.util.ArrayList;

public class MerchantState {

    private Agent agent;
    private GrandExchange ge;

    public MerchantState(Agent agent, GrandExchange ge) {
        this.agent = agent;
        this.ge = ge;
    }

    public MerchantTask getCurrentTask() {
        if(agent.actionWaiting()) {
            return MerchantTask.AGENT_DECISION;
        }
        return MerchantTask.IDLE;
    }

    public void performAgentAction() {
        this.agent.performAction();
    }

}
