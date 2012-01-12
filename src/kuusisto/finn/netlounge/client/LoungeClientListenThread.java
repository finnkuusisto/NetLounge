package kuusisto.finn.netlounge.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class LoungeClientListenThread extends Thread {
	
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
	
	public boolean connect(InetAddress host, int port) {
		this.serverAddress = host;
		this.serverPort = port;
		//TODO
		return false;
	}
	
	public void run() {
		System.out.println("listening...");
		//TODO
	}

}
