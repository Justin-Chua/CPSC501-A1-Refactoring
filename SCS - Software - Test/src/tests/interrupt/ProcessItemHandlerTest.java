package tests.interrupt;

import interrupt.ProcessItemHandler;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import store.Inventory;
import store.Store;
import user.Customer;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProcessItemHandlerTest
{
    // Static variables that will be used during testing
    final Currency currency = Currency.getInstance("CAD");
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final int scaleMaximumWeight = 100;
    final int scaleSensitivity = 10;
    BarcodedProduct product1 = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
    PLUCodedProduct product2 = new PLUCodedProduct(new PriceLookupCode("1000"), "N/A", new BigDecimal("10.00"));

    SelfCheckoutStation selfCheckoutStation;
    SelfCheckoutSoftware selfCheckoutSoftware;
    SupervisionStation supervisionStation;
    SupervisionSoftware supervisionSoftware;

    ProcessItemHandler processItemHandler;
    Customer customer;

    @Before
    public void setup()
    {
        Inventory.clear();
        Inventory.addProduct(product1);
        Inventory.addProduct(product2);
        selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);
        supervisionStation = new SupervisionStation();
        supervisionSoftware = new SupervisionSoftware(supervisionStation);
        supervisionSoftware.add(selfCheckoutSoftware);
        processItemHandler = new ProcessItemHandler(selfCheckoutSoftware);
        customer = new Customer();
        processItemHandler.setCustomer(customer);
        Store.setSupervisionSoftware(supervisionSoftware);
        Store.addSelfCheckoutSoftware(selfCheckoutSoftware);
    }

    @Test
    public void attachAndDetachTest()
    {
        selfCheckoutStation.mainScanner.detach(processItemHandler);
        selfCheckoutStation.handheldScanner.detach(processItemHandler);
        selfCheckoutStation.scanningArea.detach(processItemHandler);
        selfCheckoutStation.baggingArea.detach(processItemHandler);

        assertFalse(selfCheckoutStation.mainScanner.detach(processItemHandler));
        assertFalse(selfCheckoutStation.handheldScanner.detach(processItemHandler));
        assertFalse(selfCheckoutStation.scanningArea.detach(processItemHandler));
        assertFalse(selfCheckoutStation.baggingArea.detach(processItemHandler));

        processItemHandler.attachAll();

        assertTrue(selfCheckoutStation.mainScanner.detach(processItemHandler));
        assertTrue(selfCheckoutStation.handheldScanner.detach(processItemHandler));
        assertTrue(selfCheckoutStation.scanningArea.detach(processItemHandler));
        assertTrue(selfCheckoutStation.baggingArea.detach(processItemHandler));

        processItemHandler.attachAll();
        processItemHandler.detatchAll();

        assertFalse(selfCheckoutStation.mainScanner.detach(processItemHandler));
        assertFalse(selfCheckoutStation.handheldScanner.detach(processItemHandler));
        assertFalse(selfCheckoutStation.scanningArea.detach(processItemHandler));
        assertFalse(selfCheckoutStation.baggingArea.detach(processItemHandler));
    }

    @Test
    public void enableAndDisableHardwareTest()
    {
        assertFalse(selfCheckoutStation.mainScanner.isDisabled());
        assertFalse(selfCheckoutStation.handheldScanner.isDisabled());
        assertFalse(selfCheckoutStation.scanningArea.isDisabled());
        assertFalse(selfCheckoutStation.baggingArea.isDisabled());

        processItemHandler.disableHardware();

        assertTrue(selfCheckoutStation.mainScanner.isDisabled());
        assertTrue(selfCheckoutStation.handheldScanner.isDisabled());
        assertTrue(selfCheckoutStation.scanningArea.isDisabled());
        assertTrue(selfCheckoutStation.baggingArea.isDisabled());

        processItemHandler.enableHardware();

        assertFalse(selfCheckoutStation.mainScanner.isDisabled());
        assertFalse(selfCheckoutStation.handheldScanner.isDisabled());
        assertFalse(selfCheckoutStation.scanningArea.isDisabled());
        assertFalse(selfCheckoutStation.baggingArea.isDisabled());
    }

    @Test
    public void enableBaggingAreaTest()
    {
        selfCheckoutStation.baggingArea.disable();

        assertTrue(selfCheckoutStation.baggingArea.isDisabled());

        processItemHandler.enableBaggingArea();

        assertFalse(selfCheckoutStation.baggingArea.isDisabled());
    }

    @Test
    public void barcodeScannedTest()
    {
        selfCheckoutSoftware.addItem();
        processItemHandler.barcodeScanned(selfCheckoutStation.mainScanner, product1.getBarcode());
    }

    @Test
    public void barcodeScannedFailTest()
    {
        processItemHandler.setCustomer(null);
        processItemHandler.barcodeScanned(selfCheckoutStation.mainScanner, product1.getBarcode());
    }

    @Test
    public void barcodeScannedFailTest2()
    {
        processItemHandler.barcodeScanned(selfCheckoutStation.mainScanner, new Barcode(new Numeral[] {Numeral.one}));
    }

    @Test
    public void weightChangedTest()
    {
        processItemHandler.weightChanged(selfCheckoutStation.baggingArea, scaleMaximumWeight * 2);
    }

    @Test
    public void weightChangedTest2()
    {
        selfCheckoutSoftware.addPLUItem();

        processItemHandler.weightChanged(selfCheckoutStation.scanningArea, scaleMaximumWeight * 2);

        selfCheckoutSoftware.idle();

        processItemHandler.weightChanged(selfCheckoutStation.scanningArea, scaleMaximumWeight * 2);
    }

    @Test
    public void weightChangedTest3()
    {
        selfCheckoutStation.scanningArea.add(new BarcodedItem(product1.getBarcode(), scaleMaximumWeight * 2));

        processItemHandler.weightChanged(selfCheckoutStation.scanningArea, scaleMaximumWeight * 2);
    }

    @Test
    public void weightChangedTest4()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.CASH);
        selfCheckoutSoftware.paymentCompleted();

        processItemHandler.weightChanged(selfCheckoutStation.baggingArea, 0);
    }

    @Test
    public void weightChangedTest5()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.CASH);
        selfCheckoutSoftware.paymentCompleted();

        processItemHandler.weightChanged(selfCheckoutStation.baggingArea, product1.getExpectedWeight());
    }

    @Test
    public void weightChangedTest6()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.addOwnBag();

        processItemHandler.weightChanged(selfCheckoutStation.baggingArea, 2);
    }

    @Test
    public void weightChangedTest7()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.weightDiscrepancy();

        processItemHandler.weightChanged(selfCheckoutStation.baggingArea, product1.getExpectedWeight());
        processItemHandler.weightChanged(selfCheckoutStation.baggingArea, 0);
    }

    @Test
    public void weightChangedTest8()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.bagItem();

        processItemHandler.weightChanged(selfCheckoutStation.baggingArea, product1.getExpectedWeight());
    }

    @Test
    public void weightChangedTest9()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.bagItem();

        processItemHandler.weightChanged(selfCheckoutStation.baggingArea, 0);
    }

    @Test
    public void overloadTest()
    {
        processItemHandler.overload(selfCheckoutStation.baggingArea);
        assertTrue(selfCheckoutStation.mainScanner.isDisabled());
    }

    @Test
    public void outOfOverloadTest()
    {
        processItemHandler.outOfOverload(selfCheckoutStation.baggingArea);
        assertFalse(selfCheckoutStation.mainScanner.isDisabled());
    }

    @Test
    public void overrideWeightTest()
    {
        selfCheckoutStation.baggingArea.add(new BarcodedItem(product1.getBarcode(), product1.getExpectedWeight()));
        processItemHandler.overrideWeight();
    }

    @Test
    public void overrideWeightFailTest()
    {
        selfCheckoutStation.baggingArea.add(new BarcodedItem(product1.getBarcode(), scaleMaximumWeight * 2));
        processItemHandler.overrideWeight();
    }
}
