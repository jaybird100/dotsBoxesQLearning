package game.Q;

import game.*;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static game.Graph.*;
import static game.gameThread.checkMatching;

public class QLearning {
    boolean text=false;

    public ArrayList<int[]> saveInd = new ArrayList<>();
    ArrayList<int[]> saveRew = new ArrayList<>();
    public double[][] qTable;
    ArrayList<ArrayList<ELine>> allStates = new ArrayList<>();
    double episilon=0.2;
    public int stateID;
    public int actionID;
    int availIndex;
    boolean illegalMove=false;
    double lr=0.5;
    double gamma=0.8;
    double reward;
    double winReward = 10;
    public QLearning() throws IOException {
        for(int q=1;q<Graph.getAvailableLines().size();q++) {
            SimpleCombinationGenerator<ELine> t = new SimpleCombinationGenerator<>(Graph.getAvailableLines(), q);
            Iterator it = t.iterator();
            while(it.hasNext()){
                allStates.add((ArrayList<ELine>)(it.next()));
            }
        }
        allStates.add(Graph.getAvailableLines());
        init();
        File Q = new File("Q.txt");
        if(Q.createNewFile()){
         //   System.out.println("File created: " + Q.getName());
            writeToFile();
        }else{
           // System.out.println("File already created");
            readFromFile();
        }
       // printQTable();
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        GameBoard e = new GameBoard(3,3);
    }
    public void printQTable(){
        System.out.println();
        for(double[] a:qTable){
            for(double b:a) {
                System.out.print(b+"|");
            }
            System.out.println();
        }
        double max = Double.MAX_VALUE*-1;
        int mA=0;
        int mS=0;
        for(int a=0;a<qTable.length;a++){
            for(int b=0;b<qTable[0].length;b++){
                if(qTable[a][b]>max){
                    max=qTable[a][b];
                    mA=a;
                    mS=b;
                }

            }
        }
        System.out.println("BIGGEST: qTable["+mA+"]["+mS+"] = "+qTable[mA][mS]);
        System.out.println("STATE: "+Arrays.deepToString(allStates.get(mS).toArray())+" ACTION: "+Graph.getEdgeList().get(mA).getEline().toString());
        double min = Double.MAX_VALUE;
        int minA=0;
        int minS=0;
        for(int a=0;a<qTable.length;a++){
            for(int b=0;b<qTable[0].length;b++){
                if(qTable[a][b]<max){
                    max=qTable[a][b];
                    minA=a;
                    minS=b;
                }
            }
        }
        System.out.println("SMALLEST: qTable["+minA+"]["+minS+"] = "+qTable[minA][minS]);
        System.out.println("STATE: "+Arrays.deepToString(allStates.get(minS).toArray())+" ACTION: "+Graph.getEdgeList().get(minA).getEline().toString());

    }
    public void writeToFile() throws IOException {
       // System.out.println("WRITE");
        File Q = new File("Q.txt");
        FileWriter myWriter = new FileWriter("Q.txt");
        for(int a=0;a<qTable.length;a++){
            for(int b=0;b<qTable[0].length;b++) {
                myWriter.write(Double.toString(qTable[a][b])+' ');
            }
            myWriter.write('\n');
        }
        myWriter.close();
    }
    public void readFromFile() throws FileNotFoundException {
      //  System.out.println("READ");
        File Q = new File("Q.txt");
        Scanner myReader = new Scanner(Q);
        for(int a=0;a<qTable.length;a++){
            for(int b=0;b<qTable[0].length;b++){
                qTable[a][b]=myReader.nextDouble();
            }
            myReader.nextLine();
        }
    }
    public void init(){
        int actions = Graph.getEdgeList().size();
       // System.out.println(states);
        qTable = new double[actions][allStates.size()];
      //  System.out.println(qTable.length+" x "+qTable[0].length);

        for (int a = 0; a < qTable.length; a++) {
            for (int b = 0; b < qTable[0].length; b++) {
                qTable[a][b] = Math.random() * 0.05 - 0.025;
                // qTable[a][b] = 0;
            }
        }

    }
    public static boolean equals(ArrayList<ELine> first, ArrayList<ELine> second){
        Collections.sort(first);
        Collections.sort(second);
        return first.equals(second);
    }
    public void turn() {
       // printQTable();
        availableLines=Graph.availCheck(availableLines);
        setStateID();
        selectAction();
      //  System.out.println("selected action: "+actionID+" EDGE: "+getEdgeList().get(actionID).getEline().toString());
        checkAction();
     //   System.out.println("check action: "+illegalMove);
        while(illegalMove){
      //      System.out.println("Illegal");
            punishQ();
            selectAction();
         //   System.out.println("selected action2: "+actionID+" EDGE: "+getEdgeList().get(actionID).getEline().toString());
            checkAction();
        //    System.out.println("check action2: "+illegalMove);
        }
        performAction();
        updateQ();
       // System.out.println("turn end");
    }
    public void setStateID(){
        int index=-1;
        boolean stop=false;
        for(int a=0;a<allStates.size();a++){
            if(!stop&&equals(Graph.getAvailableLines(),allStates.get(a))){
                stop=true;
                index=a;
            }
        }
        stateID=index;
    }
    public void selectAction(){
        if(!illegalMove) {
            setStateID();
        }
        if(stateID==-1){
       //     System.out.println("ERROR");
        }
        if(Math.random()<episilon){
        //    System.out.println("Random");
            ELine find = Graph.getAvailableLines().get((int)(Math.random()*Graph.getAvailableLines().size()));
            for(int w=0;w<Graph.getEdgeList().size();w++){
                if(Graph.getEdgeList().get(w).getEline().compareTo(find)==0){
                    actionID=w;
                }
            }
        }else{
            //exploit
          //  System.out.println("Chosen");
            double max = -1*Double.MAX_VALUE;
            for(int k=0;k<qTable.length;k++){
                if(text){
                    System.out.println("sA1: " + Graph.getEdgeList().get(k).getEline().toString() + " = " + qTable[k][stateID]);
                }
                if(qTable[k][stateID]>max){
                    if(text) {
                        System.out.println("MAX");
                    }
                    max = qTable[k][stateID];
                    actionID=k;
                }
            }
        }
    }
    public void checkAction(){
        ELine test = Graph.getEdgeList().get(actionID).getEline();
        illegalMove=true;
        for(int w = 0; w< Graph.availableLines.size(); w++){
          //  System.out.println(test.toString()+" == "+Graph.availableLines.get(w).toString());
            if(!Graph.availableLines.get(w).isActivated()&&(Graph.availableLines.get(w).vertices.get(0).id==test.vertices.get(0).id&&Graph.availableLines.get(w).vertices.get(1).id==test.vertices.get(1).id)||(Graph.availableLines.get(w).vertices.get(1).id==test.vertices.get(0).id&&Graph.availableLines.get(w).vertices.get(0).id==test.vertices.get(1).id)){
           //     System.out.println("ACTIVATED: "+Graph.availableLines.get(w).isActivated());
            //    System.out.println("NOT ILLEGAL");
                illegalMove=false;
                availIndex=w;
            }
        }
    }

    public void performAction() {
        int chosen=availIndex;
        ELine line = availableLines.get(chosen);
        line.setActivated(true);
        // make it black
        line.setBackground(Color.BLUE);
        line.repaint();
        // set the adjacency matrix to 2, 2==is a line, 1==is a possible line
        Graph.matrix[line.vertices.get(0).getID()][line.vertices.get(1).getID()] = 2;
        Graph.matrix[line.vertices.get(1).getID()][line.vertices.get(0).getID()] = 2;
        // gets an arrayList of each box the ELine creates. The box is an arrayList of 4 vertices.
        ArrayList<ArrayList<Vertex>> boxes = gameThread.checkBox(line);
        if (boxes != null) {
            for (ArrayList<Vertex> box : boxes) {
                rewardQForGettingBoxes();
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
    public void punishQ(){
        reward=-100;
       // System.out.println("Punish: qTable["+actionID+"]["+stateID+"] = "+qTable[actionID][stateID]);
        qTable[actionID][stateID] = qTable[actionID][stateID] + (lr*(reward+(0)));
     //   System.out.println("Punished: qTable["+actionID+"]["+stateID+"] = "+qTable[actionID][stateID]);
    }

    public void rewardQForGettingBoxes(){
        reward=5;
        if(text) {
            System.out.println("BoxReward: qTable[" + actionID + "][" + stateID + "] = " + qTable[actionID][stateID]);
        }
        qTable[actionID][stateID] = qTable[actionID][stateID] + (lr*(reward));
        if(text) {
            System.out.println("BoxReward: qTable[" + actionID + "][" + stateID + "] = " + qTable[actionID][stateID]);
        }
        int[] stored= new int[2];
        stored[0]=actionID;
        stored[1]=stateID;
        saveRew.add(stored);
    }
    public void unRewardQ(){
        for(int[] s:saveRew){
            reward=-2;
            if(text) {
                System.out.println("UnReward: qTable[" + s[0] + "][" + s[1] + "] = " + qTable[s[0]][s[1]]);
            }
                qTable[s[0]][s[1]] = qTable[s[0]][s[1]] + (lr*(reward));
            if(text) {
                System.out.println("UnReward: qTable[" + s[0] + "][" + s[1] + "] = " + qTable[s[0]][s[1]]);
            }
        }
    }
    public void resetSaveRew(){
        saveRew= new ArrayList<>();
    }


    public void punishQForLosingBoxes(){
        reward=-5;
        if(text) {
            System.out.println("BoxPunish: qTable[" + actionID + "][" + stateID + "] = " + qTable[actionID][stateID]);
        }
        qTable[actionID][stateID] = qTable[actionID][stateID] + (lr*(reward));
        if(text) {
            System.out.println("BoxPunished: qTable[" + actionID + "][" + stateID + "] = " + qTable[actionID][stateID]);
        }
        int[] stored= new int[2];
        stored[0]=actionID;
        stored[1]=stateID;
        saveInd.add(stored);
    }
    public void unPunishQ(){
        for(int[] s:saveInd){
            reward=5;
            if(text) {
                System.out.println("UnPunish: qTable[" + s[0] + "][" + s[1] + "] = " + qTable[s[0]][s[1]]);
            }
            qTable[s[0]][s[1]] = qTable[s[0]][s[1]] + (lr*(reward));
            if(text) {
                System.out.println("UnPunish: qTable[" + s[0] + "][" + s[1] + "] = " + qTable[s[0]][s[1]]);
            }
        }
    }
    public void resetSaveInd(){
    saveInd= new ArrayList<>();
    }
    public void updateQ(){
        if(text) {
            System.out.println("update: qTable[" + actionID + "][" + stateID + "] = " + qTable[actionID][stateID]);
        }
        if(player1Score+player2Score==Graph.getCounterBoxes().size()){

            /*
            if(qPlayer1) {
                if (player1Score > player2Score) {
                    reward = winReward;
                }
                if (player1Score < player2Score) {
                    reward = -1* winReward;
                }
            }else{
                if (player1Score < player2Score) {
                    reward = winReward;
                }
                if (player1Score > player2Score) {
                    reward = -1* winReward;
                }
            }

            if(player1Score==player2Score){
                reward=0;
            }

             */
            if(player2Score==player1Score){
                reward = winReward;
            }else{
                reward = -1* winReward;
            }
        }else{
            reward =0;
        }
        int[][] matrixClone = new int[matrix.length][matrix[0].length];
        for(int p=0;p<matrixClone.length;p++){
            for(int q=0;q<matrixClone[0].length;q++){
                matrixClone[p][q]=matrix[p][q];
            }
        }
      //  System.out.println("Matrix: "+Arrays.deepToString(matrixClone));
        if(qPlayer1) {
            qTable[actionID][stateID] = qTable[actionID][stateID] + (lr*(reward+(gamma*estimateFuture(actionID,stateID,true, matrixClone,player1Score,player2Score,0))));
        }else {
            qTable[actionID][stateID] = qTable[actionID][stateID] + (lr*(reward+(gamma*estimateFuture(actionID,stateID,true, matrixClone,player2Score,player1Score,0))));
        }
        if(text) {
            System.out.println("update2: qTable[" + actionID + "][" + stateID + "] = " + qTable[actionID][stateID]);
        }
    }
    static ArrayList<ArrayList<ELine>> states;
    static ArrayList<Integer> playerScores;
    static ArrayList<Integer> otherPlayerScores;


    public double estimateFuture(int actionID, int stateID, boolean turn,int[][] matri, int tplayerScore,int totherPlayerScore,int counter){
        counter++;
        ArrayList<ELine> state = allStates.get(stateID);
        ELine action = Graph.getEdgeList().get(actionID).getEline();
        if(text) {
            System.out.println("State: " + Arrays.deepToString(state.toArray()));
            System.out.println("Action: " + action.toString() + " StateID: " + stateID + " turn: " + turn + " playerScore: " + tplayerScore + " otherPlayerScore: " + totherPlayerScore + " counter: " + counter);
            System.out.println("QTABLE: " + qTable[actionID][stateID]);
        }
        for(int t = state.size()-1;t>=0;t--){
            if(action.compareTo(state.get(t))==0){
                state.remove(t);
            }
        }
        stateID=setStateID2(state);
        actionID = selectAction2(stateID);
        if(actionID!=-1) {
            ELine action2 = Graph.getEdgeList().get(actionID).getEline();
            matri[action2.vertices.get(0).id][action2.vertices.get(1).id] = 2;
            matri[action2.vertices.get(1).id][action2.vertices.get(0).id] = 2;
            int tem = checkBox(action2, matri);
            if (tem > 0) {
                for (int l = 0; l < tem; l++) {
                    if (turn&&counter!=0) {
                        tplayerScore++;
                    } else {
                        totherPlayerScore++;
                    }
                }
            } else {
                turn = !turn;
            }
        }
        if(state.size()==0){

    //        System.out.println("playerScore: "+tplayerScore+"   player2Score:"+totherPlayerScore);
         //   System.out.println("Matrix: "+Arrays.deepToString(matri));
            if(tplayerScore>totherPlayerScore) {
            //    System.out.println("10 * "+gamma+"^"+counter+" = "+10*Math.pow(gamma,counter));
                return winReward*Math.pow(gamma,counter);
            }
            if(tplayerScore<totherPlayerScore) {
             //   System.out.println("-10 * "+gamma+"^"+counter+" = "+-10*Math.pow(gamma,counter));
                return -1*winReward * Math.pow(gamma, counter);
            }
            return 0;


            /*
            if(tplayerScore==totherPlayerScore){
                return winReward*Math.pow(gamma,counter);
            }
            return -1*winReward * Math.pow(gamma, counter);

             */
        }
        return estimateFuture(actionID,stateID,turn,matri,tplayerScore,totherPlayerScore,counter);
    }
    public int setStateID2(ArrayList<ELine> fakeAv){
        int index=-1;
        boolean stop=false;
        for(int a=0;a<allStates.size();a++){
            if(!stop&&equals(fakeAv,allStates.get(a))){
                stop=true;
                index=a;
            }
        }
        return index;
    }
    public int selectAction2(int stateID){
        int index=-1;
        double max = -1*Double.MAX_VALUE;
        for(int k=0;k<qTable.length;k++){
            if(checkLegal(stateID,k)){
                if(text) {
                    System.out.println("sA: " + Graph.getEdgeList().get(k).getEline().toString() + " = " + qTable[k][stateID]);
                }
                if(qTable[k][stateID]>=max) {
                    if(text) {
                        System.out.println("MAX");
                    }
                    max=qTable[k][stateID];
                    index = k;
                }
            }
        }
        return index;
    }
    public boolean checkLegal(int stateID, int actionID){
        ArrayList<ELine> state = allStates.get(stateID);
        ELine action = Graph.getEdgeList().get(actionID).getEline();
        for(int g=0;g<state.size();g++){
            if(state.get(g).compareTo(action)==0){
                return true;
            }
        }
        return false;
    }
    public int checkBox(ELine line,int[][] matrix){
            ArrayList<ArrayList<Vertex>> listOfBoxes = new ArrayList<>();
            if(line.getHorizontal()){
                if(line.vertices.get(0).getUpVertex()!=null){
                    if(matrix[line.vertices.get(0).getID()][line.vertices.get(0).getUpVertex().getID()]==2&&matrix[line.vertices.get(1).getID()][line.vertices.get(1).getUpVertex().getID()]==2&&matrix[line.vertices.get(0).getUpVertex().getID()][line.vertices.get(1).getUpVertex().getID()]==2){
                        ArrayList<Vertex> box = new ArrayList<>();
                        box.add(line.vertices.get(0));
                        box.add(line.vertices.get(1));
                        box.add(line.vertices.get(0).getUpVertex());
                        box.add(line.vertices.get(1).getUpVertex());
                        listOfBoxes.add(box);
                    }
                }
                if(line.vertices.get(0).getDownVertex()!=null){
                    if(matrix[line.vertices.get(0).getID()][line.vertices.get(0).getDownVertex().getID()]==2&&matrix[line.vertices.get(1).getID()][line.vertices.get(1).getDownVertex().getID()]==2&&matrix[line.vertices.get(0).getDownVertex().getID()][line.vertices.get(1).getDownVertex().getID()]==2){
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
                    if(matrix[line.vertices.get(0).getID()][line.vertices.get(0).getRightVertex().getID()]==2&&matrix[line.vertices.get(1).getID()][line.vertices.get(1).getRightVertex().getID()]==2&&matrix[line.vertices.get(0).getRightVertex().getID()][line.vertices.get(1).getRightVertex().getID()]==2){
                        ArrayList<Vertex> box3 = new ArrayList<>();
                        box3.add(line.vertices.get(0));
                        box3.add(line.vertices.get(1));
                        box3.add(line.vertices.get(0).getRightVertex());
                        box3.add(line.vertices.get(1).getRightVertex());
                        listOfBoxes.add(box3);
                    }
                }
                if(line.vertices.get(0).getLeftVertex()!=null){
                    if(matrix[line.vertices.get(0).getID()][line.vertices.get(0).getLeftVertex().getID()]==2&& matrix[line.vertices.get(1).getID()][line.vertices.get(1).getLeftVertex().getID()]==2&& matrix[line.vertices.get(0).getLeftVertex().getID()][line.vertices.get(1).getLeftVertex().getID()]==2){
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
                return 0;
            }
            /*
            for(ArrayList<Vertex> box:listOfBoxes){
                System.out.print("BOX: "+box.get(0).id+", "+box.get(1).id+", "+box.get(2).id+", "+box.get(3).id+" ");
            }
            System.out.println();

             */
            return listOfBoxes.size();
    }



    public interface IGenerator<T> extends Iterable<T> {

        Stream<T> stream();
    }
    static class SimpleCombinationGenerator<T> implements IGenerator<List<T>> {

        final List<T> originalVector;
        final int combinationLength;


        SimpleCombinationGenerator(Collection<T> originalVector,
                                   int combinationsLength) {
            this.originalVector = new ArrayList<>(originalVector);
            this.combinationLength = combinationsLength;
        }


        /**
         * Creates an iterator of the simple combinations (without repetitions)
         */
        @Override
        public Iterator<List<T>> iterator() {
            return new SimpleCombinationIterator<>(this);
        }

        @Override
        public Stream<List<T>> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), 0), false);
        }


    }
    static class SimpleCombinationIterator<T> implements Iterator<List<T>> {

        private final SimpleCombinationGenerator<T> generator;
        private final int lengthN;
        private final int lengthK;
        private List<T> currentSimpleCombination = null;
        private long currentIndex = 0;
        // Internal array
        private int[] bitVector = null;

        //Criteria to stop iterating
        private int endIndex = 0;


        SimpleCombinationIterator(SimpleCombinationGenerator<T> generator) {
            this.generator = generator;
            lengthN = generator.originalVector.size();
            lengthK = generator.combinationLength;
            currentSimpleCombination = new ArrayList<T>();
            bitVector = new int[lengthK + 1];
            for (int i = 0; i <= lengthK; i++) {
                bitVector[i] = i;
            }
            if (lengthN > 0) {
                endIndex = 1;
            }
            currentIndex = 0;
        }

        private static <T> void setValue(List<T> list, int index, T value) {
            if (index < list.size()) {
                list.set(index, value);
            } else {
                list.add(index, value);
            }
        }

        /**
         * Returns true if all combinations were iterated, otherwise false
         */
        @Override
        public boolean hasNext() {
            return !((endIndex == 0) || (lengthK > lengthN));
        }

        /**
         * Moves to the next combination
         */
        @Override
        public List<T> next() {
            currentIndex++;

            for (int i = 1; i <= lengthK; i++) {
                int index = bitVector[i] - 1;
                if (generator.originalVector.size() > 0) {
                    setValue(currentSimpleCombination, i - 1,
                            generator.originalVector.get(index));
                }
            }

            endIndex = lengthK;

            while (bitVector[endIndex] == lengthN - lengthK + endIndex) {
                endIndex--;
                if (endIndex == 0) {
                    break;
                }
            }
            bitVector[endIndex]++;
            for (int i = endIndex + 1; i <= lengthK; i++) {
                bitVector[i] = bitVector[i - 1] + 1;
            }

            // return the current combination
            return new ArrayList<>(currentSimpleCombination);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "SimpleCombinationIterator=[#" + currentIndex + ", " + currentSimpleCombination + "]";
        }
    }
}
