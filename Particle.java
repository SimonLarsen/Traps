import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Particle {
	public int x,y;
	public boolean alive;

	public Particle(int x, int y){
		this.x = x; this.y = y;
		alive = true;
	}

	public void update(){
	}

	public void draw(Graphics g, BufferedImage img){
	}
}
