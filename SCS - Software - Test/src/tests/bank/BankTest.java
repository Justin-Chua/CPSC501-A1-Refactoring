package tests.bank;

import bank.Bank;
import org.junit.Before;
import org.junit.Test;
import org.lsmr.selfcheckout.external.CardIssuer;

import static org.junit.Assert.*;

public class BankTest
{
    // Static variables that will be used during testing
    final String card1Number = "1234";
    final String card2Number = "5678";

    // Declare the card issuers
    CardIssuer cardIssuer1;
    CardIssuer cardIssuer2;

    // Setup that is run before each test case
    @Before
    public void setup()
    {
        // Initialize the card issuers
        cardIssuer1 = new CardIssuer("Visa");
        cardIssuer2 = new CardIssuer("Mastercard");

        // Resets the card issuers
        Bank.clearIssuers();
    }

    @Test
    public void addAndGetIssuerTest()
    {
        assertFalse(Bank.getIssuers().contains(cardIssuer1));
        assertFalse(Bank.getIssuers().contains(cardIssuer2));
        assertEquals(0, Bank.getIssuers().size());

        Bank.addIssuer(cardIssuer1);
        Bank.addIssuer(cardIssuer2);

        assertTrue(Bank.getIssuers().contains(cardIssuer1));
        assertTrue(Bank.getIssuers().contains(cardIssuer2));
        assertEquals(2, Bank.getIssuers().size());
    }

    @Test
    public void removeIssuerTest()
    {
        assertFalse(Bank.getIssuers().contains(cardIssuer1));
        assertFalse(Bank.getIssuers().contains(cardIssuer2));
        assertEquals(0, Bank.getIssuers().size());

        Bank.addIssuer(cardIssuer1);
        Bank.addIssuer(cardIssuer2);
        Bank.removeIssuer(cardIssuer1);
        Bank.removeIssuer(cardIssuer2);

        assertFalse(Bank.getIssuers().contains(cardIssuer1));
        assertFalse(Bank.getIssuers().contains(cardIssuer2));
        assertEquals(0, Bank.getIssuers().size());
    }

    @Test
    public void addCardAndGetIssuerTest()
    {
        Bank.addIssuer(cardIssuer1);
        Bank.addIssuer(cardIssuer2);

        Bank.addCardIssuer(card1Number, cardIssuer1);
        Bank.addCardIssuer(card2Number, cardIssuer2);

        assertEquals(cardIssuer1, Bank.getCardIssuer(card1Number));
        assertEquals(cardIssuer2, Bank.getCardIssuer(card2Number));
    }

    @Test
    public void removeCardTest()
    {
        Bank.addIssuer(cardIssuer1);
        Bank.addIssuer(cardIssuer2);

        Bank.addCardIssuer(card1Number, cardIssuer1);
        Bank.addCardIssuer(card2Number, cardIssuer2);

        assertEquals(cardIssuer1, Bank.getCardIssuer(card1Number));
        assertEquals(cardIssuer2, Bank.getCardIssuer(card2Number));

        Bank.removeCardIssuer(card1Number);
        Bank.removeCardIssuer(card2Number);

        assertNull(Bank.getCardIssuer(card1Number));
        assertNull(Bank.getCardIssuer(card2Number));
    }

    @Test
    public void clearIssuerTest()
    {
        assertFalse(Bank.getIssuers().contains(cardIssuer1));
        assertFalse(Bank.getIssuers().contains(cardIssuer2));
        assertEquals(0, Bank.getIssuers().size());

        Bank.addIssuer(cardIssuer1);
        Bank.addIssuer(cardIssuer2);
        Bank.clearIssuers();

        assertFalse(Bank.getIssuers().contains(cardIssuer1));
        assertFalse(Bank.getIssuers().contains(cardIssuer2));
        assertEquals(0, Bank.getIssuers().size());
    }

    @Test
    public void clearCardsTest()
    {
        Bank.addIssuer(cardIssuer1);
        Bank.addIssuer(cardIssuer2);
        Bank.addCardIssuer(card1Number, cardIssuer1);
        Bank.addCardIssuer(card2Number, cardIssuer2);

        assertEquals(cardIssuer1, Bank.getCardIssuer(card1Number));
        assertEquals(cardIssuer2, Bank.getCardIssuer(card2Number));

        Bank.clearCardIssuers();

        assertNull(Bank.getCardIssuer(card1Number));
        assertNull(Bank.getCardIssuer(card2Number));
    }
}
