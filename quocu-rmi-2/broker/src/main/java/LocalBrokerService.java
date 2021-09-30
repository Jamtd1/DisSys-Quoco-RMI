import java.util.LinkedList;
import java.util.List;

import java.rmi.registry.Registry;

/**
 * Implementation of the broker service that uses the Service Registry.
 * 
 * @author Rem
 *
 */
public class LocalBrokerService implements BrokerService {
	public Registry registry ;

	public List<Quotation> getQuotations(ClientInfo info) {
		List<Quotation> quotations = new LinkedList<Quotation>();
		try {
			for (String name : this.registry.list()) {
				if (name.startsWith("qs-")) {
					QuotationService service = (QuotationService) this.registry.lookup(name);
					quotations.add(service.generateQuotation(info));
				}
			}
		}catch (Exception e) {
			System.out.println("Trouble: " + e);
		}

		return quotations;
	}

	public void registerService(String name, java.rmi.Remote service) {
		try {
			registry.bind(name, service);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
