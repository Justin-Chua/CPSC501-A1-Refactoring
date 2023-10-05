package GUI;

import java.math.BigDecimal;
import java.util.List;

import javax.swing.JFrame;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.PLUCodedItem;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

import application.AppControl;
import application.Main;
import software.SelfCheckoutSoftware.PaymentMethod;
import software.SelfCheckoutSoftware.Phase;
import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import store.Inventory;
import store.Membership;
import store.Store;
import store.credentials.AuthorizationRequiredException;

import user.Customer;
import user.User;
import java.util.Currency;

public class GUI {

	private static AppControl ac;
	private static Scenes scenes = new Scenes();
	private static GUIObserver observer = new GUIObserver();

	private GUI() {
	}

	public static void init(AppControl appControl) {
		ac = appControl;
		// Initializes the openning scene, Self-Checkout Overview
		scenes.getScene(Scenes.SC_OVERVIEW);

		Store.getSupervisionSoftware().addObserver(observer);
		for (SelfCheckoutSoftware scs : Store.getSelfCheckoutSoftwareList()) {
			scs.addObserver(observer);
		}
	}

	public static void close() {
		System.exit(0);
	}

	/**
	 * checks what kind of user to add to the simulation.
	 * only one attendant can be allowed at one time
	 * 
	 * @param newUserType
	 * @return true if user successfully added, false otherwise
	 */
	public static boolean newUser(int newUserType) {
		if (newUserType == AppControl.CUSTOMER) {
			ac.addNewCustomer();
			return true;
		} else if (newUserType == AppControl.ATTENDANT) {
			// check list of users for an existing attendant
			User[] users = ac.getActiveUsers();

			for (int i = 0; i < users.length; i++) {
				if (users[i] != null && users[i].getUserType() == AppControl.ATTENDANT) {
					Scenes.errorMsg("An Attendant is already on duty. Sorry.");
					return false;
				}
			}

			// no attendant found, add a new one
			ac.addNewAttendant();
			return true;
		}
		return false;
	}

	/**
	 * checks if the provided station is free to use by the
	 * active user before letting them proceed.
	 * 
	 * @param station - the specific station index
	 * @return true if station is free, false otherwise
	 */
	public static boolean userApproachesStation(int station) {
		if (ac.getActiveUser().getUserType() == AppControl.CUSTOMER) {
			if (station == 0) { // this is the attendant's station
				Scenes.errorMsg("You are not authorized to view the attendant station.");
				return false;
			} else if (ac.getUserAt(station) != null) {
				if (ac.getUserAt(station).getUserType() == AppControl.ATTENDANT) {
					Scenes.errorMsg("Station being serviced");
					return false;
				} else if (ac.getUserAt(station).getUserType() == AppControl.CUSTOMER) {
					Scenes.errorMsg("A customer is already using this station");
					return false;
				}
			} else {
				ac.customerUsesStation(station);
				scenes.setCurrentStation(station);
				scenes.getScene(Scenes.SCS_OVERVIEW);
				return true;
			}
		} else if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			if (station == 0) { // this is the attendant's station
				scenes.setCurrentStation(station);
				scenes.getScene(Scenes.AS_TOUCH);
				return true;
			} else {
				if (ac.isAttendantLoggedIn()) {
					ac.attendantUsesStation(station);
					scenes.setCurrentStation(station);
					scenes.getScene(Scenes.SCS_OVERVIEW);
					return true;
				} else {
					Scenes.errorMsg("Please log in at the attendant station");
					return false;
				}
			}
		}

		return false;
	}

	public static void userLeavesStation(int station) {
		if (ac.getActiveUser().getUserType() == AppControl.CUSTOMER) {
			ac.customerLeavesStation(station);
			scenes.newUserPrompt = -1; // dirty way of getting the system to prompt for new user type
		} else if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			ac.attendantLeavesStation(station);
		}
	}

	//
	public static void userBagsItem(int currentStation) {
		SelfCheckoutSoftware software = ac.getSelfCheckoutSoftware(currentStation);
		SelfCheckoutStation hardware = software.getSelfCheckoutStation();

		Item item = ac.getLastCheckedOutItem();
		if (item != null) {
			hardware.baggingArea.add(item);
		}
		ac.clearLastCheckedOutItem();
	}

	public static void userInsertsBanknote(int currentStation, int value) {
		if (ac.getStationPhase(currentStation).equals(Phase.CHOOSING_PAYMENT_METHOD)) {
			SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
			scs.selectedPaymentMethod(PaymentMethod.CASH);
			try {
				scs.getSelfCheckoutStation().banknoteInput.accept(new Banknote(Main.Configurations.currency, value));
				if (scs.getCustomer().hasSufficientCashBalance()) {
					scs.makeChange();
				}
			} catch (DisabledException e) {
				e.printStackTrace();
			} catch (OverloadException e) {
				e.printStackTrace();
			}
		}
	}

	public static void userRemovesBanknote(int currentStation) {
		SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
		SelfCheckoutStation scs = scss.getSelfCheckoutStation();
		if (ac.getActiveUser().getUserType() == AppControl.CUSTOMER
				&& (scss.getPhase() == Phase.PAYMENT_COMPLETE || scss.getPhase() == Phase.PROCESSING_PAYMENT)
				&& !(scs.banknoteOutput.hasSpace())) {
			if (scss.getBanknoteDangling()) {
				scss.setBanknoteDangling(false);
				scs.banknoteOutput.removeDanglingBanknotes();
				System.out.println("bill taken"); // TODO
			}
		}
	}

	public static void userServicesStation(int currentStation) {
		if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			// we assume the attendant has the key to the station
			scenes.getScene(Scenes.SCS_MAINTENANCE);
		}
	}

	public static void userInsertsCoin(int currentStation, BigDecimal value) {
		SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());

		if (ac.getStationPhase(currentStation) == Phase.CHOOSING_PAYMENT_METHOD) {
			scs.selectedPaymentMethod(PaymentMethod.CASH);
		}

		if (ac.getStationPhase(currentStation) == Phase.CHOOSING_PAYMENT_METHOD ||
				ac.getStationPhase(currentStation) == Phase.PROCESSING_PAYMENT) {
			try {
				scs.getSelfCheckoutStation().coinSlot.accept(new Coin(Main.Configurations.currency, value));
				if (scs.getCustomer().hasSufficientCashBalance()) {
					scs.makeChange();
				}
				if (scs.getCustomer().hasSufficientCashBalance()) {
					scs.makeChange();
				}
			} catch (DisabledException e) {
				e.printStackTrace();
			} catch (OverloadException e) {
				e.printStackTrace();
			}

		}
	}

	public static void userRemovesCoins(int currentStation) {
		SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
		SelfCheckoutStation scs = scss.getSelfCheckoutStation();
		if (ac.getActiveUser().getUserType() == AppControl.CUSTOMER &&
				scss.getPhase() == Phase.PAYMENT_COMPLETE) {
			if (scss.getCoinInTray()) {
				scs.coinTray.collectCoins();
				scss.setCoinInTray(false);
				System.out.println("Coins taken");
			}
		}
	}

	public static void userScansItem(int currentStation, boolean usedMainScanner) {
		if (ac.getStationPhase(currentStation) == Phase.SCANNING_ITEM
				|| ac.getStationPhase(currentStation) == Phase.CHOOSING_PAYMENT_METHOD) {
			Item item = ac.getCustomersNextItem(currentStation);
			if (item instanceof BarcodedItem) {
				try {

					SelfCheckoutSoftware software = ac.getSelfCheckoutSoftware(currentStation);
					SelfCheckoutStation hardware = software.getSelfCheckoutStation();

					software.addItem();

					while (ac.getStationPhase(currentStation) != Phase.BAGGING_ITEM) {
						if (usedMainScanner) {
							hardware.mainScanner.scan(item);
						} else {
							hardware.handheldScanner.scan(item);
						}
					}

					ac.removeCustomerNextItem(currentStation);
				} catch (Exception e) {
					Scenes.errorMsg("You cannot scan this item");
				}
			}
		}
	}

	public static void userRemovesReceipt(int currentStation) {
		SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
		SelfCheckoutStation scs = scss.getSelfCheckoutStation();
		if (ac.getActiveUser().getUserType() == AppControl.CUSTOMER
				&& scss.getPhase() == Phase.PAYMENT_COMPLETE || scss.getPhase() == Phase.IDLE) {
			try {
				String receipt = scs.printer.removeReceipt();
				System.out.println("\n\n" + receipt + "\n");
			} catch (Exception e) {
				Scenes.errorMsg("You are trying to remove a non-existent receipt");
			}
		}
	}

	public static void userAccessCardReader(int currentStation) {
		if (ac.getActiveUser().getUserType() == AppControl.CUSTOMER) {
			scenes.getScene(Scenes.SCS_CARDREADER);
		} else if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {

		}
	}

	public static void userAccessTouchscreen(int currentStation) {
		if (ac.getStationPhase(currentStation) == Phase.SCANNING_ITEM
				|| ac.getStationPhase(currentStation) == Phase.BAGGING_ITEM
				|| ac.getStationPhase(currentStation) == Phase.CHOOSING_PAYMENT_METHOD) {
			scenes.getScene(Scenes.SCS_TOUCH);
		} else if (ac.getStationPhase(currentStation) == Phase.BLOCKING) {
			Scenes.errorMsg("Station is blocked.  Wait for an attendant.");
		} else if (ac.getStationPhase(currentStation) == Phase.PLACING_OWN_BAG) {
			Scenes.errorMsg("Wait for an attendant to approve your bags.");
		}
	}

	public static void attendantLogsOut() {
		SupervisionSoftware svs = Store.getSupervisionSoftware();
		svs.logout();
	}

	public static void removePaidItemsFromBagging() {
		SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
		scs.checkoutComplete();
	}

	/**
	 * 
	 * @param station
	 * @return
	 */
	public static String stationStatus(int station) {

		return ac.getStationState(station);
	}

	/**
	 * 
	 * @param station
	 */
	public static void attendantBlockToggle(int station) {

		ac.toggleBlock(station);
	}

	/**
	 * 
	 * @param station
	 */
	public static void attendantApprovesStation(int station) {

		ac.approveStationDiscrepancy(station);
	}

	public static void startupStation(int station) {
		try {
			Store.getSupervisionSoftware().startUpStation(ac.getSelfCheckoutSoftware(station));
			System.out.println("System started");
		} catch (AuthorizationRequiredException e) {
			e.printStackTrace();
		}
	}

	public static void userTapsCard(int cardType) {
		SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
		if (ac.getStationPhase(scenes.getCurrentStation()).equals(Phase.CHOOSING_PAYMENT_METHOD)) {
			if (cardType == AppControl.CREDIT) {
				scs.selectedPaymentMethod(PaymentMethod.BANK_CARD);
				ac.customerTapsCreditCard(scenes.getCurrentStation());
			} else if (cardType == AppControl.DEBIT) {
				scs.selectedPaymentMethod(PaymentMethod.BANK_CARD);
				ac.customerTapsDebitCard(scenes.getCurrentStation());
			}
		} else if (cardType == AppControl.MEMBERSHIP) {
			ac.customerTapsMembershipCard(scenes.getCurrentStation());
		}
	}

	public static void userSwipesCard(int cardType) {
		SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
		if (ac.getStationPhase(scenes.getCurrentStation()).equals(Phase.CHOOSING_PAYMENT_METHOD)) {
			if (cardType == AppControl.CREDIT) {
				scs.selectedPaymentMethod(PaymentMethod.BANK_CARD);
				ac.customerSwipesCreditCard(scenes.getCurrentStation());
			}
			if (cardType == AppControl.DEBIT) {
				scs.selectedPaymentMethod(PaymentMethod.BANK_CARD);
				ac.customerSwipesDebitCard(scenes.getCurrentStation());
			}
			if (cardType == AppControl.MEMBERSHIP) {
				ac.customerSwipesMembershipCard(scenes.getCurrentStation());
			}
		}

	}

	public static void userInsertCard(int cardType, String pin) {
		SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
		if (ac.getStationPhase(scenes.getCurrentStation()).equals(Phase.CHOOSING_PAYMENT_METHOD)) {
			if (cardType == AppControl.CREDIT) {
				scs.selectedPaymentMethod(PaymentMethod.BANK_CARD);
				ac.customerInsertCreditCard(scenes.getCurrentStation(), pin);
			}
			if (cardType == AppControl.DEBIT) {
				scs.selectedPaymentMethod(PaymentMethod.BANK_CARD);
				ac.customerInsertDebitCard(scenes.getCurrentStation(), pin);
			}
		}

	}

	public static void userSkipsBagging() {
		SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
		scss.notBaggingItem();
		ac.skipBagging(scenes.getCurrentStation());
	}

	public static void refillBanknoteDispensers() {
		int currentStation = scenes.getCurrentStation();
		SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
		SelfCheckoutStation scs = scss.getSelfCheckoutStation();

		int[] banknoteDenoms = scs.banknoteDenominations;
		Currency currency = Currency.getInstance("CAD");

		if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			// For every dispenser (there is one dispenser for each banknote denomination)
			for (int denom : banknoteDenoms) {
				int numBillsInDispenser = scs.banknoteDispensers.get(denom).size();
				int dispenserCapacity = SelfCheckoutStation.BANKNOTE_DISPENSER_CAPACITY;

				Banknote note = new Banknote(currency, denom);

				while (numBillsInDispenser != dispenserCapacity) {
					try {
						scs.banknoteDispensers.get(denom).load(note);
					} catch (OverloadException e) {
						e.printStackTrace();
					}
					numBillsInDispenser++;

				}
				System.out.println(denom + ": " + numBillsInDispenser);
			}
		}

	}

	public static void refillCoinDispenser() {
		int currentStation = scenes.getCurrentStation();
		SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
		SelfCheckoutStation scs = scss.getSelfCheckoutStation();

		List<BigDecimal> coinDenoms = scs.coinDenominations;
		Currency currency = Currency.getInstance("CAD");

		if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			// For every dispenser (there is one dispenser for each banknote denomination)
			for (BigDecimal denom : coinDenoms) {
				int numCoinsInDispenser = scs.coinDispensers.get(denom).size();
				int dispenserCapacity = scs.COIN_DISPENSER_CAPACITY;

				Coin coin = new Coin(currency, denom);

				while (numCoinsInDispenser != dispenserCapacity) {
					try {
						scs.coinDispensers.get(denom).load(coin);
					} catch (OverloadException e) {
						e.printStackTrace();
					}
					numCoinsInDispenser++;
				}
				System.out.println(denom + ": " + numCoinsInDispenser);
			}
		}

	}

	public static void addPaper(int currentStation) {
		if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
			SelfCheckoutStation scs = scss.getSelfCheckoutStation();
			try {
				if (scss.getPaperUsed() == 0) {
					Scenes.errorMsg("The paper cartridge is already full");
				} else {
					// refill the printer to max capacity
					scs.printer.addPaper(scss.getPaperUsed());
					scss.resetPaperUsed();
				}
			} catch (OverloadException e) {
				// overload exception should never be thrown
			}
		}
	}

	public static void addInk(int currentStation) {
		if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
			SelfCheckoutStation scs = scss.getSelfCheckoutStation();
			try {
				if (scss.getInkUsed() == 0) {
					Scenes.errorMsg("The ink cartridge is already full");
				} else {
					scs.printer.addInk(scss.getInkUsed());
					scss.resetInkUsed();
				}
			} catch (OverloadException e) {
				// overload exception should never be thrown
			}
		}
	}

	/*
	 * Emptying the banknote storage is done with a key, but we assume the attendant
	 * would have
	 * this. This can happen during any phase
	 */
	public static void emptyBanknoteStorage() {

		int currentStation = scenes.getCurrentStation();
		SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
		SelfCheckoutStation scs = scss.getSelfCheckoutStation();

		if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			scs.banknoteStorage.unload();
		}

	}

	/*
	 * Emptying the coin storage is done with a key, but we assume the attendant
	 * would have
	 * this. This can happen during any phase
	 */
	public static void emptyCoinStorage() {
		int currentStation = scenes.getCurrentStation();
		SelfCheckoutSoftware scss = ac.getSelfCheckoutSoftware(currentStation);
		SelfCheckoutStation scs = scss.getSelfCheckoutStation();
		if (ac.getActiveUser().getUserType() == AppControl.ATTENDANT) {
			scs.coinStorage.unload();
		}

	}

	public static void proceedToCheckout() {
		SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
		scs.getCustomer().setPlasticBags(scenes.promptNumOfBags());
		try {
			scs.checkout();
		} catch (IllegalStateException e) {

		}
	}

	public static void userUsesOwnBags(int currentStation) {
		Item bag = new PLUCodedItem(new PriceLookupCode("0000"), 5.0);
		ac.getSelfCheckoutSoftware(currentStation).addOwnBag();
		ac.getSelfCheckoutSoftware(currentStation).getSelfCheckoutStation().baggingArea.add(bag);
	}

	public static void userEntersMembership(int num) {
		if (Membership.isMember(Integer.toString(num))) {
			SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
			scs.getCustomer().setMemberID(Integer.toString(num));
		}
	}

	/**
	 * user has selected a particular item from the search menu
	 * 
	 * @param pluCodedProduct
	 */
	public static void selectedItem(PLUCodedProduct pluCodedProduct) {
		int code = 0;
		code += (int) pluCodedProduct.getPLUCode().getNumeralAt(0).getValue() * 1000;
		code += (int) pluCodedProduct.getPLUCode().getNumeralAt(1).getValue() * 100;
		code += (int) pluCodedProduct.getPLUCode().getNumeralAt(2).getValue() * 10;
		code += (int) pluCodedProduct.getPLUCode().getNumeralAt(3).getValue();
		userEntersPLUCode(code, scenes.getCurrentStation());
	}

	public static void userEntersPLUCode(int code, int currentStation) {
		try {
			Item item = ac.getCustomersNextItem(currentStation);

			PriceLookupCode plu = new PriceLookupCode(Integer.toString(code));
			PLUCodedItem pluItem = (PLUCodedItem) ac.getCustomersNextItem(currentStation);

			// for simulation purpose only
			// if (!plu.equals(pluItem.getPLUCode())) {
			// Scenes.errorMsg("PLU Code does not match.");
			// return;
			// }

			// check if the PLU exists in the Inventory
			if (Inventory.getProduct(plu).getPLUCode().equals(plu)) {
				// get software and set phase
				SelfCheckoutSoftware software = ac.getSelfCheckoutSoftware(currentStation);
				software.addPLUItem();

				// get the customer and set the PLU code
				Customer customer = software.getCustomer();
				customer.enterPLUCode(plu);

				// get the hardware and weighs the item.
				SelfCheckoutStation hardware = software.getSelfCheckoutStation();
				hardware.scanningArea.add(item);

				ac.removeCustomerNextItem(currentStation);
				hardware.scanningArea.remove(item);
			} else
				Scenes.errorMsg("PLU code does not exist!");
		} catch (Exception e) {
			Scenes.errorMsg("The item you're trying to checkout is not a PLU item");
		}
	}

	public static boolean attendantPassword(String password) {

		return ac.attendantPassword(password);
	}

	public static void removeItem(int station, int index) {
		ac.removeItemFromCustomersCart(station, index);
	}

	public static void shutdownStation(int stationNumber) throws AuthorizationRequiredException {
		Store.getSupervisionSoftware().shutDownStation(ac.getSelfCheckoutSoftware(stationNumber));
		System.out.println("System stopped");

	}

	public static boolean attendantLogin(String name, String password) {

		return ac.attendantLogin(name, password);
	}

	public static List<Product> getBaggedItems(int station) {
		return ac.getCustomerCart(station);
	}

	/**
	 * Simulate the user at the previous station
	 */
	public static void selectPreviousUser() {
		ac.prevActiveUser();
		updateScene(ac.getActiveUsersStation());
	}

	/**
	 * Simulate the user at the next station
	 */
	public static void selectNextUser() {
		ac.nextActiveUser();
		updateScene(ac.getActiveUsersStation());
	}

	/**
	 * Take me to the scene of the currently selected user.
	 * 
	 * @param station
	 */
	private static void updateScene(int station) {
		scenes.setCurrentStation(station);
		if (station == -1) {
			return;
		} else if (station == 0) {
			scenes.getScene(Scenes.AS_TOUCH);
		} else {
			scenes.getScene(Scenes.SCS_OVERVIEW);
		}
	}

	public void customerDoesNotWantToBagItem() {
		SelfCheckoutSoftware scs = ac.getSelfCheckoutSoftware(scenes.getCurrentStation());
		scs.notBaggingItem();
	}

	public static boolean isAttendantLoggedIn() {
		return ac.isAttendantLoggedIn();
	}

	public static String getAmountPaid(int station) {
		String paid = ac.getCustomerPaidAmount(station);
		if (paid == null) {
			return "Paid $0.00";
		}
		return "Paid $" + paid;
	}

	public static String getSubtotal(int station) {
		String subtotal = ac.getCustomersSubtotal(station);
		if (subtotal == null) {
			return "Subtotal $0.00";
		}
		return "Subtotal $" + subtotal;
	}

	public static String getNextItemDescription(int station) {
		String desc = "";
		Item item = ac.getCustomersNextItem(station);

		if (item == null)
			return "";

		if (item instanceof PLUCodedItem) {
			PLUCodedItem pluItem = (PLUCodedItem) item;
			PLUCodedProduct p = Inventory.getProduct(pluItem.getPLUCode());
			desc = "<html>PLU Coded Item<br>";
			desc += p.getDescription() + "  $" + p.getPrice() + "/kg";
			desc += "<br>Code: " + p.getPLUCode() + "</html>";
		} else if (item instanceof BarcodedItem) {
			BarcodedItem barItem = (BarcodedItem) item;
			BarcodedProduct b = Inventory.getProduct(barItem.getBarcode());
			desc = "<html>Barcoded Item<br>";
			desc += b.getDescription() + " " + b.getPrice() + "</html>";
		}
		return desc;
	}

	/**
	 * 
	 * @return
	 */
	public static String getUserInstruction(int scene) {
		String instruction = null;

		if (ac.getStationPhase(scenes.getCurrentStation()) == Phase.CHOOSING_PAYMENT_METHOD) {
			instruction = "Insert Banknote/Coin or pay with Card";
		} else if (ac.getStationPhase(scenes.getCurrentStation()) == Phase.PAYMENT_COMPLETE) {
			instruction = "Take Change and Receipt";
		} else if (ac.getStationPhase(scenes.getCurrentStation()) == Phase.BAGGING_ITEM) {
			instruction = "Put item in bagging area or request to Skip Bagging";
		} else {
			Item item = ac.getCustomersNextItem(scenes.getCurrentStation());
			if (item instanceof PLUCodedItem) {
				instruction = "Look up Product or enter PLU code on Touchscreen";
			} else if (item instanceof BarcodedItem) {
				instruction = "Scan Barcoded Item";
			}
		}
		return instruction;
	}

	public static Phase getPhase(int stationNumber) {
		System.out.println(ac.getSelfCheckoutSoftware(stationNumber).getPhase()); // TODO remove after testing
		return ac.getSelfCheckoutSoftware(stationNumber).getPhase();
	}
}
