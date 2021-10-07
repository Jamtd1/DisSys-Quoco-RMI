import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.concurrent.Executors;

import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import service.core.Quoter;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class Main {
    public static void main(String[] args) {
		try {

			// obsolete as host was depricated from the serviceInfo creation 
			// String host = args.length > 0 ? args[0]:"locathost";

			// create an endpoint for the auldfellas quoter class
			Endpoint endpoint = Endpoint.create(new Quoter());

			// set up the http server on port 9004
			HttpServer server = HttpServer.create(new InetSocketAddress(9004), 5);
			
			// Set the executor to have 5 threads
			server.setExecutor(Executors.newFixedThreadPool(5));
			
			// set the url suffix to be /broker to distinguish it from a quoter service
			HttpContext context = server.createContext("/quotation");

			// publish the server
			endpoint.publish(context);

			// start the server
			server.start();

			// add a delay so that the broker may establish connection to the service
			Thread.sleep(8000);
			
			// intiailise the jmdns instance and create the service
			JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
			
			// create a serviceInfo with the broker declaration, port and path
			ServiceInfo serviceInfo = ServiceInfo.create(
			
			// depricated
			// 	"_quotation._tcp.local.", "sqs", 9000, "path=http://"+host+":9001/quotation?wsdl"
			
			// create the serviceInfo with a call tag of _quotation._tcp.local. so client cannot find it along with the service name, port number and wsdl url suffix
			"_quotation._tcp.local.", "auldfellas", 9004, "path=/quotation?wsdl"
			);

			// register the service to jmdns
			jmdns.registerService(serviceInfo);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
