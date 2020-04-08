package server;

import com.beust.jcommander.ParameterException;
import server.ThreadPool;
import server.SimpleTask;
import server.DicSocket;


import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.beust.jcommander.JCommander;

import com.beust.jcommander.Parameter;

public class Server {
	// parameters variable
	@Parameter(names={"--port", "-p"}, description = "Port number for listening")
	private static int port = 3456;
	@Parameter(names={"--dict", "-d"}, description = "Path to dictionary")
	private static String dict_path = "./dictionary.json";
	@Parameter(names={"--nworkers", "-n"}, description = "Number of workers (Thread)")
	private static int workers = 10;
	@Parameter(names={"--inactive, -i"}, description = "The longest time (sec) we can wait for the respond before disconnected.")
	private static int inactiveWaitingTime = 300;
	@Parameter(names={"--autosave, -a"}, description = "The longest time to auto backup dictionary data")
	private static int autoSaveTime = 600;
	@Parameter(names = {"--help"}, help = true)
	private static boolean help = false;

	private static ServerSocket server;
	private static ThreadPool pool;
	private static Thread autoSaver;
	private static int saverCounter;

    public static void main(String...args) {
		// parse the command line arguments

		try {
			JCommander commander = JCommander.newBuilder().addObject(new Server()).build();
			commander.parse(args);

			if (help) {
				commander.usage();
				return;
			}
		} catch (ParameterException e) {
			println(e.getMessage());
			println("Please use -h or --help for the usage");
			return;
		}

		saverCounter = autoSaveTime;

	    println("Listening on " + port + " using " + dict_path);

		Runtime.getRuntime().addShutdownHook(new Thread(Server::closeAll));

		// create a thread to auto backup the server every X minutes  (10 minutes by default)
		autoSaver = new Thread(() -> {
			while(true) {
				try {

					Thread.sleep(1000);
					saverCounter--;

					if(saverCounter <= 0) {
						println(Dictionary.getDictionary().writeBack().getMessage());
						saverCounter = autoSaveTime;
					}

				} catch (InterruptedException e) {
					println("system print failed");
				}
			}
		});
		autoSaver.start();

		pool = new ThreadPool(workers);

		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		Dictionary my_dict = Dictionary.getDictionary(dict_path);

		try {
			server = factory.createServerSocket(port);
			println("server init successfully, now waiting for connection");

			while (true) {
				Socket client = server.accept();
				client.setSoTimeout(inactiveWaitingTime*1000);

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
			if (server != null) {server.close();}
			autoSaver.interrupt();
		} catch (IOException e) {
			println("closing error, now force quit!");
		}

		println(Dictionary.getDictionary().writeBack().getMessage());
		println("cleaned up");
	}
}
