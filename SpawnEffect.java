import java.awt.Graphics;

public class SpawnEffect extends Particle {
	private int step,player;

	public SpawnEffect(int x, int y, int player){
		super(x,y);
		this.player = player;
		this.step = 0;
	}

	public void update(){
		step++;
		if(step > 8)
			alive = false;
	}

	public void draw(Graphics g){
	}
}
