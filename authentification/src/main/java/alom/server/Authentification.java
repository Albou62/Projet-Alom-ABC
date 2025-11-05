package alom.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        try {
            String form = "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8.name())
                    + "&nickname=" + URLEncoder.encode(login, StandardCharsets.UTF_8.name());

            URL url = new URL(INTERFACE_RETOUR_REGISTER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            byte[] bytes = form.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(bytes.length);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            int code = conn.getResponseCode();
            System.out.println("[authentification] Appel interface-retour /register => HTTP " + code);
            conn.disconnect();
        } catch (IOException e) {
            System.err.println("[authentification] Erreur lors de l'envoi du token à interface-retour: " + e.getMessage());
        }
    }
}
