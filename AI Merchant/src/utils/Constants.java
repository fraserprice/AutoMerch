package utils;

import java.awt.*;

public class Constants {

    public static class GuiConstants {
        public final static String
                GUI_WINDOW_TITLE = "123FLIP",
                START_BUTTON_TEXT = "Start",
                SAVE_CONFIG_BUTTON_TEXT = "Save",
                LOAD_CONFIG_BUTTON_TEXT = "Load",
                NEW_ITEM_BUTTON_TEXT = "Add Item";
    }

    public static class PaintConstants {
        public static final double
                MS_IN_HOUR = 3600000.0;

        public static final int
                TEXT_X = 25,
                TEXT_Y = 375,
                Y_OFFSET = 20,
                X_OFFSET = 175,
                RECT_X = 5,
                RECT_Y = 345,
                RECT_WIDTH = 510,
                RECT_HEIGHT = 130,
                TOGGLE_VISIBLE_BUTTON_WIDTH = 50,
                TOGGLE_VISIBLE_BUTTON_HEIGHT = 20,
                TOGGLE_VISIBLE_BUTTON_X = RECT_X + RECT_WIDTH - TOGGLE_VISIBLE_BUTTON_WIDTH,
                TOGGLE_VISIBLE_BUTTON_Y = RECT_Y + RECT_HEIGHT - TOGGLE_VISIBLE_BUTTON_HEIGHT;

        public static final Rectangle
                TOGGLE_VISIBLE_BUTTON = new Rectangle(
                        TOGGLE_VISIBLE_BUTTON_X,
                        TOGGLE_VISIBLE_BUTTON_Y,
                        TOGGLE_VISIBLE_BUTTON_WIDTH,
                        TOGGLE_VISIBLE_BUTTON_HEIGHT
                );

        public static final Color
                BLACK = Color.BLACK,
                WHITE = Color.WHITE;
    }

}
