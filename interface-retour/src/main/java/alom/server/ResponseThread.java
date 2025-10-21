package alom.server;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;


public class ResponseThread implements Runnable {

    private Map<String, Socket> connexions;
    private boolean running = true;

    public ResponseThread(Map<String, Socket> connexions) {
        this.connexions = connexions;
    }
    
    @Override
    public void run() {
        System.out.println("[ResponseThread] Démarré - Envoi de 'coucou' à toto toutes les 10 secondes");

        while(running){
            try {
                System.out.println("On attend 10 secondes pour essayer d'envoyer un message à toto");
                Thread.sleep(10000);
                
                Socket totoSocket = connexions.get("toto");
                
                if (totoSocket != null && !totoSocket.isClosed()) {
                    PrintWriter out = new PrintWriter(totoSocket.getOutputStream(), true);
                    out.println("coucou");
                    System.out.println("[ResponseThread] Message 'coucou' envoyé à toto");
                } 
                else {
                    System.out.println("[ResponseThread] toto n'est pas connecté");
                }
                
            } 
            catch (InterruptedException e) {
                System.out.println("[ResponseThread] Thread interrompu");
                this.finish();
            }
            catch (Exception e) {
                System.err.println("[ResponseThread] Erreur: " + e.getMessage());
                e.printStackTrace();
            }
            
        }
        
        System.out.println("[ResponseThread] Arrêté");
    }

    public void finish() {
        this.running = false;
    }
    
}
