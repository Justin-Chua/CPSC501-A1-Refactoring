package checkout;

import org.lsmr.selfcheckout.devices.SelfCheckoutStation;

import software.SelfCheckoutSoftware;
import user.Customer;

public class Screen {
    private final SelfCheckoutSoftware scss;
    private final SelfCheckoutStation scs;
    private Customer customer;

    public Screen(SelfCheckoutSoftware scss) {
        this.scss = scss;
        this.scs = this.scss.getSelfCheckoutStation();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void enableHardware() {
        this.scs.screen.enable();
    }

    public void disableHardware() {
        this.scs.screen.disable();
    }
}
