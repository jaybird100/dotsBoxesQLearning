package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static game.Graph.availableLines;
import static game.gameThread.clickEdge;


public class ELine extends JLabel implements Comparable{
    // The graphical display of the edges


    // Whether the line has been clicked or not
	private boolean activated = false;
    // the bottom left x and y coordinates of the line
	private int startX;
	private int startY;
    private int edgeListIndex;
    // the vertices
    public ArrayList<Vertex> vertices;
    // whether it's horizontal
    private boolean horizontal;
    public ELine(int w,int h,int x,int y,ArrayList<Vertex> v){
        vertices=v;
        if(vertices.get(1).getID()-vertices.get(0).getID()==1){
            horizontal=true;
        }else{
            horizontal=false;
        }
        startX=x;
        startY=y;
        // the line starts off invisible, e.g White
        setBackground(Color.WHITE);
        setBounds(x,y,w,h);
        setOpaque(true);
        // the mouseListener
        addMouseListener(new MouseAdapter() {
            // when the player hovers over a line it displays it in their colour
            @Override
            public void mouseEntered(MouseEvent e){
                if(!activated) {
                    if (Graph.getPlayer1Turn()) {
                        setBackground(Color.RED);
                    } else {
                        setBackground(Color.BLUE);
                    }
                }
            }
            @Override
            public void mouseExited(MouseEvent e){
                if(!activated) {
                    setBackground(Color.WHITE);
                }
            }
            // when clicked
            @Override
            public void mousePressed(MouseEvent e) {
                //  if the line has not been activated before
                if(!activated) {
                    int index=-1;
                    System.out.println("Clicked: "+vertices.get(0).getID()+" -- "+vertices.get(1).getID());
                    for(int p = availableLines.size()-1; p>=0; p--){
                        if(availableLines.get(p).vertices.get(0).getID()==vertices.get(0).getID()&&availableLines.get(p).vertices.get(1).getID()==vertices.get(1).getID()){
                            index=p;
                        }
                    }
                    System.out.println("Index: "+index);
                    if(index==-1){
                        for(ELine x:availableLines){
                            System.out.print(x.toString()+" | ");
                        }
                        System.out.println();
                    }
                    try {
                        clickEdge(index);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        });
    }


    public void setActivated(boolean b) {
    	activated=b;
    }
    
    public boolean getHorizontal() {
    	return horizontal;
    }
    public int getEdgeListIndex() {
        return edgeListIndex;
    }

    public void setEdgeListIndex(int edgeListIndex) {
        this.edgeListIndex = edgeListIndex;
    }
    public boolean isActivated() {
        return activated;
    }
    public String toString(){
        return vertices.get(0).getID()+" -- "+vertices.get(1).getID();
    }

    @Override
    public int compareTo(Object o) {
        if(vertices.get(0).id>((ELine) o).vertices.get(0).id){
            return 1;
        }
        if(vertices.get(0).id<((ELine) o).vertices.get(0).id){
            return -1;
        }
        if(vertices.get(0).id==((ELine) o).vertices.get(0).id){
            if(vertices.get(1).id>((ELine) o).vertices.get(1).id){
                return 1;
            }
            if(vertices.get(1).id<((ELine) o).vertices.get(1).id){
                return -1;
            }
        }
        return 0;
    }

}

