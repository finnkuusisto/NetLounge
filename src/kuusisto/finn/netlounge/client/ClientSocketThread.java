package kuusisto.finn.netlounge.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

import kuusisto.finn.netlounge.Constants;

public class ClientSocketThread extends Thread {
	
	public static final int CONFIRM_TIMEOUT = 3000;
	
	private DatagramSocket socket;
	private InetAddress serverAddress;
	private int serverPort;
	private ClientLoungeState state;
	private CrappyClientMixer mixer;
	private long lastStateReceived;
	private Queue<DatagramPacket> sendQueue;
	
	public ClientSocketThread(ClientLoungeState state) 
			throws SocketException {
		super("LoungeClientListenThread");
		this.socket = new DatagramSocket();
		this.state = state;
		this.lastStateReceived = 0;
		this.sendQueue = new LinkedList<DatagramPacket>();
	}
	
	public void setMixer(CrappyClientMixer mixer) {
		this.mixer = mixer;
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
			this.socket.setSoTimeout(ClientSocketThread.CONFIRM_TIMEOUT);
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
		//should be a state or voice message
		String[] parts = message.split(Constants.MSG_LINE_SEP);
		if (parts.length > 4 && parts[0].equals(Constants.MSG_AUDIO)) {
			//do we have a mixer?
			if (this.mixer == null) {
				return;
			}
			//second line should be id
			//third line should be audio update #
			//fourth line should be num audio bytes
			try {
				int id = Integer.parseInt(parts[1]);
				int updateNum = Integer.parseInt(parts[2]);
				int numBytes = Integer.parseInt(parts[3]);
				this.mixer.writeBytes(id, updateNum, packet.getData(),
						(packet.getLength() - numBytes), packet.getLength());
			}
			catch (NumberFormatException e) {
				return;
			}
		}
		else if (parts.length > 1 && parts[0].equals(Constants.MSG_STATE)) {
			//second line should be state update #
			try {
				long updateNum = Long.parseLong(parts[1]);
				if (updateNum <= this.lastStateReceived) {
					return;
				}
				this.lastStateReceived = updateNum;
			} catch (NumberFormatException e) {
				return;
			}
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
