package utils;

import org.dreambot.api.script.AbstractScript;
import state.ai.agents.item_margin_agents.GEPriceCheckerMarginAgent;
import state.ai.agents.item_margin_agents.MarginAgent;
import state.ai.agents.item_selection_agents.ItemSelectionAgent;
import state.ge.GrandExchangeAPI;
import state.ge.items.Item;
import state.ge.items.ItemRestrictions;
import state.ge.utils.Flip;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/*
 * Interface for retrieving data from script. Used by paint + to send to webserver for analytics
 */
public class ScriptData {

    // ==================================================================================

    // Abstractscript
    private AbstractScript abstractScript;

    // Start Time + Script Status
    private static final long START_TIME = System.currentTimeMillis();
    private boolean isScriptStarted = false;
    private ScriptStatus status = ScriptStatus.IDLE;

    // Flipping Strategy Objects
    private ItemSelectionAgent itemSelectionAgent;
    private Map<Item, MarginAgent> itemMarginAgents;
    private Map<Item, ItemRestrictions> itemRestrictions;
    private Queue<Item> itemQueue;
    private Set<Flip> completedFlips;

    // Ge
    private GrandExchangeAPI ge;

    // ==================================================================================

    public ScriptStatus getStatus() {
        return status;
    }

    public void setStatus(ScriptStatus status) {
        this.status = status;
    }

    public void setCompletedFlips(Set<Flip> completedFlips) {
        this.completedFlips = completedFlips;
    }

    public Set<Flip> getCompletedFlips() {
        return completedFlips;
    }

    public int getGoldMade() {
        return completedFlips.stream().mapToInt(Flip::getProfit).sum();
    }

    public long getTimeRun() {
        return System.currentTimeMillis() - START_TIME;
    }

    public String getTimeRunString() {
        long time = getTimeRun();
        String format = String.format("%02d:%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toDays(time),
            TimeUnit.MILLISECONDS.toHours(time),
            TimeUnit.MILLISECONDS.toMinutes(time)
                    - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                    .toHours(time)),
            TimeUnit.MILLISECONDS.toSeconds(time)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                    .toMinutes(time)));
        return format;
    }

    public boolean isScriptStarted() {
        return isScriptStarted;
    }

    public void setScriptStarted(boolean scriptStarted) {
        isScriptStarted = scriptStarted;
    }

    public void setItemQueue(Queue<Item> itemQueue) {
        this.itemQueue = itemQueue;
    }

    public Queue<Item> getItemQueue() {
        return itemQueue;
    }

    public Map<Item, MarginAgent> getItemMarginAgents() {
        return itemMarginAgents;
    }

    public void setItemMarginAgents(Map<Item, MarginAgent> itemMarginAgents) {
        this.itemMarginAgents = itemMarginAgents;
    }

    public GrandExchangeAPI getGe() {
        return ge;
    }

    public void setGe(GrandExchangeAPI ge) {
        this.ge = ge;
    }

    public ItemSelectionAgent getItemSelectionAgent() {
        return itemSelectionAgent;
    }

    public void setItemSelectionAgent(ItemSelectionAgent itemSelectionAgent) {
        this.itemSelectionAgent = itemSelectionAgent;
    }

    public Map<Item, ItemRestrictions> getItemRestrictions() {
        return itemRestrictions;
    }

    public void setItemRestrictions(Map<Item, ItemRestrictions> itemRestrictions) {
        this.itemRestrictions = itemRestrictions;
    }

    public AbstractScript getAbstractScript() {
        return abstractScript;
    }

    public void setAbstractScript(AbstractScript abstractScript) {
        this.abstractScript = abstractScript;
    }
}
