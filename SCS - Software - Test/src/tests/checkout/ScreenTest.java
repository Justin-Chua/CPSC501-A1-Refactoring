package tests.checkout;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import checkout.Screen;
import software.SelfCheckoutSoftware;
import user.Customer;

public class ScreenTest { 
    Currency currency = Currency.getInstance("USD");
    int[] banknoteDenominations = {1, 5, 10, 25, 100};
    BigDecimal[] coinDenominations = {new BigDecimal("0.01"), new BigDecimal("0.05"), new BigDecimal("0.1"), new BigDecimal("0.25"), new BigDecimal("1.00")};
    int scaleMaximumWeight = 10;
    int scaleSensitivity = 1;
    SelfCheckoutStation selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
    SelfCheckoutSoftware scs = new SelfCheckoutSoftware(selfCheckoutStation);

	@Test
	public void customerTest() {
		Screen screen = new Screen(scs);
        Customer customer = new Customer();
        screen.setCustomer(customer);
		assertTrue(screen.getCustomer().equals(customer));
	}

	@Test
	public void disableHardwareTest() {
		Screen screen = new Screen(scs);
		screen.disableHardware();
		assertTrue(selfCheckoutStation.screen.isDisabled());
	}
}
