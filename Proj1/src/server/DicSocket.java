package server;

import utils.Actions;
import utils.Feedback;
import utils.FeedbackType;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import static utils.Logger.*;

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
            logError("Failed to connect client using socket, might be client quit or timeout:" + e.getMessage());
        }
    }

    private String processQuery(JSONObject query) {
        Feedback sendLine = new Feedback(FeedbackType.ERROR, "Unknown instruction, please check again");

        if (query.has(Actions.SEARCH.toString())) {
            // search query : {search:word}
            sendLine = myDict.search(query.getString(Actions.SEARCH.toString()));

        } else if (query.has(Actions.ADD.toString())) {
            // add query {add:word, others:{meaning:the_meaning_of_word,...extra:info}}
            // just put what after

            if (query.has("others")) {
                // has structure {others:{meaning:the_meaning, ...extra:info}}
                JSONObject subQuery = new JSONObject(query.getString("others"));

                if (subQuery.has("meaning")) {
                    sendLine = myDict.modify(query.getString(Actions.ADD.toString()), subQuery.toString(), Actions.ADD);
                } else {
                    // this should be checked at client side in order to save server resources
                    // but put it there just to feel safe
                    sendLine = new Feedback(FeedbackType.ERROR, "No meaning provided");
                }
            } else {
                // this will be handled at client side but just leave it here to be safe
                sendLine = new Feedback(FeedbackType.ERROR, "No meta-data for the word (namely 'others' section) provided");
            }

        } else if (query.has(Actions.DEL.toString())) {
            sendLine = myDict.modify(query.getString(Actions.DEL.toString()), null, Actions.DEL);
        }

        return sendLine.toJsonString();
    }
}
