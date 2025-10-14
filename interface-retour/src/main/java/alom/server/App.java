package alom.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Hello world!
 *
 */
public class App
{
	
	public static final int PORT = 8080;
	private static ConcurrentLinkedQueue<Socket> messages = new ConcurrentLinkedQueue<>();
	private static boolean running = true;
	
    public static void main( String[] args )
    {
    	
    	try {
        	ServerSocket ss = new ServerSocket(App.PORT);
        	Thread t = new Thread(new ConnexionThread(ss,messages));
        	t.start();
        	System.out.println("Le serveur a été démarré");
        	while (running) {
        	    Socket client = messages.poll();
        	    if (client != null) {
        	        Thread thread = new Thread(new MessageThread(client));
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
    
    public static void finish() {
        running = false;
    }
}
