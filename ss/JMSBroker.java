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
import java.util.List;
import java.util.ArrayList;


import service.core.*;
import service.message.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSBroker {
	
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

		try{
			Connection connection = factory.createConnection();
			connection.setClientID("broker");
			Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            // initialise a static map to store the clientinfo and a request id
			Map<Long, ClientInfo> cache = new HashMap<Long, ClientInfo>();
			
            // initialise a starting seed
            int SEED_ID = 0;

            // Start the connection
			connection.start();

            // Queue for client requets - brokers listens(consumes) for requests for quotes from the client
            Queue clientRequests = session.createQueue("REQUESTS");
            // Consumer to recieve client messages
            MessageConsumer clientConsumer = session.createConsumer(clientRequests);

            // Queue for client requets - brokers listens(consumes) for requests for quotes from the client
            Queue clientResponses = session.createQueue("RESPONSES");
            // Consumer to produce client messages
            MessageProducer clientProducer = session.createProducer(clientResponses);

            // Queue for quote responses - brokers listens(consumes) for requests for quotes from the client
            Queue quoteRequests = session.createQueue("QUOTATIONS");
            // Consumer to recieve quote messages
            MessageConsumer appsConsumer = session.createConsumer(quoteRequests);

            // Topic for applications - broker sends(produces) the client information to all of the quotation services
            Topic applications = session.createTopic("APPLICATIONS");
            // Create a producer for publishing to the Applications
            MessageProducer applicationProducer = session.createProducer(applications);

            while (true) {
                // Thread wait = new Thread() {
                //     public void run() {
                //         try {
                //             Thread.sleep(1000);
                //         } catch(InterruptedException v){}
                //     }
                // }.start();

                // Get a message from the client
                Message message = clientConsumer.receive();

                if (message instanceof ObjectMessage) {
                    QuotationRequestMessage quoteMessage = (QuotationRequestMessage)((ObjectMessage) message).getObject();

                    // Send the message to the applications
                    applicationProducer.send(message);

                    boolean finished = false; 
                    
                    // Thread wait = new Thread() {
                    //     public void run() {
                    //         try {
                    //             Thread.sleep(5000);
                    //             finished = true;
                    //         } catch(InterruptedException v){}
                    //     }
                    // }.start();
                    
                    List quotations = new ArrayList();

                    ClientApplicationMessage response = new ClientApplicationMessage(quoteMessage.id, quoteMessage.info, quotations);

                    // Get a message from the apps
                    Message appsMessage = appsConsumer.receive();

                    if (message instanceof ObjectMessage) {
                        QuotationResponseMessage quoteAppsMessage = (QuotationResponseMessage)((ObjectMessage) appsMessage).getObject();
                        quotations.add(quoteAppsMessage.quotation);
                    }

                    Message quoteResponse = session.createObjectMessage(response);

                    clientProducer.send(quoteResponse);

                }
                


            // }
            // // Get a message from the client
            // Message message = clientConsumer.recieve();

            // if (message instanceof ObjectMessage) {
            //     // Get the message object from the response
            //     Object content = ((ObjectMessage) message).getObject();
            //     System.out.print(content);
            //     if (content instanceof QuotationRequestMessage) {
            //         applicationProducer.send((QuotationRequestMessage) content)
            //     }
            // }



            // Thread wait = new Thread() {
            //     public void run() {
            //         try {
            //             Thread.sleep(1000);
            //         } catch(InterruptedException v){

            //         }
            //     }
            // }.start()

            // while(true) {
            //     Thread wait = new Thread() {
            //         public void run() {
            //             try {
            //                 Thread.sleep(1000);
            //             } catch(InterruptedException v){

            //             }
            //         }
            //     }.start()

            // }
            





            //
            // QUOTATIONS CODE
            //
            // Topic for applications - broker sends(produces) the client information to all of the quotation services
            // Topic applications = session.createTopic("APPLICATIONS");

            // MessageProducer applicationProducer = session.createProducer(applications);
            // MwssageConsumer applicationConsumer = session.createConsumer(applications);

            // // Create a message to get
			// QuotationRequestMessage quotationRequest = new QuotationRequestMessage(SEED_ID++, clients[0]);
            

            // // // Queue for quotations - broker listens(consumes) for the quotations returned by the quotation services 
			// // Queue servicesQ = session.createQueue("SERVICES");

            // // // Consumer to recieve quotation services messages
            // // MessageConsumer servicesConsumer = session.createConsumer(servicesQ);


            // // PUBSUB
            // // Topic for applications - broker sends(produces) the client information to all of the quotation services
			// Topic applications = session.createTopic("APPLICATIONS");

			// MessageProducer producerApplications = session.createProducer(applications);
			// // MessageConsumer consumerRequests = session.createConsumer(requests);
            // MwssageConsumer consumerQuotations= session.createConsumer(quotations);


            
			// QuotationRequestMessage quotationRequest = new QuotationRequestMessage(SEED_ID++, clients[0]);
			// Message request = session.createObjectMessage(quotationRequest);
			// System.out.print(request);
			// cache.put(quotationRequest.id, quotationRequest.info);

			// producer.send(request);

			// System.out.println("Message Sent");
			
			// Message message = consumer.receive();

			// System.out.println("Message recieved");

			// if (message instanceof ObjectMessage) {
			// 	Object content = ((ObjectMessage) message).getObject();
			// 	if (content instanceof QuotationResponseMessage) {
			// 		QuotationResponseMessage response = (QuotationResponseMessage) content;
			// 		ClientInfo info = cache.get(response.id);
			// 		displayProfile(info);
			// 		displayQuotation(response.quotation);
			// 		System.out.println("\n");
			// 	}
			// 	message.acknowledge();
			// } else {
			// 	System.out.println("Unknown message type: " +
			// 	message.getClass().getCanonicalName());
			// }
            }
			
		} catch(JMSException e) {
			System.out.println("ERROR OCCURRED");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Display the client info nicely.
	 * 
	//  * @param info
	 */
	// public static void displayProfile(ClientInfo info) {
	// 	System.out.println("|=================================================================================================================|");
	// 	System.out.println("|                                     |                                     |                                     |");
	// 	System.out.println(
	// 			"| Name: " + String.format("%1$-29s", info.name) + 
	// 			" | Gender: " + String.format("%1$-27s", (info.gender==ClientInfo.MALE?"Male":"Female")) +
	// 			" | Age: " + String.format("%1$-30s", info.age)+" |");
	// 	System.out.println(
	// 			"| License Number: " + String.format("%1$-19s", info.licenseNumber) + 
	// 			" | No Claims: " + String.format("%1$-24s", info.noClaims+" years") +
	// 			" | Penalty Points: " + String.format("%1$-19s", info.points)+" |");
	// 	System.out.println("|                                     |                                     |                                     |");
	// 	System.out.println("|=================================================================================================================|");
	// }

	// /**
	//  * Display a quotation nicely - note that the assumption is that the quotation will follow
	//  * immediately after the profile (so the top of the quotation box is missing).
	//  * 
	//  * @param quotation
	//  */
	// public static void displayQuotation(Quotation quotation) {
	// 	System.out.println(
	// 			"| Company: " + String.format("%1$-26s", quotation.company) + 
	// 			" | Reference: " + String.format("%1$-24s", quotation.reference) +
	// 			" | Price: " + String.format("%1$-28s", NumberFormat.getCurrencyInstance().format(quotation.price))+" |");
	// 	System.out.println("|=================================================================================================================|");
	// }
	
	// /**
	//  * Test Data
	//  */
	// public static final ClientInfo[] clients = {
	// 	new ClientInfo("Niki Collier", ClientInfo.FEMALE, 43, 0, 5, "PQR254/1"),
	// 	new ClientInfo("Old Geeza", ClientInfo.MALE, 65, 0, 2, "ABC123/4"),
	// 	new ClientInfo("Hannah Montana", ClientInfo.FEMALE, 16, 10, 0, "HMA304/9"),
	// 	new ClientInfo("Rem Collier", ClientInfo.MALE, 44, 5, 3, "COL123/3"),
	// 	new ClientInfo("Jim Quinn", ClientInfo.MALE, 55, 4, 7, "QUN987/4"),
	// 	new ClientInfo("Donald Duck", ClientInfo.MALE, 35, 5, 2, "XYZ567/9")		
	// };
}
