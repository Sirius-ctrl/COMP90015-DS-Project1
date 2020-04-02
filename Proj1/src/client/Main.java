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
        output = client.connect();
        gui.setOutput(output);
    }
}
