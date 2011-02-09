import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Player extends Entity {
	public static final int MOVESPEED = 4;
	public static final float JUMPPOWER = 9.f;
	public static final float MAXSPEED = 8.f;
	public static final float GRAVITY = 0.8f;
	public static final int DOUBLEJUMPWAIT = 10; // Updates

	private float xspeed, yspeed;
	private int player, skin;
	private int djwait, walkWait;
	public boolean dir; // False = left. True = right.
	private boolean onGround, onCeiling, hasDoubleJumped, walkState, moving;
	public int[] cs;
	public boolean[] keys;

	public Player(int x, int y, int player, int skin){
		super(x+3,y,10,16);
		this.xspeed = this.yspeed = 0;
		this.player = player;	
		this.skin = skin;

		// Create keystate array and 
		if(player == 1){
			cs = ControlScheme.P1DEFAULT;
			dir = false;
		}
		else if(player == 2){
			cs = ControlScheme.P2DEFAULT;
			dir = true;
		}
	}

	public Player(Spawn sp, int player, int skin){
		this(sp.x,sp.y,player,skin);
	}

	public void move(int[][] map, boolean[] keys){
		onGround = onCeiling = moving = false;

		yspeed += GRAVITY;

		// Falling
		if(yspeed > 0){
			for(float ys = yspeed; ys > 0; ys-=1){
				if(canMove(map,(int)x,(int)(y+ys))){
					y += ys;
					break;
				}
				else{
					onGround = true;
					hasDoubleJumped = false;
				}
			}
		}
		// Jumping
		if(yspeed < 0){
			for(int ys = (int)yspeed; ys < 0; ys++){
				if(canMove(map,(int)x,(int)(y+ys))){
					y += ys;
					break;
				}
				else
					onCeiling = true;
			}
		}
		if(onGround || onCeiling){
			yspeed = 0;
		}

		// Handle jump and double jump
		if(keys[cs[2]]){
			if(onGround){
				yspeed = -JUMPPOWER;
				djwait = DOUBLEJUMPWAIT;
				keys[cs[2]] = false;
			}
			//else if(djwait < 0 && hasDoubleJumped == false){
			else if(hasDoubleJumped == false && yspeed > -2.0f){
				yspeed = -(JUMPPOWER * 0.8f); // Cut down jumppower second time
				hasDoubleJumped = true;
			}
		}
		
		// Move left
		if(keys[cs[0]]){
			dir = false;
			for(int i = MOVESPEED; i > 0; --i){
				if(canMove(map,(int)x-i,(int)y)){
					x = x-i;
					moving = true;
					break;
				}
			}
		}
		// Move right
		if(keys[cs[1]]){
			dir = true;
			for(int i = MOVESPEED; i > 0; --i){
				if(canMove(map,(int)x+i,(int)y)){
					x = x+i;
					moving = true;
					break;
				}
			}
		}
		if(walkWait < 0){
			walkState = !walkState;
			walkWait = 7;
		}
		else
			walkWait--;
	}

	public void handleCollision(Entity e){
		// Jumppad
		if(e instanceof Jumppad){
			Jumppad jp = (Jumppad)e;
			yspeed = jp.power;
		}
		else if(e instanceof Lava){
			yspeed = 0;
		}
	}

	public boolean canMove(int[][] map, int cx, int cy){
		int x1 = (int)cx/16;
		int x2 = ((int)cx+w-1)/16;
		int y1 = (int)cy/16;
		int y2 = ((int)cy+h-1)/16;

		if(x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0)
			return false;
		if(x1 >= Game.MAPWIDTH || x2 >= Game.MAPWIDTH || y1 >= Game.MAPHEIGHT || y2 >= Game.MAPHEIGHT)
			return false;

		if(map[x1][y1] == 1 || map[x2][y1] == 1 || map[x1][y2] == 1 || map[x2][y2] == 1)
			return false;
		return true;
	}

	public void draw(Graphics g, BufferedImage skins){
		int srcx = 0;
		/*
			REMOVED ANIMATIONS FOR NOW
			TODO: Reimplement

		// On ground
		if(yspeed == 0){
			// standing still = do nothing = 0
			if(moving){
				if(walkState){
					srcx = 16;
				}
				else{
					srcx = 32;	
				}
			}
		}
		// jumping
		//else if(yspeed < 0)
		else
			srcx = 32;
		// falling = standing still = 0
		*/
		if(dir) // right
			g.drawImage(skins, (int)x-3,(int)y,(int)x+13,(int)y+16, srcx,skin*16,srcx+16,(skin+1)*16, null);
		else    // left
			g.drawImage(skins, (int)x-3,(int)y,(int)x+13,(int)y+16, srcx+16,skin*16,srcx,(skin+1)*16, null);
	}

	public void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}
}
