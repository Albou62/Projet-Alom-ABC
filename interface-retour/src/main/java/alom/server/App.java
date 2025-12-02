package alom.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import alom.KafkaConsumerIndividual;


public class App
{
	
	public static final int PORT = 9090;
	private static ConcurrentLinkedQueue<Socket> messages = new ConcurrentLinkedQueue<>();
	private static boolean running = true;
    
	private static Map<String, String> tokenToNickname = new ConcurrentHashMap<>();
	private static Map<String, Socket> connexions = new ConcurrentHashMap<>();
	
	private static Map<String, ConcurrentHashMap<String, Boolean>> channelSubscriptions = new ConcurrentHashMap<>();
	
	private static Map<String, KafkaConsumerIndividual> kafkaConsumers = new ConcurrentHashMap<>();
	
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
	

	public static void subscribeToChannel(String nickname, String channel) {
		channelSubscriptions.computeIfAbsent(channel, k -> new ConcurrentHashMap<>())
			.put(nickname, true);
		System.out.println("[App] " + nickname + " abonné au channel '" + channel + "'");
	}
	

	public static void unsubscribeFromChannel(String nickname, String channel) {
		ConcurrentHashMap<String, Boolean> subscribers = channelSubscriptions.get(channel);
		if (subscribers != null) {
			subscribers.remove(nickname);
			System.out.println("[App] " + nickname + " désabonné du channel '" + channel + "'");
		}
	}
	

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
					//System.out.println("[App] Envoi du message: " + message);
					clientSocket.getOutputStream().flush();
					System.out.println("[App] Message envoyé à " + nickname);
				} catch (Exception e) {
					System.err.println("[App] Erreur envoi à " + nickname + ": " + e.getMessage());
				}
			}
		}
	}
	
	
	public static String getNicknameFromToken(String token) {
		return tokenToNickname.get(token);
	}
	
	public static void registerClientSocket(String nickname, Socket socket) {
		connexions.put(nickname, socket);
		System.out.println("[App] Socket enregistrée pour " + nickname);
	}
	
	
	public static void startKafkaConsumerForUser(String nickname) {
		Socket clientSocket = connexions.get(nickname);
		
		if (clientSocket == null) {
			System.err.println("[App] Pas de connexion socket pour " + nickname);
			return;
		}
		
		KafkaConsumerIndividual oldConsumer = kafkaConsumers.get(nickname);
		if (oldConsumer != null) {
			oldConsumer.stop();
		}
		
		KafkaConsumerIndividual consumer = new KafkaConsumerIndividual(nickname, clientSocket);
		kafkaConsumers.put(nickname, consumer);
		
		Thread consumerThread = new Thread(consumer);
		consumerThread.setDaemon(true);
		consumerThread.start();
		
		System.out.println("[App] Kafka Consumer individuel démarré pour " + nickname);
	}
    
    public static void finish() {
        running = false;
        
        for (KafkaConsumerIndividual consumer : kafkaConsumers.values()) {
            consumer.stop();
        }
    }
}
