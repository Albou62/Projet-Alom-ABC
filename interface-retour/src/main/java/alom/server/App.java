package alom.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class App
{
	
	public static final int PORT = 9090;
	private static ConcurrentLinkedQueue<Socket> messages = new ConcurrentLinkedQueue<>();
	private static boolean running = true;
    
	private static Map<String, String> tokenToNickname = new ConcurrentHashMap<>();
	private static Map<String, Socket> connexions = new ConcurrentHashMap<>();
	
	// Map pour gérer les abonnements aux channels: channel -> Map<nickname, Boolean>
	private static Map<String, ConcurrentHashMap<String, Boolean>> channelSubscriptions = new ConcurrentHashMap<>();
	
    public static void main( String[] args )
    {
    	
    	try {
        	ServerSocket ss = new ServerSocket(App.PORT);
        	Thread t = new Thread(new ConnexionThread(ss, messages, tokenToNickname));
        	t.start();
        	System.out.println("Le serveur Interface Retour a été démarré sur le port " + App.PORT);
        	System.out.println("Tokens enregistrés : " + tokenToNickname.size());
        	
        	while (running) {
        	    Socket client = messages.poll();
				//System.out.println("Nouveau client à traiter : " + client);
        	    if (client != null) {
        	        Thread thread = new Thread(new MessageThread(client, tokenToNickname, connexions));
        	        thread.start();
        	    }
			}
        	System.out.println("Le serveur a été arrêté");	
        	}
        	catch(Exception e) {
        		e.printStackTrace();
    			App.finish();
        	}
    }
    

	public static void inscriptionToken(String token, String nickname){
		tokenToNickname.put(token,nickname);
	}
	
	/**
	 * Abonner un utilisateur à un channel
	 */
	public static void subscribeToChannel(String nickname, String channel) {
		channelSubscriptions.computeIfAbsent(channel, k -> new ConcurrentHashMap<>())
			.put(nickname, true);
		System.out.println("[App] " + nickname + " abonné au channel '" + channel + "'");
	}
	
	/**
	 * Désabonner un utilisateur d'un channel
	 */
	public static void unsubscribeFromChannel(String nickname, String channel) {
		ConcurrentHashMap<String, Boolean> subscribers = channelSubscriptions.get(channel);
		if (subscribers != null) {
			subscribers.remove(nickname);
			System.out.println("[App] " + nickname + " désabonné du channel '" + channel + "'");
		}
	}
	
	/**
	 * Envoyer un message à tous les abonnés d'un channel
	 */
	public static void sendMessageToChannel(String channel, String message) {
		ConcurrentHashMap<String, Boolean> subscribers = channelSubscriptions.get(channel);
		if (subscribers == null || subscribers.isEmpty()) {
			System.out.println("[App] Aucun abonné sur le channel '" + channel + "'");
			return;
		}
		
		System.out.println("[App] Envoi du message sur channel '" + channel + "' à " + subscribers.size() + " abonné(s)");
		
		for (String nickname : subscribers.keySet()) {
			Socket clientSocket = connexions.get(nickname);
			if (clientSocket != null && !clientSocket.isClosed()) {
				try {
					clientSocket.getOutputStream().write((message + "\n").getBytes());
					clientSocket.getOutputStream().flush();
					System.out.println("[App] Message envoyé à " + nickname);
				} catch (Exception e) {
					System.err.println("[App] Erreur envoi à " + nickname + ": " + e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Récupérer le nickname depuis un token
	 */
	public static String getNicknameFromToken(String token) {
		return tokenToNickname.get(token);
	}
    
    public static void finish() {
        running = false;
    }
}
