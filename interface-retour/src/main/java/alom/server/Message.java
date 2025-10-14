package alom.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Message {
	public Socket s;
	public InputStream in;
	public OutputStream out;
	public String msg;
	
	public Message(Socket s) throws IOException {
		this.s = s;
		out = s.getOutputStream();
	}
	
	public Socket getSocket() {
		return this.s;
	}
}
