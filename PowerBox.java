import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class PowerBox extends Entity {
	public static final int RESPAWNTIME = 32;
	private int spawn;
	public int ownedBy;
	public int type;

	public PowerBox(int x, int y){
		super(x+3,y+3,10,9);
		ownedBy = 0;
		type = TYPE_NONE;
	}

	public void update(){
	}

	public void handleCollision(Entity e){
		if(e instanceof Player){
			Player p = (Player)e;
			if(ownedBy == 0 && p.power == null){
				RM.getInstance().auPower.play();
				type = Game.rand.nextInt(TYPES) + 1;
				ownedBy = p.player;
				p.power = this;
			}
		}
	}

	public void reset(){
		ownedBy = 0;
		type = TYPE_NONE;
	}

	public void draw(Graphics g){
		int srcx = type*16;
		int srcy = 32;
		if(type > 0 && ownedBy == 2){
			srcy += 16;
		}
		g.drawImage(RM.getInstance().imgEntities, (int)x-3,(int)y-3,(int)x+13,(int)y+13,srcx,srcy,srcx+16,srcy+16, null);	
	}

	public static final int[] POWER_TIMES = {0,100,100,200,0,250};
	public static final int TYPE_NONE    = 0;
	public static final int TYPE_VVVVVV  = 1;
	public static final int TYPE_FREEZE  = 2;
	public static final int TYPE_REVERSE = 3;
	public static final int TYPE_SWITCH  = 4;
	public static final int TYPE_BOMB    = 5;
	public static final int TYPES = 5;
}
