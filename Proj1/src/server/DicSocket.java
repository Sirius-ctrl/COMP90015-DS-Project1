package server;

import feedback.Feedback;
import feedback.FeedbackType;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;

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

                JSONObject query = new JSONObject(recvLine);

                System.out.println(query.toString());

                sendLine = processQuery(query);

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

    private String processQuery(JSONObject query) {
        Feedback sendLine = new Feedback(FeedbackType.ERROR, "Unknown instruction, please check again");

        if (query.has("search")) {
            // search query : {search:word}
            sendLine = myDict.search(query.getString("search"));

        } else if (query.has("add")) {
            // add query {add:word, others:{meaning:the_meaning_of_word,...extra:info}}
            // just put what after

            if (query.has("others")) {
                // has structure {others:{meaning:the_meaning, ...extra:info}}
                JSONObject subQuery = new JSONObject(query.getString("others"));

                if (subQuery.has("meaning")) {
                    sendLine = myDict.add(query.getString("add"), subQuery.toString());
                } else {
                    // this should be checked at client side in order to save server resources
                    // but put it there just to feel safe
                    sendLine = new Feedback(FeedbackType.ERROR, "No meaning provided");
                }
            } else {
                // this will be handled at client side but just leave it here to be safe
                sendLine = new Feedback(FeedbackType.ERROR, "No others provided");
            }

        } else if (query.has("del")) {
            sendLine = myDict.delete(query.getString("del"));
        }

        return sendLine.toJsonString();
    }
}
