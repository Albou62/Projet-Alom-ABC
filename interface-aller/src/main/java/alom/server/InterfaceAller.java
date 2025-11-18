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
						  .entity("{\"erreur\": \"Service authentification indisponible\"}")
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
						  .entity("{\"erreur\": \"Service authentification indisponible\"}")
						  .build();
		}
	}


}