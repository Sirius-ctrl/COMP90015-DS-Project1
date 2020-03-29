package client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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

            println("connected with the client");
            // create data stream for socket
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            String helloData = "I want to connect";

            output.writeUTF(helloData);
            output.flush();

            while (input.available() > 0) {
                String message = input.readUTF();
                println(message);
            }

        } catch (IOException e) {
            println("client socket error: ");
            println(e.getMessage());
        }
    }

    public static void println(String thing) {
        System.out.println(thing);
    }
}
