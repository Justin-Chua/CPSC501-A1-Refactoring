package software;

import checkout.Checkout;
import checkout.Receipt;
import checkout.Screen;
import interrupt.BanknoteHandler;
import interrupt.CardHandler;
import interrupt.CoinHandler;
import interrupt.ProcessItemHandler;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import software.SelfCheckoutSoftware.Phase;
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

    private final SelfCheckoutStation scStation;
    private SupervisionSoftware svs;
    private SelfCheckoutController scController;
    private SelfCheckoutHandler scHandler;
    private SelfCheckoutState scState;
    
    private Customer customer;
    private Attendant attendant;
    
    private Phase phase;


    public SelfCheckoutSoftware(SelfCheckoutStation scStation)
    {
        this.scStation = scStation;
        this.phase = Phase.IDLE;

        this.startSystem();
        this.scHandler.disableAll(); // Default by disable all of them
    }
    
    public SelfCheckoutStation getSelfCheckoutStation()
    {
        return this.scStation;
    }
    
    public SelfCheckoutController getSelfCheckoutController() {
    	return this.scController;
    }
    
    public SelfCheckoutHandler getSelfCheckoutHandler() {
    	return this.scHandler;
    }
    
    public SelfCheckoutState getSelfCheckoutState() {
    	return this.scState;
    }

    public Customer getCustomer()
    {
        return this.customer;
    }
    
    public Attendant getAttendant()
    {
        return this.attendant;
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
        
        this.scHandler.setAllCustomers(customer);

        this.scController.setAllCustomers(customer);
    }

    private void setAttendant(Attendant attendant)
    {
        this.attendant = attendant;

        // attendant must be accompanied by customer to process items
        // but an attedant alone can service the station
        // TODO: consider if components need to be altered do to the presence of an
        // attendant

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

    /**
     * This method is used for starting or restarting a system.
     * We do not want to mess with the SelfCheckoutStation because we do not create
     * new hardware
     * when something is turned on/off.
     */
    public void startSystem()
    {
    	this.scHandler = new SelfCheckoutHandler(this);

    	this.scController = new SelfCheckoutController(this);
    	
    	this.scState = new SelfCheckoutState();
    	
        this.scHandler.enableAll();
        this.scController.getScreen().enableHardware();

        this.notifyObservers(observer -> observer.softwareStarted(this));
        this.scState.setIsShutDown(false);
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
        this.scHandler.disableAll();
        this.scController.getScreen().disableHardware();

        this.scHandler.resetHandlers();

        this.scController.resetControllers();

        this.notifyObservers(observer -> observer.softwareStopped(this));
        
        this.setPhase(Phase.IDLE);
        this.scState.setIsShutDown(true);
    }

    public void blockSystem()
    {
        // If blocking:
        // 1. disbale all hardware devices
        // 2. set isBlocked to true
        // 3. notify all observers that current phase is BLOCKING
        // 4. notify GUI that touch screen is blocked

        this.scHandler.disableAll();
        this.scHandler.getProcessItemHandler().enableBaggingArea(); // Bagging area should be enabled basically all the time
        this.scState.setIsBlocked(true);
        this.notifyObservers(observer -> observer.phaseChanged(Phase.BLOCKING));
        this.notifyObservers(observer -> observer.touchScreenBlocked());
    }

    public void unblockSystem() {
        // If unblocking:
        // 1. enable all hardware devices
        // 2. set isBlocked to false
        // 3. notify all observers that current phase is the original phase
        // 4. notify GUI that touch screen is unblocked

        this.scHandler.enableAll();
        this.scState.setIsBlocked(false);
        this.notifyObservers(observer -> observer.phaseChanged(this.phase));
        this.notifyObservers(observer -> observer.touchScreenUnblocked());
    }

    public void makeChange()
    {
        this.scController.getCheckout().makeChange();
    }

    // ========== PHASE MANAGEMENT ========== //

    /**
     * @return
     */
    public Phase getPhase()
    {
        if (this.scState.getIsError())
        {
            return Phase.ERROR;
        } else if (this.scState.getIsBlocked())
        {
            return Phase.BLOCKING;
        } else if (this.scState.getIsWeightDiscrepancy())
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

        this.scHandler.getCardHandler().enableHardware();
        this.setCustomer(customer);
        this.addItem(); // Directly jump to addItem phase
    }

    public void addItem()
    {
        this.scHandler.disableAll();
        this.scHandler.getCardHandler().enableHardware();
        this.scHandler.getProcessItemHandler().enableHardware();

        this.setPhase(Phase.SCANNING_ITEM);
    }

    public void addPLUItem() {
        this.scHandler.disableAll();
        this.scHandler.getCardHandler().enableHardware();
        this.scHandler.getProcessItemHandler().enableHardware();

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

        this.scHandler.disableAll();
        this.scHandler.getCardHandler().enableHardware();
        this.scHandler.getProcessItemHandler().enableBaggingArea();

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
        this.scHandler.disableAll();
        this.scHandler.getCardHandler().enableHardware();
        this.scHandler.getProcessItemHandler().enableBaggingArea();

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
        this.scHandler.enableAll();
        this.scHandler.getCardHandler().enableHardware();
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
        this.scHandler.disableAll();
        this.scController.getCheckout().enablePaymentHardware(method);
    }

    public void paymentCompleted()
    {
        if (this.phase != Phase.PROCESSING_PAYMENT)
        {
            throw new IllegalStateException("Cannot have a completed payment without a processed payment");
        }
        this.scHandler.disableAll();
        this.scHandler.getProcessItemHandler().enableBaggingArea();
        this.scController.getReceipt().printReceipt();
        this.setPhase(Phase.PAYMENT_COMPLETE);
    }

    public void checkoutComplete()
    {
        if (this.phase != Phase.PAYMENT_COMPLETE)
        {
            throw new IllegalStateException("Cannot have a completed checkout without a completeted payment");
        }

        this.scHandler.getProcessItemHandler().resetScale();
        this.scHandler.disableAll();
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
        this.scHandler.disableAll();

        this.setPhase(Phase.SCANNING_ITEM);
    }

    public void weightDiscrepancy()
    {
        this.scHandler.disableAll();
        this.scHandler.getProcessItemHandler().enableBaggingArea();

        this.scState.setIsWeightDiscrepancy(true);
        this.notifyObservers(observer -> observer.phaseChanged(Phase.HAVING_WEIGHT_DISCREPANCY));
        this.notifyObservers(observer -> observer.touchScreenBlocked());
    }

    protected void approveWeightDiscrepancy()
    {
        if (!this.scState.getIsWeightDiscrepancy())
        {
            throw new IllegalStateException("Cannot approve weight discrepancy when the system is not waiting for approval");
        }

        this.scHandler.getProcessItemHandler().overrideWeight();
        this.scHandler.getProcessItemHandler().enableHardware();

        this.scState.setIsWeightDiscrepancy(false);
        this.notifyObservers(observer -> observer.phaseChanged(this.phase));
        this.notifyObservers(observer -> observer.touchScreenUnblocked());
    }

    public void errorOccur()
    {
        this.scHandler.disableAll();
        this.scHandler.getProcessItemHandler().enableBaggingArea();
        this.scState.setIsError(true);

        this.notifyObservers(observer -> observer.phaseChanged(Phase.ERROR));
        this.notifyObservers(observer -> observer.touchScreenBlocked());
    }

    protected void resolveError() {
        if (!this.scState.getIsError()) {
            throw new IllegalStateException("Cannot resolve error when the system is not in error");
        }

        this.scState.setIsError(false);
        this.notifyObservers(observer -> observer.phaseChanged(this.phase));
    }

}
