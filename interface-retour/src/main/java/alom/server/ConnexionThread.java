package alom.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnexionThread implements Runnable {

	private boolean running =true;
	private ServerSocket serverSocket;
	private ConcurrentLinkedQueue<Socket> requestParams;
	
	public ConnexionThread(ServerSocket ss, ConcurrentLinkedQueue<Socket> requestParams) {
		this.serverSocket=ss;
		this.requestParams=	requestParams;
	}
	

	@Override
	public void run() {
		while(this.running) {
			try {
				Socket client = this.serverSocket.accept();
				this.requestParams.add(client);
				System.out.println("Le client "+client+" viens de se connecter");
			} catch (Exception e) {
				e.printStackTrace();
				this.finish();
			}
		}
	}
	
	public void finish() {
		this.running=false;
		try {
			this.serverSocket.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
