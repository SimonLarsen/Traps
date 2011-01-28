import java.awt.*;
import javax.swing.JFrame;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.*;

public class Editor extends JFrame {
	public static final int CELLW = 32;
	public static final int SCREENWIDTH = Game.MAPWIDTH*CELLW;
	public static final int SCREENHEIGHT = Game.MAPHEIGHT*CELLW;
	public static final String filename = "map1.map";

	private int[][] map;
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
			map = (int[][]) in.readObject();
			in.close();
			fileIn.close();
			System.out.println("Map read from " + filename);
		} catch (IOException ioe) {
			map = new int[Game.MAPWIDTH][Game.MAPHEIGHT];
			System.out.println("Couldn't read map " + filename + "\nCreating new map.");
		} catch (ClassNotFoundException cnfe) {
			map = new int[Game.MAPWIDTH][Game.MAPHEIGHT];
			System.out.println("Couldn't read map " + filename + "\nCreating new map.");
		}
	}

	private class MyCanvas extends Canvas implements MouseListener, KeyListener {

		private boolean drawGrid;
		private int selection;

		public MyCanvas(){
			setSize(new Dimension(SCREENWIDTH,SCREENHEIGHT));
			addMouseListener(this);
			addKeyListener(this);
			drawGrid = true;
			selection = Map.TYPE_SOLID;
		}

		public void paint(Graphics g){
			g.setColor(Color.white);
			g.fillRect(0,0,SCREENWIDTH,SCREENHEIGHT);
			g.setColor(Color.black);
			for(int iy = 0; iy < Game.MAPHEIGHT; ++iy){
				for(int ix = 0; ix < Game.MAPWIDTH; ++ix){
					switch(map[ix][iy]){
						case Map.TYPE_BLANK:   g.setColor(Color.white); break;
						case Map.TYPE_SOLID:   g.setColor(Color.black); break;
						case Map.TYPE_JUMPPAD: g.setColor(Color.red); break;
						default: g.setColor(Color.white); break;
					}
					g.fillRect(ix*CELLW,iy*CELLW,CELLW,CELLW);
					g.setColor(Color.black);
					if(map[ix][iy] == Map.TYPE_P1START)
						g.drawString("P1",ix*CELLW+10,iy*CELLW+20);
					if(map[ix][iy] == Map.TYPE_P2START)
						g.drawString("P2",ix*CELLW+10,iy*CELLW+20);
				}
			}
			if(drawGrid){
				g.setColor(Color.gray);
				for(int ix = 0; ix < Game.MAPWIDTH; ++ix){
					g.drawLine(ix*CELLW,0,ix*CELLW,SCREENHEIGHT);
				}
				for(int iy = 0; iy < Game.MAPHEIGHT; ++iy){
					g.drawLine(0,iy*CELLW,SCREENWIDTH,iy*CELLW);
				}
			}
			g.drawString(""+selection,10,20);
		}

		public void mouseReleased(MouseEvent e){
			int mx = e.getX()/CELLW;
			int my = e.getY()/CELLW;
			switch(e.getButton()){
				case MouseEvent.BUTTON1: map[mx][my] = selection; break;
				case MouseEvent.BUTTON3: map[mx][my] = 0; break;
			}
			repaint();
		}

		public void keyReleased(KeyEvent e){
			switch(e.getKeyCode()){
				case KeyEvent.VK_S: saveMapToFile(filename); break;
				case KeyEvent.VK_G: drawGrid = !drawGrid; repaint(); break;
			}
			if(e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_0 + Map.BLOCK_TYPES-1){
				selection = e.getKeyCode() - KeyEvent.VK_0;
				repaint();
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
