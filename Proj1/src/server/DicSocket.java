package server;

import java.io.*;
import java.net.Socket;

public class DicSocket implements Runnable {

    private final Socket client;
    private Dictionary myDict;

    public DicSocket(Socket client, Dictionary myDict) {
        this.client = client;
        this.myDict = myDict;
    }

    @Override
    public void run() {
        try {

            DataInputStream input = new DataInputStream(client.getInputStream());
            DataOutputStream output = new DataOutputStream(client.getOutputStream());

            String recvLine, sendLine;

            while ( true ) {
                System.out.println("==== receiving messages ====");
                recvLine = input.readUTF();
                System.out.println("Client: " + recvLine + " len:" + recvLine.length());

                if (recvLine.equals("$bye")) {
                    break;
                }

                sendLine = "Received Message: " + recvLine;
                // todo : send query/add/delete feedback to client
                output.writeUTF(sendLine);
                output.flush();
                System.out.println("==== responds sent ====");
            }

            System.out.println("==== User Disconnected ====");
            input.close();
            output.close();
            client.close();
        } catch (IOException e) {
            System.out.println("Error occur when server communicating with client in DicSocket:" + e.getMessage());
        }
    }
}
