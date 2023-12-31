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

	private final SelfCheckoutSoftware scSoftware;
	private final SelfCheckoutStation scStation;
	private Customer customer;

	private boolean coinDetected = false;
	private BigDecimal coinValue;

	public CoinHandler(SelfCheckoutSoftware scSoftware) {
		this.scSoftware = scSoftware;
		this.scStation = this.scSoftware.getSelfCheckoutStation();

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
		this.scStation.coinTray.attach(this);
		this.scStation.coinSlot.attach(this);
		this.scStation.coinValidator.attach(this);
		this.scStation.coinStorage.attach(this);
		this.scStation.coinDispensers.forEach((k, v) -> v.attach(this));
	}

	/**
	 * Used to reboot/shutdown the software. Detatches the handler so that
	 * we can stop listening or assign a new handler.
	 */
	public void detatchAll() {
		this.scStation.coinTray.detach(this);
		this.scStation.coinSlot.detach(this);
		this.scStation.coinValidator.detach(this);
		this.scStation.coinStorage.detach(this);
		this.scStation.coinDispensers.forEach((k, v) -> v.detach(this));
	}

	/**
	 * Used to enable all the associated hardware in a single function.
	 */
	public void enableHardware() {
		this.scStation.coinSlot.enable();
		this.scStation.coinTray.enable();
		this.scStation.coinStorage.enable();
		this.scStation.coinValidator.enable();
		this.scStation.coinDispensers.forEach((k, v) -> v.enable());
	}

	/**
	 * Used to disable all the associated hardware in a single function.
	 */
	public void disableHardware() {
		this.scStation.coinSlot.disable();
		this.scStation.coinTray.disable();
		this.scStation.coinStorage.disable();
		this.scStation.coinValidator.disable();
		this.scStation.coinDispensers.forEach((k, v) -> v.disable());
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
		this.scSoftware.notifyObservers(observer -> observer.invalidCoinDetected());
	}

	@Override
	public void coinsLoaded(CoinStorageUnit unit) {
	}

	@Override
	public void coinsUnloaded(CoinStorageUnit unit) {
		this.scStation.coinSlot.enable();
	}

	// disables the coin slot when coin storage is full
	@Override
	public void coinsFull(CoinStorageUnit unit) {
		this.scStation.coinSlot.disable();

		// Notify attendant that the coin storage is full
		SupervisionSoftware svs = scSoftware.getSupervisionSoftware();
		svs.notifyObservers(observer -> observer.coinStorageFull(scSoftware));

		this.scSoftware.notifyObservers(observer -> observer.coinStorageFull());
	}
	
	private void coinAddedLogic() {
		if (this.customer != null && coinDetected == true) {
			this.customer.addCashBalance(coinValue);

			// Notify observer so GUI can update current cash balance on display
			this.scSoftware.notifyObservers(observer -> observer.coinAdded());
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
		this.scSoftware.notifyObservers(observer -> observer.coinDispenserEmpty());
		this.scSoftware.getSupervisionSoftware().notifyObservers(observer -> observer.coinDispenserEmpty(this.scSoftware));
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
