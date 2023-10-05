package tests.interrupt;

import bank.Bank;
import interrupt.CardHandler;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.*;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.external.CardIssuer;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import software.SelfCheckoutSoftware;
import store.GiftCard;
import store.Inventory;
import store.Membership;
import user.Customer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Currency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CardHandlerTest
{
    // Static variables that will be used during testing
    final Currency currency = Currency.getInstance("CAD");
    final int[] banknoteDenominations = {5, 10, 20, 50};
    final BigDecimal[] coinDenominations = {new BigDecimal("0.05"), new BigDecimal("0.10"), new BigDecimal("0.25"), new BigDecimal("1.00"), new BigDecimal("2.00")};
    final int scaleMaximumWeight = 100;
    final int scaleSensitivity = 10;
    final String customer1Name = "John Smith";
    final String customer2Name = "Samantha Smith";
    final String customer3Name = "Lucas Smith";
    final String customer4Name = "Bob Smith";
    final String customer1AccountType = "Debit";
    final String customer2AccountType = "Credit";
    final String customer3AccountType = "Gift";
    final String customer4AccountType = "Membership";
    final String card1Number = "1111";
    final String card2Number = "3333";
    final String card3Number = "5555";
    final String card4Number = "7777";
    final String card1CVV = "222";
    final String card2CVV = "444";
    final String card3CVV = "666";
    final String card4CVV = "888";
    final String card1Pin = "1234";
    final String card2Pin = "5678";
    final String card3Pin = "2468";
    final String card4Pin = "3690";
    final boolean card1TapEnabled = true;
    final boolean card2TapEnabled = false;
    final boolean card3TapEnabled = true;
    final boolean card4TapEnabled = true;
    final boolean card1HasChip = false;
    final boolean card2HasChip = true;
    final boolean card3HasChip = true;
    final boolean card4HasChip = true;
    final BarcodedProduct barcodedProduct = new BarcodedProduct(new Barcode(new Numeral[] {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four}), "N/A", new BigDecimal("5.00"), 17.5);

    final BigDecimal[] balanceAdjustmentAmounts = {new BigDecimal("5.00"), new BigDecimal("10.00"), new BigDecimal("20.00")};

    // Declare all relevant implementations
    SelfCheckoutStation selfCheckoutStation;
    SelfCheckoutSoftware selfCheckoutSoftware;
    CardHandler cardHandler;
    CardIssuer cardIssuer1, cardIssuer2, cardIssuer3;
    Customer customer1, customer2, customer3, customer4;
    Card card1, card2, card3, card4;

    // Setup that is run before each test case
    @Before
    public void setup() throws OverloadException
    {
        Bank.clearIssuers();
        Bank.clearCardIssuers();
        Membership.clear();
        GiftCard.clear();

        selfCheckoutStation = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        selfCheckoutSoftware = new SelfCheckoutSoftware(selfCheckoutStation);

        customer1 = new Customer();
        customer2 = new Customer();
        customer3 = new Customer();
        customer4 = new Customer();

        cardIssuer1 = new CardIssuer("Visa");
        cardIssuer2 = new CardIssuer("Mastercard");
        cardIssuer3 = new CardIssuer("Store");

        cardHandler = new CardHandler(selfCheckoutSoftware);

        card1 = new Card(customer1AccountType, card1Number, customer1Name, card1CVV, card1Pin, card1TapEnabled, card1HasChip);
        card2 = new Card(customer2AccountType, card2Number, customer2Name, card2CVV, card2Pin, card2TapEnabled, card2HasChip);
        card3 = new Card(customer3AccountType, card3Number, customer3Name, card3CVV, card3Pin, card3TapEnabled, card3HasChip);
        card4 = new Card(customer4AccountType, card4Number, customer4Name, card4CVV, card4Pin, card4TapEnabled, card4HasChip);

        Bank.addIssuer(cardIssuer1);
        Bank.addIssuer(cardIssuer2);

        Bank.addCardIssuer(card1Number, cardIssuer1);
        Bank.addCardIssuer(card2Number, cardIssuer2);

        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.YEAR, 10);
        cardIssuer1.addCardData(card1Number, customer1Name, expiry, card1CVV, balanceAdjustmentAmounts[2]);
        cardIssuer2.addCardData(card2Number, customer2Name, expiry, card2CVV, balanceAdjustmentAmounts[1]);
        cardIssuer3.addCardData(card3Number, customer3Name, expiry, card3CVV, balanceAdjustmentAmounts[1]);

        GiftCard.createGiftCard(card3Number, balanceAdjustmentAmounts[1]);

        Membership.createMembership(card4Number, customer4Name);

        Inventory.addProduct(barcodedProduct);

        selfCheckoutStation.cardReader.detachAll();
        selfCheckoutStation.cardReader.attach(cardHandler);
    }

    @Test
    public void constructorTest()
    {
        assertTrue(selfCheckoutStation.cardReader.detach(cardHandler));

        cardHandler = new CardHandler(selfCheckoutSoftware);

        assertTrue(selfCheckoutStation.cardReader.detach(cardHandler));
    }

    @Test
    public void attachAndDetachAllTest()
    {
        assertTrue(selfCheckoutStation.cardReader.detach(cardHandler));

        cardHandler.attachAll();

        assertTrue(selfCheckoutStation.cardReader.detach(cardHandler));

        cardHandler.attachAll();
        cardHandler.detatchAll();

        assertFalse(selfCheckoutStation.cardReader.detach(cardHandler));
    }

    @Test
    public void enableAndDisableTest()
    {
        assertFalse(selfCheckoutStation.cardReader.isDisabled());

        cardHandler.disableHardware();

        assertTrue(selfCheckoutStation.cardReader.isDisabled());

        cardHandler.enableHardware();

        assertFalse(selfCheckoutStation.cardReader.isDisabled());
    }

    @Test
    public void cardDataReadBankCardTest() throws IOException
    {
        cardHandler.setCustomer(customer1);
        customer1.addProduct(barcodedProduct);
        selfCheckoutSoftware.start(customer1);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);

        performTapOrSwipeOrInsertUntilSuccessful(card1, card1Pin, customer1, "Tap");
    }

    @Test
    public void cardDataReadBankCardFailTest() throws IOException
    {
        Inventory.clear();
        BarcodedProduct product = new BarcodedProduct(barcodedProduct.getBarcode(), barcodedProduct.getDescription(), balanceAdjustmentAmounts[2], barcodedProduct.getExpectedWeight());
        Inventory.addProduct(product);
        cardHandler.setCustomer(customer2);
        customer2.addProduct(product);
        selfCheckoutSoftware.start(customer2);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.BANK_CARD);

        performTapOrSwipeOrInsertUntilSuccessful(card2, card2Pin, customer2, "Insert");
    }

    @Test
    public void cardDataReadMembershipTest() throws IOException
    {
        cardHandler.setCustomer(customer4);
        selfCheckoutSoftware.start(customer4);

        performTapOrSwipeOrInsertUntilSuccessful(card4, card4Pin, customer4, "Tap");
    }

    @Test
    public void cardDataReadMembershipFailTest() throws IOException
    {
        Membership.clear();
        cardHandler.setCustomer(customer4);
        selfCheckoutSoftware.start(customer4);

        performTapOrSwipeOrInsertUntilSuccessful(card4, card4Pin, customer4, "Tap");
    }

    @Test
    public void cardDataReadGiftCardTest() throws IOException
    {
        cardHandler.setCustomer(customer3);
        customer1.addProduct(barcodedProduct);
        selfCheckoutSoftware.start(customer3);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.GIFT_CARD);

        performTapOrSwipeOrInsertUntilSuccessful(card3, card3Pin, customer3, "Swipe");
    }

    @Test
    public void cardDataReadGiftCardFailTest() throws IOException
    {
        Inventory.clear();
        BarcodedProduct product = new BarcodedProduct(barcodedProduct.getBarcode(), barcodedProduct.getDescription(), balanceAdjustmentAmounts[2], barcodedProduct.getExpectedWeight());
        Inventory.addProduct(product);
        cardHandler.setCustomer(customer3);
        customer3.addProduct(product);
        selfCheckoutSoftware.start(customer3);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(product.getBarcode(), product.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.GIFT_CARD);

        performTapOrSwipeOrInsertUntilSuccessful(card3, card3Pin, customer3, "Swipe");
    }

    @Test
    public void cardDataReadGiftCardTest2() throws IOException
    {
        GiftCard.clear();
        cardHandler.setCustomer(customer3);
        customer1.addProduct(barcodedProduct);
        selfCheckoutSoftware.start(customer3);
        selfCheckoutStation.mainScanner.scan(new BarcodedItem(barcodedProduct.getBarcode(), barcodedProduct.getExpectedWeight()));
        selfCheckoutSoftware.addItem();
        selfCheckoutSoftware.checkout();
        selfCheckoutSoftware.selectedPaymentMethod(SelfCheckoutSoftware.PaymentMethod.GIFT_CARD);

        performTapOrSwipeOrInsertUntilSuccessful(card3, card3Pin, customer3, "Swipe");
    }

    @Test
    public void cardDataReadInvalidCardTest() throws IOException
    {
        Card card = new Card("INVALID", card1Number, customer1Name, card1CVV, card1Pin, card1TapEnabled, card1HasChip);
        cardHandler.setCustomer(customer1);
        selfCheckoutSoftware.start(customer1);

        performTapOrSwipeOrInsertUntilSuccessful(card, card1Pin, customer1, "Tap");
    }

    private void performTapOrSwipeOrInsertUntilSuccessful(Card card, String pin, Customer customer, String paymentMethod) throws IOException
    {
        while (true)
        {
            try
            {
                cardHandler.setCustomer(customer);
                switch (paymentMethod)
                {
                    case "Tap" -> selfCheckoutStation.cardReader.tap(card);
                    case "Swipe" -> selfCheckoutStation.cardReader.swipe(card);
                    case "Insert" -> {
                        selfCheckoutStation.cardReader.insert(card, pin);
                        selfCheckoutStation.cardReader.remove();
                    }
                }
                break;
            } catch (ChipFailureException | MagneticStripeFailureException ignored)
            {
                if (paymentMethod.equals("Insert")) selfCheckoutStation.cardReader.remove();
            }
        }
    }
}
