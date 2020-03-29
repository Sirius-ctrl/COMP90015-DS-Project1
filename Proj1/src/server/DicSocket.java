package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DicSocket implements Runnable {

    private final Socket client;

    public DicSocket(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println();
        try {
            DataInputStream input = new DataInputStream(client.getInputStream());
            DataOutputStream output = new DataOutputStream(client.getOutputStream());

            String msg = input.readUTF();
            System.out.println("Client: "+ msg );

            // todo : make this function can receive command multiple times

            String reply = "Received Message: " + msg;
            output.writeUTF(reply);
            output.flush();

            // close all the thing when finish
            input.close();
            output.close();
            client.close();
        } catch (IOException e) {
            System.out.println("Error occur when server communicating with client in DicSocket:");
            System.out.println(e.getMessage());
        }
    }
}
