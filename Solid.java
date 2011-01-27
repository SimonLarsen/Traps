import java.awt.Rectangle;

public class Solid {
	public float x,y;
	public int w,h;

	public Solid(float x, float y, int w, int h){
		this.x = x; this.y = y;
		this.w = w; this.h = h;
	}

	public Rectangle getBBox(){
		return new Rectangle((int)x,(int)y,w,h);
	}

	public static boolean collides(Solid s1, Solid s2){
		Rectangle r1 = s1.getBBox();
		Rectangle r2 = s2.getBBox();

		if( r1.x > r2.x+r2.width )
			return false;
		if( r1.y > r2.y+r2.height )
			return false;
		if( r1.x+r1.width < r2.x )
			return false;
		if( r1.y+r1.height < r2.y )
			return false;
		return true;
	}
}
