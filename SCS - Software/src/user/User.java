package user;

public abstract class User {

	/**
	 * the type of user in the simulation
	 * 2 users are currently considered:
	 * 		AppControl.CUSTOMER
	 * 		AppControl.ATTENDANT
	 * 
	 * @return int that represents one of the considered user types
	 */
	public abstract int getUserType();

}
