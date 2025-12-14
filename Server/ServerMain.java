import java.net.*;
import java.io.*;

/**
 * This is Server Main, which is accountable of creating Server Sockets and client connection
 * Receiving and Sending messages to/from client and call relevant classes for processing.
 */
public class ServerMain {
	final int PORT = 12346;
	private ClientHandler handler1;
	private ClientHandler handler2;
	private GameState gameState;
	private boolean isFreshMatch = true;
	
	private int player1wins = 0;
	private int player2wins = 0;
	private int draws = 0;
	
	private boolean p1NameSubmit = false;
	private boolean p2NameSubmit = false;
	
	
	/**
     * Function to just create an instance of ServerMain class
     */
	public static void main(String[] args) throws IOException {
		ServerMain server = new ServerMain();
		server.go();
	}
	
	/**
     * Function to create server sockets and client sockets when client has made connection.
     * Maximum of two clients
     */
	public void go() throws IOException {
		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("Server started. Waiting for client to connect...");
		
		while (true) {
			Socket playerSocket = serverSocket.accept();
			ClientHandler handler= null;
			
			if(handler1 == null) {
				//System.out.println("Waiting for Player 1 to connect");
			
				handler =  new ClientHandler(playerSocket, this, "Player1");
				handler1 = handler;
				Thread t1 = new Thread(handler1);
				t1.start();
				handler1.sendMessage("NewGame: Player1");
			}else if (handler2== null) {
				System.out.println("Waiting for Player 2 to connect");

				handler =  new ClientHandler(playerSocket, this, "Player2");
				handler2 = handler;
				Thread t2 = new Thread(handler2);
				t2.start();
					
				//startNewGame();
			}else {
				try {
					handler =  new ClientHandler(playerSocket, this, "temp");
					handler1 = handler;
					handler.sendMessage("Maximum Player Reached.");
					playerSocket.close();
				}catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
     * Function to receive the messages from client side like Names, moves, ResetBoard, Closing Connection of leaving player
     * 
     * @param action are message type client
     * @param sender is the player whether player 1 or player 2
     */
	public void playerActionHandler(String action, ClientHandler sender) throws IOException {
		System.out.println("Message from Client " + action);
		
		if (action != null  && action.startsWith("Move: ")) {
			if (gameState == null) {
				//System.out.println("Game State is Null, Cannot perform the movement ");
				return;
			}
			
			String[] move = action.substring(6).split(",");
			int row = Integer.parseInt(move[0]);
			int col = Integer.parseInt(move[1]);
			try {
				processMove(row, col, sender);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}else if (action !=null && action.startsWith("ResetBoard: ")) {
			//System.out.println("Reseting the Game Board from Server");
			handler1.sendMessage("Connection: Please make your turn.");
			handler2.sendMessage("Connection: Wait for Opponent Move.");
			if(gameState !=null) {
				gameState.resetBoard();
				broadcastMessage("ResetBoard: ");
			}else {
				//System.out.println("Reseting the Game Board for new player");
				if(handler1 != null && handler2 != null) {
					System.out.println("Reseting the Game Board if noone left");
					startNewGame();
					
				}
			}
		
		}else if(action !=null && action.startsWith("ClosePlayerConnection: ")){
			System.out.println("Closing the Player connection");
			handlingPlayerDisconnection(sender);
			
		
		}else if(action !=null && action.startsWith("Name: ")){
			String clientName = action.substring(6);
			System.out.println("Player name recieved: " +clientName + " for "+ getPlayerName(sender));
			
			if (sender == handler1) p1NameSubmit = true;
			if (sender == handler2) p2NameSubmit = true;
			if (handler1 != null && handler2 !=null && gameState == null && p1NameSubmit && p2NameSubmit) {
	
				startNewGame();
			}
		}
	}
	
	/**
     * Function to just start new Game when suppose one player left the match and another player just came in.
     */
	public synchronized void startNewGame() throws IOException{
		if (handler1 != null && handler2 != null) {
			System.out.println("Both Players  has joined, starting new game");
			
			if(isFreshMatch) {
				player1wins = 0;
				player2wins = 0;
				draws = 0;
			}
			
			gameState = new GameState(handler1, handler2, this);
			handler1.sendMessage("NewGame: Player1");
			handler2.sendMessage("NewGame: Player2");
			System.out.println("Starting a new game");
			
			handler1.sendMessage("Connection: Opponent have Joined! Make your turn.");
			broadcastMessage("ScoreUpdate: Match,"+ player1wins + "," + player2wins + "," + draws);
			isFreshMatch = false;
		}
	}	
	
	/**
     * Function to disconnect the left player and closing it socket connection
     * 
     * @param LeavingPlayer is the client that left the game
     */
	public synchronized void handlingPlayerDisconnection(ClientHandler leavingPlayer) throws IOException {
		if (leavingPlayer == null) return;
		
		//String leavingPlayerType = (leavingPlayer == handler1) ? "Player1" : "Player2";
		//System.out.println("Handling disconnection for "+ leavingPlayerType);
		//ClientHandler otherPlayer = (leavingPlayer == handler1) ? handler2 : handler1;
		
		ClientHandler otherPlayer= null;
		
		if (leavingPlayer == handler1) {
			System.out.println("Player1 Disconnected");
			otherPlayer = handler2;
			handler1 = null;
			handler2 = null;
			p1NameSubmit = false;
			
		}else if  (leavingPlayer == handler2){
			System.out.println("Player2 Disconnected");
			otherPlayer = handler1;
			handler1 = null;
			handler2 = null;
			p2NameSubmit = false;
		}
		gameState = null;
		leavingPlayer.closeConnection();
		if(otherPlayer !=null) {
			try {
				System.out.println("Notifying other player about disconnection");
				otherPlayer.sendMessage("OtherPlayerLeft: ");
				//otherPlayer.closeConnection();
				handler1= otherPlayer;
				handler1.playerType = "Player1";
				isFreshMatch = true;
				p1NameSubmit = true;
				System.out.println("Now You are player X");
			}catch(IOException e) { e.printStackTrace(); }
		}else {
			isFreshMatch = true;
		}
	}
	
	/**
     * Function to call GameState on whether the move made cause anyone to win or draw or nothing
     * 
     * @param row row of of clicked box from 3x3 gameBoard on client side
     * @param col column of of clicked box from 3x3 gameBoard on client side
     * @param player is the client that made that move
     */
	public void processMove(int row, int col, ClientHandler player) throws IOException {
			
		if (gameState == null){
			player.sendMessage("Game State is Null, wait for player");
			return;
		}
		String result = gameState.performMove(row, col, player);
		String playerType = player ==handler1? "Player1" : "Player2";
			
		if (result.equals("Continue: ")) {
			broadcastPlayerTurns(row, col, player);
		}else if (result.equals("Win: ")) {
			//playerWins(player);
			broadcastWinLoseMessage(row, col, player);
		}else if (result.equals("Draw: ")) {
			//drawGame();
			broadcastMessage("Draw: " + row + "," + col + "," + playerType);
		}
	}
	
	/**
    * Function to Increment the Player wins
    * 
    * @param winner is the client that win that game
    */
	public synchronized void playerWins(ClientHandler winner) throws IOException{
		if (winner == handler1) {
			player1wins++;
			broadcastMessage("ScoreUpdate: Player1,"+ player1wins + "," + player2wins + "," + draws);
			
		}else {
			player2wins++;
			broadcastMessage("ScoreUpdate: Player2,"+ player1wins + "," + player2wins + "," + draws);
		}
	}
	
	/**
     * Function to increment the draw if no client wins that game
     */
	public synchronized void drawGame() throws IOException{
		draws++;
		broadcastMessage("ScoreUpdate: Draw,"+ player1wins + "," + player2wins + "," + draws);
	}
	
	/**
     * Function to send message to one client after the waiting after it make valid move
     * and another client to make its move
     * 
     * @param row row of of clicked box from 3x3 gameBoard on client side
     * @param col column of of clicked box from 3x3 gameBoard on client side
     * @param player is the client that made that move
     */
	public void broadcastPlayerTurns(int row, int col, ClientHandler player) throws IOException {
		String playerType = player ==handler1? "Player1" : "Player2";
		
		handler1.sendMessage("Continue: " + row + "," + col + "," + playerType);
		handler2.sendMessage("Continue: " + row + "," + col + "," + playerType);
		//player.sendMessage("Valid move, wait for your opponent");
		
		if (player == handler1) {
			//handler2.sendMessage("Continue: " + row + "," + col + "," + playerType);
			System.out.println("handler 1: inside ");
			handler2.sendMessage("Your opponent have moved. Now is your turn.");
			handler1.sendMessage("Valid move, wait for your opponent");
		}else {
			//handler1.sendMessage("Continue: " + row + "," + col + "," + playerType);
			System.out.println("handler 2: inside ");
			handler1.sendMessage("Your opponent have moved. Now is your turn.");
			handler2.sendMessage("Valid move, wait for your opponent");
		}
	}
	
	/**
     * Function to send message to one client that has win and another client that has lose
     * 
     * @param row row of of clicked box from 3x3 gameBoard on client side
     * @param col column of of clicked box from 3x3 gameBoard on client side
     * @param player is the client that made that move
     */
	public void broadcastWinLoseMessage(int row, int col, ClientHandler player) throws IOException {
		String winnerPlayer = (player == handler1) ? "Player1" : "Player2";
		String loserPlayer = (player == handler2) ? "Player2" : "Player1";
		player.sendMessage("Win: " + row + "," + col + "," + winnerPlayer);
		if (player == handler1) {
			handler2.sendMessage("Lose: " + row + "," + col + "," + loserPlayer);
		}else {
			handler1.sendMessage("Lose: " + row + "," + col + "," + loserPlayer);
		}
	}
	
	/**
     * Function to send message to both client about general score, reseting board and draw
     *
     * @param msg is the message type like Score Update, ResetBoard, and draw
     */
	public void broadcastMessage(String msg) throws IOException {
		if (handler1 != null) handler1.sendMessage(msg);
		if (handler2 != null) handler2.sendMessage(msg);
	}	

	private String getPlayerName(ClientHandler handler) {
		if (handler ==null) return "Null";
		return(handler == handler1) ? "Player1" : "Player2";
	}
	
	
	
}
