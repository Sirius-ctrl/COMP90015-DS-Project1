package client;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

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
            // send out search query
            String resHeader = "==== " + word + " ====\n\n";
            output.writeUTF(obj.toString());
            output.flush();

            // parse the server results, should like {SUCCESS:{meaning:the_meaning_of_the_word, ...extra:info}}
            JSONObject queryResult = new JSONObject(input.readUTF());


//            // query finish, disconnect with server
//            Feedback goodbyeFeedback = goodbye();
//            if (goodbyeFeedback.getFeedbackType() == FeedbackType.ERROR) {
//                return goodbyeFeedback.getMessage();
//            } else {
//                println(goodbyeFeedback.getMessage());
//            }

            if (queryResult.has(FeedbackType.SUCCESS.toString())) {

                JSONObject subContext = new JSONObject(queryResult.getString(FeedbackType.SUCCESS.toString()));

                if (subContext.has("reservedFormat")) {
                    beautify = !subContext.getBoolean("reservedFormat");
                }

                Beautifier bu = Beautifier.getBeautifier();

                println("==== search query succeed ====");
                if (beautify) {
                    return resHeader + bu.beautifySearch(subContext.getString("meaning")) + "\n"
                            + (subContext.has("extraMeaning")?subContext.getString("extraMeaning"):"");
                } else {
                    return resHeader + bu.beautifySearch(subContext.getString("meaning"), true) + "\n"
                            + (subContext.has("extraMeaning")?subContext.getString("extraMeaning"):"");
                }
            } else {
                println("==== search query failed ====");
                return resHeader + queryResult.getString(FeedbackType.ERROR.toString());
            }

        } catch (IOException | JSONException e) {
            println(e.getMessage());
        }

        return "Search error, sever may failed or check your won network connection!";
    }


    public String add(HashMap<String, Object> parameters) {
        // add query {add:word, others:{meaning:the_meaning_of_word,...extra:info}}
        JSONObject obj = new JSONObject();

        obj.put("add", parameters.get("word"));
        parameters.remove("add");
        JSONObject others = new JSONObject(parameters);
        obj.put("others", others.toString());
        println(obj.toString());

        // connect with server and handle error if not success
        Feedback connectFeedback = connect();
        if (connectFeedback.getFeedbackType() == FeedbackType.ERROR) {
            return connectFeedback.getMessage();
        } else {
            println(connectFeedback.getMessage());
        }

        try {
            output.writeUTF(obj.toString());
            output.flush();

            JSONObject queryResult = new JSONObject(input.readUTF());

//            // word addition query finish, close the connection
//            Feedback goodbyeFeedback = goodbye();
//            if (goodbyeFeedback.getFeedbackType() == FeedbackType.ERROR) {
//                return goodbyeFeedback.getMessage();
//            } else {
//                println(goodbyeFeedback.getMessage());
//            }

            // now processing the query feedback
            if (queryResult.has(FeedbackType.SUCCESS.toString())) {
                println("==== addition query succeed ==== ");
                return queryResult.getString(FeedbackType.SUCCESS.toString());
            } else {
                println("==== addition query failed ====");
                return queryResult.getString(FeedbackType.ERROR.toString());
            }

        } catch (IOException e) {
            println(e.getMessage());
        }

        return "Addition error, sever may failed or check your won network connection!";
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

//            // deletion finish now close the connection
//            Feedback goodbyeFeedback = goodbye();
//            if (goodbyeFeedback.getFeedbackType() == FeedbackType.ERROR) {
//                return goodbyeFeedback.getMessage();
//            } else {
//                println(goodbyeFeedback.getMessage());
//            }

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

        return "Deletion error, sever may failed or check your won network connection!";
    }


    private Feedback connect() {
        if ((socket != null) && (input != null) && (output != null)) {
            if (socket.isConnected()) {

                try {
                    output.writeUTF("$beat");
                    return new Feedback(FeedbackType.SUCCESS, "The connection still alive");
                } catch (IOException e) {
                    println("Server restart, try to rebuilt connection");
                }
            }
        }

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

        return new Feedback(FeedbackType.SUCCESS, "==== goodbye ====");
    }


    public void closeAll() {

        if (socket != null && socket.isConnected()) {
            // tell sever the client close the application to free the sever resources
            Feedback goodbyeFeedback = goodbye();
            println(goodbyeFeedback.getMessage());
        }

        println("cleaned up");
    }


    public static void println(String thing) {
        System.out.println(thing);
    }

}
