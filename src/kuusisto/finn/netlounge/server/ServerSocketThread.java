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
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

import kuusisto.finn.netlounge.Constants;

public class ServerSocketThread extends Thread {
	
	private LoungeServer server;
	private DatagramSocket socket;
	private Queue<DatagramPacket> sendQueue;
	private boolean running;
	private int nextID;
	
	public ServerSocketThread(int port, LoungeServer server) {
		super("LoungeServerListenThread");
		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Invalid port specified: using " +
					LoungeServer.DEFAULT_PORT);
			try {
				this.socket = new DatagramSocket(LoungeServer.DEFAULT_PORT);
			} catch (SocketException ee) {
				//wow, let's just kill ourselves now
				ee.printStackTrace();
				System.exit(1);
			}
		}
		this.sendQueue = new LinkedList<DatagramPacket>();
		this.running = true;
		this.server = server;
		//insecurely start IDs at 0
		this.nextID = 0;
	}
	
	public void shutdown() {
		this.running = false;
	}
	
	public void issueSend(DatagramPacket packet) {
		synchronized (this.sendQueue) {
			this.sendQueue.add(packet);
		}
	}
	
	private void send() {
		synchronized (this.sendQueue) {
			DatagramPacket toSend = this.sendQueue.poll();
			while (toSend != null) {
				try {
					this.socket.send(toSend);
					
				} catch (IOException e) {
					System.out.println("Failed sending to " +
							toSend.getAddress());
				}
				toSend = this.sendQueue.poll();
			}
		}
	}
	
	private void processData(DatagramPacket packet) {
		//all messages are Strings
		String message = new String(packet.getData(), 0, packet.getLength());
		if (message.startsWith(Constants.MSG_COMMAND + 
				Constants.MSG_LINE_SEP)) {
			//handle issued command
			if (LoungeServer.VERBOSE) {
				System.out.println("command from " + packet.getAddress());
				System.out.flush();
			}
			this.handleCommand(message);
		}
		else if (message.startsWith(Constants.MSG_CONNECT + 
				Constants.MSG_LINE_SEP)) {
			//handle connect request
			if (LoungeServer.VERBOSE) {
				System.out.println("connect request from " +
						packet.getAddress());
				System.out.flush();
			}
			this.handleConnect(message, packet);
		}
		else if (message.startsWith(Constants.MSG_KEEPALIVE + 
				Constants.MSG_LINE_SEP)) {
			if (LoungeServer.VERBOSE2) {
				System.out.println("keep-alive from " + packet.getAddress());
				System.out.flush();
			}
			this.handleKeepAlive(message);
		}
		else if (message.startsWith(Constants.MSG_AUDIO +
				Constants.MSG_LINE_SEP)) {
			String[] parts = message.split(Constants.MSG_LINE_SEP);
			if (parts.length > 4) {
				try {
					int id = Integer.parseInt(parts[1]);
					this.server.issueAudioBroadcast(packet.getData(),
							packet.getLength(), id);
				}
				catch (NumberFormatException e) {
					return;
				}
			}
		}
		//otherwise it's an invalid message
	}
	
	private void handleCommand(String message) {
		String[] parts = message.split(Constants.MSG_LINE_SEP);
		//command should have two lines
		if (parts.length == 2) {
			String[] commandParts = parts[1].split(Constants.MSG_INLINE_SEP);
			//second line should have two parts as well (id then command)
			if (commandParts.length == 2) {
				try {
					int id = Integer.parseInt(commandParts[0]);
					int commandVal = Integer.parseInt(commandParts[1]);
					PersonCommand command = new PersonCommand(id, commandVal);
					this.server.issuePersonCommand(command);
				} catch (NumberFormatException e) { }
			}
		}
	}
	
	private void handleConnect(String message, DatagramPacket packet) {
		String[] parts = message.split(Constants.MSG_LINE_SEP);
		//connect should have two lines
		if (parts.length == 2) {
			//get them an ID
			int id = this.nextID;
			this.nextID++;
			//the second line is their name, validate it
			String name = this.getValidatedName(parts[1]);
			Person connectingPerson = new Person(packet.getAddress(),
					packet.getPort(), name, id);
			this.server.issuePersonConnecting(connectingPerson);
			//now send them a confirm message
			String confirm = this.buildConfirmMessage(id);
			byte[] data = confirm.getBytes();
			DatagramPacket reply = new DatagramPacket(data, data.length, 
					packet.getAddress(), packet.getPort());
			try {
				this.socket.send(reply);
			} catch (IOException e) {
				System.out.println("Failed sending confirm packet to " +
						reply.getAddress());
			}
		}
	}
	
	private String buildConfirmMessage(int id) {
		//confirm includes their id and the lounge dimensions
		StringBuilder str = new StringBuilder();
		str.append(Constants.MSG_CONFIRM);
		str.append(Constants.MSG_LINE_SEP);
		str.append(id);
		str.append(Constants.MSG_LINE_SEP);
		str.append(LoungeState.WIDTH);
		str.append(Constants.MSG_INLINE_SEP);
		str.append(LoungeState.HEIGHT);
		return str.toString();
	}
	
	private String getValidatedName(String name) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			//only allow letters, digits and space
			if ((c >= 'A' && c <= 'Z') || //upper-case
				(c >= 'a' && c <= 'z') || //lower-case
				(c >= '0' && c <= '9') || //digit
				c == ' ') { //space
				str.append(c);
			}
		}
		String ret = str.toString();
		//shorten it to 20 characters
		if (ret.length() > 20) {
			ret = ret.substring(0, 20);
		}
		//replace empty or all-space names with ???
		if (ret.length() == 0 || ret.replaceAll(" ", "").length() == 0) {
			ret = "???";
		}
		return ret;
	}
	
	private void handleKeepAlive(String message) {
		String[] parts = message.split(Constants.MSG_LINE_SEP);
		//keep alive should only have two lines
		if (parts.length == 2) {
			try {
				int id = Integer.parseInt(parts[1]);
				this.server.issueKeepAlive(id);
			} catch (NumberFormatException e) { }
		}
	}
	
	@Override
	public void run() {
		System.out.println("listening...");
		try {
			this.socket.setSoTimeout(1);
		} catch (SocketException e1) {
			System.out.println("failed setting socket timeout");
		}
		while (running) {
			//receive data
			try {
				byte[] buf = new byte[2048];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				this.socket.receive(packet);
				//process data
				this.processData(packet);
			} catch (SocketTimeoutException e) {
				//don't do shit
				//I've always said that Exceptions shouldn't be normal behavior
				// :/
			} catch (IOException e) {
				e.printStackTrace();
			}
			//send outgoing packets
			this.send();
		}
	}

}
