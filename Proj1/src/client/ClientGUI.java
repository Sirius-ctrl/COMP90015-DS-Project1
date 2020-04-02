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

        searchButton.addActionListener(e -> {
            String userInput = input.getText();

            System.out.println(userInput.length());

            if (userInput.length() == 0) {
                output.setText("Please enter a search word!");
                return;
            }

            // trying to remove all the thing that not alpha before next step
            for (int i = 0; i < userInput.length(); i++) {
                if(!Character.isAlphabetic(userInput.charAt(i))) {
                    String out = "Please enter a valid word which only contains alphabetic!\n Do you mean: ";
                    String correction = "";
                    // try to correct the word
                    for (int j = 0; j < userInput.length(); j++) {
                        if(!Character.isAlphabetic(userInput.charAt(j))) {
                            continue;
                        }
                        correction += userInput.charAt(j);
                    }
                    output.setText(out+correction);
                    input.setText(correction);
                    return;
                }
            }

            output.setText("Searching |" + userInput + "|");

            // set the feedback to text panel
            output.setText(client.search(userInput.toLowerCase()));

            // reset the input section in order to wait for the next input
            input.setText("");
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
                //output.setText(client.delete(userInput));
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
        frame.setSize(1000,600);
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
