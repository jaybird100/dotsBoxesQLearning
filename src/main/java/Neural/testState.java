package Neural;

import game.ELine;
import game.Graph;
import game.scoreBox;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.List;

public class testState implements Encodable{
    int[][] matrix;
    int botScore;
    int otherPlayerScore;
    int numMoves;
    ArrayList<ELine> availLines;
    boolean botTurn;
    ArrayList<scoreBox> countBoxes;
    List<Graph.Edge> edgeList;
    public testState(int[][] m,int b,int op,int nm,ArrayList<ELine> av,boolean bt,ArrayList<scoreBox> cb,List<Graph.Edge> eL){
        matrix=m;
        botScore=b;
        otherPlayerScore=op;
        numMoves=nm;
        availLines=av;
        botTurn=bt;
        countBoxes=cb;
        edgeList=eL;
    }
    @Override
    public double[] toArray() {
        double[] array = new double[matrix.length*matrix[0].length];
        int i=0;
        for(int a=0;a< matrix.length;a++){
            for(int b=0;b<matrix[0].length;b++){
                array[i]= matrix[a][b];
                i++;
            }
        }
        return array;
    }

    @Override
    public boolean isSkipped() {
        return false;
    }

    @Override
    public INDArray getData() {
        return Nd4j.create(toArray());
    }

    @Override
    public Encodable dup() {
        return new testState(matrix.clone(),botScore,otherPlayerScore,numMoves,availLines,botTurn,countBoxes,edgeList);
    }

    public String toString(){
        return "Score: "+botScore+":"+otherPlayerScore+". Num moves: "+numMoves+" availLines size: "+availLines.size()+" botTurn: "+botTurn;
    }

}
