import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class AuldfellasUnitTest {
    private static Registry registry;
    @BeforeClass
    public static void setup() {
        QuotationService afqService = new AFQService();
        try {
            registry = LocateRegistry.createRegistry(1099);
            QuotationService quotationService = (QuotationService)
                UnicastRemoteObject.exportObject(afqService,0);
            registry.bind(Constants.AULD_FELLAS_SERVICE, quotationService);
        } catch (Exception e) {
            System.out.println("Trouble: " + e);
        }
    }
    @Test
    public void connectionTest() throws Exception {
        QuotationService service = (QuotationService)
            registry.lookup(Constants.AULD_FELLAS_SERVICE);
        assertNotNull(service);
    }

    @Test
    public void generationTest() throws Exception {
        QuotationService service = (QuotationService)
            registry.lookup(Constants.AULD_FELLAS_SERVICE);

        ClientInfo info = new ClientInfo("Test", 'F', 30, 0, 0, "122-d-7890");
        
        Quotation quotation = service.generateQuotation(info);
        assertEquals("The price is correct", 480, quotation.price, 480);
    }
}