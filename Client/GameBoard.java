import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 *  This Class represents the 3x3 Grid game board and related work on it.
 *  It handles the player1 and player2 moves and send that moves to the server side
 *  It Update the client side moves of putting X and O for relevant players.
 */
public class GameBoard extends JPanel {
    private JButton[][] buttons;
    private TicTacToeGUI parent;
    //private ScoreBoard score;
    private Object[] options = {"Yes"};
    
    /**
     * Constructor for Game State
     * 
     * @param parent which refers to the Tic tac Toe class Instance
     */
    public GameBoard(TicTacToeGUI parent){
        this.parent = parent;
        //this.score = score;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        initBoard();
    }
    
    /**
     * Function to create a GameBoard on the client side
     */
    public void initBoard(){
        GridBagConstraints c = new GridBagConstraints();
        buttons = new JButton[3][3];
        c.ipadx = 20;
        c.ipady = 40;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        for (int row =0; row < 3; row++){
           for(int col =0; col < 3; col++){
            c.gridx = col;
            c.gridy = row;
            buttons[row][col] = new JButton("");
            buttons[row][col].setPreferredSize(new Dimension(80,80));
            buttons[row][col].setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
            buttons[row][col].addActionListener(new checkMoves(row, col));
            add(buttons[row][col], c);
           }
        }
    }
    
    /**
    * Function Listener Whenever the Button the 3x3 grid is pressed
    */
    class checkMoves implements ActionListener{
        private int row;
        private int col;

        public checkMoves(int row, int col){
            this.row = row;
            this.col = col;
        }
        public void actionPerformed(ActionEvent event){
        	String msg = "Move: " + row + "," + col;
        	parent.SendMessagetoServer(msg);
        }
    }
    
    /**
     * Function to update the game Board (X and O) on the client side
     * 
     * @param row row of of clicked box from 3x3 gameBoard on client side
     * @param col column of of clicked box from 3x3 gameBoard on client side
     * @param player is the client that made that move
     */
    public void updateBoard(int row, int col, String player ) {
    	
    	if (player.equals("Player1")) {
    		ImageIcon img = new ImageIcon(getClass().getResource("/Cross.png"));
    	    Image imgsize = img.getImage().getScaledInstance(50,50,Image.SCALE_SMOOTH);
    	    img = new ImageIcon(imgsize);
    	    buttons[row][col].setIcon(img);
    	}else if(player.equals("Player2")) {
    		ImageIcon img = new ImageIcon(getClass().getResource("/Circle.png"));
    	    Image imgsize = img.getImage().getScaledInstance(50,50,Image.SCALE_SMOOTH);
    	    img = new ImageIcon(imgsize);
    	    buttons[row][col].setIcon(img);
    	}
    	buttons[row][col].setEnabled(false);    	
    }
    
    /**
     * Function to reset the Client Side game board when a new player join or new game starts
     */
    public void resetBoard(){
        for (int row =0; row < 3; row++){
            for(int col =0; col < 3; col++){
                buttons[row][col].setIcon(null);
                buttons[row][col].putClientProperty("mark", null);
                buttons[row][col].setText("");
                buttons[row][col].setEnabled(true);
            }
        }
    }
    
    /**
    * Function to Enable or Disable the Game board
    */
    public void setBoardEnabled(boolean enabled){
        for (int row =0; row < 3; row++){
            for(int col =0; col < 3; col++){
                {
                    buttons[row][col].setEnabled(enabled);
                }
            }
        }
    }
}
