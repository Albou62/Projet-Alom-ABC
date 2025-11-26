package alom.server;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("auth")
public class AuthResource {

    @GET
    @Path("nickname")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getNickname(@QueryParam("token") String token) {
        if (token == null || token.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Token requis").build();
        }
        
        String nickname = App.getNicknameFromToken(token);
        
        if (nickname == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token invalide").build();
        }
        
        return Response.ok(nickname).build();
    }
}
