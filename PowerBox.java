import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class PowerBox extends Entity {
	public static final int RESPAWNTIME = 32;
	private int spawn;

	public PowerBox(int x, int y){
		super(x+3,y+3,10,9);
	}

	public void update(){
		if(!alive){
			spawn--;
			if(spawn <= 0)
				alive = true;
		}
	}

	public void handleCollision(Entity e){
		if(e instanceof Player){
			alive = false;
			spawn = RESPAWNTIME;
		}
	}

	public void draw(Graphics g, BufferedImage img){
		if(alive)
			g.drawImage(img, (int)x,(int)y,(int)x+w,(int)y+h,3,35,13,44, null);	
	}
}
