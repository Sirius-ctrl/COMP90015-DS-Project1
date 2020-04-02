package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import client.Client;

public class ClientGUI {
    private JPanel background;
    private JButton searchButton;
    private JButton addButton;
    private JButton deleteButton;
    private JTextField input;
    private JEditorPane output;

    private static Client client = Client.getClient();
    private static ClientGUI gui;

    public ClientGUI() {

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = input.getText();

                System.out.println(userInput.length());

                if (userInput.length() == 0) {
                    output.setText("Please enter a search word!");
                    return;
                }

                //todo : finish this
                output.setText("Searching |" + userInput + "|");

                // reset the input section
                input.setText("");
            }
        });

        addButton.addActionListener(e -> {
            String userInput = input.getText();

            if (userInput.length() == 0) {
                output.setText("Please enter the word you want to add!");
                return;
            }
            // todo : finish this
            String meaning = JOptionPane.showInputDialog("please give a meaning");
            output.setText("Adding " + userInput + ":\n" + meaning);
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = input.getText();

                if (userInput.length() == 0) {
                    output.setText("Please enter the word you want to delete!");
                    return;
                }

                //todo : connect to client and delete the word
                output.setText(client.delete(userInput));
            }
        });

        buildGUI();
    }

    public void buildGUI() {
        JFrame frame = new JFrame("Dictionary");
        frame.setContentPane(this.background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(500,400);
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
}
