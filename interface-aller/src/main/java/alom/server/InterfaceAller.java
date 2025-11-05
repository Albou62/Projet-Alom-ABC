package alom.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Path("router")
public class InterfaceAller {

	private static final String AUTH_BASE = "http://127.0.0.1:8080/authentification/webapi/authentification";

	// DTO pour la réception JSON depuis Postman
	public static class Credentials {
		public String login;
		public String password;

		public Credentials() {}
	}

	@POST
	@Path("connexion")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response proxyConnexion(Credentials body) {
		if (body == null || body.login == null || body.password == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\":\"login et password requis\"}").build();
		}
		try {
			// Transmet en x-www-form-urlencoded à authentification
			String form = "login=" + URLEncoder.encode(body.login, StandardCharsets.UTF_8.name()) +
						  "&password=" + URLEncoder.encode(body.password, StandardCharsets.UTF_8.name());

			URL url = new URL(AUTH_BASE + "/connexion");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

			byte[] bytes = form.getBytes(StandardCharsets.UTF_8);
			conn.setFixedLengthStreamingMode(bytes.length);
			try (OutputStream os = conn.getOutputStream()) { os.write(bytes); }

			int code = conn.getResponseCode();
			InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
			String resp = readAllAsString(is);
			conn.disconnect();

			return Response.status(code)
					.entity(resp == null || resp.isEmpty() ? "{\"status\":\"forwarded\"}" : resp)
					.type(MediaType.APPLICATION_JSON)
					.build();
		} catch (IOException e) {
			return Response.status(Response.Status.BAD_GATEWAY)
					.entity("{\"error\":\"appel authentification échoué: " + escape(e.getMessage()) + "\"}")
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
	}

	private static String escape(String s) {
		return s == null ? "" : s.replace("\"", "\\\"");
	}

	private static String readAllAsString(InputStream is) throws IOException {
		if (is == null) return "";
		byte[] buf = new byte[4096];
		int n;
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		while ((n = is.read(buf)) != -1) {
			baos.write(buf, 0, n);
		}
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}
}