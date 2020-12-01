package game;

import graphics.Paths;

import javax.swing.*;
import java.awt.*;

public class scoreLabel extends JLabel {
    // the score counters

    // if num==1, it's for player 1, if num==2 it's for player 2
    private int num;
    public scoreLabel(int num){
        setBorder(BorderFactory.createStrokeBorder(new BasicStroke(5.0f)));
        setBackground(Color.LIGHT_GRAY);
        setOpaque(true);
        this.num=num;
        this.setFont(new Font("TimesRoman",Font.PLAIN,30));
        setHorizontalAlignment(SwingConstants.CENTER);
        if(num==1) {
            this.setText(Graph.getPlayer1Score()+" : "+Graph.getGamesWon1());
            this.setForeground(Color.RED);
            this.setBounds(Paths.FRAME_WIDTH / 4, 20, 80, 70);

        }else{
            this.setText(Graph.getPlayer2Score()+" : "+Graph.getGamesWon2());
            this.setForeground(Color.BLUE);
            this.setBounds(3*Paths.FRAME_WIDTH/4, 20,80,70);
        }
    }
    // updates the score
    public void setScore(){
        if(num==1) {
            this.setText(Graph.getPlayer1Score()+" : "+Graph.getGamesWon1());
        }else{
            this.setText(Graph.getPlayer2Score()+" : "+Graph.getGamesWon2());
        }
    }
}
