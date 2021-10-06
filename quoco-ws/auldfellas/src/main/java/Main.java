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

			// String host = args.length > 0 ? args[0]:"locathost";

			Endpoint endpoint = Endpoint.create(new Quoter());
			HttpServer server = HttpServer.create(new InetSocketAddress(9004), 5);
			server.setExecutor(Executors.newFixedThreadPool(5));
			HttpContext context = server.createContext("/quotation");
			endpoint.publish(context);
			server.start();

			Thread.sleep(8000);
			
			JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
			// ServiceInfo serviceInfo = ServiceInfo.create(
			// 	"_quotation._tcp.local.", "sqs", 9000, "path=http://"+host+":9001/quotation?wsdl"
			// );

			ServiceInfo serviceInfo = ServiceInfo.create(
				"_quotation._tcp.local.", "auldfellas", 9004, "path=/quotation?wsdl"
			);

			jmdns.registerService(serviceInfo);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
