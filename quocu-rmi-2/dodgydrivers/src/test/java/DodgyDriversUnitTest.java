import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DodgyDriversUnitTest {
    private static Registry registry;

    @BeforeClass
    public static void setup() {
        QuotationService ddqService = new DDQService();
        try {
            registry = LocateRegistry.createRegistry(1099);

            QuotationService quotationService = (QuotationService)
                UnicastRemoteObject.exportObject(ddqService,0);

            registry.bind(Constants.DODGY_DRIVERS_SERVICE, quotationService);
        } catch (Exception e) {
            System.out.println("Trouble: " + e);
        }
    }
    @Test
    public void connectionTest() throws Exception {
        QuotationService service = (QuotationService)
            registry.lookup(Constants.DODGY_DRIVERS_SERVICE);
        assertNotNull(service);
    }

    @Test
    public void generationTest() throws Exception {
        QuotationService service = (QuotationService)
            registry.lookup(Constants.DODGY_DRIVERS_SERVICE);

        ClientInfo info = new ClientInfo("Test", 'F', 30, 0, 0, "122-d-7890");
        
        Quotation quotation = service.generateQuotation(info);
        assertEquals("The price is correct", 800, quotation.price, 1000);
    }
}
