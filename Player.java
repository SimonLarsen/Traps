import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Player extends Entity {
	public static final int MOVESPEED = 4;
	public static final float JUMPPOWER = 9.f;
	public static final float MAXSPEED = 8.f;
	public static final float GRAVITY = 0.8f;
	public static final int DOUBLEJUMPWAIT = 10; // Updates

	private float xspeed, yspeed;
	public int player, lives;
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
		this.lives = Game.start_lives;

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
			keys[cs[3]] = false;
			returnCode = power.type;
			power.reset();
			power = null;
		}

		// Increment walk cycle counter
		walkFrame += 0.25f;
		if(walkFrame >= 4){
			walkFrame = 0.f;
		}

		// Count down punishment timer
		if(punishment > 0){
			punishmentTime--;
			if(punishmentTime <= 0){
				if(punishment == PowerBox.TYPE_BOMB){
					returnCode = RETURN_BOMBED;
				}
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
			RM.getInstance().auJump.play();
		}
		else if(e instanceof PowerBox){
		}
		else if(e instanceof Lava){
			Game.keys[cs[0]] = false;	
			Game.keys[cs[1]] = false;	
		}
		else if(e instanceof Player){
			if(punishment == PowerBox.TYPE_BOMB && punishmentTime < PowerBox.POWER_TIMES[PowerBox.TYPE_BOMB]-BOMB_WAIT){
				Player p = (Player)e;	
				if(p.punishment != PowerBox.TYPE_BOMB){
					p.punish(PowerBox.TYPE_BOMB);
					this.punishment = this.punishmentTime = 0;
				}
			}
		}
	}
	
	public void punish(int pType){
		if(pType == PowerBox.TYPE_SWITCH){
			float p1x = Game.p1.x;
			float p1y = Game.p1.y;
			Game.p1.setPos(Game.p2.x-3,Game.p2.y);
			Game.p1.yspeed = Game.p2.yspeed = 0.f;
			Game.p2.setPos(p1x-3,p1y);
		}
		else{
			punishment = pType;
			punishmentTime = PowerBox.POWER_TIMES[pType];
		}
	}

	public void respawn(Spawn sp){
		setPos(sp);
		yspeed = 0;
		if(power != null){
			//if(power.ownedBy == player){
				power.reset();
			//}
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

		if(map[x1][y1] > 0 || map[x2][y1] > 0 || map[x1][y2] > 0 || map[x2][y2] > 0)
			return false;
		return true;
	}

	public void draw(Graphics g){
		int srcx = 0;
		// On ground
		if(yspeed == 0){
			if(moving){
				srcx = (int)walkFrame*16+16;
			}
			// standing still = do nothing = 0
		}
		// jumping
		else{
			srcx = 16;
		}
		if(punishment == PowerBox.TYPE_FREEZE){
			srcx = 16;
		}
		// falling = standing still = 0
		if(dir) // right
			g.drawImage(RM.getInstance().imgSkins, (int)x-3,(int)y,(int)x+13,(int)y+16, srcx,skin*16,srcx+16,(skin+1)*16, null);
		else    // left
			g.drawImage(RM.getInstance().imgSkins, (int)x-3,(int)y,(int)x+13,(int)y+16, srcx+16,skin*16,srcx,(skin+1)*16, null);

		// Draw ice cube if frozen
		if(punishment == PowerBox.TYPE_FREEZE){
			int offset = 0;
			if(punishmentTime < 16){
				offset = 16 - punishmentTime;
			}
			g.drawImage(RM.getInstance().imgSkins,(int)x-3,(int)y+offset,(int)x+13,(int)y+16, 80,offset,96,16, null); 
		}
		// Draw swirly on head if reversed
		else if(punishment == PowerBox.TYPE_REVERSE){
			int xstart = 88;
			if((int)walkFrame < 2)
				xstart = 80;
			g.drawImage(RM.getInstance().imgSkins,(int)x+1,(int)y-8,(int)x+9,(int)y, xstart,16,xstart+8,24, null); 
		}
		// Draw bomb if bombed?
		else if(punishment == PowerBox.TYPE_BOMB){
			int xstart = 80;
			if((int)walkFrame < 2)
				xstart = 88;
			g.drawImage(RM.getInstance().imgSkins,(int)x+2,(int)y-9,(int)x+10,(int)y-1, xstart,24,xstart+8,32, null);	
		}
	}

	public void setPos(float x, float y){
		this.x = x+3;
		this.y = y;
	}

	public void setPos(Spawn sp){
		this.x = sp.x+3;
		this.y = sp.y;
	}

	/** NOTE: FIRST PowerBox.TYPE ints should be reserved
	 *  for using powers. Use high values for other purposes!
	 */
	public static final int RETURN_DIED        = 666;
	public static final int RETURN_BOMBED      = 123;
	public static final int BOMB_WAIT = 30;
}
