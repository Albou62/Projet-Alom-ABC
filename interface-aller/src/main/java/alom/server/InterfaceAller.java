package alom.server;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("router")
public class InterfaceAller {

	private static final String AUTHENTIFICATION_URL = "http://127.0.0.1:8080/authentification/webapi/hello";

	@GET
    @Produces(MediaType.TEXT_PLAIN)
    public String forwardToServiceAuthentification() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(AUTHENTIFICATION_URL);

        Response response = target.request(MediaType.TEXT_PLAIN).get();

        if (response.getStatus() == 200) {
            String result = response.readEntity(String.class);
            response.close();
            return "Reponse de l'autre microservice : " + result;
        } 
		else {
            response.close();
            return "Erreur lors de l'appel à authentification : " + response.getStatus();
        }
    }

}