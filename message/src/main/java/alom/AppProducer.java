package alom;

import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Producer Kafka simple qui envoie un message "message" sur le topic "topic"
 *
 */
public class AppProducer
{
    public static void main( String[] args )
    {
        System.out.println("début de la méthode main pour le producer Kafka");
        
        Properties configuration = new Properties();
        configuration.put(ProducerConfig.CLIENT_ID_CONFIG,InetAddress.getLocalHost().getHostName());
        configuration.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configuration.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        configuration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configuration.put(ProducerConfig.ACKS_CONFIG, "all");
        
        System.out.println("Configuration effectuée");
        KafkaProducer<Long,String> producer = new KafkaProducer<>(configuration);
        
        System.out.println("Producteur instancié");
        
        final ProducerRecord<Long, String> record = new ProducerRecord<>("topic", System.currentTimeMillis(), "message");
        
        Future<RecordMetadata> future = producer.send(record);
        
        RecordMetadata metadata = future.get();
        
        producer.close(); 
        
        System.out.println("fin de la méthode main pour le producer Kafka");
        
    }
}
