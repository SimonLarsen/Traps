import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class BloodExplosion extends Particle {
	private int step;
	private BloodDrop[] drops;

	public BloodExplosion(int x, int y){
		super(x,y);
		drops = new BloodDrop[32];
		for(int i = 0; i < drops.length; ++i){
			drops[i] = new BloodDrop();
			drops[i].x = x;
			drops[i].y = y;
			drops[i].xspeed = 15.f*((float)Math.random()-0.5f);
			drops[i].yspeed = 15.f*((float)Math.random()-0.5f);
		}
		this.step = 0;
	}

	public void update(){
		for(int i = 0; i < drops.length; ++i){
			drops[i].x += drops[i].xspeed;
			drops[i].yspeed += 0.5f;
			drops[i].y += drops[i].yspeed;
		}
		step++;
		if(step > 50)
			alive = false;
	}

	public void draw(Graphics g){
		g.setColor(java.awt.Color.red);
		for(int i = 0; i < drops.length; ++i){
			g.fillRect((int)drops[i].x,(int)drops[i].y,2,2);
		}
	}
}
