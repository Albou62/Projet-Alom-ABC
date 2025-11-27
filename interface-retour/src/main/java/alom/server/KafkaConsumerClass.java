package alom.server;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;


public class KafkaConsumerClass {
    
    public static void main(String[] args) {
        consume();
    }
    
    public static void consume() {
        System.out.println("Démarrage du Consumer de channels");
        
        Properties configuration = new Properties();
        configuration.put(ConsumerConfig.GROUP_ID_CONFIG, "ChannelConsumerGroup");
        configuration.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configuration.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configuration.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configuration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        
        System.out.println("Configuration effectuée");
        
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configuration);
        
        System.out.println("Consommateur instancié");
        
        consumer.subscribe(java.util.Arrays.asList("channels"));
        Duration duration = Duration.ofMillis(1000);
        
        System.out.println("En attente de messages sur le topic 'channels'...");
        
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(duration);
                
                records.forEach(record -> {
                    String channelName = record.key();
                    String message = record.value();
                    System.out.println("Message reçu - Channel: " + channelName + ", Message: " + message);
                    
                    try {
                        App.sendMessageToChannel(channelName, message);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'envoi du message au channel: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[KafkaConsumer] Erreur consumer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            consumer.close();
            System.out.println("[KafkaConsumer] Consumer fermé");
        }
    }
}
