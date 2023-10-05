package bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.external.CardIssuer;

/**
 * This class represents the banks database.
 * 
 * This class is static since bank database is universal.
 * Refer to: ProductDatabases.java in hardware package.
 * 
 * Conceptually, each bank is a card issuer.
 * 
 * @see https://d2l.ucalgary.ca/d2l/le/422926/discussions/threads/1636487/View
 * @author Tyler Chen
 * @author Yunfan Yang
 *
 */
public final class Bank {
	private static final List<CardIssuer> ISSUERS = new ArrayList<CardIssuer>();
	private static final Map<String, CardIssuer> CARD_ISSUER = new HashMap<String, CardIssuer>();

	/**
	 * Instantiation of this class is not needed, so the constructor is private.
	 */
	private Bank() {
	}

	public static List<CardIssuer> getIssuers() {
		return Collections.unmodifiableList(Bank.ISSUERS);
	}

	public static void addIssuer(CardIssuer issuer) {
		Bank.ISSUERS.add(issuer);
	}

	public static void removeIssuer(CardIssuer issuer) {
		Bank.ISSUERS.remove(issuer);
	}

	public static void clearIssuers() {
		Bank.ISSUERS.clear();
	}

	public static CardIssuer getCardIssuer(String cardNumber) {
		return Bank.CARD_ISSUER.get(cardNumber);
	}

	public static void addCardIssuer(String cardNumber, CardIssuer issuer) {
		Bank.CARD_ISSUER.put(cardNumber, issuer);
	}

	public static void removeCardIssuer(String cardNumber) {
		Bank.CARD_ISSUER.remove(cardNumber);
	}

	public static void clearCardIssuers() {
		Bank.CARD_ISSUER.clear();
	}
}
