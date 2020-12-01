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
    ObservationSpace<testState> observationSpace = new ArrayObservationSpace(new int[] {Graph.getEdgeList().size()});
    private testState state = new testState(Graph.getMatrix(),0);
    private NeuralNetFetchable<IDQN> fetchable;
    boolean illegal=false;
    public testEnv(){}
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
        return new testState(Graph.getMatrix(),0);
    }
    @Override
    public void close() { }
    @Override
    public StepReply<testState> step(Integer action) {
      //  System.out.println("Action: "+action);
     //   System.out.println(Arrays.deepToString(Graph.getMatrix()));
        int reward=0;
        try {
            placeEdge(action);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // change the getPlayer1 to whichever player the neural is
     //   System.out.println("step: "+state.step);
        if(!illegal) {
            System.out.println("Not Illegal");
            if (isDone()) {
                if (Graph.getPlayer1Score() > Graph.getPlayer2Score()) {
                    reward = 5;
                } else {
                    reward = -5;
                }
            }else {
                if (Graph.numOfMoves < 1) {
                    if (Graph.player1Turn) {
                        Graph.player1Turn = false;
                    } else {
                        Graph.player1Turn = true;
                    }
                    Graph.setNumOfMoves(1);
                    while (Graph.numOfMoves > 0) {
                            //       System.out.println(Arrays.deepToString(Graph.getMatrix()));
                        if (!isDone()) {
                            Graph.getRandomBot().placeRandomEdge();
                        } else {
                            Graph.numOfMoves = 0;
                            if (Graph.getPlayer1Score() > Graph.getPlayer2Score()) {
                                reward = 5;
                            } else {
                                reward = -5;
                            }
                        }
                    }
                    if (!isDone()) {
                        if (Graph.player1Turn) {
                            Graph.player1Turn = false;
                        } else {
                            Graph.player1Turn = true;
                        }
                        Graph.setNumOfMoves(1);
                    }
                }
            }
        }else{
            reward=-10;
            illegal=false;
        }
        testState t = new testState(Graph.getMatrix(), state.step + 1);
        state=t;
        return new StepReply<>(t, reward, isDone(), null);
    }

    @Override
    public boolean isDone() {
        return gameThread.checkFinished();
    }

    @Override
    public MDP<testState, Integer, DiscreteSpace> newInstance() {
        testEnv test = new testEnv();
        test.setFetchable(fetchable);
        return test;
    }
    public void setFetchable(NeuralNetFetchable<IDQN> fetchable) {
        this.fetchable = fetchable;
    }

    public void placeEdge(int index) throws InterruptedException {
        ELine line = Graph.getEdgeList().get(index).getEline();
        if(!line.isActivated()) {
            System.out.println("NChosen: "+line.vertices.get(0).id+"--"+line.vertices.get(1).id);
            line.setActivated(true);
            // make it black
            line.setBackground(Color.BLACK);
            line.repaint();
            // set the adjacency matrix to 2, 2==is a line, 1==is a possible line
            Graph.matrix[line.vertices.get(0).getID()][line.vertices.get(1).getID()] = 2;
            Graph.matrix[line.vertices.get(1).getID()][line.vertices.get(0).getID()] = 2;
            // gets an arrayList of each box the ELine creates. The box is an arrayList of 4 vertices.
            ArrayList<ArrayList<Vertex>> boxes = gameThread.checkBox(line);
            if (boxes != null) {
                for (ArrayList<Vertex> box : boxes) {
                    // looks through the counterBoxes arrayList and sets the matching one visible.
                    gameThread.checkMatching(box);
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
        }else{
          //  System.out.println("ILLEGAL");
            illegal=true;
        }
    }
}
