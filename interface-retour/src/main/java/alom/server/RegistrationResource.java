package alom.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("register")
public class RegistrationResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response registerToken(@FormParam("token") String token,
                                  @FormParam("nickname") String nickname) {
        if (token == null || token.isEmpty() || nickname == null || nickname.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("token et nickname sont requis").build();
        }
        App.inscriptionToken(token, nickname);
        System.out.println("[REST] Token enregistré via WS: token=" + token + ", nickname=" + nickname);
        return Response.ok("OK").build();
    }
}
