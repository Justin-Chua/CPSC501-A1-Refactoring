package software;

import interrupt.BanknoteHandler;
import interrupt.CardHandler;
import interrupt.CoinHandler;
import interrupt.ProcessItemHandler;
import user.Customer;

public class SelfCheckoutHandler {
	
    private BanknoteHandler banknoteHandler;
    private CardHandler cardHandler;
    private CoinHandler coinHandler;
    private ProcessItemHandler processItemHandler;
    
    public SelfCheckoutHandler(SelfCheckoutSoftware scSoftware) {
        this.banknoteHandler = new BanknoteHandler(scSoftware);
        this.cardHandler = new CardHandler(scSoftware);
        this.coinHandler = new CoinHandler(scSoftware);
        this.processItemHandler = new ProcessItemHandler(scSoftware);
    }
    
    public BanknoteHandler getBanknoteHandler() {
    	return this.banknoteHandler;
    }
    
    public CardHandler getCardHandler() {
    	return this.cardHandler;
    }
    
    public CoinHandler getCoinHandler() {
    	return this.coinHandler;
    }
    
    public ProcessItemHandler getProcessItemHandler() {
    	return this.processItemHandler;
    }
    
    public void setAllCustomers(Customer customer) {
        this.banknoteHandler.setCustomer(customer);
        this.cardHandler.setCustomer(customer);
        this.coinHandler.setCustomer(customer);
        this.processItemHandler.setCustomer(customer);
    }
    
    public void enableAll() {
        this.banknoteHandler.enableHardware();
        this.cardHandler.enableHardware();
        this.coinHandler.enableHardware();
        this.processItemHandler.enableHardware();
    }
    
    public void disableAll() {
        this.banknoteHandler.disableHardware();
        this.cardHandler.disableHardware();
        this.coinHandler.disableHardware();
        this.processItemHandler.disableHardware();
    }
    
    public void resetHandlers() {
	  this.banknoteHandler.detatchAll();
      this.banknoteHandler = null;

      this.cardHandler.detatchAll();
      this.cardHandler = null;

      this.coinHandler.detatchAll();
      this.coinHandler = null;

      this.processItemHandler.detatchAll();
      this.processItemHandler = null;
    }
}
