package GUI;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class CoinWallet extends JFrame implements MouseListener {

	private final JPanel contentPanel = new JPanel();
	JFrame window;
	JLabel nickelLabel;
	JLabel lblDime;
	JLabel lblQuarter;
	JLabel lblLoonie;
	JLabel lblToonie;
	Scenes parent;

	/**
	 * Create the dialog.
	 */
	public CoinWallet(Scenes parent) {
		this.parent = parent;
		window = this;
		
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		setResizable(false);
		setBounds(100, 100, 135, 520);
		setUndecorated(true);
		setLayout(new BorderLayout());
		add(contentPanel, BorderLayout.CENTER);
		setLocationRelativeTo(null);
		
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(null);
		{
			nickelLabel = new JLabel("nickel");
			ImageIcon nickel = new ImageIcon("src/GUI/images/nickel.png");
			nickelLabel.setIcon(nickel);
			nickelLabel.setBounds(25, 33, 84, 84);
			nickelLabel.addMouseListener(this);
			contentPanel.add(nickelLabel);
		}
		{
			JLabel lblNewLabel_1 = new JLabel("Coin Wallet");
			lblNewLabel_1.setBounds(31, 6, 91, 16);
			contentPanel.add(lblNewLabel_1);
		}
		{
			lblDime = new JLabel("dime");
			ImageIcon dime = new ImageIcon("src/GUI/images/dime.png");
			lblDime.setIcon(dime);
			lblDime.setBounds(25, 126, 84, 84);
			lblDime.addMouseListener(this);
			contentPanel.add(lblDime);
		}
		{
			lblQuarter = new JLabel("quarter");
			ImageIcon quarter = new ImageIcon("src/GUI/images/quarter.png");
			lblQuarter.setIcon(quarter);
			lblQuarter.setBounds(25, 219, 84, 84);
			lblQuarter.addMouseListener(this);
			contentPanel.add(lblQuarter);
		}
		{
			lblLoonie = new JLabel("loonie");
			ImageIcon loonie = new ImageIcon("src/GUI/images/loonie.png");
			lblLoonie.setIcon(loonie);
			lblLoonie.setBounds(25, 317, 84, 84);
			lblLoonie.addMouseListener(this);
			contentPanel.add(lblLoonie);
		}
		{
			lblToonie = new JLabel("toonie");
			ImageIcon toonie = new ImageIcon("src/GUI/images/toonie.png");
			lblToonie.setIcon(toonie);
			lblToonie.setBounds(25, 422, 84, 84);
			lblToonie.addMouseListener(this);
			contentPanel.add(lblToonie);
		}

		setVisible(true);
	}

	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == nickelLabel) {
			parent.coinWalletReturnValue(new BigDecimal("0.05"));
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		} else if (e.getSource() == lblDime) {
			parent.coinWalletReturnValue(new BigDecimal("0.10"));
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		} else if (e.getSource() == lblQuarter) {
			parent.coinWalletReturnValue(new BigDecimal("0.25"));
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		} else if (e.getSource() == lblLoonie) {
			parent.coinWalletReturnValue(new BigDecimal("1.00"));
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		} else if (e.getSource() == lblToonie) {
			parent.coinWalletReturnValue(new BigDecimal("2.00"));
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

}
