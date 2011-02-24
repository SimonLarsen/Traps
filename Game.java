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
import java.util.Scanner;

/*
	TODO: Fix PowerBox ownage problem :(
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
	public static int[][] map;
	private boolean keys[];
	public static Player p1,p2;
	private ArrayList<Entity> entities;
	private ArrayList<Particle> particles;
	private ArrayList<Spawn> spawns;
	public static Random rand;
	private int p1skin, p2skin, tileset;
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
		p2skin = 2;
		tileset = 1;
		loadLevelFromASCII("map1.txt");
		RM.getInstance().loadSFX();
		RM.getInstance().loadGFX();
		//RM.getInstance().loadBGM();
		//RM.getInstance().bgm1.loop();
		SKYCOLOR = new Color(124,176,195);
		p1 = new Player(spawns.get(rand.nextInt(spawns.size())),1,p1skin);
		p2 = new Player(spawns.get(rand.nextInt(spawns.size())),2,p2skin);
		running = true;
		while(running){
			time = System.currentTimeMillis();
			/*
				Game logic
			*/
			// move players
			int p1Status = p1.move(map,keys);
			int p2Status = p2.move(map,keys);
			// handle player statuses
			if(p1Status >= 1 && p1Status <= PowerBox.TYPES)
				p2.punish(p1Status);
			else if(p1Status == Player.RETURN_DIED){
				p1.deaths++;
				p1.respawn(spawns.get(rand.nextInt(spawns.size())));
			}
			else if(p1Status == Player.RETURN_BOMBED){
				RM.getInstance().auExplosion.play();
				p1.deaths++;
				particles.add(new BloodExplosion((int)p1.x+6,(int)p1.y+4));
				p1.respawn(spawns.get(rand.nextInt(spawns.size())));
			}

			if(p2Status >= 1 && p2Status <= PowerBox.TYPES)
				p1.punish(p2Status);
			else if(p2Status == Player.RETURN_DIED){
				p2.deaths++;
				p2.respawn(spawns.get(rand.nextInt(spawns.size())));
			}
			else if(p2Status == Player.RETURN_BOMBED){
				RM.getInstance().auExplosion.play();
				p2.deaths++;
				particles.add(new BloodExplosion((int)p2.x+6,(int)p2.y+4));
				p2.respawn(spawns.get(rand.nextInt(spawns.size())));
			}

			// Collide players with entities
			for(int i = 0; i < entities.size(); ++i){
				Entity e = entities.get(i);
				if(Solid.collides(p1,e)){
					if(e instanceof Lava){
						RM.getInstance().auBurn.play();
						particles.add(new BurntCorpse((int)p1.x-3,(int)p1.y,p1.dir));
						p1.deaths++;
						p1.respawn(spawns.get(rand.nextInt(spawns.size())));
						particles.add(new SpawnEffect(p1));
					}
					else if(e instanceof Saw){
						RM.getInstance().auSaw.play();
						p1.deaths++;
						particles.add(new Blood((int)p1.x+8,(int)p1.y));
						p1.respawn(spawns.get(rand.nextInt(spawns.size())));
						particles.add(new SpawnEffect(p1));
					}
					e.handleCollision(p1);
					p1.handleCollision(e);
				}
				if(Solid.collides(p2,e)){
					if(e instanceof Lava){
						RM.getInstance().auBurn.play();
						particles.add(new BurntCorpse((int)p2.x-3,(int)p2.y,p2.dir));
						p2.deaths++;
						p2.respawn(spawns.get(rand.nextInt(spawns.size())));
						particles.add(new SpawnEffect(p2));
					}
					else if(e instanceof Saw){
						RM.getInstance().auSaw.play();
						p2.deaths++;
						particles.add(new Blood((int)p2.x+8,(int)p2.y));
						p2.respawn(spawns.get(rand.nextInt(spawns.size())));
						particles.add(new SpawnEffect(p2));
					}
					e.handleCollision(p2);
					p2.handleCollision(e);
				}
				e.update();
			}
			// Collide players with eachother
			if(Solid.collides(p1,p2)){
				p1.handleCollision(p2);
				p2.handleCollision(p1);
			}

			/*
				Redraw screen
			*/
			g.setColor(SKYCOLOR);
			g.fillRect(0,0,BUFFERWIDTH,BUFFERWIDTH);
			// Draw tiles
			for(int iy = 0; iy < MAPHEIGHT; ++iy){
				for(int ix = 0; ix < MAPWIDTH; ++ix){
					// Platform
					if(map[ix][iy] == Map.TYPE_PLATFORM){
						g.drawImage(RM.getInstance().imgTiles, ix*CELLWIDTH, iy*CELLWIDTH, (ix+1)*CELLWIDTH, (iy+1)*CELLWIDTH,
									0,tileset*CELLWIDTH,16,(tileset+1)*CELLWIDTH, null);
					}
					// Block
					else if(map[ix][iy] == Map.TYPE_BLOCK){
						g.drawImage(RM.getInstance().imgTiles, ix*CELLWIDTH, iy*CELLWIDTH, (ix+1)*CELLWIDTH, (iy+1)*CELLWIDTH,
									16,tileset*CELLWIDTH,32,(tileset+1)*CELLWIDTH, null);
					}
					// Wall
					else if(map[ix][iy] == Map.TYPE_WALL){
						g.drawImage(RM.getInstance().imgTiles, ix*CELLWIDTH, iy*CELLWIDTH, (ix+1)*CELLWIDTH, (iy+1)*CELLWIDTH,
									32,tileset*CELLWIDTH,48,(tileset+1)*CELLWIDTH, null);
					}
				}
			}
			// Draw entites
			for(int i = 0; i < entities.size(); ++i){
				entities.get(i).draw(g);
			}
			// Draw players
			p1.draw(g);
			p2.draw(g);
			// Update and draw particles
			Iterator<Particle> iter = particles.iterator();
			while(iter.hasNext()){
				Particle p = iter.next();
				if(p.alive == false)
					iter.remove();
				else{
					p.update();
					p.draw(g);
				}
			}
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

	public void loadLevelFromASCII(String filename){
		map = new int[MAPWIDTH][MAPHEIGHT];
		try{
			File file = new File(filename);
			Scanner scan = new Scanner(file);
			int iy = 0;
			while(iy < MAPHEIGHT && scan.hasNextLine()){
				String line = scan.nextLine();
				int ix = 0;
				while(ix < MAPWIDTH && ix < line.length()){
					char c = line.charAt(ix);
					switch(c){
						case '|': map[ix][iy] = Map.TYPE_WALL; break;
						case '#': map[ix][iy] = Map.TYPE_BLOCK; break;
						case '=': map[ix][iy] = Map.TYPE_PLATFORM; break;
						case 'x':
						case 'X': entities.add(new Jumppad(ix*CELLWIDTH,iy*CELLWIDTH,Jumppad.POWER));
							 map[ix][iy] = Map.TYPE_BLANK; break;
						case 's':
						case 'S': spawns.add(new Spawn(ix*CELLWIDTH,iy*CELLWIDTH));
							 map[ix][iy] = Map.TYPE_BLANK; break;
						case '~': int cx = 0;
							while(line.charAt(ix+cx) == '~' && ix+cx < MAPWIDTH && ix+cx < line.length()){
								map[ix+cx][iy] = Map.TYPE_BLANK; cx++;
							}
							entities.add(new Lava(ix*CELLWIDTH, iy*CELLWIDTH, cx));
							ix = ix+cx-1; break;
						case '*': int cx2 = 0;
							while(line.charAt(ix+cx2) == '*' && ix+cx2 < MAPWIDTH && ix-cx2 < line.length()){
								map[ix+cx2][iy] = Map.TYPE_BLANK; cx2++;
							}
							entities.add(new Saw(ix*CELLWIDTH, iy*CELLWIDTH, cx2));
							ix = ix+cx2-1; break;
						case '?': entities.add(new PowerBox(ix*CELLWIDTH,iy*CELLWIDTH));
							 map[ix][iy] = Map.TYPE_BLANK; break;
						default: map[ix][iy] = Map.TYPE_BLANK; break;
					}
					++ix;
				}
				++iy;
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("File not found");
		}
	}

	/*
	    REMOVED FOR NOW
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
						case Map.TYPE_SAW: int cx2 = 0;
								while(map[ix+cx2][iy] == Map.TYPE_SAW && ix+cx2 < MAPWIDTH){
									map[ix+cx2][iy] = Map.TYPE_BLANK; cx2++;
								}
								entities.add(new Saw(ix*CELLWIDTH, iy*CELLWIDTH, cx2));
								ix = ix+cx2-1; break;
						case Map.TYPE_POWERBOX: entities.add(new PowerBox(ix*CELLWIDTH,iy*CELLWIDTH));
							 map[ix][iy] = Map.TYPE_BLANK; break;
					}
				}
			}
		}
	}
	*/

	public void drawDebugInfo(Graphics g){
		g.setColor(Color.black);
		g.drawString("Entities: " + entities.size(),8,16);
		g.drawString("Particles: " + particles.size(),8,32);
		g.drawString("P1 X: "+(int)p1.x+" Y: "+(int)p1.y,8,48);
		g.drawString("P2 X: "+(int)p2.x+" Y: "+(int)p2.y,8,64);
		g.drawString("P1 deaths: "+p1.deaths,8,80);
		g.drawString("P2 deaths: "+p2.deaths,8,96);
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
