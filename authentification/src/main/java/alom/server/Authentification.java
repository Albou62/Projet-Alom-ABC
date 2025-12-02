package alom.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("authentification")
public class Authentification {

    private static Map<String, String> coupleLoginPassword = new HashMap<>();
    private static Map<String,String> coupleLoginToken = new HashMap<>();
    private static final String INTERFACE_RETOUR_BASE = "http://127.0.0.1:8080/interface-retour/webapi/authentification";
    private static final Client client = ClientBuilder.newClient();

    public static class Credentials {
        public String login;
        public String password;
        public Credentials() {}
    }

    @POST
    @Path("connexion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String connexion(Credentials body) {
        try {
            if (body == null || body.login == null || body.password == null) {
                return "login/password manquants\n";
            }
            String login = body.login;
            String password = body.password;

            if (!String.valueOf(coupleLoginPassword.get(login)).equals(password) || !coupleLoginPassword.containsKey(login)) {
                return "Login or password false\n";
            }

            String token = coupleLoginToken.get(login);
            if (token == null || token.isEmpty()) {
                return "Token not found for user\n";
            }

            try {
                sendLoginTokenToInterfaceRetour(login, token);
            } catch (Exception e) {
                System.out.println("Interface retour indisponible: " + e.getMessage()+"\n");
            }
            return "Votre token est " + token + "\n"; 
        } 
        catch (Exception e) {
            return "Error during connexion\n";
        }
    }

    
    @POST
    @Path("inscription")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String inscription(Credentials body){
        if (body == null || body.login == null || body.password == null) {
            return "login/password manquants\n";
        }
        String login = body.login;
        String password = body.password;

        if (coupleLoginPassword.containsKey(login)){
            return "Login already exist\n";
        } else {
            coupleLoginPassword.put(login, password);
            String token = generateToken();
            coupleLoginToken.put(login, token);
            return "Votre token est " + token + "\n"; 
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
    private void sendLoginTokenToInterfaceRetour(String login, String token) {
        Form form = new Form();
        form.param("token", token);
        form.param("nickname", login);

        Response response = client
                .target(INTERFACE_RETOUR_BASE + "/connexion")
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.form(form));

        try {
            int status = response.getStatus();
            String body = null;
            try { body = response.readEntity(String.class); } catch (Exception ignore) {}
            System.out.println("[authentification] POST -> interface-retour/authentification/connexion HTTP " + status +
                    (body != null ? ": " + body : ""));
        } finally {
            response.close();
        }
    }
}
