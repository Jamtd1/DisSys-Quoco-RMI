import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import service.core.Broker;


public class Main {
    public static void main(String[] args) {
		try {
			// Endpoint endpoint = Endpoint.create(new Broker());
			// HttpServer server = HttpServer.create(new InetSocketAddress(9000), 5);
			// server.setExecutor(Executors.newFixedThreadPool(5));
			// HttpContext context = server.createContext("/broker");
			// endpoint.publish(context);
			// server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
