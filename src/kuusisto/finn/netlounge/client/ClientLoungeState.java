/*
 * Copyright (C) 2012 by Finn Kuusisto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
