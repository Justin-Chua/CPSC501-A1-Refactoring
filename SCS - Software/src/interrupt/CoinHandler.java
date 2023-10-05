package interrupt;

import java.math.BigDecimal;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.CoinDispenser;
import org.lsmr.selfcheckout.devices.CoinSlot;
import org.lsmr.selfcheckout.devices.CoinStorageUnit;
import org.lsmr.selfcheckout.devices.CoinTray;
import org.lsmr.selfcheckout.devices.CoinValidator;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.CoinDispenserObserver;
import org.lsmr.selfcheckout.devices.observers.CoinSlotObserver;
import org.lsmr.selfcheckout.devices.observers.CoinStorageUnitObserver;
import org.lsmr.selfcheckout.devices.observers.CoinTrayObserver;
import org.lsmr.selfcheckout.devices.observers.CoinValidatorObserver;

import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import user.Customer;

/**
 * @author: Mohammed Allam
 * @author: Michelle Cheung
 *
 *          This class handles Coin related hardware,
 *          and the communication between the this.customer and the
 *          self-checkout
 *          station.
 *
 */
public class CoinHandler extends Handler
		implements CoinDispenserObserver, CoinSlotObserver, CoinStorageUnitObserver, CoinTrayObserver,
		CoinValidatorObserver {

	private final SelfCheckoutSoftware scss;
	private final SelfCheckoutStation scs;
	private Customer customer;

	private boolean coinDetected = false;
	private BigDecimal coinValue;

	public CoinHandler(SelfCheckoutSoftware scss) {
		this.scss = scss;
		this.scs = this.scss.getSelfCheckoutStation();

		this.attachAll();
		this.enableHardware();
	}

	// Set this.customer
	public void setCustomer(Customer customer) {
		this.customer = customer;
		this.coinDetected = false;
		this.coinValue = BigDecimal.ZERO;
	}

	// Get this.customer
	public Customer getCustomer() {
		return this.customer;
	}

	// Attach all the hardware
	public void attachAll() {
		this.scs.coinTray.attach(this);
		this.scs.coinSlot.attach(this);
		this.scs.coinValidator.attach(this);
		this.scs.coinStorage.attach(this);
		this.scs.coinDispensers.forEach((k, v) -> v.attach(this));
	}

	/**
	 * Used to reboot/shutdown the software. Detatches the handler so that
	 * we can stop listening or assign a new handler.
	 */
	public void detatchAll() {
		this.scs.coinTray.detach(this);
		this.scs.coinSlot.detach(this);
		this.scs.coinValidator.detach(this);
		this.scs.coinStorage.detach(this);
		this.scs.coinDispensers.forEach((k, v) -> v.detach(this));
	}

	/**
	 * Used to enable all the associated hardware in a single function.
	 */
	public void enableHardware() {
		this.scs.coinSlot.enable();
		this.scs.coinTray.enable();
		this.scs.coinStorage.enable();
		this.scs.coinValidator.enable();
		this.scs.coinDispensers.forEach((k, v) -> v.enable());
	}

	/**
	 * Used to disable all the associated hardware in a single function.
	 */
	public void disableHardware() {
		this.scs.coinSlot.disable();
		this.scs.coinTray.disable();
		this.scs.coinStorage.disable();
		this.scs.coinValidator.disable();
		this.scs.coinDispensers.forEach((k, v) -> v.disable());
	}

	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
	}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
	}

	// when a coin is inserted, we set coin detected flag to True
	@Override
	public void coinInserted(CoinSlot slot) {
		this.coinDetected = true;
	}

	public boolean getCoinDetected() {
		return this.coinDetected;
	}

	@Override
	public void coinAdded(CoinTray tray) {
	}

	// when an inserted coin is valid, set coin-detected-is-valid flag to True
	@Override
	public void validCoinDetected(CoinValidator validator, BigDecimal value) {
		this.coinDetected = true;
		this.coinValue = value;
	}

	public BigDecimal getCoinValue() {
		return this.coinValue;
	}

	// when inserted coin is invalid, we notify this.customer that the coin is
	// invalid
	@Override
	public void invalidCoinDetected(CoinValidator validator) {
		this.coinDetected = false;
		this.coinValue = BigDecimal.ZERO;
		this.scss.notifyObservers(observer -> observer.invalidCoinDetected());
	}

	@Override
	public void coinsLoaded(CoinStorageUnit unit) {
	}

	@Override
	public void coinsUnloaded(CoinStorageUnit unit) {
		this.scs.coinSlot.enable();
	}

	// disables the coin slot when coin storage is full
	@Override
	public void coinsFull(CoinStorageUnit unit) {
		this.scs.coinSlot.disable();

		// Notify attendant that the coin storage is full
		SupervisionSoftware svs = scss.getSupervisionSoftware();
		svs.notifyObservers(observer -> observer.coinStorageFull(scss));

		this.scss.notifyObservers(observer -> observer.coinStorageFull());
	}
	
	private void coinAddedLogic() {
		if (this.customer != null && coinDetected == true) {
			this.customer.addCashBalance(coinValue);

			// Notify observer so GUI can update current cash balance on display
			this.scss.notifyObservers(observer -> observer.coinAdded());
		}

		this.coinDetected = false;
		this.coinValue = BigDecimal.ZERO;
		
	}

	// if coin dispenser is full & coin is valid;
	// this method adds value of coin to the this.customers accumulated currency
	@Override
	public void coinAdded(CoinStorageUnit unit) {
		coinAddedLogic();
	}

	@Override
	public void coinsFull(CoinDispenser dispenser) {
	}

	@Override
	public void coinsEmpty(CoinDispenser dispenser) {
		this.scss.notifyObservers(observer -> observer.coinDispenserEmpty());
		this.scss.getSupervisionSoftware().notifyObservers(observer -> observer.coinDispenserEmpty(this.scss));
	}

	/**
	 * We don't care about the following events:
	 * - coinAdded
	 * - coinRemoved
	 * - coinsLoaded
	 * - coinsUnloaded
	 * 
	 * <p>
	 * <b>NOTICE: </b>
	 * {@code coinAdded} event is for: when a coin is being, likely attendant, added
	 * to the coin dispenser. This event is not for customer inserting coins.
	 * </p>
	 */
	@Override
	public void coinAdded(CoinDispenser dispenser, Coin coin) {
		coinAddedLogic();
	}

	@Override
	public void coinRemoved(CoinDispenser dispenser, Coin coin) {
		// currently we don't do anything when a coin is removed from the coin dispenser
	}

	@Override
	public void coinsLoaded(CoinDispenser dispenser, Coin... coins) {
		// currently we don't do anything when a coin is loaded in the coin dispenser
	}

	@Override
	public void coinsUnloaded(CoinDispenser dispenser, Coin... coins) {
		// currently we don't do anything when a coin is unloaded from the coin
		// dispenser
	}
}
