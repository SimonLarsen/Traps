import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Jumppad extends Entity {
	public static final int DOWNTIME = 5;
	public static final float POWER = -15.f;
	public float power;
	public int down;

	public Jumppad(int x,int y, float power){
		super(x+1,y+9,14,7);
		this.power = power;
	}

	public void update(){
		if(down > 0)	
			down--;
	}

	public void handleCollision(Entity e){
		if(e instanceof Player)
			down = DOWNTIME;
	}

	public void draw(Graphics g, BufferedImage traps){
		if(down > 0)
			g.drawImage(traps,(int)x-1,(int)y-9,(int)x+15,(int)y+7,16,0,32,16,null);
		else
			g.drawImage(traps,(int)x-1,(int)y-9,(int)x+15,(int)y+7,0,0,16,16,null);
	}
}
