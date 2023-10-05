package software;

import checkout.Checkout;
import checkout.Receipt;
import checkout.Screen;
import interrupt.BanknoteHandler;
import interrupt.CardHandler;
import interrupt.CoinHandler;
import interrupt.ProcessItemHandler;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import software.observers.SelfCheckoutObserver;
import user.Attendant;
import user.Customer;
import user.User;

/**
 * A software for a self-checkout station.
 * <p>
 * The creation of this object can be seen as a self-checkout station software
 * system is launched. The constructor initializes / binds the hardware
 * handlers, the hardware devices have observers.
 *
 * @author Yunfan Yang
 */
public class SelfCheckoutSoftware extends Software <SelfCheckoutObserver>
{
    // See: https://github.com/ScorpiosCrux/SENG-300-Iteration3/issues/31
    public static enum Phase
    {
        IDLE, SCANNING_ITEM, CHOOSING_PAYMENT_METHOD, PROCESSING_PAYMENT, PAYMENT_COMPLETE,

        WEIGHING_PLU_ITEM, BAGGING_ITEM, NON_BAGGABLE_ITEM, PLACING_OWN_BAG,

        HAVING_WEIGHT_DISCREPANCY, BLOCKING, ERROR
    }

    ;

    public static enum PaymentMethod
    {
        CASH, BANK_CARD, GIFT_CARD,
    }

    ;

    private Phase phase;
    private boolean isBlocked;
    private boolean isWeightDiscrepancy;
    private boolean isError;
    private boolean isShutdown;
    
    private boolean coinInTray = false;
    private boolean banknoteDangling = false;

    private final SelfCheckoutStation scs;
    private SupervisionSoftware svs;
    private Customer customer;
    private Attendant attendant;

    private BanknoteHandler banknoteHandler;
    private CardHandler cardHandler;
    private CoinHandler coinHandler;
    private ProcessItemHandler processItemHandler;

    private Checkout checkout; // Controller for processing checkout
    private Receipt receipt; // Controller for printing receipt
    private Screen screen; // Controller for displaying messages

    public SelfCheckoutSoftware(SelfCheckoutStation scs)
    {
        this.scs = scs;
        this.phase = Phase.IDLE;

        this.startSystem();
        this.disableHardware(); // Default by disable all of them
    }

    public void setUser(User user)
    {
        if (user instanceof Customer)
        {
            setCustomer((Customer) user);
        } else if (user instanceof Attendant)
        {
            setAttendant((Attendant) user);
        }
    }

    private void setCustomer(Customer customer)
    {
        this.customer = customer;

        this.banknoteHandler.setCustomer(customer);
        this.cardHandler.setCustomer(customer);
        this.coinHandler.setCustomer(customer);
        this.processItemHandler.setCustomer(customer);

        this.checkout.setCustomer(customer);
        this.receipt.setCustomer(customer);
        this.screen.setCustomer(customer);
    }

    private void setAttendant(Attendant attendant)
    {
        this.attendant = attendant;

        // attendant must be accompanied by customer to process items
        // but an attedant alone can service the station
        // TODO: consider if components need to be altered do to the presence of an
        // attendant

    }

    public Attendant getAttendant()
    {
        return this.attendant;
    }

    public void removeUser(User user)
    {
        if (user instanceof Customer)
        {
            customer = null;
            idle();
        } else if (user instanceof Attendant)
        {
            attendant = null;
            idle();
        }
    }

    public SelfCheckoutStation getSelfCheckoutStation()
    {
        return this.scs;
    }

    public Customer getCustomer()
    {
        return this.customer;
    }

    public int getPaperUsed() {
        return this.receipt.getPaperUsed();
    }

    public void resetPaperUsed() {
        this.receipt.resetPaperUsed();
    }

    public int getInkUsed() {
        return this.receipt.getInkUsed();
    }

    public void resetInkUsed() {
        this.receipt.resetInkUsed();
    }

    /**
     * This method should not be used.
     * If want to set supersivion software for this self-checkout software,
     * please use {@link SupervisionSoftware#add(SelfCheckoutSoftware)}.
     *
     * @param svs
     *
     * @author Yunfan Yang
     */
    protected void setSupervisionSoftware(SupervisionSoftware svs)
    {
        this.svs = svs;
    }

    public SupervisionSoftware getSupervisionSoftware()
    {
        return this.svs;
    }

    public void enableHardware()
    {
        this.banknoteHandler.enableHardware();
        this.cardHandler.enableHardware();
        this.coinHandler.enableHardware();
        this.processItemHandler.enableHardware();
    }

    public void disableHardware()
    {
        this.banknoteHandler.disableHardware();
        this.cardHandler.disableHardware();
        this.coinHandler.disableHardware();
        this.processItemHandler.disableHardware();
    }

    /**
     * This method is used for starting or restarting a system.
     * We do not want to mess with the SelfCheckoutStation because we do not create
     * new hardware
     * when something is turned on/off.
     */
    public void startSystem()
    {
        this.banknoteHandler = new BanknoteHandler(this);
        this.cardHandler = new CardHandler(this);
        this.coinHandler = new CoinHandler(this);
        this.processItemHandler = new ProcessItemHandler(this);
        this.checkout = new Checkout(this);
        this.receipt = new Receipt(this);
        this.screen = new Screen(this);

        this.enableHardware();
        this.screen.enableHardware();

        this.notifyObservers(observer -> observer.softwareStarted(this));
        this.isShutdown = false;
    }

    /**
     * Turns off the system by setting everything to null, the Handlers are
     * technically turned off.
     * We do not want to mess with the SelfCheckoutStation because we do not create
     * new hardware
     * when something is turned off.
     */
    public void stopSystem()
    {
        this.disableHardware();
        this.screen.disableHardware();

        this.banknoteHandler.detatchAll();
        this.banknoteHandler = null;

        this.cardHandler.detatchAll();
        this.cardHandler = null;

        this.coinHandler.detatchAll();
        this.coinHandler = null;

        this.processItemHandler.detatchAll();
        this.processItemHandler = null;

        this.receipt.detatchAll();
        this.receipt = null;

        this.checkout = null;
        this.screen = null;

        this.notifyObservers(observer -> observer.softwareStopped(this));
        
        this.setPhase(Phase.IDLE);
        this.isShutdown = true;
    }

    public void blockSystem()
    {
        // If blocking:
        // 1. disbale all hardware devices
        // 2. set isBlocked to true
        // 3. notify all observers that current phase is BLOCKING
        // 4. notify GUI that touch screen is blocked

        this.disableHardware();
        this.processItemHandler.enableBaggingArea(); // Bagging area should be enabled basically all the time
        this.isBlocked = true;
        this.notifyObservers(observer -> observer.phaseChanged(Phase.BLOCKING));
        this.notifyObservers(observer -> observer.touchScreenBlocked());
    }

    public void unblockSystem() {
        // If unblocking:
        // 1. enable all hardware devices
        // 2. set isBlocked to false
        // 3. notify all observers that current phase is the original phase
        // 4. notify GUI that touch screen is unblocked

        this.enableHardware();
        this.isBlocked = false;
        this.notifyObservers(observer -> observer.phaseChanged(this.phase));
        this.notifyObservers(observer -> observer.touchScreenUnblocked());
    }

    public void makeChange()
    {
        this.checkout.makeChange();
    }

    // ========== PHASE MANAGEMENT ========== //

    /**
     * @return
     */
    public Phase getPhase()
    {
        if (this.isError)
        {
            return Phase.ERROR;
        } else if (this.isBlocked)
        {
            return Phase.BLOCKING;
        } else if (this.isWeightDiscrepancy)
        {
            return Phase.HAVING_WEIGHT_DISCREPANCY;
        }

        return this.phase;
    }

    private void setPhase(Phase phase)
    {
        this.phase = phase;
        this.notifyObservers(observer -> observer.phaseChanged(this.phase));
        System.out.println("Set phase: " + this.phase);
    }
    
    public boolean isShutdown() {
    	return this.isShutdown;
    }

    /**
     * When the checkout station has no customer using (for purchasing specifically)
     */
    public void idle()
    {
        this.setCustomer(null);
        this.setPhase(Phase.IDLE);
    }

    /**
     * When a customer approaches the station and pressed start button
     *
     * @param customer
     */
    public void start(Customer customer)
    {
        if (this.phase != Phase.IDLE)
        {
            throw new IllegalStateException("Cannot start a new customer when the system is not idle");
        }

        this.cardHandler.enableHardware();
        this.setCustomer(customer);
        this.addItem(); // Directly jump to addItem phase
    }

    public void addItem()
    {
        this.disableHardware();
        this.cardHandler.enableHardware();
        this.processItemHandler.enableHardware();

        this.setPhase(Phase.SCANNING_ITEM);
    }

    public void addPLUItem() {
        this.disableHardware();
        this.cardHandler.enableHardware();
        this.processItemHandler.enableHardware();

        this.setPhase(Phase.WEIGHING_PLU_ITEM);
    }

    /**
     * When customer added a product to their cart, and now they need to bag the
     * item.
     * <p>
     * 1. For barcoded item, this method is called whenever an item is scanned. GUI
     * won't need to call this method.
     * 2. For PLU coded item, GUI will need to call this method after they selected
     * the product.
     */
    public void bagItem()
    {
        if (this.phase != Phase.SCANNING_ITEM && this.phase != Phase.WEIGHING_PLU_ITEM)
        {
            throw new IllegalStateException("Cannot add item when the system is not scanning item");
        }

        this.disableHardware();
        this.cardHandler.enableHardware();
        this.processItemHandler.enableBaggingArea();

        this.setPhase(Phase.BAGGING_ITEM); // Expecting GUI switchs to bagging item view
    }

    /**
     * Customer wishes to use their own bag
     * <p>
     * When they have placed their own bag in the bagging area, the phase will be
     * set back
     */
    public void addOwnBag()
    {
        if (this.phase != Phase.SCANNING_ITEM || this.customer == null)
        {
            throw new IllegalStateException("Cannot add own bag when the system is not scanning item");
        }

        // Only enable bagging area
        this.disableHardware();
        this.cardHandler.enableHardware();
        this.processItemHandler.enableBaggingArea();

        this.setPhase(Phase.PLACING_OWN_BAG);
    }

    public void notBaggingItem()
    {
        if (this.phase != Phase.BAGGING_ITEM)
        {
            throw new IllegalStateException("Need to be in the process of bagging an item to choose not to bag and item");
        }

        this.setPhase(Phase.NON_BAGGABLE_ITEM);
        SupervisionSoftware svs = this.getSupervisionSoftware();
        svs.notifyObservers(observer -> observer.customerDoesNotWantToBagItem(this));
    }

    /**
     * When customer wishes to checkout
     */
    public void checkout()
    {
        if (this.phase != Phase.SCANNING_ITEM || this.customer == null)
        {
            throw new IllegalStateException("Cannot checkout when the system is not scanning item");
        }

        // keep hardware enabled so they can go back to adding products
        this.enableHardware();
        this.cardHandler.enableHardware();
        this.setPhase(Phase.CHOOSING_PAYMENT_METHOD);
    }

    /**
     * When customer has choosen their payment method and they are ready to pay
     *
     * @param method
     */
    public void selectedPaymentMethod(PaymentMethod method)
    {
        if (this.phase != Phase.CHOOSING_PAYMENT_METHOD)
        {
            throw new IllegalStateException("Cannot checkout when the system is not choosing payment method");
        }

        this.setPhase(Phase.PROCESSING_PAYMENT);

        // Relative devices are enabled in checkout
        this.disableHardware();
        this.checkout.enablePaymentHardware(method);
    }

    public void paymentCompleted()
    {
        if (this.phase != Phase.PROCESSING_PAYMENT)
        {
            throw new IllegalStateException("Cannot have a completed payment without a processed payment");
        }
        this.disableHardware();
        this.processItemHandler.enableBaggingArea();
        this.receipt.printReceipt();
        this.setPhase(Phase.PAYMENT_COMPLETE);
    }

    public void checkoutComplete()
    {
        if (this.phase != Phase.PAYMENT_COMPLETE)
        {
            throw new IllegalStateException("Cannot have a completed checkout without a completeted payment");
        }

        this.processItemHandler.resetScale();
        this.disableHardware();
        this.idle();
    }

    /**
     * When customer wishes to go back and add more items
     */
    public void cancelCheckout()
    {
        // When the phase is not choosing payment method or processing their payment,
        // invalid operation
        if (this.phase != Phase.PROCESSING_PAYMENT && this.phase != Phase.CHOOSING_PAYMENT_METHOD)
        {
            throw new IllegalStateException("Cannot cancel checkout when the system is not processing payment");
        }

        // Relative devices are disabled in checkout
        this.disableHardware();

        this.setPhase(Phase.SCANNING_ITEM);
    }

    public void weightDiscrepancy()
    {
        this.disableHardware();
        this.processItemHandler.enableBaggingArea();

        this.isWeightDiscrepancy = true;
        this.notifyObservers(observer -> observer.phaseChanged(Phase.HAVING_WEIGHT_DISCREPANCY));
        this.notifyObservers(observer -> observer.touchScreenBlocked());
    }

    protected void approveWeightDiscrepancy()
    {
        if (!this.isWeightDiscrepancy)
        {
            throw new IllegalStateException("Cannot approve weight discrepancy when the system is not waiting for approval");
        }

        this.processItemHandler.overrideWeight();
        this.processItemHandler.enableHardware();

        this.isWeightDiscrepancy = false;
        this.notifyObservers(observer -> observer.phaseChanged(this.phase));
        this.notifyObservers(observer -> observer.touchScreenUnblocked());
    }

    public boolean hasPendingChanges() {
        return this.checkout.hasPendingChange();
    }

    public void errorOccur()
    {
        this.disableHardware();
        this.processItemHandler.enableBaggingArea();
        this.isError = true;

        this.notifyObservers(observer -> observer.phaseChanged(Phase.ERROR));
        this.notifyObservers(observer -> observer.touchScreenBlocked());
    }

    public void setCoinInTray(boolean coinInTray) {
        this.coinInTray = coinInTray;
    }

    public boolean getCoinInTray() {
        return this.coinInTray;
    }

    public void setBanknoteDangling(boolean banknoteDangling) {
        this.banknoteDangling = banknoteDangling;
    }

    public boolean getBanknoteDangling() {
        return this.banknoteDangling;
    }

    protected void resolveError() {
        if (!this.isError) {
            throw new IllegalStateException("Cannot resolve error when the system is not in error");
        }

        this.isError = false;
        this.notifyObservers(observer -> observer.phaseChanged(this.phase));
    }

}
