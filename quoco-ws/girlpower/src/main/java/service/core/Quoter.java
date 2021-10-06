package service.core;

import service.core.AbstractQuotationService;
import service.core.ClientInfo;
import service.core.Quotation;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.*;

import javax.xml.ws.Endpoint;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

/**
 * Implementation of the Girl Power insurance quotation service.
 * 
 * @author Rem
 *
 */
@WebService
@SOAPBinding(style = Style.RPC, use = Use.LITERAL)
public class Quoter extends AbstractQuotationService {
	// All references are to be prefixed with an DD (e.g. DD001000)
	public static final String PREFIX = "GP";
	public static final String COMPANY = "Girl Power Inc.";
	
	public static void main(String[] args) {
		try {
			// String host = args.length > 0 ? args[0]:"localhost";

			Endpoint endpoint = Endpoint.create(new Quoter());
			HttpServer server = HttpServer.create(new InetSocketAddress(9002), 5);
			server.setExecutor(Executors.newFixedThreadPool(5));
			HttpContext context = server.createContext("/quotation");
			endpoint.publish(context);
			server.start();

			Thread.sleep(8000);
			
			JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
			// ServiceInfo serviceInfo = ServiceInfo.create(
			// 	"_quotation._tcp.local.", "sqs", 9000, "path=http://"+host+":9002/quotation?wsdl"
			// );

			ServiceInfo serviceInfo = ServiceInfo.create(
				"_quotation._tcp.local.", "girlpower", 9002, "path=/quotation?wsdl"
			);

			jmdns.registerService(serviceInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Quote generation:
	 * 50% discount for being female
	 * 20% discount for no penalty points
	 * 15% discount for < 3 penalty points
	 * no discount for 3-5 penalty points
	 * 100% penalty for > 5 penalty points
	 * 5% discount per year no claims
	 */
	@WebMethod
	public Quotation generateQuotation(ClientInfo info) {
		// Create an initial quotation between 600 and 1000
		double price = generatePrice(600, 400);
		
		// Automatic 50% discount for being female
		int discount = (info.gender == ClientInfo.FEMALE) ? 50:0;
		
		// Add a points discount
		discount += getPointsDiscount(info);
		
		// Add a no claims discount
		discount += getNoClaimsDiscount(info);
		
		// Generate the quotation and send it back
		return new Quotation(COMPANY, generateReference(PREFIX), (price * (100-discount)) / 100);
	}

	private int getNoClaimsDiscount(ClientInfo info) {
		return 5*info.noClaims;
	}

	private int getPointsDiscount(ClientInfo info) {
		if (info.points == 0) return 20;
		if (info.points < 3) return 15;
		if (info.points < 6) return 0;
		return -100;
		
	}

}
