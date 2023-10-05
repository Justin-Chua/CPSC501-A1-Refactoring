package software;

import java.util.ArrayList;
import java.util.List;

import org.lsmr.selfcheckout.devices.SupervisionStation;

import application.Main.Tangibles;
import store.Store;
import store.credentials.AuthorizationRequiredException;
import store.credentials.CredentialsSystem;
import store.credentials.IncorrectCredentialException;
import user.Attendant;
import software.observers.SupervisionObserver;

/**
 * A software for a supervision station.
 * 
 * The creation of this object can be seen as a supervision station software
 * system is launched. The constructor initializes / binds the hardware
 * handlers, the hardware devices have observers.
 * 
 * Use cases implemented here:
 * - Attendant logs in to their control console
 * - Attendant logs out from their control console
 * - Attendant starts up a station
 * - Attendant sthuts down a station
 * 
 * 
 * @author Yunfan Yang
 * @author Tyler Chen
 */
public class SupervisionSoftware extends Software<SupervisionObserver> {
	private SupervisionStation svs;
	private Attendant attendant;
	private boolean logged_in;
	private List<SelfCheckoutSoftware> softwareList = new ArrayList<SelfCheckoutSoftware>();

	public SupervisionSoftware(SupervisionStation svs) {
		this.svs = svs;

		// TODO: Initialize handlers that supervision software needs.
	}

	// For restarting a station, we don't want to restart the other stations too.
	public SupervisionSoftware(SupervisionStation svs, List<SelfCheckoutSoftware> softwareList) {
		this.svs = svs;
		this.softwareList.clear();

		for (SelfCheckoutSoftware software : softwareList) {
			this.add(software);
		}
	}

	public SupervisionStation getSupervisionStation() {
		return this.svs;
	}

	public Attendant getAttendant() {
		return this.attendant;
	}

	public void add(SelfCheckoutSoftware software) {
		this.softwareList.add(software);
		software.setSupervisionSoftware(this);
	}

	public void remove(SelfCheckoutSoftware software) {
		this.softwareList.remove(software);
		software.setSupervisionSoftware(null);
	}

	public void clear() {
		// For each software, remove from list
		for (SelfCheckoutSoftware software : this.softwareList) {
			this.remove(software);
		}

		this.softwareList.clear();
	}

	public List<SelfCheckoutSoftware> getSoftwareList() {
		return this.softwareList;
	}

	/**
	 * Given a username and password, checks if they exist in the database and then
	 * sets the stations attendant to the matching attendant stored in Tangibles.
	 * @param username provided from the user via gui
	 * @param password provided from the user via gui
	 * @throws IncorrectCredentialException
	 */
	public void login(String username, String password) throws IncorrectCredentialException {
		if(CredentialsSystem.checkLogin(username, password)) {
			Tangibles.ATTENDANTS.forEach(attendant -> {
				if (attendant.getUsername().equals(username) && attendant.getPassword().equals(password)) {
					this.attendant = attendant;
					this.logged_in = true;
					return;
				}
			});
		} else {
			throw new IncorrectCredentialException("Attendant credential is invalid");
		}
	}
	
	/**
	 * Logout method to make sure that someone has logged out.
	 * 
	 * @return true if it successfully logs out
	 */
	public void logout() {
		this.attendant = null;
		this.logged_in = false;
	}
	
	/**
	 * Check whether an attendant is logged into the supervision station
	 * @return
	 */
	public boolean isLoggedIn() {
		return this.logged_in;
	}

	/**
	 * Given the hardware, we need to start up the station for use. Assuming that
	 * the station
	 * has been shut off due to some reason.
	 * 
	 * @param scs - SelfCheckoutStation that it wants to boot up.
	 * @return T/F - Whether the software has been booted.
	 *         TODO: Disable hardware? Boot up GUI?
	 *         If it enables hardware, should it before or after? I don't think we
	 *         do
	 *         because the hardware team should have it enabled?
	 */
	public void startUpStation(SelfCheckoutSoftware scss) throws AuthorizationRequiredException {
		if (this.logged_in) {
			scss.startSystem();
		} else {
			throw new AuthorizationRequiredException("Attendant needs to log in");
		}
	}

	/**
	 * Shuts down the software simply by removing it from the HashMap of
	 * checkoutStations
	 * 
	 * @return T/F - whether the checkoutStation has been removed. (If false
	 *         the station most likely is not in the HashMap not exist)
	 */
	public void shutDownStation(SelfCheckoutSoftware scss) throws AuthorizationRequiredException {
		if (this.logged_in) {
			scss.stopSystem();
		} else {
			throw new AuthorizationRequiredException("Attendant needs to log in");
		}
	}

	/**
	 * This function should block the Station. We disable the hardware
	 * so that we cannot receive events.
	 * 
	 * @param scss - the SelfCheckoutSoftware
	 * @return T/F whether the station has been blocked.
	 */
	public void blockStation(SelfCheckoutSoftware scss) throws AuthorizationRequiredException {
		if (this.logged_in) {
			scss.blockSystem();
		} else {
			throw new AuthorizationRequiredException("Attendant needs to log in");
		}
	}

	public void unblockStation(SelfCheckoutSoftware scss) throws AuthorizationRequiredException {
		if (this.logged_in) {
			scss.unblockSystem();
		} else {
			throw new AuthorizationRequiredException("Attendant needs to log in");
		}
	}

	public void approveWeightDiscrepancy(SelfCheckoutSoftware scss) throws AuthorizationRequiredException {
		if (this.logged_in) {
			scss.approveWeightDiscrepancy();
		} else {
			throw new AuthorizationRequiredException("Attendant needs to log in");
		}
	}

	public void approveItemNotBaggable(SelfCheckoutSoftware scss) throws AuthorizationRequiredException {
		if (this.logged_in) {
			scss.addItem();
		} else {
			throw new AuthorizationRequiredException("Attendant needs to log in");
		}
	}
	

	public void approveUseOfOwnBags(SelfCheckoutSoftware scss) throws AuthorizationRequiredException {
		if (this.logged_in) {
			scss.addItem();
		} else {
			throw new AuthorizationRequiredException("Atendant needs to log in");
		}
	}

	/**
	 * Start up the SupervisionSoftware, only by setting the store
	 */
	public void startUp() {
		Store.setSupervisionSoftware(this);
	}

	/**
	 * Should only be used when a full shut down is needed.
	 * WARNING: Loses all SelfCheckoutSoftware
	 * 
	 * @throws AuthorizationRequiredException
	 */
	public void shutdown() throws AuthorizationRequiredException {
		if (this.logged_in) {
			Store.setSupervisionSoftware(null);
			this.clear();
		} else {
			throw new AuthorizationRequiredException("Attendant needs to log in");
		}
	}

	/**
	 * Restarts the Supervision Software while keeping the list of
	 * SelfCheckoutSoftware
	 * 
	 * @throws AuthorizationRequiredException
	 */
	public void restart() throws AuthorizationRequiredException {
		if (this.logged_in) {
			SupervisionSoftware scss = new SupervisionSoftware(svs, softwareList);
			for (SelfCheckoutSoftware software : softwareList)
				software.setSupervisionSoftware(scss);
			Store.setSupervisionSoftware(scss);
		} else {
			throw new AuthorizationRequiredException("Attendant needs to log in");
		}
	}

	public void resolveError(SelfCheckoutSoftware scss) {
		scss.resolveError();
	}
}
