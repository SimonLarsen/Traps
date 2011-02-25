import javazoom.jl.player.Player;
import java.io.InputStream;

public class MP3Player {
	private Player player;

	public MP3Player(){
		try{
			InputStream	is = getClass().getResourceAsStream("sfx/bgm.mp3");
			player = new Player(is);
		} catch (Exception e) {	}
	}

	public void play(){
		PlayerThread pt = new PlayerThread();
		pt.start();
	}

	private class PlayerThread extends Thread {
		public void run(){
			try {
				player.play();
			} catch (Exception e) {	}
		}
	}
}
