package software.observers;

import software.SelfCheckoutSoftware;

public interface SupervisionObserver extends Observer {
    // Banknore Handler
    public Void banknoteStorageFull(SelfCheckoutSoftware scs);

    public Void banknoteDispenserEmpty(SelfCheckoutSoftware scs);

    // Coin Handler
    public Void coinStorageFull(SelfCheckoutSoftware scs);

    public Void coinDispenserEmpty(SelfCheckoutSoftware scs);

    // Checkout Handler
    public Void dispenseChangeFailed(SelfCheckoutSoftware scs);//

    // Receipt Handler
    public Void receiptPrinterOutOfPaper(SelfCheckoutSoftware scs);//
    
    public Void receiptPrinterLowOnPaper(SelfCheckoutSoftware scs);//
    
    public Void receiptPrinterPaperOverloaded(SelfCheckoutSoftware scs);

    public Void receiptPrinterOutOfInk(SelfCheckoutSoftware scs);//
    
    public Void receiptPrinterLowOnInk(SelfCheckoutSoftware scs);//
    
    public Void receiptPrinterInkOverloaded(SelfCheckoutSoftware scs);

    //Process Item Handler
    public Void weightDiscrepancyDetected(SelfCheckoutSoftware scs);//

    public Void touchScreenBlocked(SelfCheckoutSoftware scs);

    public Void touchScreenUnblocked(SelfCheckoutSoftware scs);

    public Void scaleOverloadedDetected(SelfCheckoutSoftware scs);

    public Void scaleOverloadedResolved(SelfCheckoutSoftware scs);

    public Void customerDoesNotWantToBagItem(SelfCheckoutSoftware scs);//
}
