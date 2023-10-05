package checkout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.EmptyException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import software.SelfCheckoutSoftware;
import software.SelfCheckoutSoftware.PaymentMethod;
import software.SelfCheckoutSoftware.Phase;
import user.Customer;

/**
 * Handles the configuration of each device in a self checkout station when a
 * customer
 * is ready to checkout.
 * 
 * @author Justin Chua
 * 
 *         Calculates and dispenses change to customer when paying with cash.
 * @author joshuaplosz
 *
 *         Customer can choose to cancel their checkout, go back and add more
 *         items.
 * @author Yunfan Yang
 */
public class Checkout {
	private final SelfCheckoutSoftware scss;
	private final SelfCheckoutStation scs;
	private Customer customer;

	// This list contains "banknote" cash objects, the cash object is simply the
	// denomination of banknotes and coins
	private List<Cash> pendingChanges = new ArrayList<Cash>();

	public Checkout(SelfCheckoutSoftware scss) {
		this.scss = scss;
		this.scs = this.scss.getSelfCheckoutStation();
	}

	/**
	 * @param customer
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Customer getCustomer() {
		return customer;
	}

	/**
	 * Customer wish to proceed to checkout.
	 * Enables/disables each device in a self checkout station.
	 * 
	 * This method will only be invoked after a customer has completed inputting all
	 * of their items.
	 * 
	 */
	public void enablePaymentHardware(PaymentMethod method) {
		// devices are only configured if there is a customer at the station
		if (this.customer == null) {
			throw new IllegalStateException("No customer at checkout station.");
		}

		if (method == PaymentMethod.BANK_CARD || method == PaymentMethod.GIFT_CARD) {
			this.enableCardReader();
		} else if (method == PaymentMethod.CASH) {
			this.enableBanknoteInput();
			this.enableCoinInput();
		}
	}

	/**
	 * Customer wish to cancel checkout and go back to add more items.
	 * Enables/disables each device in a self checkout station.
	 * 
	 * This method will only be invoked after a customer is ready to checkout.
	 * of their items.
	 * 
	 */
	public void cancelCheckout() {
		// devices are only configured if there is a customer at the station
		// or the customer has not paid clear or the total is just 0
		if (this.customer == null) {
			throw new IllegalStateException("No customer at checkout station.");
		}

		BigDecimal subtotal = this.customer.getCartSubtotal();

		if (subtotal.compareTo(customer.getCashBalance()) < 1 // x.compareTo(y): returns 1 if x is < y
																// this may be backwards ^ consider the reverse when
																// testing
				&& subtotal.compareTo(BigDecimal.ZERO) != 0) {
			throw new IllegalStateException("Customer has paid clear");
		}

		this.scss.cancelCheckout();
	}

	private void enableBanknoteInput() {
		// enable all input/output devices relating to the banknote slot
		this.scs.banknoteInput.enable();
		this.scs.banknoteOutput.enable();
		this.scs.banknoteValidator.enable();
		this.scs.banknoteStorage.enable();
		this.scs.banknoteDispensers.forEach((k, v) -> v.enable());
	}

	private void enableCoinInput() {
		// enable all input/output devices relating to the coin slot
		this.scs.coinSlot.enable();
		this.scs.coinTray.enable();
		this.scs.coinValidator.enable();
		this.scs.coinStorage.enable();
		this.scs.coinDispensers.forEach((k, v) -> v.enable());
	}

	private void enableCardReader() {
		// enable all input/output devices relating to the card reader
		this.scs.cardReader.enable();
	}

	public boolean hasPendingChange() {
		return !this.pendingChanges.isEmpty();
	}

	/**
	 * Returns change to the customer by dispensing banknotes and coins,
	 * prioritizing the highest denomination value.
	 * 
	 * Coins are dispensed consecutively to the coin tray.
	 * 
	 * Banknotes are emitted one at a time, in which the BanknoteSlot assigned
	 * to output will trigger a removedEjectedBanknote event that recursively
	 * calls this method until we no longer consider banknotes for dispensing
	 * change or the change has been fully dispensed.
	 * 
	 * @param pendingChanges - total amount to be returned to the customer
	 */
	public void makeChange() {
		// Dispense remaining pending change to customer
		if (this.scss.getPhase() != Phase.PROCESSING_PAYMENT) {
			throw new IllegalStateException("Cannot make change if it currently is not processing payment");
		}

		// If customer does not have enough cash balance
		if (this.customer.getCashBalance().compareTo(this.customer.getCartSubtotal()) <= -1) {
			throw new IllegalStateException("Customer has insufficient cash balance to make change");
		}

		// If the pending change has not been calculated yet, calculate it
		if (this.pendingChanges.isEmpty()) {
			// Calculate how much change to return to customer
			BigDecimal change = this.customer.getCashBalance().subtract(this.customer.getCartSubtotal());
			this.pendingChanges = new ArrayList<Cash>(this.calculatePendingChanges(change));
		}

		// If no pending changes, return
		if (this.pendingChanges.isEmpty()) {
			this.scss.paymentCompleted();
			return;
		}

		// If there are still changes to be dispensed, dispense them
		int size = this.pendingChanges.size();

		// New pending changes list
		List<Cash> newPendingChanges = new ArrayList<Cash>(this.pendingChanges);
		
		// There's change pending to be returned to customer
		// start emitting change to slot devices
		for (Cash cash : this.pendingChanges) {
			if (cash.type.equals("banknote")) {
				try {
					this.scs.banknoteDispensers.get(cash.value.intValue()).emit();
					newPendingChanges.remove(cash);
					this.scss.setBanknoteDangling(true);
				} catch (EmptyException | DisabledException | OverloadException e) {
					continue;
				}
			} else if (cash.type.equals("coin")) {
				try {
					this.scs.coinDispensers.get(cash.value).emit();
					newPendingChanges.remove(cash);
					this.scss.setCoinInTray(true);
				} catch (OverloadException | EmptyException | DisabledException e) {
					continue;
				}
			}
		}

		this.pendingChanges = new ArrayList<Cash>(newPendingChanges);

		// If size does not change, meaning no change is successfully emmited for
		// customer, encounters error, notify attendant
		if (size <= newPendingChanges.size()) {
			this.scss.errorOccur();
			this.scss.getSupervisionSoftware()
					.notifyObservers(observer -> observer.dispenseChangeFailed(this.scss));
			return;
		}

		// If the last one is dispensed, to next phase
		if (this.pendingChanges.isEmpty()) {
			this.scss.paymentCompleted();
			return;
		}
	}

	private List<Cash> calculatePendingChanges(BigDecimal change) {
		// Clear pending change list
		List<Cash> pendingChanges = new ArrayList<Cash>();

		// No change needs to be returned to customer
		if (change.equals(BigDecimal.ZERO)) {
			return pendingChanges;
		}

		// Establish what banknote and coin denominations are available
		ArrayList<Cash> availableDenominations = new ArrayList<>();
		ArrayList<Cash> acceptableDenominations = new ArrayList<>();

		// go through all banknotes and record all the accepted as well as the available
		// notes
		this.scs.banknoteDispensers.forEach((value, dispenser) -> {
			Cash cash = new Cash(value);
			acceptableDenominations.add(cash);

			if (dispenser.size() > 0) {
				availableDenominations.add(cash);
			}
		});

		// same thing with coins
		this.scs.coinDispensers.forEach((value, dispenser) -> {
			Cash cash = new Cash(value);
			acceptableDenominations.add(cash);

			if (dispenser.size() > 0) {
				availableDenominations.add(cash);
			}
		});

		// sort the accepted and available denominations to get the lowest possible cash
		// value
		Collections.sort(acceptableDenominations);
		Collections.sort(availableDenominations, Collections.reverseOrder());

		// Starting from the highest value available denomination, dispense denomination
		// to customer
		// and substract value from 'change' only if the 'change' amount is not less
		// than it's value.
		// Otherwise, remove denomination from consideration.
		while (availableDenominations.size() > 0 && change.compareTo(BigDecimal.ZERO) > 0) {
			Cash cash = availableDenominations.get(0);

			if (change.compareTo(cash.value) >= 0) {
				// Add this to the pending change list
				pendingChanges.add(new Cash(cash));
				change = change.subtract(cash.value);
			} else {
				// current denomination is bigger than 'change' amount. Remove it from
				// consideration.
				availableDenominations.remove(0);
			}
		}

		return pendingChanges;
	}

	/**
	 * Encapsulates banknotes and coins into a single class.
	 * Used to provide a generic interface for calculating customers change.
	 * 
	 * @author joshuaplosz
	 *
	 */
	private class Cash implements Comparable<Cash> {
		String type;
		BigDecimal value;

		Cash(Integer value) {
			type = "banknote";
			this.value = new BigDecimal(value);
		}

		Cash(BigDecimal value) {
			type = "coin";
			this.value = value;
		}

		Cash(Cash copy){
			this.type = copy.type;
			this.value = copy.value;
		}

		@Override
		public int compareTo(Cash other) {
			return this.value.compareTo(other.value);
		}
	}
	
}
