package Neural;

import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;

public class testState implements Encodable {
    int[][] matrix;
    int step;
    public testState(int[][] m,int step){
        matrix=m;
        this.step=step;
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
        return null;
    }

    @Override
    public Encodable dup() {
        return null;
    }
}
