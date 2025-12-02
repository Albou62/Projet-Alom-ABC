package alom;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class App {
    
    private static final String TOPIC = "channels";
    private static KafkaProducer<String, String> producer;
    
    static {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }
    
    public static void processAndSendMessage(String fullMessage) {
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]\\s*(.+)");
        Matcher matcher = pattern.matcher(fullMessage);
        
        if (matcher.find()) {
            String channel = matcher.group(1);  
            String content = matcher.group(2);  
            
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, channel, content);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Erreur lors de l'envoi: " + exception.getMessage());
                } 
                else {
                    System.out.println("[Producer] Message envoyé sur channel '" + channel + "': " + content);
                }
            });
        } 
        else {
            System.err.println("Format de message invalide: " + fullMessage);
            System.err.println("Format attendu: [channel] (user) message");
        }
    }
    
    public static void main(String[] args) {
        if (args.length > 0) {
            String message = String.join(" ", args);
            processAndSendMessage(message);
        } else {
            System.out.println("Usage: java alom.App \"[channel] (user) message\"");
        }
    }
}