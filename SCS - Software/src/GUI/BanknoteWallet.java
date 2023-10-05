package GUI;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BanknoteWallet extends JFrame implements MouseListener {

	private final JPanel contentPanel = new JPanel();
	JFrame window;
	JLabel fiveDollar;
	JLabel tenDollar;
	JLabel twentyDollar;
	JLabel fiftyDollar;
	JLabel hundredDollar;
	Scenes parent;
	
	/**
	 * Create the dialog.
	 */
	public BanknoteWallet(Scenes parent) {
		this.parent = parent;
		window = this;
		
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		setResizable(false);
		setBounds(100, 100, 268, 450);
		setUndecorated(true);
		setLayout(new BorderLayout());
		add(contentPanel, BorderLayout.CENTER);
		setLocationRelativeTo(null);
		
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(null);
		{
			JLabel lblNewLabel = new JLabel("Banknote Wallet");
			lblNewLabel.setBounds(82, 6, 105, 16);
			contentPanel.add(lblNewLabel);
		}
		{
			fiveDollar = new JLabel("five");
			ImageIcon five = new ImageIcon("src/GUI/images/fiveDollar.jpg");
			fiveDollar.setIcon(five);
			fiveDollar.setBounds(46, 44, 179, 64);
			fiveDollar.addMouseListener(this);
			contentPanel.add(fiveDollar);
		}
		{
			tenDollar = new JLabel("ten");
			ImageIcon ten = new ImageIcon("src/GUI/images/tenDollar.jpg");
			tenDollar.setIcon(ten);
			tenDollar.setBounds(46, 120, 179, 64);
			tenDollar.addMouseListener(this);
			contentPanel.add(tenDollar);
		}
		{
			twentyDollar = new JLabel("twenty");
			ImageIcon twenty = new ImageIcon("src/GUI/images/twentyDollar.jpg");
			twentyDollar.setIcon(twenty);
			twentyDollar.setBounds(46, 196, 179, 64);
			twentyDollar.addMouseListener(this);
			contentPanel.add(twentyDollar);
		}
		{
			fiftyDollar = new JLabel("fifty");
			ImageIcon fifty = new ImageIcon("src/GUI/images/fiftyDollar.jpg");
			fiftyDollar.setIcon(fifty);
			fiftyDollar.setBounds(46, 272, 179, 64);
			fiftyDollar.addMouseListener(this);
			contentPanel.add(fiftyDollar);
		}
		{
			hundredDollar = new JLabel("hundred");
			ImageIcon hundred = new ImageIcon("src/GUI/images/hundredDollar.jpg");
			hundredDollar.setIcon(hundred);
			hundredDollar.setBounds(46, 348, 179, 64);
			hundredDollar.addMouseListener(this);
			contentPanel.add(hundredDollar);
		}
		
		setVisible(true);
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == fiveDollar) {
			parent.banknoteWalletReturnValue(5);
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		} else if (e.getSource() == tenDollar) {
			parent.banknoteWalletReturnValue(10);
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		} else if (e.getSource() == twentyDollar) {
			parent.banknoteWalletReturnValue(20);
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		} else if (e.getSource() == fiftyDollar) {
			parent.banknoteWalletReturnValue(50);
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		} else if (e.getSource() == hundredDollar) {
			parent.banknoteWalletReturnValue(100);
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}
