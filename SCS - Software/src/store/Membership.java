package store;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.lsmr.selfcheckout.Card.CardData;
import org.lsmr.selfcheckout.external.CardIssuer;

/**
 * This class represents the Membership database.
 * 
 * This class is static since membership database is universal.
 * Refer to: ProductDatabases.java in hardware package.
 * 
 * We have a CardIssuer for membership as well, since for a Card, it's type can
 * be "membership", as said in Card documentation.
 * This would imply that: Membership cards can be used as the same way as other
 * payment cards, to be swiped, inserted or tapped.
 * The only difference would be that the cards cannot be used to pay. Don't
 * think we need to post transactions for these cards, or at least conceptually
 * they're not PAYMENT transactions; could be points transaction.
 * 
 * @author Abdelhak Khalfallah
 * @author Sharjeel Junaid
 * @author Yunfan Yang
 */
public class Membership {
	private static final Set<String> MEMBERS = new HashSet<String>();
	private static CardIssuer MEMBERSHIP_ISSUER = new CardIssuer("Membership");

	/**
	 * Instances of this class are not needed, so the constructor is private.
	 */
	private Membership() {
	}

	public static boolean isMember(String memberID) {
		if (Membership.MEMBERS.contains(memberID))
			return true;
		return false;
	}

	public static void createMembership(String memberID, String holder) {
		if (Membership.MEMBERS.contains(memberID)) {
			throw new IllegalArgumentException("Member already exists.");
		}

		Membership.MEMBERS.add(memberID);

		Calendar expiry = Calendar.getInstance();
		expiry.add(Calendar.YEAR, 10);
		Membership.MEMBERSHIP_ISSUER.addCardData(memberID, holder, expiry, "000", BigDecimal.ONE);
	}

	public static void clear() {
		Membership.MEMBERS.clear();
		Membership.MEMBERSHIP_ISSUER = new CardIssuer("Membership");
	}
}