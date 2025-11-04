package alom.server;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Path("router")
public class InterfaceAller {

    @GET
    @Path("connexion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response connexion(@QueryParam("nickname") String nickname, @QueryParam("password") String password) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:8080/interface-aller/webapi/authentification/connexion")
                .queryParam("nickname", nickname)
                .queryParam("password", password);
            
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            String result = response.readEntity(String.class);
            
            return Response.status(response.getStatus())
                .entity(result)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } 
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @POST
    @Path("inscription")
    public void inscription(String nickname, String password){
        //TODO Implémenter l'inscription
    };


}
