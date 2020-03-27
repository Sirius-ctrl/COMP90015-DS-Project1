package server;

import server.ThreadPool;
import server.SimpleTask;
import server.DicSocket;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {

	    final int port = Integer.decode(args[0]);
	    final String dict = args[1];
	    println("Listening on " + port + " using " + dict);

	    println("now testing the thread pool with " + args[2] + " workers");
		ThreadPool pool = new ThreadPool(Integer.parseInt(args[2]));

		ServerSocketFactory factory = ServerSocketFactory.getDefault();

		try {
			ServerSocket server = factory.createServerSocket(port);
			println("server init successfully, now waiting for connection");

			while (true) {
				Socket client = server.accept();
				Runnable connection = new DicSocket(client);
				pool.exec(connection);
			}

		} catch (IOException e){
			println("Error occur when creating server sockets");
			println(e.getMessage());
		}


    }

    public static void println(String thing) {
    	System.out.println(thing);
	}
}
