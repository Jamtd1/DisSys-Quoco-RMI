package service.auldfellas;

import service.core.ClientInfo;
import service.core.AbstractQuotationService;
import service.core.Quotation;
import service.core.Constants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.Serializable;


/**
 * Implementation of the AuldFellas insurance quotation service.
 * 
 * @author Rem
 *
 */
@RestController
public class AFQService extends AbstractQuotationService implements Serializable {

	// default constructor
	public AFQService() {}

	// All references are to be prefixed with an AF (e.g. AF001000)
	public static final String PREFIX = "AF";
	public static final String COMPANY = "Auld Fellas Ltd.";

	// private fields
	private double price;
	private int discount;

	// getters
	public double getPrice() {
		return price;
	}
	public int getDiscount() {
		return discount;
	}

	// setters
	public void setPrice(double price) {
		this.price = price;
	}
	public void setDiscount(int discount) {
		this.discount = discount;
	}

	/**
	 * Quote generation:
	 * 30% discount for being male
	 * 2% discount per year over 60
	 * 20% discount for less than 3 penalty points
	 * 50% penalty (i.e. reduction in discount) for more than 60 penalty points 
	 */

	public Quotation generateQuotation(ClientInfo info) {
		
		// Create an initial quotation between 600 and 1200
		double initialPrice = generatePrice(600, 600);
		setPrice(initialPrice);
		
		// Automatic 30% discount for being male
		int initialDiscount = (info.getGender() == ClientInfo.MALE) ? 30:0;
		setDiscount(initialDiscount);
		
		// Automatic 2% discount per year over 60...
		int additionalDiscount = getDiscount();
		additionalDiscount += (info.getAge() > 60) ? (2*(info.getAge()-60)) : 0;
		
		// Add a points discount
		additionalDiscount += getPointsDiscount(info);
		setDiscount(additionalDiscount);
		
		// Generate the quotation and send it back
		return new Quotation(COMPANY, generateReference(PREFIX), (getPrice() * (100-getDiscount())) / 100);
	}

	private int getPointsDiscount(ClientInfo info) {
		if (info.getPoints() < 3) return 20;
		if (info.getPoints() <= 6) return 0;
		return -50;
		
	}


	// the below methods handles the POST request that is submitted to "/quotations/" URI.
	private Map<String, Quotation> quotations = new HashMap<>();

	@RequestMapping(value="/quotations",method=RequestMethod.POST)
	public ResponseEntity<Quotation> createQuotation(@RequestBody ClientInfo info) throws URISyntaxException{
		Quotation quotation = generateQuotation(info);
		quotations.put(quotation.getReference(), quotation);
	String path = ServletUriComponentsBuilder.fromCurrentContextPath().
		build().toUriString()+ "/quotations/"+quotation.getReference();
	HttpHeaders headers = new HttpHeaders();
	headers.setLocation(new URI(path));
	return new ResponseEntity<>(quotation, headers, HttpStatus.CREATED);
	}

	// Mechanism to get a representation of the quotation resource
	@RequestMapping(value="/quotations/{reference}",method=RequestMethod.GET)
	public Quotation getResource(@PathVariable("reference") String reference) {
		Quotation quotation = quotations.get(reference);
		if (quotation == null) throw new NoSuchQuotationException();
		return quotation;
	}

	// Annotation of the exception to indicate what the response code should be returned if the exceptoion occurs
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public class NoSuchQuotationException extends RuntimeException {
		static final long serialVersionUID = -6516152229878843037L;
	}
}
