package state.ai;

import org.dreambot.api.script.AbstractScript;
import utils.ScriptData;

/*
 *  Interface to be used by agent nodes. Boolean performAction() returns true if an action was indeed performed.
 *
 *  MerchAgents should pass performAction() to item strategy of highest priority item.
 *
 *  For ItemStrategies, performAction() should perform actions relating to the flip of a given item.
 *
 *  Usage should be specific to each IdleAgent node.
*/
public abstract class AgentNode {
    protected ScriptData scriptData;

    public AgentNode(ScriptData scriptData) {
        this.scriptData = scriptData;
    }

    public abstract boolean performAction();

}
