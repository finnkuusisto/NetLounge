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

public class LoungeState {
	
	public static final double WIDTH = 500;
	public static final double HEIGHT = 500;
	public static final double STEPS_PER_TICK = 1;
	public static final int TICKS_PER_SEC = 30;

	private Map<Integer,Person> persons;
	
	public LoungeState() {
		this.persons = new HashMap<Integer,Person>();
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
					case PersonCommand.UP: 
						yd -= LoungeState.STEPS_PER_TICK;
						break;
					case PersonCommand.UP_RIGHT:
						//so what if they move farther on diagonals
						xd += LoungeState.STEPS_PER_TICK;
						yd -= LoungeState.STEPS_PER_TICK;
						break;
					case PersonCommand.RIGHT:
						xd += LoungeState.STEPS_PER_TICK;
						break;
					case PersonCommand.DOWN_RIGHT:
						//so what if they move farther on diagonals
						xd += LoungeState.STEPS_PER_TICK;
						yd += LoungeState.STEPS_PER_TICK;
						break;
					case PersonCommand.DOWN:
						yd += LoungeState.STEPS_PER_TICK;
						break;
					case PersonCommand.DOWN_LEFT:
						//so what if they move farther on diagonals
						xd -= LoungeState.STEPS_PER_TICK;
						yd += LoungeState.STEPS_PER_TICK;
						break;
					case PersonCommand.LEFT:
						xd -= LoungeState.STEPS_PER_TICK;
						break;
					case PersonCommand.UP_LEFT:
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
	
}
