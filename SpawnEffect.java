import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class SpawnEffect extends Particle {
	private int step,player;

	public SpawnEffect(int x, int y, int player){
		super(x,y);
		this.player = player;
		this.step = 0;
	}

	public SpawnEffect(Player p){
		super((int)p.x-3,(int)p.y);
		this.player = p.player;
		this.step = 0;
	}

	public void update(){
		step++;
		if(step > 16)
			alive = false;
	}

	public void draw(Graphics g, BufferedImage img){
		if(player == 1)
			g.setColor(java.awt.Color.red);
		else
			g.setColor(java.awt.Color.blue);
		int hstep = step/2;
		g.drawOval(x+8-hstep,y+8-hstep,step,step);
	}
}
