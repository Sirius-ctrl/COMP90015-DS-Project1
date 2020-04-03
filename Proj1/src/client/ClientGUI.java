package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import client.Client;

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

    private static int dpWidth = 100;

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
            for (int i = 0; i < userInput.length(); i++) {
                if(!Character.isAlphabetic(userInput.charAt(i))) {
                    String out = "Please enter a valid word which only contains alphabetic!\n Do you mean: ";
                    StringBuilder correction = new StringBuilder();
                    // try to correct the word
                    for (int j = 0; j < userInput.length(); j++) {
                        if(!Character.isAlphabetic(userInput.charAt(j))) {
                            continue;
                        }
                        correction.append(userInput.charAt(j));
                    }
                    output.setText(out + correction.toString());
                    input.setText(correction.toString());
                    return;
                }
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

                    return;
                }
            }

            String meaning = output.getText();

            // add "$*$" to user meaning if keep format
            // todo : finish interaction with client

            output.setText("Added " + userInput + ":\n" + meaning);
            input.setText("");
        });


        deleteButton.addActionListener(e -> {
            String userInput = input.getText();

            if (userInput.length() == 0) {
                output.setText("Please enter the word you want to delete!");
                return;
            }

            //todo : connect to client and delete the word
            //output.setText(client.delete(userInput));
        });

        // default enter action is to search the word
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    searchButton.doClick();
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

        buildGUI();
    }

    public void buildGUI() {
        JFrame frame = new JFrame("Dictionary");
        frame.setContentPane(this.background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(800,600);
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

}
