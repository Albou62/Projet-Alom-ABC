package alom.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
// imports Produces/MediaType non utilisés pour l'instant


@Path("authentification")
public class Authentification {

    private static Map<String, String> coupleLoginPassword = new HashMap<>();
    private static Map<String,String> coupleLoginToken = new HashMap<>();
    // Adapter l'URL si le contexte Tomcat diffère
    private static final String INTERFACE_RETOUR_REGISTER_URL = "http://127.0.0.1:8080/interface-retour/webapi/register";

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
            String token = generateToken();
            coupleLoginToken.put(login,token);
            //TODO Envoie validation inscription
            sendTokenToInterfaceRetour(login, token);
        }
    };

    private String generateToken(){
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        String result = "";
        Random random = new Random();

        for (int i = 0; i < 15; i++) {
            int index = random.nextInt(characters.length());
            result += characters.charAt(index);
        }
        return result;
    }

    private void sendTokenToInterfaceRetour(String login, String token) {
       // à implémenter : envoyer le token à l'interface retour
    }
}
