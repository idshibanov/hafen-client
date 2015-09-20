package haven;

import haven.Coord;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import javax.imageio.ImageIO;

public class StitcherService {
	private static final int width = 100;
	private static final int height = 100;

	public static void stitch(String session, Coord min, Coord max) {
		try {
			File folder = new File("map/" + session);
			File[] tiles = folder.listFiles();

			int cols = max.x - min.x + 1;
			int rows = max.y - min.y + 1;
			
			BufferedImage result = new BufferedImage(cols * width, rows * height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = result.createGraphics();
			g2d.setComposite(AlphaComposite.Clear);
			g2d.fillRect(0, 0, cols * width, rows * height);

			g2d.setComposite(AlphaComposite.Src);
			for (File file : tiles) {
			  String fname = file.getName();
			  String coord = fname.substring(5, fname.length() - 4);
			  int i = coord.indexOf('_');
			  int x = Integer.parseInt(coord.substring(0, i));
			  int y = Integer.parseInt(coord.substring(i + 1));

			  BufferedImage maptile = ImageIO.read(file);
			  g2d.drawImage(maptile, (x - min.x) * width, (y - min.y) * height, null);
			}

			ImageIO.write(result, "png", new File("map/" + session + ".png"));	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}