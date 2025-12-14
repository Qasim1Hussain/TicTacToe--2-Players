import java.net.*;

import javax.swing.ImageIcon;

import java.awt.Image;
import java.io.*;

/**
 * This is Client handler for each client, which is accountable of receiving the message from client side
 * and pass the message to server and vice versa.
 * It is also responsible for closing the player socket
 */

public class ClientHandler implements Runnable {
	
	private Socket socket;
	private ServerMain serverMain;
	private String clientName;
	private BufferedReader in;
	private BufferedWriter out;
	public String playerType;
	
	/**
     * Constructor for Client handler
     * 
     * @param socket refer to socket connection of client
     * @param serverMain refers ServerMain instance
     * @param playerNamer refers to whether client is player1 or player2
     */
	
	public ClientHandler(Socket socket, ServerMain serverMain, String playerName) throws IOException {
		this.socket = socket;
		this.serverMain = serverMain;
		this.playerType = playerName;
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	/**
     * Function to bypass the client message to server like ClosePlayerConnection, ResetBoard, etc.
     *
     */
	@Override
	public void run() {
		try {	
			while(true) {
				String action = in.readLine();
				if(action != null) {
					serverMain.playerActionHandler(action, this);	
				}
			}
		}catch(IOException e) {
			System.out.println("Connection error with client of " + clientName);
		} finally {
			try {
				serverMain.handlingPlayerDisconnection(this);
				//socket.close();			
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
     * Function to send message to client
     *
     * @param msg is the message type like Score Update, ResetBoard, and draw
     */
	public void sendMessage(String msg) throws IOException {
		out.write(msg);
		out.newLine();
		out.flush();
	}
	
	/**
     * Function to close the client connection from sever when ever the client left the game 
     */
	public void closeConnection() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
		
}
