package interrupt;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BarcodeScannerObserver;
import org.lsmr.selfcheckout.devices.observers.ElectronicScaleObserver;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

import software.SelfCheckoutSoftware;
import software.SelfCheckoutSoftware.Phase;
import store.Inventory;
import user.Customer;

/**
 * Currenty handles any barcode scanner events and electronic scale events from
 * the bagging area.
 * Easily extendable to incorporate PLU codes in future iterations.
 * 
 * @author joshuaplosz
 * @author Michelle Cheung
 * @author Yunfan Yang
 *
 */
public class ProcessItemHandler extends Handler implements BarcodeScannerObserver, ElectronicScaleObserver {

	private static double DISCREPANCY = 1.0; // Scales have margins of errors, this is how much we allow

	private final SelfCheckoutStation scs;
	private final SelfCheckoutSoftware scss;
	private Customer customer;

	private double currentWeight = 0.0;
	private double expectedWeight = 0.0;
	private boolean scaleOverloaded;

	public ProcessItemHandler(SelfCheckoutSoftware scss) {
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
		resetScale();
		this.scaleOverloaded = false;
	}

	public void attachAll() {
		// Attach both scanners
		this.scs.mainScanner.attach(this);
		this.scs.handheldScanner.attach(this);
		this.scs.scanningArea.attach(this);
		this.scs.baggingArea.attach(this);
	}

	/**
	 * Used to reboot/shutdown the software. Detatches the handler so that
	 * we can stop listening or assign a new handler.
	 */
	public void detatchAll() {
		this.scs.mainScanner.detach(this);
		this.scs.handheldScanner.detach(this);
		this.scs.scanningArea.detach(this);
		this.scs.baggingArea.detach(this);
	}

	/**
	 * Used to enable all the associated hardware in a single function.
	 */
	public void enableHardware() {
		this.scs.mainScanner.enable();
		this.scs.handheldScanner.enable();
		this.scs.scanningArea.enable();
		this.scs.baggingArea.enable();
	}

	/**
	 * Used to disable all the associated hardware in a single function.
	 */
	public void disableHardware() {
		this.scs.mainScanner.disable();
		this.scs.handheldScanner.disable();
		this.scs.scanningArea.disable();
		this.scs.baggingArea.disable();
	}

	public void enableBaggingArea() {
		this.scs.baggingArea.enable();
	}

	@Override
	public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// do nothing when barcode scanner or electronic scale is enabled
	}

	@Override
	public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// do nothing when barcode scanner or electronic scale is disabled
	}

	/**
	 * When barcode scan event occurs check if store inventory actually contains the
	 * item represented by the barcode. If item is available for purchase disable
	 * the scanner, record the weight of the item scanned, and add the item to the
	 * customers cart. Notify the customer to add the item to the bagging area.
	 */
	@Override
	public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
		if (this.customer == null) {
			return;
		}

		Product product = Inventory.getProduct(barcode);

		if (product == null) {
			this.scss.notifyObservers(observer -> observer.productCannotFound());
			return;
		}

		this.scs.mainScanner.disable();
		this.scs.handheldScanner.disable();

		// This can only be BarcodedProduct
		if (product instanceof BarcodedProduct) {
			BarcodedProduct barcodedProduct = (BarcodedProduct) product;
			this.expectedWeight = barcodedProduct.getExpectedWeight();
		}

		// TODO: For PLU items, Incorporate this scanning area electronic scale

		this.customer.addProduct(product);
		this.scss.bagItem();
		this.scss.notifyObservers(observer -> observer.placeInBaggingAreaBlocked());
	}

	/**
	 * Override the current recorded weight with the actual weight on scale
	 */
	public void overrideWeight() {
		try {
			this.currentWeight = this.scs.baggingArea.getCurrentWeight();
			this.expectedWeight = 0.0;
		} catch (OverloadException e) {
			// Hopefully not possible
		}
	}

	/**
	 * When electronic scale weight change event occurs under normal operation
	 * compare
	 * the weight of the current item scanned and the scales changed weight. If they
	 * match then re-enable the scanner for customer to continue scanning. If the
	 * weight
	 * change is different than the current scanned item then an unexpected item was
	 * placed in the bagging area. Record the weight the scale needs to return to
	 * and set
	 * a flag for an unexpected item. The scanner can not be re-enabled until the
	 * scale
	 * weight returns to what it was at before the unexpected item was added and the
	 * expected item has been added.
	 */
	@Override
	public void weightChanged(ElectronicScale scale, double weightInGrams) {
		if (this.scaleOverloaded) {
			return;
		}

		if (scale.equals(this.scs.scanningArea)) {
			if (this.scss.getPhase() == Phase.WEIGHING_PLU_ITEM) {
				customer.addProduct(Inventory.getProduct(this.customer.getPLU()), weightInGrams);
				this.expectedWeight = weightInGrams;
				this.scss.bagItem();
				return; //we want to get the call from bagging area.
			}
			return;
		}

		if (this.scss.getPhase() == Phase.PAYMENT_COMPLETE) {
			if (weightInGrams == 0.0) {
				this.scss.checkoutComplete();
			}
			return;
		}

		// Get the weight of the bag and store it, if the customer is trying to add
		// their own bag to the bagging area
		if (this.scss.getPhase() == Phase.PLACING_OWN_BAG) {
			this.currentWeight = weightInGrams; // Record the new weight (with the bag)
			// this.scss.addItem(); // go back to add item phase !!!! not until attendant says so!
			return;
		}
		
		// If currently detecting weight discrepancy and required removal
		// The weight should be back to currentWeight
		if (this.scss.getPhase() == Phase.HAVING_WEIGHT_DISCREPANCY) {
			// When not adding new item, the weight should be back to currentWeight, which
			// is 0.0 + currentWeight.
			// If it's adding new item, the weight should be the item weight + currrent
			// weight.
			// this.expectedWeight will always be set to 0.0 when the item is added, so that
			// it guarantees the validity of discrepancy algorithm.
			double expected = this.currentWeight + this.expectedWeight;
			double discrepancy = Math.abs(expected - weightInGrams);

			// Discrepancy is resolved
			if (discrepancy <= DISCREPANCY) {
				this.acceptNewWeight(weightInGrams);
				this.scss.notifyObservers(observer -> observer.weightDiscrepancyInBaggingAreaResolved());
			}
			// Else do nothing, the discrepancy phase keeps

			return;
		}

		// If the current phase is not bagging item, then there's unexpected item
		if (this.scss.getPhase() != Phase.BAGGING_ITEM) {
			this.scss.weightDiscrepancy();
			this.scss.notifyObservers(observer -> observer.weightDiscrepancyInBaggingAreaDetected());
			this.scss.getSupervisionSoftware().notifyObservers(observer -> observer.weightDiscrepancyDetected(this.scss));
			return;
		}

		// ========= The rest is only for bagging item phase ========= //

		double expected = this.currentWeight + this.expectedWeight;
		double discrepancy = Math.abs(expected - weightInGrams);
		
		

		// If the discrepancy is too large
		if (discrepancy > DISCREPANCY) {
			this.scss.weightDiscrepancy();
			this.scss.notifyObservers(observer -> observer.weightDiscrepancyInBaggingAreaDetected());
			this.scss.getSupervisionSoftware().notifyObservers(observer -> observer.weightDiscrepancyDetected(this.scss));
			return;
		}

		// Accept new weight
		this.acceptNewWeight(weightInGrams);

	}

	private void acceptNewWeight(double weightInGrams) {
		this.currentWeight = weightInGrams;
		this.expectedWeight = 0.0;
		this.scss.addItem(); // Go back to add item phase
	}

	public void resetScale() {
		this.currentWeight = 0.0;
		this.expectedWeight = 0.0;
	}

	@Override
	public void overload(ElectronicScale scale) {
		System.out.println("Scale overloaded");
		this.scaleOverloaded = true;
		this.scss.blockSystem();
		this.scss.getSupervisionSoftware().notifyObservers(observer -> observer.scaleOverloadedDetected(this.scss));
	}

	@Override
	public void outOfOverload(ElectronicScale scale) {
		System.out.println("Scale out of overloaded");
		this.scaleOverloaded = false;
		this.scss.unblockSystem();
		this.scss.getSupervisionSoftware().notifyObservers(observer -> observer.scaleOverloadedResolved(this.scss));
	}
}