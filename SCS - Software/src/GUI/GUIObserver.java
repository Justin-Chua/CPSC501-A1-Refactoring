package GUI;

import software.SelfCheckoutSoftware;
import software.SelfCheckoutSoftware.Phase;
import software.observers.SelfCheckoutObserver;
import software.observers.SupervisionObserver;

public class GUIObserver implements SelfCheckoutObserver, SupervisionObserver {

    @Override
    public Void banknoteStorageFull(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Banknote Storage Full");
        return null;
    }

    @Override
    public Void banknoteDispenserEmpty(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Banknote Dispenser Empty");
        return null;
    }

    @Override
    public Void coinStorageFull(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Coin Storage Full");
        return null;
    }

    @Override
    public Void coinDispenserEmpty(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Coin Dispenser Empty");
        return null;
    }

    @Override
    public Void dispenseChangeFailed(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Dispense Change Failed");
        return null;
    }

    @Override
    public Void receiptPrinterOutOfPaper(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Receipt Printer Out of Paper");
        return null;
    }

    @Override
    public Void receiptPrinterLowOnPaper(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Receipt printer paper cartridge at 10% of max capacity");
        return null;
    }

    @Override
    public Void receiptPrinterPaperOverloaded(SelfCheckoutSoftware scs) {
        // not needed
        return null;
    }

    @Override
    public Void receiptPrinterOutOfInk(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Receipt Printer Out of Ink");
        return null;
    }

    @Override
    public Void receiptPrinterLowOnInk(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Receipt printer ink cartridge at 10% of max capacity");
        return null;
    }

    @Override
    public Void receiptPrinterInkOverloaded(SelfCheckoutSoftware scs) {
        // not needed
        return null;
    }

    @Override
    public Void weightDiscrepancyDetected(SelfCheckoutSoftware scs) {
        Scenes.errorMsg("Weight Discrepancy Detected");
        return null;
    }

    @Override
    public Void touchScreenBlocked(SelfCheckoutSoftware scs) {
        // not needed
        return null;
    }

    @Override
    public Void touchScreenUnblocked(SelfCheckoutSoftware scs) {
        // not needed
        return null;
    }

    @Override
    public Void scaleOverloadedDetected(SelfCheckoutSoftware scs) {
        // not needed
        return null;
    }

    @Override
    public Void scaleOverloadedResolved(SelfCheckoutSoftware scs) {
        // not needed
        return null;
    }

    @Override
    public Void customerDoesNotWantToBagItem(SelfCheckoutSoftware scs) {
        // not needed
        // Scenes.errorMsg("Customer Does Not Want to Bag Item");
        return null;
    }

    @Override
    public Void invalidBanknoteDetected() {
        // need other prompt method
        return null;
    }

    @Override
    public Void banknoteAdded() {
        // need other prompt method
        return null;
    }

    @Override
    public Void banknoteStorageFull() {
        Scenes.errorMsg("Banknote Storage Full");
        return null;
    }

    @Override
    public Void banknoteDispenserEmpty() {
        Scenes.errorMsg("Banknote Dispenser Empty");
        return null;
    }

    @Override
    public Void invalidCoinDetected() {
        // need other prompt method
        return null;
    }

    @Override
    public Void coinAdded() {
        // need other prompt method
        return null;
    }

    @Override
    public Void coinStorageFull() {
        Scenes.errorMsg("Coin Storage Full");
        return null;
    }

    @Override
    public Void coinDispenserEmpty() {
        Scenes.errorMsg("Coin Dispenser Empty");
        return null;
    }

    @Override
    public Void invalidCardTypeDetected() {
        Scenes.errorMsg("Invalid Card Type Detected");
        return null;
    }

    @Override
    public Void cardTransactionSucceeded() {
        // need other prompt method
        return null;
    }

    @Override
    public Void invalidGiftCardDetected() {
        Scenes.errorMsg("Invalid Gift Card Detected");
        return null;
    }

    @Override
    public Void invalidMembershipCardDetected() {
        Scenes.errorMsg("Invalid Membership Card Detected");
        return null;
    }

    @Override
    public Void membershipCardDetected(String memberID) {
        // not needed
        System.out.println("Membership id: " + memberID);
        return null;
    }

    @Override
    public Void paymentHoldingAuthorizationFailed() {
        Scenes.errorMsg("Payment Holding Authorization Failed");
        return null;
    }

    @Override
    public Void paymentPostingTransactionFailed() {
        Scenes.errorMsg("Payment Posting Transaction Failed");
        return null;
    }

    @Override
    public Void paymentCompleted() {
        // need other prompt method
        return null;
    }

    @Override
    public Void placeInBaggingAreaBlocked() {
        // not needed
        // Scenes.errorMsg("Place In Bagging Area Blocked");
        return null;
    }

    @Override
    public Void placeInBaggingAreaUnblocked() {
        // not needed
        return null;
    }

    @Override
    public Void weightDiscrepancyInBaggingAreaDetected() {
        Scenes.errorMsg("Weight Discrepancy In Bagging Area Detected");
        return null;
    }

    @Override
    public Void weightDiscrepancyInBaggingAreaResolved() {
        // not needed
        return null;
    }

    @Override
    public Void productNotFound() {
        Scenes.errorMsg("Product Not Found ");
        return null;
    }

    @Override
    public Void softwareStarted(SelfCheckoutSoftware scss) {
        // not needed
        return null;
    }

    @Override
    public Void softwareStopped(SelfCheckoutSoftware scss) {
        // not needed
        return null;
    }

    @Override
    public Void touchScreenBlocked() {
        // not needed
        // Scenes.errorMsg("Touch Screen Blocked");
        return null;
    }

    @Override
    public Void touchScreenUnblocked() {
        // not needed
        return null;
    }

    @Override
    public Void phaseChanged(Phase phase) {
        // not needed
        return null;
    }

    @Override
    public Void productCannotFound() {
        // TODO Auto-generated method stub
        return null;
    }
}
