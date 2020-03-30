package server;

import java.io.*;
import java.net.Socket;

public class DicSocket implements Runnable {

    private final Socket client;

    public DicSocket(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
//            BufferedWriter output =
//                    new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
//            BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

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
                // todo : make this function can receive command multiple times

                sendLine = "Received Message: " + recvLine;
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
