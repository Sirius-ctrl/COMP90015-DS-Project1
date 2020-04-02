package client;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

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

//
//    public static void connect(String ip_addr, int port) {
//
//        try {
//            socket = new Socket(ip_addr, port);
//
//            println("==== connected with the client ====");
//            // create data stream for socket
//            input = new DataInputStream(socket.getInputStream());
//            output = new DataOutputStream(socket.getOutputStream());
//
//            Scanner userIn = new Scanner(System.in);
//
//            while (true) {
//                System.out.print("Message: ");
//                // the msg should be a json string
//                String msg = userIn.nextLine();
//
//
//                if (msg.isEmpty() || msg.equals("$bye")) {
//                    output.writeUTF(msg);
//                    output.flush();
//                    break;
//                }
//
//                output.writeUTF(msg);
//                output.flush();
//
//                println("==== waiting for responds ====");
//                String message = input.readUTF();
//                println(message);
//            }
//
//            // close all when finish
//            input.close();
//            output.close();
//            socket.close();
//        } catch (IOException e) {
//            println("client socket error: ");
//            println(e.getMessage());
//        }
//    }


    public String connect() {

        try {
            socket = new Socket(ip_addr, port);

            println("==== initiate connection with the sever ====");
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            return "Connection initiate successfully, please enter the word you want to search/add/remove";

        } catch (IOException e) {
            println("Connection initiate failed!");
            exit(1);
        }

        return "";
    }

    public static void println(String thing) {
        System.out.println(thing);
    }


    public String delete(String word) {
        println("del " + word);
        return "SUCCESS";
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
