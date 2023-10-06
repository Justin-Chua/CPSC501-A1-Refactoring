package checkout;

import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import software.SelfCheckoutSoftware;
import user.Customer;

public class Screen {
    private final SelfCheckoutSoftware scss;
    private final SelfCheckoutStation scStation;
    private Customer customer;

    public Screen(SelfCheckoutSoftware scss) {
        this.scss = scss;
        this.scStation = this.scss.getSelfCheckoutStation();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void enableHardware() {
        this.scStation.screen.enable();
    }

    public void disableHardware() {
        this.scStation.screen.disable();
    }
}
