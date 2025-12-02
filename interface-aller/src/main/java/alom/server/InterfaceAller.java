package alom.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("routeur")
public class InterfaceAller {

	private static final String AUTHENTIFICATION_SERVICE = "http://127.0.0.1:8080/authentification/webapi";
	private static final String INTERFACE_RETOUR_SERVICE = "http://127.0.0.1:8080/interface-retour/webapi";
	
	@POST
	@Path("connexion")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response connexion(String jsonData) {
		try {
			Client client = ClientBuilder.newClient();
			Response response = client.target(AUTHENTIFICATION_SERVICE + "/authentification/connexion")
									  .request(MediaType.APPLICATION_JSON)
									  .post(Entity.json(jsonData));
			
			String result = response.readEntity(String.class);
			int status = response.getStatus();
			response.close();
			client.close();
			
			return Response.status(status).entity(result).build();	
		} 
        
        catch (Exception e) {
			return Response.status(500)
						  .entity("{\"erreur\": \"Service authentification indisponible\"}\n")
						  .build();
		}
	}

	@POST
	@Path("inscription")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response inscription(String jsonData) {
		try {
			Client client = ClientBuilder.newClient();
			Response response = client.target(AUTHENTIFICATION_SERVICE + "/authentification/inscription")
									  .request(MediaType.APPLICATION_JSON)
									  .post(Entity.json(jsonData));
			
			String result = response.readEntity(String.class);
			int status = response.getStatus();
			response.close();
			client.close();
			
			return Response.status(status).entity(result).build();
		} 

        catch (Exception e) {
			return Response.status(500)
						  .entity("{\"erreur\": \"Service authentification indisponible\"}\n")
						  .build();
		}
	}

	@POST
	@Path("channel")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response channel(String jsonData) {
		try {
			String message = extractJsonField(jsonData, "message");
			String token = extractJsonField(jsonData, "token");
			
			if (message == null || token == null) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Les champs 'message' et 'token' sont requis\"}\n")
							  .build();
			}
			
			alom.App.processAndSendMessage(message);
			
			System.out.println("[InterfaceAller] Message traité et envoyé sur Kafka: " + message);
			
			return Response.status(200)
						  .entity("{\"message\": \"Message envoyé\"}\n")
						  .build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
						  .entity("{\"erreur\": \"Erreur lors de l'envoi du message: " + e.getMessage() + "\"}\n")
						  .build();
		}
	}
	
	@POST
	@Path("message")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response message(String jsonData) {
		try {
			String message = extractJsonField(jsonData, "message");
			String token = extractJsonField(jsonData, "token");
			String destinataire = extractJsonField(jsonData, "destinataire");
			
			if (message == null || token == null) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Les champs 'message' et 'token' sont requis\"}\n")
							  .build();
			}
			
			// Si destinataire est fourni, c'est un message direct
			if (destinataire != null && !destinataire.isEmpty()) {
				// Récupérer le nickname de l'expéditeur depuis le token
				String expediteur = getNicknameFromToken(token);
				if (expediteur == null) {
					return Response.status(401)
								  .entity("{\"erreur\": \"Token invalide\"}\n")
								  .build();
				}
				
				// Envoyer le message via le service message
				String targetUrl = "http://127.0.0.1:8080/message/webapi/send";
				String formattedMessage = "(" + destinataire + ") (" + expediteur + ") " + message;
				String newJsonData = "{\"message\":\"" + formattedMessage + "\",\"token\":\"" + token + "\"}";
				
				Client client = ClientBuilder.newClient();
				Response response = client.target(targetUrl)
										  .request(MediaType.APPLICATION_JSON)
										  .post(Entity.json(newJsonData));
				
				String result = response.readEntity(String.class);
				int status = response.getStatus();
				response.close();
				client.close();
				
				return Response.status(status).entity(result).build();
			}
			
			// Sinon, vérifier si c'est un message channel avec [channel]
			if (message.trim().startsWith("[")) {
				alom.App.processAndSendMessage(message);
				return Response.status(200)
							  .entity("{\"message\": \"Message envoyé sur channel\"}\n")
							  .build();
			}
			
			// Format non reconnu
			return Response.status(400)
						  .entity("{\"erreur\": \"Format de message invalide. Utilisez 'destinataire' pour message direct ou '[channel]' pour channel\"}\n")
						  .build();
		} 

        catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
						  .entity("{\"erreur\": \"Erreur lors de l'envoi: " + e.getMessage() + "\"}\n")
						  .build();
		}
	}
	
	/**
	 * Récupérer le nickname depuis le token en appelant interface-retour
	 */
	private String getNicknameFromToken(String token) {
		try {
			Client client = ClientBuilder.newClient();
			Response response = client.target(INTERFACE_RETOUR_SERVICE + "/authentification/validate")
									  .request(MediaType.TEXT_PLAIN)
									  .post(Entity.text(token));
			
			if (response.getStatus() == 200) {
				String nickname = response.readEntity(String.class);
				response.close();
				client.close();
				return nickname;
			}
			response.close();
			client.close();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	@POST
	@Path("subscribe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response subscribe(String jsonData) {
		try {
			String channel = extractJsonField(jsonData, "channel");
			String token = extractJsonField(jsonData, "token");
			
			if (channel == null || token == null) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Les champs 'channel' et 'token' sont requis\"}\n")
							  .build();
			}
			
			Client client = ClientBuilder.newClient();
			jakarta.ws.rs.core.Form form = new jakarta.ws.rs.core.Form();
			form.param("token", token);
			form.param("channel", channel);
			
			Response response = client.target(INTERFACE_RETOUR_SERVICE + "/authentification/subscribe")
									  .request(MediaType.TEXT_PLAIN)
									  .post(Entity.form(form));
			
			String result = response.readEntity(String.class);
			int status = response.getStatus();
			response.close();
			client.close();
			
			return Response.status(status).entity("{\"message\": \"" + result + "\"}\n").build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
					  .entity("{\"erreur\": \"Service indisponible\"}\n")
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
			String token = extractJsonField(jsonData, "token");
			
			if (channel == null || token == null) {
				return Response.status(400)
							  .entity("{\"erreur\": \"Les champs 'channel' et 'token' sont requis\"}\n")
							  .build();
			}
			
			Client client = ClientBuilder.newClient();
			jakarta.ws.rs.core.Form form = new jakarta.ws.rs.core.Form();
			form.param("token", token);
			form.param("channel", channel);
			
			Response response = client.target(INTERFACE_RETOUR_SERVICE + "/authentification/unsubscribe")
									  .request(MediaType.TEXT_PLAIN)
									  .post(Entity.form(form));
			
			String result = response.readEntity(String.class);
			int status = response.getStatus();
			response.close();
			client.close();
			
			return Response.status(status).entity("{\"message\": \"" + result + "\"}\n").build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
					  .entity("{\"erreur\": \"Service indisponible\"}\n")
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
			System.err.println("Erreur extraction JSON field '" + fieldName + "': " + e.getMessage());
			return null;
		}
	}

}