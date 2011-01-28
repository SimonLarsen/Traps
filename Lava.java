import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Lava extends Entity {

	public Lava(int x, int y, int w){
		super(x,y+4,w*16,12);		
	}

	public void update(){

	}

	public void draw(Graphics g, BufferedImage tex){
		int stop = 0;
		while(stop < w){
			if(stop+64 >= w){
				int diff = w - stop;
				g.drawImage(tex,(int)x+stop,(int)y,(int)x+stop+diff,(int)y+12,0,20,diff,32,null);
				stop += diff;
			}
			else{
				g.drawImage(tex,(int)x+stop,(int)y,(int)x+stop+64,(int)y+12,0,20,64,32,null);
				stop += 64;
			}
		}
	}
}
