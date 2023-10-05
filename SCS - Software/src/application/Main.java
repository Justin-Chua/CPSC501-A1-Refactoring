package application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.JDialog;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.PLUCodedItem;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.SimulationException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.ReceiptPrinter;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import org.lsmr.selfcheckout.external.CardIssuer;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;

import GUI.BanknoteWallet;
import GUI.CoinWallet;
import GUI.GUI;
import bank.Bank;
import store.Membership;
import store.Store;
import store.credentials.CredentialsSystem;
import user.Attendant;
import software.SelfCheckoutSoftware;
import software.SupervisionSoftware;
import store.Inventory;

/**
 * This class represents the entry point of entire software.
 * 
 * This class is to set up everything for simulation. This is not a part of the
 * software.
 * 
 * This class has a singleton Store instance.
 * This class initializes both the store, external parties, and tangibles.
 * 
 * The singleton initializes the following for simulation:
 * - the bank (CardIssuer)
 * - the inventory (Inventory)
 * - the store (Store)
 * 
 * @author Yunfan Yang
 */

public final class Main {
    private static Store store;

    public static void main(String[] args) {
        Main.initializeCardAndIssuers();
        Main.initializeProductDatabase();
        Main.initializeStore();
        Main.initializeMembership();
        Main.initializeCredentialsSytem();

        GUI.init(new AppControl());
    }

    public static void initializeCardAndIssuers() {
        Bank.clearIssuers();
        Bank.clearCardIssuers();

        CardIssuer rbc = new CardIssuer("RBC");
        CardIssuer scotia = new CardIssuer("Scotiabank");

        Calendar expiry1 = Calendar.getInstance();
        expiry1.set(Calendar.YEAR, expiry1.get(Calendar.YEAR) + 1);
        String cardNo1 = "4510123456789000";
        rbc.addCardData(cardNo1, "Yunfan Yang", expiry1, "054", new BigDecimal("11903.56"));
        Card card1 = new Card("credit", cardNo1, "Yunfan Yang", "054", "8522", true, true);
        Tangibles.PAYMENT_CARDS.add(card1);

        Calendar expiry2 = Calendar.getInstance();
        expiry2.set(Calendar.YEAR, expiry2.get(Calendar.YEAR) + 1);
        String cardNo2 = "4510987654321000";
        scotia.addCardData(cardNo2, "Joshua Plosz", expiry2, "563", new BigDecimal("19532.20"));
        Card card2 = new Card("debit", cardNo2, "Joshua Plosz", "563", "9423", true, true);
        Tangibles.PAYMENT_CARDS.add(card2);

        Calendar expiry3 = Calendar.getInstance();
        expiry3.set(Calendar.YEAR, expiry3.get(Calendar.YEAR) + 2);
        String cardNo3 = "4511220329440683";
        scotia.addCardData(cardNo3, "Tyler Chen", expiry3, "232", new BigDecimal("6046.89"));
        Card card3 = new Card("debit", cardNo3, "Tyler Chen", "232", "1111", true, true);
        Tangibles.PAYMENT_CARDS.add(card3);

        Bank.addIssuer(rbc);
        Bank.addIssuer(scotia);
        Bank.addCardIssuer(cardNo1, rbc);
        Bank.addCardIssuer(cardNo2, scotia);
        Bank.addCardIssuer(cardNo3, scotia);
    }

    public static void initializeProductDatabase() {
        Inventory.clear();

        // PLU coded items
        PriceLookupCode cornCode = new PriceLookupCode("4055");
        PLUCodedProduct corn = new PLUCodedProduct(cornCode, "Corn", new BigDecimal("2.00"));
        Inventory.addProduct(corn);

        PriceLookupCode tomatoCode = new PriceLookupCode("4165");
        PLUCodedProduct tomato = new PLUCodedProduct(tomatoCode, "Tomato", new BigDecimal("1.99"));
        Inventory.addProduct(tomato);

        PriceLookupCode beetCode = new PriceLookupCode("1055");
        PLUCodedProduct beet = new PLUCodedProduct(beetCode, "Beet", new BigDecimal("2.49"));
        Inventory.addProduct(beet);

        PriceLookupCode gAppleCode = new PriceLookupCode("4066");
        PLUCodedProduct gApple = new PLUCodedProduct(gAppleCode, "Green Apple", new BigDecimal("1.68"));
        Inventory.addProduct(gApple);

        PriceLookupCode avocadoCode = new PriceLookupCode("3945");
        PLUCodedProduct avocado = new PLUCodedProduct(avocadoCode, "Avocado", new BigDecimal("2.99"));
        Inventory.addProduct(avocado);

        // Barcoded items
        Barcode coffeeCode = randomBarcode();
        int coffeeWeight = 940;
        BarcodedProduct coffee = new BarcodedProduct(coffeeCode, "Coffee", new BigDecimal("13.80"), coffeeWeight);
        Inventory.addProduct(coffee);

        Barcode fruitLoopsCode = randomBarcode();
        int fruitLoopsWeight = 400;
        BarcodedProduct fruitLoops = new BarcodedProduct(fruitLoopsCode, "Fruit Loops", new BigDecimal("5.95"),
                fruitLoopsWeight);
        Inventory.addProduct(fruitLoops);

        Barcode kraftDinnerCode = randomBarcode();
        int kraftDinnerWeight = 120;
        BarcodedProduct kraftDinner = new BarcodedProduct(kraftDinnerCode, "Kraft Dinner", new BigDecimal("2.49"),
                kraftDinnerWeight);
        Inventory.addProduct(kraftDinner);

        Barcode plasticBagCode = new Barcode(new Numeral[] { Numeral.zero, Numeral.zero, Numeral.zero, Numeral.zero });
        int plasticBagWeight = 1;
        BarcodedProduct plasticBag = new BarcodedProduct(plasticBagCode, "Plastic Bag", new BigDecimal("0.1"),
                plasticBagWeight);
        Inventory.addProduct(plasticBag);

        // Add 1 of each plu item
        // they should be unique because of their weight
        Vector<PLUCodedItem> pItems = new Vector<>();
        pItems.add(new PLUCodedItem(cornCode, 194));
        pItems.add(new PLUCodedItem(cornCode, 186));
        pItems.add(new PLUCodedItem(cornCode, 222));
        pItems.add(new PLUCodedItem(cornCode, 220));
        pItems.add(new PLUCodedItem(cornCode, 210));
        Inventory.setQuantity(corn, 5);

        pItems.add(new PLUCodedItem(tomatoCode, 105));
        pItems.add(new PLUCodedItem(tomatoCode, 139));
        pItems.add(new PLUCodedItem(tomatoCode, 112));
        pItems.add(new PLUCodedItem(tomatoCode, 124));
        Inventory.setQuantity(tomato, 4);

        pItems.add(new PLUCodedItem(beetCode, 140));
        pItems.add(new PLUCodedItem(beetCode, 144));
        pItems.add(new PLUCodedItem(beetCode, 136));
        Inventory.setQuantity(beet, 3);

        pItems.add(new PLUCodedItem(gAppleCode, 240));
        pItems.add(new PLUCodedItem(gAppleCode, 230));
        pItems.add(new PLUCodedItem(gAppleCode, 233));
        pItems.add(new PLUCodedItem(gAppleCode, 242));
        pItems.add(new PLUCodedItem(gAppleCode, 240));
        pItems.add(new PLUCodedItem(gAppleCode, 251));
        Inventory.setQuantity(gApple, 6);

        // I don't envy data entry workers DX

        pItems.add(new PLUCodedItem(avocadoCode, 290));
        pItems.add(new PLUCodedItem(avocadoCode, 301));
        pItems.add(new PLUCodedItem(avocadoCode, 299));
        pItems.add(new PLUCodedItem(avocadoCode, 300));
        Inventory.setQuantity(avocado, 4);

        pItems.forEach(it -> Tangibles.ITEMS.add(it));

        // add several barcoded items
        // no need for uniqueness as weight is not recorded per item
        Vector<BarcodedItem> bItems = new Vector<>();

        int quantityOfEachBarcodedItem = 6;
        for (int i = 0; i < quantityOfEachBarcodedItem; i++) {
            bItems.add(new BarcodedItem(coffeeCode, coffeeWeight));
            bItems.add(new BarcodedItem(fruitLoopsCode, fruitLoopsWeight));
            bItems.add(new BarcodedItem(kraftDinnerCode, kraftDinnerWeight));
        }

        Inventory.setQuantity(coffee, quantityOfEachBarcodedItem);
        Inventory.setQuantity(fruitLoops, quantityOfEachBarcodedItem);
        Inventory.setQuantity(kraftDinner, quantityOfEachBarcodedItem);

        bItems.forEach(bi -> Tangibles.ITEMS.add(bi));

    }

    // We assume we are working in Canadian denominations
    public static void initializeStore() {
        Currency currency = Configurations.currency;
        int[] banknoteDenominations = { 5, 10, 20, 50, 100 };
        BigDecimal[] coinDenominations = {
                new BigDecimal("0.05"),
                new BigDecimal("0.10"),
                new BigDecimal("0.25"),
                new BigDecimal("1.00"),
                new BigDecimal("2.00")
        };

        // Initialize supervision station
        SupervisionStation svs = new SupervisionStation();
        Tangibles.SUPERVISION_STATION = svs;

        // Create supervision software for this svs
        // and set it to the store
        SupervisionSoftware svss = new SupervisionSoftware(svs);
        svss.startUp();

        // Initialize n self-checkout stations
        // and add them to the supervision station to be supervised
        for (int t = 0; t < Configurations.stations; t++) {
            SelfCheckoutStation station = new SelfCheckoutStation(currency, banknoteDenominations,
                    coinDenominations, 80000, 2);

            // Add ink to the station
            try {
                station.printer.addInk(ReceiptPrinter.MAXIMUM_INK);
            } catch (OverloadException e) {
                e.printStackTrace();
            }

            // Add paper to the station
            try {
                station.printer.addPaper(ReceiptPrinter.MAXIMUM_PAPER);
            } catch (OverloadException e) {
                e.printStackTrace();
            }

            if (t != 5) {
                station.banknoteDispensers.forEach((value, dispenser) -> {
                    for (int i = 0; i < SelfCheckoutStation.BANKNOTE_DISPENSER_CAPACITY; i++) {
                        Banknote note = new Banknote(currency, value);
                        try {
                            dispenser.load(note);
                        } catch (OverloadException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }

            if (t != 5) {
                station.coinDispensers.forEach((value, dispenser) -> {
                    for (int i = 0; i < SelfCheckoutStation.COIN_DISPENSER_CAPACITY; i++) {
                        Coin coin = new Coin(currency, value);
                        try {
                            dispenser.load(coin);
                        } catch (SimulationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (OverloadException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (t == 5) {
                for (int i = 0; i < SelfCheckoutStation.COIN_STORAGE_CAPACITY - 1; i++) {
                    Coin coin = new Coin(currency, new BigDecimal(1.00));
                    try {
                        station.coinStorage.load(coin);
                    } catch (SimulationException e) {
                        e.printStackTrace();
                    } catch (OverloadException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < SelfCheckoutStation.BANKNOTE_STORAGE_CAPACITY - 1; i++) {
                    Banknote note = new Banknote(currency, 10);
                    try {
                        station.banknoteStorage.load(note);
                    } catch (SimulationException e) {
                        e.printStackTrace();
                    } catch (OverloadException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            // Add this station to tangibles, and add this station to the supervision
            // station
            Tangibles.SELF_CHECKOUT_STATIONS.add(station);
            Tangibles.SUPERVISION_STATION.add(station);

            // Create SelfCheckoutSoftware for this station
            // and add this softeare to supervision software
            SelfCheckoutSoftware software = new SelfCheckoutSoftware(station);
            Store.addSelfCheckoutSoftware(software);
            
        }
    }

    /**
     * Generates a random barcode that does not exist yet.
     * 
     * @return
     */
    private static Barcode randomBarcode() {
        Random rand = new Random();
        Barcode barcode;
        Numeral[] nums = new Numeral[4];

        do {
            nums[0] = Numeral.valueOf((byte) rand.nextInt(10));
            nums[1] = Numeral.valueOf((byte) rand.nextInt(10));
            nums[2] = Numeral.valueOf((byte) rand.nextInt(10));
            nums[3] = Numeral.valueOf((byte) rand.nextInt(10));
            barcode = new Barcode(nums);
        } while (Inventory.getBarcodedProducts().containsKey(barcode));
        return barcode;
    }

    public static void initializeMembership() {
        Membership.clear();

        String card1No = "12345";
        String card2No = "49555";
        String card1Holder = "Gagan";
        String card2Holder = "Justin";

        Card card1 = new Card("membership", card1No, card1Holder, null, null, true, false);
        Card card2 = new Card("membership", card2No, card2Holder, null, null, true, false);
        Tangibles.MEMBER_CARDS.add(card1);
        Tangibles.MEMBER_CARDS.add(card2);

        Membership.createMembership(card1No, card1Holder);
        Membership.createMembership(card2No, card2Holder);
    }

    public static void initializeCredentialsSytem() {
        String username1 = "Sharjeel";
        String password1 = "1234";
        CredentialsSystem.addAccount(username1, password1);

        String username2 = "Richi";
        String password2 = "123password";
        CredentialsSystem.addAccount(username2, password2);

        Attendant a1 = new Attendant();
        a1.setLogin(username1, password1);

        Attendant a2 = new Attendant();
        a2.setLogin(username2, password2);

        // for ease of testing
        String username3 = "a";
        String password3 = "a";
        CredentialsSystem.addAccount(username3, password3);
        Attendant a3 = new Attendant();
        a3.setLogin(username3, password3);

        Tangibles.ATTENDANTS.add(a1);
        Tangibles.ATTENDANTS.add(a2);

        Tangibles.ATTENDANTS.add(a3);
    }

    public static Store getStore() {
        if (Main.store == null) {
            Main.main(null);
        }

        return Main.store;
    }

    /**
     * This class is to keep track of all the tangibles in the simulation world.
     * 
     * GUI can grab tangibles and interact them with hardware, in order to trigger
     * softwares.
     * Eg, GUI can grab an item and use Barcode scanner to scan it.
     * When this item is scanned, software will be notified via observers.
     * 
     * This is necessary, because software should never deal with tangibles, namely,
     * Cards, Items, Cash, etc.
     * For items, simulation should use barcode scanner or touch screen to input,
     * via Hardware. Once hardware has these events happened, software will be
     * notified via attached observers. At this point, software should only know
     * about Barcode or PLU code (from observer arguments), and software finds the
     * corresponding product information.
     * For cards, simulation should use, eg, CardReader to read the card
     * information. Once card information is read by hardware, CardData will be
     * passed into the attached observers, and software will be notified. At this
     * point, software would only deal with CardData instead of tangible Card.
     * (Therefore, previous implementation of Inventory class which contains dealing
     * with Item is incorrect)
     * TODO: Banknote and Coin
     * 
     * As mentioned in Membership class, it is also a type of Card. To distinguish
     * membership cards and bank payment cards, two copntants are provided.
     * 
     * This clas is for simulation and not a part of the software.
     * 
     * @author Yunfan Yang
     */
    public static class Tangibles {
        public static SupervisionStation SUPERVISION_STATION;
        public static final List<SelfCheckoutStation> SELF_CHECKOUT_STATIONS = new ArrayList<SelfCheckoutStation>();

        public static final List<Item> ITEMS = new ArrayList<Item>();
        public static final List<Card> MEMBER_CARDS = new ArrayList<Card>();
        public static final List<Card> PAYMENT_CARDS = new ArrayList<Card>();
        public static final List<Attendant> ATTENDANTS = new ArrayList<Attendant>();

        /**
         * This class is not to be instantiated.
         */
        private Tangibles() {
        }
    }

    /**
     * Just to keep track of some global variables
     * 
     * @author Yunfan Yang
     *
     */
    public static class Configurations {
        public static final Currency currency = Currency.getInstance("CAD");
        public static final int stations = 6;
    }
}