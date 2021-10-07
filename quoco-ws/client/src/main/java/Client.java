import service.core.*;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import java.util.List;
import java.text.NumberFormat;

import java.util.EventListener;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class Client {
    public static void main(String[] args) throws Exception{
        // try {
			// String host = "localhost";
			// int port = 9000;
			// // More Advanced flag-based configuration
			// for (int i=0; i < args.length; i++) {
			// 	switch (args[i]) {
			// 		case "-h":
			// 			host = args[++i];
			// 			break;
			// 		case "-p":
			// 			port = Integer.parseInt(args[++i]);
			// 			break;
			// 		default:
			// 			System.out.println("Unknown flag: " + args[i] +"\n");
			// 			System.out.println("Valid flags are:");
			// 			System.out.println("\t-h <host>\tSpecify the hostname of the target service");
			// 			System.out.println("\t-p <port>\tSpecify the port number of the target service");
			// 			System.exit(0);
			// 	}
			// }

		// 	URL wsdlUrl = new
		// 		URL("http://" + host + ":" + port + "/broker?wsdl");

		// 	QName serviceName =
		// 		new QName("http://core.service/", "BrokerService");

		// 	Service service = Service.create(wsdlUrl, serviceName);

		// 	QName portName = new QName("http://core.service/", "BrokerPort");

		// 	BrokerService brokerService =
		// 		service.getPort(portName, BrokerService.class);

		// 	for (ClientInfo info : clients) {
		// 		displayProfile(info);
		// 		List<Quotation> quotations = brokerService.getQuotations(info);

		// 		for (Quotation quotation : quotations) {
		// 			displayQuotation(quotation);
		// 		}
		// 	}
        // // } catch (Exception e) {
        // //     e.printStackTrace();
        // // }

		// initialise a JmDNS variable and assign it an InetAddress
		JmDNS jmDNS = JmDNS.create(InetAddress.getLocalHost());

		// Add a service listener to listen for the broker service indicated by _broker
		jmDNS.addServiceListener("_broker._tcp.local.", new WSDLServiceListener());
	}

	//implement a WSDLServiceListener class which implements the ServiceListener class
	public static class WSDLServiceListener implements ServiceListener {
		
		// default code, no changes required
		@Override
		public void serviceAdded(ServiceEvent event) {}

		// default code, no changes required
		@Override
		public void serviceRemoved(ServiceEvent event) {}

		// When the listener finds a service get the path to the service and store it in a string variable called url
		@Override
		public void serviceResolved(ServiceEvent event) {
			String path = event.getInfo().getPropertyString("path");
			// if (path != null) connectToService(path);
			if (path != null) {
				String url = event.getInfo().getURLs()[0];
				connectToService(url);
			}
		}

	}

	// initialise the connectToService method with the url found in the listener
	private static void connectToService(String url) {
		try {

			// assign the url received in the args to a variable
			URL wsdlURL = new URL(url);
			
			// Initialise variable serviceName and assign the namespace BrokerService to it
			QName serviceName =
				new QName("http://core.service/", "BrokerService");

			// Initialise service variable and assign a service based on the listener url and the namespace
			Service service = Service.create(wsdlURL, serviceName);

			// Initialise the variable portname and assign the namespace BrokerPort to it
			QName portName = new QName("http://core.service/", "BrokerPort");

			// Initialise a variable brokerService and assign the retrieved port to it
			BrokerService brokerService =
				service.getPort(portName, BrokerService.class);

			// for each client in the list of clients 
			for (ClientInfo info : clients) {

				// retireve the clients info and print it
				displayProfile(info);

				// create a list of quotations and call the getQuotations method with the client info from the brokerService
				List<Quotation> quotations = brokerService.getQuotations(info);

				// display each of the quotations retrieved by the broker service
				for (Quotation quotation : quotations) {
					displayQuotation(quotation);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
	 * Display the client info nicely.
	 * 
	 * @param info
	 */
	public static void displayProfile(ClientInfo info) {
		System.out.println("|=================================================================================================================|");
		System.out.println("|                                     |                                     |                                     |");
		System.out.println(
				"| Name: " + String.format("%1$-29s", info.name) + 
				" | Gender: " + String.format("%1$-27s", (info.gender==ClientInfo.MALE?"Male":"Female")) +
				" | Age: " + String.format("%1$-30s", info.age)+" |");
		System.out.println(
				"| License Number: " + String.format("%1$-19s", info.licenseNumber) + 
				" | No Claims: " + String.format("%1$-24s", info.noClaims+" years") +
				" | Penalty Points: " + String.format("%1$-19s", info.points)+" |");
		System.out.println("|                                     |                                     |                                     |");
		System.out.println("|=================================================================================================================|");
	}

	/**
	 * Display a quotation nicely - note that the assumption is that the quotation will follow
	 * immediately after the profile (so the top of the quotation box is missing).
	 * 
	 * @param quotation
	 */
	public static void displayQuotation(Quotation quotation) {
		System.out.println(
				"| Company: " + String.format("%1$-26s", quotation.company) + 
				" | Reference: " + String.format("%1$-24s", quotation.reference) +
				" | Price: " + String.format("%1$-28s", NumberFormat.getCurrencyInstance().format(quotation.price))+" |");
		System.out.println("|=================================================================================================================|");
	}
	
	/**
	 * Test Data
	 */
	public static final ClientInfo[] clients = {
		new ClientInfo("Niki Collier", ClientInfo.FEMALE, 43, 0, 5, "PQR254/1"),
		new ClientInfo("Old Geeza", ClientInfo.MALE, 65, 0, 2, "ABC123/4"),
		new ClientInfo("Hannah Montana", ClientInfo.FEMALE, 16, 10, 0, "HMA304/9"),
		new ClientInfo("Rem Collier", ClientInfo.MALE, 44, 5, 3, "COL123/3"),
		new ClientInfo("Jim Quinn", ClientInfo.MALE, 55, 4, 7, "QUN987/4"),
		new ClientInfo("Donald Duck", ClientInfo.MALE, 35, 5, 2, "XYZ567/9")		
	};
}
