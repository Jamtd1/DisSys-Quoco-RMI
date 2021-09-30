import java.util.List;

//import the rmi Remote packages
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for defining the behaviours of the broker service
 * @author Rem
 *
 */
public interface BrokerService extends Remote {
	public List<Quotation> getQuotations(ClientInfo info) throws RemoteException;
	public void registerService(String name, java.rmi.Remote service) throws Exception;
}
