import java.awt.Graphics;
import java.awt.Color;

public class SpawnEffect extends Particle {
	private int step,player,lives;

	public SpawnEffect(int x, int y, int player,int lives){
		super(x,y);
		this.player = player;
		this.step = 0;
		this.lives = lives;
	}

	public SpawnEffect(Player p){
		super((int)p.x-3,(int)p.y);
		this.player = p.player;
		this.lives = p.lives;
		this.step = 0;
	}

	public void update(){
		step++;
		if(step > 32)
			alive = false;
	}

	public void draw(Graphics g){
		g.setFont(RM.getInstance().smallFont);
		if(player == 1){
			g.setColor(new Color(255,0,0,255-7*step));
			g.drawString(String.valueOf(lives),(int)Game.p1.x+1,(int)Game.p1.y-4);
		}
		else{
			g.setColor(new Color(0,0,255,255-7*step));
			g.drawString(String.valueOf(lives),(int)Game.p2.x+1,(int)Game.p2.y-4);
		}

		if(step < 16){
			g.fillRect(x+7-step, y+7+step, 2,2);
			g.fillRect(x+7-step, y+7-step, 2,2);
			g.fillRect(x+7+step, y+7+step, 2,2);
			g.fillRect(x+7+step, y+7-step, 2,2);
		}
	}
}
