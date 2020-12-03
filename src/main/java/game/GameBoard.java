package game;


import graphics.Paths;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.io.IOException;

public class GameBoard{
    // Overall launcher for the game
    private JFrame frame;
    // Graph is the background of the game
    private static Graph graph;
    // paintBoard is the JPanel for the edges, score counter and score boxes
    private paintBoard panel;
    public GameBoard() throws IOException, InterruptedException {
        if(!Graph.allWaysReplay) {
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            graph = new Graph(Graph.getHeight(), Graph.getWidth(), frame);
            graph.createGraph();
            panel = new paintBoard(graph);
            // dotDrawer draws the dots over the edges, I used layerUI because it draws over JLabels.
            LayerUI<JComponent> layerUI = new dotDrawer(graph);
            JLayer<JComponent> jlayer = new JLayer<JComponent>(panel, layerUI);
            frame.setSize(Paths.FRAME_WIDTH, Paths.FRAME_HEIGHT);
            frame.setResizable(false);
            frame.add(jlayer);
            frame.setVisible(true);
            // activate randomBot
        }else{
            graph = new Graph(Graph.getHeight(), Graph.getWidth(), frame);
            graph.createGraph();
        }
        gameThread thread = new gameThread();
        thread.start();
    }
    public GameBoard(int h, int w) throws IOException, InterruptedException {
        if(!Graph.allWaysReplay) {
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            graph = new Graph(h, w, frame);
            graph.createGraph();
            panel = new paintBoard(graph);
            // dotDrawer draws the dots over the edges, I used layerUI because it draws over JLabels.
            LayerUI<JComponent> layerUI = new dotDrawer(graph);
            JLayer<JComponent> jlayer = new JLayer<JComponent>(panel, layerUI);
            frame.setSize(Paths.FRAME_WIDTH, Paths.FRAME_HEIGHT);
            frame.setResizable(false);
            frame.add(jlayer);
            frame.setVisible(true);
            // activate randomBot
        }else{
            graph = new Graph(h, w, frame);
            graph.createGraph();
        }
        if(!Graph.simMode) {
            gameThread thread = new gameThread();
            thread.start();
        }
    }

}
