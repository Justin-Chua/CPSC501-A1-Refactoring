package user;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

import application.AppControl;
import store.Inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer extends User {
	// used to organize list of products
	public class CartEntry {
		private Product product;
		private Double weight;

		private CartEntry(Product product, Double weight) {
			this.product = product;
			this.weight = weight;
		}

		public Product getProduct() {
			return this.product;
		}

		public Double getWeight() {
			return this.weight;
		}
	}

	// List of both PLU and Bar coded products
	private List<CartEntry> cart = new ArrayList<CartEntry>();

	private BigDecimal cashBalance = BigDecimal.ZERO;
	private boolean ownBagsUsed = false;
	private int numOfPlasticBags = 0;
	private String memberID;
	private PriceLookupCode plu;

	public void addCashBalance(BigDecimal value) {
		this.cashBalance = this.cashBalance.add(value);
	}

	public BigDecimal getCashBalance() {
		return new BigDecimal(this.cashBalance.toString());
	}

	/**
	 * Add barcoded product to cart
	 * 
	 * @param product
	 */
	public void addProduct(BarcodedProduct product) {
		this.cart.add(new CartEntry(product, product.getExpectedWeight()));
	}

	/**
	 * Add PLU coded product to cart
	 * 
	 * @param product
	 * @param weight  the weight of the product in grams
	 */
	public void addProduct(PLUCodedProduct product, double weight) {
		this.cart.add(new CartEntry(product, weight));
	}

	public void addProduct(Product product) {
		if (product instanceof BarcodedProduct) {
			this.addProduct((BarcodedProduct) product);
		} else if (product instanceof PLUCodedProduct) {
			// Due to the need for PLU products to have a weight
			throw new IllegalArgumentException("Use proper addToCart for PLU coded items");
		} else {
			throw new IllegalArgumentException("Not a valid product type");
		}
	}

	/**
	 * Remove a product
	 * 
	 * @param index the index in the cart
	 */
	public void removeProduct(int index) {
		this.cart.remove(index);
	}

	/**
	 * Remove a product
	 * 
	 * @param product the entry in the cart
	 */
	public void removeProduct(CartEntry product) {
		this.cart.remove(product);
	}

	public BigDecimal getCartSubtotal() {
		BigDecimal subtotal = BigDecimal.ZERO;

		for (CartEntry entry : this.getCartEntries()) {
			if (entry.getProduct().isPerUnit()) { // If per-unit (barcoded)
				subtotal = subtotal.add(entry.getProduct().getPrice());
			} else { // Else per-kilogram (PLU coded)
				BigDecimal weightKilo = new BigDecimal(entry.getWeight()).divide(new BigDecimal(1000));
				BigDecimal priceKilo = entry.getProduct().getPrice();
				BigDecimal price = weightKilo.multiply(priceKilo);

				subtotal = subtotal.add(price);
			}
		}

		return subtotal;
	}

	/**
	 * Returns a list containing all the products in the cart
	 * 
	 * @return List<Product>
	 */
	public List<Product> getCart() {
		List<Product> list = new ArrayList<Product>();

		for (CartEntry entry : this.getCartEntries()) {
			list.add(entry.getProduct());
		}

		return Collections.unmodifiableList(list);
	}

	/**
	 * Returns a map containing the product itself and its weight
	 * 
	 * @return List<Product>
	 */
	public List<CartEntry> getCartEntries() {
		List<CartEntry> cart = new ArrayList<CartEntry>(this.cart);
		
		// Add plastic bags
		Product plasticBag = Inventory.getProduct(new Barcode(new Numeral[] { Numeral.zero, Numeral.zero, Numeral.zero, Numeral.zero }));
		for (int t = 0; t < this.numOfPlasticBags; t++) {
			cart.add(new CartEntry(plasticBag, 1.0));
		}

		return Collections.unmodifiableList(cart);
	}

	public void setMemberID(String memberID) {
		this.memberID = memberID;
	}

	public String getMemberID() {
		return this.memberID == null ? null : this.memberID.toString();
	}

	// set and get methods for own bags
	// the customer should not be calling this it is used by the scale to set the
	// weight of the bags
	public void setOwnBagsUsed(boolean ownBagsUsed) {
		this.ownBagsUsed = ownBagsUsed;
	}

	public boolean getUseOwnBags() {
		return ownBagsUsed;
	}

	// set and get methods for plastic bags
	public void setPlasticBags(int numOfPlasticBags) {
		this.numOfPlasticBags = numOfPlasticBags;
	}

	public int getPlasticBags() {
		return numOfPlasticBags;
	}

	public boolean hasSufficientCashBalance() {
		return this.getCashBalance().compareTo(this.getCartSubtotal()) >= 0;
	}
	
	public void enterPLUCode(PriceLookupCode plu) {
		this.plu = plu;
	}

	public PriceLookupCode getPLU() {
		return this.plu;
	}

	@Override
	public int getUserType() {
		return AppControl.CUSTOMER;
	}
}
