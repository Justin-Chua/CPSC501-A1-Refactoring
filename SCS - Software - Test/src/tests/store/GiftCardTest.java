package tests.store;

import org.junit.Before;
import org.junit.Test;
import store.GiftCard;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class GiftCardTest
{
    // Static variables that will be used during testing
    final String giftCard1ID = "1";
    final String giftCard2ID = "2";
    final BigDecimal giftCard1Balance = new BigDecimal("10.00");
    final BigDecimal giftCard2Balance = new BigDecimal("20.00");

    // Setup that is run before each test case
    @Before
    public void setup()
    {
        // Resets the gift card information
        GiftCard.clear();
    }

    @Test
    public void giftCardSignupAndCheckTest()
    {
        assertFalse(GiftCard.isGiftCard(giftCard1ID));
        assertFalse(GiftCard.isGiftCard(giftCard2ID));

        GiftCard.createGiftCard(giftCard1ID, giftCard1Balance);
        GiftCard.createGiftCard(giftCard2ID, giftCard2Balance);

        assertTrue(GiftCard.isGiftCard(giftCard1ID));
        assertTrue(GiftCard.isGiftCard(giftCard2ID));
    }

    @Test
    public void giftCardClearTest()
    {
        assertFalse(GiftCard.isGiftCard(giftCard1ID));
        assertFalse(GiftCard.isGiftCard(giftCard2ID));

        GiftCard.createGiftCard(giftCard1ID, giftCard1Balance);
        GiftCard.createGiftCard(giftCard2ID, giftCard2Balance);
        GiftCard.clear();

        assertFalse(GiftCard.isGiftCard(giftCard1ID));
        assertFalse(GiftCard.isGiftCard(giftCard2ID));
    }

    @Test
    public void getCardIssuerTest()
    {
        assertNotNull(GiftCard.getCardIssuer());
    }
}
