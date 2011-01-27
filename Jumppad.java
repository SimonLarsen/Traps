import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Jumppad extends Entity {
	public float power;

	public Jumppad(int x,int y, float power){
		super(x+1,y+9,14,7,Entity.TYPE_JUMPPAD_UP);
		this.power = power;
	}

	public void draw(Graphics g, BufferedImage traps){
		g.setColor(java.awt.Color.red);
		g.fillRect((int)x,(int)y,w,h);
		g.drawImage(traps,(int)x-1,(int)y-9,(int)x+15,(int)y+7,0,0,16,16,null);
	}
}
