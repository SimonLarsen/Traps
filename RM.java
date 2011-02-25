import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class RM {
	private static final RM INSTANCE = new RM();
	public static AudioClip auBurn, auSaw, auPower, auJump, auExplosion;
	public static BufferedImage imgTiles, imgSkins, imgEntities, imgParticles, imgSplash, imgSelection;
	public static Player bgm1;
	public static Font baseFont, smallFont, menuFont;

	private RM() {}

	public static RM getInstance() {
		return INSTANCE;
	}

	public boolean loadGFX(){
		try{
			imgSkins = ImageIO.read(getClass().getResource("gfx/skins.png"));
			imgTiles = ImageIO.read(getClass().getResource("gfx/tiles.png"));
			imgEntities = ImageIO.read(getClass().getResource("gfx/entities.png"));
			imgParticles = ImageIO.read(getClass().getResource("gfx/particles.png"));
			imgSplash = ImageIO.read(getClass().getResource("gfx/splash.png"));
			imgSelection = ImageIO.read(getClass().getResource("gfx/selection.png"));
			InputStream is = getClass().getResourceAsStream("gfx/font.ttf");
			baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
			smallFont = baseFont.deriveFont(8.f);
			menuFont = baseFont.deriveFont(16.f);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean loadSFX(){
		auBurn = Applet.newAudioClip(Game.class.getResource("sfx/burn.wav"));
		auSaw = Applet.newAudioClip(Game.class.getResource("sfx/saw.wav"));
		auPower = Applet.newAudioClip(Game.class.getResource("sfx/power.wav"));
		auJump = Applet.newAudioClip(Game.class.getResource("sfx/jump.wav"));
		auExplosion = Applet.newAudioClip(Game.class.getResource("sfx/explosion.wav"));
		return true;
	}
}
