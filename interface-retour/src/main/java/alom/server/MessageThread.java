package alom.server;

import java.net.Socket;


public class MessageThread implements Runnable {
	
	private Socket client;
	
	
	public MessageThread(Socket client) {
		this.client = client;
	}

	@Override
	public void run() {
		try {
			Message m = new Message(client);
			client.close();
			System.out.println("Connexion fermée");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
