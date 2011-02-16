import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class BurntCorpse extends Particle {
	private int step;
	private boolean dir;

	public BurntCorpse(int x, int y, boolean dir){
		super(x,y);
		this.step = 0;
		this.dir = dir;
	}

	public void update(){
		step++;
		if(step >= 64)
			alive = false;
	}

	public void draw(Graphics g){
		int offset = step/4;
		int xoff = Math.round((float)Math.random());
		if(dir == false) // left
			g.drawImage(RM.getInstance().imgParticles,(int)x+xoff,(int)y+offset,(int)x+16+xoff,(int)y+16,16,0,0,16-offset,null);
		else // right
			g.drawImage(RM.getInstance().imgParticles,(int)x+xoff,(int)y+offset,(int)x+16+xoff,(int)y+16,0,0,16,16-offset,null);
	}
}
