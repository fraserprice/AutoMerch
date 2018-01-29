package state.ai.agents.idle_agents;

import org.dreambot.api.script.AbstractScript;
import utils.ScriptData;

import java.util.Random;

import static org.dreambot.api.methods.MethodProvider.log;

public class AfkIdleAgent extends IdleAgent {

    private Random random = new Random();
    private long nextMovementTime = getNextMovementTime();

    public AfkIdleAgent(ScriptData scriptData) {
        super(scriptData);
    }

    @Override
    public boolean performAction() {
        if(System.currentTimeMillis() > nextMovementTime) {
            scriptData.getAbstractScript().getCamera().rotateTo(random.nextInt(360), random.nextInt(360));
            nextMovementTime = getNextMovementTime();
        }
        return true;
    }

    private long getNextMovementTime() {
        long timer = (long) (random.nextGaussian() * 120000 + 60000);
        if(timer > 240000) {
            return getNextMovementTime();
        }
        return System.currentTimeMillis() + timer;
    }
}
