package tests.application;

import GUI.GUI;
import application.AppControl;
import application.Main;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import software.SelfCheckoutSoftware.Phase;
import software.SupervisionSoftware;
import user.Customer;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertTrue;

public class MainTest
{

    Currency currency = Currency.getInstance("USD");
    int[] banknoteDenominations = {1, 5, 10, 25, 100};
    BigDecimal[] coinDenominations = {new BigDecimal("0.01"), new BigDecimal("0.05"), new BigDecimal("0.1"), new BigDecimal("0.25"), new BigDecimal("1.00")};
    int scaleMaximumWeight = 10;
    int scaleSensitivity = 1;
    SelfCheckoutStation scs = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
    //AppControl ac = new AppControl();
    Customer c = new Customer();
    SupervisionStation supervisionStation = new SupervisionStation();
    SupervisionSoftware supervision = new SupervisionSoftware(supervisionStation);
    Numeral[] barcodeNumeral = {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four};
    Barcode barcode = new Barcode(barcodeNumeral);
    BarcodedProduct b = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("5.00"), 3.12);

    @Test
    public void testGetStore()
    {
        Main m = new Main();
        m.main(null);
        GUI.init(new AppControl());
        assertTrue(m.getStore() == null);
    }

    @Test
    public void customer()
    {
        Main m = new Main();
        m.main(null);
        GUI.init(new AppControl());
        assertTrue(GUI.newUser(AppControl.CUSTOMER));

    }

    @Test
    public void att()
    {
        Main m = new Main();
        m.main(null);
        GUI.init(new AppControl());
        assertTrue(GUI.newUser(AppControl.ATTENDANT));

    }

    @Test
    public void approach()
    {
        Main m = new Main();
        m.main(null);
        AppControl ac = new AppControl();

        GUI.init(ac);
        ac.addNewCustomer();
        GUI.newUser(AppControl.CUSTOMER);
        assertTrue(GUI.userApproachesStation(1));

    }

    @Test
    public void approach2()
    {
        Main m = new Main();
        m.main(null);
        AppControl ac = new AppControl();

        GUI.init(ac);
        ac.addNewCustomer();

        GUI.newUser(AppControl.CUSTOMER);
        assertTrue(GUI.userApproachesStation(1));

    }

    @Test
    public void password()
    {
        Main m = new Main();
        m.main(null);
        AppControl ac = new AppControl();

        GUI.init(ac);
        ac.addNewCustomer();
        assertTrue(GUI.attendantPassword("test") == false);

    }

    @Test
    public void attlogin()
    {
        Main m = new Main();
        m.main(null);
        AppControl ac = new AppControl();

        GUI.init(ac);
        ac.addNewCustomer();
        assertTrue(GUI.attendantLogin("test", "test") == false);

    }

    @Test
    public void attlogin2()
    {
        Main m = new Main();
        m.main(null);
        AppControl ac = new AppControl();

        GUI.init(ac);
        ac.addNewCustomer();
        assertTrue(GUI.isAttendantLoggedIn() == false);

    }

    @Test
    public void phasetest()
    {
        Main m = new Main();
        m.main(null);
        AppControl ac = new AppControl();

        GUI.init(ac);
        ac.addNewCustomer();
        assertTrue(GUI.getPhase(1) == Phase.IDLE);

    }
    //	@Test(expected = Exception.class)
    //	public void enters() {
    //		Main m = new Main();
    //		m.main(null);
    //	    AppControl ac = new AppControl();
    //
    //		GUI.init(ac);
    //		ac.addNewCustomer();
    //		GUI.userEntersPLUCode(1000, 1);
    //
    //	}

}
