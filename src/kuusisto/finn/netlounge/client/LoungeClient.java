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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;

import kuusisto.finn.netlounge.Constants;

public class LoungeClient {
	
	public static final int WIDTH = 800;
	public static final int HEIGHT = 800;
	
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
	//audio updates
	public static final int AUDIO_PLAY_PER_SEC = 20;
	public static final long NANOS_PER_AUDIO_PLAY =
		1000000000 / LoungeClient.AUDIO_PLAY_PER_SEC;
	public static final int AUDIO_SEND_PER_SEC = 20;
	public static final long NANOS_PER_AUDIO_SEND =
		1000000000 / LoungeClient.AUDIO_SEND_PER_SEC;
	//keep-alives keep the connection with the server
	public static final int KEEPALIVES_PER_SEC = 3;
	public static final long NANOS_PER_KEEPALIVE =
		1000000000 / LoungeClient.KEEPALIVES_PER_SEC;
	public static final long MILLIS_PER_KEEPALIVE =
		1000 / LoungeClient.KEEPALIVES_PER_SEC;
	
	private InetAddress serverAddress;
	private int serverPort;
	private ClientSocketThread socketThread;
	private ClientLoungeState state;
	private CrappyClientMixer mixer;
	private TargetDataLine micLine;
	private SourceDataLine speakerLine;
	
	private JFrame frame;
	private Canvas canvas;
	private BufferStrategy buffer;
	
	public LoungeClient(String host, int port) throws UnknownHostException,
			SocketException {
		//setup network crap
		this.serverAddress = InetAddress.getByName(host);
		this.serverPort = port;
		this.state = new ClientLoungeState();
		this.socketThread = new ClientSocketThread(this.state);
		//setup sound stuff
		this.mixer = new CrappyClientMixer();
		AudioFormat format =
			new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 
				Constants.SAMPLE_RATE, 8, 1, 1, Constants.SAMPLE_RATE, false);
		DataLine.Info inInfo = new DataLine.Info(TargetDataLine.class, 
		    format);
		DataLine.Info outInfo = new DataLine.Info(SourceDataLine.class, 
			    format);
		if (AudioSystem.isLineSupported(inInfo) &&
				AudioSystem.isLineSupported(outInfo)) {
			//obtain and open the lines.
			try {
			    micLine = (TargetDataLine)AudioSystem.getLine(inInfo);
			    micLine.open(format);
			    speakerLine = (SourceDataLine)AudioSystem.getLine(outInfo);
			    speakerLine.open(format);
			    micLine.start();
			    speakerLine.start();
			}
			catch (LineUnavailableException e) {
			    System.out.println("Unable to open audio lines!");
			    this.micLine = null;
			    this.speakerLine = null;
			    this.mixer = null;
			}
		}
		else {
		    System.out.println("Unsupported audio format!");
		    this.micLine = null;
		    this.speakerLine = null;
		    this.mixer = null;
		}
		this.socketThread.setMixer(this.mixer);
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
	}

	private byte[] playData =
		new byte[Constants.SAMPLE_RATE / LoungeClient.AUDIO_PLAY_PER_SEC];
	private void playAudio() {
		//make sure we have access to the speakers
		if (this.speakerLine == null) {
			return;
		}
		int numRead = this.mixer.readBytes(playData);
		this.speakerLine.write(playData, 0, numRead);
	}
	
	private byte[] readData =
		new byte[Constants.SAMPLE_RATE / LoungeClient.AUDIO_SEND_PER_SEC];
	private int audioMessagesSent = 0;
	private void sendAudio() {
		//make sure we have access to the mic
		if (this.micLine == null) {
			return;
		}
		int numRead = this.micLine.read(readData, 0, readData.length);
		//read any or not talking
		if (numRead <= 0 || !Controller.isKeyDown(Controller.K_SPACE)) {
			return;
		}
		//get the text part first
		byte[] text = this.buildAudioMessage(numRead).getBytes();
		//now build the full data
		byte[] data = Arrays.copyOf(text, text.length + numRead);
		for (int i = 0; i < numRead; i++) {
			data[i + text.length] = readData[i];
		}
		//build the packet
		DatagramPacket packet = new DatagramPacket(data, data.length,
				this.serverAddress, this.serverPort);
		this.socketThread.issueSend(packet);
		this.audioMessagesSent++;
	}
	
	private String buildAudioMessage(int numBytes) {
		StringBuilder str = new StringBuilder();
		str.append(Constants.MSG_AUDIO);
		str.append(Constants.MSG_LINE_SEP);
		str.append(this.state.getClientID());
		str.append(Constants.MSG_LINE_SEP);
		str.append(this.audioMessagesSent);
		str.append(Constants.MSG_LINE_SEP);
		str.append(numBytes);
		str.append(Constants.MSG_LINE_SEP);
		return str.toString();
		
	}
	
	private void sendCommand(int command) {
		if (command != -1) {
			String commandMessage = this.buildCommandMessage(command);
			byte[] data = commandMessage.getBytes();
			DatagramPacket packet = new DatagramPacket(data, data.length,
					this.serverAddress, this.serverPort);
			this.socketThread.issueSend(packet);
		}
	}
	
	private void sendKeepAlive() {
		String keepAlive = this.buildKeepAliveMessage();
		byte[] data = keepAlive.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length,
				this.serverAddress, this.serverPort);
		this.socketThread.issueSend(packet);
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
		if (!this.socketThread.connect(this.serverAddress, this.serverPort,
				name)) {
			System.out.println("Unable to connect to host!");
			System.exit(1);
		}
		//if successful, start the listen thread
		this.socketThread.start();
		try { //sleep for the listener to get going
			Thread.sleep(2);
		} catch (InterruptedException e1) { }
		//show the frame
		this.frame.setVisible(true);
		//start the input/render loop
		long now = 0;
		long lastTick = 0;
		long lastDraw = 0;
		long lastKeepAlive = 0;
		long lastAudioSend = 0;
		long lastAudioPlay = 0;
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
			//if time to keep alive
			if (now - lastKeepAlive > LoungeClient.NANOS_PER_KEEPALIVE) {
				lastKeepAlive = System.nanoTime();
				this.sendKeepAlive();
			}
			//if time to send voice
			if (now - lastAudioSend > LoungeClient.NANOS_PER_AUDIO_SEND) {
				lastAudioSend = System.nanoTime();
				this.sendAudio();
			}
			//if time to play voice
			if (now - lastAudioPlay > LoungeClient.NANOS_PER_AUDIO_PLAY) {
				lastAudioPlay = System.nanoTime();
				this.playAudio();
			}
			//sleep for a bit
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) { }
		}
		if (this.micLine != null) {
			this.micLine.stop();
		}
		if (this.speakerLine != null) {
			this.speakerLine.stop();
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
