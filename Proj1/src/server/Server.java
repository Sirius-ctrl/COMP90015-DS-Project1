package server;

import server.ThreadPool;
import server.SimpleTask;
import server.DicSocket;


import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.beust.jcommander.JCommander;

import com.beust.jcommander.Parameter;

import static java.lang.System.exit;

public class Server {
	// parameters variable
	@Parameter(names={"--port", "-p"}, description = "Port number for listening")
	private static int port = 3456;
	@Parameter(names={"--dict", "-d"}, description = "Path to dictionary")
	private static String dict_path = "./dictionary.json";
	@Parameter(names={"--nworkers", "-n"}, description = "Number of workers (Thread)")
	private static int workers = 10;


	private static ServerSocket server;
	private static ThreadPool pool;

    public static void main(String...args) {
		// parse the command line arguments
		// todo : regulate input
		JCommander.newBuilder().addObject(new Server()).build().parse(args);

	    println("Listening on " + port + " using " + dict_path);

		Runtime.getRuntime().addShutdownHook(new Thread(Server::closeAll));

		pool = new ThreadPool(workers);

		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		Dictionary my_dict = new Dictionary(dict_path);

		try {
			server = factory.createServerSocket(port);
			println("server init successfully, now waiting for connection");

			while (true) {
				Socket client = server.accept();
				Runnable connection = new DicSocket(client, my_dict);
				pool.exec(connection);
				System.out.println("==== New Request Received ====");
			}

		} catch (IOException e){
			println("Error occur when creating server sockets");
			println(e.getMessage());
		}


    }

    public static void println(String thing) {
    	System.out.println(thing);
	}

	public static void closeAll() {

		try {
			server.close();
			println("cleaned up");
		} catch (IOException e) {
			println("closing error, now force quit!");
		}
	}
}
