package tests.software;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;

import software.SelfCheckoutController;
import software.SelfCheckoutSoftware;

public class SelfCheckoutControllerTest {
	
	SelfCheckoutStation scStation;
	SelfCheckoutSoftware scSoftware;
	SelfCheckoutController scController;
	
	@Before
	public void setup() {
		 // Static variables that will be used during testing
	    final Currency currency = Currency.getInstance("CAD");
	    final int[] banknoteDenominations = {5, 10, 20, 50};
	    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
	    final int scaleMaximumWeight = 100;
	    final int scaleSensitivity = 10;
	    
        scStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        scSoftware = new SelfCheckoutSoftware(scStation);
	}
	
	@Test
	public void testSelfCheckoutControllerConstructor() {
		scController = new SelfCheckoutController(scSoftware);
		assertNotNull(scController.getCheckout());
		assertNotNull(scController.getReceipt());
		assertNotNull(scController.getScreen());
	}
	
	@Test
	public void testSelfCheckoutControllerSetAllCustomers() {
		scController = new SelfCheckoutController(scSoftware);
		scController.setAllCustomers(scSoftware.getCustomer());
		assertEquals(scController.getCheckout().getCustomer(), scSoftware.getCustomer());
		assertEquals(scController.getReceipt().getCustomer(), scSoftware.getCustomer());
		assertEquals(scController.getScreen().getCustomer(), scSoftware.getCustomer());
	}
	
	@Test
	public void testSelfCheckoutControllerResetControllers() {
		scController = new SelfCheckoutController(scSoftware);
		scController.resetControllers();
		assertNull(scController.getCheckout());
		assertNull(scController.getReceipt());
		assertNull(scController.getScreen());
	}
}
