package alom.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


public class App
{
	
	public static final int PORT = 8080;
	private static ConcurrentLinkedQueue<Socket> messages = new ConcurrentLinkedQueue<>();
	private static boolean running = true;
	
	private static Map<String, String> tokenToNickname = new HashMap<>();
	
    public static void main( String[] args )
    {
    	initializeTokens();
    	
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
        	        Thread thread = new Thread(new MessageThread(client, tokenToNickname));
        	        thread.start();
        	    }
        	    
        	    Thread.sleep(100);
			}
        	System.out.println("Le serveur a été arrêté");	
        	}
        	catch(Exception e) {
        		e.printStackTrace();
    			App.finish();
        	}
    }
    

    private static void initializeTokens() {
    	tokenToNickname.put("abc123", "toto");
    	tokenToNickname.put("def456", "titi");
    	tokenToNickname.put("ghi789", "tata");
    	tokenToNickname.put("jkl123", "tutu");
    }
    
    public static void finish() {
        running = false;
    }
}
