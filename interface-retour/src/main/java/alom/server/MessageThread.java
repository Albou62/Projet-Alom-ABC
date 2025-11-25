package alom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class MessageThread implements Runnable {
	
	private Socket client;
	private Map<String, String> tokenToNickname;
	private Map<String, Socket> connexions;
	private boolean running = true;
	BufferedReader in = null;
	PrintWriter out = null;
	String nickname = null;
	
	
	
	public MessageThread(Socket client, Map<String, String> tokenToNickname, Map<String, Socket> connexions) {
		this.client = client;
		this.tokenToNickname = tokenToNickname;
		this.connexions = connexions;
	}

	@Override
	public void run() {

			try {
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new PrintWriter(client.getOutputStream(), true);

				String token = in.readLine();
				System.out.println("Le client: " + client.getRemoteSocketAddress() + " a envoyé le token: " + token);
				
				nickname = tokenToNickname.get(token);

				while (nickname == null && running) {
					System.out.println("Client: " +  client.getRemoteSocketAddress());
					System.out.println("Token invalide: " + token);
					out.println("Erreur: Token invalide");
					token = in.readLine();
					nickname = tokenToNickname.get(token);
				}

				this.connexions.put(nickname,client);
				System.out.println("Connexions: " + this.connexions);
				System.out.println("Bienvenue " + nickname + " !");
				out.println("Bienvenue " + nickname + " !");

				
				while(running) {
					try {
						

					} 
					catch (Exception e) {
						e.printStackTrace();
						System.out.println("Une exception est survenue lors de l'envoi du message.");
						this.finish();
					}
			}	
			}		

			catch(IOException e) {
				System.out.println("Une exception est survenue: " + e.getMessage());
				this.finish();
			} 
			
		
		System.out.println("Fermeture de la connexion pour " + nickname);
		this.finish();
	}

	public void finish() {
			this.running = false;
			try {
				connexions.remove(nickname);
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
