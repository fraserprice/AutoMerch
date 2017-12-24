package state.ai.agents;

import static org.dreambot.api.methods.MethodProvider.log;

public class PriorityQueueAgentBuilder extends AgentBuilder<PriorityQueueAgentBuilder> {
    @Override
    protected PriorityQueueAgentBuilder getThis() {
        return this;
    }

    public PriorityQueueAgent build() {
        return new PriorityQueueAgent(ge, itemQueue, itemRestrictions, pc, itemStrategies);
    }
}
