import java.io.*;
import javax.imageio.ImageIO;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/*
	TODO: Walking animation
	TODO: Pretty much everything
*/

public class Game extends Applet implements Runnable, KeyListener {
	public static final int MAPWIDTH = 20;
	public static final int MAPHEIGHT = 15;
	public static final int CELLWIDTH = 16;
	public static final int SCALE = 2;
	public static final int BUFFERWIDTH = MAPWIDTH*CELLWIDTH;
	public static final int BUFFERHEIGHT = MAPHEIGHT*CELLWIDTH;
	public static final int SCREENWIDTH = MAPWIDTH*CELLWIDTH*SCALE;
	public static final int SCREENHEIGHT = MAPHEIGHT*CELLWIDTH*SCALE;
	public static final int NUMKEYS = 256; // Size of keystates array
	public static final int SLEEPTIME = 23;

	public static final boolean DEBUG_INFO = false;

	public static Color SKYCOLOR;

	private Graphics2D g;
	private Graphics appletg;
	private BufferedImage dbImage;
	private BufferedImage imgTiles, imgSkins, imgEntities, imgParticles;
	private int[][] map;
	private boolean keys[];
	private Player p1,p2;
	private ArrayList<Entity> entities;
	private ArrayList<Particle> particles;
	private ArrayList<Spawn> spawns;
	public static Random rand;
	private int p1skin, p2skin;
	private boolean running;
	private long time;

	public void start(){
		dbImage = new BufferedImage(BUFFERWIDTH,BUFFERHEIGHT,BufferedImage.TYPE_INT_RGB);
		g = dbImage.createGraphics();
		appletg = this.getGraphics();
		keys = new boolean[NUMKEYS];
		entities = new ArrayList<Entity>();
		particles = new ArrayList<Particle>();
		spawns = new ArrayList<Spawn>();
		addKeyListener(this);
		rand = new Random();

		new Thread(this).start();
	}

	public void run(){
		p1skin = 0;
		p2skin = 1;
		running = true;
		loadLevelFromFile("map1.map");
		loadResources();
		p1 = new Player(spawns.get(rand.nextInt(spawns.size())),1,0);
		p2 = new Player(spawns.get(rand.nextInt(spawns.size())),2,2);
		while(running){
			time = System.currentTimeMillis();
			/*
				Game logic
			*/
			// move players
			p1.move(map,keys);
			p2.move(map,keys);

			// Collide players with entities
			for(int i = 0; i < entities.size(); ++i){
				Entity e = entities.get(i);
				if(Solid.collides(p1,e)){
					if(e instanceof Lava){
						particles.add(new BurntCorpse((int)p1.x-3,(int)p1.y,p1.dir));
						Spawn sp = spawns.get(rand.nextInt(spawns.size()));
						p1.setPos(sp.x,sp.y);
					}
					e.handleCollision(p1);
					p1.handleCollision(e);
				}
				if(Solid.collides(p2,e)){
					if(e instanceof Lava){
						particles.add(new BurntCorpse((int)p2.x-3,(int)p2.y,p2.dir));
						Spawn sp = spawns.get(rand.nextInt(spawns.size()));
						p2.setPos(sp.x,sp.y);
					}
					e.handleCollision(p2);
					p2.handleCollision(e);
				}
				e.update();
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
					if(map[ix][iy] == 1){
						g.drawImage(imgTiles, ix*CELLWIDTH, iy*CELLWIDTH, CELLWIDTH, CELLWIDTH, null);
					}
				}
			}
			// Draw entites
			for(int i = 0; i < entities.size(); ++i){
				entities.get(i).draw(g,imgEntities);
			}
			// Update and draw particles
			Iterator<Particle> iter = particles.iterator();
			while(iter.hasNext()){
				Particle p = iter.next();
				if(p.alive == false)
					iter.remove();
				else{
					p.update();
					p.draw(g,imgParticles);
				}
			}
			// Draw players
			p1.draw(g,imgSkins);
			p2.draw(g,imgSkins);
			//Debug info
			if(DEBUG_INFO)
				drawDebugInfo(g);
			// Draw buffer to screen
			appletg.drawImage(dbImage, 0, 0, SCREENWIDTH, SCREENHEIGHT, this); 

			long diffTime = System.currentTimeMillis() - time;
			if(diffTime < SLEEPTIME){
				try{
					Thread.sleep(SLEEPTIME - diffTime);
				} catch (Exception e) {}
			}
		}
	}

	public boolean loadResources(){
		SKYCOLOR = new Color(124,176,195);
		try{
			imgTiles = ImageIO.read(getClass().getResource("gfx/tiles.png"));
			imgSkins = ImageIO.read(getClass().getResource("gfx/skins.png"));
			imgEntities = ImageIO.read(getClass().getResource("gfx/entities.png"));
			imgParticles = ImageIO.read(getClass().getResource("gfx/particles.png"));
		} catch (IOException ioe){
			return false;
		}
		return true;
	}

	public void loadLevelFromFile(String filename){
		try{
			//FileInputStream fileIn = new FileInputStream(filename);
			InputStream fileIn = getClass().getResourceAsStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			map = (int[][]) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
		for(int iy = 0; iy < MAPHEIGHT; ++iy){
			for(int ix = 0; ix < MAPWIDTH; ++ix){
				if(map[ix][iy] > 0){
					switch(map[ix][iy]){
						case Map.TYPE_JUMPPAD: entities.add(new Jumppad(ix*CELLWIDTH,iy*CELLWIDTH,Jumppad.POWER));
							 map[ix][iy] = Map.TYPE_BLANK; break;
						case Map.TYPE_SPAWN: spawns.add(new Spawn(ix*CELLWIDTH,iy*CELLWIDTH));
							 map[ix][iy] = Map.TYPE_BLANK; break;
						case Map.TYPE_LAVA: int cx = 0;
								while(map[ix+cx][iy] == Map.TYPE_LAVA && ix+cx < MAPWIDTH){
									map[ix+cx][iy] = Map.TYPE_BLANK; cx++;
								}
								entities.add(new Lava(ix*CELLWIDTH, iy*CELLWIDTH, cx));
								ix = ix+cx-1; break;
						case Map.TYPE_POWERBOX: entities.add(new PowerBox(ix*CELLWIDTH,iy*CELLWIDTH));
							 map[ix][iy] = Map.TYPE_BLANK; break;
					}
				}
			}
		}
	}

	public void drawDebugInfo(Graphics g){
		g.setColor(Color.black);
		g.drawString("Entities: " + entities.size(),8,16);
		g.drawString("Particles: " + particles.size(),8,32);
		g.drawString("P1 X: "+(int)p1.x+" Y: "+(int)p1.y,8,48);
		g.drawString("P2 X: "+(int)p2.x+" Y: "+(int)p2.y,8,64);
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
