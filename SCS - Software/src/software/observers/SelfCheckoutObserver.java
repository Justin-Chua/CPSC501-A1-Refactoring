package software.observers;

import software.SelfCheckoutSoftware;
import software.SelfCheckoutSoftware.Phase;

/**
 * The Observer class for SelfCheckoutSoftware.
 * 
 * PLEASE NOTICE: When you define the return type, instead of {@code Void},
 * please use its wrapper class {@code Void} object, the one with capital V.
 * 
 * Please notice the naming pattern. It should be noun + verb past tense.
 * 
 * @author Yunfan Yang
 */
public interface SelfCheckoutObserver extends Observer {
    // Banknote Handler
    public Void invalidBanknoteDetected();//

    public Void banknoteAdded();//

    public Void banknoteStorageFull();//-super

    public Void banknoteDispenserEmpty();//-super

    // Coin Handler
    public Void invalidCoinDetected();//

    public Void coinAdded();//

    public Void coinStorageFull();//-super

    public Void coinDispenserEmpty();//-super

    // Card Handler
    public Void invalidCardTypeDetected();//

    public Void cardTransactionSucceeded();//

    public Void invalidGiftCardDetected();//

    public Void invalidMembershipCardDetected();//

    public Void membershipCardDetected(String memberID);

    public Void paymentHoldingAuthorizationFailed();//

    public Void paymentPostingTransactionFailed();//

    public Void paymentCompleted();//

    // Process Item Handler
    public Void placeInBaggingAreaBlocked();//

    public Void placeInBaggingAreaUnblocked();

    public Void weightDiscrepancyInBaggingAreaDetected();//

    public Void weightDiscrepancyInBaggingAreaResolved();

    public Void productNotFound();//

    // Self-Checkout Software

    public Void softwareStarted(SelfCheckoutSoftware scss);

    public Void softwareStopped(SelfCheckoutSoftware scss);

    public Void touchScreenBlocked();//

    public Void touchScreenUnblocked();

    // Phase
    public Void phaseChanged(Phase phase);

	public Void productCannotFound();

    
}
