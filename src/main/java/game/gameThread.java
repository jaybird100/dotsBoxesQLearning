package game;

import Neural.testState;
import org.nd4j.linalg.factory.Nd4j;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static game.Graph.*;

public class gameThread extends Thread{
    static int numOfMoves=0;
    static int illegalMoves=0;
    public void run(){
        try{
            while(!checkFinished()) {
                if(allWaysReplay){
                    sleep(30);
                }
                if(sleep>0) {
                    sleep(500*sleep);
                }
                availableLines=availCheck(Graph.getAvailableLines());
                if(neural&&player1Turn){
                    testState state = new testState(matrix,player1Score,player2Score,numOfMoves,availableLines,player1Turn,getCounterBoxes(),getEdgeList());
                    placeEdgeN(QBrain.nextAction(Nd4j.expandDims(Nd4j.create(state.toArray()), 0)));
                }
                if (Graph.getActivateRandom() && Graph.player1Turn == Graph.getRandBotPlayer1()) {
                    if(sleep>0) {
                        sleep(250*sleep);
                    }
                    if(allWaysReplay){
                        sleep(15);
                    }
                    Graph.getRandomBot().placeRandomEdge();
                }
                if(Graph.qPlay&&Graph.qPlayer1==Graph.player1Turn){
                    if(sleep>0) {
                        sleep(250*sleep);
                    }
                    if(allWaysReplay){
                        sleep(15);
                    }
                    Graph.q.turn();
                }
                if(Graph.qPlayers){
                    if(allWaysReplay){
                        sleep(20);
                    }
                    if(sleep>0) {
                        sleep(250*sleep);
                    }
                    if(!player1Turn){
                        q.turn();
                    }else{
                        q2.turn();
                    }
                }

                if (Graph.getNumOfMoves() < 1) {
                    Graph.setNumOfMoves(1);
                    if (Graph.player1Turn) {
                        Graph.player1Turn = false;
                    } else {
                        Graph.player1Turn = true;
                    }
                }
            }
                try {
                    Graph.getScreen().toggle();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static boolean checkFinished(){
        for(scoreBox box: Graph.getCounterBoxes()){
            if(!box.getActivated()){
                return false;
            }
        }
        return true;
    }
    public static void placeEdgeN(int index){
        numOfMoves++;
        ELine line = Graph.getEdgeList().get(index).getEline();
        if(line.isActivated()){
            illegalMoves++;
            line = availableLines.get((int)(Math.random()*availableLines.size()));
        }
        line.setActivated(true);
        // make it black
        line.setBackground(Color.BLACK);
        line.repaint();
        // set the adjacency matrix to 2, 2==is a line, 1==is a possible line
        Graph.matrix[line.vertices.get(0).getID()][line.vertices.get(1).getID()] = 2;
        Graph.matrix[line.vertices.get(1).getID()][line.vertices.get(0).getID()] = 2;
        // gets an arrayList of each box the ELine creates. The box is an arrayList of 4 vertices.
        ArrayList<ArrayList<Vertex>> boxes = checkBox(line);
        if (boxes != null) {
            for (ArrayList<Vertex> box : boxes) {
                // looks through the counterBoxes arrayList and sets the matching one visible.
                checkMatching(box);
                // updates the score board
                if (Graph.getPlayer1Turn()) {
                    Graph.setPlayer1Score(Graph.getPlayer1Score() + 1);
                    Graph.getScore1().setScore();
                } else {
                    Graph.setPlayer2Score(Graph.getPlayer2Score() + 1);
                    Graph.getScore2().setScore();
                }
            }
            // if every counterBox has been activated, the game is over
        } else {
            Graph.setNumOfMoves(0);
            // switches turn. If randomBot is active switches to their turn.
        }
    }
    public static void clickEdge(int index) throws InterruptedException {
        ELine line = Graph.getAvailableLines().get(index);
        line.setActivated(true);
        // make it black
        line.setBackground(Color.BLACK);
        line.repaint();
        // set the adjacency matrix to 2, 2==is a line, 1==is a possible line
        Graph.matrix[line.vertices.get(0).getID()][line.vertices.get(1).getID()] = 2;
        Graph.matrix[line.vertices.get(1).getID()][line.vertices.get(0).getID()] = 2;
        // gets an arrayList of each box the ELine creates. The box is an arrayList of 4 vertices.
        ArrayList<ArrayList<Vertex>> boxes = checkBox(line);
        if (boxes != null) {
            for (ArrayList<Vertex> box : boxes) {
                // looks through the counterBoxes arrayList and sets the matching one visible.
                checkMatching(box);
                // updates the score board
                if (Graph.getPlayer1Turn()) {
                    Graph.setPlayer1Score(Graph.getPlayer1Score()+1);
                    Graph.getScore1().setScore();
                } else {
                    Graph.setPlayer2Score(Graph.getPlayer2Score()+1);
                    Graph.getScore2().setScore();
                }
            }
            // if every counterBox has been activated, the game is over
        } else {
            Graph.setNumOfMoves(0);
            // switches turn. If randomBot is active switches to their turn.
        }
    }
    // checks to find the matching box in counterBoxes through their average x and y coordinates, then displays it.
    public static void checkMatching(ArrayList<Vertex> box){
        int avgX=0;
        int avgY=0;
        for(Vertex v:box){
            avgX+=v.getWidth();
            avgY+=v.getHeight();
        }
        avgX=avgX/4;
        avgY=avgY/4;
        for(scoreBox sc: Graph.getCounterBoxes()){
            if(sc.getAvgX()==avgX&&sc.getAvgY()==avgY){
                sc.setText();
            }
        }
    }
    // checks whether an edge creates a box, through the adjacency matrix
    public static ArrayList<ArrayList<Vertex>> checkBox(ELine line){
        ArrayList<ArrayList<Vertex>> listOfBoxes = new ArrayList<>();
        if(line.getHorizontal()){
            if(line.vertices.get(0).getUpVertex()!=null){
                if(Graph.getMatrix()[line.vertices.get(0).getID()][line.vertices.get(0).getUpVertex().getID()]==2&&Graph.getMatrix()[line.vertices.get(1).getID()][line.vertices.get(1).getUpVertex().getID()]==2&&Graph.getMatrix()[line.vertices.get(0).getUpVertex().getID()][line.vertices.get(1).getUpVertex().getID()]==2){
                    ArrayList<Vertex> box = new ArrayList<>();
                    box.add(line.vertices.get(0));
                    box.add(line.vertices.get(1));
                    box.add(line.vertices.get(0).getUpVertex());
                    box.add(line.vertices.get(1).getUpVertex());
                    listOfBoxes.add(box);
                }
            }
            if(line.vertices.get(0).getDownVertex()!=null){
                if(Graph.getMatrix()[line.vertices.get(0).getID()][line.vertices.get(0).getDownVertex().getID()]==2&&Graph.getMatrix()[line.vertices.get(1).getID()][line.vertices.get(1).getDownVertex().getID()]==2&&Graph.getMatrix()[line.vertices.get(0).getDownVertex().getID()][line.vertices.get(1).getDownVertex().getID()]==2){
                    ArrayList<Vertex> box2 = new ArrayList<>();
                    box2.add(line.vertices.get(0));
                    box2.add(line.vertices.get(1));
                    box2.add(line.vertices.get(0).getDownVertex());
                    box2.add(line.vertices.get(1).getDownVertex());
                    listOfBoxes.add(box2);
                }
            }
        }else{
            if(line.vertices.get(0).getRightVertex()!=null){
                if(Graph.getMatrix()[line.vertices.get(0).getID()][line.vertices.get(0).getRightVertex().getID()]==2&&Graph.getMatrix()[line.vertices.get(1).getID()][line.vertices.get(1).getRightVertex().getID()]==2&&Graph.getMatrix()[line.vertices.get(0).getRightVertex().getID()][line.vertices.get(1).getRightVertex().getID()]==2){
                    ArrayList<Vertex> box3 = new ArrayList<>();
                    box3.add(line.vertices.get(0));
                    box3.add(line.vertices.get(1));
                    box3.add(line.vertices.get(0).getRightVertex());
                    box3.add(line.vertices.get(1).getRightVertex());
                    listOfBoxes.add(box3);
                }
            }
            if(line.vertices.get(0).getLeftVertex()!=null){
                if(Graph.getMatrix()[line.vertices.get(0).getID()][line.vertices.get(0).getLeftVertex().getID()]==2&&Graph.getMatrix()[line.vertices.get(1).getID()][line.vertices.get(1).getLeftVertex().getID()]==2&&Graph.getMatrix()[line.vertices.get(0).getLeftVertex().getID()][line.vertices.get(1).getLeftVertex().getID()]==2){
                    ArrayList<Vertex> box4 = new ArrayList<>();
                    box4.add(line.vertices.get(0));
                    box4.add(line.vertices.get(1));
                    box4.add(line.vertices.get(0).getLeftVertex());
                    box4.add(line.vertices.get(1).getLeftVertex());
                    listOfBoxes.add(box4);
                }
            }
        }
        // if it creates no boxes, return null.
        if(listOfBoxes.isEmpty()){
            return null;
        }
        return listOfBoxes;
    }
}
