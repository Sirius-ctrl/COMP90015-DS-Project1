package client;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

import feedback.*;

import static java.lang.System.*;

public class Client {

    // parameters variable
    @Parameter(names={"--port", "-p"}, description = "Port number to connect server")
    public static int port = 3456;
    @Parameter(names={"--addr", "-a"}, description = "IP address to server")
    public static String ip_addr = "127.0.0.1";

    private static Socket socket;
    private static DataInputStream input;
    private static DataOutputStream output;

    public static Client client;

    public static Client getClient() {
        if (client != null) {
            return client;
        }

        client = new Client();
        return client;
    }

    public String search(String word, boolean beautify) {

        Feedback connectFeedback = connect();

        // build connection first
        if (connectFeedback.getFeedbackType() == FeedbackType.ERROR) {
            return connectFeedback.getMessage();
        } else {
            println(connectFeedback.getMessage());
        }

        //try to parse the result
        JSONObject obj = new JSONObject();
        obj.put("search", word);
        println(obj.toString());

        try {
            String resHeader = "==== " + word + " ====\n\n";
            output.writeUTF(obj.toString());
            output.flush();

            JSONObject query = new JSONObject(input.readUTF());

            // query finish, disconnect with server
            Feedback goodbyeFeedback = goodbye();
            if (goodbyeFeedback.getFeedbackType() == FeedbackType.ERROR) {
                return goodbyeFeedback.getMessage();
            } else {
                println(goodbyeFeedback.getMessage());
            }

            if (query.has(FeedbackType.SUCCESS.toString())) {

                String feedbackString = query.getString("SUCCESS");

                // reserved starting substring that means user want to keep the format
                if (feedbackString.substring(0,3).equals("$*$")) {
                    beautify = false;
                    feedbackString = feedbackString.substring(3);
                }

                Beautifier bu = Beautifier.getBeautifier();

                println("==== search query succeed ====");
                if (beautify) {
                    return resHeader + bu.beautifySearch(feedbackString);
                } else {
                    return resHeader + bu.beautifySearch(feedbackString, true);
                }
            } else {
                println("==== search query failed ====");
                return resHeader + query.getString(FeedbackType.ERROR.toString());
            }

        } catch (IOException e) {
            println(e.getMessage());
        }

        return "Search error, please try again or check network connection!";
    }


    public String add(String word, String meaning, boolean format) {
        //assume all the word and meaning reach here are valid

        if (!format) {
            meaning = Beautifier.getBeautifier().cleanFormat(meaning);
        } else {
            meaning = "$*$" + meaning;
        }

        // connect with server and handle error if not success
        Feedback connectFeedback = connect();
        if (connectFeedback.getFeedbackType() == FeedbackType.ERROR) {
            return connectFeedback.getMessage();
        } else {
            println(connectFeedback.getMessage());
        }

        JSONObject obj = new JSONObject();
        obj.put("add", word);
        obj.put("meaning", meaning);

        try {
            output.writeUTF(obj.toString());
            output.flush();

            JSONObject query = new JSONObject(input.readUTF());

            // word addition query finish, close the connection
            Feedback goodbyeFeedback = goodbye();
            if (goodbyeFeedback.getFeedbackType() == FeedbackType.ERROR) {
                return goodbyeFeedback.getMessage();
            } else {
                println(goodbyeFeedback.getMessage());
            }

            // now processing the query feedback
            if (query.has(FeedbackType.SUCCESS.toString())) {
                println("==== addition query succeed ==== ");
                return query.getString(FeedbackType.SUCCESS.toString());
            } else {
                println("==== addition query failed ====");
                return query.getString(FeedbackType.ERROR.toString());
            }

        } catch (IOException e) {
            println(e.getMessage());
        }

        return "Addition error, please try again or check network connection!";
    }


    public String del(String word) {

        // connect with server and handle error if not success
        Feedback connectFeedback = connect();
        if (connectFeedback.getFeedbackType() == FeedbackType.ERROR) {
            return connectFeedback.getMessage();
        } else {
            println(connectFeedback.getMessage());
        }

        JSONObject obj = new JSONObject();
        obj.put("del", word);

        try {
            output.writeUTF(obj.toString());
            output.flush();

            JSONObject query = new JSONObject(input.readUTF());

            // deletion finish now close the connection
            Feedback goodbyeFeedback = goodbye();
            if (goodbyeFeedback.getFeedbackType() == FeedbackType.ERROR) {
                return goodbyeFeedback.getMessage();
            } else {
                println(goodbyeFeedback.getMessage());
            }

            // now processing the query feedback
            if (query.has(FeedbackType.SUCCESS.toString())) {
                println("==== deletion query succeed ==== ");
                return query.getString(FeedbackType.SUCCESS.toString());
            } else {
                println("==== deletion query failed ====");
                return query.getString(FeedbackType.ERROR.toString());
            }

        } catch (IOException e ) {
            println(e.getMessage());
        }

        return "Deletion error, please try again or check network connection!";
    }


    private Feedback connect() {
        try {
            socket = new Socket(ip_addr, port);

            println("==== initiate connection with the sever ====");
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            return new Feedback(FeedbackType.SUCCESS, "connection built!");
        } catch (IOException e) {
            return new Feedback(FeedbackType.ERROR,"Connection initiate failed! " +
                    "The server might not be available yet!\n Please try again later or check network connection!");
        }
    }


    private Feedback goodbye() {

        try {
            output.writeUTF("$bye");
            output.flush();

            input.close();
            output.close();
            socket.close();

            input = null;
            output = null;
            socket = null;
        } catch (IOException e) {
            return new Feedback(FeedbackType.ERROR, "Cannot disconnect from server");
        }

        return new Feedback(FeedbackType.SUCCESS, "Request finish successfully");
    }


    public void closeAll() {

        try {

            if (socket != null && socket.isConnected()) {
                // tell sever the client close the application to free the sever resources
                output.writeUTF("$bye");
                output.flush();

                // close all
                input.close();
                output.close();
                socket.close();

            }

            println("cleaned up");
        } catch (IOException e) {
            println("socket and stream can not be closed, now force quit!");
            exit(1);
        }
    }


    public static void println(String thing) {
        System.out.println(thing);
    }

}
