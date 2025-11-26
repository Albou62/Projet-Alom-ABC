package alom.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("authentification")
public class Authentification {

    @POST
    @Path("connexion")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response connexion(@FormParam("token") String token,
                              @FormParam("nickname") String nickname) {
        if (token == null || token.isEmpty() || nickname == null || nickname.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("token et nickname sont requis").build();
        }
        App.inscriptionToken(token, nickname);
        
        App.startKafkaConsumerForUser(nickname);
        
        return Response.ok("Accept connexion").build();
    }
    
    @POST
    @Path("subscribe")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response subscribe(@FormParam("token") String token,
                              @FormParam("channel") String channel) {
        if (token == null || token.isEmpty() || channel == null || channel.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("token et channel sont requis").build();
        }
        
        String nickname = App.getNicknameFromToken(token);
        if (nickname == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token invalide").build();
        }
        
        App.subscribeToChannel(nickname, channel);
        return Response.ok("Abonné au channel " + channel).build();
    }
    
    @POST
    @Path("unsubscribe")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response unsubscribe(@FormParam("token") String token,
                                @FormParam("channel") String channel) {
        if (token == null || token.isEmpty() || channel == null || channel.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("token et channel sont requis").build();
        }
        
        String nickname = App.getNicknameFromToken(token);
        if (nickname == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token invalide").build();
        }
        
        App.unsubscribeFromChannel(nickname, channel);
        return Response.ok("Désabonné du channel " + channel).build();
    }
}


