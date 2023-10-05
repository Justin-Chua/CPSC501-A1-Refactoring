package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

import application.AppControl;
import application.Main.Tangibles;
import software.SelfCheckoutSoftware;
import software.SelfCheckoutSoftware.Phase;
import store.Inventory;
import store.Store;
import store.credentials.AuthorizationRequiredException;

public class Scenes {

	public static final int SC_OVERVIEW = 0;
	public static final int AS_TOUCH = 1;
	public static final int SCS_OVERVIEW = 2;
	public static final int SCS_TOUCH = 3;
	public static final int SCS_CARDREADER = 4;
	public static final int SCS_MAINTENANCE = 5;

	// arbitrary application window size
	// Note: this resolution size must be at least the
	// size of all scenes that have hard coded sizes
	private static final int xResolution = 1280;
	private static final int yResolution = 720;

	private static JFrame filterFrame = new JFrame();

	// reference to the current station we are interacting with
	// 0 = attendant station; 1-6 = self checkout 1-6; -1 = not at a station
	private int currentStation;
	public int newUserPrompt = -1;

	private Color defaultBackground = new Color(220, 227, 230);

	public Scenes() {
		initDimmingFilter();
	}

	public int getCurrentStation() {
		return currentStation;
	}

	public void setCurrentStation(int currentStation) {
		this.currentStation = currentStation;
	}

	/**
	 * First applies a dimming filter to hide interaction with background
	 * scenes, then constructs the scene requested.
	 * 
	 * @param scene - one of the following scenes:
	 *              SC_OVERVIEW, AS_TOUCH, SCS_OVERVIEW, SCS_TOUCH, SCS_CARDREADER,
	 *              SCS_MAINTENANCE
	 * @return
	 */
	public JFrame getScene(int scene) {
		setDimmingFilter();

		if (scene == SC_OVERVIEW) {

			setCurrentStation(-1);
			return new SC_Overview_Scene().getScene();

		} else if (scene == AS_TOUCH) {

			return new AS_Touchscreen_Scene().getScene();

		} else if (scene == SCS_OVERVIEW) {

			return new SCS_Overview_Scene().getScene();

		} else if (scene == SCS_TOUCH) {

			return new SCS_Touch_Scene().getScene();

		} else if (scene == SCS_CARDREADER) {

			return new SCS_Cardreader_Scene().getScene();

		} else if (scene == SCS_MAINTENANCE) {

			return new SCS_Maintenance_Scene().getScene();

		}

		return null;
	}

	// #######################################################################
	// Self-Checkout Overview Scene
	// #######################################################################
	private class SC_Overview_Scene extends JFrame implements ActionListener, MouseListener {

		// Self-Checkout Overview interactable componenets
		JButton[] sbn; // station buttons
		JButton abn; // attendant button

		JLabel banner_info = new JLabel();
		JLabel banner_title = new JLabel();

		public JFrame getScene() {
			// init the scene and retrieve the JPanel canvas in which to build on
			JPanel scene = preprocessScene(this, xResolution, yResolution);

			// include a banner for navigation
			generateBanner(scene, true, banner_info, banner_title, "X");

			// This overview scene should be the only scene to
			// terminate the actual program. Set a window
			// listener here to exit when scene is exitted.
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

			this.addWindowFocusListener(new WindowAdapter() {
				public void windowGainedFocus(WindowEvent e) {
					if (newUserPrompt == -1) {
						// prompt the user to reply with what type of user they are
						do {
							int userType = promptForUserType();
							if (userType == -1) {
								System.exit(0);
							}
							newUserPrompt = (userType == 0) ? AppControl.CUSTOMER : AppControl.ATTENDANT;

						} while (!GUI.newUser(newUserPrompt));
					}
				}
			});

			// main content panel of the scene
			// contains the visually interactable
			// components in the scene.
			JPanel content = new JPanel();
			content.setSize(1280, 670);
			content.setBackground(defaultBackground);
			content.setLayout(null);

			// create panels to mark where the self checkout stations
			// will be in our scene. Accompany them with buttons to
			// provide a way to select the station.
			List<SelfCheckoutSoftware> scssList = Store.getSelfCheckoutSoftwareList();
			int numOfSCS = scssList.size();
			JPanel[] scs = new JPanel[numOfSCS];
			sbn = new JButton[numOfSCS];

			for (int i = 0; i < numOfSCS; i++) {
				// create an individual station panel
				scs[i] = new JPanel();
				scs[i].setLayout(null);
				scs[i].setBounds((175 * (i + 1)) + 50, (60 * i) + 25, 150, 150);
				scs[i].setBackground(new Color((20 * i) + 100, (10 * i) + 120, 180));

				// create a button to select a particular station
				sbn[i] = new JButton();
				sbn[i].setName("Use Self-Checkout Station " + (i + 1));
				sbn[i].setFont(new Font("Arial", Font.BOLD, 12));
				sbn[i].setText("Station " + (i + 1));
				sbn[i].setBounds(30, 35, 90, 40);
				sbn[i].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				sbn[i].addActionListener(this);
				sbn[i].addMouseListener(this);

				sbn[i].putClientProperty("station-id", i + 1); // add one to record station instead of index
				sbn[i].putClientProperty("station-scss", scssList.get(i));

				// add button to the stations panel
				scs[i].add(sbn[i]);

				// add station panel to the main content panel
				content.add(scs[i]);
			}

			// create a panel for the attendant station
			JPanel as = new JPanel();
			as.setLayout(null);
			as.setBounds(125, 450, 220, 125);
			as.setBackground(new Color(190, 200, 180));

			// create a button to select the attendant station
			abn = new JButton();
			abn.setName("Use the Attendant Station");
			abn.setFont(new Font("Arial", Font.BOLD, 12));
			abn.setText("Attendant Station");
			abn.setBounds(35, 35, 150, 40);
			abn.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			abn.addActionListener(this);
			abn.addMouseListener(this);
			abn.putClientProperty("station-id", 0);
			as.add(abn); // add attendant selection button to it's panel

			// add the swing components to the main content panel
			content.add(as);

			// add the visual content to the scene
			scene.add(content);

			// to display frame, make visible after adding all components
			this.setVisible(true);

			return this;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == abn) { // attendant station
				if (GUI.userApproachesStation(0)) {
					banner_title.setText("Attendant");
					this.repaint();
				}
			} else {
				// self-checkout stations
				int station = (int) ((JButton) e.getSource()).getClientProperty("station-id");

				if (GUI.userApproachesStation(station)) {
					banner_title.setText("Customer");
					this.repaint();
				}
			}
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
			banner_info.setText(e.getComponent().getName());
			this.repaint();
		}

		public void mouseExited(MouseEvent e) {
			banner_info.setText("");
			this.repaint();
		}
	}

	// #######################################################################
	// Self-Checkout Station Overview Scene
	// #######################################################################
	private class SCS_Overview_Scene extends JFrame implements ActionListener {

		// actionable components to add to listeners
		JButton bagScale;
		JButton bnInSlot;
		JButton bnOutSlot;
		JButton maintenance;
		JButton coinInSlot;
		JButton coinTray;
		JButton scanner;
		JButton handScanner;
		JButton cardReader;
		JButton printer;
		JButton touchscreen;

		JLabel banner_info = new JLabel();
		JLabel banner_title = new JLabel();

		JLabel paid;
		JLabel subtotal;
		JLabel nextItem;

		public JFrame getScene() {
			JPanel scene = preprocessScene(this, 900, 600);

			generateBanner(scene, false, banner_info, banner_title, "END");

			int i = getCurrentStation();
			banner_title.setText("Station " + i + "  ");

			this.addWindowFocusListener(new WindowAdapter() {
				public void windowGainedFocus(WindowEvent e) {
					banner_info.setText(GUI.getUserInstruction(SCS_OVERVIEW));
					updateDisplay();

				}
			});

			JPanel content = new JPanel();
			content.setBackground(new Color(220 - (i * 5), 227 - (i * 7), 230 - (i * 4)));
			content.setLayout(null);

			// update amount paind
			paid = new JLabel();
			paid.setBounds(200, 170, 150, 40);
			paid.setFont(new Font("Arial", Font.BOLD, 16));
			paid.setHorizontalAlignment(JLabel.CENTER);
			paid.setText(GUI.getAmountPaid(currentStation));
			paid.setBorder(BorderFactory.createLineBorder(Color.red));
			paid.setFocusable(false);
			paid.setOpaque(true);
			content.add(paid);

			// update subtotal
			subtotal = new JLabel();
			subtotal.setBounds(200, 10, 150, 40);
			subtotal.setFont(new Font("Arial", Font.BOLD, 16));
			subtotal.setHorizontalAlignment(JLabel.CENTER);
			subtotal.setText(GUI.getSubtotal(currentStation));
			subtotal.setBorder(BorderFactory.createLineBorder(Color.blue));
			subtotal.setFocusable(false);
			subtotal.setOpaque(true);
			content.add(subtotal);

			// customers next item to process
			nextItem = new JLabel();
			nextItem.setBounds(200, 55, 150, 110);
			nextItem.setFont(new Font("Arial", Font.BOLD, 16));
			nextItem.setHorizontalAlignment(SwingConstants.CENTER);
			nextItem.setHorizontalTextPosition(SwingConstants.CENTER);
			nextItem.setText(GUI.getNextItemDescription(currentStation));
			nextItem.setBorder(BorderFactory.createLineBorder(Color.gray));
			nextItem.setFocusable(false);
			nextItem.setOpaque(true);
			content.add(nextItem);

			// bagging scale
			bagScale = new JButton();
			bagScale.setBounds(70, 350, 300, 140);
			bagScale.setFont(new Font("Arial", Font.BOLD, 20));
			bagScale.setText("Bagging Area");
			bagScale.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			bagScale.addActionListener(this);
			bagScale.setFocusable(false);
			content.add(bagScale);

			// banknote input slot
			bnInSlot = new JButton();
			bnInSlot.setBounds(400, 370, 110, 30);
			bnInSlot.setFont(new Font("Arial", Font.BOLD, 14));
			bnInSlot.setText("Banknote In");
			bnInSlot.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			bnInSlot.addActionListener(this);
			bnInSlot.setFocusable(false);
			content.add(bnInSlot);

			// banknote output slot
			bnOutSlot = new JButton();
			bnOutSlot.setBounds(540, 370, 110, 30);
			bnOutSlot.setFont(new Font("Arial", Font.BOLD, 14));
			bnOutSlot.setText("Banknote Out");
			bnOutSlot.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			bnOutSlot.addActionListener(this);
			bnOutSlot.setFocusable(false);
			content.add(bnOutSlot);

			// maintenance hatch
			maintenance = new JButton();
			maintenance.setBounds(400, 420, 250, 100);
			maintenance.setFont(new Font("Arial", Font.BOLD, 18));
			maintenance.setText("Maintenance Hatch");
			maintenance.setBorder(BorderFactory.createLineBorder(Color.black, 1, false));
			maintenance.addActionListener(this);
			maintenance.setFocusable(false);
			content.add(maintenance);

			// coin input slot
			coinInSlot = new JButton();
			coinInSlot.setBounds(720, 370, 50, 70);
			coinInSlot.setFont(new Font("Arial", Font.BOLD, 14));
			coinInSlot.setText("<html>Coin<br>In</html>");
			coinInSlot.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			coinInSlot.addActionListener(this);
			coinInSlot.setFocusable(false);
			content.add(coinInSlot);

			// coin tray
			coinTray = new JButton();
			coinTray.setBounds(695, 470, 100, 50);
			coinTray.setFont(new Font("Arial", Font.BOLD, 14));
			coinTray.setText("Coin Tray");
			coinTray.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			coinTray.addActionListener(this);
			coinTray.setFocusable(false);
			content.add(coinTray);

			// item weigh scale
			JLabel weighScale = new JLabel();
			weighScale = new JLabel();
			weighScale.setBounds(435, 250, 215, 90);
			weighScale.setFont(new Font("Arial", Font.BOLD, 16));
			weighScale.setText("Item Weigh Scale");
			weighScale.setHorizontalAlignment(SwingConstants.CENTER);
			weighScale.setBorder(BorderFactory.createLineBorder(Color.black, 3, true));
			weighScale.setFocusable(false);
			weighScale.setOpaque(true);
			content.add(weighScale);

			// stationary barcode scanner
			scanner = new JButton();
			scanner.setBounds(670, 250, 150, 90);
			scanner.setFont(new Font("Arial", Font.BOLD, 16));
			scanner.setText("Barcode Scanner");
			scanner.setBorder(BorderFactory.createLineBorder(Color.black, 3, true));
			scanner.addActionListener(this);
			scanner.setFocusable(false);
			content.add(scanner);

			// handheld barcode scanner
			handScanner = new JButton();
			handScanner.setBounds(750, 100, 60, 90);
			handScanner.setFont(new Font("Arial", Font.BOLD, 12));
			handScanner.setText("<html>Handheld<br>Scanner</html>");
			handScanner.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			handScanner.addActionListener(this);
			handScanner.setFocusable(false);
			content.add(handScanner);

			// card reader
			cardReader = new JButton();
			cardReader.setBounds(670, 50, 60, 90);
			cardReader.setFont(new Font("Arial", Font.BOLD, 12));
			cardReader.setText("<html>Card<br>Reader</html>");
			cardReader.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			cardReader.addActionListener(this);
			cardReader.setFocusable(false);
			content.add(cardReader);

			// receipt printer
			printer = new JButton();
			printer.setBounds(670, 170, 60, 40);
			printer.setFont(new Font("Arial", Font.BOLD, 12));
			printer.setText("Receipt");
			printer.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			printer.addActionListener(this);
			printer.setFocusable(false);
			content.add(printer);

			// touchscreen
			touchscreen = new JButton();
			touchscreen.setBounds(450, 50, 200, 140);
			touchscreen.setFont(new Font("Arial", Font.BOLD, 18));
			touchscreen.setText("Touchscreen");
			touchscreen.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			touchscreen.addActionListener(this);
			touchscreen.setFocusable(false);
			content.add(touchscreen);

			// import self-checkout station model
			content.add(new BackgroundImage("src/GUI/images/selfcheckoutstation_model.png"));

			scene.add(content);

			this.setVisible(true);

			return this;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == bagScale) {
				GUI.userBagsItem(currentStation);
				// updateDisplay();
				if (GUI.getPhase(currentStation) == Phase.PAYMENT_COMPLETE) {
					GUI.removePaidItemsFromBagging();
				}
			} else if (e.getSource() == bnInSlot) {
				if (GUI.getPhase(currentStation) == Phase.CHOOSING_PAYMENT_METHOD ||
						GUI.getPhase(currentStation) == Phase.PROCESSING_PAYMENT)
					getBanknoteFromUser();
			} else if (e.getSource() == bnOutSlot) {
				GUI.userRemovesBanknote(currentStation);
			} else if (e.getSource() == maintenance) {
				GUI.userServicesStation(currentStation);
			} else if (e.getSource() == coinInSlot) {
				if (GUI.getPhase(currentStation) == Phase.CHOOSING_PAYMENT_METHOD ||
						GUI.getPhase(currentStation) == Phase.PROCESSING_PAYMENT)
					getCoinFromUser();
			} else if (e.getSource() == coinTray) {
				GUI.userRemovesCoins(currentStation);
			} else if (e.getSource() == scanner) {
				GUI.userScansItem(currentStation, true);
				// updateDisplay();
			} else if (e.getSource() == handScanner) {
				GUI.userScansItem(currentStation, false);
				// updateDisplay();
			} else if (e.getSource() == cardReader) {
				GUI.userAccessCardReader(currentStation);
			} else if (e.getSource() == printer) {
				GUI.userRemovesReceipt(currentStation);
			} else if (e.getSource() == touchscreen) {
				GUI.userAccessTouchscreen(currentStation);
			}
			updateDisplay();
		}

		private void updateDisplay() {
			if (GUI.getPhase(currentStation) == Phase.BAGGING_ITEM) {
				nextItem.setForeground(new Color(220, 30, 40));
				nextItem.setFont(new Font("Arial", Font.BOLD, 18));
				nextItem.setText("> BAG ITEM <");
			} else if (GUI.getPhase(currentStation) == Phase.PAYMENT_COMPLETE) {
				bagScale.setForeground(new Color(220, 30, 40));
				bagScale.setFont(new Font("Arial", Font.BOLD, 18));
				bagScale.setText("> REMOVE ALL ITEMS <");
				bagScale.repaint();
			} else if (GUI.getPhase(currentStation) == Phase.IDLE) {
				bagScale.setForeground(Color.black);
				bagScale.setFont(new Font("Arial", Font.BOLD, 20));
				bagScale.setText("Bagging Area");
				bagScale.repaint();
			} else {
				nextItem.setForeground(Color.black);
				nextItem.setFont(new Font("Arial", Font.BOLD, 16));
				nextItem.setText(GUI.getNextItemDescription(currentStation));
			}

			nextItem.repaint();

			paid.setText(GUI.getAmountPaid(currentStation));
			paid.repaint();

			subtotal.setText(GUI.getSubtotal(currentStation));
			subtotal.repaint();

		}

	}

	// #######################################################################
	// Attendant Station Touch Screen Scene
	// #######################################################################
	private class AS_Touchscreen_Scene extends JFrame implements ActionListener {

		// cosmetic panel colors
		Color tint_one = new Color(220, 230, 234);
		Color tint_two = new Color(205, 220, 230);

		JLabel[] station_status = new JLabel[Tangibles.SUPERVISION_STATION.supervisedStationCount()];
		JLabel[] station_light = new JLabel[Tangibles.SUPERVISION_STATION.supervisedStationCount()];
		JButton[] station_block = new JButton[Tangibles.SUPERVISION_STATION.supervisedStationCount()];
		JButton[] station_approve = new JButton[Tangibles.SUPERVISION_STATION.supervisedStationCount()];
		JButton[] station_startup = new JButton[Tangibles.SUPERVISION_STATION.supervisedStationCount()];

		JLabel banner_info = new JLabel();
		JLabel banner_title = new JLabel();

		public JFrame getScene() {
			JPanel scene = preprocessScene(this, 900, 650);

			JPanel banner = generateBanner(scene, false, banner_info, banner_title, "X");
			JPanel end = (JPanel) banner.getComponent(banner.getComponentCount() - 1);

			JButton logout = new JButton();
			logout.addActionListener(e -> {
				Store.getSupervisionSoftware().logout();
				this.dispose();
				Scenes.errorMsg("Successfully logged out from the supervision station.");
			});
			logout.setPreferredSize(new Dimension(150, 50));
			logout.setFont(new Font("Arial", Font.BOLD, 20));
			logout.setFocusable(false);
			logout.setText("LOGOUT");
			end.add(logout, 1);

			JPanel content = new JPanel();
			content.setBackground(defaultBackground);
			content.setLayout(null);

			Border border = BorderFactory.createLineBorder(Color.black, 2, true);

			// separate each station on it's own pseudo-banner for organization
			for (int i = 0; i < Tangibles.SUPERVISION_STATION.supervisedStationCount(); i++) {

				JPanel station = new JPanel();
				station.setLayout(null);
				station.setBounds(0, i * 100, 900, 100);
				station.setBackground((i % 2 == 0) ? tint_one : tint_two);

				// display the stations relavent status to the attendant
				station_status[i] = new JLabel();
				station_status[i].setFont(new Font("Lucida Grande", Font.BOLD, 16));
				station_status[i].setText(GUI.stationStatus(i));
				station_status[i].setBounds(30, 10, 200, 80);
				station_status[i].setHorizontalAlignment(JLabel.CENTER);
				station.add(station_status[i]);

				Color station_light_color = checkStationAttention(i);

				// station light draws attention to erogenous status'
				station_light[i] = new JLabel();
				station_light[i].setBounds(260, 30, 40, 40);
				station_light[i].setBackground(station_light_color);
				station_light[i].setOpaque(true);
				station.add(station_light[i]);

				// display which station this banner is associated with
				JLabel station_label = new JLabel();
				station_label.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
				station_label.setText("STATION " + (i + 1));
				station_label.setBounds(335, 10, 130, 80);
				station_label.setHorizontalAlignment(JLabel.CENTER);
				station_label.setBorder(border);
				station_label.setBackground((i % 2 == 0) ? tint_two : tint_one);
				station_label.setOpaque(true);
				station.add(station_label);

				// create a button for the attendant to block/unblock a station
				station_block[i] = new JButton();
				station_block[i].setBounds(485, 25, 130, 50);
				station_block[i].setFont(new Font("Lucida Grande", Font.BOLD, 12));
				station_block[i].setText(checkBlockStatus(i));
				station_block[i].addActionListener(this);
				station_block[i].setFocusable(false);
				station.add(station_block[i]);

				// create a button for the attendant to approve relevant erogenous states
				station_approve[i] = new JButton();
				station_approve[i].setBounds(635, 25, 130, 50);
				station_approve[i].setFont(new Font("Lucida Grande", Font.BOLD, 12));
				station_approve[i].setText("APPROVE");
				station_approve[i].addActionListener(this);
				station_approve[i].setFocusable(false);
				station.add(station_approve[i]);

				station_startup[i] = new JButton();
				station_startup[i].setBounds(785, 25, 80, 50);
				station_startup[i].setFont(new Font("Lucida Grande", Font.BOLD, 12));
				station_startup[i].setText("START");
				station_startup[i].addActionListener(this);
				station_startup[i].setFocusable(false);
				station.add(station_startup[i]);

				content.add(station);
			}

			scene.add(content);

			this.setVisible(true);

			// only prompt attendant to log in if not already done so
			if (!GUI.isAttendantLoggedIn()) {
				setDimmingFilter();
				promptAttendantForLogIn();
			}

			return this;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < Tangibles.SUPERVISION_STATION.supervisedStationCount(); i++) {
				if (e.getSource() == station_block[i]) {
					GUI.attendantBlockToggle(i);
					station_block[i].setText(checkBlockStatus(i));
					station_status[i].setText(GUI.stationStatus(i));
					station_light[i].setBackground(checkStationAttention(i));
				} else if (e.getSource() == station_approve[i]) {
					GUI.attendantApprovesStation(i);
					station_status[i].setText(GUI.stationStatus(i));
					station_light[i].setBackground(checkStationAttention(i));
				} else if (e.getSource() == station_startup[i]) {
					GUI.startupStation(i+1);
					station_status[i].setText(GUI.stationStatus(i)); 
					station_light[i].setBackground(checkStationAttention(i));
				}
			}
		}
	}

	private static boolean expectingPLUCode = false;
	private static boolean expectingMembershipNum = false;
	private static boolean expectingCreditPIN = false;
	private static boolean expectingDebitPIN = false;

	// #######################################################################
	// Self-Checkout Station Touch Screen Scene
	// #######################################################################
	private class SCS_Touch_Scene extends JFrame implements ActionListener {

		JButton search;
		JButton plu_code;
		JButton checkout;
		JButton attendant;
		JButton ownBags;
		JButton membership;
		JButton skip;

		JLabel banner_info = new JLabel();
		JLabel banner_title = new JLabel();

		boolean shouldClose = false;
		JFrame window = this;

		public JFrame getScene() {
			JPanel scene = preprocessScene(this, 750, 500);

			generateBanner(scene, true, banner_info, banner_title, "X");

			this.addWindowFocusListener(new WindowAdapter() {
				public void windowGainedFocus(WindowEvent e) {
					if (shouldClose) {
						window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
					}
				}
			});

			JPanel content = new JPanel();
			content.setBackground(defaultBackground);
			content.setLayout(null);

			// search for product
			search = new JButton();
			search.setBounds(50, 50, 200, 60);
			search.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
			search.setText("SEARCH FOR ITEM");
			search.setHorizontalTextPosition(JButton.CENTER);
			search.addActionListener(this);
			search.setFocusable(false);
			content.add(search);

			// enter plu code
			plu_code = new JButton();
			plu_code.setBounds(50, 140, 200, 60);
			plu_code.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
			plu_code.setText("ENTER PLU CODE");
			plu_code.setHorizontalTextPosition(JButton.CENTER);
			plu_code.addActionListener(this);
			plu_code.setFocusable(false);
			content.add(plu_code);

			// proceed to checkout
			checkout = new JButton();
			checkout.setBounds(140, 300, 280, 100);
			checkout.setFont(new Font("Lucida Grande", Font.BOLD, 18));
			checkout.setText("PROCEED TO CHECKOUT");
			checkout.setHorizontalTextPosition(JButton.CENTER);
			checkout.addActionListener(this);
			checkout.setFocusable(false);
			content.add(checkout);

			// attedant options
			attendant = new JButton();
			attendant.setBounds(550, 340, 150, 60);
			attendant.setFont(new Font("Lucida Grande", Font.BOLD, 14));
			attendant.setText("ATTENDANT");
			attendant.setHorizontalTextPosition(JButton.CENTER);
			attendant.addActionListener(this);
			attendant.setFocusable(false);
			content.add(attendant);

			// use own bags
			ownBags = new JButton();
			ownBags.setBounds(500, 50, 200, 60);
			ownBags.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
			ownBags.setText("USE OWN BAGS");
			ownBags.setHorizontalTextPosition(JButton.CENTER);
			ownBags.addActionListener(this);
			ownBags.setFocusable(false);
			content.add(ownBags);

			// membership number
			membership = new JButton();
			membership.setBounds(500, 140, 200, 60);
			membership.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
			membership.setText("ENTER MEMBERSHIP #");
			membership.setHorizontalTextPosition(JButton.CENTER);
			membership.addActionListener(this);
			membership.setFocusable(false);
			content.add(membership);

			// skip bagging
			skip = new JButton();
			skip.setBounds(275, 140, 200, 60);
			skip.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
			skip.setText("SKIP BAGGING");
			skip.setHorizontalTextPosition(JButton.CENTER);
			skip.addActionListener(this);
			skip.setFocusable(false);
			content.add(skip);

			scene.add(content);

			this.setVisible(true);

			return this;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == search) {
				promptSelectItems();
				shouldClose = true;
			} else if (e.getSource() == plu_code) {
				expectingPLUCode = true;
				getNumberFromUser("Enter the PLU code");
				shouldClose = true;
			} else if (e.getSource() == checkout) {
				GUI.proceedToCheckout();
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			} else if (e.getSource() == attendant) {
				if (promptAttendantForPassword()) {
					stationAttendantOptions();
				}
			} else if (e.getSource() == ownBags) {
				GUI.userUsesOwnBags(currentStation);
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			} else if (e.getSource() == membership) {
				expectingMembershipNum = true;
				getNumberFromUser("<html>Enter your<br>Membership number</html>");
			} else if (e.getSource() == skip) {
				GUI.userSkipsBagging();
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			}
		}
	}

	// #######################################################################
	// Self-Checkout Station Card Reader Scene
	// #######################################################################
	private class SCS_Cardreader_Scene extends JFrame implements MouseListener {

		JLabel tap;
		JLabel swipe;
		JLabel insert;

		JLabel banner_info = new JLabel();
		JLabel banner_title = new JLabel();

		int cardType;

		public JFrame getScene() {
			JPanel scene = preprocessScene(this, 250, 350);

			generateBanner(scene, true, banner_info, banner_title, "X");

			JPanel content = new JPanel();
			content.setBackground(defaultBackground);
			content.setLayout(null);

			// card tap
			tap = new JLabel();
			tap.setBounds(40, 40, 130, 110);
			tap.setText("TAP");
			tap.setOpaque(true);
			tap.setHorizontalAlignment(JLabel.CENTER);
			tap.addMouseListener(this);
			content.add(tap);

			// card swipe
			swipe = new JLabel();
			swipe.setBounds(190, 40, 20, 220);
			swipe.setText("<html>S<br>W<br>I<br>P<br>E</html>");
			swipe.setOpaque(true);
			swipe.setHorizontalAlignment(JLabel.CENTER);
			swipe.setHorizontalTextPosition(JLabel.CENTER);
			swipe.addMouseListener(this);
			content.add(swipe);

			// card insert
			insert = new JLabel();
			insert.setBounds(40, 190, 130, 70);
			insert.setText("INSERT");
			insert.setOpaque(true);
			insert.setHorizontalAlignment(JLabel.CENTER);
			insert.addMouseListener(this);
			content.add(insert);

			scene.add(content);

			this.setVisible(true);

			return this;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getSource() == tap) {
				GUI.userTapsCard(promptCustomerForCard());
				this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			} else if (e.getSource() == swipe) {
				GUI.userSwipesCard(promptCustomerForCard());
				this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			} else if (e.getSource() == insert) {
				if (cardType == AppControl.CREDIT) {
					expectingCreditPIN = true;
				} else if (cardType == AppControl.DEBIT) {
					expectingDebitPIN = true;
				}
				getNumberFromUser("Enter your PIN");
				this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

	}

	// #######################################################################
	// Self-Checkout Station Maintenance Scene
	// #######################################################################
	private class SCS_Maintenance_Scene extends JFrame implements ActionListener {

		JButton refillBnDispensers;
		JButton addPaper;
		JButton addInk;
		JButton refillCoinDispensers;
		JButton bnEmptyStorage;
		JButton coinEmptyStorage;

		JLabel banner_info = new JLabel();
		JLabel banner_title = new JLabel();

		public JFrame getScene() {
			JPanel scene = preprocessScene(this, 600, 400);

			generateBanner(scene, true, banner_info, banner_title, "X");

			JPanel content = new JPanel();
			content.setBackground(defaultBackground);
			content.setLayout(null);

			// banknote dispensers
			JLabel bnDispensers = new JLabel();
			bnDispensers.setBounds(40, 30, 120, 125);
			bnDispensers.setBackground(Color.lightGray);
			bnDispensers.setOpaque(true);
			bnDispensers.setHorizontalAlignment(JLabel.CENTER);

			refillBnDispensers = new JButton();
			refillBnDispensers.setBounds(10, 10, 100, 105);
			refillBnDispensers.setText("<html>Refill Banknote<br>Dispsensers</html>");
			refillBnDispensers.addActionListener(this);
			bnDispensers.add(refillBnDispensers);

			content.add(bnDispensers);

			// receipt printer
			JPanel printer = new JPanel();
			printer.setLayout(null);
			printer.setBounds(170, 30, 260, 125);
			printer.setBackground(Color.lightGray);
			printer.setOpaque(true);

			addPaper = new JButton();
			addPaper.setBounds(15, 10, 230, 45);
			addPaper.setText("ADD PAPER");
			addPaper.addActionListener(this);
			printer.add(addPaper);

			addInk = new JButton();
			addInk.setBounds(15, 70, 230, 45);
			addInk.setText("ADD INK");
			addInk.addActionListener(this);
			printer.add(addInk);

			content.add(printer);

			// coin dispensers
			JLabel coinDispensers = new JLabel();
			coinDispensers.setBounds(440, 30, 120, 125);
			coinDispensers.setBackground(Color.lightGray);
			coinDispensers.setOpaque(true);

			refillCoinDispensers = new JButton();
			refillCoinDispensers.setBounds(10, 10, 100, 105);
			refillCoinDispensers.setText("<html>Refill Coin<br>Dispsensers</html>");
			refillCoinDispensers.addActionListener(this);
			coinDispensers.add(refillCoinDispensers);

			content.add(coinDispensers);

			// banknote storage
			JPanel bnStorage = new JPanel();
			bnStorage.setLayout(null);
			bnStorage.setBounds(40, 180, 250, 125);
			bnStorage.setBackground(Color.lightGray);
			bnStorage.setOpaque(true);

			bnEmptyStorage = new JButton();
			bnEmptyStorage.setBounds(10, 25, 230, 75);
			bnEmptyStorage.setText("EMPTY BANKNOTE STORAGE");
			bnEmptyStorage.addActionListener(this);
			bnStorage.add(bnEmptyStorage);

			content.add(bnStorage);

			// coin storage
			JPanel coinStorage = new JPanel();
			coinStorage.setLayout(null);
			coinStorage.setBounds(310, 180, 250, 125);
			coinStorage.setBackground(Color.lightGray);
			coinStorage.setOpaque(true);

			coinEmptyStorage = new JButton();
			coinEmptyStorage.setBounds(10, 25, 230, 75);
			coinEmptyStorage.setText("EMPTY COIN STORAGE");
			coinEmptyStorage.addActionListener(this);
			coinStorage.add(coinEmptyStorage);

			content.add(coinStorage);

			scene.add(content);

			this.setVisible(true);

			return this;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == refillBnDispensers) {
				GUI.refillBanknoteDispensers();
			} else if (e.getSource() == refillCoinDispensers) {
				GUI.refillCoinDispenser();
			} else if (e.getSource() == addPaper) {
				GUI.addPaper(currentStation);
			} else if (e.getSource() == addInk) {
				GUI.addInk(currentStation);
			} else if (e.getSource() == bnEmptyStorage) {
				GUI.emptyBanknoteStorage();
			} else if (e.getSource() == coinEmptyStorage) {
				GUI.emptyCoinStorage();
			}
		}
	}

	// cosmetic status indicator colors
	Color red_light = new Color(235, 80, 70);
	Color green_light = new Color(80, 225, 80);

	/**
	 * Checks if the station provided is in a state that requires the
	 * attendants attention.
	 * 
	 * @param station - index of the station to check
	 * @return a green Color if the station does not require attendants attention, a
	 *         red Color otherwise
	 */
	private Color checkStationAttention(int station) {
		return (GUI.stationStatus(station) != "BLOCKED" && GUI.stationStatus(station) != "WEIGHT DISCREPANCY" &&
				GUI.stationStatus(station) != "ITEM NOT BAGGED" && GUI.stationStatus(station) != "USE OWN BAGS"
				&& GUI.stationStatus(station) != "STATION OFFLINE") 
				? green_light : red_light;
	}

	/**
	 * Sends a keypad prompt to the user to input a number.
	 * A message is passed along to the keypad.
	 * 
	 * @param msg - message to display to the user
	 */
	public void getNumberFromUser(String msg) {
		new Keypad(msg, this);
	}

	/**
	 * 
	 */
	public void getCoinFromUser() {
		new CoinWallet(this);
	}

	/**
	 * 
	 */
	public void getBanknoteFromUser() {
		new BanknoteWallet(this);
	}

	/**
	 * Displays a window with self checkout station options that only
	 * a logged in attendant can do.
	 */
	public void stationAttendantOptions() {
		JFrame authorizedWindow = new JFrame();
		authorizedWindow.addWindowFocusListener(new WindowAdapter() {
			public void windowLostFocus(WindowEvent e) {
				authorizedWindow.dispatchEvent(new WindowEvent(authorizedWindow, WindowEvent.WINDOW_CLOSING));
			}
		});

		JPanel options = preprocessScene(authorizedWindow, 300, 300);
		options.setLayout(null);

		// remove previously processed item
		JButton removeItem = new JButton();
		removeItem.setBounds(50, 50, 200, 80);
		removeItem.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		removeItem.setText("REMOVE ITEM");
		removeItem.setFocusable(false);
		removeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				promptRemoveItems();
				authorizedWindow.dispatchEvent(new WindowEvent(authorizedWindow, WindowEvent.WINDOW_CLOSING));
			}
		});
		options.add(removeItem);

		// shutdown station
		JButton shutdown = new JButton();
		shutdown.setBounds(50, 170, 200, 80);
		shutdown.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		shutdown.setText("SHUTDOWN STATION");
		shutdown.setFocusable(false);
		shutdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					GUI.shutdownStation(currentStation);
				} catch (AuthorizationRequiredException e1) {
					Scenes.errorMsg("incorrect login info");
				}
				authorizedWindow.dispatchEvent(new WindowEvent(authorizedWindow, WindowEvent.WINDOW_CLOSING));
			}
		});
		options.add(shutdown);

		authorizedWindow.add(options);
		authorizedWindow.setVisible(true);
	}

	/**
	 * Checks if a station is in the BLOCKED state or not.
	 * 
	 * @param station - index of station to check
	 * @return "UNBLOCK" if the station IS blocked, "BLOCK" otherwise
	 */
	private String checkBlockStatus(int station) {
		return (GUI.stationStatus(station) == "BLOCKED")
				? "UNBLOCK"
				: "BLOCK";
	}

	/**
	 * Ask the user is they are a customer or attendant
	 * 
	 * @return 0 if the user responds as a customer, 1 if they respond as an
	 *         attendant
	 */
	private static int promptForUserType() {
		String[] userTypes = { "Customer", "Attendant" };
		int answer = JOptionPane.showOptionDialog(null, "Are you a Customer or Attendant?",
				"User?", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, userTypes, -1);

		return answer;
	}

	/**
	 * 
	 * @return
	 */
	private int promptCustomerForCard() {
		String[] cardTypes = { "CREDIT", "DEBIT", "MEMBERSHIP", "GIFT CARD" };
		return JOptionPane.showOptionDialog(null, "Which card will you use?",
				"Card?", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, cardTypes, 0);
	}

	/**
	 * 
	 * @return
	 */
	public int promptNumOfBags() {
		String answer = JOptionPane.showInputDialog("How many bags used?");
		int ans;
		try {
			ans = Integer.valueOf(answer);
		} catch (NumberFormatException e) {
			errorMsg("Not a number");
			ans = promptNumOfBags();
		}

		return ans;
	}

	/**
	 * Prompts the attendant for a username and password.
	 * Used when the attendant approaches the attendant station
	 * and no one else is logged in.
	 * 
	 * @return true if username and password exist
	 */
	private boolean promptAttendantForLogIn() {
		Box box = Box.createVerticalBox();

		JLabel namePrompt = new JLabel("  Username");
		JTextField name = new JTextField(20);
		box.add(namePrompt);
		box.add(name);

		JLabel passwordPrompt = new JLabel("  Password");
		JPasswordField password = new JPasswordField(20);
		box.add(passwordPrompt);
		box.add(password);

		JOptionPane.showConfirmDialog(null, box, "Attendant Log in", JOptionPane.OK_CANCEL_OPTION);

		return GUI.attendantLogin(name.getText(), new String(password.getPassword()));
	}

	/**
	 * Prompts the attendant for a password.
	 * Attendant must already be logged in via the
	 * attendant station.
	 * Used when the user attempts to access attendant
	 * controls on the selfcheckout station or attendant
	 * goes back to the attendant station.
	 * 
	 * @return true if password matches logged in attendant
	 */
	private static boolean promptAttendantForPassword() {
		Box box = Box.createVerticalBox();

		JLabel passwordPrompt = new JLabel("  Password");
		JPasswordField password = new JPasswordField(20);
		box.add(passwordPrompt);
		box.add(password);

		JOptionPane.showConfirmDialog(null, box, "Credentials Required", JOptionPane.OK_CANCEL_OPTION);

		return GUI.attendantPassword(new String(password.getPassword()));
	}

	/**
	 * Initializes the scenes window size and attaches
	 * a JPanel with a Border Layout to use as a default
	 * canvas for the scenes components.
	 * 
	 * @param f - the window to initilize
	 * @param x - the windows x-axis size
	 * @param y - the windows y-axis size
	 * @return JPanel attached to provided window
	 */
	private JPanel preprocessScene(JFrame f, int x, int y) {
		f.setSize(x, y);
		f.setResizable(false);
		f.setUndecorated(true);
		f.setLocationRelativeTo(null);

		// Create a panel to take up the window and act as our canvas
		JPanel scene = new JPanel();
		scene.setLayout(new BorderLayout());

		// add the canvas to the frame
		f.add(scene);

		return scene;
	}

	/**
	 * Creates a banner at the top of the window for navigation
	 * and information display. Allows navigating between current
	 * users if the banner is not meant for hardware window.
	 * 
	 * @param p           - panel with BorderLayout
	 * @param forHardware - whether the banner is meant for a hardware window
	 * @return the banner created for any further customizations
	 */
	private JPanel generateBanner(JPanel p, boolean forHardware, JLabel banner_info, JLabel banner_title,
			String closeButton) {

		JPanel banner = new JPanel();
		banner.setPreferredSize(new Dimension(100, 50));
		banner.setLayout(new BorderLayout());
		banner.setBackground(Color.lightGray);

		JFrame window = (JFrame) SwingUtilities.getWindowAncestor(p);

		// only add navigation between users if the banner is not meant for hardware
		// windows
		if (!forHardware) {
			JPanel swap = new JPanel();
			swap.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
			swap.setOpaque(false);

			JButton prev = new JButton();
			prev.setPreferredSize(new Dimension(48, 48));
			prev.setFont(new Font("Arial", Font.BOLD, 18));
			prev.setText("<");
			prev.setFocusable(false);
			prev.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (currentStation != -1)
						window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
					GUI.selectPreviousUser();
				}
			});

			swap.add(prev);

			JButton newUser = new JButton();
			newUser.setPreferredSize(new Dimension(80, 48));
			newUser.setFont(new Font("Arial", Font.BOLD, 18));
			newUser.setText("NEW");
			newUser.setFocusable(false);
			newUser.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (currentStation != -1)
						window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
					currentStation = -1; // returned to the main overview scene
					int newUserType;
					do {
						newUserType = (promptForUserType() == 0) ? AppControl.CUSTOMER : AppControl.ATTENDANT;
					} while (!GUI.newUser(newUserType));
				}
			});
			swap.add(newUser);

			JButton next = new JButton();
			next.setPreferredSize(new Dimension(48, 48));
			next.setFont(new Font("Arial", Font.BOLD, 18));
			next.setText(">");
			next.setFocusable(false);
			next.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (getCurrentStation() != -1)
						window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
					GUI.selectNextUser();
				}
			});
			swap.add(next);

			banner.add(swap, BorderLayout.WEST);
		} else {
			// replace user navigation with an invisible panel to match spacing
			JPanel emptyPanel = new JPanel();
			emptyPanel.setBorder(BorderFactory.createEmptyBorder(200, 50, 200, 50));
			emptyPanel.setOpaque(false);
			banner.add(emptyPanel, BorderLayout.WEST);
		}

		// the centre display of the banner
		banner_info.setHorizontalAlignment(JLabel.CENTER);
		banner_info.setFont(new Font("Arial", Font.BOLD, 14));
		banner_info.setFocusable(false);
		banner.add(banner_info, BorderLayout.CENTER);

		// the end of the banner
		JPanel end = new JPanel();
		end.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		end.setOpaque(false);

		banner_title.setPreferredSize(new Dimension(180, 50));
		banner_title.setFont(new Font("Arial", Font.BOLD, 16));
		banner_title.setFocusable(false);
		end.add(banner_title, BorderLayout.CENTER);

		JButton exit = new JButton();
		exit.addActionListener(e -> {
			// only leave the station if this banner is neither attached to
			// the main overview scene or a hardware window
			if (getCurrentStation() != -1 && !forHardware) {
				GUI.userLeavesStation(getCurrentStation());
				setCurrentStation(-1);
			}
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		});
		exit.setPreferredSize(new Dimension(80, 50));
		exit.setFont(new Font("Arial", Font.BOLD, 16));
		exit.setText(closeButton);
		exit.setFocusable(false);
		end.add(exit);

		banner.add(end, BorderLayout.EAST);

		p.add(banner, BorderLayout.NORTH);
		return banner;
	}

	/**
	 * Resets the dimming filter to cover all currently displayed windows
	 */
	private static void setDimmingFilter() {
		filterFrame.setVisible(true);
	}

	/**
	 * Sets the properties for the JFrame that covers the background
	 * windows to pull focus towards the current JFrame in focus.
	 */
	private static void initDimmingFilter() {
		filterFrame.setSize(xResolution, yResolution);
		filterFrame.setResizable(false);
		filterFrame.setUndecorated(true);
		filterFrame.setLocationRelativeTo(null);

		JPanel filter = new JPanel();
		filter.setBackground(Color.black);
		filterFrame.add(filter);

		filterFrame.setOpacity((float) 0.75);
		filterFrame.setFocusableWindowState(false);
	}

	/**
	 * Creates a JPanel that attachs a provided background image to it.
	 * Code copied from:
	 * https://mathbits.com/JavaBitsNotebook/Graphics/InsertBackground.html
	 * 
	 * @author joshuaplosz
	 *
	 */
	private class BackgroundImage extends JPanel {
		Image img;

		public BackgroundImage(String imgPath) {
			ImageIcon icon = new ImageIcon(imgPath);
			img = icon.getImage();
			this.setBounds(0, 0, img.getWidth(null), img.getHeight(null));
			this.setOpaque(false);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(img, 0, 0, null);
		}
	}

	/**
	 * Displays the list of Products currently processed by this stations
	 * customer in a drop down menu.
	 * This method should only be used by a previously approved attendant.
	 */
	public void promptRemoveItems() {
		List<Product> customerCart = GUI.getBaggedItems(currentStation);
		if (customerCart == null) {
			errorMsg("No items have been bagged");
			return;
		}

		Vector<String> products = new Vector<>();
		customerCart.forEach(product -> {
			if (product instanceof PLUCodedProduct) {
				PLUCodedProduct pluProduct = (PLUCodedProduct) product;
				products.add(Inventory.getProduct(pluProduct.getPLUCode()).getDescription());
			} else if (product instanceof BarcodedProduct) {
				BarcodedProduct barProduct = (BarcodedProduct) product;
				products.add(Inventory.getProduct(barProduct.getBarcode()).getDescription());
			}
		});

		JFrame window = new JFrame();
		window.addWindowFocusListener(new WindowAdapter() {
			public void windowLostFocus(WindowEvent e) {
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			}
		});

		JPanel panel = preprocessScene(window, 200, 100);
		panel.setBackground(new Color(210, 207, 210));
		panel.setLayout(null);

		JComboBox<String> dropMenu = new JComboBox<>(products);
		dropMenu.setBounds(40, 60, 120, 30);
		dropMenu.setFont(new Font("Arial", Font.PLAIN, 14));
		panel.add(dropMenu);

		JButton remove = new JButton();
		remove.setBounds(40, 20, 120, 30);
		remove.setHorizontalAlignment(JButton.CENTER);
		remove.setFont(new Font("Arial", Font.PLAIN, 12));
		remove.setText("REMOVE");
		remove.setFocusable(false);
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUI.removeItem(currentStation, dropMenu.getSelectedIndex());
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			}
		});

		panel.add(remove);

		window.add(panel);
		window.setVisible(true);
	}

	/**
	 * Displays the list of PLU coded items to the user in a drop down menu,
	 * allows them to select one for processing its input
	 */
	public void promptSelectItems() {
		ArrayList<PLUCodedProduct> pluItems = new ArrayList<>(Inventory.getPLUProducts().values());
		Vector<String> items = new Vector<>();
		pluItems.forEach(item -> items.add(item.getDescription()));

		JFrame window = new JFrame();
		window.addWindowFocusListener(new WindowAdapter() {
			public void windowLostFocus(WindowEvent e) {
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			}
		});

		JPanel panel = preprocessScene(window, 250, 150);
		panel.setBackground(new Color(210, 207, 210));
		panel.setLayout(null);

		JComboBox<String> dropMenu = new JComboBox<>(items);
		dropMenu.setBounds(55, 80, 140, 40);
		dropMenu.setFont(new Font("Arial", Font.PLAIN, 16));
		panel.add(dropMenu);

		JButton select = new JButton();
		select.setBounds(55, 20, 140, 40);
		select.setHorizontalAlignment(JButton.CENTER);
		select.setFont(new Font("Arial", Font.BOLD, 16));
		select.setText("SELECT");
		select.setFocusable(false);
		select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUI.selectedItem(pluItems.get(dropMenu.getSelectedIndex()));
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			}
		});

		panel.add(select);

		window.add(panel);
		window.setVisible(true);
	}

	/**
	 * Displays a message to the user in the form of an error.
	 * Requires interaction to dismiss.
	 * 
	 * @param msg - message to display with the error
	 */
	public static void errorMsg(String msg) {
		JOptionPane.showMessageDialog(null, msg, null, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * When a Keypad object is created for number input it
	 * calls this method to handle what the number is for.
	 * 
	 * @param number
	 */
	public void keypadReturnValue(int number) {
		if (expectingPLUCode) {
			GUI.userEntersPLUCode(number, currentStation);
			expectingPLUCode = false;
		} else if (expectingMembershipNum) {
			GUI.userEntersMembership(number);
			expectingMembershipNum = false;
		} else if (expectingCreditPIN) {
			GUI.userInsertCard(AppControl.CREDIT, String.valueOf(number));
			expectingCreditPIN = false;
		} else if (expectingDebitPIN) {
			GUI.userInsertCard(AppControl.DEBIT, String.valueOf(number));
			expectingDebitPIN = false;
		}
	}

	public void coinWalletReturnValue(BigDecimal value) {
		GUI.userInsertsCoin(currentStation, value);
	}

	public void banknoteWalletReturnValue(int value) {
		GUI.userInsertsBanknote(currentStation, value);
	}
}
