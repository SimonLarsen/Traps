import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class BurntCorpse extends Particle {
	private int step;

	public BurntCorpse(int x, int y){
		super(x,y);
		this.step = 0;
	}

	public void update(){
		step++;
		if(step >= 64)
			alive = false;
	}

	public void draw(Graphics g, BufferedImage img){
		int offset = step/4;
		int xoff = Math.round((float)Math.random());
		g.drawImage(img,(int)x+xoff,(int)y+offset,(int)x+16+xoff,(int)y+16,0,0,16,16-offset,null);
	}
}
