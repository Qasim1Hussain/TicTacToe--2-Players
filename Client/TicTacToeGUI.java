import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This represent the main Tic Tac Toe Game GUI and it is a main window on the client side.
 * It manages the game board, style, score board, player input and time on the client side.
 */
public class TicTacToeGUI {
    private JFrame frame;
    private JPanel panel;
    private JMenuBar menuBar;
    private GameBoard gameBoard;
    private ScoreBoard scoreBoard;
    private JLabel labelplayerNameUpper; 
    private JLabel labelplayerNameLower;
    private JTextField fieldplayerNameLower;
    private JButton submitName;
    private JLabel timeLabel;
    private String PlayerName;
    
    private String playerType;
    private Object[] options = {"Yes", "NO"};
    private Object[] options2 = {"Okey"};
    

    
    BufferedReader in;
    BufferedWriter out;
    Socket socket;
    
    
    private void processReceiveMessage(String message) {
    	System.out.println("Messages from server: "+ message);
    	if(message.startsWith("NewGame: ")) {
			playerType = message.substring(9);
			gameBoard.resetBoard();
			//gameBoard.setBoardEnabled(false);
			//if (playerType.equals("Player1")) {
			//	playerMovedText("Waiting for opponent");
			//}else {
			//	playerMovedText("Both Players here.");
			//}
			
		}else if (message.startsWith("Continue: ")){
			String[] move = message.substring(10).split(",");
			int row = Integer.parseInt(move[0]);
			int col = Integer.parseInt(move[1]);
			String player = move[2];
			gameBoard.updateBoard(row, col, player); 
			
		}else if (message.startsWith("Valid ")){
			playerMovedText(message);
			gameBoard.setBoardEnabled(false);
			
		}else if(message.startsWith("Your ")){
			playerMovedText(message);
			gameBoard.setBoardEnabled(true);
			
		}else if (message.startsWith("Win: ")){
			String[] move = message.substring(5).split(",");
			int row = Integer.parseInt(move[0]);
			int col = Integer.parseInt(move[1]);
			String player = move[2];
			gameBoard.updateBoard(row, col, player); 
		
			ImageIcon infoIcon = new ImageIcon(getClass().getResource("/InformationIcon.png"));
	        Image IconScaled = infoIcon.getImage().getScaledInstance(50,50, Image.SCALE_SMOOTH);
	        ImageIcon infoIconScaled = new ImageIcon(IconScaled);
			int choice = JOptionPane.showOptionDialog(frame, "Congratulations. You wins! Do you want to play again?", "Game Over",
			JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, infoIconScaled, options, options[0]);
			if (choice == 0) {
	        	SendMessagetoServer("ResetBoard: ");
	        }else {
	        	SendMessagetoServer("ClosePlayerConnection: ");
	        	closeGUI();
	        }
		
		}else if (message.startsWith("Lose: ")){
			String[] move = message.substring(6).split(",");
			int row = Integer.parseInt(move[0]);
			int col = Integer.parseInt(move[1]);
			String player = move[2];
			gameBoard.updateBoard(row, col, player); 
			ImageIcon infoIcon = new ImageIcon(getClass().getResource("/InformationIcon.png"));
	        Image IconScaled = infoIcon.getImage().getScaledInstance(50,50, Image.SCALE_SMOOTH);
	        ImageIcon infoIconScaled = new ImageIcon(IconScaled);
			int choice = JOptionPane.showOptionDialog(frame, "You lose. Do you want to play again?", "Game Over",
			JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, infoIconScaled, options, options[0]);
			if (choice == 0) {
	        	SendMessagetoServer("ResetBoard: ");
	        }else {
	        	SendMessagetoServer("ClosePlayerConnection: ");
	        	closeGUI();
	        }
		}else if (message.startsWith("Draw: ")){
			String[] move = message.substring(6).split(",");
			int row = Integer.parseInt(move[0]);
			int col = Integer.parseInt(move[1]);
			String player = move[2];
			gameBoard.updateBoard(row, col, player); 
			ImageIcon infoIcon = new ImageIcon(getClass().getResource("/InformationIcon.png"));
	        Image IconScaled = infoIcon.getImage().getScaledInstance(50,50, Image.SCALE_SMOOTH);
	        ImageIcon infoIconScaled = new ImageIcon(IconScaled);
	        int choice =   JOptionPane.showOptionDialog(frame, "Its a draw! Play again?", "Game Over", 
	        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, infoIconScaled, options, options[0]);
	        if (choice == 0) {
	        	SendMessagetoServer("ResetBoard: ");
	        }else {
	        	SendMessagetoServer("ClosePlayerConnection: ");
	        	closeGUI();
	        }
	      
		}else if (message.equals("OtherPlayerLeft: ")) {
			playerMovedText("Game Started. Wait for your opponent");
			gameBoard.setBoardEnabled(false);			
			ImageIcon infoIcon = new ImageIcon(getClass().getResource("/InformationIcon.png"));
	        Image IconScaled = infoIcon.getImage().getScaledInstance(50,50, Image.SCALE_SMOOTH);
	        ImageIcon infoIconScaled = new ImageIcon(IconScaled);
	        JOptionPane.showOptionDialog(frame, "Game Ends. One of the players left", "Game Over", 
	        JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, infoIconScaled, options2, options[0]);        
	        
	        //try {socket.close();} catch(IOException ex){}
		
		}else if (message.equals("ResetBoard: ")) {
			 gameBoard.resetBoard();	 
		}else if (message.startsWith("ScoreUpdate: ")) {
			String[] words = message.substring(13).split(",");
			String outcome = words[0];
			int player1wins = Integer.parseInt(words[1]);
			int player2wins = Integer.parseInt(words[2]);
			int draws = Integer.parseInt(words[3]);
			scoreBoard.updateScore(player1wins, player2wins, draws);
		}else if (message.startsWith("Connection: ")) {
			String words = message.substring(12);
			System.out.println(words);
			playerMovedText(words);
		}
 		
    }
    
    private class readReceiveMessage implements Runnable {
    	@Override
    	public void run() {
    		try {
        		String message;
        		while((message = in.readLine())!=null) {
        			processReceiveMessage(message);
        		}
        	} catch(IOException e) {
        		e.printStackTrace();
        	}finally {
        		SwingUtilities.invokeLater(()->{
        			playerMovedText("Opponent left. Waiting for new player");
        			gameBoard.setBoardEnabled(false);
        			//gameBoard.resetBoard();
        		});
        	}
    	}
    }
    
    /**
     * Helper function to create a thread for listening to server.
     */
    private void serverMessageListener() {
    	Thread t = new Thread(new readReceiveMessage());
    	t.setDaemon(true);
    	t.start();
    }
    
    /**
     * Initialize the TicTacToe GUI instance.
     * 
     * @param socket A socket connection for players
     * @param in For reading the message from Server
     * @param out For writing the message to Server
     */
    public TicTacToeGUI(Socket socket, BufferedReader in,BufferedWriter out) {
    	this.socket = socket;
    	this.in = in;
    	this.out = out;
    }
   
    /**
     * This initialize the GUI of the TicTacToe Board Using GridBagLayout and build the layout.
     */
    public void go(){
        // Creating a main frame
        frame = new JFrame("Tic Tac Toe");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent e) {
        		SendMessagetoServer("ClosePlayerConnection: ");
        		closeGUI();
        	}
        });
        
        ImageIcon img = new ImageIcon("batman.webp");
        frame.setIconImage(img.getImage());

        // Creating the Panel and GridBagLayout
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        // Creating The Menu Bar
        menuBar = new JMenuBar();

        JMenu menuControl = new JMenu("Control");
        menuControl.setFont(menuControl.getFont().deriveFont(Font.BOLD));
        JMenuItem menuControlItem = new JMenuItem("Exit");
        menuControlItem.setFont(menuControlItem.getFont().deriveFont(Font.BOLD));
        menuControlItem.addActionListener(new menuControlItemExit());
        menuControl.add(menuControlItem);
        menuBar.add(menuControl);
        
        JMenu menuHelp = new JMenu("Help");
        menuHelp.setFont(menuHelp.getFont().deriveFont(Font.BOLD));
        JMenuItem menuHelpItem = new JMenuItem("Instruction");
        menuHelpItem.setFont(menuHelpItem.getFont().deriveFont(Font.BOLD));
        menuHelpItem.addActionListener(new menuHelpItemInstruction());
        menuHelp.add(menuHelpItem);
        menuBar.add(menuHelp);
        frame.setJMenuBar(menuBar);
        

        // Creating the Name
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        c.ipadx = 5;
        c.ipady = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        labelplayerNameUpper = new JLabel("Enter your player name...", SwingConstants.CENTER);
        labelplayerNameUpper.setFont(labelplayerNameUpper.getFont().deriveFont(Font.BOLD));
        panel.add(labelplayerNameUpper, c);      
        
         //Creating The 3 x 3 Grid
        gameBoard = new GameBoard(this);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.7;
        c.gridwidth = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(gameBoard, c);
        gameBoard.setBoardEnabled(false);
  
        
        // Creating the ScoreBoard
        scoreBoard = new ScoreBoard();
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.3;
        c.gridwidth = 1;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        panel.add(scoreBoard,c); 
        

        // Creating name label, field and submit button;
        c.gridx = 0;
        c.gridy= 2;
        c.weightx = 0.2;
        c.gridwidth = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        labelplayerNameLower = new JLabel("Enter your name:");
        labelplayerNameLower.setFont(labelplayerNameLower.getFont().deriveFont(Font.BOLD));
        panel.add(labelplayerNameLower, c);

        //Creating name field;
        c.gridx = 1;
        c.weightx = 0.6;
        c.gridwidth = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        fieldplayerNameLower = new JTextField(20);
        panel.add(fieldplayerNameLower, c);
        
        //Creating name submit button;
        c.gridx = 2;
        c.weightx = 0.2;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        submitName = new JButton("Submit");
        submitName.addActionListener(new submitPlayerName());
        panel.add(submitName, c);
        
        
     // For timer
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        timeLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel.setFont(timeLabel.getFont().deriveFont(16f));
        panel.add(timeLabel, c);
        Timer timer = new Timer(1000, e ->{
            SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
            timeLabel.setText("Current Time: "+ time.format(new Date()));
        });
        timer.start();

        //frame.pack();
        frame.add(panel);
        frame.setSize(600, 600);
        frame.setVisible(true);
        
        serverMessageListener();
    }
    
    /**
     *  Action Listener to exit the game when Exit is Pressed on the Menu item's Control.
     */
	class menuControlItemExit implements ActionListener{
	    public void actionPerformed (ActionEvent event){
	    	SendMessagetoServer("ClosePlayerConnection: ");
        	closeGUI();
	    }
	}
	
	/**
     * Action Listener to read the instruction of Game when Instruction is Pressed on the Menu item's Help.
     */
	class menuHelpItemInstruction implements ActionListener{
	    public void actionPerformed (ActionEvent event){
	        ImageIcon infoIcon = new ImageIcon(getClass().getResource("/InformationIcon.png"));
	        Image IconScaled = infoIcon.getImage().getScaledInstance(50,50, Image.SCALE_SMOOTH);
	        ImageIcon infoIconScaled = new ImageIcon(IconScaled);
	        JOptionPane.showOptionDialog(frame, "Some information about the game:\n" + 
	        "- The move is not occupied by any mark.\n" +
	        "- The move is made in the player's turn.\n" +
	        "- The move is made within the 3 x 3 board.\n"+ 
	        "The Game would continue and switch among the opposite player until it reaches either one of the following conditions:\n" +
	        "- Player 1 wins.\n" +
	        "- Player 2 wins.\n"+
	        "- Draw.\n" +
	        "- One of the players leaves the game.",
	        "Game Information",
	        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, infoIconScaled, options ,options[0]);
	    }
	}
    
	/**
    * Action Listener to submit name of player from the FieldName.
	*/
    class submitPlayerName implements ActionListener{
        public void actionPerformed (ActionEvent event){
        	PlayerName = fieldplayerNameLower.getText().trim();
            if (PlayerName.isEmpty()){
                return;
            }
            try {
            	out.write("Name: " + PlayerName); out.newLine(); out.flush();
            }catch(IOException e) {
            	e.printStackTrace();
            }
            labelplayerNameUpper.setText("WELCOME " + PlayerName.toUpperCase());
            frame.setTitle("Tic Tac Toe - Player: " + PlayerName);
            
            fieldplayerNameLower.setEnabled(false);  
            submitName.setEnabled(false);
            
            //gameBoard.setBoardEnabled(false); // Enable the Board After name is Submit
           // playerMovedText("Waiting for opponent to joins");
        }
    }
  
    /**
     * Helper function to write out the client side actions.
     * 
     * @param msg it message that will be send to server like Name, Move (row and column) ClosePlayerConnection
     */
    public void SendMessagetoServer(String msg) {
    	try {
    		System.out.println(msg);
    		out.write(msg);
    		out.newLine();
    		out.flush();
    	}catch(IOException e) {
        	e.printStackTrace();
        }
    }
    
    /**
     * Helper function to close the client side GUI
     */
    public void closeGUI() {
    	frame.dispose();
    	System.exit(0);
    }
    
    /**
     * Helper function to put the Player's name on the top side of window.
     * 
     * @param text it refers to the name of the player
     */
    public void playerMovedText(String text){
        labelplayerNameUpper.setText(text);
    }
    
}
