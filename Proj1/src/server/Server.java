package server;

import com.beust.jcommander.ParameterException;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.beust.jcommander.JCommander;

import com.beust.jcommander.Parameter;

import static utils.Logger.*;

public class Server {
	// parameters variable
	@Parameter(names={"--port", "-p"}, description = "Port number for listening")
	private static int port = 3456;
	@Parameter(names={"--dict", "-d"}, description = "Path to dictionary")
	private static String dictPath = "./dictionary.json";
	@Parameter(names={"--nworkers", "-n"}, description = "Number of workers (Thread)")
	private static int workers = 10;
	@Parameter(names={"--inactive", "-i"}, description = "The longest time (sec) we can wait for the respond before disconnected.")
	private static int inactiveWaitingTime = 300;
	@Parameter(names={"--autosave", "-a"}, description = "The longest time to auto backup dictionary data")
	private static int autoSaveTime = 600;
    @Parameter(names={"--suggestions", "-s"}, description = "Max number of suggest for fuzzy search, set 0 to disable")
	private static int suggestions = 8;
	@Parameter(names = {"-h","--help"}, help = true)
	private static boolean help = false;


	private static ServerSocket serverSocket;
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
			logError(e.getMessage() + "\nPlease use -h or --help for the usage");
			return;
		}

		saverCounter = autoSaveTime;

	    log("Listening on " + port + " using " + dictPath);

		Runtime.getRuntime().addShutdownHook(new Thread(Server::closeAll));

		// create a thread to auto backup the server every X minutes  (10 minutes by default)
		autoSaver = new Thread(() -> {
			while(true) {
				try {

					Thread.sleep(1000);
					saverCounter--;

					if(saverCounter <= 0) {
						logFeedback(Dictionary.getDictionary().writeBack());
						saverCounter = autoSaveTime;
					}

				} catch (InterruptedException e) {
					logError("autoSave thread get interrupted");
				}
			}
		});
		autoSaver.start();

		pool = new ThreadPool(workers);

		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		Dictionary myDict = Dictionary.getDictionary(dictPath, suggestions);

		try {
			serverSocket = factory.createServerSocket(port);
			log("server init successfully");

			while (true) {
				Socket client = serverSocket.accept();
				client.setSoTimeout(inactiveWaitingTime*1000);

				Runnable connection = new DicSocket(client, myDict);
				pool.exec(connection);
				log("New Request Received");
			}

		} catch (IOException e){
			logError(e.getMessage());
		}
    }


	/**
	 * house-keeping function to cleanup when program terminate
	 */
	public static void closeAll() {

		try {
			if (serverSocket != null) {serverSocket.close();}
		} catch (IOException e) {
			logError("closing error, now force quit!");
		}

		logFeedback(Dictionary.getDictionary().writeBack());

		log("cleaned up");
	}
}
