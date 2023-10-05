package tests.user;

import application.AppControl;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;
import store.Inventory;
import user.Customer;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class CustomerTest
{
    // Static variables that will be used during testing
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final String membershipID = "1234";
    final BarcodedProduct product1 = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
    final PLUCodedProduct product2 = new PLUCodedProduct(new PriceLookupCode("1000"), "N/A", new BigDecimal("10.00"));
    final BarcodedProduct plasticBagProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.zero, Numeral.zero, Numeral.zero}), "Plastic Bag", new BigDecimal("0.1"), 1);
    final double product2Weight = 22.5;
    final BigDecimal product2ExpectedPrice = product2.getPrice().divide(new BigDecimal("1000.00")).multiply(BigDecimal.valueOf(product2Weight));
    final int plasticBags = 10;

    Customer customer;

    @Before
    public void setup()
    {
        customer = new Customer();
        Inventory.clear();
    }

    @Test
    public void typeTest()
    {
        assertEquals(AppControl.CUSTOMER, customer.getUserType());
    }

    @Test
    public void addAndGetCashBalanceTest()
    {
        assertEquals(BigDecimal.ZERO, customer.getCashBalance());

        customer.addCashBalance(new BigDecimal(banknoteDenominations[0]));
        customer.addCashBalance(coinDenominations[0]);

        assertEquals(coinDenominations[0].add(new BigDecimal(banknoteDenominations[0])), customer.getCashBalance());
    }

    @Test
    public void addAndGetPLUTest()
    {
        assertNull(customer.getPLU());

        customer.enterPLUCode(product2.getPLUCode());

        assertEquals(product2.getPLUCode(), customer.getPLU());
    }

    @Test
    public void setAndGetMembershipTest()
    {
        assertNull(customer.getMemberID());

        customer.setMemberID(membershipID);

        assertEquals(membershipID, customer.getMemberID());
    }

    @Test
    public void setAndGetUsingOwnBagsTest()
    {
        assertFalse(customer.getUseOwnBags());

        customer.setOwnBagsUsed(true);

        assertTrue(customer.getUseOwnBags());
    }

    @Test
    public void setAndGetPlasticBagsTest()
    {
        assertEquals(0, customer.getPlasticBags());

        customer.setPlasticBags(plasticBags);

        assertEquals(plasticBags, customer.getPlasticBags());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProductTest()
    {
        assertTrue(customer.getCart().isEmpty());
        assertTrue(customer.getCartEntries().isEmpty());

        customer.addProduct((Product) product1);
        customer.addProduct((Product) product2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInvalidProductTest()
    {
        assertTrue(customer.getCart().isEmpty());
        assertTrue(customer.getCartEntries().isEmpty());

        customer.addProduct((Product) null);
    }

    @Test
    public void hasSufficientCashBalanceTest()
    {
        customer.addProduct(product1);
        customer.addProduct(product2, product2Weight);

        assertFalse(customer.hasSufficientCashBalance());

        customer.addCashBalance(new BigDecimal(banknoteDenominations[banknoteDenominations.length - 1]));

        assertTrue(customer.hasSufficientCashBalance());
    }

    @Test
    public void cartTest()
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
        assertEquals(0, customer.getCartSubtotal().compareTo(product1.getPrice().add(product2ExpectedPrice)));

        customer.removeProduct(1);
        customer.removeProduct(customer.getCartEntries().get(0));

        assertTrue(customer.getCart().isEmpty());
        assertTrue(customer.getCartEntries().isEmpty());
        assertEquals(BigDecimal.ZERO, customer.getCartSubtotal());
    }

    @Test
    public void cartTest2()
    {
        assertTrue(customer.getCart().isEmpty());
        assertTrue(customer.getCartEntries().isEmpty());
        assertEquals(BigDecimal.ZERO, customer.getCartSubtotal());

        Inventory.addProduct(plasticBagProduct);
        customer.setPlasticBags(10);

        assertEquals(plasticBags, customer.getCart().size());
        assertEquals(plasticBags, customer.getCartEntries().size());
        assertEquals(0, customer.getCartSubtotal().compareTo(plasticBagProduct.getPrice().multiply(new BigDecimal(plasticBags))));

        customer.setPlasticBags(0);

        assertTrue(customer.getCart().isEmpty());
        assertTrue(customer.getCartEntries().isEmpty());
        assertEquals(BigDecimal.ZERO, customer.getCartSubtotal());
    }
}
