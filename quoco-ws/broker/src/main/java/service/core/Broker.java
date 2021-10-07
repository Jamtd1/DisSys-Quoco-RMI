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

	// initialise a list of services using the ArrayList type
	public List<String> services = new ArrayList<String>();

	// initialise the main method
	public static void main(String[] args) {

		try {

			// create a broker variable and assign the broker class to it
			Broker broker = new Broker();

			// create a JmDNS instance and create an address 
			JmDNS jmDNS = JmDNS.create(InetAddress.getLocalHost());

			// add a srvice listener to the instance which looks for quotation services usingn _quotation
			jmDNS.addServiceListener("_quotation._tcp.local.", new WSDLServiceListener(broker));

			// Obsolete as no longer needing host in ServiceInfo
			// Set up the host variable for JmDNS
			// String host = args.length > 0 ? args[0]:"localhost";

			// create an endpoint for borker
			Endpoint endpoint = Endpoint.create(broker);

			// initialise the httpserrver with a port of 9000
			HttpServer server = HttpServer.create(new InetSocketAddress(9000), 5);

			// Set the executor to have 5 threads
			server.setExecutor(Executors.newFixedThreadPool(5));

			// set the url suffix to be /broker to distinguish it from a quoter service
			HttpContext context = server.createContext("/broker");

			// publish the context
			endpoint.publish(context);

			// start the server
			server.start();

			// allow a delay for the service to connect with the client and other services
			Thread.sleep(8000);

			// create a JmDNS instance for the broker
			JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

			// create a serviceInfo with the broker declaration, port and path
			ServiceInfo serviceInfo = ServiceInfo.create(
			
			// Obsolete as better way to enable this is seen below
			// 	"_broker._tcp.local", "sqs", 9000, "path=http://"+host+":9000/quotation?wsdl"
			
			// _borker._tcp.local. allows the client to see this service 
			// boker, 9000 and path create the wsdl document
			"_broker._tcp.local.", "broker", 9000, "path=/broker?wsdl"
			);

			// register the service
			jmdns.registerService(serviceInfo);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create a WSDLServiceListener class implementing the ServiceListener
	public static class WSDLServiceListener implements ServiceListener {
	
		//initialise a broker variable
		Broker broker;

		// assign a broker variable type to the WSDLServiceListener
		public WSDLServiceListener(Broker broker) {
			this.broker = broker;
		}

		@Override
		public void serviceAdded(ServiceEvent event) {}
		@Override
		public void serviceRemoved(ServiceEvent event) {}

		// use serviceResolved to fethc the url from the serviceListener
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

	// method to add the url fethced in the serviceListener to the services arrayList.
	public void addService(String url) {
		this.services.add(url);
	}

	// method to fetch the quotations for the client from each quoter service
	public List<Quotation> getQuotations(ClientInfo info) {
		
		//initialise a quotations list
		List<Quotation> quotations = new LinkedList<Quotation>();

		// if the serives list is null print no services found and return
		if (this.services == null) {
			System.out.println("No services found");
			return quotations;
		}

		// for each url set up a namespace with associated port and serivce url
		for (String str : this.services) {
			try {
				URL url = new URL(str);
				QName serviceName = new QName("http://core.service/", "QuoterService");
				Service service = Service.create(url, serviceName);
		
				QName portName = new QName("http://core.service/", "QuoterPort");
				QuoterService quoterService = 
					service.getPort(portName, QuoterService.class);
		
				// add the quotations generated to quotations 
				quotations.add(quoterService.generateQuotation(info));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// return the list of quotations
		return quotations;
	}
}
