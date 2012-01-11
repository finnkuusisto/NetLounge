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

package kuusisto.finn.netlounge.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kuusisto.finn.netlounge.Constants;

public class LoungeState {
	
	public static final double WIDTH = 500;
	public static final double HEIGHT = 500;
	public static final double STEPS_PER_TICK = 1;

	private Map<Integer,Person> persons;
	
	public LoungeState() {
		this.persons = new HashMap<Integer,Person>();
	}
	
	public void addPerson(Person person) {
		//assign them a position
		person.setX(Math.random() * LoungeState.WIDTH);
		person.setY(Math.random() * LoungeState.HEIGHT);
		this.persons.put(person.getID(), person);
	}
	
	public void removePerson(Person person) {
		this.persons.remove(person.getID());
	}
	
	public void removePerson(int id) {
		this.persons.remove(id);
	}
	
	public boolean hasPerson(int id) {
		return this.persons.containsKey(id);
	}
	
	public void tick(List<PersonCommand> commands) {
		for (PersonCommand command : commands) {
			Person person = this.persons.get(command.getPersonID());
			//do we have a person with that ID
			if (person != null) {
				//how much do they move?
				double xd = 0;
				double yd = 0;
				switch (command.getCommand()) {
					case Constants.PCMD_UP: 
						yd -= LoungeState.STEPS_PER_TICK;
						break;
					case Constants.PCMD_UP_RIGHT:
						//so what if they move farther on diagonals
						xd += LoungeState.STEPS_PER_TICK;
						yd -= LoungeState.STEPS_PER_TICK;
						break;
					case Constants.PCMD_RIGHT:
						xd += LoungeState.STEPS_PER_TICK;
						break;
					case Constants.PCMD_DOWN_RIGHT:
						//so what if they move farther on diagonals
						xd += LoungeState.STEPS_PER_TICK;
						yd += LoungeState.STEPS_PER_TICK;
						break;
					case Constants.PCMD_DOWN:
						yd += LoungeState.STEPS_PER_TICK;
						break;
					case Constants.PCMD_DOWN_LEFT:
						//so what if they move farther on diagonals
						xd -= LoungeState.STEPS_PER_TICK;
						yd += LoungeState.STEPS_PER_TICK;
						break;
					case Constants.PCMD_LEFT:
						xd -= LoungeState.STEPS_PER_TICK;
						break;
					case Constants.PCMD_UP_LEFT:
						//so what if they move farther on diagonals
						xd -= LoungeState.STEPS_PER_TICK;
						yd -= LoungeState.STEPS_PER_TICK;
						break;
				}
				//bounds check
				double newX = person.getX() + xd;
				double newY = person.getY() + yd;
				if (newX < 0) {
					newX = 0;
				}
				if (newY < 0) {
					newY = 0;
				}
				if (newX > LoungeState.WIDTH) {
					newX = LoungeState.WIDTH;
				}
				if (newY > LoungeState.HEIGHT) {
					newY = LoungeState.HEIGHT;
				}
				//update
				person.setX(newX);
				person.setY(newY);
			}
		}
	}
	
	public String getStateMessage() {
		StringBuilder str = new StringBuilder();
		//start with message type
		str.append(Constants.MSG_STATE);
		//and add 1 line per person
		for (Person p : this.persons.values()) {
			str.append(Constants.MSG_LINE_SEP);
			str.append(p.getName());
			str.append(Constants.MSG_INLINE_SEP);
			str.append(p.getX());
			str.append(Constants.MSG_INLINE_SEP);
			str.append(p.getY());
		}
		return str.toString();
	}
	
}
