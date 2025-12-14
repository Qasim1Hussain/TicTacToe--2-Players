import java.net.*;
import java.io.*;

/**
 * Class to create a Client socket and make connection with server
 */

public class ClientMain {
	
	/**
     * Function to just create an instance of Client Main class
     * 
     * @param args to receive argument from CLI
     */
	public static void main(String[] args) throws IOException {
		ClientMain client = new ClientMain();
		String serverAddress = "Localhost";
		int port = 12346;

		if(args.length >=1){
			serverAddress = args[0];
		}

		if (args.length >=2){
			port = Integer.parseInt(args[1]);
		}

		client.go(serverAddress, port);
	}
	
	/**
	 * Function to Make connection with Server and call for
	 */
	public void go(String serverAddress, int port) throws UnknownHostException, IOException {
		Socket socket = new Socket(serverAddress, port);
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		System.out.println("Client is Online");
		
		TicTacToeGUI gui = new TicTacToeGUI(socket, in, out);
	    gui.go();
		
	}
}
