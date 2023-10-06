package software.observers;

import software.SelfCheckoutSoftware;

public interface SupervisionObserver extends Observer {
    // Banknore Handler
    public Void banknoteStorageFull(SelfCheckoutSoftware scSoftware);

    public Void banknoteDispenserEmpty(SelfCheckoutSoftware scSoftware);

    // Coin Handler
    public Void coinStorageFull(SelfCheckoutSoftware scSoftware);

    public Void coinDispenserEmpty(SelfCheckoutSoftware scSoftware);

    // Checkout Handler
    public Void dispenseChangeFailed(SelfCheckoutSoftware scSoftware);//

    // Receipt Handler
    public Void receiptPrinterOutOfPaper(SelfCheckoutSoftware scSoftware);//
    
    public Void receiptPrinterLowOnPaper(SelfCheckoutSoftware scSoftware);//
    
    public Void receiptPrinterPaperOverloaded(SelfCheckoutSoftware scSoftware);

    public Void receiptPrinterOutOfInk(SelfCheckoutSoftware scSoftware);//
    
    public Void receiptPrinterLowOnInk(SelfCheckoutSoftware scSoftware);//
    
    public Void receiptPrinterInkOverloaded(SelfCheckoutSoftware scSoftware);

    //Process Item Handler
    public Void weightDiscrepancyDetected(SelfCheckoutSoftware scSoftware);//

    public Void touchScreenBlocked(SelfCheckoutSoftware scSoftware);

    public Void touchScreenUnblocked(SelfCheckoutSoftware scSoftware);

    public Void scaleOverloadedDetected(SelfCheckoutSoftware scSoftware);

    public Void scaleOverloadedResolved(SelfCheckoutSoftware scSoftware);

    public Void customerDoesNotWantToBagItem(SelfCheckoutSoftware scSoftware);//
}
