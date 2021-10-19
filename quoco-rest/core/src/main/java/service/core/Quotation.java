package service.core;

import java.io.Serializable;

/**
 * Class to store the quotations returned by the quotation services
 * 
 * @author Rem
 *
 */
public class Quotation implements Serializable {
	public Quotation(String company, String reference, double price) {
		this.company = company;
		this.reference = reference;
		this.price = price;
		
	}
	
	// Default constructor
	public Quotation() {}

	/**
	 * In order to convert Quotation to a JavaBean the fields are required to be private 
	 * Since the fields are private getters and setters are required to assign the variables to them
	 */

	private String company;
	private String reference;
	private double price;

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference){
		this.reference = reference;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
