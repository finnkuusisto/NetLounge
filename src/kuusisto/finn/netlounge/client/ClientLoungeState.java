package kuusisto.finn.netlounge.client;

import java.awt.Graphics;
import java.util.List;

public class ClientLoungeState {
	
	private int width;
	private int height;
	private List<ClientPerson> persons;
	
	public ClientLoungeState() { }
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public void setPersons(List<ClientPerson> persons) {
		this.persons = persons;
	}
	
	public void draw(Graphics g) {
		for (ClientPerson p : this.persons) {
			int x = (int)((p.getX() / this.width) * LoungeClient.WIDTH) -
				(ClientPerson.IMG.getWidth() / 2);
			int y = (int)((p.getY() / this.height) * LoungeClient.HEIGHT) -
				(ClientPerson.IMG.getHeight() / 2);
			g.drawImage(ClientPerson.IMG, x, y, null);
		}
	}

}
