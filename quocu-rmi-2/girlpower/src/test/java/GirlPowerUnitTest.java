import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

// import service.core.Constants;
// import service.core.QuotationService;
// import service.auldfellas.AFQService;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GirlPowerUnitTest {
    private static Registry registry;

    @BeforeClass
    public static void setup() {
        QuotationService gpqService = new GPQService();
        try {
            registry = LocateRegistry.createRegistry(1099);

            QuotationService quotationService = (QuotationService)
                UnicastRemoteObject.exportObject(gpqService,0);

            registry.bind(Constants.GIRL_POWER_SERVICE, quotationService);
        } catch (Exception e) {
            System.out.println("Trouble: " + e);
        }
    }
    @Test
    public void connectionTest() throws Exception {
        QuotationService service = (QuotationService)
            registry.lookup(Constants.GIRL_POWER_SERVICE);
        assertNotNull(service);
    }

    @Test
    public void generationTest() throws Exception {
        QuotationService service = (QuotationService)
            registry.lookup(Constants.GIRL_POWER_SERVICE);

        ClientInfo info = new ClientInfo("Test", 'F', 30, 0, 0, "122-d-7890");
        
        Quotation quotation = service.generateQuotation(info);
        assertEquals("The price is correct", 180, quotation.price, 300);
    }
}
