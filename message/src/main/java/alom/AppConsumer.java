package alom;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Consumer Kafka simple qui consomme des messages du topic "topic"
 */
public class AppConsumer {
    public static void main(String[] args) {
        System.out.println("début de la méthode main pour le consumer Kafka");
        
        Properties configuration = new Properties();
        configuration.put(ConsumerConfig.GROUP_ID_CONFIG,"Groupe");
        configuration.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configuration.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        configuration.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    
        System.out.println("Configuration effectuée");
        
        KafkaConsumer<Long,String> consumer = new KafkaConsumer<>(configuration);
        
        System.out.println("Consommateur instancié");
       
        System.out.println("méthode 1 pour consommation de message");
        
        TopicPartition t = new TopicPartition("topic",0);
        consumer.assign(List.of(t));
        Duration duration = Duration.ofMillis(5000);
        ConsumerRecords<Long, String> records1 =consumer.poll(duration);
        records1.forEach(r->{
        	System.out.println(" value = "+r.value());
        });
        
        System.out.println("méthode 2 pour consommation de message");
        
        Map<String,List<PartitionInfo>> topics = consumer.listTopics();
        List<PartitionInfo> partitions = topics.get("topic");
        
        /*
        System.out.println("partitions : ");
        for(PartitionInfo pi:partitions) {
        	System.out.println(" -- "+pi);
        }*/
        TopicPartition topicPartition = new TopicPartition(partitions.get(0).topic(), partitions.get(0).partition());

        consumer.assign(List.of(topicPartition));
        consumer.seek(topicPartition, 0);
                
        ConsumerRecords<Long, String> records2 = consumer.poll(duration);

        records2.forEach(r->{
        	System.out.println("offset = "+r.offset()+" , key = "+r.key()+" , value = "+r.value());
        });
        
        consumer.close(); 
        
        System.out.println("fin de la méthode main pour le consumer Kafka");
        
    }
}
