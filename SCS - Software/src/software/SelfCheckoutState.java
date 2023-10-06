package software;

public class SelfCheckoutState {
	
    private boolean isBlocked;
    private boolean isWeightDiscrepancy;
    private boolean isError;
    private boolean isShutdown;
    private boolean coinInTray = false;
    private boolean banknoteDangling = false;
    
    
    public void setIsWeightDiscrepancy(boolean status) {
    	this.isWeightDiscrepancy = status;
    }
    
    public void setIsError(boolean status) {
    	this.isError = status;
    }
    
    public void setIsShutDown(boolean status) {
    	this.isShutdown = status;
    }
    
    public void setIsBlocked(boolean status) {
    	this.isBlocked = status;
    }
    
    public void setCoinInTray(boolean coinInTray) {
        this.coinInTray = coinInTray;
    }

    public void setBanknoteDangling(boolean banknoteDangling) {
        this.banknoteDangling = banknoteDangling;
    }
    
    public boolean getIsBlocked() {
    	return this.isBlocked;
    }
    
    public boolean getIsWeightDiscrepancy() {
    	return this.isWeightDiscrepancy;
    }
    
    public boolean getIsError() {
    	return this.isError;
    }
   
    public boolean getIsShutDown( ) {
    	return this.isShutdown;
    }
    
    public boolean getCoinInTray() {
        return this.coinInTray;
    }
    
    public boolean getBanknoteDangling() {
        return this.banknoteDangling;
    }
}
