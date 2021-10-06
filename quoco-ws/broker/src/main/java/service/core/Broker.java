package service.core;

import service.core.AbstractQuotationService;
import service.core.ClientInfo;
import service.core.Quotation;
import service.core.*;
import service.core.Broker;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.*;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Endpoint;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import java.net.InetAddress;
import java.net.URL;
import java.net.InetSocketAddress;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.EventListener;

import java.io.IOException;
import java.lang.reflect.Constructor;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
/**
 * Implementation of the broker service that uses the Service Registry.
 * 
 * @author Rem
 *
 */
@WebService
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
public class Broker {
	public List<String> services = new ArrayList<String>();

	public static void main(String[] args) {
		try {
			Broker broker = new Broker();
			JmDNS jmDNS = JmDNS.create(InetAddress.getLocalHost());
			// jmDNS.addServiceListener("_quotation._tcp.local.", new WSDLServiceListener(broker));
			jmDNS.addServiceListener("_quotation._tcp.local.", new WSDLServiceListener(broker));

			// String host = args.length > 0 ? args[0]:"localhost";

			Endpoint endpoint = Endpoint.create(broker);
			HttpServer server = HttpServer.create(new InetSocketAddress(9000), 5);
			server.setExecutor(Executors.newFixedThreadPool(5));
			HttpContext context = server.createContext("/broker");
			endpoint.publish(context);
			server.start();

			Thread.sleep(8000);

			JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
			// ServiceInfo serviceInfo = ServiceInfo.create(
			// 	"_broker._tcp.local", "sqs", 9000, "path=http://"+host+":9000/quotation?wsdl"
			// );

			ServiceInfo serviceInfo = ServiceInfo.create(
				"_broker._tcp.local.", "broker", 9000, "path=/broker?wsdl"
			);

			jmdns.registerService(serviceInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class WSDLServiceListener implements ServiceListener {
		Broker broker;

		public WSDLServiceListener(Broker broker) {
			this.broker = broker;
		}

		@Override
		public void serviceAdded(ServiceEvent event) {}
		@Override
		public void serviceRemoved(ServiceEvent event) {}
		@Override
		public void serviceResolved(ServiceEvent event) {
			String path = event.getInfo().getPropertyString("path");
			
			// if (path != null) this.broker.addService(path);
			if (path != null) {
				
				String url = event.getInfo().getURLs()[0];
				this.broker.addService(url);
			}
		}
	}

	public void addService(String url) {
		this.services.add(url);
	}

	public List<Quotation> getQuotations(ClientInfo info) {
		List<Quotation> quotations = new LinkedList<Quotation>();

		if (this.services == null) {
			System.out.println("No services found");
			return quotations;
		}

		for (String str : this.services) {
			try {
				URL url = new URL(str);
				QName serviceName = new QName("http://core.service/", "QuoterService");
				Service service = Service.create(url, serviceName);
		
				QName portName = new QName("http://core.service/", "QuoterPort");
				QuoterService quoterService = 
					service.getPort(portName, QuoterService.class);
		
				quotations.add(quoterService.generateQuotation(info));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return quotations;
	}
}
