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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JFrame;

import kuusisto.finn.netlounge.Constants;

public class LoungeClient {
	
	public static final int WIDTH = 800;
	public static final int HEIGHT = 800;
	
	public static final int TIMEOUT = 10;
	public static final int DRAWS_PER_SEC = 30;
	public static final long NANOS_PER_DRAW = 
		1000000000 / LoungeClient.DRAWS_PER_SEC;
	public static final long MILLIS_PER_DRAW =
		1000 / LoungeClient.DRAWS_PER_SEC;
	//ticks are for user input while draws are rendering
	public static final int TICKS_PER_SEC = 20;
	public static final long NANOS_PER_TICK =
		1000000000 / LoungeClient.TICKS_PER_SEC;
	public static final long MILLIS_PER_TICK =
		1000 / LoungeClient.TICKS_PER_SEC;
	
	private InetAddress serverAddress;
	private int serverPort;
	private DatagramSocket socket;
	private LoungeClientListenThread listener;
	private ClientLoungeState state;
	
	private JFrame frame;
	private Canvas canvas;
	private BufferStrategy buffer;
	
	public LoungeClient(String host, int port) throws UnknownHostException,
			SocketException {
		//setup network crap
		this.serverAddress = InetAddress.getByName(host);
		this.serverPort = port;
		this.socket = new DatagramSocket();
		this.state = new ClientLoungeState();
		this.listener = new LoungeClientListenThread(this.state);
		//setup window crap
		Dimension dim = new Dimension(LoungeClient.WIDTH, LoungeClient.HEIGHT);
		this.frame = new JFrame("Lounge Client");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setIgnoreRepaint(true);
		this.frame.setResizable(false);
		this.canvas = new Canvas();
		this.canvas.setIgnoreRepaint(true);
		this.canvas.setPreferredSize(dim);
		this.canvas.setBackground(Color.WHITE);
		this.frame.add(canvas);
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.canvas.createBufferStrategy(2);
		this.buffer = this.canvas.getBufferStrategy();
		this.canvas.addKeyListener(Controller.getListener());
		this.frame.addKeyListener(Controller.getListener());
	}
	
	private void tick() {
		//figure out what the user is pressing
		int command = -1;
		if (Controller.isKeyDown(Controller.K_UP)) {
			command = Constants.PCMD_UP;
			//also right or left?
			if (Controller.isKeyDown(Controller.K_RIGHT)) {
				command = Constants.PCMD_UP_RIGHT;
			}
			else if (Controller.isKeyDown(Controller.K_LEFT)) {
				command = Constants.PCMD_UP_LEFT;
			}
		}
		else if (Controller.isKeyDown(Controller.K_DOWN)) {
			command = Constants.PCMD_DOWN;
			//also right or left?
			if (Controller.isKeyDown(Controller.K_RIGHT)) {
				command = Constants.PCMD_DOWN_RIGHT;
			}
			else if (Controller.isKeyDown(Controller.K_LEFT)) {
				command = Constants.PCMD_DOWN_LEFT;
			}
		}
		else if (Controller.isKeyDown(Controller.K_RIGHT)) {
			command = Constants.PCMD_RIGHT;
		}
		else if (Controller.isKeyDown(Controller.K_LEFT)) {
			command = Constants.PCMD_LEFT;
		}
		//send command
		this.sendCommand(command);
		//send keep-alive
		this.sendKeepAlive();
	}
	
	private void sendCommand(int command) {
		if (command != -1) {
			String commandMessage = this.buildCommandMessage(command);
			byte[] data = commandMessage.getBytes();
			DatagramPacket packet = new DatagramPacket(data, data.length,
					this.serverAddress, this.serverPort);
			try {
				this.socket.send(packet);
			} catch (IOException e) { }
		}
	}
	
	private void sendKeepAlive() {
		String keepAlive = this.buildKeepAliveMessage();
		byte[] data = keepAlive.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length,
				this.serverAddress, this.serverPort);
		try {
			this.socket.send(packet);
		} catch (IOException e) { }
	}
	
	private String buildKeepAliveMessage() {
		StringBuilder str = new StringBuilder();
		str.append(Constants.MSG_KEEPALIVE);
		str.append(Constants.MSG_LINE_SEP);
		str.append(this.state.getClientID());
		return str.toString();
	}
	
	private String buildCommandMessage(int command) {
		StringBuilder str = new StringBuilder();
		str.append(Constants.MSG_COMMAND);
		str.append(Constants.MSG_LINE_SEP);
		str.append(this.state.getClientID());
		str.append(Constants.MSG_INLINE_SEP);
		str.append(command);
		return str.toString();
	}
	
	private void draw() {
		synchronized (this.state) {
			Graphics g = null;
			try {
				g = this.buffer.getDrawGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, LoungeClient.WIDTH, LoungeClient.HEIGHT);
				this.state.draw(g);
			}
			finally {
				g.dispose();
				this.buffer.show();
				this.canvas.getToolkit().sync();
			}
		}
	}
	
	//TODO the loop should really be better
	public void run() {
		//first get the user's name
		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter your name: ");
		String name = scan.nextLine();
		//try to connect
		if (!this.listener.connect(this.serverAddress, this.serverPort,
				name)) {
			System.out.println("Unable to connect to host!");
			System.exit(1);
		}
		//if successful, start the listen thread
		this.listener.start();
		try { //sleep for the listener to get going
			Thread.sleep(2);
		} catch (InterruptedException e1) { }
		//show the frame
		this.frame.setVisible(true);
		//start the input/render loop
		long now = 0;
		long lastTick = 0;
		long lastDraw = 0;
		boolean running = true;
		while (running) {
			now = System.nanoTime();
			//if time to tick
			if (now - lastTick > LoungeClient.NANOS_PER_TICK) {
				lastTick = System.nanoTime();
				this.tick();
			}
			//if time to draw
			if (now - lastDraw > LoungeClient.NANOS_PER_DRAW) {
				lastDraw = System.nanoTime();
				this.draw();
			}
			//sleep for a bit
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) { }
		}
	}
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage:");
			System.out.println("LoungeClient <host> <port>");
			return;
		}
		LoungeClient client = null;
		try {
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			client = new LoungeClient(host, port);
		} catch (NumberFormatException e) {
			System.out.println("Invalid port!");
			System.exit(1);
		} catch (UnknownHostException e) {
			System.out.println("Unknown host!");
			System.exit(1);
		} catch (SocketException e) {
			System.out.println("Unable to open socket!");
			e.printStackTrace();
			System.exit(1);
		}
		client.run();
	}

}
