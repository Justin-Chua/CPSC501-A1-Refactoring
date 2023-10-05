package tests.store;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import store.Store;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.*;

public class StoreTest
{
    final Currency currency = Currency.getInstance("CAD");
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final int scaleMaximumWeight = 100;
    final int scaleSensitivity = 10;
    SelfCheckoutStation selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
    SelfCheckoutSoftware selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);
    SupervisionStation supervisionStation = new SupervisionStation();
    SupervisionSoftware supervisionSoftware = new SupervisionSoftware(supervisionStation);

    @Before
    public void setup()
    {
        Store.setSupervisionSoftware(null);
    }

    @Test
    public void setAndGetSupervisionSoftwareTest()
    {
        assertNull(Store.getSupervisionSoftware());

        Store.setSupervisionSoftware(supervisionSoftware);

        assertEquals(supervisionSoftware, Store.getSupervisionSoftware());
    }

    @Test
    public void setAndGetSelfCheckoutSoftwareTest()
    {
        Store.setSupervisionSoftware(supervisionSoftware);

        assertTrue(Store.getSelfCheckoutSoftwareList().isEmpty());

        Store.addSelfCheckoutSoftware(selfCheckoutSoftware);

        assertTrue(Store.getSelfCheckoutSoftwareList().contains(selfCheckoutSoftware));
        assertEquals(1, Store.getSelfCheckoutSoftwareList().size());
    }
}
