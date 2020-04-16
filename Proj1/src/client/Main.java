package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import server.Server;

public class Main {

    public static void main(String ... args) {

        try {
            JCommander commander = JCommander.newBuilder().addObject(new Client()).build();
            commander.parse(args);


            if (Client.isHelp()) {
                commander.usage();
                return;
            }
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            System.out.println("Please use -h or --help for the usage");
            return;
        }

        System.out.println("client connecting " + Client.getIP() + ":" + Client.getPort());

        Client client = Client.getClient();
        // clean up when the application is closed
        Runtime.getRuntime().addShutdownHook(new Thread(client::closeAll));

        // put client into event  thread
        java.awt.EventQueue.invokeLater(() -> {
            ClientGUI gui = ClientGUI.getGUI();
            gui.setOutput("Welcome, please enter a word in the above blank if you want to search/delete, " +
                    "or write some extra meaning in this section to submit a meaning of a new word");
        });

    }
}
