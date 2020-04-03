package client;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

import feedback.*;

import static java.lang.System.exit;
import static java.lang.System.out;

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


    public Feedback connect() {
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


    public String search(String word, boolean beautify) {

        Feedback connect = connect();

        // build connection first
        if (connect.getFeedbackType() == FeedbackType.ERROR) {
            return connect.getMessage();
        } else {
            println(connect.getMessage());
        }

        //try to parse the result
        JSONObject obj = new JSONObject();
        obj.put("search", word);
        println(obj.toString());

        try {
            String resHeader = "==== " + word + " ====\n\n";
            output.writeUTF(obj.toString());
            output.flush();

            JSONObject feedback = new JSONObject(input.readUTF());

            // query finish, disconnect with server
            goodbye();

            if (feedback.has("SUCCESS")) {

                String feedbackString = feedback.getString("SUCCESS");

                // todo: test this is actually working
                // reserved starting substring that means user want to keep the format
                if (feedbackString.substring(0,3).equals("$*$")) {
                    beautify = false;
                }

                Beautifier bu = Beautifier.getBeautifier();

                if (beautify) {
                    return resHeader + bu.beautifySearch(feedbackString);
                } else {
                    return resHeader + bu.beautifySearch(feedbackString, true);
                }
            } else {
                return resHeader + feedback.getString("ERROR");
            }

        } catch (IOException e) {
            println(e.getMessage());
        }

        return "Search error, please try again or check network connection!";
    }


    public String add(String word, String meaning) {
        //assume all the word and meaning reach here are valid
        // todo : finish this and test $*$ can keep the format
        return "";
    }

    private void goodbye() throws IOException {
        output.writeUTF("$bye");
        output.flush();

        input.close();
        output.close();
        socket.close();

        input = null;
        output = null;
        socket = null;
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
