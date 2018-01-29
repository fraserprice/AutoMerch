import gui.Gui;
import gui.Paint;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import state.ai.AgentNode;
import state.ai.agents.idle_agents.AfkIdleAgent;
import state.ai.agents.item_selection_agents.ItemSelectionAgent;
import state.ai.agents.item_selection_agents.PriorityQueueItemSelectionAgent;
import state.ge.GrandExchangeAPI;
import state.ge.items.Item;
import utils.ScriptData;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

@ScriptManifest(
        author = "XD123",
        name = "123Flip",
        version = 1.0,
        description = "AIO Flipping on the GE for profit",
        category = Category.MONEYMAKING)
public class AIOMerchant extends AbstractScript implements MouseListener {

    private Queue<AgentNode> agentNodes = new LinkedList<>();
    private ScriptData scriptData;

    private Paint paint;
    private Gui gui;

    @Override
    public void onStart() {
        scriptData = new ScriptData();
        scriptData.setAbstractScript(this);
        paint = new Paint(scriptData);
        // TODO: Gui should init scriptData
        gui = new Gui(scriptData);
        scriptData.setGe(new GrandExchangeAPI(this, 3));


        String[] items = new String[] {};
        Queue<Item> itemQueue = new LinkedList<>();
        Arrays.stream(items).forEach(itemName -> itemQueue.add(new Item(itemName)));
        scriptData.setItemQueue(itemQueue);

        scriptData.setItemRestrictions(new HashMap<>());

        ItemSelectionAgent itemSelectionAgent = new PriorityQueueItemSelectionAgent(scriptData);

        agentNodes.add(itemSelectionAgent);
        agentNodes.add(new AfkIdleAgent(scriptData));
    }

    @Override
    public int onLoop() {
        if(scriptData.isScriptStarted()) {
            log("--------------------");
            for(AgentNode agentNode : agentNodes) {
                if(agentNode.performAction()) {
                    break;
                }
            }
        }
        return 1000;
    }

    @Override
    public void onExit() {
        System.out.println("Thanks for using 123Flip!");
    }

    @Override
    public void onPaint(Graphics g) {
        paint.repaint(g);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(paint != null) {
            paint.handleMousePress(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}

