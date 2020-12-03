package Neural;

import game.*;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.learning.NeuralNetFetchable;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import oshi.json.util.JsonUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class testEnv implements MDP<testState, Integer, DiscreteSpace> {
    DiscreteSpace actionSpace = new DiscreteSpace(Graph.getEdgeList().size());

    // takes amount of possible edges      ^
    ObservationSpace<testState> observationSpace = new ArrayObservationSpace(new int[] {Graph.matrix.length*Graph.matrix[0].length});
    private testState state = new testState(Graph.getMatrix(),0,0,1,Graph.getAvailableLines(),true,Graph.getCounterBoxes(),Graph.getEdgeList());
    boolean illegal=false;
    public testEnv(){ }
    @Override
    public ObservationSpace<testState> getObservationSpace() {
        return observationSpace;
    }
    @Override
    public DiscreteSpace getActionSpace() {
        return actionSpace;
    }
    @Override
    public testState reset() {
       // System.out.println("RESET");
        try {
            GameBoard r = new GameBoard(3,3);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
     //   System.out.println("RESET");
        state=new testState(Graph.getMatrix(),0,0,1,Graph.getAvailableLines(),true,Graph.getCounterBoxes(),Graph.getEdgeList());
        return state;
    }
    @Override
    public void close() { }
    @Override
    public StepReply<testState> step(Integer action) {
      //  System.out.println(state.toString());
      //  System.out.println("Action: "+action);
     //   System.out.println(Arrays.deepToString(Graph.getMatrix()));
        int reward=0;
        placeEdge(action);
        state.availLines=Graph.availCheck(state.availLines);
        //System.out.println("BOT SCORE: "+Graph.player1Score);
        // change the getPlayer1 to whichever player the neural is
     //   System.out.println("step: "+state.step);
        if(!illegal) {
            if (isDone()&&(state.botScore+state.otherPlayerScore)==4) {
                System.out.println("SCORE: "+state.botScore+":"+state.otherPlayerScore);
              //  System.out.println("score = 4 : "+((state.botScore+state.otherPlayerScore)==4));
                if (state.botScore > state.otherPlayerScore) {
                    reward = 15;
                } else {
                    reward = -5;
                }
            }else {
                if (state.numMoves < 1) {
                    if (state.botTurn) {
                        state.botTurn = false;
                    } else {
                        state.botTurn = true;
                    }
                    state.numMoves=1;
                    while (state.numMoves > 0) {
                            //       System.out.println(Arrays.deepToString(Graph.getMatrix()));
                        if (!isDone()) {
                            placeRandomEdge();
                            state.availLines=Graph.availCheck(state.availLines);
                        } else {
                            state.numMoves = 0;
                            if((state.botScore+state.otherPlayerScore)==4) {
                                System.out.println("score: " + state.botScore + ":" + state.otherPlayerScore);
                             //   System.out.println("score = 4 : "+((state.botScore+state.otherPlayerScore)==4));
                                if (state.botScore > state.otherPlayerScore) {
                                    reward = 15;
                                } else {
                                    if (state.botScore == state.otherPlayerScore) {
                                        reward = -5;
                                    } else {
                                        reward = -10;
                                    }
                                }
                            }
                        }
                    }
                   // System.out.println("RBot score: "+Graph.player2Score);
                    if (!isDone()) {
                        if (state.botTurn) {
                            state.botTurn = false;
                        } else {
                            state.botTurn = true;
                        }
                        state.numMoves=1;
                    }
                }
            }
        }else{
            reward=-30;
            illegal=false;
        }
        testState t = new testState(state.matrix, state.botScore,state.otherPlayerScore,state.numMoves,state.availLines,state.botTurn,state.countBoxes,state.edgeList);
        state=t;
        return new StepReply<>(t, reward, isDone(), null);
    }

    @Override
    public boolean isDone() {
        for(scoreBox box: state.countBoxes){
            if(!box.getActivated()){
                return false;
            }
        }
        return true;
    }

    @Override
    public MDP<testState, Integer, DiscreteSpace> newInstance() {
        testEnv test = new testEnv();
        return test;
    }
    public void placeEdge(int index){
        ELine line = state.edgeList.get(index).getEline();
        if(!line.isActivated()) {
         //   System.out.println("NChosen: "+line.vertices.get(0).id+"--"+line.vertices.get(1).id);
            line.setActivated(true);
            // make it black
            line.setBackground(Color.BLACK);
            line.repaint();
            // set the adjacency matrix to 2, 2==is a line, 1==is a possible line
            state.matrix[line.vertices.get(0).getID()][line.vertices.get(1).getID()] = 2;
            state.matrix[line.vertices.get(1).getID()][line.vertices.get(0).getID()] = 2;
            // gets an arrayList of each box the ELine creates. The box is an arrayList of 4 vertices.
            ArrayList<ArrayList<Vertex>> boxes = checkBox(line);
            if (boxes != null) {
                for (ArrayList<Vertex> box : boxes) {
                    // looks through the counterBoxes arrayList and sets the matching one visible.
                    checkMatching(box);
                    // updates the score board
                    if (state.botTurn) {
                        state.botScore++;
                    } else {
                        state.otherPlayerScore++;
                    }
                }
                // if every counterBox has been activated, the game is over
            } else {
                state.numMoves=0;
                // switches turn. If randomBot is active switches to their turn.
            }
        }else{
          //  System.out.println("ILLEGAL");
            illegal=true;
        }
    }
    public ArrayList<ArrayList<Vertex>> checkBox(ELine line){
        ArrayList<ArrayList<Vertex>> listOfBoxes = new ArrayList<>();
        if(line.getHorizontal()){
            if(line.vertices.get(0).getUpVertex()!=null){
                if(state.matrix[line.vertices.get(0).getID()][line.vertices.get(0).getUpVertex().getID()]==2&&state.matrix[line.vertices.get(1).getID()][line.vertices.get(1).getUpVertex().getID()]==2&&state.matrix[line.vertices.get(0).getUpVertex().getID()][line.vertices.get(1).getUpVertex().getID()]==2){
                    ArrayList<Vertex> box = new ArrayList<>();
                    box.add(line.vertices.get(0));
                    box.add(line.vertices.get(1));
                    box.add(line.vertices.get(0).getUpVertex());
                    box.add(line.vertices.get(1).getUpVertex());
                    listOfBoxes.add(box);
                }
            }
            if(line.vertices.get(0).getDownVertex()!=null){
                if(state.matrix[line.vertices.get(0).getID()][line.vertices.get(0).getDownVertex().getID()]==2&&state.matrix[line.vertices.get(1).getID()][line.vertices.get(1).getDownVertex().getID()]==2&&state.matrix[line.vertices.get(0).getDownVertex().getID()][line.vertices.get(1).getDownVertex().getID()]==2){
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
                if(state.matrix[line.vertices.get(0).getID()][line.vertices.get(0).getRightVertex().getID()]==2&&state.matrix[line.vertices.get(1).getID()][line.vertices.get(1).getRightVertex().getID()]==2&&state.matrix[line.vertices.get(0).getRightVertex().getID()][line.vertices.get(1).getRightVertex().getID()]==2){
                    ArrayList<Vertex> box3 = new ArrayList<>();
                    box3.add(line.vertices.get(0));
                    box3.add(line.vertices.get(1));
                    box3.add(line.vertices.get(0).getRightVertex());
                    box3.add(line.vertices.get(1).getRightVertex());
                    listOfBoxes.add(box3);
                }
            }
            if(line.vertices.get(0).getLeftVertex()!=null){
                if(state.matrix[line.vertices.get(0).getID()][line.vertices.get(0).getLeftVertex().getID()]==2&&state.matrix[line.vertices.get(1).getID()][line.vertices.get(1).getLeftVertex().getID()]==2&&state.matrix[line.vertices.get(0).getLeftVertex().getID()][line.vertices.get(1).getLeftVertex().getID()]==2){
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

    public void checkMatching(ArrayList<Vertex> box){
        int avgX=0;
        int avgY=0;
        for(Vertex v:box){
            avgX+=v.getWidth();
            avgY+=v.getHeight();
        }
        avgX=avgX/4;
        avgY=avgY/4;
        for(scoreBox sc: state.countBoxes){
            if(sc.getAvgX()==avgX&&sc.getAvgY()==avgY){
                sc.setText();
            }
        }
    }

    public void placeRandomEdge() {
        // chosen is the index in availableLines of the edge it will choose to place
        int chosen;
        // checks to see if it can create a box
        int c=checkForBox();
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
        ELine line = state.availLines.get(chosen);
     //   System.out.println("RChosen: "+line.vertices.get(0).id+"--"+line.vertices.get(1).id);
        line.setActivated(true);
        // make it black
        line.setBackground(Color.BLACK);
        line.repaint();
        // set the adjacency matrix to 2, 2==is a line, 1==is a possible line
        state.matrix[line.vertices.get(0).getID()][line.vertices.get(1).getID()] = 2;
        state.matrix[line.vertices.get(1).getID()][line.vertices.get(0).getID()] = 2;
        // gets an arrayList of each box the ELine creates. The box is an arrayList of 4 vertices.
        ArrayList<ArrayList<Vertex>> boxes = checkBox(line);
        if (boxes != null) {
            for (ArrayList<Vertex> box : boxes) {
                // q.punishQForLosingBoxes();
                // looks through the counterBoxes arrayList and sets the matching one visible.
                checkMatching(box);
                // updates the score board
                if (state.botTurn) {
                    state.botScore++;
                } else {
                    state.otherPlayerScore++;
                }
            }
        } else {
            state.numMoves=0;
        }
    }
    // checks to see if it can create a box
    // returns the edge that creates the box's index in availableLines
    public int checkForBox(){
        // for each box in counterBoxes
        for(scoreBox box: state.countBoxes){
            int a = state.matrix[box.getVertices().get(0).getID()][box.getVertices().get(1).getID()];
            int b = state.matrix[box.getVertices().get(0).getID()][box.getVertices().get(2).getID()];
            int c = state.matrix[box.getVertices().get(1).getID()][box.getVertices().get(3).getID()];
            int d = state.matrix[box.getVertices().get(2).getID()][box.getVertices().get(3).getID()];
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
        for(int p=state.availLines.size()-1;p>=0;p--){
            if(state.availLines.get(p).vertices.get(0).getID()==a&&state.availLines.get(p).vertices.get(1).getID()==b){
                return p;
            }
        }
        for(int p=state.availLines.size()-1;p>=0;p--){
            if(state.availLines.get(p).vertices.get(0).getID()==b&&state.availLines.get(p).vertices.get(1).getID()==a){
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
        for(int q=0;q<state.availLines.size();q++){
            ELine edge = state.availLines.get(q);
            boolean noBox=true;
            // if the edge is vertical, it can only have a box to the right and left of it.
            if(!edge.getHorizontal()){
                int leftBox=0;
                int rightBox=0;
                if(edge.vertices.get(0).getRightVertex()!=null){
                    rightBox = state.matrix[edge.vertices.get(0).getID()][edge.vertices.get(0).getID()+1]+state.matrix[edge.vertices.get(0).getID()+1][edge.vertices.get(1).getID()+1]+state.matrix[edge.vertices.get(1).getID()][edge.vertices.get(1).getID()+1];
                }
                if(edge.vertices.get(0).getLeftVertex()!=null){
                    leftBox=state.matrix[edge.vertices.get(0).getID()][edge.vertices.get(0).getID()-1]+state.matrix[edge.vertices.get(0).getID()-1][edge.vertices.get(1).getID()-1]+state.matrix[edge.vertices.get(1).getID()][edge.vertices.get(1).getID()-1];
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
                    downBox=state.matrix[edge.vertices.get(0).getID()][edge.vertices.get(0).getID()+Graph.getWidth()]+state.matrix[edge.vertices.get(0).getID()+Graph.getWidth()][edge.vertices.get(1).getID()+Graph.getWidth()]+state.matrix[edge.vertices.get(1).getID()][edge.vertices.get(1).getID()+Graph.getWidth()];
                }
                if(edge.vertices.get(0).getUpVertex()!=null){
                    upBox=state.matrix[edge.vertices.get(0).getID()][edge.vertices.get(0).getID()-Graph.getWidth()]+state.matrix[edge.vertices.get(0).getID()-Graph.getWidth()][edge.vertices.get(1).getID()-Graph.getWidth()]+state.matrix[edge.vertices.get(1).getID()][edge.vertices.get(1).getID()-Graph.getWidth()];
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
            return (int)(Math.random()*state.availLines.size());
        }
    }
    public boolean checkPick(int c){
        if(state.availLines.get(c).isActivated()){
            state.availLines.remove(c);
            return false;
        }
        return true;
    }
}
