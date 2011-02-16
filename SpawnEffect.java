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
		if(step > 8)
			alive = false;
	}

	public void draw(Graphics g){
		if(player == 1)
			g.setColor(java.awt.Color.red);
		else
			g.setColor(java.awt.Color.blue);

		g.fillRect(x+7-step,y+7+step,2,2);
		g.fillRect(x+7-step,y+7-step,2,2);
		g.fillRect(x+7+step,y+7+step,2,2);
		g.fillRect(x+7+step,y+7-step,2,2);
	}
}
