package user;

import application.AppControl;
import software.SelfCheckoutSoftware;
import user.Customer.CartEntry;

/**
 * This class represents the attendant and their actions.
 * 
 * @author Michelle Cheung
 *
 */
public class Attendant extends User {

	private boolean unexpectedItemDecision;

	private String username;
	private String password;

	/**
	 * remove item if it is in cart
	 * if item is not in cart, ignore and move on
	 */
	public void removeProduct(SelfCheckoutSoftware software, int index) {
		software.getCustomer().removeProduct(index);
	}

	public void removeProduct(SelfCheckoutSoftware software, CartEntry entry) {
		software.getCustomer().removeProduct(entry);
	}

	@Override
	public int getUserType() {
		return AppControl.ATTENDANT;
	}

	/**
	 * The GUI will set the what the attendant made about the unexpected item.
	 */
	public void setUnexpectedItemDecision(boolean value) {
		this.unexpectedItemDecision = value;
	}

	/**
	 * Get the decision the attendant made about the unexpected Item
	 */
	public boolean getUnexpectedItemDecision() {
		return this.unexpectedItemDecision;
	}

	///////////////////// SupervisionSoftware.java /////////////////////

	/**
	 * Set the attendant login information.
	 */
	public void setLogin(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Get the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the password
	 */
	public String getPassword() {
		return password;
	}

	///////////////////// SupervisionSoftware.java /////////////////////

}
