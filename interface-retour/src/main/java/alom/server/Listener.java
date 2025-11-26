package alom.server;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class Listener implements ServletContextListener {

    private Thread tcpServerThread;
    private Thread kafkaConsumersThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[Listener] Application (WAR) démarrée — lancement du serveur TCP interface-retour...");

        // Démarre le serveur TCP dans un thread d'arrière-plan
        tcpServerThread = new Thread(() -> {
            try {
                App.main(new String[0]);
            } 
            catch (Throwable t) {
                System.err.println("[Listener] Erreur au lancement du serveur TCP: " + t.getMessage());
                t.printStackTrace();
            }
        }, "tcp-interface-retour");
        tcpServerThread.setDaemon(true); // ne bloque pas l'arrêt de Tomcat
        tcpServerThread.start();

        // Démarre le thread pour les consumers Kafka
        kafkaConsumersThread = new Thread(() -> {
            try {
                KafkaConsumerClass.consume();
            } 
            catch (Throwable t) {
                System.err.println("[Listener] Erreur au lancement des consumers Kafka: " + t.getMessage());
                t.printStackTrace();
            }
        }, "kafka-consumers");
        kafkaConsumersThread.setDaemon(true);
        kafkaConsumersThread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[Listener] Application (WAR) arrêtée — arrêt du serveur TCP interface-retour...");
        try {
            App.finish();
            if (tcpServerThread != null) {
                tcpServerThread.join(1000); // attend brièvement la fin
            }
        } 
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } 
        catch (Throwable t) {
            System.err.println("[Listener] Erreur à l'arrêt du serveur TCP: " + t.getMessage());
        }
    }
}
