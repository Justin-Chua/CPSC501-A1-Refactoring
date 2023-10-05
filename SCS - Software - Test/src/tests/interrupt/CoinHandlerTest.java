package tests.interrupt;


import interrupt.CoinHandler;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Coin;
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

public class CoinHandlerTest
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

    CoinHandler coinHandler;
    Customer customer;

    Coin coin1;
    Coin coin2;

    @Before
    public void setup()
    {
        selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);
        supervisionStation = new SupervisionStation();
        supervisionSoftware = new SupervisionSoftware(supervisionStation);
        supervisionSoftware.add(selfCheckoutSoftware);
        selfCheckoutStation.coinSlot.detachAll();
        selfCheckoutStation.coinDispensers.forEach((k, v) -> v.detachAll());
        selfCheckoutStation.coinStorage.detachAll();
        selfCheckoutStation.coinTray.detachAll();
        selfCheckoutStation.coinValidator.detachAll();
        coinHandler = new CoinHandler(selfCheckoutSoftware);
        customer = new Customer();
        coinHandler.setCustomer(customer);
        coin1 = new Coin(currency, coinDenominations[2]);
        coin2 = new Coin(currency, coinDenominations[3]);
        Store.setSupervisionSoftware(supervisionSoftware);
        Store.addSelfCheckoutSoftware(selfCheckoutSoftware);
    }

    @Test
    public void attachAndDetachTest()
    {
        selfCheckoutStation.coinSlot.detach(coinHandler);
        selfCheckoutStation.coinDispensers.forEach((k, v) -> v.detach(coinHandler));
        selfCheckoutStation.coinStorage.detach(coinHandler);
        selfCheckoutStation.coinTray.detach(coinHandler);
        selfCheckoutStation.coinValidator.detach(coinHandler);

        assertFalse(selfCheckoutStation.coinSlot.detach(coinHandler));
        selfCheckoutStation.coinDispensers.forEach((k, v) -> assertFalse(v.detach(coinHandler)));
        assertFalse(selfCheckoutStation.coinStorage.detach(coinHandler));
        assertFalse(selfCheckoutStation.coinTray.detach(coinHandler));
        assertFalse(selfCheckoutStation.coinValidator.detach(coinHandler));

        coinHandler.attachAll();

        assertTrue(selfCheckoutStation.coinSlot.detach(coinHandler));
        selfCheckoutStation.coinDispensers.forEach((k, v) -> assertTrue(v.detach(coinHandler)));
        assertTrue(selfCheckoutStation.coinStorage.detach(coinHandler));
        assertTrue(selfCheckoutStation.coinTray.detach(coinHandler));
        assertTrue(selfCheckoutStation.coinValidator.detach(coinHandler));

        coinHandler.attachAll();
        coinHandler.detatchAll();

        assertFalse(selfCheckoutStation.coinSlot.detach(coinHandler));
        selfCheckoutStation.coinDispensers.forEach((k, v) -> assertFalse(v.detach(coinHandler)));
        assertFalse(selfCheckoutStation.coinStorage.detach(coinHandler));
        assertFalse(selfCheckoutStation.coinTray.detach(coinHandler));
        assertFalse(selfCheckoutStation.coinValidator.detach(coinHandler));
    }

    @Test
    public void enableAndDisableHardwareTest()
    {
        assertFalse(selfCheckoutStation.coinSlot.isDisabled());
        assertFalse(selfCheckoutStation.coinTray.isDisabled());
        assertFalse(selfCheckoutStation.coinStorage.isDisabled());
        assertFalse(selfCheckoutStation.coinValidator.isDisabled());

        coinHandler.disableHardware();

        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
        assertTrue(selfCheckoutStation.coinTray.isDisabled());
        assertTrue(selfCheckoutStation.coinStorage.isDisabled());
        assertTrue(selfCheckoutStation.coinValidator.isDisabled());

        coinHandler.enableHardware();

        assertFalse(selfCheckoutStation.coinSlot.isDisabled());
        assertFalse(selfCheckoutStation.coinTray.isDisabled());
        assertFalse(selfCheckoutStation.coinStorage.isDisabled());
        assertFalse(selfCheckoutStation.coinValidator.isDisabled());
    }

    @Test
    public void getCustomerTest()
    {
        assertEquals(customer, coinHandler.getCustomer());
    }

    @Test
    public void insertAndGetCoinTest() throws OverloadException, DisabledException
    {
        assertFalse(coinHandler.getCoinDetected());
        assertEquals(BigDecimal.ZERO, customer.getCashBalance());

        selfCheckoutStation.coinSlot.accept(coin1);

        assertFalse(coinHandler.getCoinDetected());
        assertEquals(coin1.getValue(), customer.getCashBalance());
    }

    @Test
    public void insertAndGetCoinTest2() throws OverloadException, DisabledException
    {
        assertFalse(coinHandler.getCoinDetected());
        assertEquals(BigDecimal.ZERO, customer.getCashBalance());

        coinHandler.setCustomer(null);
        selfCheckoutStation.coinSlot.accept(coin1);

        assertFalse(coinHandler.getCoinDetected());
        assertEquals(BigDecimal.ZERO, customer.getCashBalance());
    }

    @Test
    public void invalidCoinTest() throws OverloadException, DisabledException
    {
        assertFalse(coinHandler.getCoinDetected());

        selfCheckoutStation.coinSlot.accept(new Coin(currency, new BigDecimal(banknoteDenominations[0])));

        assertFalse(coinHandler.getCoinDetected());
        assertEquals(BigDecimal.ZERO, coinHandler.getCoinValue());
    }

    @Test
    public void insertAndUnloadCoinTest() throws OverloadException, DisabledException
    {
        selfCheckoutStation.coinSlot.disable();

        selfCheckoutStation.coinStorage.accept(coin1);
        selfCheckoutStation.coinStorage.accept(coin2);

        assertTrue(selfCheckoutStation.coinSlot.isDisabled());

        selfCheckoutStation.coinStorage.unload();

        assertFalse(selfCheckoutStation.coinSlot.isDisabled());
    }

    @Test
    public void coinStorageFullTest()
    {
        assertFalse(selfCheckoutStation.coinSlot.isDisabled());

        coinHandler.coinsFull(selfCheckoutStation.coinStorage);

        assertTrue(selfCheckoutStation.coinSlot.isDisabled());
    }

    @Test
    public void coinAddedTest() throws OverloadException, DisabledException
    {
        selfCheckoutStation.coinSlot.accept(coin1);

        coinHandler.coinAdded(selfCheckoutStation.coinStorage);

        assertEquals(coin1.getValue(), customer.getCashBalance());
    }

    @Test
    public void coinsEmpty()
    {
        coinHandler.coinsEmpty(selfCheckoutStation.coinDispensers.get(0));
    }
}
