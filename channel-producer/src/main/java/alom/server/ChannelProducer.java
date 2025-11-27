package alom.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("send")
public class ChannelProducer {

	private static KafkaProducer<String, String> producer;
	
	private static Map<String, Set<String>> channelSubscriptions = new ConcurrentHashMap<>();
	
	private static final Pattern CHANNEL_PATTERN = Pattern.compile("^\\[([^\\]]+)\\]\\s*\\(([^)]+)\\)\\s*(.+)$");
	
	static {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, 3);
		
		producer = new KafkaProducer<>(props);
		
		System.out.println("[ChannelProducer] Kafka Producer initialisé");
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendChannelMessage(String jsonData) {
		try {
			String message = extractJsonField(jsonData, "message");
			String token = extractJsonField(jsonData, "token");
			
			if (message == null) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Le champ 'message' est requis\"}\n")
							  .build();
			}
			
			Matcher matcher = CHANNEL_PATTERN.matcher(message.trim());
			
			if (!matcher.matches()) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Format invalide. Utilisez: [channel] (user) message\"}\n")
							  .build();
			}
			
			String channel = matcher.group(1).trim();
			String expediteur = matcher.group(2).trim();
			String contenu = matcher.group(3).trim();
			
			Set<String> subscribers = channelSubscriptions.get(channel);
			
			if (subscribers == null || subscribers.isEmpty()) {
				System.out.println("[ChannelProducer] Aucun abonné sur le channel '" + channel + "'");
				return Response.status(200)
							  .entity(String.format(
								  "{\"success\": true, \"message\": \"Aucun abonné sur le channel '%s'\", \"recipients\": 0}",
								  channel
							  ))
							  .build();
			}
			
			String kafkaMessage = String.format("[%s] (%s) %s", channel, expediteur, contenu);
			
			int sentCount = 0;
			for (String subscriber : subscribers) {
				String topic = "user-" + subscriber;
				
				ProducerRecord<String, String> record = new ProducerRecord<>(topic, kafkaMessage);
				producer.send(record, (metadata, exception) -> {
					if (exception != null) {
						System.err.println("[ChannelProducer] Erreur envoi à " + subscriber + ": " + exception.getMessage());
					} else {
						System.out.println(String.format(
							"[ChannelProducer] Message envoyé: subscriber=%s, topic=%s, partition=%d, offset=%d",
							subscriber, metadata.topic(), metadata.partition(), metadata.offset()
						));
					}
				});
				
				sentCount++;
			}
			
			producer.flush();
			
			System.out.println(String.format(
				"[ChannelProducer] Message channel [%s] de %s envoyé à %d abonné(s): '%s'",
				channel, expediteur, sentCount, contenu
			));
			
			return Response.status(200)
						  .entity(String.format(
							  "{\"success\": true, \"message\": \"Message envoyé sur le channel '%s'\", \"recipients\": %d}",
							  channel, sentCount
						  ))
						  .build();
			
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
						  .entity("{\"erreur\": \"Erreur lors de l'envoi: " + e.getMessage() + "\"}\n")
						  .build();
		}
	}
	
	
	@POST
	@Path("subscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response subscribe(String jsonData) {
		try {
			String channel = extractJsonField(jsonData, "channel");
			String nickname = extractJsonField(jsonData, "nickname");
			
			if (channel == null || nickname == null) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Les champs 'channel' et 'nickname' sont requis\"}\n")
							  .build();
			}
			
			channelSubscriptions.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet())
				.add(nickname);
			
			System.out.println("[ChannelProducer] " + nickname + " abonné au channel '" + channel + "'");
			
			return Response.status(200)
						  .entity(String.format(
							  "{\"success\": true, \"message\": \"%s abonné au channel '%s'\"}\n",
							  nickname, channel
						  ))
						  .build();
			
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
						  .entity("{\"erreur\": \"" + e.getMessage() + "\"}\n")
						  .build();
		}
	}
	
	
	@POST
	@Path("unsubscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response unsubscribe(String jsonData) {
		try {
			String channel = extractJsonField(jsonData, "channel");
			String nickname = extractJsonField(jsonData, "nickname");
			
			if (channel == null || nickname == null) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Les champs 'channel' et 'nickname' sont requis\"}\n")
							  .build();
			}
			
			Set<String> subscribers = channelSubscriptions.get(channel);
			if (subscribers != null) {
				subscribers.remove(nickname);
			}
			
			System.out.println("[ChannelProducer] " + nickname + " désabonné du channel '" + channel + "'");
			
			return Response.status(200)
						  .entity(String.format(
							  "{\"success\": true, \"message\": \"%s désabonné du channel '%s'\"}\n",
							  nickname, channel
						  ))
						  .build();
			
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
						  .entity("{\"erreur\": \"" + e.getMessage() + "\"}\n")
						  .build();
		}
	}
	
	private String extractJsonField(String json, String fieldName) {
		try {
			String searchKey = "\"" + fieldName + "\"";
			int keyIndex = json.indexOf(searchKey);
			if (keyIndex == -1) return null;
			
			int colonIndex = json.indexOf(":", keyIndex);
			if (colonIndex == -1) return null;
			
			int startQuote = json.indexOf("\"", colonIndex);
			if (startQuote == -1) return null;
			
			int endQuote = json.indexOf("\"", startQuote + 1);
			if (endQuote == -1) return null;
			
			return json.substring(startQuote + 1, endQuote);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Set<String> getSubscribers(String channel) {
		return channelSubscriptions.getOrDefault(channel, new HashSet<>());
	}
}
