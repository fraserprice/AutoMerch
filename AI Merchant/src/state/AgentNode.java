package state;

import org.dreambot.api.script.AbstractScript;

public abstract class AgentNode implements Actionable {
    protected AbstractScript abstractScript;

    public AgentNode(AbstractScript abstractScript) {
        this.abstractScript = abstractScript;
    }

}
