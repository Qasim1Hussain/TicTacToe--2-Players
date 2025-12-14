import java.io.IOException;

public class GameState {
	private ClientHandler player1;
	private ClientHandler player2;
	private ClientHandler currentPlayer;
	private ServerMain serverMain;
	private String[][] gameBoard = new String[3][3];
	
	
	/**
     * Constructor for Game State
     * 
     * @param player1 is the player that first joined
     * @param player2 is the player that second joined
     * @param server is the socket server
     */
	public GameState(ClientHandler player1, ClientHandler player2, ServerMain server) {
		this.player1 = player1;
		this.player2 = player2;
		this.currentPlayer = player1;
		this.serverMain = server;
		initBoard();
	}
	
	private void initBoard() {
		for (int i =0; i < 3; i++) {
			for (int j = 0; j<3; j++) {
				gameBoard[i][j] = " ";
			}
		}
	}
	/**
     * Function to reset the the Game board from the serverSide
     */
	public void resetBoard() {
		for (int i =0; i < 3; i++) {
			for (int j = 0; j<3; j++) {
				gameBoard[i][j] = " ";
			}
		}
		currentPlayer = player1;
	}
	
	/**
     * Function to call GameState on whether the move made cause anyone to win or draw or nothing
     * 
     * @param row row of of clicked box from 3x3 gameBoard on client side
     * @param col column of of clicked box from 3x3 gameBoard on client side
     * @param player is the client that made that move
     * @return whether there is "Draw", "Win" or "Continue" the game
     */
	public synchronized String performMove(int row, int col, ClientHandler player) {
		// Check whether player making invalid move
		if(!checkBlockAvaliability(row, col, player)) {
			System.out.println("Invalid Move in Game State");
			return "Invalid";
		}
		
		fillGameBoard(row, col, player);
		
		if (checkWin()) {
			try {
				if (player == player1) {
					serverMain.playerWins(player1);
				}else {
					serverMain.playerWins(player2);
				}
			}catch (IOException e){
				e.printStackTrace();
			}
			return "Win: ";
		}
		
		if (isDraw()) {
			try {
				serverMain.drawGame();
			}catch (IOException e){
				e.printStackTrace();
			}
			return "Draw: ";
		}
		
		currentPlayer = (currentPlayer == player1) ? player2: player1;
		return "Continue: ";
		
	}
	
	/**
     * Function to check whether the move that is just made is not already taken and move made is not done my other player
     * 
     * @param row row of of clicked box from 3x3 gameBoard on client side
     * @param col column of of clicked box from 3x3 gameBoard on client side
     * @param player is the client that made that move
     * 
     * @return Whether the move is make by right player or not and whether it is taken or not
     */
	public synchronized boolean checkBlockAvaliability(int row, int col, ClientHandler player){
		if (!gameBoard[row][col].equals(" ")) {
			//System.out.println("Already Taken Block");
			return false;
		}
		if (player != currentPlayer) {
			//System.out.println("NO Current player");
			return false;
		}
		return true;
	}
	
	
	/**
     * Function to fill the Game Board on server side
     * 
     * @param row row of of clicked box from 3x3 gameBoard on client side
     * @param col column of of clicked box from 3x3 gameBoard on client side
     * @param player is the client that made that move
     */
	public synchronized void fillGameBoard(int row, int col, ClientHandler player) {
		if (player == player1) {
			gameBoard[row][col] = "X";
		}else {
			gameBoard[row][col] = "O";
		}
	}
	
	/**
     * Function to check winning
     * 
     * @return whether there is win or not
     */
	public synchronized boolean checkWin() {
		for (int i=0; i<3; i++) {
			if (!gameBoard[0][i].equals(" ") && (gameBoard[0][i].equals(gameBoard[1][i])) && (gameBoard[1][i].equals(gameBoard[2][i]))) {
				return true;
			}
		}
		for (int i=0; i<3; i++) {
			if (!gameBoard[i][0].equals(" ") && (gameBoard[i][0].equals(gameBoard[i][1])) && (gameBoard[i][1].equals(gameBoard[i][2]))) {
				return true;
			}
		}
		if (!gameBoard[0][0].equals(" ") && (gameBoard[0][0].equals(gameBoard[1][1])) && (gameBoard[1][1].equals(gameBoard[2][2]))) {
			return true;
		}
		
		if (!gameBoard[0][2].equals(" ") &&(gameBoard[0][2].equals(gameBoard[1][1])) && (gameBoard[1][1].equals(gameBoard[2][0]))) {
			return true;
		}
		return false;
	}
	
	/**
     * Function to check draw
     * 
     * @return whether there is draw or not
     */
	public synchronized boolean isDraw() {
		for (int i =0; i < 3; i++) {
			for (int j = 0; j<3; j++) {
				if (gameBoard[i][j].equals(" ")) {
					return false;
				}
			}
		}
		return true;
	}
	
}
