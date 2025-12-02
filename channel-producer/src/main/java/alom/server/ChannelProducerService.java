package alom.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/webapi")
public class ChannelProducerService {
    
    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendChannelMessage(String jsonData) {
        try {
            System.out.println("[ChannelProducerService] Message reçu: " + jsonData);
            
            String message = extractJsonField(jsonData, "message");
            String token = extractJsonField(jsonData, "token");
            String channel = extractJsonField(jsonData, "channel");
            
            if (message == null || token == null || channel == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"erreur\": \"Les champs 'message', 'token' et 'channel' sont requis\"}\n")
                        .build();
            }
            
            String expediteur = validateTokenAndGetNickname(token);
            if (expediteur == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"erreur\": \"Token invalide\"}\n")
                        .build();
            }
            
            String formattedMessage = "[" + channel + "] (" + expediteur + ") " + message;
            
            alom.App.processAndSendMessage(formattedMessage);
            
            System.out.println("[ChannelProducerService] Message envoyé: " + formattedMessage);
            
            return Response.ok("{\"message\": \"Message envoyé\"}\n").build();
            
        } 
        catch (Exception e) {
            System.err.println("[ChannelProducerService] Erreur: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"erreur\": \"Erreur lors de l'envoi: " + e.getMessage() + "\"}\n")
                    .build();
        }
    }

    private String validateTokenAndGetNickname(String token) {
        try {
            jakarta.ws.rs.client.Client client = jakarta.ws.rs.client.ClientBuilder.newClient();
            String url = "http://127.0.0.1:8080/interface-retour/webapi/authentification/validate";
            
            jakarta.ws.rs.core.Response response = client.target(url)
                    .request(jakarta.ws.rs.core.MediaType.TEXT_PLAIN)
                    .post(jakarta.ws.rs.client.Entity.text(token));
            
            if (response.getStatus() == 200) {
                String nickname = response.readEntity(String.class);
                client.close();
                return nickname;
            }
            client.close();
            return null;
        } catch (Exception e) {
            System.err.println("[ChannelProducerService] Erreur validation token: " + e.getMessage());
            return null;
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
