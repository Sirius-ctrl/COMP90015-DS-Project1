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

    public ClientGUI() {
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tamp = input.getText();
                output.setText("Searching |" + tamp + "|");
            }
        });

        addButton.addActionListener(e -> {
            String text = "";
            text = JOptionPane.showInputDialog("please give a meaning");
            output.setText(text);
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                output.setText("");
            }
        });
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Dictionary");
        frame.setContentPane(new ClientGUI().background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(500,400);
    }
}
