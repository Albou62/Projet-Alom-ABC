package alom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;


public class MessageThread implements Runnable {
	
	private Socket client;
	private Map<String, String> tokenToNickname;
	private boolean running = true;
	BufferedReader in = null;
	PrintWriter out = null;
	String nickname = null;
	
	
	public MessageThread(Socket client, Map<String, String> tokenToNickname) {
		this.client = client;
		this.tokenToNickname = tokenToNickname;
	}

	@Override
	public void run() {
		
		while (running){
			try {
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new PrintWriter(client.getOutputStream(), true);

				String token = in.readLine();
				System.out.println("Le client: " + client.getRemoteSocketAddress() + " a envoyé le token: " + token);
				
				nickname = tokenToNickname.get(token);

				while (nickname == null) {
					System.out.println("Token invalide: " + token);
					out.println("Erreur: Token invalide");
					token = in.readLine();
					nickname = tokenToNickname.get(token);
				}
				
				System.out.println("Bienvenue " + nickname + " !");
				out.println("Bienvenue " + nickname + " !");
				
				String message;

				while ((message = in.readLine()) != null) {					
					System.out.println(nickname + " a envoyé le message: " + message);			
				}

			}

			catch(IOException e) {
				System.out.println("Une exception est survenue: " + e.getMessage());
				this.finish();
			} 
			
		}	
	}

	public void finish() {
			this.running = false;
			try {
				out.close();
				in.close();
				client.close();
				System.out.println("Connexion fermée pour un client.");
			} 

			catch (IOException e) {
				e.printStackTrace();
			}
		}
}
