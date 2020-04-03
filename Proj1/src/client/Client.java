package client;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

import static java.lang.System.exit;

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


    public String connect() {

        try {
            socket = new Socket(ip_addr, port);

            println("==== initiate connection with the sever ====");
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            return "Connection initiate successfully, \nplease enter the word you want to search/add/remove";

        } catch (IOException e) {
            println("==== Connection initiate failed! The server might not be available yet! ====");
            exit(1);
        }

        return "";
    }

    public static void println(String thing) {
        System.out.println(thing);
    }


    public String search(String word, boolean beautify) {
        JSONObject obj = new JSONObject();
        obj.put("search", word);
        println(obj.toString());

        try {
            String resHeader = "==== " + word + " ====\n\n";
            output.writeUTF(obj.toString());
            output.flush();

            JSONObject feedback = new JSONObject(input.readUTF());

            if (feedback.has("SUCCESS")) {

                String feedbackString = feedback.getString("SUCCESS");

                // todo: test this is actually working
                // reserved starting substring that means user want to keep the format
                if (feedbackString.substring(0,3).equals("$*$")) {
                    beautify = false;
                }

                if (beautify) {
                    return resHeader + beautifySearch(feedbackString);
                } else {
                    return resHeader + feedbackString;
                }
            } else {
                return resHeader + feedback.getString("ERROR");
            }

        } catch (IOException e) {
            println(e.getMessage());
        }

        return "Search error, please try again or check network connection!";
    }


    public String beautifySearch(String context) {
        StringBuilder res = new StringBuilder();
        boolean firstSeen = true;

        // check whether we need to beautify the string (skip the user add string)
        for (int i = 0; i < context.length(); i++) {
            if (context.charAt(i) == '-') {
                if ((i+2 < context.length()) && (context.charAt(i+1) == '-')){
                    if (context.charAt(i-1) == '\n'){
                        return context;
                    }
                }
            }
        }

        for (int i = 0; i < context.length(); i++) {

            if (context.charAt(i) == '-') {
                if ((i+2 < context.length()) && (context.charAt(i+1) == '-')){
                    if (firstSeen) {
                        res.append("\n\n**** useful terms ****\n");
                        firstSeen = false;
                    }

                    // two -- occur, i+1 for skip the current --
                    int endSection = chopTermSection(context, i+1);
                    // -1 as otherwise it will skip the next one
                    String subsection = context.substring(i, endSection-1);

                    res.append("\n").append(indentationCorrecter(subsection, "\n      "));

                    i = endSection-1;
                }
            } else if (firstSeen) {
                // not yet reach the terms section
                int endSection = chopNotTermSection(context, i);
                String subsection = context.substring(i, endSection);
                res.append(indentationCorrecter(subsection+"\n", "\n"));
                i = endSection;
            } else {
                res.append(context.charAt(i));
            }
        }

        return res.toString();
    }


    public int chopNotTermSection(String context, int start) {
        int end;
        //find the next \n

        for (end = start; end < context.length(); end++) {
            // if we walk into terms section, just early return
            if (context.charAt(end) == '-'){
                if ((end+1 < context.length()) && (context.charAt(end+1) == '-')) {
                    return end-1;
                }
            }

            if (context.charAt(end) == '\n'){
                return end;
            }
        }

        return end;
    }


    private int chopTermSection(String context, int start) {
        int end;
        // find the position of next proper "-"
        for (end = start+1; end < context.length(); end++) {
            if(context.charAt(end) == '-') {
                if ((end+1 < context.length()) && (context.charAt(end+1) == '-')) {
                    return end;
                }
            }
        }

        return end;
    }

    private String indentationCorrecter (String context, String indentation) {
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < context.length(); i++) {

            if (i < context.length()-3) {
                // found things like |2. |
                if (Character.isDigit(context.charAt(i)) && context.charAt(i+1) == '.' && context.charAt(i+2) == ' ') {
                    if (context.charAt(i+3) == '(' || Character.isAlphabetic(context.charAt(i+3))) {
                        res.append(indentation).append(context, i, i+3);
                        i += 3;
                    }
                }
            }
            res.append(context.charAt(i));
        }

        return res.toString();
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

}
