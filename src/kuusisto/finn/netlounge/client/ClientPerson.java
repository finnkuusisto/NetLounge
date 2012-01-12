package kuusisto.finn.netlounge.client;

import java.awt.image.BufferedImage;

public class ClientPerson {
	
	private static final String IMG_NAME = "/person.gif";
	public static final BufferedImage IMG =
		ImageUtils.loadImage(ClientPerson.IMG_NAME);
	
	private String name;
	private double x;
	private double y;
	
	public String getName() {
		return this.name;
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}

}
