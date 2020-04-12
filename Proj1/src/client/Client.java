package client;
import com.beust.jcommander.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

import utils.*;

import static utils.Logging.*;

public class Client {

    // parameters variable
    @Parameter(names={"--port", "-p"}, description = "Port number to connect server")
    private static int port = 3456;
    @Parameter(names={"--addr", "-a"}, description = "IP address to server")
    private static String ip_addr = "127.0.0.1";
    @Parameter(names={"--help", "-h"}, help = true)
    private static boolean help = false;

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

    /**
     * Search word in server dictionary
     * @param word The word this will be query
     * @param beautify whether you want the result to be beautify
     * @return the result string, if success then return the meaning, otherwise return the error message
     */
    public String search(String word, boolean beautify) {

        Feedback connectFeedback = connect();

        logFeedback(connectFeedback);
        // build connection first
        if (connectFeedback.getFeedbackType() == FeedbackType.ERROR) {
            return connectFeedback.getMessage();
        }

        //make query
        JSONObject obj = new JSONObject();
        obj.put(Actions.SEARCH.toString(), word);
        log("query send: " + obj.toString());

        try {
            // send out search query
            String resHeader = "==== " + word + " ====\n\n";
            output.writeUTF(obj.toString());
            output.flush();

            // parse the server results, should like {SUCCESS:{meaning:the_meaning_of_the_word, ...extra:info}}
            JSONObject queryResult = new JSONObject(input.readUTF());

            if (queryResult.has(FeedbackType.SUCCESS.toString())) {

                JSONObject subContext = new JSONObject(queryResult.getString(FeedbackType.SUCCESS.toString()));

                if (subContext.has("reservedFormat") && subContext.getBoolean("reservedFormat")) {
                    beautify = false;
                }

                log("search query succeed");
                if (beautify) {
                    return resHeader + Beautifier.beautifySearch(subContext.getString("meaning")) + "\n"
                            + (subContext.has("extraMeaning")?subContext.getString("extraMeaning"):"");
                } else {
                    return resHeader +Beautifier.beautifySearch(subContext.getString("meaning"), true) + "\n"
                            + (subContext.has("extraMeaning")?subContext.getString("extraMeaning"):"");
                }
            } else {
                log("search query failed ====");
                return resHeader + queryResult.getString(FeedbackType.ERROR.toString());
            }

        } catch (JSONException e) {
            logError("Bad Data: " + e.getMessage());
            return "Bad Data！Search again Please!";
        } catch (EOFException e) {
            // no more input from the input stream means server failed
            logError("Server down! Please Context server provider");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        socket = null;
        input = null;
        output = null;

        return "Search error, sever may failed or check your network connection!";
    }


    /**
     * Add a new word to server dictionary
     * @param parameters all the key,value pairs in add query
     * @return the result string, either success or error messages
     */
    public String add(HashMap<String, Object> parameters) {
        // make add query {add:word, others:{meaning:the_meaning_of_word,...extra:info}}
        JSONObject obj = new JSONObject();
        obj.put(Actions.ADD.toString(), parameters.get("word"));
        parameters.remove("word");
        JSONObject others = new JSONObject(parameters);


        if(!others.getBoolean("reservedFormat")) {
            log("cleaning format");
            others.put("meaning", Beautifier.cleanFormat(others.getString("meaning")));
        }

        obj.put("others", others.toString());

        log("add query: " + obj.toString());

        // connect with server and handle error if not success
        Feedback connectFeedback = connect();
        logFeedback(connectFeedback);

        if (connectFeedback.getFeedbackType() == FeedbackType.ERROR) {
            return connectFeedback.getMessage();
        }

        try {
            output.writeUTF(obj.toString());
            output.flush();

            JSONObject queryResult = new JSONObject(input.readUTF());

            // now processing the query feedback
            if (queryResult.has(FeedbackType.SUCCESS.toString())) {
                log("addition query succeed");
                return queryResult.getString(FeedbackType.SUCCESS.toString());
            } else {
                log("addition query failed");
                return queryResult.getString(FeedbackType.ERROR.toString());
            }

        } catch (JSONException e) {
            logError("Bad Data: " + e.getMessage());
            return "Bad Data！Search again Please!";
        } catch (EOFException e) {
            // no more input from the input stream means server failed
            logError("Server down! Please Context server provider");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        socket = null;
        input = null;
        output = null;

        return "Addition error, sever may failed or check your network connection!";
    }

    /**
     * delete a word from the server dictionary
     * @param word the word you want to delete from the server dictionary
     * @return either success message or error message
     */
    public String del(String word) {

        // connect with server and handle error if not success
        Feedback connectFeedback = connect();
        logFeedback(connectFeedback);
        if (connectFeedback.getFeedbackType() == FeedbackType.ERROR) {
            return connectFeedback.getMessage();
        }

        JSONObject obj = new JSONObject();
        obj.put(Actions.DEL.toString(), word);

        try {
            output.writeUTF(obj.toString());
            output.flush();

            JSONObject query = new JSONObject(input.readUTF());

            // now processing the query feedback
            if (query.has(FeedbackType.SUCCESS.toString())) {
                log("deletion query succeed");
                return query.getString(FeedbackType.SUCCESS.toString());
            } else {
                log("deletion query failed");
                return query.getString(FeedbackType.ERROR.toString());
            }

        } catch (JSONException e) {
            logError("Bad Data: " + e.getMessage());
            return "Bad Data！Search again Please!";
        } catch (EOFException e) {
            // no more input from the input stream means server failed
            logError("Server down! Please Context server provider");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        socket = null;
        input = null;
        output = null;

        return "Deletion error, sever may failed or check your network connection!";
    }


    /**
     * house-keeping function to cleanup when program terminate
     */
    public void closeAll() {

        if (socket != null && socket.isConnected()) {
            // tell sever the client close the application to free the sever resources
            Feedback goodbyeFeedback = goodbye();
            logFeedback(goodbyeFeedback);
        }

        log("cleaned up");
    }


    /**
     * whether -h/--help is passed to the program
     * @return true if we want to display the help, otherwise false
     */
    public static boolean isHelp() { return help; }

    public static String getIp_addr() { return ip_addr; }

    public static int getPort() { return port; }



    private Feedback connect() {
        if ((socket != null) && (input != null) && (output != null)) {
            if (socket.isConnected()) {
                return new Feedback(FeedbackType.SUCCESS, "==== still connected ====");
            }
        }

        try {
            socket = new Socket(ip_addr, port);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            return new Feedback(FeedbackType.SUCCESS, "connection built!");
        } catch (IOException e) {
            return new Feedback(FeedbackType.ERROR,"Connection failed! " +
                    "The server might not be available yet!\nPlease try again later or check network connection!");
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

        return new Feedback(FeedbackType.SUCCESS, "==== goodbye ====");
    }

}
