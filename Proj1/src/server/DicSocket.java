package server;

import org.json.JSONObject;

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

            String recvLine, sendLine = "Unknown Command, please try again";

            while ( true ) {
                System.out.println("==== receiving messages ====");
                recvLine = input.readUTF();
                System.out.println("Client: " + recvLine + " len:" + recvLine.length());

                if (recvLine.equals("$bye")) {
                    break;
                }

                // todo: might need to handle if recvLine is not a proper json string
                JSONObject query = new JSONObject(recvLine);

                System.out.println(query.toString());

                // todo : I might think to remove the feedback class which is unnecessary
                // todo : create a method to handle any query to the dictionary
                if (query.has("query")) {
                    sendLine = myDict.query(query.getString("query")).getMessage();
                } else if (query.has("add")) {

                    if (query.has("meaning")){
                        sendLine = myDict.add(query.getString("add"), query.getString("meaning")).getMessage();
                    } else {
                        sendLine = "Error: no meaning provided";
                    }

                } else if (query.has("delete")) {
                    myDict.delete(query.getString("delete"));
                } else {
                    sendLine = "Unknown Command, please try again";
                }

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
