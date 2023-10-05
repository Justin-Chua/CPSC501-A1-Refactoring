package tests.store;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;
import store.Inventory;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class InventoryTest
{
    // Declare the products
    BarcodedProduct barcodedProduct;
    PLUCodedProduct pluCodedProduct;
    Product product1;
    Product product2;

    // Setup that is run before each test case
    @Before
    public void setup()
    {
        // Initialize the products
        barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero}), "N/A", new BigDecimal("5.00"), 15.50);
        pluCodedProduct = new PLUCodedProduct(new PriceLookupCode("1234"), "N/A", new BigDecimal("10.00"));
        product1 = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.one}), "N/A", new BigDecimal("15.00"), 20.75);
        product2 = new PLUCodedProduct(new PriceLookupCode("5678"), "N/A", new BigDecimal("20.00"));

        // Resets the inventory
        Inventory.clear();
    }

    @Test
    public void addAndGetProductTest()
    {
        assertNull(Inventory.getProduct(barcodedProduct.getBarcode()));
        assertNull(Inventory.getProduct(pluCodedProduct.getPLUCode()));
        assertNull(Inventory.getProduct(((BarcodedProduct) product1).getBarcode()));
        assertNull(Inventory.getProduct(((PLUCodedProduct) product2).getPLUCode()));
        assertTrue(Inventory.getBarcodedProducts().isEmpty());
        assertTrue(Inventory.getPLUProducts().isEmpty());
        assertFalse(Inventory.getProducts().contains(product1));
        assertFalse(Inventory.getProducts().contains(product2));

        Inventory.addProduct(barcodedProduct);
        Inventory.addProduct(pluCodedProduct);
        Inventory.addProduct(product1);
        Inventory.addProduct(product2);

        assertEquals(barcodedProduct, Inventory.getProduct(barcodedProduct.getBarcode()));
        assertEquals(pluCodedProduct, Inventory.getProduct(pluCodedProduct.getPLUCode()));
        assertEquals(product1, Inventory.getProduct(((BarcodedProduct) product1).getBarcode()));
        assertEquals(product2, Inventory.getProduct(((PLUCodedProduct) product2).getPLUCode()));
        assertEquals(2, Inventory.getBarcodedProducts().size());
        assertEquals(2, Inventory.getPLUProducts().size());
        assertTrue(Inventory.getProducts().contains(product1));
        assertTrue(Inventory.getProducts().contains(product2));
        assertTrue(Inventory.getBarcodedProducts().containsKey(barcodedProduct.getBarcode()));
        assertTrue(Inventory.getBarcodedProducts().containsKey(((BarcodedProduct) product1).getBarcode()));
        assertTrue(Inventory.getPLUProducts().containsKey(pluCodedProduct.getPLUCode()));
        assertTrue(Inventory.getPLUProducts().containsKey(((PLUCodedProduct) product2).getPLUCode()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInvalidProductTest()
    {
        Inventory.addProduct((Product) null);
    }

    @Test
    public void setAndGetQuantityTest()
    {
        Inventory.addProduct(barcodedProduct);
        Inventory.addProduct(pluCodedProduct);

        assertEquals(0, Inventory.getQuantity(barcodedProduct));
        assertEquals(0, Inventory.getQuantity(pluCodedProduct));

        Inventory.setQuantity(barcodedProduct, 1);
        Inventory.setQuantity(pluCodedProduct, 2);

        assertEquals(1, Inventory.getQuantity(barcodedProduct));
        assertEquals(2, Inventory.getQuantity(pluCodedProduct));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setQuantityUnsuccessfullyTest()
    {
        Inventory.setQuantity(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setQuantityUnsuccessfullyTest2()
    {
        Inventory.addProduct(barcodedProduct);

        Inventory.setQuantity(barcodedProduct, -1);
    }

    @Test
    public void clearInventoryTest()
    {
        assertNull(Inventory.getProduct(barcodedProduct.getBarcode()));
        assertNull(Inventory.getProduct(pluCodedProduct.getPLUCode()));

        Inventory.addProduct(barcodedProduct);
        Inventory.addProduct(pluCodedProduct);
        Inventory.clear();

        assertNull(Inventory.getProduct(barcodedProduct.getBarcode()));
        assertNull(Inventory.getProduct(pluCodedProduct.getPLUCode()));
    }
}
