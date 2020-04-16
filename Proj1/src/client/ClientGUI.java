package client;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;

import utils.FeedbackType;
import static utils.Logger.*;

public class ClientGUI {
    private JPanel background;
    private JButton searchButton;
    private JButton addButton;
    private JButton deleteButton;
    private JTextField input;
    private JEditorPane output;
    private JCheckBox beautify;
    private JCheckBox fixedFormat;
    private JButton displayWidth;
    private JButton cleanOutput;

    private static int dpWidth = 90;

    private static Client client = Client.getClient();
    private static ClientGUI gui;

    private static boolean processing = false;
    private static boolean showed = false;

    public ClientGUI() {

        searchButton.addActionListener(e -> {
            String userInput = input.getText();

            if(isWaiting()){
                return;
            }

            if (userInput.length() == 0) {
                output.setText("Please enter a search word!");
                return;
            }

            // trying to remove all the thing that not alpha before next step
            if (userInputCorrection(userInput) == FeedbackType.ERROR){
                return;
            }

            output.setText("Searching |" + userInput + "| from server ... too many users, please wait!");
            setProcessing(true);

            new Thread(() -> {
                try {
                    ClientGUI.getGUI().setOutput(client.search(userInput.toLowerCase(), beautify.isSelected()));
                } catch (Exception ex) {
                    logError(ex.getMessage());
                }
                // reset indicator
                resetState();
            }).start();
        });


        addButton.addActionListener(e -> {
            String userInput = input.getText();

            if(isWaiting()) {
                return;
            }

            if (userInput.length() == 0) {
                output.setText("Please enter the word you want to add!");
                return;
            }

            // trying to remove all the thing that not alpha before next step
            if (userInputCorrection(userInput) == FeedbackType.ERROR){
                return;
            }

            String meaning = output.getText();

            if (meaning.length() == 0) {
                output.setText("Meaning could not be empty\n");
                return;
            }

            // very easy to extent if additional information is required, just key,value pair to map
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("word", userInput);
            parameters.put("meaning", meaning);
            parameters.put("reservedFormat", fixedFormat.isSelected());

            output.setText("Adding |" + userInput + "| from server ... too many users, please wait!");
            setProcessing(true);

            new Thread(() -> {
                try {
                    ClientGUI.getGUI().setOutput(client.add(parameters));
                } catch (Exception ex) {
                    logError(ex.getMessage());
                }
                // reset indicator
                resetState();
            }).start();

        });


        deleteButton.addActionListener(e -> {
            String userInput = input.getText();

            if(isWaiting()) {
                return;
            }

            if (userInput.length() == 0) {
                output.setText("Please enter the word you want to delete!");
                return;
            }

            output.setText("Deleting |" + userInput + "| from server ... too many users, please wait!");
            setProcessing(true);

            new Thread(() -> {
                try {
                    ClientGUI.getGUI().setOutput(client.del(userInput));
                } catch (Exception ex) {
                    logError(ex.getMessage());
                }
                resetState();
            }).start();

        });

        input.setFocusTraversalKeysEnabled(false);
        // default enter action is to search the word
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    searchButton.doClick();
                } else if (e.getKeyChar() == KeyEvent.VK_TAB) {
                    output.setText("");
                    output.grabFocus();
                }
            }
        });

        displayWidth.addActionListener(e -> {
            String userInput = JOptionPane.showInputDialog("Enter the width that suit you well, " +
                    "the setting will take effect on next search");

            for (int i = 0; i < userInput.length(); i++) {
                if (!Character.isDigit(userInput.charAt(i))){
                    JOptionPane.showMessageDialog(null, "please only give digits");
                    return;
                }
            }

            // restrict the max length of dpWidth no longer than the maximum
            dpWidth = Integer.parseInt(userInput) < output.getWidth()?Integer.parseInt(userInput):output.getWidth();
            JOptionPane.showMessageDialog(null, "Success");
        });


        cleanOutput.addActionListener(e -> {
            output.setText("");
        });

        buildGUI();
    }


    private boolean isWaiting() {
        // investigate whether there is already a query waiting for responds
        if (processing) {
            if(!showed) {
                setOutput(output.getText() + "\n\n still waiting ....");
                setShowed(true);
            }
            log("still processing previous command");
            return true;
        }
        return false;
    }


    /**
     * return a gui instance
     * @return the gui instance
     */
    public static ClientGUI getGUI() {
        if (gui == null) {
            gui = new ClientGUI();
        }
        return gui;
    }

    /**
     * Set the user output panel with some text
     * @param text text will be set
     */
    public void setOutput(String text) {

        if(!text.equals("Bad Data！Search again Please!")) {
            // reset the input section in order to wait for the next input
            input.setText("");
        }

        output.setText(text);
    }

    public void setProcessing(boolean state) {
        processing = state;
    }

    public void setShowed(boolean state) {
        showed = state;
    }

    public void resetState() {
        setProcessing(false);
        setShowed(false);
    }

    /**
     * get current display width restriction
     * @return current width size
     */
    public int getWidth() { return dpWidth; }


    private void buildGUI() {
        JFrame frame = new JFrame("Dictionary");
        frame.setContentPane(this.background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(800,600);
    }


    private FeedbackType userInputCorrection(String userInput) {
        // trying to remove all the thing that not alpha before next step
        for (int i = 0; i < userInput.length(); i++) {
            if(!Character.isAlphabetic(userInput.charAt(i))) {
                String out = "Please enter a valid word which only contains alphabetic!\nDo you mean: ";
                StringBuilder correction = new StringBuilder();
                // try to correct the word
                for (int j = 0; j < userInput.length(); j++) {
                    if(!Character.isAlphabetic(userInput.charAt(j))) {
                        continue;
                    }
                    correction.append(userInput.charAt(j));
                }

                output.setText(output.getText() + "\n----------------------\n" + out + correction.toString() + "?");
                input.setText(correction.toString());

                return FeedbackType.ERROR;
            }
        }

        return FeedbackType.SUCCESS;
    }

}
