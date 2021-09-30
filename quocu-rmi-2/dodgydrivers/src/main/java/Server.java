import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static void main(String[] args) {
        QuotationService ddqService = new DDQService();
        try {
            // Connect to the RMI Registry - creating the registry will be the
            // responsibility of the broker.
            Registry registry = null;
            if (args.length == 0) {
                System.out.println("The are no arguments, create a registry");
                registry = LocateRegistry.createRegistry(1099);
            } else {
                System.out.println("there are " + args.length + "args");
                registry = LocateRegistry.getRegistry(args[0], 1099);
            }

            // Create the Remote Object
            QuotationService quotationService = (QuotationService)
            UnicastRemoteObject.exportObject(ddqService,0);
            
            // Register the object with the RMI Registry
            registry.bind(Constants.DODGY_DRIVERS_SERVICE, quotationService);
            
            System.out.println("STOPPING SERVER SHUTDOWN");
            while (true) {Thread.sleep(1000); }
        
        } catch (Exception e) {
            System.out.println("Trouble: " + e);
        }
    }
}