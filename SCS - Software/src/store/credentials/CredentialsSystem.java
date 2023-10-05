package store.credentials;

import java.util.HashMap;
import java.util.Map;


public class CredentialsSystem {
	
	private static final Map<String, String> ACCOUNTS = new HashMap<String, String>();

	/**
	 * Instances of this class are not needed, so the constructor is private.
	 */
	private CredentialsSystem(){
	}

	public static void addAccount(String username, String password) {
		CredentialsSystem.ACCOUNTS.put(username, password);
	}
	
	public static void removeAccount(String username) {
		CredentialsSystem.ACCOUNTS.remove(username);
	}
	
	/**
	 * 
	 * @param username - account username
	 * @param password - account password
	 * @return false if check failed; true if the check succeeded. 
	 */
	public static boolean checkLogin(String username, String password) {
		String actualPassword = ACCOUNTS.get(username);
		return password.equals(actualPassword);
	}

}
