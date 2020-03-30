package client;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        final String ip_addr = args[0];
        final int port = Integer.decode((args[1]));
        println("client connecting " + ip_addr + ":" + port);
        connect(ip_addr, port);
    }


    public static void connect(String ip_addr, int port) {

        try {
            Socket socket = new Socket(ip_addr, port);

            println("==== connected with the client ====");
            // create data stream for socket
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            Scanner userIn = new Scanner(System.in);

            while (true) {
                System.out.print("Message: ");
                String msg = userIn.nextLine();

                if (msg.isEmpty() || msg.equals("$bye")) {
                    output.writeUTF(msg);
                    output.flush();
                    break;
                }

                output.writeUTF(msg);
                output.flush();

                println("==== waiting for responds ====");
                String message = input.readUTF();
                println(message);
            }

            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            println("client socket error: ");
            println(e.getMessage());
        }
    }

    public static void println(String thing) {
        System.out.println(thing);
    }
}
