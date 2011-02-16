import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Saw extends Entity {
	private int frame;

	public Saw(int x, int  y, int w){
		super(x,y,w*16,12);
		frame = 0;
	}

	public void update(){
		frame = (frame + 1) % 3;
	}

	public void draw(Graphics g){
		// draw lower layer
		int start = 0;
		while(start+32 <= w){
			g.drawImage(RM.getInstance().imgEntities,(int)x+start,(int)y,(int)x+start+32,(int)y+16,frame*32,64,frame*32+32,80,null);
			start += 32;
		}
		// draw upper layer
		start = 16;
		while(start+32 <= w){
			g.drawImage(RM.getInstance().imgEntities,(int)x+start,(int)y,(int)x+start+32,(int)y+16,frame*32+32,64,frame*32,80,null);
			start += 32;
		}
	}
}
