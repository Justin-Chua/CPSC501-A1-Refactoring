package tests.interrupt;


import interrupt.BanknoteHandler;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import store.Store;
import user.Customer;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.*;

public class BanknoteHandlerTest
{
    // Static variables that will be used during testing
    final Currency currency = Currency.getInstance("CAD");
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final int scaleMaximumWeight = 100;
    final int scaleSensitivity = 10;

    SelfCheckoutStation selfCheckoutStation;
    SelfCheckoutSoftware selfCheckoutSoftware;
    SupervisionStation supervisionStation;
    SupervisionSoftware supervisionSoftware;

    BanknoteHandler banknoteHandler;
    Customer customer;

    Banknote banknote1;
    Banknote banknote2;

    @Before
    public void setup()
    {
        selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);
        supervisionStation = new SupervisionStation();
        supervisionSoftware = new SupervisionSoftware(supervisionStation);
        supervisionSoftware.add(selfCheckoutSoftware);
        banknoteHandler = new BanknoteHandler(selfCheckoutSoftware);
        customer = new Customer();
        banknoteHandler.setCustomer(customer);
        banknote1 = new Banknote(currency, banknoteDenominations[0]);
        banknote2 = new Banknote(currency, banknoteDenominations[1]);
        Store.setSupervisionSoftware(supervisionSoftware);
        Store.addSelfCheckoutSoftware(selfCheckoutSoftware);
    }

    @Test
    public void attachAndDetachTest()
    {
        selfCheckoutStation.banknoteInput.detach(banknoteHandler);
        selfCheckoutStation.banknoteDispensers.forEach((k, v) -> v.detach(banknoteHandler));
        selfCheckoutStation.banknoteOutput.detach(banknoteHandler);
        selfCheckoutStation.banknoteValidator.detach(banknoteHandler);

        assertFalse(selfCheckoutStation.banknoteInput.detach(banknoteHandler));
        selfCheckoutStation.banknoteDispensers.forEach((k, v) -> assertFalse(v.detach(banknoteHandler)));
        assertFalse(selfCheckoutStation.banknoteOutput.detach(banknoteHandler));
        assertFalse(selfCheckoutStation.banknoteValidator.detach(banknoteHandler));

        banknoteHandler.attachAll();

        assertTrue(selfCheckoutStation.banknoteInput.detach(banknoteHandler));
        selfCheckoutStation.banknoteDispensers.forEach((k, v) -> assertTrue(v.detach(banknoteHandler)));
        assertTrue(selfCheckoutStation.banknoteOutput.detach(banknoteHandler));
        assertTrue(selfCheckoutStation.banknoteValidator.detach(banknoteHandler));

        banknoteHandler.attachAll();
        banknoteHandler.detatchAll();

        assertFalse(selfCheckoutStation.banknoteInput.detach(banknoteHandler));
        selfCheckoutStation.banknoteDispensers.forEach((k, v) -> assertFalse(v.detach(banknoteHandler)));
        assertFalse(selfCheckoutStation.banknoteOutput.detach(banknoteHandler));
        assertFalse(selfCheckoutStation.banknoteValidator.detach(banknoteHandler));
    }

    @Test
    public void enableAndDisableHardwareTest()
    {
        assertFalse(selfCheckoutStation.banknoteInput.isDisabled());
        assertFalse(selfCheckoutStation.banknoteOutput.isDisabled());
        assertFalse(selfCheckoutStation.banknoteStorage.isDisabled());
        assertFalse(selfCheckoutStation.banknoteValidator.isDisabled());

        banknoteHandler.disableHardware();

        assertTrue(selfCheckoutStation.banknoteInput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteOutput.isDisabled());
        assertTrue(selfCheckoutStation.banknoteStorage.isDisabled());
        assertTrue(selfCheckoutStation.banknoteValidator.isDisabled());

        banknoteHandler.enableHardware();

        assertFalse(selfCheckoutStation.banknoteInput.isDisabled());
        assertFalse(selfCheckoutStation.banknoteOutput.isDisabled());
        assertFalse(selfCheckoutStation.banknoteStorage.isDisabled());
        assertFalse(selfCheckoutStation.banknoteValidator.isDisabled());
    }

    @Test
    public void getCustomerTest()
    {
        assertEquals(customer, banknoteHandler.getCustomer());
    }

    @Test
    public void insertAndGetBanknoteTest() throws OverloadException, DisabledException
    {
        assertFalse(banknoteHandler.isBanknoteDetected());
        assertEquals(BigDecimal.ZERO, customer.getCashBalance());

        selfCheckoutStation.banknoteInput.accept(banknote1);

        assertFalse(banknoteHandler.isBanknoteDetected());
        assertEquals(0, customer.getCashBalance().compareTo(new BigDecimal(banknote1.getValue())));
    }

    @Test
    public void banknoteAddedFailTest() throws OverloadException, DisabledException
    {
        banknoteHandler.banknoteAdded(selfCheckoutStation.banknoteStorage);
    }

    @Test
    public void banknoteAddedFailTest2() throws OverloadException, DisabledException
    {
        banknoteHandler.banknoteAdded(selfCheckoutStation.banknoteStorage);
    }

    @Test
    public void banknoteFullAndEmptyTest()
    {
        assertFalse(selfCheckoutStation.banknoteInput.isDisabled());

        banknoteHandler.banknotesFull(selfCheckoutStation.banknoteStorage);

        assertTrue(selfCheckoutStation.banknoteInput.isDisabled());

        banknoteHandler.banknotesEmpty(selfCheckoutStation.banknoteDispensers.get(0));
    }

    @Test
    public void invalidBanknoteTest() throws OverloadException, DisabledException
    {
        assertFalse(banknoteHandler.isBanknoteDetected());

        selfCheckoutStation.banknoteInput.accept(new Banknote(currency, coinDenominations[4].intValue()));

        assertFalse(banknoteHandler.isBanknoteDetected());
        assertEquals(BigDecimal.ZERO, banknoteHandler.getBanknoteValue());
    }

    @Test
    public void banknoteRemovedFailTest()
    {
        selfCheckoutSoftware.hasPendingChanges();
        banknoteHandler.banknoteRemoved(selfCheckoutStation.banknoteOutput);
    }

    @Test
    public void banknoteRemovedFailTest2()
    {
        banknoteHandler.banknoteRemoved(selfCheckoutStation.banknoteInput);
    }

    //    @Test
    //    public void insertAndUnloadBanknoteTest() throws OverloadException, DisabledException
    //    {
    //        selfCheckoutStation.coinSlot.disable();
    //
    //        selfCheckoutStation.coinStorage.accept(banknote1);
    //        selfCheckoutStation.coinStorage.accept(banknote2);
    //
    //        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
    //
    //        selfCheckoutStation.coinStorage.unload();
    //
    //        assertFalse(selfCheckoutStation.coinSlot.isDisabled());
    //    }
    //
    //    @Test
    //    public void coinStorageFullTest()
    //    {
    //        assertFalse(selfCheckoutStation.coinSlot.isDisabled());
    //
    //        banknoteHandler.coinsFull(selfCheckoutStation.coinStorage);
    //
    //        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
    //    }
    //
    //    @Test
    //    public void coinAddedTest() throws OverloadException, DisabledException
    //    {
    //        selfCheckoutStation.coinSlot.accept(banknote1);
    //
    //        banknoteHandler.coinAdded(selfCheckoutStation.coinStorage);
    //
    //        assertEquals(banknote1.getValue(), customer.getCashBalance());
    //    }
    //
    //    @Test
    //    public void coinsEmpty()
    //    {
    //        banknoteHandler.coinsEmpty(selfCheckoutStation.coinDispensers.get(0));
    //    }
}
