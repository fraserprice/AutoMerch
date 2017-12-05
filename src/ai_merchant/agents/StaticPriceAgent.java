package ai_merchant.agents;

import ai_merchant.ge.GrandExchange;

import java.util.Queue;

public class StaticPriceAgent extends Agent {

    public StaticPriceAgent(GrandExchange ge, Queue<String> itemList, int availableGold) {
        super(ge, itemList, availableGold);
    }

    @Override
    public void performAction() {

    }

    @Override
    public boolean actionWaiting() {
        return false;
    }
}
