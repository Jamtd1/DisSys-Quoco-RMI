package service.girlpower;

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
 * Implementation of the Girl Power insurance quotation service.
 * 
 * @author Rem
 *
 */
@RestController
public class GPQService extends AbstractQuotationService implements Serializable {

	// default constructor
	public GPQService() {}

	// All references are to be prefixed with an DD (e.g. DD001000)
	public static final String PREFIX = "GP";
	public static final String COMPANY = "Girl Power Inc.";
	
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
	 * 50% discount for being female
	 * 20% discount for no penalty points
	 * 15% discount for < 3 penalty points
	 * no discount for 3-5 penalty points
	 * 100% penalty for > 5 penalty points
	 * 5% discount per year no claims
	 */

	public Quotation generateQuotation(ClientInfo info) {
		// Create an initial quotation between 600 and 1000
		double initialPrice = generatePrice(600, 400);
		setPrice(initialPrice);
		
		// Automatic 50% discount for being female
		int initialDiscount = (info.getGender() == ClientInfo.FEMALE) ? 50:0;
		setDiscount(initialDiscount);

		// Add a points discount
		int additionalDiscount = getDiscount();
		additionalDiscount += getPointsDiscount(info);
		
		// Add a no claims discount
		additionalDiscount += getNoClaimsDiscount(info);
		setDiscount(additionalDiscount);
		
		// Generate the quotation and send it back
		return new Quotation(COMPANY, generateReference(PREFIX), (getPrice() * (100-getDiscount())) / 100);
	}

	private int getNoClaimsDiscount(ClientInfo info) {
		return 5*info.getNoClaims();
	}

	private int getPointsDiscount(ClientInfo info) {
		if (info.getPoints() == 0) return 20;
		if (info.getPoints() < 3) return 15;
		if (info.getPoints() < 6) return 0;
		return -100;
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
