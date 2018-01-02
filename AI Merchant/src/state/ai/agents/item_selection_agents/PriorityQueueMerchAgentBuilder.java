package state.ai.agents.item_selection_agents;

public class PriorityQueueMerchAgentBuilder extends MerchAgentBuilder<PriorityQueueMerchAgentBuilder> {
    @Override
    protected PriorityQueueMerchAgentBuilder getThis() {
        return this;
    }

    public PriorityQueueMerchAgent build() {
        return new PriorityQueueMerchAgent(abstractScript, ge, itemQueue, itemRestrictions, itemStrategies);
    }
}
