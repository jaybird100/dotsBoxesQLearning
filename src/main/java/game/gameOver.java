package game;

import graphics.Paths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class gameOver{
    File winR = new File("winR.txt");
    File drawR = new File("drawR.txt");
    // JFrame for the current game, so it can close it down
    private JFrame frame;
    // game over screen.
    private JFrame next;
    private JPanel panel;
    static int drawCounter=0;
    static int gamesPlayed=0;
    double winRaten;
    double drawRate;

    public gameOver(JFrame frame){
        this.frame=frame;
    }
    // turns it on
    public void toggle() throws IOException, InterruptedException {
        gamesPlayed++;
        if(Graph.qPlay) {
       //     System.out.println(Graph.availableLines.size()+" av size");
           // Graph.q.updateQ();
            Graph.q.writeToFile();
            if (Graph.getPlayer2Score() > Graph.getPlayer1Score()) {
                Graph.q.unPunishQ();
            }
            if (Graph.getPlayer2Score() < Graph.getPlayer1Score()) {
                Graph.q.unRewardQ();
            }
            Graph.q.resetSaveRew();
            Graph.q.resetSaveInd();
           // Graph.q.printQTable();
        }
        if(Graph.qPlayers){
            Graph.q.updateQ();
            Graph.q2.updateQ();
            if (Graph.getPlayer2Score() > Graph.getPlayer1Score()) {
                Graph.q.unPunishQ();
            }
            Graph.q.resetSaveInd();
            Graph.q.writeToFile();
            Graph.q2.writeToFile();
        }
        if(Graph.allWaysReplay){
            if(Graph.gamesToPlay==0) {
                System.out.println("SCORE: " + Graph.getPlayer1Score() + " : " + Graph.getPlayer2Score());
                if (Graph.getPlayer1Score() > Graph.getPlayer2Score()) {
                    Graph.setGamesWon1(Graph.getGamesWon1() + 1);
                } else {
                    if (Graph.getPlayer2Score() > Graph.getPlayer1Score()) {
                        Graph.setGamesWon2(Graph.getGamesWon2() + 1);
                    }
                    if (Graph.getPlayer2Score() == Graph.getPlayer1Score()) {
                        drawCounter++;
                    }
                }
                // System.out.println("1: "+Graph.getGamesWon1()+" 2: "+ Graph.getGamesWon2());
                winRaten = 100.0 * ((double) (Graph.getGamesWon2()) / (double) (gamesPlayed));
                drawRate = 100.0 * ((double) (drawCounter) / (double) (gamesPlayed));
                FileWriter writer = new FileWriter("winR.txt", true);
                writer.write(Double.toString(winRaten) + '\n');
                writer.close();
                FileWriter writer1 = new FileWriter("drawR.txt");
                writer1.write(Double.toString(drawRate) + '\n');
                if (Graph.getGamesWon2() != 0) {
                    //    System.out.println("WIN PERCENTAGE: " +winRaten +" | DRAW PERCENTAGE: "+ drawRate);
                }
                Graph.player1Score = 0;
                Graph.player2Score = 0;
                 new GameBoard();
            }else{
                System.out.println(gamesPlayed);
                if (Graph.getPlayer1Score() > Graph.getPlayer2Score()) {
                    Graph.setGamesWon1(Graph.getGamesWon1() + 1);
                } else {
                    if (Graph.getPlayer2Score() > Graph.getPlayer1Score()) {
                        Graph.setGamesWon2(Graph.getGamesWon2() + 1);
                    }
                    if (Graph.getPlayer2Score() == Graph.getPlayer1Score()) {
                        drawCounter++;
                    }
                }
                if(gamesPlayed<=Graph.gamesToPlay){
                    Graph.player1Score = 0;
                    Graph.player2Score = 0;
                    new GameBoard();
                }else{
                    System.out.println("OVERALL SCORE: "+Graph.getGamesWon1()+":"+Graph.getGamesWon2());
                    System.out.println("DRAWS: "+(Graph.gamesToPlay-(Graph.getGamesWon1()+Graph.getGamesWon2())));
                    System.out.println("Illegal move: percentage: "+((double)(gameThread.illegalMoves)/(double)(gameThread.numOfMoves)));
                }
            }
        }else {
            frame.setVisible(false);
            next = new JFrame();
            panel = new JPanel(new FlowLayout());
            panel.setBounds(0, 0, Paths.FRAME_WIDTH, Paths.FRAME_HEIGHT);
            panel.setBackground(Color.DARK_GRAY);
            panel.setOpaque(true);
            JLabel text = new JLabel();
            text.setText("GAME OVER");
            text.setFont(new Font("TimesRoman", Font.BOLD, (int) (Math.sqrt(Paths.FRAME_HEIGHT * Paths.FRAME_WIDTH) / 8)));
            JLabel otherText = new JLabel();
            otherText.setFont(new Font("TimesRoman", Font.PLAIN, (int) (Math.sqrt(Paths.FRAME_HEIGHT * Paths.FRAME_WIDTH) / 10)));
            JLabel wonCounter = new JLabel();
            wonCounter.setFont(new Font("TimesRoman", Font.PLAIN, (int) (Math.sqrt(Paths.FRAME_HEIGHT * Paths.FRAME_WIDTH) / 20)));
            if (Graph.getPlayer1Score() > Graph.getPlayer2Score()) {
                text.setForeground(Color.RED);
                otherText.setForeground(Color.RED);
                otherText.setText("Player 1 wins");
                Graph.setGamesWon1(Graph.getGamesWon1() + 1);
                wonCounter.setForeground(Color.RED);
                if (Graph.getGamesWon1() > 1) {
                    wonCounter.setText("They have won " + Graph.getGamesWon1() + " times. " + '\n');
                } else {
                    wonCounter.setText("They have won " + Graph.getGamesWon1() + " time. " + '\n');
                }
            } else {
                if (Graph.getPlayer2Score() > Graph.getPlayer1Score()) {
                    text.setForeground(Color.BLUE);
                    otherText.setForeground(Color.BLUE);
                    otherText.setText("Player 2 wins");
                    Graph.setGamesWon2(Graph.getGamesWon2() + 1);
                    wonCounter.setForeground(Color.BLUE);
                    if (Graph.getGamesWon2() > 1) {
                        wonCounter.setText("They have won " + Graph.getGamesWon2() + " times. " + '\n');
                    } else {
                        wonCounter.setText("They have won " + Graph.getGamesWon2() + " time. " + '\n');
                    }
                } else {
                    text.setForeground(Color.WHITE);
                    otherText.setForeground(Color.WHITE);
                    otherText.setText("TIE");
                }
            }
            JButton playAgain = new JButton("Play again?");
            // creates the next game
            playAgain.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    next.setVisible(false);
                    try {
                        new GameBoard(Graph.getHeight(),Graph.getWidth());
                    } catch (IOException | InterruptedException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });
            JLabel score = new JLabel();
            score.setFont(new Font("TimesRoman", Font.PLAIN, (int) (Math.sqrt(Paths.FRAME_HEIGHT * Paths.FRAME_WIDTH) / 20)));
            score.setForeground(Color.WHITE);
            score.setText("The score was " + Graph.getPlayer1Score() + " : " + Graph.getPlayer2Score());
            panel.add(text);
            panel.add(otherText);
            panel.add(wonCounter);
            panel.add(playAgain);
            panel.add(score);
            next.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            next.setSize(Paths.FRAME_WIDTH, Paths.FRAME_HEIGHT);
            next.setResizable(false);
            next.add(panel);
            next.setVisible(true);
        }
    }
}
