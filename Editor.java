import java.awt.*;
import javax.swing.JFrame;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.*;

public class Editor extends JFrame {
	public static final int MAPWIDTH = 20;
	public static final int MAPHEIGHT = 15;
	public static final int CELLW = 16;
	public static final int SCREENWIDTH = MAPWIDTH*CELLW;
	public static final int SCREENHEIGHT = MAPHEIGHT*CELLW;
	public static final String filename = "map1.map";

	private Map map;
	private MyCanvas canvas;

	public Editor(){
		readMapFromFile(filename);
		canvas = new MyCanvas();
		getContentPane().add(canvas);
		pack();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main (String[] args){
		Editor editor = new Editor();
		editor.setVisible(true);
	}

	public void saveMapToFile(String filename){
		try {
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(map);
			out.close();
			fileOut.close();
			System.out.println("Map saved to file " + filename);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("Couldn't save file " + filename);
		}
	}

	public void readMapFromFile(String filename){
		try{
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			map = (Map) in.readObject();
			in.close();
			fileIn.close();
			System.out.println("Map read from " + filename);
		} catch (IOException ioe) {
			map = new Map(MAPWIDTH,MAPHEIGHT);
			System.out.println("Couldn't read map " + filename + "\nCreating new map.");
		} catch (ClassNotFoundException cnfe) {
			map = new Map(MAPWIDTH,MAPHEIGHT);
			System.out.println("Couldn't read map " + filename + "\nCreating new map.");
		}
	}

	private class MyCanvas extends Canvas implements MouseListener, KeyListener {

		private boolean drawGrid;

		public MyCanvas(){
			setSize(new Dimension(SCREENWIDTH,SCREENHEIGHT));
			addMouseListener(this);
			addKeyListener(this);
			drawGrid = true;
		}

		public void paint(Graphics g){
			g.setColor(Color.white);
			g.fillRect(0,0,SCREENWIDTH,SCREENHEIGHT);
			g.setColor(Color.black);
			for(int iy = 0; iy < MAPHEIGHT; ++iy){
				for(int ix = 0; ix < MAPWIDTH; ++ix){
					if(map.map[ix][iy] == 1)
						g.fillRect(ix*CELLW,iy*CELLW,CELLW,CELLW);
				}
			}
			if(drawGrid){
				g.setColor(Color.gray);
				for(int ix = 0; ix < MAPWIDTH; ++ix){
					g.drawLine(ix*CELLW,0,ix*CELLW,SCREENHEIGHT);
				}
				for(int iy = 0; iy < MAPHEIGHT; ++iy){
					g.drawLine(0,iy*CELLW,SCREENWIDTH,iy*CELLW);
				}
			}
		}

		public void mouseReleased(MouseEvent e){
			int mx = e.getX()/CELLW;
			int my = e.getY()/CELLW;
			switch(e.getButton()){
				case MouseEvent.BUTTON1: map.map[mx][my] = 1; break;
				case MouseEvent.BUTTON3: map.map[mx][my] = 0; break;
				case MouseEvent.BUTTON2: drawGrid = !drawGrid; break;
			}
			repaint();
		}

		public void keyReleased(KeyEvent e){
			if(e.getKeyChar() == 's'){
				saveMapToFile(filename);
			}
		}

		public void keyPressed(KeyEvent e){
		}
		public void keyTyped(KeyEvent e){
		}
		public void mousePressed(MouseEvent e){
		}
		public void mouseClicked(MouseEvent e){
		}
		public void mouseEntered(MouseEvent e){
		}
		public void mouseExited(MouseEvent e){
		}
		public void update(Graphics g){
			paint(g);
		}
	}
}
