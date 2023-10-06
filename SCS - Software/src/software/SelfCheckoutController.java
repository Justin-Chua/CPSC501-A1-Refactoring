package software;

import checkout.Checkout;
import checkout.Receipt;
import checkout.Screen;
import user.Customer;

public class SelfCheckoutController {
	
    private Checkout checkout; // Controller for processing checkout
    private Receipt receipt; // Controller for printing receipt
    private Screen screen; // Controller for displaying messages
    
    public SelfCheckoutController(SelfCheckoutSoftware scSoftware) {
        this.checkout = new Checkout(scSoftware);
        this.receipt = new Receipt(scSoftware);
        this.screen = new Screen(scSoftware);
    }
    
    public void setAllCustomers(Customer customer) {
        this.checkout.setCustomer(customer);
        this.receipt.setCustomer(customer);
        this.screen.setCustomer(customer);
    }
    
    public Checkout getCheckout() {
    	return this.checkout;
    }
    
    public Receipt getReceipt() {
    	return this.receipt;
    }
    
    public Screen getScreen() {
    	return this.screen;
    }
    
    public void resetControllers() {
        this.receipt.detatchAll();
        this.receipt = null;

        this.checkout = null;
        this.screen = null;
    }
}
