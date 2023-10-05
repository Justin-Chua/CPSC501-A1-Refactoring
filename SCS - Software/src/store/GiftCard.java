package store;

import java.util.Set;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashSet;

import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.external.CardIssuer;
/**
 * This class represents the GiftCard database.
 * 
 * This class is static since gift card database is universal.
 * Refer to: ProductDatabases.java in hardware package.
 * 
 * We have a CardIssuer for gift cards as well, since for a Card, it's type can
 * be "giftcard".
 * Gift cards can be used similarly to other payment cards, 
 * payment cards, to be swiped, inserted or tapped.
 * The only difference would be that the cards cannot be used to pay. Don't
 * think we need to post transactions for these cards, or at least conceptually
 * they're not PAYMENT transactions; could be points transaction.
 */
public class GiftCard
{
    private static final Set<String> GIFTCARDS = new HashSet<String>();
	private static CardIssuer GIFTCARD_ISSUER = new CardIssuer("GiftCard");

    private GiftCard()
    {

    }

    public static CardIssuer getCardIssuer()
    {
        return GiftCard.GIFTCARD_ISSUER;
    }

    public static boolean isGiftCard(String cardNumber)
    {
        return GiftCard.GIFTCARDS.contains(cardNumber);
    }
    
    public static void createGiftCard(String cardID, BigDecimal cardValue)
    {
        GiftCard.GIFTCARDS.add(cardID);

        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.YEAR, 10);
        GiftCard.GIFTCARD_ISSUER.addCardData(cardID, cardID, expiry, "000", cardValue);
    }

    public static void clear()
    {
        GiftCard.GIFTCARDS.clear();
        GiftCard.GIFTCARD_ISSUER = new CardIssuer("GiftCard");
    }
}