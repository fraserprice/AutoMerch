package gui;

import utils.Constants.PaintConstants;
import utils.ScriptData;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Paint {

    private ScriptData scriptData;
    private boolean paintIsVisible = true;

    public Paint(ScriptData scriptData) {
        this.scriptData = scriptData;
    }

    public void repaint(Graphics g) {
        if(paintIsVisible) {
            g.setColor(Color.ORANGE);
            g.fill3DRect(PaintConstants.RECT_X, PaintConstants.RECT_Y,
                    PaintConstants.RECT_WIDTH, PaintConstants.RECT_HEIGHT,
                    true);
            g.setColor(PaintConstants.BLACK);
            List<String> infoMessages = new ArrayList<String>() {{
                add(getStatusMessage());
                add(getTimeRunMessage());
                add(getProfitMessage());
                add(getProfitPerHourMessage());
            }};
            for(int i = 0; i < infoMessages.size(); i++) {
                g.drawString(infoMessages.get(i),
                        PaintConstants.TEXT_X, PaintConstants.TEXT_Y + PaintConstants.Y_OFFSET * i);
            }
            g.setColor(Color.GREEN);
            g.fill3DRect(PaintConstants.TOGGLE_VISIBLE_BUTTON_X, PaintConstants.TOGGLE_VISIBLE_BUTTON_Y,
                    PaintConstants.TOGGLE_VISIBLE_BUTTON_WIDTH, PaintConstants.TOGGLE_VISIBLE_BUTTON_HEIGHT,
                    true);
        } else {
            g.setColor(Color.RED);
            g.fill3DRect(PaintConstants.TOGGLE_VISIBLE_BUTTON_X, PaintConstants.TOGGLE_VISIBLE_BUTTON_Y,
                    PaintConstants.TOGGLE_VISIBLE_BUTTON_WIDTH, PaintConstants.TOGGLE_VISIBLE_BUTTON_HEIGHT,
                    true);
        }
    }

    public String getStatusMessage() {
        return "Status: " + scriptData.getStatus().getMessage();
    }

    public String getTimeRunMessage() {
        return "Time Running: " + scriptData.getTimeRunString();
    }

    public String getProfitMessage() {
        return "Profit: " + scriptData.getGoldMade();
    }

    public String getProfitPerHourMessage() {
        return "Profit Per Hour: " + (int)
                (scriptData.getGoldMade() / (scriptData.getTimeRun()/PaintConstants.MS_IN_HOUR));
    }

    public void handleMousePress(MouseEvent e) {
        if(PaintConstants.TOGGLE_VISIBLE_BUTTON.contains(e.getPoint())) {
            this.paintIsVisible = !paintIsVisible;
        }
    }
}
