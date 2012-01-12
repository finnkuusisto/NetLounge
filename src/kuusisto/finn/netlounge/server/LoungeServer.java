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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoungeServer {

	public static final long DISCONNECT_MILLIS = 2000; //2 secs
	public static final int TICKS_PER_SEC = 30;
	public static final long NANOS_PER_TICK =
		1000000000 / LoungeServer.TICKS_PER_SEC;
	public static final long MILLIS_PER_TICK =
		1000 / LoungeServer.TICKS_PER_SEC;
	public static final int DEFAULT_PORT = 7777;
	public static final boolean VERBOSE = true;
	
	private LoungeServerListenThread listener;
	private DatagramSocket sendSocket;
	private LoungeState state;
	private Map<Integer,PersonCommand> incomingCommands;
	private Map<Integer,Long> lastKeepAlive;	
	private Map<Integer,Person> connectingPersons;
	private Map<Integer,Person> connectedPersons;
	
	public LoungeServer(int port) {
		this.listener = new LoungeServerListenThread(port, this);
		try {
			this.sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			//ugh
			System.out.println("Couldn't open send socket!");
			System.exit(1);
		}
		this.state = new LoungeState();
		this.incomingCommands = new HashMap<Integer,PersonCommand>();
		this.lastKeepAlive = new HashMap<Integer,Long>();
		this.connectingPersons = new HashMap<Integer,Person>();
		this.connectedPersons = new HashMap<Integer,Person>();
	}
	
	private void run() {
		//start the listen thread
		this.listener.start();
		try { //sleep to let the listener start
			Thread.sleep(2);
		} catch (InterruptedException e1) {}
		//start the simulation loop
		long now = 0;
		long lastTick = 0;
		boolean running = true;
		while (running) {
			now = System.nanoTime();
			//if time to tick
			if (now - lastTick > LoungeServer.NANOS_PER_TICK) {
				lastTick = System.nanoTime();
				//collect and clear commands
				List<PersonCommand> commands = this.collectAndClearCommands();
				//issue the commands tick
				this.state.tick(commands);
				//send state info
				this.sendState();
			}
			//check for connecting Persons
			this.checkConnects();
			//check for disconnected Persons
			this.checkDisconnects();
			//sleep for a bit
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) { }
		}
	}
	
	private List<PersonCommand> collectAndClearCommands() {
		List<PersonCommand> ret = new ArrayList<PersonCommand>();
		//lock incoming commands
		synchronized (this.incomingCommands) {
			//gather commands
			for (PersonCommand pc : this.incomingCommands.values()) {
				//only add those that are valid
				if (this.state.hasPerson(pc.getPersonID())) {
					ret.add(pc);
				}
			}
			//clear commands
			this.incomingCommands.clear();
		}
		return ret;
	}
	
	private void sendState() {
		String stateMessage = this.state.getStateMessage();
		byte[] data = stateMessage.getBytes();
		for (Person p : this.connectedPersons.values()) {
			DatagramPacket packet = new DatagramPacket(data, data.length, 
					p.getAddress(), p.getPort());
			try {
				this.sendSocket.send(packet);
			} catch (IOException e) {
				System.out.println("Failed sending state packet to " +
						p.getAddress());
			}
		}
	}
	
	private void checkDisconnects() {
		//first determine who is disconnected
		List<Integer> disconnectIDs = new ArrayList<Integer>();
		synchronized (this.lastKeepAlive) {
			long now = System.currentTimeMillis();
			//check every keep alive
			for (Integer i : this.lastKeepAlive.keySet()) {
				long timestamp = this.lastKeepAlive.get(i);
				//disconnect if timestamp too old
				if (now - timestamp > LoungeServer.DISCONNECT_MILLIS) {
					disconnectIDs.add(i);
				}
			}
		}
		//then disconnect them
		synchronized (this.connectedPersons) {
			for (Integer i : disconnectIDs) {
				this.connectedPersons.remove(i);
				this.state.removePerson(i);
			}
		}
		synchronized (this.connectingPersons) {
			for (Integer i : disconnectIDs) {
				this.connectingPersons.remove(i);
			}
		}
	}
	
	private void checkConnects() {
		List<Person> connecting = new ArrayList<Person>();
		synchronized (this.connectingPersons) {
			connecting.addAll(this.connectingPersons.values());
		}
		//check if they have issued a keep alive, only include those who have
		synchronized (this.lastKeepAlive) {
			for (int i = connecting.size() - 1; i >= 0; i--) {
				int id = connecting.get(i).getID();
				if (this.lastKeepAlive.containsKey(id)) {
					connecting.remove(i);
				}
			}
		}
		//connect those who if successfully connected
		if (connecting.size() > 0) {
			//remove them from the connecting group
			synchronized (this.connectingPersons) {
				for (Person p : connecting) {
					this.connectingPersons.remove(p.getID());
				}
			}
			//add them to the connected group and state
			synchronized (this.connectedPersons) {
				for (Person p : connecting) {
					if (!this.connectedPersons.containsKey(p.getID())) {
						if (LoungeServer.VERBOSE) {
							System.out.println("Connect success from " +
									p.getAddress());
							System.out.flush();
						}
						this.connectedPersons.put(p.getID(), p);
						this.state.addPerson(p);
					}
				}
			}
		}
	}
	
	public void issuePersonConnecting(Person person) {
		synchronized (this.connectingPersons) {
			this.connectingPersons.put(person.getID(), person);
		}
	}
	
	public void issuePersonCommand(PersonCommand command) {
		//lock incoming commands
		synchronized (this.incomingCommands) {
			this.incomingCommands.put(command.getPersonID(), command);
		}
	}
	
	public void issueKeepAlive(int personID) {
		//lock keep alive map
		synchronized (this.lastKeepAlive) {
			//someone could just flood the server with keep alive requests since
			//this doesn't validate, but that's not a huge concern right now
			this.lastKeepAlive.put(personID, System.currentTimeMillis());
		}
	}
	
	public static void main(String[] args) {
		int port = LoungeServer.DEFAULT_PORT;
		if (args.length != 1) {
			System.out.println("No port specified: using " +
					LoungeServer.DEFAULT_PORT);
		}
		else {
			try {
				int tmp = Integer.parseInt(args[0]);
				port = tmp;
				System.out.println("Using port " + port);
			} catch (NumberFormatException e) {
				System.out.println("Invalid port specified: using " +
						LoungeServer.DEFAULT_PORT);
			}
		}
		LoungeServer server = new LoungeServer(port);
		server.run();
	}

}
