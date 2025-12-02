package alom.server;

import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;


public class KafkaConsumerClass implements Runnable {
    private org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;
    
    private static final Pattern USER_TOPIC_PATTERN = Pattern.compile("^user-(.+)$");
    
    private final java.util.concurrent.ConcurrentLinkedQueue<String> topicsToAdd = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public KafkaConsumerClass() {
        this.consumer = createConsumer();
    }

    private org.apache.kafka.clients.consumer.KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "unified-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props);
        
        List<String> topics = new ArrayList<>();
        topics.add("channels");
        consumer.subscribe(topics);
        
        System.out.println("[KafkaConsumer] Abonné aux topics: " + topics);
        return consumer;
    }

    @Override
    public void run() {
        System.out.println("[KafkaConsumer] Démarrage du consumer");
        
        try {
            while (running) {
                checkAndAddNewTopics();
                
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    String topic = record.topic();
                    String key = record.key();
                    String message = record.value();
                    
                    System.out.println("[KafkaConsumer] Message reçu - Topic: " + topic + ", Key: " + key + ", Message: " + message);
                    
                    if (topic.equals("channels")) {
                        handleChannelMessage(key, message);
                    } 
                    else if (topic.startsWith("user-")) {
                        Matcher matcher = USER_TOPIC_PATTERN.matcher(topic);
                        if (matcher.matches()) {
                            String nickname = matcher.group(1);
                            handleIndividualMessage(nickname, message);
                        }
                    }
                }
            }
        } 
        catch (Exception e) {
            if (running) {
                System.err.println("[KafkaConsumer] Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        } 
        finally {
            if (consumer != null) {
                consumer.close();
                System.out.println("[KafkaConsumer] Consumer fermé");
            }
        }
    }

    private void handleChannelMessage(String channelName, String message) {
        try {
            String formattedMessage = "[" + channelName + "] " + message;
            System.out.println("[KafkaConsumer] Message de channel: " + formattedMessage);
            
            App.sendMessageToChannel(channelName, formattedMessage);
        } 
        catch (Exception e) {
            System.err.println("[KafkaConsumer] Erreur lors de l'envoi du message au channel: " + e.getMessage());
        }
    }

    private void handleIndividualMessage(String nickname, String message) {
        try {
            System.out.println("[KafkaConsumer] Message individuel pour " + nickname + ": " + message);
            
            Socket clientSocket = App.getClientSocket(nickname);
            
            if (clientSocket != null && !clientSocket.isClosed()) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
                System.out.println("[KafkaConsumer] Message envoyé à " + nickname);
            } 
            else {
                System.err.println("[KafkaConsumer] Socket fermée ou inexistante pour " + nickname);
            }
        } 
        catch (Exception e) {
            System.err.println("[KafkaConsumer] Erreur lors de l'envoi du message individuel: " + e.getMessage());
        }
    }

    public void subscribeToUserTopic(String nickname) {
        String userTopic = "user-" + nickname;
        topicsToAdd.offer(userTopic);
        System.out.println("[KafkaConsumer] Demande d'abonnement au topic: " + userTopic);
    }
    

    private void checkAndAddNewTopics() {
        if (!topicsToAdd.isEmpty()) {
            List<String> currentTopics = new ArrayList<>(consumer.subscription());
            boolean hasChanges = false;
            
            String topic;
            while ((topic = topicsToAdd.poll()) != null) {
                if (!currentTopics.contains(topic)) {
                    currentTopics.add(topic);
                    hasChanges = true;
                    System.out.println("[KafkaConsumer] Ajout du topic: " + topic);
                }
            }
            
            if (hasChanges) {
                consumer.subscribe(currentTopics);
                System.out.println("[KafkaConsumer] Topics actifs: " + currentTopics);
            }
        }
    }

    public void stop() {
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
    }
}
