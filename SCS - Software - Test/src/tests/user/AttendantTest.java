package tests.user;

import application.AppControl;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import software.SelfCheckoutSoftware;
import store.Inventory;
import user.Attendant;
import user.Customer;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.*;

public class AttendantTest
{
    // Static variables that will be used during testing
    final Currency currency = Currency.getInstance("CAD");
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final int scaleMaximumWeight = 100;
    final int scaleSensitivity = 10;
    final String username = "username";
    final String password = "password";
    BarcodedProduct product1 = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
    PLUCodedProduct product2 = new PLUCodedProduct(new PriceLookupCode("1000"), "N/A", new BigDecimal("10.00"));
    final double product2Weight = 22.5;

    SelfCheckoutStation selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
    SelfCheckoutSoftware selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);

    Customer customer;
    Attendant attendant;

    @Before
    public void setup()
    {
        customer = new Customer();
        attendant = new Attendant();

        selfCheckoutSoftware.setUser(customer);
        selfCheckoutSoftware.setUser(attendant);

        Inventory.clear();
    }

    @Test
    public void typeTest()
    {
        assertEquals(AppControl.ATTENDANT, attendant.getUserType());
    }

    @Test
    public void setAndGetLoginTest()
    {
        assertNull(attendant.getUsername());
        assertNull(attendant.getPassword());

        attendant.setLogin(username, password);

        assertEquals(username, attendant.getUsername());
        assertEquals(password, attendant.getPassword());
    }

    @Test
    public void removeProductTest()
    {
        assertTrue(customer.getCart().isEmpty());
        assertTrue(customer.getCartEntries().isEmpty());
        assertEquals(BigDecimal.ZERO, customer.getCartSubtotal());

        Inventory.addProduct(product1);
        Inventory.addProduct(product2);
        customer.addProduct(product1);
        customer.addProduct(product2, product2Weight);

        assertTrue(customer.getCart().contains(product1));
        assertTrue(customer.getCart().contains(product2));
        assertEquals(2, customer.getCart().size());
        assertEquals(2, customer.getCartEntries().size());

        attendant.removeProduct(selfCheckoutSoftware, 1);
        attendant.removeProduct(selfCheckoutSoftware, customer.getCartEntries().get(0));

        assertTrue(customer.getCart().isEmpty());
        assertTrue(customer.getCartEntries().isEmpty());
    }

    @Test
    public void setAndGetUnexpectedItemDecisionTest()
    {
        assertFalse(attendant.getUnexpectedItemDecision());

        attendant.setUnexpectedItemDecision(true);

        assertTrue(attendant.getUnexpectedItemDecision());

        attendant.setUnexpectedItemDecision(false);

        assertFalse(attendant.getUnexpectedItemDecision());
    }
}
