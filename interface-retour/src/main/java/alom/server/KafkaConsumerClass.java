package alom.server;

import java.util.Properties;

import java.time.Duration;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;


public class KafkaConsumerClass {
    
    public static void consume() {
        System.out.println("début de la méthode main - Consumer de channels");
        
        Properties configuration = new Properties();
        configuration.put(ConsumerConfig.GROUP_ID_CONFIG,"ChannelConsumerGroup");
        configuration.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configuration.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configuration.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configuration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    
        System.out.println("Configuration effectuée");
        
        KafkaConsumer<String,String> consumer = new KafkaConsumer<>(configuration);
        
        System.out.println("Consommateur instancié");
       
        consumer.subscribe(List.of("channels"));
        Duration duration = Duration.ofMillis(1000);
        
        System.out.println("En attente de messages sur le topic 'channels'...");
        
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
    }
}
