package store;

import java.util.Collections;
import java.util.List;

import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;

public final class Store {
    private static SupervisionSoftware SUPERVISION_SOFTWARE;

    private Store() {
    }

    public static void setSupervisionSoftware(SupervisionSoftware supervisionSoftware) {
        Store.SUPERVISION_SOFTWARE = supervisionSoftware;
    }

    public static SupervisionSoftware getSupervisionSoftware() {
        return SUPERVISION_SOFTWARE;
    }

    public static void addSelfCheckoutSoftware(SelfCheckoutSoftware software) {
        SUPERVISION_SOFTWARE.add(software);
    }

    public static List<SelfCheckoutSoftware> getSelfCheckoutSoftwareList() {
        return Collections.unmodifiableList(SUPERVISION_SOFTWARE.getSoftwareList());
    }

    
}
