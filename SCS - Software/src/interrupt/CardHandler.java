package interrupt;

import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.devices.*;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.CardReaderObserver;
import org.lsmr.selfcheckout.external.CardIssuer;

import bank.Bank;
import software.SelfCheckoutSoftware;
import store.GiftCard;
import store.Membership;
import user.Customer;

/**
 * This class handles interactions with the CardReader hardware.
 * This includes, payment and membership cards.
 * 
 * @author Tyler Chen
 *
 */
public class CardHandler extends Handler implements CardReaderObserver {

	private final SelfCheckoutSoftware scSoftware;
	private final SelfCheckoutStation scStation;
	private Customer customer;

	/*
	 * Constructor for creating a CardHandler. Attaches itself to the cardReader.
	 */
	public CardHandler(SelfCheckoutSoftware scSoftware) {
		this.scSoftware = scSoftware;
		this.scStation = this.scSoftware.getSelfCheckoutStation();

		this.attachAll();
		this.enableHardware();
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public void attachAll() {
		this.scStation.cardReader.attach(this);
	}

	/**
	 * Used to reboot/shutdown the software. Detatches the handler so that
	 * we can stop listening or assign a new handler.
	 */
	public void detatchAll() {
		this.scStation.cardReader.detach(this);
	}

	/**
	 * Used to enable all the associated hardware.
	 */
	public void enableHardware() {
		this.scStation.cardReader.enable();
	}

	/**
	 * Used to disable all the associated hardware.
	 */
	public void disableHardware() {
		this.scStation.cardReader.disable();
	}

	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// we don't have to do anything when the device is enabled

	}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// we don't have to do anything when the device is disabled
	}

	/**
	 * We don't care about the following events:
	 * - cardInserted
	 * - cardRemoved
	 * - cardTapped
	 * - cardSwiped
	 * Because we only want to disable the device when a card data is actually read,
	 * and re-enable the device when the transaction is complete or failed.
	 */
	@Override
	public void cardInserted(CardReader reader) {
	}

	@Override
	public void cardRemoved(CardReader reader) {
	}

	@Override
	public void cardTapped(CardReader reader) {
	}

	@Override
	public void cardSwiped(CardReader reader) {
	}

	/**
	 * On a successful card inserted, tapped, or swiped, we read the data on the
	 * card.
	 * The card can be debit, credit or a membership card.
	 * If the card was a membership card, we only want the numbers.
	 * If the card was debit, credit then we will attempt to charge the card.
	 * If the card was swiped, we cannot get the cvv.
	 * After completion of payment
	 */
	@Override
	public void cardDataRead(CardReader reader, CardData data) {
		// The card data is read, so disable the device until the transaction is
		// complete.
		this.scStation.cardReader.disable();

		// Get the type of card first and strip all whitespace and make it lowercase
		String type = data.getType().toLowerCase().strip();

		if (type.equals("membership")) {
			this.processMembership(reader, data);
		} else if (type.equals("debit") || type.equals("credit") || type.equals("gift")) {
			this.processPayment(reader, data, type);
		} else {
			this.scSoftware.notifyObservers(observer -> observer.invalidCardTypeDetected());
		}

		// Re-enable card reader since transaction is complete or failed.
		this.scStation.cardReader.enable();

		// Variables will be reset after when the next customer is binded.
	}
	
	private void processMembership(CardReader reader, CardData data) {
		String memberID = data.getNumber();
		boolean isMember = Membership.isMember(memberID);

		if (!isMember) {
			this.scSoftware.notifyObservers(observer -> observer.invalidMembershipCardDetected());
			return;
		}

		this.customer.setMemberID(memberID);
		this.scSoftware.notifyObservers(observer -> observer.membershipCardDetected(memberID));
	}
	
	private void processPayment(CardReader reader, CardData data, String type) {
		String cardNumber = data.getNumber();
		CardIssuer issuer = null;
		if (type.equals("gift")) {
			if (GiftCard.isGiftCard(data.getNumber())) {
				issuer = GiftCard.getCardIssuer();
			} else {
				this.scSoftware.notifyObservers(observer -> observer.invalidGiftCardDetected());
				this.scSoftware.paymentCompleted();
				return;
			}
		} else if (type.equals("debit") || type.equals("credit")) {
			issuer = Bank.getCardIssuer(cardNumber);
		}
		
		int holdNumber = issuer.authorizeHold(cardNumber, this.customer.getCartSubtotal());

		// Fail to hold the authorization
		if (holdNumber == -1) {
			this.scSoftware.notifyObservers(observer -> observer.paymentHoldingAuthorizationFailed());
			return;
		}

		boolean posted = issuer.postTransaction(cardNumber, holdNumber, this.customer.getCartSubtotal());

		// Fail to post transaction
		if (!posted) {
			this.scSoftware.notifyObservers(observer -> observer.paymentPostingTransactionFailed());
			return;
		}

		this.scSoftware.paymentCompleted(); // Transaction is complete, go to idle state
		this.scSoftware.notifyObservers(observer -> observer.paymentCompleted());
	}
}