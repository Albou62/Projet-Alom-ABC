package alom.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread qui gère l'acceptation des connexions TCP entrantes
 */
public class ConnexionThread implements Runnable {

	private boolean running = true;
	private ServerSocket serverSocket;
	private ConcurrentLinkedQueue<Socket> requestParams;
	private Map<String, String> tokenToNickname;
	
	public ConnexionThread(ServerSocket ss, ConcurrentLinkedQueue<Socket> requestParams, Map<String, String> tokenToNickname) {
		this.serverSocket = ss;
		this.requestParams = requestParams;
		this.tokenToNickname = tokenToNickname;
	}
	

	@Override
	public void run() {
		while(this.running) {
			try {
				Socket client = this.serverSocket.accept();
				this.requestParams.add(client);
				System.out.println("Nouvelle connexion acceptée : " + client.getRemoteSocketAddress());
				System.out.println(tokenToNickname);
				
			} catch (Exception e) {
				if (this.running) {
					e.printStackTrace();
					this.finish();
				}
			}
		}
	}


	
	public void finish() {
		this.running = false;
		try {
			this.serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
