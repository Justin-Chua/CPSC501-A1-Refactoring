package tests.software;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import store.Inventory;
import store.Store;
import user.Attendant;
import user.Customer;
import user.User;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.*;

public class SelfCheckoutSoftwareTest
{
    // Static variables that will be used during testing
    final Currency currency = Currency.getInstance("CAD");
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final int scaleMaximumWeight = 100;
    final int scaleSensitivity = 10;
    BarcodedProduct product = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);

    SelfCheckoutStation selfCheckoutStation;
    SelfCheckoutSoftware selfCheckoutSoftware;
    SupervisionStation supervisionStation;
    SupervisionSoftware supervisionSoftware;

    Customer customer;
    Attendant attendant;

    @Before
    public void setup()
    {
        selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);
        supervisionStation = new SupervisionStation();
        supervisionSoftware = new SupervisionSoftware(supervisionStation);

        customer = new Customer();
        attendant = new Attendant();

        Store.setSupervisionSoftware(supervisionSoftware);
        Store.addSelfCheckoutSoftware(selfCheckoutSoftware);

        Inventory.clear();
        Inventory.addProduct(product);
    }

    @Test
    public void constructorTest()
    {
        selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);

        assertEquals(SelfCheckoutSoftware.Phase.IDLE, selfCheckoutSoftware.getPhase());
        assertEquals(selfCheckoutStation, selfCheckoutSoftware.getSelfCheckoutStation());
    }

    @Test
    public void setAndGetAndRemoveCustomerTest()
    {
        assertNull(selfCheckoutSoftware.getCustomer());

        selfCheckoutSoftware.setUser(customer);

        assertEquals(customer, selfCheckoutSoftware.getCustomer());

        selfCheckoutSoftware.removeUser(customer);

        assertNull(selfCheckoutSoftware.getCustomer());
    }

    @Test
    public void setAndGetAndRemoveAttendantTest()
    {
        assertNull(selfCheckoutSoftware.getAttendant());

        selfCheckoutSoftware.setUser(attendant);

        assertEquals(attendant, selfCheckoutSoftware.getAttendant());

        selfCheckoutSoftware.removeUser(attendant);

        assertNull(selfCheckoutSoftware.getAttendant());
    }

    @Test
    public void setAndRemoveInvalidUserTest()
    {
        assertNull(selfCheckoutSoftware.getCustomer());
        assertNull(selfCheckoutSoftware.getAttendant());

        selfCheckoutSoftware.setUser((User) null);

        assertNull(selfCheckoutSoftware.getCustomer());
        assertNull(selfCheckoutSoftware.getAttendant());

        selfCheckoutSoftware.removeUser((User) null);

        assertNull(selfCheckoutSoftware.getCustomer());
        assertNull(selfCheckoutSoftware.getAttendant());
    }

    @Test
    public void updatePaperAndInkUsedTest()
    {
        selfCheckoutSoftware.getPaperUsed();
        selfCheckoutSoftware.getInkUsed();
        selfCheckoutSoftware.resetPaperUsed();
        selfCheckoutSoftware.resetInkUsed();
    }

    @Test(expected = IllegalStateException.class)
    public void makeChangeTest()
    {
        selfCheckoutSoftware.makeChange();
    }

    @Test
    public void enableAndDisableHardwareTest()
    {
        assertTrue(selfCheckoutStation.banknoteInput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteOutput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteStorage.isDisabled());
        assertTrue(selfCheckoutStation.banknoteValidator.isDisabled());
        assertTrue(selfCheckoutStation.cardReader.isDisabled());
        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
        assertTrue(selfCheckoutStation.coinTray.isDisabled());
        assertTrue(selfCheckoutStation.coinStorage.isDisabled());
        assertTrue(selfCheckoutStation.coinValidator.isDisabled());
        assertTrue(selfCheckoutStation.mainScanner.isDisabled());
        assertTrue(selfCheckoutStation.handheldScanner.isDisabled());
        assertTrue(selfCheckoutStation.scanningArea.isDisabled());
        assertTrue(selfCheckoutStation.baggingArea.isDisabled());

        selfCheckoutSoftware.enableHardware();

        assertFalse(selfCheckoutStation.banknoteInput.isDisabled());
        assertFalse(selfCheckoutStation.banknoteOutput.isDisabled());
        assertFalse(selfCheckoutStation.banknoteStorage.isDisabled());
        assertFalse(selfCheckoutStation.banknoteValidator.isDisabled());
        assertFalse(selfCheckoutStation.cardReader.isDisabled());
        assertFalse(selfCheckoutStation.coinSlot.isDisabled());
        assertFalse(selfCheckoutStation.coinTray.isDisabled());
        assertFalse(selfCheckoutStation.coinStorage.isDisabled());
        assertFalse(selfCheckoutStation.coinValidator.isDisabled());
        assertFalse(selfCheckoutStation.mainScanner.isDisabled());
        assertFalse(selfCheckoutStation.handheldScanner.isDisabled());
        assertFalse(selfCheckoutStation.scanningArea.isDisabled());
        assertFalse(selfCheckoutStation.baggingArea.isDisabled());

        selfCheckoutSoftware.disableHardware();

        assertTrue(selfCheckoutStation.banknoteInput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteOutput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteStorage.isDisabled());
        assertTrue(selfCheckoutStation.banknoteValidator.isDisabled());
        assertTrue(selfCheckoutStation.cardReader.isDisabled());
        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
        assertTrue(selfCheckoutStation.coinTray.isDisabled());
        assertTrue(selfCheckoutStation.coinStorage.isDisabled());
        assertTrue(selfCheckoutStation.coinValidator.isDisabled());
        assertTrue(selfCheckoutStation.mainScanner.isDisabled());
        assertTrue(selfCheckoutStation.handheldScanner.isDisabled());
        assertTrue(selfCheckoutStation.scanningArea.isDisabled());
        assertTrue(selfCheckoutStation.baggingArea.isDisabled());
    }

    @Test
    public void startAndStopSystemTest()
    {
        assertTrue(selfCheckoutStation.banknoteInput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteOutput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteStorage.isDisabled());
        assertTrue(selfCheckoutStation.banknoteValidator.isDisabled());
        assertTrue(selfCheckoutStation.cardReader.isDisabled());
        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
        assertTrue(selfCheckoutStation.coinTray.isDisabled());
        assertTrue(selfCheckoutStation.coinStorage.isDisabled());
        assertTrue(selfCheckoutStation.coinValidator.isDisabled());
        assertTrue(selfCheckoutStation.mainScanner.isDisabled());
        assertTrue(selfCheckoutStation.handheldScanner.isDisabled());
        assertTrue(selfCheckoutStation.scanningArea.isDisabled());
        assertTrue(selfCheckoutStation.baggingArea.isDisabled());

        selfCheckoutSoftware.startSystem();

        assertFalse(selfCheckoutStation.banknoteInput.isDisabled());
        assertFalse(selfCheckoutStation.banknoteOutput.isDisabled());
        assertFalse(selfCheckoutStation.banknoteStorage.isDisabled());
        assertFalse(selfCheckoutStation.banknoteValidator.isDisabled());
        assertFalse(selfCheckoutStation.cardReader.isDisabled());
        assertFalse(selfCheckoutStation.coinSlot.isDisabled());
        assertFalse(selfCheckoutStation.coinTray.isDisabled());
        assertFalse(selfCheckoutStation.coinStorage.isDisabled());
        assertFalse(selfCheckoutStation.coinValidator.isDisabled());
        assertFalse(selfCheckoutStation.mainScanner.isDisabled());
        assertFalse(selfCheckoutStation.handheldScanner.isDisabled());
        assertFalse(selfCheckoutStation.scanningArea.isDisabled());
        assertFalse(selfCheckoutStation.baggingArea.isDisabled());

        selfCheckoutSoftware.stopSystem();

        assertTrue(selfCheckoutStation.banknoteInput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteOutput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteStorage.isDisabled());
        assertTrue(selfCheckoutStation.banknoteValidator.isDisabled());
        assertTrue(selfCheckoutStation.cardReader.isDisabled());
        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
        assertTrue(selfCheckoutStation.coinTray.isDisabled());
        assertTrue(selfCheckoutStation.coinStorage.isDisabled());
        assertTrue(selfCheckoutStation.coinValidator.isDisabled());
        assertTrue(selfCheckoutStation.mainScanner.isDisabled());
        assertTrue(selfCheckoutStation.handheldScanner.isDisabled());
        assertTrue(selfCheckoutStation.scanningArea.isDisabled());
        assertTrue(selfCheckoutStation.baggingArea.isDisabled());
    }

    @Test
    public void blockSystemTest()
    {
        selfCheckoutSoftware.enableHardware();
        selfCheckoutSoftware.blockSystem();

        assertTrue(selfCheckoutStation.banknoteInput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteOutput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteStorage.isDisabled());
        assertTrue(selfCheckoutStation.banknoteValidator.isDisabled());
        assertTrue(selfCheckoutStation.cardReader.isDisabled());
        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
        assertTrue(selfCheckoutStation.coinTray.isDisabled());
        assertTrue(selfCheckoutStation.coinStorage.isDisabled());
        assertTrue(selfCheckoutStation.coinValidator.isDisabled());
        assertTrue(selfCheckoutStation.mainScanner.isDisabled());
        assertTrue(selfCheckoutStation.handheldScanner.isDisabled());
        assertTrue(selfCheckoutStation.scanningArea.isDisabled());
        assertFalse(selfCheckoutStation.baggingArea.isDisabled());
    }

    @Test
    public void getPhaseTest()
    {
        assertEquals(SelfCheckoutSoftware.Phase.IDLE, selfCheckoutSoftware.getPhase());

        selfCheckoutSoftware.weightDiscrepancy();

        assertEquals(SelfCheckoutSoftware.Phase.HAVING_WEIGHT_DISCREPANCY, selfCheckoutSoftware.getPhase());

        selfCheckoutSoftware.blockSystem();

        assertEquals(SelfCheckoutSoftware.Phase.BLOCKING, selfCheckoutSoftware.getPhase());

        selfCheckoutSoftware.errorOccur();

        assertEquals(SelfCheckoutSoftware.Phase.ERROR, selfCheckoutSoftware.getPhase());
    }

    @Test
    public void hasPendingChangesTest()
    {
        assertFalse(selfCheckoutSoftware.hasPendingChanges());
    }

    @Test
    public void startTest()
    {
        assertEquals(SelfCheckoutSoftware.Phase.IDLE, selfCheckoutSoftware.getPhase());
        assertNull(selfCheckoutSoftware.getCustomer());

        selfCheckoutSoftware.start(customer);

        assertEquals(SelfCheckoutSoftware.Phase.SCANNING_ITEM, selfCheckoutSoftware.getPhase());
        assertEquals(customer, selfCheckoutSoftware.getCustomer());
    }

    @Test(expected = IllegalStateException.class)
    public void startFailTest()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.start(customer);
    }

    @Test
    public void bagItemTest()
    {
        selfCheckoutSoftware.start(customer);

        selfCheckoutSoftware.bagItem();

        assertEquals(SelfCheckoutSoftware.Phase.BAGGING_ITEM, selfCheckoutSoftware.getPhase());
    }

    @Test
    public void bagItemTest2()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.addPLUItem();

        selfCheckoutSoftware.bagItem();

        assertEquals(SelfCheckoutSoftware.Phase.BAGGING_ITEM, selfCheckoutSoftware.getPhase());
    }

    @Test(expected = IllegalStateException.class)
    public void bagItemFailTest()
    {
        selfCheckoutSoftware.bagItem();
    }

    @Test
    public void addOwnBagTest()
    {
        selfCheckoutSoftware.start(customer);

        selfCheckoutSoftware.addOwnBag();

        assertEquals(SelfCheckoutSoftware.Phase.PLACING_OWN_BAG, selfCheckoutSoftware.getPhase());
    }

    @Test(expected = IllegalStateException.class)
    public void addOwnBagFailTest()
    {
        selfCheckoutSoftware.addOwnBag();
    }

    @Test(expected = IllegalStateException.class)
    public void addOwnBagFailTest2()
    {
        selfCheckoutSoftware.start(null);

        selfCheckoutSoftware.addOwnBag();
    }

    @Test
    public void notBaggingItemTest()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutSoftware.addPLUItem();
        selfCheckoutSoftware.bagItem();

        selfCheckoutSoftware.notBaggingItem();

        assertEquals(SelfCheckoutSoftware.Phase.NON_BAGGABLE_ITEM, selfCheckoutSoftware.getPhase());
    }

    @Test(expected = IllegalStateException.class)
    public void notBaggingItemFailTest()
    {
        selfCheckoutSoftware.notBaggingItem();
    }

    @Test
    public void checkoutTest()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();

        selfCheckoutSoftware.checkout();

        assertEquals(SelfCheckoutSoftware.Phase.CHOOSING_PAYMENT_METHOD, selfCheckoutSoftware.getPhase());
    }

    @Test(expected = IllegalStateException.class)
    public void checkoutFailTest()
    {
        selfCheckoutSoftware.checkout();
    }

    @Test(expected = IllegalStateException.class)
    public void checkoutFailTest2()
    {
        selfCheckoutSoftware.start(null);

        selfCheckoutSoftware.checkout();
    }

    @Test
    public void selectPaymentMethodTest()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();

        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.CASH);

        assertEquals(SelfCheckoutSoftware.Phase.PROCESSING_PAYMENT, selfCheckoutSoftware.getPhase());
    }

    @Test(expected = IllegalStateException.class)
    public void selectPaymentMethodFailTest()
    {
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.CASH);
    }

    @Test
    public void paymentCompleteTest()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.CASH);

        selfCheckoutSoftware.paymentCompleted();

        assertEquals(SelfCheckoutSoftware.Phase.PAYMENT_COMPLETE, selfCheckoutSoftware.getPhase());
    }

    @Test(expected = IllegalStateException.class)
    public void paymentCompleteFailTest()
    {
        selfCheckoutSoftware.paymentCompleted();
    }

    @Test
    public void checkoutCompleteTest()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.CASH);
        selfCheckoutSoftware.paymentCompleted();

        selfCheckoutSoftware.checkoutComplete();

        assertEquals(SelfCheckoutSoftware.Phase.IDLE, selfCheckoutSoftware.getPhase());
    }

    @Test(expected = IllegalStateException.class)
    public void checkoutCompleteFailTest()
    {
        selfCheckoutSoftware.checkoutComplete();
    }

    @Test
    public void idleTest()
    {
        selfCheckoutSoftware.start(customer);

        assertEquals(customer, selfCheckoutSoftware.getCustomer());
        assertEquals(SelfCheckoutSoftware.Phase.SCANNING_ITEM, selfCheckoutSoftware.getPhase());

        selfCheckoutSoftware.idle();

        assertNull(selfCheckoutSoftware.getCustomer());
        assertEquals(SelfCheckoutSoftware.Phase.IDLE, selfCheckoutSoftware.getPhase());
    }

    @Test
    public void cancelCheckoutTest()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();

        selfCheckoutSoftware.cancelCheckout();

        assertEquals(SelfCheckoutSoftware.Phase.SCANNING_ITEM, selfCheckoutSoftware.getPhase());
    }

    @Test
    public void cancelCheckoutTest2()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.CASH);

        selfCheckoutSoftware.cancelCheckout();

        assertEquals(SelfCheckoutSoftware.Phase.SCANNING_ITEM, selfCheckoutSoftware.getPhase());
    }

    @Test(expected = IllegalStateException.class)
    public void cancelCheckoutFailTest()
    {
        selfCheckoutSoftware.start(customer);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.CASH);
        selfCheckoutSoftware.paymentCompleted();

        selfCheckoutSoftware.cancelCheckout();
    }

    @Test
    public void setAndGetBanknoteDanglingTest()
    {
        assertFalse(selfCheckoutSoftware.getBanknoteDangling());

        selfCheckoutSoftware.setBanknoteDangling(true);

        assertTrue(selfCheckoutSoftware.getBanknoteDangling());
    }

    @Test
    public void setAndGetCoinInTrayTest()
    {
        assertFalse(selfCheckoutSoftware.getCoinInTray());

        selfCheckoutSoftware.setCoinInTray(true);

        assertTrue(selfCheckoutSoftware.getCoinInTray());
    }
}
