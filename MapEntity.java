import java.io.Serializable;

public class MapEntity implements Serializable {
	public int type, x, y;

	public MapEntity(int type, int x, int y){
		this.x = x;
		this.y = y;
		this.type = type;
	}
}
