package tests.software;

import application.Main;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import store.Store;
import store.credentials.AuthorizationRequiredException;
import store.credentials.CredentialsSystem;
import store.credentials.IncorrectCredentialException;
import user.Attendant;
import user.Customer;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.*;

public class SupervisionSoftwareTest
{
    // Static variables that will be used during testing
    final Currency currency = Currency.getInstance("CAD");
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final int scaleMaximumWeight = 100;
    final int scaleSensitivity = 10;
    static final String username = "username5555";
    static final String password = "password3333";

    SelfCheckoutStation selfCheckoutStation1;
    SelfCheckoutStation selfCheckoutStation2;
    SelfCheckoutSoftware selfCheckoutSoftware1;
    SelfCheckoutSoftware selfCheckoutSoftware2;
    SupervisionStation supervisionStation;
    SupervisionSoftware supervisionSoftware;

    Customer customer;
    Attendant attendant;

    @BeforeClass
    public static void initialSetup()
    {
        CredentialsSystem.addAccount(username, password);
    }

    @AfterClass
    public static void cleanup()
    {
        CredentialsSystem.removeAccount(username);
    }

    @Before
    public void setup()
    {
        Store.setSupervisionSoftware(null);

        selfCheckoutStation1 = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        selfCheckoutStation2 = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        selfCheckoutSoftware1 = new SelfCheckoutSoftware(selfCheckoutStation1);
        selfCheckoutSoftware2 = new SelfCheckoutSoftware(selfCheckoutStation2);
        supervisionStation = new SupervisionStation();
        supervisionSoftware = new SupervisionSoftware(supervisionStation);

        customer = new Customer();
        attendant = new Attendant();

        attendant.setLogin(username, password);

        Main.Tangibles.ATTENDANTS.clear();
        Main.Tangibles.ATTENDANTS.add(attendant);
    }

    @Test
    public void constructorTest()
    {
        supervisionSoftware = new SupervisionSoftware(supervisionStation);
        assertNotNull(supervisionSoftware);
        assertEquals(supervisionStation, supervisionSoftware.getSupervisionStation());
    }

    @Test
    public void constructorTest2()
    {
        supervisionSoftware.add(selfCheckoutSoftware1);
        supervisionSoftware.add(selfCheckoutSoftware2);

        assertEquals(2, supervisionSoftware.getSoftwareList().size());
        assertTrue(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware1));
        assertTrue(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware2));

        supervisionSoftware = new SupervisionSoftware(supervisionStation, supervisionSoftware.getSoftwareList());

        assertEquals(2, supervisionSoftware.getSoftwareList().size());
        assertTrue(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware1));
        assertTrue(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware2));
    }

    @Test
    public void addAndRemoveTest()
    {
        assertTrue(supervisionSoftware.getSoftwareList().isEmpty());
        assertFalse(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware1));
        assertFalse(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware2));

        supervisionSoftware.add(selfCheckoutSoftware1);
        supervisionSoftware.add(selfCheckoutSoftware2);

        assertEquals(2, supervisionSoftware.getSoftwareList().size());
        assertTrue(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware1));
        assertTrue(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware2));

        supervisionSoftware.remove(selfCheckoutSoftware1);
        supervisionSoftware.remove(selfCheckoutSoftware2);

        assertTrue(supervisionSoftware.getSoftwareList().isEmpty());
        assertFalse(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware1));
        assertFalse(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware2));
    }

    @Test
    public void clearTest()
    {
        supervisionSoftware.add(selfCheckoutSoftware1);
        supervisionSoftware.add(selfCheckoutSoftware2);

        supervisionSoftware.clear();

        assertTrue(supervisionSoftware.getSoftwareList().isEmpty());
        assertFalse(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware1));
        assertFalse(supervisionSoftware.getSoftwareList().contains(selfCheckoutSoftware2));
    }

    @Test
    public void getAttendantTest() throws IncorrectCredentialException
    {
        assertNull(supervisionSoftware.getAttendant());

        supervisionSoftware.login(username, password);

        assertEquals(attendant, supervisionSoftware.getAttendant());
    }

    @Test
    public void isLoggedInTest() throws IncorrectCredentialException
    {
        assertFalse(supervisionSoftware.isLoggedIn());

        supervisionSoftware.login(username, password);

        assertTrue(supervisionSoftware.isLoggedIn());
    }

    @Test
    public void loginAndLogoutTest() throws IncorrectCredentialException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.logout();
    }

    @Test(expected = IncorrectCredentialException.class)
    public void loginUnsuccessfullyTest() throws IncorrectCredentialException
    {
        supervisionSoftware.login(password, username);
    }

    @Test
    public void loginTest2() throws IncorrectCredentialException
    {
        Main.Tangibles.ATTENDANTS.clear();
        Attendant testAttendant1 = new Attendant();
        Attendant testAttendant2 = new Attendant();
        testAttendant1.setLogin(password, username);
        testAttendant2.setLogin(username, username);
        Main.Tangibles.ATTENDANTS.add(testAttendant1);
        Main.Tangibles.ATTENDANTS.add(testAttendant2);
        Main.Tangibles.ATTENDANTS.add(attendant);

        supervisionSoftware.login(username, password);
    }

    @Test
    public void startUpAndShutDownTest() throws AuthorizationRequiredException, IncorrectCredentialException
    {
        supervisionSoftware.login(username, password);

        assertNull(Store.getSupervisionSoftware());

        supervisionSoftware.startUp();

        assertEquals(supervisionSoftware, Store.getSupervisionSoftware());

        supervisionSoftware.shutdown();

        assertNull(Store.getSupervisionSoftware());
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void shutDownUnsuccessfullyTest() throws AuthorizationRequiredException
    {
        supervisionSoftware.shutdown();
    }

    @Test
    public void restartTest() throws AuthorizationRequiredException, IncorrectCredentialException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.add(selfCheckoutSoftware1);
        supervisionSoftware.add(selfCheckoutSoftware2);
        List <SelfCheckoutSoftware> selfCheckoutSoftwareList = supervisionSoftware.getSoftwareList();

        supervisionSoftware.restart();

        assertNotEquals(supervisionSoftware, Store.getSupervisionSoftware());
        assertEquals(selfCheckoutSoftwareList, Store.getSupervisionSoftware().getSoftwareList());
    }

    @Test
    public void restartTest2() throws AuthorizationRequiredException, IncorrectCredentialException
    {
        supervisionSoftware.login(username, password);
        List <SelfCheckoutSoftware> selfCheckoutSoftwareList = supervisionSoftware.getSoftwareList();

        supervisionSoftware.restart();

        assertEquals(selfCheckoutSoftwareList, Store.getSupervisionSoftware().getSoftwareList());
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void restartUnsuccessfullyTest() throws AuthorizationRequiredException
    {
        supervisionSoftware.restart();
    }

    @Test
    public void startUpSelfCheckoutStationTest() throws AuthorizationRequiredException, IncorrectCredentialException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.add(selfCheckoutSoftware1);

        supervisionSoftware.startUpStation(selfCheckoutSoftware1);
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void startUpSelfCheckoutStationUnsuccessfullyTest() throws AuthorizationRequiredException
    {
        supervisionSoftware.add(selfCheckoutSoftware1);

        supervisionSoftware.startUpStation(selfCheckoutSoftware1);
    }

    @Test
    public void shutDownSelfCheckoutStationTest() throws AuthorizationRequiredException, IncorrectCredentialException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.add(selfCheckoutSoftware1);

        supervisionSoftware.shutDownStation(selfCheckoutSoftware1);
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void shutDownSelfCheckoutStationUnsuccessfullyTest() throws AuthorizationRequiredException
    {
        supervisionSoftware.add(selfCheckoutSoftware1);

        supervisionSoftware.shutDownStation(selfCheckoutSoftware1);
    }

    @Test
    public void blockStationTest() throws IncorrectCredentialException, AuthorizationRequiredException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.add(selfCheckoutSoftware1);

        assertNotEquals(SelfCheckoutSoftware.Phase.BLOCKING, selfCheckoutSoftware1.getPhase());

        supervisionSoftware.blockStation(selfCheckoutSoftware1);

        assertEquals(SelfCheckoutSoftware.Phase.BLOCKING, selfCheckoutSoftware1.getPhase());
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void blockStationUnsuccessfullyTest() throws AuthorizationRequiredException
    {
        supervisionSoftware.blockStation(selfCheckoutSoftware1);
    }

    @Test
    public void unblockStationTest() throws IncorrectCredentialException, AuthorizationRequiredException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.add(selfCheckoutSoftware1);
        supervisionSoftware.blockStation(selfCheckoutSoftware1);

        assertEquals(SelfCheckoutSoftware.Phase.BLOCKING, selfCheckoutSoftware1.getPhase());

        supervisionSoftware.unblockStation(selfCheckoutSoftware1);

        assertNotEquals(SelfCheckoutSoftware.Phase.BLOCKING, selfCheckoutSoftware1.getPhase());

    }

    @Test(expected = AuthorizationRequiredException.class)
    public void unblockStationUnsuccessfullyTest() throws AuthorizationRequiredException
    {
        supervisionSoftware.unblockStation(selfCheckoutSoftware1);
    }

    @Test()
    public void approveWeightDiscrepancyTest() throws IncorrectCredentialException, AuthorizationRequiredException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.add(selfCheckoutSoftware1);
        selfCheckoutSoftware1.weightDiscrepancy();

        supervisionSoftware.approveWeightDiscrepancy(selfCheckoutSoftware1);
    }

    @Test(expected = IllegalStateException.class)
    public void approveWeightDiscrepancyUnsuccessfullyTest() throws AuthorizationRequiredException, IncorrectCredentialException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.add(selfCheckoutSoftware1);

        supervisionSoftware.approveWeightDiscrepancy(selfCheckoutSoftware1);
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void approveWeightDiscrepancyUnsuccessfullyTest2() throws AuthorizationRequiredException
    {
        selfCheckoutSoftware1.weightDiscrepancy();
        supervisionSoftware.approveWeightDiscrepancy(selfCheckoutSoftware1);
    }

    @Test
    public void approveItemTest() throws IncorrectCredentialException, AuthorizationRequiredException
    {
        supervisionSoftware.login(username, password);
        supervisionSoftware.add(selfCheckoutSoftware1);

        assertNotEquals(SelfCheckoutSoftware.Phase.SCANNING_ITEM, selfCheckoutSoftware1.getPhase());

        supervisionSoftware.approveItemNotBaggable(selfCheckoutSoftware1);

        assertEquals(SelfCheckoutSoftware.Phase.SCANNING_ITEM, selfCheckoutSoftware1.getPhase());
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void approveItemUnsuccessfullyTest() throws AuthorizationRequiredException
    {
        supervisionSoftware.approveItemNotBaggable(selfCheckoutSoftware1);
    }

    @Test
    public void resolveErrorTest()
    {
        selfCheckoutSoftware1.errorOccur();
        supervisionSoftware.resolveError(selfCheckoutSoftware1);
    }

    @Test(expected = IllegalStateException.class)
    public void resolveErrorUnsuccessfullyTest()
    {
        supervisionSoftware.resolveError(selfCheckoutSoftware1);
    }

    @Test
    public void approveUseOfOwnBagsTest() throws IncorrectCredentialException, AuthorizationRequiredException
    {
        supervisionSoftware.login(username, password);

        supervisionSoftware.approveUseOfOwnBags(selfCheckoutSoftware1);

        assertEquals(SelfCheckoutSoftware.Phase.SCANNING_ITEM, selfCheckoutSoftware1.getPhase());
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void approveUseOfOwnBagsUnsuccessfullyTest() throws AuthorizationRequiredException
    {
        supervisionSoftware.approveUseOfOwnBags(selfCheckoutSoftware1);
    }
}
