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
	public static boolean keys[];
	public static Player p1,p2;
	private ArrayList<Entity> entities;
	private ArrayList<Particle> particles;
	private ArrayList<Spawn> spawns;
	public static Random rand;
	public static int start_lives;
	private int p1skin, p2skin, tileset, menustate, selectedmap;
	private boolean running;
	private long time;
	private MP3Player player;

	public void start(){
		dbImage = new BufferedImage(BUFFERWIDTH,BUFFERHEIGHT,BufferedImage.TYPE_INT_RGB);
		g = dbImage.createGraphics();
		appletg = this.getGraphics();
		keys = new boolean[NUMKEYS];
		particles = new ArrayList<Particle>();
		addKeyListener(this);
		rand = new Random();
		player = new MP3Player();

		RM.getInstance().loadSFX();
		RM.getInstance().loadGFX();
		player.play();

		SKYCOLOR = new Color(124,176,195);
		p1skin = 1;
		p2skin = 0;
		tileset = 1;
		start_lives = 10;
		selectedmap = 1;
		menustate = MAIN_MENU_STATE;

		new Thread(this).start();
	}

	public void run(){
		while(true){
			switch(menustate){
				case MAIN_MENU_STATE: showMainMenu(); break;
				case GAME_STATE: gameLoop(); break;
				case SELECTION_STATE: showSelectionMenu(); break;
			}
		}
	}

	public void gameLoop(){
		loadLevelFromASCII("maps/map"+selectedmap+".txt");
		p1 = new Player(spawns.get(rand.nextInt(spawns.size())),1,p1skin);
		p2 = new Player(spawns.get(rand.nextInt(spawns.size())),2,p2skin);

		while(p1.lives > 0 && p2.lives > 0){
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
				p1.lives--;
				p1.respawn(spawns.get(rand.nextInt(spawns.size())));
			}
			else if(p1Status == Player.RETURN_BOMBED){
				RM.getInstance().auExplosion.play();
				p1.lives--;
				particles.add(new BloodExplosion((int)p1.x+6,(int)p1.y+4));
				p1.respawn(spawns.get(rand.nextInt(spawns.size())));
			}

			if(p2Status >= 1 && p2Status <= PowerBox.TYPES)
				p1.punish(p2Status);
			else if(p2Status == Player.RETURN_DIED){
				p2.lives--;
				p2.respawn(spawns.get(rand.nextInt(spawns.size())));
			}
			else if(p2Status == Player.RETURN_BOMBED){
				RM.getInstance().auExplosion.play();
				p2.lives--;
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
						p1.lives--;
						p1.respawn(spawns.get(rand.nextInt(spawns.size())));
						particles.add(new SpawnEffect(p1));
					}
					else if(e instanceof Saw){
						RM.getInstance().auSaw.play();
						p1.lives--;
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
						p2.lives--;
						p2.respawn(spawns.get(rand.nextInt(spawns.size())));
						particles.add(new SpawnEffect(p2));
					}
					else if(e instanceof Saw){
						RM.getInstance().auSaw.play();
						p2.lives--;
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
					int cur = map[ix][iy];
					if(cur >= Map.TYPE_PLATFORM && cur <= Map.TYPE_WALL){
						g.drawImage(RM.getInstance().imgTiles, ix*CELLWIDTH, iy*CELLWIDTH, (ix+1)*CELLWIDTH, (iy+1)*CELLWIDTH,
									(cur-1)*16,tileset*CELLWIDTH,cur*16,(tileset+1)*CELLWIDTH, null);
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

		showRetryScreen();
	}

	public void showRetryScreen(){
		// Show retry screen
		g.setColor(new Color(0,0,0,128));
		g.fillRect(0,0,BUFFERWIDTH,BUFFERHEIGHT);
		g.setColor(Color.white);
		g.setFont(RM.getInstance().menuFont);
		if(p1.lives > p2.lives){
			g.drawString("Player 1 wins!",50,64);
			p1.draw(g);
		}
		else if(p2.lives > p1.lives){
			g.drawString("Player 2 wins!",50,64);
			p2.draw(g);
		}
		else
			g.drawString("You are both losers!",0,64);

		g.drawString("Press ENTER",70,123);
		g.drawString("to play again!",50,145);

		g.drawString("Press ESCAPE",64,190);
		g.drawString("to reselect!",64,212);

		keys[KeyEvent.VK_ENTER] = false;
		keys[KeyEvent.VK_ESCAPE] = false;

		boolean retry_selection = false;
		while(retry_selection == false){
			if(keys[KeyEvent.VK_ENTER]){
				menustate = GAME_STATE;
				retry_selection = true;
			}
			else if(keys[KeyEvent.VK_ESCAPE]){
				menustate = SELECTION_STATE;
				retry_selection = true;
				keys[KeyEvent.VK_ESCAPE] = false;
			}

			appletg.drawImage(dbImage, 0, 0, SCREENWIDTH, SCREENHEIGHT, this); 
			try {
				Thread.sleep(20);
			} catch (Exception e) {}
		}
	}

	public void showSelectionMenu(){
		boolean p1done, p2done;
		p1done = p2done = false;
		int mapselection = 1;
		while(menustate == SELECTION_STATE){
			// Handle player 1
			if(p1done == false){
				if(keys[KeyEvent.VK_A]){
					p1skin--;
					if(p1skin == p2skin)
						p1skin--;
					if(p1skin < 0)
						p1skin = 4;
					keys[KeyEvent.VK_A] = false;
				}
				if(keys[KeyEvent.VK_D]){
					p1skin++;
					if(p1skin == p2skin)
						p1skin++;
					if(p1skin > 4)
						p1skin = 0;
					keys[KeyEvent.VK_D] = false;
				}
				if(keys[KeyEvent.VK_1]){
					p1done = true;
					keys[KeyEvent.VK_1] = false;
				}
			}
			// Handle player 2
			if(p2done == false){
				if(keys[KeyEvent.VK_LEFT]){
					p2skin--;
					if(p2skin == p1skin)
						p2skin--;
					if(p2skin < 0)
						p2skin = 4;
					keys[KeyEvent.VK_LEFT] = false;
				}
				if(keys[KeyEvent.VK_RIGHT]){
					p2skin++;
					if(p2skin == p1skin)
						p2skin++;
					if(p2skin > 4)
						p2skin = 0;
					keys[KeyEvent.VK_RIGHT] = false;
				}
				if(keys[KeyEvent.VK_MINUS]){
					p2done = true;
					keys[KeyEvent.VK_MINUS] = false;
				}
			}
			g.drawImage(RM.getInstance().imgSelection, 0, 0, BUFFERWIDTH, BUFFERHEIGHT, null);

			if(p1done == true && p2done == true){
				if(keys[KeyEvent.VK_RIGHT]){
					mapselection++;
					if(mapselection > 2)
						mapselection = 0;
					keys[KeyEvent.VK_RIGHT] = false;
				}
				if(keys[KeyEvent.VK_LEFT]){
					mapselection--;
					if(mapselection < 0)
						mapselection = 2;
					keys[KeyEvent.VK_LEFT] = false;
				}
				if(keys[KeyEvent.VK_MINUS] || keys[KeyEvent.VK_ENTER]){
					selectedmap = mapselection;
					menustate = GAME_STATE;
					keys[KeyEvent.VK_MINUS] = false;
					keys[KeyEvent.VK_ENTER] = false;
				}

				g.setColor(Color.blue);
				g.drawRect(60+mapselection*68,154,63,48);
				g.drawRect(61+mapselection*68,155,61,46);
			}

			// Draw red selection
			g.setColor(Color.red);
			g.drawRect(70+36*p1skin,66,35,35);
			g.drawRect(71+36*p1skin,67,33,33);

			// Draw blue selection
			g.setColor(Color.blue);
			g.drawRect(70+36*p2skin,66,35,35);
			g.drawRect(71+36*p2skin,67,33,33);

			if(p1done == true || p2done == true){
				g.setColor(new Color(0,0,0,64));
				if(p1done)
					g.fillRect(72+p1skin*36,68,32,32);
				if(p2done)
					g.fillRect(72+p2skin*36,68,32,32);
			}

			appletg.drawImage(dbImage,0,0,SCREENWIDTH,SCREENHEIGHT,null);

			if(keys[KeyEvent.VK_ESCAPE]){
				menustate = MAIN_MENU_STATE;
				keys[KeyEvent.VK_ESCAPE] = false;
			}

			try{
				Thread.sleep(25);
			} catch (Exception e) {}
		}
	}

	public void showMainMenu(){
		int selection = 0;
		while(menustate == MAIN_MENU_STATE){

			if(keys[KeyEvent.VK_DOWN]){
				selection++;
				keys[KeyEvent.VK_DOWN] = false;
			}
			if(keys[KeyEvent.VK_UP]){
				selection--;
				keys[KeyEvent.VK_UP] = false;
			}
			if(keys[KeyEvent.VK_ENTER] || keys[KeyEvent.VK_MINUS]){
				if(selection == 0)
					menustate = SELECTION_STATE;
				keys[KeyEvent.VK_ENTER] = false;
				keys[KeyEvent.VK_MINUS] = false;
			}
			if(selection < 0)
				selection = 2;
			else if(selection > 2)
				selection = 0;

			g.drawImage(RM.getInstance().imgSplash, 0, 0, BUFFERWIDTH, BUFFERHEIGHT, null);
			g.drawImage(RM.getInstance().imgEntities, 48+selection*16, 87+selection*37, 64+selection*16, 103+selection*37, 80,0,96,16, null);
			g.drawImage(RM.getInstance().imgEntities, 256-selection*16, 87+selection*37, 272-selection*16, 103+selection*37, 80,0,96,16, null);
			appletg.drawImage(dbImage, 0, 0, SCREENWIDTH, SCREENHEIGHT, null);

			try{
				Thread.sleep(20);
			} catch (Exception e) {}
		}
	}

	public void loadLevelFromASCII(String filename){
		map = new int[MAPWIDTH][MAPHEIGHT];
		// TODO: EMPTY INSTEAD
		entities = new ArrayList<Entity>();
		spawns = new ArrayList<Spawn>();
		try{
			//File file = new File(filename);
			//File file = new File(getClass().getResource(filename));
			Scanner scan = new Scanner(getClass().getResourceAsStream(filename));
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
		} catch (Exception e) {	}
	}

	public void drawDebugInfo(Graphics g){
		g.setColor(Color.black);
		g.drawString("Entities: " + entities.size(),8,16);
		g.drawString("Particles: " + particles.size(),8,32);
		g.drawString("P1 X: "+(int)p1.x+" Y: "+(int)p1.y,8,48);
		g.drawString("P2 X: "+(int)p2.x+" Y: "+(int)p2.y,8,64);
		g.drawString("P1 lives: "+p1.lives,8,80);
		g.drawString("P2 lives: "+p2.lives,8,96);
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

	public static final int MAIN_MENU_STATE = 0;
	public static final int GAME_STATE      = 1;
	public static final int HOWTO_STATE     = 2;
	public static final int SELECTION_STATE = 3;
}
