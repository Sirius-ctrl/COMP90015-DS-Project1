package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

import client.Client;
import feedback.Feedback;
import feedback.FeedbackType;

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

    private static int dpWidth = 95;

    private static Client client = Client.getClient();
    private static ClientGUI gui;

    public ClientGUI() {

        searchButton.addActionListener(e -> {
            String userInput = input.getText();

            if (userInput.length() == 0) {
                output.setText("Please enter a search word!");
                return;
            }

            // trying to remove all the thing that not alpha before next step
            if (userInputCorrection(userInput) == FeedbackType.ERROR){
                return;
            }

            output.setText("Searching |" + userInput + "|");

            // set the feedback to text panel
            output.setText(client.search(userInput.toLowerCase(), beautify.isSelected()));

            // reset the input section in order to wait for the next input
            input.setText("");
        });


        addButton.addActionListener(e -> {
            String userInput = input.getText();

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

            String feedback = client.add(parameters);

            output.setText(feedback);
            input.setText("");
        });


        deleteButton.addActionListener(e -> {
            String userInput = input.getText();

            if (userInput.length() == 0) {
                output.setText("Please enter the word you want to delete!");
                return;
            }

            output.setText("Deleting |" + userInput + "|");
            output.setText(client.del(userInput));
            input.setText("");
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

    public static ClientGUI getGUI() {
        if (gui == null) {
            gui = new ClientGUI();
        }
        return gui;
    }

    public void setOutput(String text) {
        output.setText(text);
    }

    public int getWidth() {
        return dpWidth;
    }

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
