package alom.server;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("authentification")
public class Authentification {

    private static Map<String, String> coupleLoginPassword = new HashMap<>();

    @GET
    @Path("connexion")
    public void connexion(String login, String password) {
        if (coupleLoginPassword.containsKey(login)){
            //TODO Renvoie erreur à aller
        }else if (coupleLoginPassword.get(login) == password) {
            //TODO Connexion au token retour
        }else{
            //TODO Message erreur connexion
        }
    };

    @POST
    @Path("inscription")
    public void inscription(String login, String password){
        if (coupleLoginPassword.containsKey(login)){
            //TODO Renvoie erreur à aller
        }else{
            coupleLoginPassword.put(login,password);
            //TODO Envoie validation insccription
        }
    };
}
