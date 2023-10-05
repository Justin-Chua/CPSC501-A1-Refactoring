package tests.application;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.Product;

import application.AppControl;
import application.Main;
import application.Main.Tangibles;
import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import software.SelfCheckoutSoftware.Phase;
import store.Inventory;
import store.Store;
import store.credentials.IncorrectCredentialException;
import store.credentials.AuthorizationRequiredException;
import user.Attendant;
import user.Customer;
import user.User;

public class AppControlTest {
	private AppControl ac;
	@Before
	public void setup() {
        Main.initializeCardAndIssuers();
        Main.initializeProductDatabase();
        Main.initializeStore();
        Main.initializeMembership();
        Main.initializeCredentialsSytem();
        ac = new AppControl();
	}
	
	@Test
	public void testConstructor() {
       
		assertTrue(AppControl.getSupervisor() == Main.Tangibles.SUPERVISION_STATION);
		assertTrue(ac.getSupervisorSoftware() == Store.getSupervisionSoftware());
		
		assertTrue(ac.getSelfStationSoftwares() == ac.getSupervisorSoftware().getSoftwareList());
		
		assertTrue(ac.getActiveUsers().length == ac.getSelfStations().size() + 1);
	}

	@Test
	public void testAddNewCustomer() {
		//Main m = new Main();
		//m.main(null);
		ac.addNewCustomer();
		ac.getActiveUser();
		assertTrue(ac.getActiveUser() instanceof Customer);
	}

	@Test
	public void testAddNewAttendant() {
		//Main m = new Main();
		//m.main(null);
		ac.addNewAttendant();
		ac.getActiveUser();
		assertTrue(ac.getActiveUser() instanceof Attendant);
	}

	@Test
	public void testNextActiveUser() {
		//Main m = new Main();
		//m.main(null);
		ac.addNewCustomer();
		User customer1 = ac.getActiveUser();
		ac.customerUsesStation(1);
		ac.addNewCustomer();
		User customer2 = ac.getActiveUser();
		ac.customerUsesStation(2);
		assertTrue(customer1 != customer2);
		assertTrue(ac.getActiveUser() == customer2);
		ac.nextActiveUser();
		assertTrue(ac.getActiveUser() == customer1);
	}

	@Test
	public void testPrevActiveUser() {
		ac.addNewCustomer();
		User customer1 = ac.getActiveUser();
		ac.customerUsesStation(3);
		ac.addNewCustomer();
		User customer2 = ac.getActiveUser();
		ac.customerUsesStation(2);
		assertTrue(customer1 != customer2);
		assertTrue(ac.getActiveUser() == customer2);
		ac.prevActiveUser();
		assertTrue(ac.getActiveUser() == customer1);
	}

	@Test
	public void testGetActiveUser() {
		ac.addNewCustomer();
		assertTrue(ac.getActiveUser() instanceof Customer);
	}

	@Test
	public void testGetUserAt() {
		ac.addNewCustomer();
		User customer = ac.getActiveUser();
		ac.customerUsesStation(1);
		assertTrue(ac.getUserAt(1) == customer);
		
	}

	@Test
	public void testGetActiveUsers() {
		assertTrue(ac.getActiveUsers() instanceof User[]);
	}
	
	@Test
	public void getSelfCheckoutStationSoftwareTest() {
		assertTrue(ac.getSelfCheckoutSoftware(1) instanceof SelfCheckoutSoftware);
	}
	
	@Test
	public void getActiveUsersStationAttendant() {
		ac.addNewAttendant();
		ac.attendantUsesStation(1);
		assertTrue(ac.getActiveUsersStation() == 1);
	}
	
	@Test
	public void getActiveUsersStationCustomer() {
		ac.addNewCustomer();
		ac.customerUsesStation(2);
		assertTrue(ac.getActiveUsersStation() == 2);
	}
	
	@Test
	public void getActiveUsersStationNoUser() {
		ac.addNewCustomer();
		assertTrue(ac.getActiveUsersStation() == -1);
	}
	
	@Test
	public void testCustomerUsesStation() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		User customer = ac.getActiveUser();
		assertTrue(ac.getActiveUsers()[1] == customer);
	}

	@Test
	public void testAttendantUsesStation() {
		ac.addNewAttendant();
		ac.attendantUsesStation(1);
		User attendant = ac.getActiveUser();
		assertTrue(ac.getActiveUsers()[1] == attendant);
	}

	@Test
	public void testCustomerLeavesStation() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		User customer = ac.getActiveUser();
		assertTrue(ac.getActiveUsers()[1] == customer);
		ac.customerLeavesStation(1);
		assertTrue(ac.getActiveUsers()[1] == null);
	}

	@Test
	public void testAttendantLeavesStation() {
		ac.addNewAttendant();
		ac.attendantUsesStation(1);
		User attendant = ac.getActiveUser();
		assertTrue(ac.getActiveUsers()[1] == attendant);
		ac.attendantLeavesStation(1);
		assertTrue(ac.getActiveUsers()[1] == null);
	}
	
	@Test
	public void testAttendantLeavesStationZero() {
		ac.addNewAttendant();
		ac.attendantUsesStation(1);
		User attendant = ac.getActiveUser();
		assertTrue(ac.getActiveUsers()[1] == attendant);
		ac.attendantLeavesStation(0);
		assertTrue(ac.getActiveUsers()[1] != null);
	}
	
	@Test
	public void testGetStationStateBlocking() {
		ac.getSelfStationSoftwares().get(1).blockSystem();
		assertTrue(ac.getStationState(1) == "BLOCKED");
	}
	
	@Test
	public void testGetStationStateNonBaggableItem() {
		ac.getSelfStationSoftwares().get(1).addItem();
		ac.getSelfStationSoftwares().get(1).bagItem();
		ac.getSelfStationSoftwares().get(1).notBaggingItem();
		assertTrue(ac.getStationState(1) == "ITEM NOT BAGGED");
	}
	
	@Test
	public void testGetStationStateWeightDiscrepancy() {
		ac.getSelfStationSoftwares().get(1).weightDiscrepancy();
		assertTrue(ac.getStationState(1) == "WEIGHT DISCREPANCY");
	}
	
	@Test
	public void testGetStationStateWeightOkay() {
		assertTrue(ac.getStationState(1) == "OKAY");
	}
	
	@Test
	public void testGetStationPhase() {
		ac.getSelfStationSoftwares().get(1).blockSystem();
		assertTrue(ac.getStationPhase(1) == Phase.BLOCKING);
	}
	
	@Test
	public void testToggleBlock() {
		try {
			ac.getSupervisorSoftware().login("a","a");
		} catch (IncorrectCredentialException e) {}
		ac.toggleBlock(1);
		assertTrue(ac.getStationPhase(1) == Phase.BLOCKING);
		ac.toggleBlock(1);
		assertTrue(ac.getStationPhase(1) != Phase.BLOCKING);
	}
	
	
	
	@Test
	public void testApproveStationDiscrepancyWeight() {
		try {
			ac.getSupervisorSoftware().login("a","a");
		} catch (IncorrectCredentialException e) {}
		ac.getSelfStationSoftwares().get(1).weightDiscrepancy();
		assertTrue(ac.getStationPhase(1) == Phase.HAVING_WEIGHT_DISCREPANCY);
		ac.approveStationDiscrepancy(1);
		assertTrue(ac.getStationPhase(1) != Phase.HAVING_WEIGHT_DISCREPANCY);

	}
	
	@Test
	public void testApproveStationDiscrepancyNotBaggable() {
		try {
			ac.getSupervisorSoftware().login("a","a");
		} catch (IncorrectCredentialException e) {}
		ac.getSelfStationSoftwares().get(1).addItem();
		ac.getSelfStationSoftwares().get(1).bagItem();
		ac.getSelfStationSoftwares().get(1).notBaggingItem();
		assertTrue(ac.getStationPhase(1) == Phase.NON_BAGGABLE_ITEM);
		ac.approveStationDiscrepancy(1);
		assertTrue(ac.getStationPhase(1) != Phase.NON_BAGGABLE_ITEM);

	}
	
	@Test
	public void testcustomerTapsCreditCard() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		
	    final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
	    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
        ac.getSelfStationSoftwares().get(0).addItem();
        ac.getSelfStationSoftwares().get(0).checkout();
        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);

		
		boolean success;
		do {
			success = ac.customerTapsCreditCard(1);
		} while (!success);
		assertTrue(success);
		
	}
	
	@Test
	public void testcustomerTapsCreditCardFailure() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		boolean success;
		do {
		    final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
	        success = !ac.customerTapsCreditCard(1);
		}	while(!success);
		
		
		assertTrue(success);
		
	}
	@Test
	public void testcustomerTapsDebitCard() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
		boolean success;
		do {
			success = ac.customerTapsDebitCard(1);
		} while (!success);
		assertTrue(success);
		
	}
	
	@Test
	public void testcustomerTapsDebitCardFailure() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		boolean success;
		do {
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
			success = !ac.customerTapsDebitCard(1);

		} while (!success);
		assertTrue(success);
		
	}
	@Test
	public void testcustomerTapsMembershipCard() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
		boolean success;
		do {
			success = ac.customerTapsMembershipCard(1);
		} while (!success);
		assertTrue(success);
		
	}
	
	@Test
	public void testcustomerTapsMembershipCardFailure() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		boolean success;
		do {
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
			success = !ac.customerTapsMembershipCard(1);

		} while (!success);

		assertTrue(success);
		
	}
	@Test
	public void testcustomerSwipesCreditCard() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
		boolean success;
		do {
			success = ac.customerSwipesCreditCard(1);
		} while (!success);
		assertTrue(success);
		
	}
	
	@Test
	public void testcustomerSwipesCreditCardFailure() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		boolean success;
		do {
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
			success = !ac.customerSwipesCreditCard(1);

		} while (!success);
	
		assertTrue(success);
		
	}
	@Test
	public void testcustomerSwipesDebitCard() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
		boolean success;
		do {
			success = ac.customerSwipesDebitCard(1);
		} while (!success);
		assertTrue(success);
		
	}
	
	@Test
	public void testcustomerSwipesDebitCardFailure() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		boolean success;
		do {
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
			success = !ac.customerSwipesDebitCard(1);
		} while (!success);

	
		assertTrue(success);
		
	}
	@Test
	public void testcustomerSwipesMembershipCard() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
		boolean success;
		do {
			success = ac.customerSwipesMembershipCard(1);
		} while (!success);
		assertTrue(success);
		
	}
	
	@Test
	public void testcustomerSwipesMembershipCardFailure() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		boolean success;
		do {
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
			success = !ac.customerSwipesMembershipCard(1);
		} while (!success);
	
		assertTrue(success);
		
	}
	@Test
	public void testcustomerInsertCreditCard() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
		boolean success;
		do {
			success = ac.customerInsertCreditCard(1, "9423");
			ac.getSelfStations().get(0).cardReader.remove();
		} while (!success);
		assertTrue(success);		
	}
	@Test
	public void testcustomerInsertCreditCardFailure() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		boolean success;
		do {
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
			success = !ac.customerInsertCreditCard(1, "9423");
			ac.getSelfStations().get(0).cardReader.remove();

		} while (!success);
	
		assertTrue(success);		
	}
	@Test
	public void testcustomerInsertDebitCard() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
		boolean success;
		do {
			success = ac.customerInsertDebitCard(1, "9423");
			ac.getSelfStations().get(0).cardReader.remove();
		} while (!success);
		assertTrue(success);		
	}
	@Test
	public void testcustomerInsertDebitCardFailure() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		boolean success;
		do {
		  final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);
		    ac.getSelfStationSoftwares().get(0).getCustomer().addProduct(barcodedProduct);
	        ac.getSelfStations().get(0).mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
	        ac.getSelfStationSoftwares().get(0).addItem();
	        ac.getSelfStationSoftwares().get(0).checkout();
	        ac.getSelfStationSoftwares().get(0).selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);
	        success = !ac.customerInsertDebitCard(1, "9423");
			ac.getSelfStations().get(0).cardReader.remove();
		} while (!success);
	
		assertTrue(success);		
	}
	
	@Test(expected = NullPointerException.class)
	public void testRemoveItemFromCustomersCart() {
		
		ac.addNewCustomer();
		ac.customerUsesStation(2);
        PriceLookupCode cornCode = new PriceLookupCode("4055");
		ac.getSelfStationSoftwares().get(1).getCustomer().addProduct(Inventory.getProduct(cornCode), 10.0);

		ac.removeItemFromCustomersCart(1,0);


	}
	@Test
	public void testattendantLogin() {
		assertTrue(ac.attendantLogin("a", "a"));
		
	}
	@Test
	public void testattendantLoginWrongCreds() {
		assertTrue(!ac.attendantLogin("wronguser", "wrongpass"));
		
	}
	
	@Test
	public void testAttendantPassword() {
		ac.attendantLogin("Sharjeel", "1234");
		assertTrue(ac.attendantPassword("1234"));
	}
	
	@Test
	public void testAttendantPasswordWrong() {
		ac.attendantLogin("Sharjeel", "1234");
		assertTrue(!ac.attendantPassword("1235"));
	}
	
	@Test
	public void testAttendantPasswordNull() {
		assertTrue(!ac.attendantPassword("1234"));
	}
	
	@Test
	public void testgetustomerCart() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		assertTrue(ac.getCustomerCart(1) instanceof List<Product>);
	}
	@Test
	public void testAttendantIsLoggedIn() {
		ac.attendantLogin("a", "a");
		assertTrue(ac.isAttendantLoggedIn());
	}
	
	@Test
	public void testCustomerNextItem() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		assertTrue(ac.getCustomersNextItem(1) instanceof Item);
		
	}
	
	@Test
	public void testRemoveCustomerNextItem() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		assertTrue(ac.getCustomersNextItem(1) instanceof Item);
		int size = ac.getInventories().get(ac.getActiveUsers()[1]).size();
		ac.removeCustomerNextItem(1);
		assertTrue(ac.getInventories().get(ac.getActiveUsers()[1]).size() == size-1);
	}
	
	@Test
	public void testGetLastCheckedOutItem() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		Item grabbedItem = Tangibles.ITEMS.get(0); 
		ac.getInventories().get(ac.getActiveUsers()[1]).add(grabbedItem);
		Tangibles.ITEMS.remove(grabbedItem);
		Item item = ac.getInventories().get(ac.getActiveUsers()[1]).get(0);
		ac.removeCustomerNextItem(1);
		assertTrue(item == ac.getLastCheckedOutItem());
	}
	
	@Test
	public void testClearLastCheckedOutItem() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		Item grabbedItem = Tangibles.ITEMS.get(0); 
		ac.getInventories().get(ac.getActiveUsers()[1]).add(grabbedItem);
		Tangibles.ITEMS.remove(grabbedItem);
		Item item = ac.getInventories().get(ac.getActiveUsers()[1]).get(0);
		ac.removeCustomerNextItem(1);
		assertTrue(item == ac.getLastCheckedOutItem());
		ac.clearLastCheckedOutItem();
		assertTrue(ac.getLastCheckedOutItem() == null);
	}
	
	
	@Test
	public void testGetSupervisor() {
		assertTrue(AppControl.getSupervisor() == Main.Tangibles.SUPERVISION_STATION);
	}


	@Test
	public void testGetSupervisorSoftware() {
		assertTrue(ac.getSupervisorSoftware() == Store.getSupervisionSoftware());
	}
	
	@Test
	public void testGetSelfStations() {
		assertTrue(ac.getSelfStations() instanceof List<SelfCheckoutStation>);
	}
	
	@Test
	public void testGetSelfStationSoftwares() {
		assertTrue(ac.getSelfStationSoftwares() instanceof List<SelfCheckoutSoftware>);
	}

	@Test
	public void testGetStationsUserType() {
		assertTrue(ac.getStationsUserType() instanceof int[]);
	}
	
	@Test
	public void testGetInventories() {
		ac.addNewCustomer();
		ac.customerUsesStation(1);
		assertTrue(ac.getInventories().size() == 1);
	}
	
	 

}
