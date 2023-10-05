package software;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

import software.observers.Observer;
import store.Inventory;

/**
 * 
 * @author Yunfan Yang
 */
public abstract class Software<T extends Observer> {
    private List<T> observers = new ArrayList<T>();

    public void addObserver(T observer) {
        this.observers.add(observer);
    }

    public void removeObserver(T observer) {
        this.observers.remove(observer);
    }

    public List<T> getObservers() {
        return this.observers;
    }

    /**
     * Usage:
     * 
     * <pre>
     * software.notifyObservers(observer -> observer.event(parameter));
     * </pre>
     * 
     * @param function
     * 
     * @author Yunfan Yang
     */
    public void notifyObservers(Function<T, Void> function) {
        for (T observer : observers) {
            function.apply(observer);
        }
    }

    /**
     * Search for product by keywords
     * 
     * @param keyword the keywords string
     * @return a list of products that match the keywords
     */
    public List<Product> lookupProduct(String keyword) {
        List<Product> result = new ArrayList<Product>();

        for (Product product : Inventory.getProducts()) {
            // If the product is a PLU coded product, we need to check the PLU
            // whether PLU is matching the keyword
            if (product instanceof PLUCodedProduct) {
                PLUCodedProduct plu = (PLUCodedProduct) product;

                String pluc = plu.getPLUCode().toString();

                // If it's matching, add to the result
                if (pluc.contains(keyword)) {
                    result.add(product);
                    continue;
                }
            }

            // Get the description of the product
            String description = "";
            if (product instanceof BarcodedProduct) {
                description = ((BarcodedProduct) product).getDescription();
            } else {
                description = ((PLUCodedProduct) product).getDescription();
            }

            // Comparing the description of the product
            if (description.contains(keyword)) {
                result.add(product);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
}
