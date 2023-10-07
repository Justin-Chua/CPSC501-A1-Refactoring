package tests.software;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import software.SelfCheckoutController;
import software.SelfCheckoutHandler;
import software.SelfCheckoutSoftware;

public class SelfCheckoutHandlerTest {
	
	SelfCheckoutStation scStation;
	SelfCheckoutSoftware scSoftware;
	SelfCheckoutHandler scHandler;
	
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
	public void testSelfCheckoutHandlerConstructor() {
		scHandler = new SelfCheckoutHandler(scSoftware);
		assertNotNull(scHandler.getBanknoteHandler());
		assertNotNull(scHandler.getCardHandler());
		assertNotNull(scHandler.getCoinHandler());
		assertNotNull(scHandler.getProcessItemHandler());
	}
	
	@Test
	public void testSelfCheckoutHandlerSetAllCustomers() {
		scHandler = new SelfCheckoutHandler(scSoftware);
		scHandler.setAllCustomers(scSoftware.getCustomer());
		assertEquals(scHandler.getBanknoteHandler().getCustomer(), scSoftware.getCustomer());
		assertEquals(scHandler.getCoinHandler().getCustomer(), scSoftware.getCustomer());
	}
	
	@Test
	public void testSelfCheckoutHandlerEnableAll() {
		scHandler = new SelfCheckoutHandler(scSoftware);
		scHandler.enableAll();
		
        assertFalse(scStation.banknoteInput.isDisabled());
        assertFalse(scStation.banknoteOutput.isDisabled());
        assertFalse(scStation.banknoteStorage.isDisabled());
        assertFalse(scStation.banknoteValidator.isDisabled());
        
        assertFalse(scStation.coinSlot.isDisabled());
        assertFalse(scStation.coinTray.isDisabled());
        assertFalse(scStation.coinStorage.isDisabled());
        assertFalse(scStation.coinValidator.isDisabled());
		
	}
	
	@Test
	public void testSelfCheckoutHandlerDisableAll() {
		scHandler = new SelfCheckoutHandler(scSoftware);
		scHandler.disableAll();
		
        assertTrue(scStation.banknoteInput.isDisabled());
        assertTrue(scStation.banknoteOutput.isDisabled());
        assertTrue(scStation.banknoteStorage.isDisabled());
        assertTrue(scStation.banknoteValidator.isDisabled());
        
        assertTrue(scStation.coinSlot.isDisabled());
        assertTrue(scStation.coinTray.isDisabled());
        assertTrue(scStation.coinStorage.isDisabled());
        assertTrue(scStation.coinValidator.isDisabled());
		
	}
	
	@Test
	public void testSelfCheckoutHandlerResetHandlers() {
		scHandler = new SelfCheckoutHandler(scSoftware);
		scHandler.resetHandlers();
		
		assertNull(scHandler.getBanknoteHandler());
		assertNull(scHandler.getCardHandler());
		assertNull(scHandler.getCoinHandler());
		assertNull(scHandler.getProcessItemHandler());
		
	}
}
