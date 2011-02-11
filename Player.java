import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Player extends Entity {
	public static final int MOVESPEED = 4;
	public static final float JUMPPOWER = 9.f;
	public static final float MAXSPEED = 8.f;
	public static final float GRAVITY = 0.8f;
	public static final int DOUBLEJUMPWAIT = 10; // Updates

	private float xspeed, yspeed;
	public int player, deaths;
	private int skin, djwait;
	private float walkFrame;
	public boolean dir; // False = left. True = right.
	private boolean onGround, onCeiling, hasDoubleJumped, walkState, moving;
	public int[] cs;
	public boolean[] keys;
	public PowerBox power;
	private int punishment, punishmentTime;

	public Player(int x, int y, int player, int skin){
		super(x+3,y,10,16);
		this.xspeed = this.yspeed = 0;
		this.player = player;	
		this.skin = skin;
		this.power = null;
		this.deaths = 0;

		this.punishment = this.punishmentTime = 0;

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

	public int move(int[][] map, boolean[] keys){
		// Reset tmp. stats
		int returnCode = 0;
		onGround = onCeiling = moving = false;

		// Check for reversed gravity
		if(punishment == PowerBox.TYPE_VVVVVV){
			yspeed -= GRAVITY;
			moving = false;
		}
		else{
			yspeed += GRAVITY;
		}

		// Falling
		if(yspeed > 0){
			for(float ys = yspeed; ys > 0; ys-=1.f){
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
			for(float ys = yspeed; ys < 0; ys+=1.f){
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
		if(keys[cs[2]] && punishment != PowerBox.TYPE_FREEZE){
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
		
		// Move only if player is not frozen
		if(punishment != PowerBox.TYPE_FREEZE){
			// Move left
			if( (keys[cs[0]] && punishment != PowerBox.TYPE_REVERSE)
			 || (keys[cs[1]] && punishment == PowerBox.TYPE_REVERSE) ){
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
			if( (keys[cs[1]] && punishment != PowerBox.TYPE_REVERSE)
			 || (keys[cs[0]] && punishment == PowerBox.TYPE_REVERSE) ){
				dir = true;
				for(int i = MOVESPEED; i > 0; --i){
					if(canMove(map,(int)x+i,(int)y)){
						x = x+i;
						moving = true;
						break;
					}
				}
			}
		}

		// Check if action button is pressed
		if(keys[cs[3]] && power != null){
			returnCode = power.type;
			power.reset();
			power = null;
		}

		// Increment walk cycle counter
		if(moving){
			walkFrame += 0.25f;
			if(walkFrame >= 4)
				walkFrame = 0.f;
		}

		// Count down punishment timer
		if(punishment > 0){
			punishmentTime--;
			if(punishmentTime <= 0){
				punishment = 0;
			}
		}

		return returnCode;
	}

	public void handleCollision(Entity e){
		// Jumppad
		if(e instanceof Jumppad){
			Jumppad jp = (Jumppad)e;
			yspeed = jp.power;
		}
		else if(e instanceof Lava){
			// Respawn called from Game
		}
		else if(e instanceof PowerBox){
			if(power == null){
				power = (PowerBox)e;
			}
		}
	}
	
	public void punish(int pType){
		punishment = pType;
		punishmentTime = PowerBox.POWER_TIMES[pType];
	}

	public void respawn(Spawn sp){
		setPos(sp);
		yspeed = 0;
		if(power != null){
			power.reset();
			power = null;
		}
		punishment = punishmentTime = 0;
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
		// On ground
		if(yspeed == 0){
			if(moving){
				srcx = (int)walkFrame*16+16;
			}
			// standing still = do nothing = 0
		}
		// jumping
		//else if(yspeed < 0)
		else
			srcx = 16;
		// falling = standing still = 0
		if(dir) // right
			g.drawImage(skins, (int)x-3,(int)y,(int)x+13,(int)y+16, srcx,skin*16,srcx+16,(skin+1)*16, null);
		else    // left
			g.drawImage(skins, (int)x-3,(int)y,(int)x+13,(int)y+16, srcx+16,skin*16,srcx,(skin+1)*16, null);
	}

	private void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}

	private void setPos(Spawn sp){
		this.x = sp.x;
		this.y = sp.y;
	}

	public static final int RETURN_NONE        = 0;
	public static final int RETURN_USED_VVVVVV = 1;
	public static final int RETURN_USED_FREEZE = 2;
	// next couple of ints should be reserved for powers
	public static final int RETURN_DIED        = 666;
}
