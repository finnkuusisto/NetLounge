package kuusisto.finn.netlounge.client;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class ClientLoungeState {
	
	private double width;
	private double height;
	private int clientID;
	private List<ClientPerson> persons;
	
	public ClientLoungeState() {
		this.persons = new ArrayList<ClientPerson>();
	}
	
	public void setWidth(double width) {
		this.width = width;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	public void setPersons(List<ClientPerson> persons) {
		this.persons = persons;
	}
	
	public void setClientID(int id) {
		this.clientID = id;
	}
	
	public void clearPersons() {
		this.persons.clear();
	}
	
	public void addPerson(ClientPerson person) {
		this.persons.add(person);
	}
	
	public int getClientID() {
		return this.clientID;
	}
	
	public void draw(Graphics g) {
		Color oldColor = g.getColor();
		g.setColor(Color.RED); //for text
		for (ClientPerson p : this.persons) {
			int x = (int)((p.getX() / this.width) * LoungeClient.WIDTH) -
				(ClientPerson.IMG.getWidth() / 2);
			int y = (int)((p.getY() / this.height) * LoungeClient.HEIGHT) -
				(ClientPerson.IMG.getHeight() / 2);
			g.drawImage(ClientPerson.IMG, x, y, null);
			int textY = y +	ClientPerson.IMG.getHeight() + 10;
			char[] name = p.getName().toCharArray();
			g.drawChars(name, 0, name.length, x, textY);
		}
		g.setColor(oldColor);
	}

}
