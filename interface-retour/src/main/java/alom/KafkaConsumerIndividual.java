package alom;

import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * Consumer Kafka pour les messages individuels.
 * Consomme les messages du topic "user-{nickname}" et les envoie via la socket TCP.
 */
public class KafkaConsumerIndividual implements Runnable {
    private final String nickname;
    private final Socket clientSocket;
    private KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    public KafkaConsumerIndividual(String nickname, Socket clientSocket) {
        this.nickname = nickname;
        this.clientSocket = clientSocket;
        this.consumer = createConsumer();
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "individual-consumer-" + nickname);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        String topic = "user-" + nickname;
        consumer.subscribe(Collections.singletonList(topic));
        
        System.out.println("[KafkaConsumerIndividual] Abonné au topic: " + topic);
        return consumer;
    }

    @Override
    public void run() {
        System.out.println("[KafkaConsumerIndividual] Démarrage du consumer pour " + nickname);
        
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    System.out.println("[KafkaConsumerIndividual] Message reçu pour " + nickname + ": " + message);
                    
                    // Envoyer le message via la socket TCP
                    sendMessageToClient(message);
                }
            }
        } catch (Exception e) {
            System.err.println("[KafkaConsumerIndividual] Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    private void sendMessageToClient(String message) {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
                System.out.println("[KafkaConsumerIndividual] Message envoyé à " + nickname + ": " + message);
            } else {
                System.err.println("[KafkaConsumerIndividual] Socket fermée pour " + nickname);
                stop();
            }
        } catch (Exception e) {
            System.err.println("[KafkaConsumerIndividual] Erreur lors de l'envoi: " + e.getMessage());
            stop();
        }
    }

    public void stop() {
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
    }
}
