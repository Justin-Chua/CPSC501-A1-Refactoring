package interrupt;

import java.math.BigDecimal;
import java.util.Currency;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BanknoteDispenser;
import org.lsmr.selfcheckout.devices.BanknoteSlot;
import org.lsmr.selfcheckout.devices.BanknoteStorageUnit;
import org.lsmr.selfcheckout.devices.BanknoteValidator;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.*;

import software.SelfCheckoutSoftware;
import user.Customer;

/**
 * Handles the interupts from banknote related hardware.
 * Hardware such as:
 * BanknoteDispenser
 * BanknoteSlot
 * BanknoteStorageUnit
 * BanknoteValidator
 * 
 * Communicates between SelfCheckoutStation and Customer
 * if a Customer currently exists.
 * 
 * @author joshuaplosz
 *
 */
public class BanknoteHandler extends Handler implements BanknoteDispenserObserver, BanknoteSlotObserver,
		BanknoteStorageUnitObserver, BanknoteValidatorObserver {

	private final SelfCheckoutSoftware scss;
	private final SelfCheckoutStation scs;
	private Customer customer;

	// record latest processed banknote(bn)
	private boolean banknoteDetected = false;
	private BigDecimal banknoteValue = BigDecimal.ZERO;

	public BanknoteHandler(SelfCheckoutSoftware scss) {
		this.scss = scss;
		this.scs = this.scss.getSelfCheckoutStation();

		this.attachAll();
		this.enableHardware();
	}

	/**
	 * Sets the current customer to receive notifications from hardware events
	 * 
	 * @param customer
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
		this.banknoteDetected = false;
		this.banknoteValue = BigDecimal.ZERO;
	}

	public void attachAll() {
		// attaches itself as an observer to all related hardware
		this.scs.banknoteInput.attach(this);
		this.scs.banknoteOutput.attach(this);
		this.scs.banknoteValidator.attach(this);
		this.scs.banknoteDispensers.forEach((k, v) -> v.attach(this));
		this.scs.banknoteStorage.attach(this);
	}

	/**
	 * Gets the current customer using the station
	 * 
	 * @return null if no customer exists
	 *         the current customer if one exists
	 */
	public Customer getCustomer() {
		return this.customer;
	}

	/**
	 * Used to reboot/shutdown the software. Detatches the handler so that
	 * we can stop listening or assign a new handler.
	 */
	public void detatchAll() {
		this.scs.banknoteInput.detach(this);
		this.scs.banknoteOutput.detach(this);
		this.scs.banknoteValidator.detach(this);
		this.scs.banknoteDispensers.forEach((k, v) -> v.detach(this));
		this.scs.banknoteStorage.detach(this);
	}

	/**
	 * Used to enable all the associated hardware in a single function.
	 */
	public void enableHardware() {
		this.scs.banknoteInput.enable();
		this.scs.banknoteOutput.enable();
		this.scs.banknoteStorage.enable();
		this.scs.banknoteValidator.enable();
		this.scs.banknoteDispensers.forEach((k, v) -> v.enable());
	}

	/**
	 * Used to disable all the associated hardware in a single function.
	 */
	public void disableHardware() {
		this.scs.banknoteInput.disable();
		this.scs.banknoteOutput.disable();
		this.scs.banknoteStorage.disable();
		this.scs.banknoteValidator.disable();
		this.scs.banknoteDispensers.forEach((k, v) -> v.disable());
	}

	public boolean isBanknoteDetected() {
		return this.banknoteDetected;
	}

	public BigDecimal getBanknoteValue() {
		return this.banknoteValue;
	}

	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
	}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
	}

	/**
	 * Sets flag to acknowledge received banknote and updates the current banknotes
	 * value.
	 */
	@Override
	public void validBanknoteDetected(BanknoteValidator validator, Currency currency, int value) {
		this.banknoteDetected = true;
		this.banknoteValue = BigDecimal.valueOf(value);
	}

	/**
	 * Sends the customer an invalid banknote notification
	 */
	@Override
	public void invalidBanknoteDetected(BanknoteValidator validator) {
		this.banknoteDetected = false;
		this.banknoteValue = BigDecimal.ZERO;
		this.scss.notifyObservers(observer -> observer.invalidBanknoteDetected());
	}

	/**
	 * Disables the banknote input slot
	 */
	@Override
	public void banknotesFull(BanknoteStorageUnit unit) {
		this.scs.banknoteInput.disable();
		this.scss.notifyObservers(observer -> observer.banknoteStorageFull());
		this.scss.getSupervisionSoftware().notifyObservers(observer -> observer.banknoteStorageFull(scss));
	}

	/**
	 * Add value of current banknote to the customers accumlated currency
	 * when a new banknote has been previously detected via validBanknoteDetected.
	 */
	@Override
	public void banknoteAdded(BanknoteStorageUnit unit) {
		if (customer != null && this.banknoteDetected) {
			this.customer.addCashBalance(banknoteValue);

			// Notify observer so GUI can update current cash balance on display
			this.scss.notifyObservers(observer -> observer.banknoteAdded());
		}

		this.banknoteDetected = false;
		this.banknoteValue = BigDecimal.ZERO;
	}

	@Override
	public void banknotesLoaded(BanknoteStorageUnit unit) {
		// We don't currently do anything when banknote storage units are loaded
	}

	@Override
	public void banknotesUnloaded(BanknoteStorageUnit unit) {
		// We don't currently do anything when banknote storage units are loaded
	}

	@Override
	public void banknoteInserted(BanknoteSlot slot) {
		// We don't currently do anything when a banknote is inserted
	}

	/**
	 * An event announcing that one or more banknotes have been returned to the
	 * user, dangling from the slot.
	 *
	 * @param slot The device on which the event occurred.
	 */
	@Override
	public void banknotesEjected(BanknoteSlot slot) {
	}

	/**
	 * Removes the banknote ejected notification from the customer.
	 * 
	 * If the checkout is in the middle of dispensing change to the customer
	 * and they have removed a banknote from the output slot then continue
	 * dispensing change.
	 */
	@Override
	public void banknoteRemoved(BanknoteSlot slot) {
		// Customer removed a banknote from banknote output
		// And Checkout keep making change to the customer in case there are pending
		// banknote not returned to customer yet
		if (slot.equals(this.scs.banknoteOutput) && this.scss.hasPendingChanges()) {
			this.scss.makeChange();
		}
	}

	@Override
	public void moneyFull(BanknoteDispenser dispenser) {
	}

	@Override
	public void banknotesEmpty(BanknoteDispenser dispenser) {
		this.scss.notifyObservers(observer -> observer.banknoteDispenserEmpty());
		this.scss.getSupervisionSoftware().notifyObservers(observer -> observer.banknoteDispenserEmpty(this.scss));
	}

	@Override
	public void billAdded(BanknoteDispenser dispenser, Banknote banknote) {
		// We don't currently do anything with the banknote dispenser
	}

	@Override
	public void banknoteRemoved(BanknoteDispenser dispenser, Banknote banknote) {
		// We don't currently do anything with the banknote dispenser
	}

	@Override
	public void banknotesLoaded(BanknoteDispenser dispenser, Banknote... banknotes) {
		// We don't currently do anything with the banknote dispenser
	}

	@Override
	public void banknotesUnloaded(BanknoteDispenser dispenser, Banknote... banknotes) {
		// We don't currently do anything with the banknote dispenser
	}
}
