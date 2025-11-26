package alom.server;

import java.net.Socket;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;


public class KafkaConsumerClass {
    
    private final String nickname;
    private final Socket clientSocket;
    private volatile boolean running = true;
    
    public KafkaConsumerClass(String nickname, Socket clientSocket) {
        this.nickname = nickname;
        this.clientSocket = clientSocket;
    }
    
    public static void consume() {
        String topic = "user-" + nickname;
        
        System.out.println("[KafkaConsumer] Démarrage consumer pour " + nickname + " sur topic: " + topic);
        
        Properties configuration = new Properties();
        configuration.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-" + nickname);
        configuration.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configuration.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configuration.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configuration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configuration.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configuration);
        consumer.subscribe(Collections.singletonList(topic));
        
        Duration duration = Duration.ofMillis(1000);
        
        System.out.println("[KafkaConsumer] " + nickname + " écoute le topic '" + topic + "'");
        
        try {
            while (running && !clientSocket.isClosed()) {
                ConsumerRecords<String, String> records = consumer.poll(duration);
                
                records.forEach(record -> {
                    String message = record.value();
                    System.out.println("[KafkaConsumer] Message reçu pour " + nickname + ": " + message);
                    
                    try {
                        if (clientSocket != null && !clientSocket.isClosed()) {
                            clientSocket.getOutputStream().write((message + "\n").getBytes());
                            clientSocket.getOutputStream().flush();
                            System.out.println("[KafkaConsumer] Message envoyé à " + nickname + " via TCP");
                        }
                    } catch (Exception e) {
                        System.err.println("[KafkaConsumer] Erreur envoi TCP à " + nickname + ": " + e.getMessage());
                        running = false;
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[KafkaConsumer] Erreur consumer " + nickname + ": " + e.getMessage());
        } finally {
            consumer.close();
            System.out.println("[KafkaConsumer] Consumer fermé pour " + nickname);
        }
    }
    
    public void stop() {
        running = false;
    }
}
