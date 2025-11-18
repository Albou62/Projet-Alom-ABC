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
    private static final Client client = ClientBuilder.newClient();

    @GET
    @Path("connexion")
    @Consumes(MediaType.APPLICATION_JSON)
    public connexion(String login, String password) {
        if (coupleLoginPassword.containsKey(login)){
            return "Login not found"
        }else if (coupleLoginPassword.get(login) == password) {
            Response response = client.targer(INTERFACE_RETOUR_REGISTER_URL+ "/connexion")
            return coupleLoginToken.get(login)
        }else{
            return "Login or password false"
        }
    };

    @POST
    @Path("inscription")
    public inscription(String login, String password){
        if (coupleLoginPassword.containsKey(login)){
            return "Login already exist"
        }else{
            coupleLoginPassword.put(login,password);
            String token = generateToken();
            coupleLoginToken.put(login,token);
            Response response = client.target(INTERFACE_RETOUR_REGISTER_URL + "/authentification").post(token,login);
            return response;
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


}
