package state.ai.agents.idle_node_agents;

import org.dreambot.api.script.AbstractScript;

import java.util.Random;

import static org.dreambot.api.methods.MethodProvider.log;

public class AfkIdleAgent extends IdleAgent {

    private Random random = new Random();
    private long nextMovementTime = getNextMovementTime();

    public AfkIdleAgent(AbstractScript abstractScript) {
        super(abstractScript);
    }

    @Override
    public boolean performAction() {
        log(Long.toString(nextMovementTime - System.currentTimeMillis()));
        if(System.currentTimeMillis() > nextMovementTime) {
            abstractScript.getCamera().rotateTo(random.nextInt(360), random.nextInt(360));
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
