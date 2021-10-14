package client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.Session;
import javax.jms.TextMessage;

import java.text.NumberFormat;
import java.util.Map;
import java.util.HashMap;

import service.core.*;
import service.message.*;
// imoort service.message.ClientApplicationMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Main {
	
	/**
	 * This is the starting point for the application. Here, we must
	 * get a reference to the Broker Service and then invoke the
	 * getQuotations() method on that service.
	 * 
	 * Finally, you should print out all quotations returned
	 * by the service.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String host = args.length > 0 ? args[0]:"localhost";
		ConnectionFactory factory = new ActiveMQConnectionFactory("failover://tcp://"+host+":61616");

		try {
			Connection connection = factory.createConnection();				
			connection.setClientID("client");
			Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			
			// Create the queue for holding all requests sent to the broker
			Queue brokerReqQ = session.createQueue("REQUESTS");
			// Create the queue for holding all responses from the broker
			Queue brokerResponseQ = session.createQueue("RESPONSES");
			// Create a producer for sending requests to the broker
			MessageProducer brokerRequests = session.createProducer(brokerReqQ);
			// Create a consumer for recieving the responses from the broker
			MessageConsumer brokerConsumer = session.createConsumer(brokerResponseQ);


			// Create a seed number for giving requests a unique ID
			int SEED_ID = 0;
			
			// Start the connection with the broker
			connection.start();

				
			for (int i = 0; i < clients.length; i++) {
				// Create a new request in the form of a class
				QuotationRequestMessage quotationRequest = new QuotationRequestMessage(SEED_ID++, clients[i]);
				// Create a request object message for sending to the broker
				Message request = session.createObjectMessage(quotationRequest);

				// Send the message to the broker
				brokerRequests.send(request);

				// Wait for a response from the broker
				Message message = brokerConsumer.receive();
				message.acknowledge();

				// Check the message type is correct
				if (message instanceof ObjectMessage) {
					// Get the message object from the response
					Object content = ((ObjectMessage) message).getObject();
					
					if (content instanceof ClientApplicationMessage) {
						// Get the response info
						ClientApplicationMessage response = (ClientApplicationMessage) content;
						
						// Display the client info to the user
						displayProfile(response.info);
						
						for(int r = 0; r < response.quotations.size(); r++) {
							displayQuotation(response.quotations.get(r));
						}
						System.out.println("\n");
					}
				} else {
					System.out.println("Unknown message type: " +
					message.getClass().getCanonicalName());
				}

			}
		} catch(JMSException e) {
			System.out.println("ERROR OCCURRED");
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
