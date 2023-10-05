package tests.checkout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.NullPointerSimulationException;
import org.lsmr.selfcheckout.Numeral;
import org.lsmr.selfcheckout.SimulationException;
import org.lsmr.selfcheckout.devices.BanknoteDispenser;
import org.lsmr.selfcheckout.devices.CoinDispenser;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SupervisionStation;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.Product;

import checkout.Checkout;
import software.SelfCheckoutSoftware;
import software.SelfCheckoutSoftware.PaymentMethod;
import software.SelfCheckoutSoftware.Phase;
import software.SupervisionSoftware;
import store.Inventory;
import user.Customer;

/**
 * The JUnit test class for the Checkout class in SCS - Software.
 *
 * @author Ricky Bhatti
 * <p>
 * Fix original test cases, and add new test cases for `cancelCheckout`
 * and `makeChanges`.
 * @author Yunfan Yang
 * 
 * Iteration 3 updates
 * @author Rayner Nyud
 */
public class CheckoutTest
{
        Currency currency = Currency.getInstance("USD");

        int[] banknoteDenominations = {1, 5, 10, 20, 100};
        BigDecimal[] coinDenominations = {new BigDecimal("0.01"), new BigDecimal("0.05"), new BigDecimal("0.1"), new BigDecimal("0.25"), new BigDecimal("1.00")};
        Coin coin = new Coin(currency, coinDenominations[3]);
        int scaleMaximumWeight = 10;
        int scaleSensitivity = 1;
        SelfCheckoutStation scs = new SelfCheckoutStation(currency, banknoteDenominations, coinDenominations, scaleMaximumWeight, scaleSensitivity);
        SelfCheckoutSoftware scss = new SelfCheckoutSoftware(scs);
        SupervisionStation supstation = new SupervisionStation();
        SupervisionSoftware sup = new SupervisionSoftware(supstation);

        Numeral[] barcodeNumeral = {Numeral.zero, Numeral.one, Numeral.two, Numeral.three, Numeral.four};
        Barcode barcode = new Barcode(barcodeNumeral);
        BigDecimal price = new BigDecimal("1.00");
        BarcodedProduct b = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("0.99"), 3.12);

        
        
        @Test
        public void constructorTest()
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
    
            assertNotNull("Checkout should never be null", checkout);
        }
        @Test(expected = IllegalStateException.class)
        public void readyToCheckoutNoCustomerSetTest()
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.enablePaymentHardware(PaymentMethod.CASH);
    
            assertTrue("Customer wish to checkout, main scanner should be disabled", scs.mainScanner.isDisabled());
            assertTrue("Customer wish to checkout, handheld scanner should be disabled", scs.handheldScanner.isDisabled());
            assertTrue("Customer wish to checkout, scanning area should be disabled", scs.scanningArea.isDisabled());
        }
        
        
        @Test
        public void readyToCheckoutCashTest()
        {
            Customer customer = new Customer();

            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            checkout.enablePaymentHardware(PaymentMethod.CASH);
    
            assertTrue("Customer wish to checkout, main scanner should be disabled", scs.mainScanner.isDisabled());
            assertTrue("Customer wish to checkout, handheld scanner should be disabled", scs.handheldScanner.isDisabled());
            assertTrue("Customer wish to checkout, scanning area should be disabled", scs.scanningArea.isDisabled());
        }
        
        @Test
        public void readyToCheckoutCCTest()
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            checkout.enablePaymentHardware(PaymentMethod.BANK_CARD);
    
            assertTrue("Customer wish to checkout, main scanner should be disabled", scs.mainScanner.isDisabled());
            assertTrue("Customer wish to checkout, handheld scanner should be disabled", scs.handheldScanner.isDisabled());
            assertTrue("Customer wish to checkout, scanning area should be disabled", scs.scanningArea.isDisabled());
        }
        
        @Test
        public void readyToCheckoutNullTest()
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            checkout.enablePaymentHardware(null);
    
            assertTrue("Customer wish to checkout, main scanner should be disabled", scs.mainScanner.isDisabled());
            assertTrue("Customer wish to checkout, handheld scanner should be disabled", scs.handheldScanner.isDisabled());
            assertTrue("Customer wish to checkout, scanning area should be disabled", scs.scanningArea.isDisabled());
        }
        
        @Test
        public void readyToCheckoutGCTest()
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            checkout.enablePaymentHardware(PaymentMethod.GIFT_CARD);
    
            assertTrue("Customer wish to checkout, main scanner should be disabled", scs.mainScanner.isDisabled());
            assertTrue("Customer wish to checkout, handheld scanner should be disabled", scs.handheldScanner.isDisabled());
            assertTrue("Customer wish to checkout, scanning area should be disabled", scs.scanningArea.isDisabled());
        }
        
        @Test
        public void getCustomerTest() {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            
            assertTrue(checkout.getCustomer().equals(customer));
        }
        
        @Test
        public void hasPendingChangeTest() {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            assertTrue(checkout.hasPendingChange() == false);
        }
        
        @Test(expected = IllegalStateException.class)
        public void makeChangeNotProcessingPaymentTest() {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            scss.setUser(customer);
            customer.addProduct(b);
            scss.addItem();

            scss.checkout();
            checkout.makeChange();
            
        }
        
        @Test(expected = IllegalStateException.class)
        public void makeChangeNoPaymentTest() {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            scss.setUser(customer);
            customer.addProduct(b);
            scss.addItem();

            scss.checkout();
            scss.selectedPaymentMethod(PaymentMethod.CASH);


            checkout.makeChange();
            
        }
    

        @Test
        public void cancelCheckout() throws DisabledException, OverloadException
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            scss.setUser(customer);
            checkout.setCustomer(customer);

            this.scss.addItem();
            this.scss.checkout();
            checkout.enablePaymentHardware(PaymentMethod.CASH);
            checkout.cancelCheckout();
    
            assertTrue("Cancel checkout, main scanner should be disabled", scs.mainScanner.isDisabled());
            assertTrue("Cancel checkout, handheld scanner should be disabled", scs.handheldScanner.isDisabled());
            assertTrue("Cancel checkout, scanning area should be disabled", scs.scanningArea.isDisabled());
        }
    
        @Test(expected = IllegalStateException.class)
        public void cancelCheckout2()
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(null);
            checkout.cancelCheckout();
        }
        
        @Test(expected = IllegalStateException.class)
        public void cancelCheckoutPaidMoreThanSubtotal() throws DisabledException, OverloadException
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            
            BarcodedProduct twoDollarsTest = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
         	sup.add(scss);
            scss.setUser(customer);
            checkout.setCustomer(customer);
            customer.addProduct(twoDollarsTest);

            this.scss.addItem();
            this.scss.checkout();
            checkout.enablePaymentHardware(PaymentMethod.CASH);
            scss.selectedPaymentMethod(PaymentMethod.CASH);
            customer.addCashBalance(new BigDecimal("2.00"));
            // Subtotal < cash balance
            checkout.cancelCheckout();
    
            
        }
        
        @Test
        public void cancelCheckoutPaidLessThanSubtotal() throws DisabledException, OverloadException
        {
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            
            BarcodedProduct twoDollarsTest = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
         	sup.add(scss);
            scss.setUser(customer);
            checkout.setCustomer(customer);
            customer.addProduct(twoDollarsTest);

            this.scss.addItem();
            this.scss.checkout();
            checkout.enablePaymentHardware(PaymentMethod.CASH);
            scss.selectedPaymentMethod(PaymentMethod.CASH);
            customer.addCashBalance(new BigDecimal("0.5"));
            // Subtotal < cash balance
            checkout.cancelCheckout();
            assertTrue("Cancel checkout, main scanner should be disabled", scs.mainScanner.isDisabled());
            assertTrue("Cancel checkout, handheld scanner should be disabled", scs.handheldScanner.isDisabled());
            assertTrue("Cancel checkout, scanning area should be disabled", scs.scanningArea.isDisabled());
            
        }
    
        private void clearDispensers()
        {
            for (BanknoteDispenser dispenser : scs.banknoteDispensers.values())
            {
                try
                {
                    dispenser.unload();
                } catch (SimulationException e)
                {
                    fail("Failed to clear dispenser");
                }
            }
    
            for (CoinDispenser dispenser : scs.coinDispensers.values())
            {
                try
                {
                    dispenser.unload();
                } catch (SimulationException e)
                {
                    fail("Failed to clear dispenser");
                }
            }
        }
    
        /**
         * This is a helper function, to put 100 coins for every denomination in
         * corresponding dispenser.
         */
        private void addCoinsToCoinDispensers()
        {
         //    For each dispenser, add 100 coins
            for (Map.Entry <BigDecimal, CoinDispenser> cds : scs.coinDispensers.entrySet())
            {
                BigDecimal denom = cds.getKey();
                CoinDispenser cd = cds.getValue();
    
                try
                {
                    // Clear the dispenser first, so dispenser is never overloaded by repeatedly
                    // adding 100 coins.
                    cd.unload();
                } catch (Exception e)
                {
                    fail("Coin dispenser unload failed");
                }
    
              //   Add 100 coins
                for (int t = 0; t < 100; t++)
                {
                    try
                    {
                        cd.load(new Coin(denom));
                    } catch (OverloadException e)
                    {
                      fail("Coin Dispenser is full");
                    } catch (SimulationException e)
                    {
                        e.printStackTrace();
                        fail("Not operated at this point");
                    }
                }
    
                assertEquals("Coin dispenser for $" + denom + " should have 100 coins", 100, cd.size());
            }
     
             //Clear coin tray
            scs.coinTray.collectCoins();
        }
    
        private void addBanknotesToBanknotesDispenser()
        {
            // For each dispenser, add 100 banknotes
            for (Entry <Integer, BanknoteDispenser> cds : scs.banknoteDispensers.entrySet())
            {
                int denom = cds.getKey();
                BanknoteDispenser cd = cds.getValue();
    
                try
                {
                    // Clear the dispenser first, so dispenser is never overloaded by repeatedly
                    // adding 100 coins.
                    cd.unload();
                } catch (Exception e)
                {
                    fail("Coin dispenser unload failed");
                }
    
                // Add 100 pieces
                for (int t = 0; t < 100; t++)
                {
                    try
                    {
                        cd.load(new Banknote(currency, denom));
                    } catch (OverloadException e)
                    {
                        fail("Coin Dispenser is full");
                    } catch (SimulationException e)
                    {
                        e.printStackTrace();
                        fail("Not operated at this point");
                    }
                }
    
                assertEquals("Banknote dispenser for $" + denom + " should have 100 pieces", 100, cd.size());
            }
    
            // Remove any left banknotes dangling at the output
            while (true)
            {
                try
                {
                    scs.banknoteOutput.removeDanglingBanknotes();
                } catch (NullPointerSimulationException e)
                {
                    break;
                }
            }
        }
    
        /**
         * This is a helper function, to take all the coins in the tray and sum them up
         *
         * @return the sum of all the coins in the tray
         */
        private BigDecimal getSumOfCoinsInCoinDispenser()
        {
            List <Coin> coins = scs.coinTray.collectCoins();
    
            // Sum up all coins in the coin tray
            BigDecimal sum = BigDecimal.ZERO;
            for (Coin coin : coins)
            {
                if (coin != null)
                {
                    sum = sum.add(coin.getValue());
                }
            }
    
            return sum;
        }
    
        private BigDecimal getSumOfBanknotesInBanknoteOutput()
        {
            BigDecimal sum = BigDecimal.ZERO;
            Banknote[] danglingBanknotes = new Banknote[0];
    
            // Take all the banknotes until there is no more
            while (true) {
            	try
            	{
            		danglingBanknotes = this.scs.banknoteOutput.removeDanglingBanknotes();
                

            	} catch (NullPointerSimulationException e)
            	{
            		// No more banknotes
            		System.out.println("no more banknote");
            		break;
            	}
    
            	// Sum of all the banknotes
            	for (Banknote next : danglingBanknotes)
            	{
            		sum = sum.add(new BigDecimal(next.getValue()));
            		System.out.println("add " + sum);
            	}
            	}
            
    
            return sum;
        }
    

        @Test
        public void makeChangeSingleCoin1()
        {
            Customer customer = new Customer();
            scss.setUser(customer);
            sup.add(scss);
            customer.addProduct(b);
            customer.addCashBalance(new BigDecimal("1.00"));
            System.out.println(customer.getCartSubtotal());
            BigDecimal change = new BigDecimal("0.01");
            Coin.DEFAULT_CURRENCY = currency;
    
            assertEquals("dispenser should have 5 denominations", 5, scs.coinDispensers.size());
    
            this.addCoinsToCoinDispensers();
            scss.addItem();
            scss.checkout();
            scss.selectedPaymentMethod(PaymentMethod.CASH);
            System.out.println(customer.getCashBalance());
            System.out.println(customer.getCartSubtotal());
            scss.makeChange();
    
            BigDecimal sum = this.getSumOfCoinsInCoinDispenser();
            
            assertEquals("Coin tray should have coins with sum of $0.01", change.doubleValue(), sum.doubleValue(), 0.01);
        }
    
        


         @Test
         public void makeChangeSingleCoin2() {
        	 BarcodedProduct twoDollarsTest = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
             Customer customer = new Customer();
             scss.setUser(customer);
             sup.add(scss);
             
             customer.addProduct(twoDollarsTest);
             customer.addCashBalance(new BigDecimal("2.00"));
             BigDecimal change = new BigDecimal("1.00");
             Coin.DEFAULT_CURRENCY = currency;
    
             assertEquals("dispenser should have 5 denominations", 5, scs.coinDispensers.size());
    
             this.addCoinsToCoinDispensers();
             scss.addItem();
             scss.checkout();
             scss.selectedPaymentMethod(PaymentMethod.CASH);
    
             scss.makeChange();
    

             BigDecimal sum = this.getSumOfCoinsInCoinDispenser();
    
             assertEquals("Coin tray should have coins with sum of $1.00", change.doubleValue(), sum.doubleValue(), 0.01);
         }
    
         @Test
         public void makeChangeMultipleCoins1() {
        	 BarcodedProduct twoCoins = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.65"), 3.12);
             Customer customer = new Customer();
             scss.setUser(customer);
             sup.add(scss);
             
             customer.addProduct(twoCoins);
             customer.addCashBalance(new BigDecimal("2.00"));

             BigDecimal change = new BigDecimal("0.35"); // Could be 0.25 + 0.01 or other combinations
             Coin.DEFAULT_CURRENCY = currency;
    
             this.addCoinsToCoinDispensers();
             
             scss.addItem();
             scss.checkout();
             scss.selectedPaymentMethod(PaymentMethod.CASH);
             
             scss.makeChange();
    
             BigDecimal sum = this.getSumOfCoinsInCoinDispenser();

    
             assertEquals("Coin tray should have coins with sum of $0.35", change.doubleValue(), sum.doubleValue(), 0.01);
         }
    
        @Test
        public void makeChangeMultipleCoins2()
        {
        	BarcodedProduct multipleCoin = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.25"), 3.12);
            Customer customer = new Customer();
            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(multipleCoin);
            customer.addCashBalance(new BigDecimal("2.00"));
    
            BigDecimal change = new BigDecimal("0.75"); // Could be 0.50 + 0.25 or other combinations
            Coin.DEFAULT_CURRENCY = currency;
    
            this.addCoinsToCoinDispensers();
            scss.addItem();
            scss.checkout();
            scss.selectedPaymentMethod(PaymentMethod.CASH);
            scss.makeChange();
    
            BigDecimal sum = this.getSumOfCoinsInCoinDispenser();
    
            assertEquals("Coin tray should have coins with sum of $0.75", change.doubleValue(), sum.doubleValue(), 0.01);
        }
    
        @Test
        public void makeChangeNoChange()
        {
            Customer customer = new Customer();
            scss.setUser(customer);
            sup.add(scss);
    
            BigDecimal change = new BigDecimal("0.00");
            Coin.DEFAULT_CURRENCY = currency;
    
            this.addCoinsToCoinDispensers();
            scss.addItem();
            scss.checkout();
            scss.selectedPaymentMethod(PaymentMethod.CASH);
            scss.makeChange();
    
            BigDecimal sum = this.getSumOfCoinsInCoinDispenser();
    
            assertEquals("Coin tray should have coins with sum of $0.00", change.doubleValue(), sum.doubleValue(), 0.01);
        }
    
        @Test
        public void makeChangeNoCoins()
        {
            Customer customer = new Customer();

            scss.setUser(customer);
            sup.add(scss);
            customer.addCashBalance(new BigDecimal("0.76"));
            BigDecimal change = new BigDecimal("0.75");
            Coin.DEFAULT_CURRENCY = currency;
    
            scss.addItem();
            scss.checkout();
            scss.selectedPaymentMethod(PaymentMethod.CASH);
            
            scss.makeChange();
    
            BigDecimal sum = this.getSumOfCoinsInCoinDispenser();
    
            assertEquals("Coin tray should have coins with sum of $0.00, because no coins in dispenser to give to customer", 0.0, sum.doubleValue(), 0.01);
        }
    
        @Test
        public void makeChangeSingleBanknote1()
        {
        	BarcodedProduct notes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
            Customer customer = new Customer();
            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(notes);
            customer.addCashBalance(new BigDecimal("6.00"));
    
            BigDecimal change = new BigDecimal("5.00");
            Coin.DEFAULT_CURRENCY = currency;
    
            this.clearDispensers(); // Force using banknote for change, since coins are empty
            this.addBanknotesToBanknotesDispenser();
            
            scss.addItem();
            scss.checkout();
            scss.selectedPaymentMethod(PaymentMethod.CASH);
            scss.makeChange();
    
            BigDecimal sum = this.getSumOfBanknotesInBanknoteOutput();
    
            assertEquals("Banknote output should have banknotes with sum of $5.00", change.doubleValue(), sum.doubleValue(), 0.01);
        }
        

    
         @Test
         public void makeChangeSingleBanknote2() {
         	BarcodedProduct notes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("10.00"), 3.12);
            Customer customer = new Customer();

            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(notes);
            customer.addCashBalance(new BigDecimal("30.00"));
    
             BigDecimal change = new BigDecimal("20.00");
             Coin.DEFAULT_CURRENCY = currency;
    
             this.clearDispensers(); // Force using banknote for change, since coins are empty
             this.addBanknotesToBanknotesDispenser();
             scss.addItem();
             scss.checkout();
             scss.selectedPaymentMethod(PaymentMethod.CASH);

             scss.makeChange();
    
             BigDecimal sum = this.getSumOfBanknotesInBanknoteOutput();
    
             assertEquals("Banknote output should have banknotes with sum of $20.00", change.doubleValue(),
                     sum.doubleValue(),
                     0.01);
         }
    
         @Test
         public void makeChangeMultipleBanknote1() {
         	BarcodedProduct notes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
            Customer customer = new Customer();
            this.scss.startSystem();
            this.scss.setUser(customer);
            sup.add(this.scss);
            
            customer.addProduct(notes);
            customer.addCashBalance(new BigDecimal("41.00"));
    
             BigDecimal change = new BigDecimal("40.00"); // Could be 20.00 + 20.00, or other combinations
             Coin.DEFAULT_CURRENCY = currency;
    
             this.clearDispensers(); // Force using banknote for change, since coins are empty
             this.addBanknotesToBanknotesDispenser();
             
             this.scss.addItem();
             this.scss.checkout();
             this.scss.selectedPaymentMethod(PaymentMethod.CASH);
    
             this.scss.makeChange();
    
             BigDecimal sum = this.getSumOfBanknotesInBanknoteOutput();
    
             assertEquals("Banknote output should have banknotes with sum of $40.00", change.doubleValue(),
                     sum.doubleValue(),
                     0.01);
         }
    
        @Test
        public void makeChangeMultipleBanknote2()
        {
        	BarcodedProduct notes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
            Customer customer = new Customer();
            Checkout checkout = new Checkout(scss);
            checkout.setCustomer(customer);
            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(notes);
            customer.addCashBalance(new BigDecimal("102.00"));
    
            BigDecimal change = new BigDecimal("101.00"); // Could be 100.00 + 1.00, or other combinations
            Coin.DEFAULT_CURRENCY = currency;
    
            this.clearDispensers(); // Force using banknote for change, since coins are empty
            this.addBanknotesToBanknotesDispenser();
            
            this.scss.addItem();
            this.scss.checkout();
            this.scss.selectedPaymentMethod(PaymentMethod.CASH);
    
            this.scss.makeChange();
    
            BigDecimal sum = this.getSumOfBanknotesInBanknoteOutput();
    
            assertEquals("Banknote output should have banknotes with sum of $101.00", change.doubleValue(), sum.doubleValue(), 0.01);
        }
    
        @Test
        public void makeChangeOneCoinAndOneBanknote()
        {
        	BarcodedProduct coinandnotes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
            Customer customer = new Customer();
            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(coinandnotes);
            customer.addCashBalance(new BigDecimal("21.25"));
    
            BigDecimal change = new BigDecimal("20.25"); // Could be 20.00 bn + 0.25 c, or other combinations
            Coin.DEFAULT_CURRENCY = currency;
    
            this.addBanknotesToBanknotesDispenser();
            this.addCoinsToCoinDispensers();

            scss.addItem();
            scss.checkout();
            scss.selectedPaymentMethod(PaymentMethod.CASH);

            scss.makeChange();
    
            BigDecimal bs = this.getSumOfBanknotesInBanknoteOutput();
            BigDecimal cs = this.getSumOfCoinsInCoinDispenser();
    
            BigDecimal sum = BigDecimal.ZERO;
            sum = sum.add(bs);
            sum = sum.add(cs);
    
            assertEquals("Banknote output should have banknotes with sum of $20.25", change.doubleValue(), sum.doubleValue(), 0.01);
        }
    
         @Test
         public void makeChangeMultipleCoinsAndOneBanknote() {
         	BarcodedProduct notes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
            Customer customer = new Customer();
            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(notes);
            customer.addCashBalance(new BigDecimal("21.85"));
    
             BigDecimal change = new BigDecimal("20.85"); // Could be 20.00 bn + 0.25 c + 0.50 c + 0.10 c, or other
                                                          // combinations
             Coin.DEFAULT_CURRENCY = currency;
    
             this.addBanknotesToBanknotesDispenser();
             this.addCoinsToCoinDispensers();
    
             scss.addItem();
             scss.checkout();
             scss.selectedPaymentMethod(PaymentMethod.CASH);
             scss.makeChange();
    
             BigDecimal bs = this.getSumOfBanknotesInBanknoteOutput();
             BigDecimal cs = this.getSumOfCoinsInCoinDispenser();
    
            BigDecimal sum = BigDecimal.ZERO;
             sum = sum.add(bs);
             sum = sum.add(cs);
    
             assertEquals("Banknote output should have banknotes with sum of $20.85", change.doubleValue(),
                     sum.doubleValue(),
                     0.01);
         }
    
         @Test
         public void makeChangeOneCoinAndMultipleBanknotes() {
         	BarcodedProduct notes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
            Customer customer = new Customer();

            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(notes);
            customer.addCashBalance(new BigDecimal("121.25"));
    
             BigDecimal change = new BigDecimal("120.25"); // 100.00 bn + 20.00 bn + 0.25 c, or other combinations
             Coin.DEFAULT_CURRENCY = currency;
    
             this.addBanknotesToBanknotesDispenser();
             this.addCoinsToCoinDispensers();
             scss.addItem();
             scss.checkout();
             scss.selectedPaymentMethod(PaymentMethod.CASH);
             scss.makeChange();
    
             BigDecimal bs = this.getSumOfBanknotesInBanknoteOutput();
             BigDecimal cs = this.getSumOfCoinsInCoinDispenser();
    
             BigDecimal sum = BigDecimal.ZERO;
             sum = sum.add(bs);
             sum = sum.add(cs);
    
             assertEquals("Banknote output should have banknotes with sum of $120.25", change.doubleValue(),
                     sum.doubleValue(),
                     0.01);
         }
    
        @Test
        public void makeChangeMultipleCoinsAndMultipleBanknotes()
        {
        	BarcodedProduct notes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
            Customer customer = new Customer();

            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(notes);
            customer.addCashBalance(new BigDecimal("137.78"));
    
            BigDecimal change = new BigDecimal("136.78");
            Coin.DEFAULT_CURRENCY = currency;
    
            this.addBanknotesToBanknotesDispenser();
            this.addCoinsToCoinDispensers();
    
            this.scss.addItem();
            this.scss.checkout();
            this.scss.selectedPaymentMethod(PaymentMethod.CASH);

            this.scss.makeChange();

    
            BigDecimal bs = this.getSumOfBanknotesInBanknoteOutput();
            BigDecimal cs = this.getSumOfCoinsInCoinDispenser();
        
            BigDecimal sum = BigDecimal.ZERO;
            sum = sum.add(bs);
            System.out.println(sum);
            sum = sum.add(cs);
            System.out.println(sum);
    
            assertEquals("Banknote output should have banknotes with sum of $136.78", change.doubleValue(), sum.doubleValue(), 0.01);
        }
    
         @Test
         public void makeChangeMultipleCoinsAndMultipleBanknotesBigAmount() {
         	BarcodedProduct notes = new BarcodedProduct(barcode, "Fake Product", new BigDecimal("1.00"), 3.12);
            Customer customer = new Customer();
            scss.setUser(customer);
            sup.add(scss);
            
            customer.addProduct(notes);
            customer.addCashBalance(new BigDecimal("1050.48"));
    
            BigDecimal change = new BigDecimal("1049.48");
             Coin.DEFAULT_CURRENCY = currency;
    
             this.addBanknotesToBanknotesDispenser();
             this.addCoinsToCoinDispensers();
             scss.addItem();
             scss.checkout();
             scss.selectedPaymentMethod(PaymentMethod.CASH);
             scss.makeChange();
    
             BigDecimal bs = this.getSumOfBanknotesInBanknoteOutput();
             BigDecimal cs = this.getSumOfCoinsInCoinDispenser();
    
             BigDecimal sum = BigDecimal.ZERO;
             sum = sum.add(bs);
             sum = sum.add(cs);
    
            assertEquals("Banknote output should have banknotes with sum of $1049.48", change.doubleValue(),
                     sum.doubleValue(),
                     0.01);
         }
    

    
        class FakeItem extends Item
        {
            public FakeItem(double weight)
            {
                super(weight);
            }
        }
    
        class FakeProduct extends Product
        {
            public FakeProduct(BigDecimal price)
            {
                super(price, false);
            }
        }
    
    
        /**
         * Testing the checkout "sums up the subtotal" loop
         */
        @Test
        public void inventorySumUpTotal()
        {
            Inventory.addProduct(b);
    
            BarcodedProduct product_test = Inventory.getProduct(barcode);

            assertEquals("product should be the same", b, product_test);
            assertEquals("price should be the same", new BigDecimal("0.99"), product_test.getPrice());
    
            Customer customer = new Customer();
            customer.addProduct(product_test);
    
            List <Product> cart = customer.getCart();
            assertEquals("cart should have one item", 1, cart.size());
            assertEquals("cart should have the same item", product_test, cart.get(0));
    
            Checkout checkout = new Checkout(scss);
            
            BigDecimal subtotal = customer.getCartSubtotal();
            assertEquals("Subtotal should be 0.99", new BigDecimal("0.99"), subtotal);
        }
}
