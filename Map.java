import java.util.ArrayList;
import java.io.Serializable;

public class Map implements Serializable {
	public int[][] map;
	public ArrayList<MapEntity> entities;

	public Map(int w, int h){
		map = new int[w][h];
		entities = new ArrayList<MapEntity>();
	}
}
