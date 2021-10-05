package service.core;

import service.core.AbstractQuotationService;
import service.core.ClientInfo;
import service.core.Quotation;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.*;

import java.util.LinkedList;
import java.util.List;

import service.core.*;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import service.core.Broker;

/**
 * Implementation of the broker service that uses the Service Registry.
 * 
 * @author Rem
 *
 */
@WebService
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
public class Broker {
	public String[] endpoints = {
		"http://localhost:9001/quotation?wsdl", 
		"http://localhost:9002/quotation?wsdl",
		"http://localhost:9003/quotation?wsdl"
	};

	public static void main(String[] args) {
		try {
			Endpoint endpoint = Endpoint.create(new Broker(args));
			HttpServer server = HttpServer.create(new InetSocketAddress(9000), 5);
			server.setExecutor(Executors.newFixedThreadPool(5));
			HttpContext context = server.createContext("/broker");
			endpoint.publish(context);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Broker(String[] urls) {
		if (urls.length >= 1) {
			this.endpoints = urls;
		}
	}

	public List<Quotation> getQuotations(ClientInfo info) {
		List<Quotation> quotations = new LinkedList<Quotation>();

		for (String str : this.endpoints) {
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
