package client;

import com.beust.jcommander.JCommander;

public class Main {

    public static void main(String ... args) {
        String output;
        JCommander.newBuilder().addObject(new Client()).build().parse(args);
        Client client = Client.getClient();
        System.out.println("client connecting " + Client.ip_addr + ":" + Client.port);

        // clean up when the application is closed
        Runtime.getRuntime().addShutdownHook(new Thread(client::closeAll));

        ClientGUI gui = ClientGUI.getGUI();
        gui.setOutput("Welcome, please enter a word in the above blank if you want to search/delete, " +
                "or write some extra meaning in this section to submit a meaning of a new word");
    }
}
