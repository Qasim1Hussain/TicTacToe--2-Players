import javax.swing.*;
import java.awt.*;

/**
 * This class represents the Score board which list the Player, Computer and Draw scores.
 */
public class ScoreBoard extends JPanel {
    private JLabel player1Label;
    private JLabel player2Label;
    private JLabel drawsLabel;
    private int player1Wins;
    private int player2Wins;
    private int draws;
    
    /**
     * Constructor for ScoreBoard instance
     * This create a panel for the score board and layout for the labels.
     */
    public ScoreBoard (){
    	player1Wins = 0;
    	player2Wins = 0;
        draws = 0;
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(150,200));
        setBorder(BorderFactory.createTitledBorder("Score"));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx= 0;
        c.gridy = 0;
        c.ipady = 80;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10,5,10,5);
        player1Label = new JLabel("Player 1 Wins:     0");
        player1Label.setFont(player1Label.getFont().deriveFont(Font.BOLD));
        add(player1Label,c);

        c.gridx= 0;
        c.gridy = 1;
        player2Label = new JLabel("Player 2 Wins:  0");
        player2Label.setFont(player2Label.getFont().deriveFont(Font.BOLD));
        add(player2Label,c);

        c.gridx= 0;
        c.gridy = 2;
        drawsLabel = new JLabel("Draws:                0");
        drawsLabel.setFont(drawsLabel.getFont().deriveFont(Font.BOLD));
        add(drawsLabel,c);
    }
    
    /**
     * Increments the Player's 1 score
     * Increments the Player's 2 score
     * Increments the Draw's score
     */
    public void updateScore(int player1wins, int player2wins, int draw) {
    	this.player1Wins = player1wins;
    	this.player2Wins = player2wins;
    	this.draws = draw;
    	player1Label.setText("Player 1 Wins:      " + player1Wins);
    	player2Label.setText("Player 2 Wins:      " + player2Wins);
    	drawsLabel.setText("Draws:               " + draws);
    }
    
    /**
     * Resets the score board back to 0.
     */
    public void resetScore(){
        updateScore(0,0,0);
    }
}
