package game;

import game.Q.QLearning;
import game.Q.QLearning2;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    public static int getNumOfMoves() {
        return numOfMoves;
    }

    public static void setNumOfMoves(int numOfMoves) {
        Graph.numOfMoves = numOfMoves;
    }

    public static int numOfMoves;
    static boolean neural = true;
    public static QLearning q;
    public static int sleep = 0;
    static boolean allWaysReplay=true;
    static boolean qPlay=false;
    public static boolean qPlayer1=false;
    static boolean qPlayers=false;
    public static QLearning2 q2;
    // Overarching game class
	private static randomBot randBot = new randomBot();
	public  static randomBot getRandomBot() {return randBot;}
    // chooses whether randBot will be player 1 or 2
	private static boolean randBotPlayer1 = true;
	public  static boolean getRandBotPlayer1() {return randBotPlayer1;}
	// chooses whether randBot is active
	private static boolean activateRandom=false;
    public static void setActivateRandom(boolean activateRandom) { Graph.activateRandom = activateRandom; }
    public  static boolean getActivateRandom() {return activateRandom;}
	// Adjacency matrix
    public static int[][] matrix;
	public  static int[][] getMatrix(){return matrix;}
    // List of dots
    private static List<Vertex> vertexList;
	public  static List<Vertex> getVertexList(){return vertexList;}
    // List of edges
    private static List<Edge> edgeList;
	public  static List<Edge> getEdgeList(){return edgeList;}
    // List of lines (edges) that haven't been activated yet
    public static ArrayList<ELine> availableLines;
	public  static ArrayList<ELine> getAvailableLines(){return availableLines;}
    // Height and width of the dots
    private static int height;
    private static int width;
    public static int getHeight(){return height;}
    public static int getWidth(){return width;}
    public static void setHeight(int height) { Graph.height = height; }
    public static void setWidth(int width) { Graph.width = width; }
    // tracking how many games each player has won
    private static int gamesWon1=0;
    private static int gamesWon2=0;
    public  static int getGamesWon1(){return gamesWon1;}
    public  static int getGamesWon2(){return gamesWon2;}
    
    public  static void setGamesWon1(int x){gamesWon1=x;}
    public  static void setGamesWon2(int x){gamesWon2=x;}
    // The JLabels for displaying the score
    private  static scoreLabel score1;
    private static scoreLabel score2;
    public  static scoreLabel getScore1(){return score1;}
    public  static scoreLabel getScore2(){return score2;}
    // tracking whether it's player 1's turn or not
    public static boolean player1Turn;
    public  static boolean getPlayer1Turn() {
    	return player1Turn;
    }
    
    public static void setPlayer1Turn(boolean b) {player1Turn =b;}
    // tracking the score in a game
    public static int player1Score = 0;
    public static int player2Score = 0;
    public  static int getPlayer1Score(){return player1Score;}
    public  static int getPlayer2Score(){return player2Score;}
    
    public  static void setPlayer1Score(int s){player1Score=s;}
    public  static void setPlayer2Score(int s){player2Score=s;}
    // All of the boxes, so if a box is completed this displays, can be either initials or the score counter
    private static ArrayList<scoreBox> counterBoxes;
    public  static ArrayList<scoreBox> getCounterBoxes(){return counterBoxes;}
    
    // Game over screen
    private static gameOver screen;
    public  static gameOver getScreen(){return screen;}
    
    // initials or score counter in ScoreBox
    private static String player1Name = "Gerald";
    public static void setPlayer1Name(String s){
        player1Name=s;
    }
    private static String player2Name = "Alex";
    public static void setPlayer2Name(String s){
        player2Name=s;
    }
    private static boolean initials;
    public static void setInitials(boolean s){
        initials=s;
    }
    
    public  static String getPlayer1Name(){return player1Name;}
    public  static String getPlayer2Name(){return player2Name;}
    public  static boolean getInitials(){return initials;}
    
    private JFrame frame;
    
    public Graph(int h, int w, JFrame screen){
        height=h;
        width=w;
        frame=screen;
    }
    // Sets up the game
    public void createGraph() throws IOException {
        numOfMoves=1;
        player1Turn=true;
        player1Score=0;
        player2Score=0;
        screen = new gameOver(frame);
        counterBoxes= new ArrayList<>();
        score1=new scoreLabel(1);
        score2=new scoreLabel(2);
        vertexList= new ArrayList<>();
        // Creates every vertex and sets it's ID and position
        for(int w=0;w<height*width;w++){
            vertexList.add(new Vertex(w));
            vertexList.get(w).setPosition(width, height);
        }
        int counter =0;
        // Creates adjacency matrix
        matrix= new int[vertexList.size()][vertexList.size()];
        int id=0;
        // sets the adjacent vertices for each vertex
        for(int l=0;l<height;l++){
            for(int e=0;e<width;e++){
                Vertex temp = vertexList.get(id);
                if(e!=0){
                    temp.setLeftVertex(vertexList.get(id-1));
                }
                if(e!=width-1){
                    temp.setRightVertex(vertexList.get(id+1));
                }
                if(l!=0){
                    temp.setUpVertex(vertexList.get(id-width));
                }
                if(l!=height-1){
                    temp.setDownVertex(vertexList.get(id+width));
                }
                vertexList.set(id,temp);
                id++;
            }
        }
        // sets all of the available edges in the adjacency matrix to 1
        for (Vertex a:vertexList) {
            if(a.getLeftVertex()!=null){
                matrix[a.getID()][a.getLeftVertex().getID()]=1;
            }
            if(a.getRightVertex()!=null){
                matrix[a.getID()][a.getRightVertex().getID()]=1;
            }
            if(a.getUpVertex()!=null){
                matrix[a.getID()][a.getUpVertex().getID()]=1;
            }
            if(a.getDownVertex()!=null){
                matrix[a.getID()][a.getDownVertex().getID()]=1;
            }
        }
        edgeList= new ArrayList<>();
        availableLines = new ArrayList<>();
        // creates a copy of the matrix to create the list of edges
        int[][] matrixCopy = new int[matrix.length][matrix[0].length];
        for(int r=0;r<matrix.length;r++){
            for(int q=0;q<matrix[0].length;q++){
                // If a space in the matrix == 1, then it creates and edge and adds it to the edge list, it then sets it so the inverse isn't added
                // e.g it adds the edge 0--1 but not 1--0
                if(matrixCopy[r][q]!=3) {
                    matrixCopy[r][q] = matrix[r][q];
                }
                if(matrixCopy[r][q]==1){
                    Edge ne = new Edge(vertexList.get(r), vertexList.get(q));
                    ne.createLine();
                    ne.line.setEdgeListIndex(edgeList.size());
                    availableLines.add(ne.line);
                    edgeList.add(ne);
                    matrixCopy[q][r]=3;
                }
            }
        }
    //    System.out.println(edgeList.size());
        // creates each available box and adds it to the counterBoxes list. This is for displaying what pops up when you complete a box
        for(int r=0;r<height;r++){
            for(int c=0;c<width;c++){
                if(r<height-1&&c<width-1) {
                    ArrayList<Vertex> box = new ArrayList<>();
                    box.add(vertexList.get(counter));
                    box.add(vertexList.get(counter + 1));
                    box.add(vertexList.get(counter + width));
                    box.add(vertexList.get(counter + width + 1));
                    counterBoxes.add(new scoreBox(box));
                }
                counter++;
            }
        }
       // neural.init();
        if(qPlay) {
            q = new QLearning();
        }
        if(qPlayers){
            q = new QLearning();
            q2 = new QLearning2();
        }
    }
    public static ArrayList<ELine> availCheck(ArrayList<ELine> av){
      //  System.out.println("AV CHECK:");
        boolean stop=false;
        for(int q=av.size()-1;q>=0;q--){
            if(av.get(q).isActivated()&&!stop){
                stop=true;
              //  System.out.println("REMOVE: "+av.get(q).vertices.get(0).id+" -- "+av.get(q).vertices.get(1).id);
                av.remove(q);
            }
        }
        return av;
    }
    public class Edge {
        // overall edge class

        // the vertices in the edge
        private ArrayList<Vertex> vertices;
        // The graphical display of the edge and the actionListener is stored in ELine
        private ELine line;
        // Whether the edge is horizontal or vertical
        boolean horizontal;
        public Edge(Vertex one, Vertex two){
            vertices=new ArrayList<>();
            vertices.add(one);
            vertices.add(two);
            // if the second vertex id - the first vertex id == 1 then it's horizontal. E.g 3-2=1 but 6-3!=1
            if(two.getID()-one.getID()==1){
                horizontal=true;
            }
            else{
                horizontal=false;
            }
        }
        
        public ArrayList<Vertex> getVertices(){
        	return this.vertices;
        }
        
        public ELine getEline() {
        	return this.line;
        }
        
        public void setVertices(ArrayList<Vertex> v){
        	this.vertices=v;
        }
        
        public void setEline(ELine l) {
        	this.line=l;
        }
        
        // Creates the ELine
        public void createLine(){
            if(horizontal){
                int wid=vertices.get(1).getWidth()-vertices.get(0).getWidth();
                int hei=8;
                line = new ELine(wid,hei,vertices.get(0).getWidth(),vertices.get(0).getHeight()-8,vertices);
            }
            else{
                int hei=vertices.get(1).getHeight()-vertices.get(0).getHeight();
                int wid=8;
                line = new ELine(wid,hei,vertices.get(0).getWidth()-8,vertices.get(0).getHeight(),vertices);
            }
        }
        public String toString(){
            return vertices.get(0).getID() + " -- "+ vertices.get(1).getID()+". ";
        }
    }
}