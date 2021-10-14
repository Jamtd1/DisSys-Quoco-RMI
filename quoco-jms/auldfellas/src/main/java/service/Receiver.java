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

import service.core.*;
import service.auldfellas.*;

import service.message.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;


public class Receiver {
    public static void main(String[] args) {
        QuotationService service = new AFQService();

        String host = args.length > 0 ? args[0]:"localhost";
        ConnectionFactory factory = new ActiveMQConnectionFactory("failover://tcp://"+host+":61616");

        try {
            Connection connection = factory.createConnection();
            
            connection.setClientID("auldfellas");
            
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            
            Queue queue = session.createQueue("QUOTATIONS");
            Topic topic = session.createTopic("APPLICATIONS");
            MessageConsumer consumer = session.createConsumer(topic);
            MessageProducer producer = session.createProducer(queue);

            connection.start();
            while (true) {
                // Get the next message from the APPLICATION topic
                Message message = consumer.receive();

                System.out.println("MESSAGE RECIEVED");

                // Check it is the right type of message
                if (message instanceof ObjectMessage) {
                    // It’s an Object Message
                    Object content = ((ObjectMessage) message).getObject();
                    
                    if (content instanceof QuotationRequestMessage) {
                        // It’s a Quotation Request Message
                        QuotationRequestMessage request = (QuotationRequestMessage) content;
                        // Generate a quotation and send a quotation response message…
                        Quotation quotation = service.generateQuotation(request.info);
                        Message response = session.createObjectMessage(
                            new QuotationResponseMessage(request.id, quotation)
                        );

                        producer.send(response);
                        System.out.println("MESSAGE SENT");
                        System.out.print(response);

                    }
                } else {
                    System.out.println("Unknown message type: " +
                    message.getClass().getCanonicalName());
                }
            }
    
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
