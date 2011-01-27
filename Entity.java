import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Entity extends Solid {
	// Entity types
	public static final int TYPE_JUMPPAD_UP = 0;

	public int type;

	public Entity(int x, int y, int w, int h, int type) {
		super(x,y,w,h);
		this.type = type;
	}

	public void draw(Graphics g, BufferedImage img){
		g.setColor(java.awt.Color.red);
		g.fillRect((int)x,(int)y,16,16);
	}
}
