import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Entity extends Solid {
	// Entity types
	public static final int TYPE_PLAYER = 0;
	public static final int TYPE_JUMPPAD_UP = 1;
	public boolean alive;

	public Entity(int x, int y, int w, int h) {
		super(x,y,w,h);
		alive = true;
	}

	public void update(){
	}

	public void handleCollision(Entity e){
	}

	public void draw(Graphics g){
	}
}
