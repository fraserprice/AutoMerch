package gui;

import state.ai.agents.item_margin_agents.*;
import state.ai.agents.item_margin_agents.OSBPriceCheckerMarginAgent;
import state.ge.items.Item;
import state.ge.utils.Margin;
import utils.Constants.GuiConstants;
import utils.ScriptData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Queue;

public class Gui {

    private ScriptData scriptData;
    private JFrame frame;

    private ItemQueueSplitPane itemQueueSplitPane;

    private ItemSelectionType itemSelectionType = ItemSelectionType.MANUAL;

    public Gui(ScriptData scriptData) {
        if(frame != null) {
            frame.dispose();
        }
        frame = new JFrame("123FLIP");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.scriptData = scriptData;

        JPanel mainPanel = getMainPanel();

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }


    private void refreshMainPane() {
        frame.setContentPane(getMainPanel());
        frame.validate();
        frame.pack();
    }


    private JPanel getMainPanel() {
        switch (itemSelectionType) {
            case MANUAL:
                JPanel splitPanel = new JPanel();
                splitPanel.setLayout(new GridLayout(2, 1));
                splitPanel.add(new ItemQueueSplitPane());
                splitPanel.add(new OptionsPanel());
                return splitPanel;
            default:
                return new OptionsPanel();
        }
    }

    private void displayErrorPopup(String errorMessage) {
        // TODO
    }

    private class NewItemFrame extends JFrame {
        private NewItemFrame() {
            setTitle("Add New Item");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JPanel newItemPanel = new JPanel(new FlowLayout());

            JTextField itemName = new JTextField("New Item");
            newItemPanel.add(itemName);

            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(e -> dispose());
            newItemPanel.add(cancel);

            JButton add = new JButton("Add");
            add.addActionListener(e -> {
                Item newItem = new Item(itemName.getText());
                if(newItem.getItemId() == -1) {
                    JOptionPane.showMessageDialog(this, "Invalid item; please ensure item name is correct");
                } else {
                    scriptData.getItemSelectionAgent().addItem(newItem);
                    refreshMainPane();
                    itemQueueSplitPane.setSelectedItem(newItem);
                    dispose();
                }
            });
            newItemPanel.add(add);

            add(newItemPanel);
            pack();
            setVisible(true);
        }
    }

    private class ItemQueueSplitPane extends JSplitPane {
        private ItemQueueSplitPane() {
            setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            setLeftComponent(new JScrollPane(getItemList()));
            setRightComponent(new JPanel());
            setOneTouchExpandable(true);
            setDividerLocation(0.3);
            setOneTouchExpandable(true);
            setDividerLocation(0.3);
            itemQueueSplitPane = this;
        }

        private void setSelectedItem(Item item) {
            setRightComponent(new ItemDescPanel(item));
            setDividerLocation(0.3);
        }

        // Creates JList of item names corresponding to item queue in scriptData
        private JList<Item> getItemList() {
            JList itemList = new JList<>();
            Queue<Item> itemQueue = scriptData.getItemQueue();
            if(itemQueue != null) {
                itemList = new JList<>(itemQueue.toArray());
            }

            itemList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JList itemList = (JList) e.getSource();
                    int index = itemList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Item selection = (Item) itemList.getModel().getElementAt(index);
                        itemQueueSplitPane.setRightComponent(new ItemDescPanel(selection));
                        itemQueueSplitPane.setDividerLocation(0.3);
                    }
                }
            });

            itemList.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    JList itemList = (JList) e.getSource();
                    int prevIndex = itemList.getSelectedIndex();
                    int index = -1;
                    if(e.getKeyCode() == KeyEvent.VK_UP && prevIndex != 0) {
                        index = prevIndex - 1;
                    } else if(e.getKeyCode() == KeyEvent.VK_DOWN && prevIndex < itemList.getModel().getSize() - 1) {
                        index = prevIndex + 1;
                    }
                    if(index != -1) {
                        Item selection = (Item) itemList.getModel().getElementAt(index);
                        itemQueueSplitPane.setRightComponent(new ItemDescPanel(selection));
                        itemQueueSplitPane.setDividerLocation(0.3);
                    }
                }
            });

            return itemList;
        }
    }


    private class ItemDescPanel extends JPanel {
        private ItemDescPanel(Item item) {
            this.setLayout(new GridLayout(3, 1));

            JTextField itemNameField = new JTextField(item.getItemName());
            this.add(itemNameField);

            JComboBox<MarginAgentType> itemFlipType = getItemFlipTypeComboBox(item);
            this.add(itemFlipType);

            if(itemFlipType.getSelectedItem() != MarginAgentType.STATIC) {
                JComboBox<Integer> undercutPercentage = new JComboBox<>(new Integer[]{0, 5, 10, 20});
                JLabel undercutPercentageLabel = new JLabel("Undercut Percentage");
                undercutPercentage.setSelectedItem(scriptData.getItemMarginAgents().get(item).getUndercutPercentage());

                this.add(undercutPercentageLabel);
                this.add(undercutPercentage);
                this.add(getSaveItemChangesButton(item, itemNameField, itemFlipType, undercutPercentage));
            } else {
                JTextField lowerMargin = new JTextField("0");
                JTextField upperMargin = new JTextField("0");
                StaticPriceMarginAgent agent = (StaticPriceMarginAgent) scriptData.getItemMarginAgents().get(item);
                Margin margin = agent.getMargin();
                if(margin.areBothValid()) {
                    lowerMargin.setText(Integer.toString(margin.getMinimum()));
                    upperMargin.setText(Integer.toString(margin.getMaximum()));
                }
                JLabel buyLabel = new JLabel("Buy price");
                JLabel sellLabel = new JLabel("Sell price");

                this.add(buyLabel);
                this.add(lowerMargin);
                this.add(sellLabel);
                this.add(upperMargin);
                this.add(getSaveItemChangesButton(item, itemNameField, lowerMargin, upperMargin));
            }

            this.add(getDeleteItemButton(item));
        }

        private JButton getDeleteItemButton(Item item) {
            JButton deleteItem = new JButton("Delete");
            deleteItem.addActionListener(e -> {
                for(Item queueItem : scriptData.getItemQueue()) {
                    if(queueItem.getItemName().equals(item.getItemName())) {
                        scriptData.getItemQueue().remove(queueItem);
                        refreshMainPane();
                    }
                }
            });
            return deleteItem;
        }

        private JComboBox<MarginAgentType> getItemFlipTypeComboBox(Item item) {
            JComboBox<MarginAgentType> itemFlipType = new JComboBox<>(MarginAgentType.values());
            MarginAgent currentAgent = scriptData.getItemMarginAgents().get(item);
            if(currentAgent != null) {
                itemFlipType.setSelectedItem(MarginAgentType.getMarginAgentType(currentAgent));
            }
            itemFlipType.addActionListener(e -> {
                JComboBox menu = (JComboBox) e.getSource();
                MarginAgentType marginAgentType = (MarginAgentType) menu.getSelectedItem();
                if(marginAgentType == MarginAgentType.STATIC) {
                    scriptData.getItemSelectionAgent().addItemMarginAgent(new StaticPriceMarginAgent(scriptData, item,
                            new Margin(0, 0)));
                } else {
                    scriptData.getItemSelectionAgent().addItemMarginAgent(marginAgentType.getMarginAgent(scriptData,
                            item, 0));
                }
                refreshMainPane();
                itemQueueSplitPane.setSelectedItem(item);
            });
            return itemFlipType;
        }

        private Item saveItemName(Item item, JTextField itemNameField) {
            String itemName = itemNameField.getText();
            Item newItem = null;
            if(!item.getItemName().equals(itemName)) {
                newItem = new Item(itemName);
                if(newItem.getItemId() == -1) {
                    displayErrorPopup("Invalid item");
                } else {
                    scriptData.getItemSelectionAgent().removeItem(item);
                    scriptData.getItemSelectionAgent().addItem(newItem);
                }
            }
            return newItem == null ? item : newItem;
        }

        private MarginAgent setStaticMarginAgent(Item item, JTextField lowerMargin, JTextField upperMargin) {
            try {
                int buyPrice = Integer.parseInt(lowerMargin.getText());
                int sellPrice = Integer.parseInt(upperMargin.getText());
                return new StaticPriceMarginAgent(scriptData, item, new Margin(buyPrice, sellPrice));
            } catch (NumberFormatException nfe) {
                displayErrorPopup("Margins are invalid");
                return null;
            }
        }

        private MarginAgent setNonStaticMarginAgent(Item item, JComboBox<Integer> undercutPercentage,
                                                    MarginAgentType agentType) {
            int undercut = (int) undercutPercentage.getSelectedItem();
            return agentType.getMarginAgent(scriptData, item, undercut);
        }

        private JButton getSaveItemChangesButton(Item item, JTextField itemNameField,
                                                 JComboBox<MarginAgentType> itemFlipType,
                                                 JComboBox<Integer> undercutPercentage) {
            JButton saveChanges = new JButton("Save");
            saveChanges.addActionListener(e -> {
                Item finalItem = saveItemName(item, itemNameField);

                MarginAgentType agentType = (MarginAgentType) itemFlipType.getSelectedItem();
                MarginAgent marginAgent = setNonStaticMarginAgent(finalItem, undercutPercentage, agentType);

                scriptData.getItemSelectionAgent().addItemMarginAgent(marginAgent);
                refreshMainPane();
                itemQueueSplitPane.setSelectedItem(finalItem);
            });
            return saveChanges;
        }

        private JButton getSaveItemChangesButton(Item item, JTextField itemNameField,
                                                 JTextField lowerMargin, JTextField upperMargin) {
            JButton saveChanges = new JButton("Save");
            saveChanges.addActionListener(e -> {
                Item finalItem = saveItemName(item, itemNameField);

                MarginAgent marginAgent = setStaticMarginAgent(item, lowerMargin, upperMargin);

                scriptData.getItemSelectionAgent().addItemMarginAgent(marginAgent);
                refreshMainPane();
                itemQueueSplitPane.setSelectedItem(finalItem);
            });
            return saveChanges;
        }
    }

    private class OptionsPanel extends JPanel {
        private OptionsPanel() {
            setLayout(new FlowLayout());

            JComboBox itemSelectionDropdown = new JComboBox<>(ItemSelectionType.values());
            itemSelectionDropdown.setSelectedItem(itemSelectionType);
            itemSelectionDropdown.addActionListener(e -> {
                JComboBox menu = (JComboBox) e.getSource();
                ItemSelectionType selection = (ItemSelectionType) menu.getSelectedItem();
                boolean refreshRequired = itemSelectionType.refreshRequired(selection);
                itemSelectionType = selection;
                if(refreshRequired) {
                    refreshMainPane();
                }
            });
            add(itemSelectionDropdown);

            if(itemSelectionType == ItemSelectionType.MANUAL) {
                JButton addItem = new JButton(GuiConstants.NEW_ITEM_BUTTON_TEXT);
                addItem.addActionListener(e -> new NewItemFrame());
                add(addItem);
            }

            JButton save = new JButton(GuiConstants.SAVE_CONFIG_BUTTON_TEXT);
            save.addActionListener(e -> {

            });
            //optionsPanel.add(save);

            JButton load = new JButton(GuiConstants.LOAD_CONFIG_BUTTON_TEXT);
            load.addActionListener(e -> {

            });
            //optionsPanel.add(load);

            JButton start = new JButton(GuiConstants.START_BUTTON_TEXT);
            start.addActionListener(e -> {
                scriptData.setScriptStarted(true);
                frame.dispose();
            });
            add(start);
        }
    }

    private enum ItemSelectionType {
        MANUAL("Manual"), OSB("From OSB"), ML("Machine Learning");

        private String message;

        ItemSelectionType(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }

        public boolean refreshRequired(ItemSelectionType itemSelectionType) {
            if(this == MANUAL) {
                return itemSelectionType != MANUAL;
            } else {
                return itemSelectionType == MANUAL;
            }
        }
    }

    private enum MarginAgentType {
        AIO("Handled by AI"), GE("Check price on GE"), OSB("Use prices from OSB"), STATIC("Use static price");

        private String message;

        MarginAgentType(String message) {
            this.message = message;
        }

        public String toString() {
            return message;
        }

        public static MarginAgentType getMarginAgentType(MarginAgent marginAgent) {
            if(marginAgent instanceof AIOMarginAgent) {
                return AIO;
            } else if(marginAgent instanceof GEPriceCheckerMarginAgent) {
                return GE;
            } else if(marginAgent instanceof OSBPriceCheckerMarginAgent) {
                return OSB;
            } else if(marginAgent instanceof StaticPriceMarginAgent) {
                return STATIC;
            }
            return null;
        }

        public MarginAgent getMarginAgent(ScriptData scriptData, Item item, int undercutPercentage) {
            switch (this) {
                case GE:
                    return new GEPriceCheckerMarginAgent(scriptData, item, undercutPercentage);
                case AIO:
                    return new AIOMarginAgent(scriptData, item, undercutPercentage);
                case OSB:
                    return new OSBPriceCheckerMarginAgent(scriptData, item, undercutPercentage);
                default:
                    return null;
            }
        }
    }

}
