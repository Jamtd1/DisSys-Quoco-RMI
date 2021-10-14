package broker;

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
                // Get a message from the client
                System.out.println("Waiting on a message");
                Message message = clientConsumer.receive();
                message.acknowledge();


                if (message instanceof ObjectMessage) {
                    QuotationRequestMessage quoteMessage = (QuotationRequestMessage)((ObjectMessage) message).getObject();

                    // Send the message to the applications
                    applicationProducer.send(message);

                    ClientApplicationMessage response = new ClientApplicationMessage(quoteMessage.id, quoteMessage.info, new ArrayList());

                    Thread getResponses = new Thread() {
                        public void run() {
                            try {
                                while(true) {
                                    Message appsMessage = appsConsumer.receive();
                                    appsMessage.acknowledge();
            
                                    if (appsMessage instanceof ObjectMessage) {
                                        QuotationResponseMessage quoteAppsMessage = (QuotationResponseMessage)((ObjectMessage) appsMessage).getObject();
                                        response.quotations.add(quoteAppsMessage.quotation);
                                    }
                                }
                            } catch (JMSException e) {
                                
                            }
                        }
                    };

                    getResponses.start();
                    try {
                        Thread.sleep(2000);
                    } catch(InterruptedException v) {

                    }
                    getResponses.interrupt();

                    Message quoteResponse = session.createObjectMessage(response);

                    clientProducer.send(quoteResponse);

                }
            }
			
		} catch(JMSException e) {
			System.out.println("ERROR OCCURRED");
			e.printStackTrace();
		}
		
	}
	
}
