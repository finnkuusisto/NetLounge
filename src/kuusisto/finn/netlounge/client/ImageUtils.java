package kuusisto.finn.netlounge.client;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtils {
	
	public static BufferedImage loadImage(String filename) {
		InputStream in = ImageUtils.class.getResourceAsStream(filename);
		BufferedImage ret = null;
		try {
			BufferedImage tmp = ImageIO.read(in);
			ret = new BufferedImage(tmp.getWidth(), tmp.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = ret.createGraphics();
			g.drawImage(tmp, 0, 0, null);
			g.dispose();
			ret.setAccelerationPriority(1.0F);
		} catch (IOException e) {
			System.err.println("Couldn't load " + filename + "!");
			e.printStackTrace();
		}
		return ret;
	}

}
