package alom.server;

import java.util.Properties;
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
public class MessageProducer {

	private static KafkaProducer<String, String> producer;
	
	private static final Pattern MESSAGE_PATTERN = Pattern.compile("^\\(([^)]+)\\)\\s*\\(([^)]+)\\)\\s*(.+)$");
	
	static {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, 3);
		
		producer = new KafkaProducer<>(props);
		
		System.out.println("[MessageProducer] Kafka Producer initialisé");
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendDirectMessage(String jsonData) {
		try {

			String message = extractJsonField(jsonData, "message");
			String token = extractJsonField(jsonData, "token");
			
			if (message == null) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Le champ 'message' est requis\"}\n")
							  .build();
			}
			
			Matcher matcher = MESSAGE_PATTERN.matcher(message.trim());
			
			if (!matcher.matches()) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Format invalide. Utilisez: (destinataire) (expediteur) message\"}\n")
							  .build();
			}
			
			String destinataire = matcher.group(1).trim();
			String expediteur = matcher.group(2).trim();
			String contenu = matcher.group(3).trim();
			
			String topic = "user-" + destinataire;
			
			String kafkaMessage = String.format("(Message de %s) %s", expediteur, contenu);
			
			ProducerRecord<String, String> record = new ProducerRecord<>(topic, kafkaMessage);
			producer.send(record, (metadata, exception) -> {
				if (exception != null) {
					System.err.println("[MessageProducer] Erreur envoi: " + exception.getMessage());
				} else {
					System.out.println(String.format(
						"[MessageProducer] Message envoyé: topic=%s, partition=%d, offset=%d",
						metadata.topic(), metadata.partition(), metadata.offset()
					));
				}
			});
			
			producer.flush();
			
			System.out.println(String.format(
				"[MessageProducer] Message direct: %s -> %s: '%s'",
				expediteur, destinataire, contenu
			));
			
			return Response.status(200)
						  .entity(String.format(
							  "{\"success\": true, \"message\": \"Message envoyé à %s\", \"topic\": \"%s\"}\n",
							  destinataire, topic
						  ))
						  .build();
			
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
						  .entity("{\"erreur\": \"Erreur lors de l'envoi: " + e.getMessage() + "\"}\n")
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
}
