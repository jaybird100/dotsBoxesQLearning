package game;

import java.awt.*;
import java.util.ArrayList;

import static game.Graph.*;
import static game.gameThread.checkBox;
import static game.gameThread.checkMatching;

public class randomBot {
    double epsilon = 0.0;
    // places edges randomly except will always complete a box and won't set up boxes for the other player
    public randomBot(){}
    // places the edge
    public void placeRandomEdge() {
        // chosen is the index in availableLines of the edge it will choose to place
        int chosen;
        // checks to see if it can create a box
        int c=checkForBox();
        if(Math.random()>epsilon) {
            if (c != -1) {
                // if it can, it sets that to the index
                chosen = c;
            } else {
                // if not, selects a random edge that doesn't set up a box for the other player.
                // if that's not possible it just selects a random edge
                chosen = checkFor3s();
                if (!checkPick(chosen)) {
                    //  System.out.println(Graph.getAvailableLines().get(chosen).toString());
                    chosen = checkFor3s();
                    //    System.out.println(Graph.getAvailableLines().get(chosen).toString());

                }
            }
        }else{
            chosen=(int)(Math.random()*Graph.getAvailableLines().size());
        }
        ELine line = Graph.getAvailableLines().get(chosen);
     //   System.out.println("RChosen: "+line.vertices.get(0).id+"--"+line.vertices.get(1).id);
        line.setActivated(true);
        // make it black
        line.setBackground(Color.RED);
        line.repaint();
        // set the adjacency matrix to 2, 2==is a line, 1==is a possible line
        Graph.matrix[line.vertices.get(0).getID()][line.vertices.get(1).getID()] = 2;
        Graph.matrix[line.vertices.get(1).getID()][line.vertices.get(0).getID()] = 2;
        // gets an arrayList of each box the ELine creates. The box is an arrayList of 4 vertices.
        ArrayList<ArrayList<Vertex>> boxes = checkBox(line);
        if (boxes != null) {
            for (ArrayList<Vertex> box : boxes) {
               // q.punishQForLosingBoxes();
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
        } else {
            Graph.numOfMoves=0;
        }
    }
    // checks to see if it can create a box
    // returns the edge that creates the box's index in availableLines
    public int checkForBox(){
        // for each box in counterBoxes
        for(scoreBox box: Graph.getCounterBoxes()){
            int a = Graph.getMatrix()[box.getVertices().get(0).getID()][box.getVertices().get(1).getID()];
            int b = Graph.getMatrix()[box.getVertices().get(0).getID()][box.getVertices().get(2).getID()];
            int c = Graph.getMatrix()[box.getVertices().get(1).getID()][box.getVertices().get(3).getID()];
            int d = Graph.getMatrix()[box.getVertices().get(2).getID()][box.getVertices().get(3).getID()];
            // if each int adds up to 7, there must be 3 lines in a box. A line = 1 when available and = 2 when placed.
            // as 3 completed lines is 3*2=6, +1 for the remaining line == 7
            if(a+b+c+d==7){
                // checks to see which line is the available one, e.g == 1
                if(a==1){
                    return findMatch(box.getVertices().get(0).getID(),box.getVertices().get(1).getID());
                }
                if(b==1){
                    return findMatch(box.getVertices().get(0).getID(),box.getVertices().get(2).getID());
                }
                if(c==1){
                    return findMatch(box.getVertices().get(1).getID(),box.getVertices().get(3).getID());
                }
                if(d==1){
                    return findMatch(box.getVertices().get(2).getID(),box.getVertices().get(3).getID());
                }
            }
        }
        return -1;
    }
    // finds the index in available lines which matches the input vertex id's
    // e.g you input 5 and 4, it returns the index of the edge 4--5.
    public int findMatch(int a, int b){
        for(int p=Graph.getAvailableLines().size()-1;p>=0;p--){
            if(Graph.getAvailableLines().get(p).vertices.get(0).getID()==a&&Graph.getAvailableLines().get(p).vertices.get(1).getID()==b){
                return p;
            }
        }
        for(int p=Graph.getAvailableLines().size()-1;p>=0;p--){
            if(Graph.getAvailableLines().get(p).vertices.get(0).getID()==b&&Graph.getAvailableLines().get(p).vertices.get(1).getID()==a){
                return p;
            }
        }
        /*
        for(ELine l: Graph.getAvailableLines()){
            System.out.println(l.vertices.get(0).getID()+" -- "+l.vertices.get(1).getID());
        }

         */
        return -1;
    }
    // removes every edge which sets up a box for the other player
    public int checkFor3s(){
        ArrayList<Integer> av = new ArrayList<>();
        // goes through each availableLine
        for(int q=0;q<getAvailableLines().size();q++){
            ELine edge = getAvailableLines().get(q);
            boolean noBox=true;
            // if the edge is vertical, it can only have a box to the right and left of it.
            if(!edge.getHorizontal()){
                int leftBox=0;
                int rightBox=0;
                if(edge.vertices.get(0).getRightVertex()!=null){
                    rightBox = Graph.getMatrix()[edge.vertices.get(0).getID()][edge.vertices.get(0).getID()+1]+Graph.getMatrix()[edge.vertices.get(0).getID()+1][edge.vertices.get(1).getID()+1]+Graph.getMatrix()[edge.vertices.get(1).getID()][edge.vertices.get(1).getID()+1];
                }
                if(edge.vertices.get(0).getLeftVertex()!=null){
                    leftBox=Graph.getMatrix()[edge.vertices.get(0).getID()][edge.vertices.get(0).getID()-1]+Graph.getMatrix()[edge.vertices.get(0).getID()-1][edge.vertices.get(1).getID()-1]+Graph.getMatrix()[edge.vertices.get(1).getID()][edge.vertices.get(1).getID()-1];
                }
                // it adds up the int value of each edge in each box in the adjacency matrix
                // if it == 5, then placing another edge there will set up a box for the other player
                // it checks the 3 edges around the chosen edge, not the chosen edge itself
                // so if the 3 edge's sum == 5, then they must be 2+2+1 = 5
                // so there's 2 lines in the box, so putting another line there sets up the other player
                if(leftBox==5||rightBox==5){
                    noBox=false;
                }
            }else{
                // does the same but for horizontal edges
                int downBox=0;
                int upBox=0;
                if(edge.vertices.get(0).getDownVertex()!=null){
                    downBox=Graph.getMatrix()[edge.vertices.get(0).getID()][edge.vertices.get(0).getID()+ Graph.getWidth()]+Graph.getMatrix()[edge.vertices.get(0).getID()+Graph.getWidth()][edge.vertices.get(1).getID()+Graph.getWidth()]+Graph.getMatrix()[edge.vertices.get(1).getID()][edge.vertices.get(1).getID()+Graph.getWidth()];
                }
                if(edge.vertices.get(0).getUpVertex()!=null){
                    upBox=Graph.getMatrix()[edge.vertices.get(0).getID()][edge.vertices.get(0).getID()-Graph.getWidth()]+Graph.getMatrix()[edge.vertices.get(0).getID()-Graph.getWidth()][edge.vertices.get(1).getID()-Graph.getWidth()]+Graph.getMatrix()[edge.vertices.get(1).getID()][edge.vertices.get(1).getID()-Graph.getWidth()];
                }
                if(upBox==5||downBox==5){
                    noBox=false;
                }
            }
            if(noBox){
                // if the line doesn't create a box it adds the index from availableLines to a new arrayList, av
                av.add(q);
            }
        }
        if(av.size()!=0){
            // if there are edges in av, it returns a random entry in av
            // all entries in av are indexes from availableLine
           // System.out.println("NO BOX: "+av.size());
            int ret = av.get((int)(Math.random()*av.size()));
            return ret;
        }else{
            // if not it just returns a random index from availableLine
            return (int)(Math.random()*getAvailableLines().size());
        }
    }
    public boolean checkPick(int c){
        if(availableLines.get(c).isActivated()){
            availableLines.remove(c);
            return false;
        }
        return true;
    }
}
