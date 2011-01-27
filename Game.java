import java.io.*;
import javax.imageio.ImageIO;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Game extends Applet implements Runnable, KeyListener {
	public static final int MAPWIDTH = 20;
	public static final int MAPHEIGHT = 15;
	public static final int CELLWIDTH = 16;
	public static final int SCALE = 2;
	public static final int BUFFERWIDTH = MAPWIDTH*CELLWIDTH;
	public static final int BUFFERHEIGHT = MAPHEIGHT*CELLWIDTH;
	public static final int SCREENWIDTH = MAPWIDTH*CELLWIDTH*SCALE;
	public static final int SCREENHEIGHT = MAPHEIGHT*CELLWIDTH*SCALE;
	public static final int NUMKEYS = 525; // Size of keystates array

	public static Color SKYCOLOR;

	private Graphics2D g;
	private Graphics appletg;
	private BufferedImage dbImage;
	private BufferedImage imgTiles, imgSkins, imgEntities;
	private Map map;
	private boolean keys[];
	private Player p1,p2;
	private ArrayList<Entity> entities;

	private boolean running;

	public void start(){
		dbImage = new BufferedImage(BUFFERWIDTH,BUFFERHEIGHT,BufferedImage.TYPE_INT_ARGB);
		g = dbImage.createGraphics();
		appletg = this.getGraphics();
		new Thread(this).start();
		keys = new boolean[NUMKEYS];
		addKeyListener(this);


		SKYCOLOR = new Color(124,176,195);
	}

	public void run(){
		running = true;
		loadLevelFromFile("map1.map");
		loadResources();
		entities = new ArrayList<Entity>();
		entities.add(new Jumppad((MAPWIDTH/2)*16,(MAPHEIGHT-2)*16,-15.f));
		p1 = new Player(6*CELLWIDTH,4*CELLWIDTH,1,0);
		p2 = new Player((MAPWIDTH-6)*CELLWIDTH,4*CELLWIDTH,2,1);
		while(running){
			/*
				Game logic
			*/
			// move players
			p1.move(map,keys);
			p2.move(map,keys);

			// Collide players with entities
			for(int i = 0; i < entities.size(); ++i){
				if(Solid.collides(p1,entities.get(i)))
					p1.handleCollision(entities.get(i));
				if(Solid.collides(p2,entities.get(i)))
					p2.handleCollision(entities.get(i));
			}

			/*
				Redraw screen
			*/
			g.setColor(SKYCOLOR);
			g.fillRect(0,0,BUFFERWIDTH,BUFFERWIDTH);
			// Draw tiles
			for(int iy = 0; iy < MAPHEIGHT; ++iy){
				for(int ix = 0; ix < MAPWIDTH; ++ix){
					// Normal block
					if(map.map[ix][iy] == 1){
						g.drawImage(imgTiles, ix*CELLWIDTH, iy*CELLWIDTH, CELLWIDTH, CELLWIDTH, null);
					}
				}
			}
			// Draw entites
			for(int i = 0; i < entities.size(); ++i){
				entities.get(i).draw(g,imgEntities);
			}
			// Draw players
			p1.draw(g,imgSkins);
			p2.draw(g,imgSkins);

			// Draw buffer to screen
			appletg.drawImage(dbImage, 0, 0, SCREENWIDTH, SCREENHEIGHT, this);

			try{
				Thread.sleep(15);
			} catch (Exception e) {}
		}
	}

	public boolean loadResources(){
		try{
			imgTiles = ImageIO.read(getClass().getResource("gfx/tiles.png"));
			imgSkins = ImageIO.read(getClass().getResource("gfx/skins.png"));
			imgEntities = ImageIO.read(getClass().getResource("gfx/entities.png"));
		} catch (IOException ioe){
			return false;
		}
		return true;
	}

	public void loadLevelFromFile(String filename){
		try{
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			map = (Map) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() < NUMKEYS){
			keys[e.getKeyCode()] = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() < NUMKEYS){
			keys[e.getKeyCode()] = false;
		}
	}

	public void keyTyped(KeyEvent e) {}
}
