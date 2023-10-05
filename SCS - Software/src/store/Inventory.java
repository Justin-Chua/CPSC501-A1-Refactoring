package store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.external.ProductDatabases;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

/**
 * Represents the stores inventory.
 * 
 * This is a delegation class for ProductDatabases.
 * The implementation is for software's own use.
 * 
 * This class contains useful helper methods for operating product database.
 * That is, intead of directly access ProductDatabases, use methods in this
 * class.
 * 
 * @author joshuaplosz
 * @author Michelle Cheung
 * @author Yunfan Yang
 */
public class Inventory {
	private static final Map<PriceLookupCode, PLUCodedProduct> PLU_PRODUCT_DATABASE = ProductDatabases.PLU_PRODUCT_DATABASE;
	private static final Map<Barcode, BarcodedProduct> BARCODED_PRODUCT_DATABASE = ProductDatabases.BARCODED_PRODUCT_DATABASE;
	private static final Map<Product, Integer> INVENTORY = ProductDatabases.INVENTORY;

	private Inventory() {
	}

	/**
	 * Used to add barcoded products to the stores inventory (Overloaded method)
	 * 
	 * @param p the product to be added
	 */
	public static void addProduct(BarcodedProduct p) {
		Barcode barcode = p.getBarcode();
		BARCODED_PRODUCT_DATABASE.put(barcode, p);
		INVENTORY.put(p, 0);
	}

	/**
	 * Used to add PLU products to the stores inventory (Overloaded method)
	 * 
	 * @param p the product to be added
	 */
	public static void addProduct(PLUCodedProduct p) {
		PriceLookupCode plu = p.getPLUCode();
		PLU_PRODUCT_DATABASE.put(plu, p);
		INVENTORY.put(p, 0);
	}

	public static void addProduct(Product p) {
		if (p instanceof BarcodedProduct) {
			addProduct((BarcodedProduct) p);
		} else if (p instanceof PLUCodedProduct) {
			addProduct((PLUCodedProduct) p);
		} else {
			throw new IllegalArgumentException("Invalid type of product");
		}
	}

	/**
	 * Change the quantity of inventory of a product
	 * 
	 * @param p        the product
	 * @param quantity the quantity to be changed. Positive to add, negative to
	 *                 remove
	 */
	public static void setQuantity(Product p, int quantity) {
		if (p == null) {
			throw new IllegalArgumentException("Product cannot be null");
		}

		int currentQuantity = INVENTORY.get(p);

		if (currentQuantity + quantity < 0) {
			throw new IllegalArgumentException("Cannot remove more than the current quantity");
		}

		INVENTORY.put(p, currentQuantity + quantity);
	}

	/**
	 * Retrieve the quantity of items that matches the barcode
	 * 
	 * @param p the product
	 * @return quantity if the barcode exists, 0 otherwise
	 */
	public static int getQuantity(Product p) {
		return INVENTORY.get(p);
	}

	/**
	 * Retrieve the Product that matches the barcode
	 * 
	 * @param barcode
	 * @return Product if the barcode exists, null otherwise
	 */
	public static BarcodedProduct getProduct(Barcode barcode) {
		return BARCODED_PRODUCT_DATABASE.get(barcode);
	}

	public static PLUCodedProduct getProduct(PriceLookupCode plu) {
		return PLU_PRODUCT_DATABASE.get(plu);
	}

	public static Map<PriceLookupCode, PLUCodedProduct> getPLUProducts() {
		return Inventory.PLU_PRODUCT_DATABASE;
	}

	public static Map<Barcode, BarcodedProduct> getBarcodedProducts() {
		return Inventory.BARCODED_PRODUCT_DATABASE;
	}

	public static List<Product> getProducts() {
		List<Product> products = new ArrayList<Product>();
		products.addAll(PLU_PRODUCT_DATABASE.values());
		products.addAll(BARCODED_PRODUCT_DATABASE.values());
		return Collections.unmodifiableList(products);
	}

	public static void clear() {
		PLU_PRODUCT_DATABASE.clear();
		BARCODED_PRODUCT_DATABASE.clear();
		INVENTORY.clear();
	}
}
