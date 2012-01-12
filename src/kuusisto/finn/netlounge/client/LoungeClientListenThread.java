package kuusisto.finn.netlounge.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import kuusisto.finn.netlounge.Constants;

public class LoungeClientListenThread extends Thread {
	
	public static final int CONFIRM_TIMEOUT = 3000;
	
	private DatagramSocket socket;
	private InetAddress serverAddress;
	private int serverPort;
	private ClientLoungeState state;
	
	public LoungeClientListenThread(ClientLoungeState state) 
			throws SocketException {
		super("LoungeClientListenThread");
		this.socket = new DatagramSocket();
		this.state = state;
	}
	
	public boolean connect(InetAddress host, int port, String name) {
		this.serverAddress = host;
		this.serverPort = port;
		//send connect message
		String connect = this.buildConnectMessage(name);
		byte[] data = connect.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length,
				this.serverAddress, this.serverPort);
		try {
			this.socket.send(packet);
		} catch (IOException e) {
			return false;
		}
		//listen for confirm
		data = new byte[1024];
		packet = new DatagramPacket(data, data.length);
		try {
			this.socket.setSoTimeout(LoungeClientListenThread.CONFIRM_TIMEOUT);
			this.socket.receive(packet);
			this.socket.setSoTimeout(0);
		} catch (IOException e) {
			return false;
		}
		//check the packet
		if (!this.handleConfirm(packet)) {
			return false;
		}
		System.out.println("received confirm from " + packet.getAddress());
		//send a keep alive
		String keepAlive = this.buildKeepAliveMessage();
		data = keepAlive.getBytes();
		packet = new DatagramPacket(data, data.length,
				this.serverAddress, this.serverPort);
		try {
			this.socket.send(packet);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	private boolean handleConfirm(DatagramPacket packet) {
		String message = new String(packet.getData(), 0, packet.getLength());
		String[] parts = message.split(Constants.MSG_LINE_SEP);
		//should be 3 lines
		if (parts.length != 3 || !parts[0].equals(Constants.MSG_CONFIRM)) {
			return false;
		}
		try {
			//second line is id
			int id = Integer.parseInt(parts[1]);
			this.state.setClientID(id);
			//third line is dimensions
			String[] dims = parts[2].split(Constants.MSG_INLINE_SEP);
			if (dims.length != 2) {
				return false;
			}
			double width = Double.parseDouble(dims[0]);
			double height = Double.parseDouble(dims[1]);
			this.state.setWidth(width);
			this.state.setHeight(height);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private String buildConnectMessage(String name) {
		StringBuilder str = new StringBuilder();
		str.append(Constants.MSG_CONNECT);
		str.append(Constants.MSG_LINE_SEP);
		str.append(name);
		return str.toString();
	}
	
	private String buildKeepAliveMessage() {
		StringBuilder str = new StringBuilder();
		str.append(Constants.MSG_KEEPALIVE);
		str.append(Constants.MSG_LINE_SEP);
		str.append(this.state.getClientID());
		return str.toString();
	}
	
	private void processData(DatagramPacket packet) {
		String message = new String(packet.getData(), 0, packet.getLength());
		//should be a state message
		String[] parts = message.split(Constants.MSG_LINE_SEP);
		if (parts.length > 1 && parts[0].equals(Constants.MSG_STATE)) {
			//all remaining lines are player positions
			synchronized (this.state) {
				this.state.clearPersons();
				for (int i = 1; i < parts.length; i++) {
					String[] player = parts[i].split(Constants.MSG_INLINE_SEP);
					//should be 3 parts
					if (player.length == 3) {
						try {
							String name = player[0];
							double x = Double.parseDouble(player[1]);
							double y = Double.parseDouble(player[2]);
							this.state.addPerson(new ClientPerson(name, x, y));
						} catch (NumberFormatException e) { }
					}
				}
			}
		}
	}
	
	public void run() {
		System.out.println("listening...");
		boolean running = true;
		while (running) {
			try {
				byte[] buf = new byte[2048];
				//receive data
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				this.socket.receive(packet);
				//process data
				this.processData(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
