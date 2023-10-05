package tests.software;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import software.SelfCheckoutSoftware;
import software.observers.SelfCheckoutObserver;
import store.Inventory;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SoftwareTest
{
    // Static variables that will be used during testing
    final Currency currency = Currency.getInstance("CAD");
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final int scaleMaximumWeight = 100;
    final int scaleSensitivity = 10;
    BarcodedProduct product1 = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "Milk", new BigDecimal("5.00"), 17.5);
    PLUCodedProduct product2 = new PLUCodedProduct(new PriceLookupCode("1000"), "Cookies", new BigDecimal("10.00"));

    SelfCheckoutStation selfCheckoutStation;
    SelfCheckoutSoftware selfCheckoutSoftware;

    @Before
    public void setup()
    {
        selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);

        Inventory.clear();
        Inventory.addProduct(product1);
        Inventory.addProduct(product2);
    }

    @Test
    public void observersTest()
    {
        SelfCheckoutObserver selfCheckoutObserver = new SelfCheckoutObserver()
        {
            @Override
            public Void invalidBanknoteDetected()
            {
                return null;
            }

            @Override
            public Void banknoteAdded()
            {
                return null;
            }

            @Override
            public Void banknoteStorageFull()
            {
                return null;
            }

            @Override
            public Void banknoteDispenserEmpty()
            {
                return null;
            }

            @Override
            public Void invalidCoinDetected()
            {
                return null;
            }

            @Override
            public Void coinAdded()
            {
                return null;
            }

            @Override
            public Void coinStorageFull()
            {
                return null;
            }

            @Override
            public Void coinDispenserEmpty()
            {
                return null;
            }

            @Override
            public Void invalidCardTypeDetected()
            {
                return null;
            }

            @Override
            public Void cardTransactionSucceeded()
            {
                return null;
            }

            @Override
            public Void invalidGiftCardDetected()
            {
                return null;
            }

            @Override
            public Void invalidMembershipCardDetected()
            {
                return null;
            }

            @Override
            public Void membershipCardDetected(String memberID)
            {
                return null;
            }

            @Override
            public Void paymentHoldingAuthorizationFailed()
            {
                return null;
            }

            @Override
            public Void paymentPostingTransactionFailed()
            {
                return null;
            }

            @Override
            public Void paymentCompleted()
            {
                return null;
            }

            @Override
            public Void placeInBaggingAreaBlocked()
            {
                return null;
            }

            @Override
            public Void placeInBaggingAreaUnblocked()
            {
                return null;
            }

            @Override
            public Void weightDiscrepancyInBaggingAreaDetected()
            {
                return null;
            }

            @Override
            public Void weightDiscrepancyInBaggingAreaResolved()
            {
                return null;
            }

            @Override
            public Void productNotFound()
            {
                return null;
            }

            @Override
            public Void productCannotFound()
            {
                return null;
            }

            @Override
            public Void softwareStarted(SelfCheckoutSoftware scss)
            {
                return null;
            }

            @Override
            public Void softwareStopped(SelfCheckoutSoftware scss)
            {
                return null;
            }

            @Override
            public Void touchScreenBlocked()
            {
                return null;
            }

            @Override
            public Void touchScreenUnblocked()
            {
                return null;
            }

            @Override
            public Void phaseChanged(SelfCheckoutSoftware.Phase phase)
            {
                return null;
            }
        };

        assertTrue(selfCheckoutSoftware.getObservers().isEmpty());

        selfCheckoutSoftware.addObserver(selfCheckoutObserver);

        assertEquals(1, selfCheckoutSoftware.getObservers().size());
        assertTrue(selfCheckoutSoftware.getObservers().contains(selfCheckoutObserver));

        selfCheckoutSoftware.notifyObservers(observer -> observer.softwareStarted(selfCheckoutSoftware));

        assertEquals(1, selfCheckoutSoftware.getObservers().size());
        assertTrue(selfCheckoutSoftware.getObservers().contains(selfCheckoutObserver));

        selfCheckoutSoftware.removeObserver(selfCheckoutObserver);

        assertTrue(selfCheckoutSoftware.getObservers().isEmpty());
    }

    @Test
    public void lookupProductTest()
    {
        assertEquals(1, selfCheckoutSoftware.lookupProduct(product1.getDescription()).size());
        assertEquals(1, selfCheckoutSoftware.lookupProduct(product2.getDescription()).size());
        assertEquals(product1, selfCheckoutSoftware.lookupProduct(product1.getDescription()).get(0));
        assertEquals(product2, selfCheckoutSoftware.lookupProduct(product2.getDescription()).get(0));

        assertEquals(1, selfCheckoutSoftware.lookupProduct(product1.getDescription().substring(1, product1.getDescription().length() - 1)).size());
        assertEquals(1, selfCheckoutSoftware.lookupProduct(product2.getDescription().substring(1, product2.getDescription().length() - 1)).size());
        assertEquals(product1, selfCheckoutSoftware.lookupProduct(product1.getDescription().substring(1, product1.getDescription().length() - 1)).get(0));
        assertEquals(product2, selfCheckoutSoftware.lookupProduct(product2.getDescription().substring(1, product2.getDescription().length() - 1)).get(0));

        assertEquals(2, selfCheckoutSoftware.lookupProduct("").size());
        assertTrue(selfCheckoutSoftware.lookupProduct("").contains(product1));
        assertTrue(selfCheckoutSoftware.lookupProduct("").contains(product2));

        assertTrue(selfCheckoutSoftware.lookupProduct(product1.getDescription() + " & " + product2.getDescription()).isEmpty());
    }
}
