package GUI;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.SwingConstants;

public class Keypad {

	private JFrame frame;
	private JTextPane display;
	private String value;
	private String msg;
	private Scenes parent;

	/**
	 * Create the application.
	 */
	public Keypad(String msg, Scenes parent) {
		this.msg = msg;
		this.parent = parent;
		this.value = "";
		initialize();
		frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		int window_width = 300;
		int window_height = 500;
		
		int numpadDimension = 85;
		int spaceBetweenBtns = 5;
		
		int numpadTotalWidth = numpadDimension * 3 + spaceBetweenBtns * 2; 
		int numpadDisplacementX = (window_width - numpadTotalWidth) / 2;
		
		int numpadTotalHeight = numpadDimension * 4 + spaceBetweenBtns * 3;
		int numpadDisplacementY = (int) ((window_height * 1.25 - numpadTotalHeight) / 2);
		
		ArrayList<JButton> numpadsBtn = new ArrayList<JButton>();
		
		frame = new JFrame();
		frame.setSize(window_width, window_height);
		frame.setUndecorated(true);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(null);
		
		int textPaneWidth = 180;
		display = new JTextPane();
		display.setEditable(true);
		display.setFont(new Font("Lucida Grande", Font.PLAIN, 21));
		display.setBounds(window_width/2 - textPaneWidth/2, 90, textPaneWidth, 31);
		frame.getContentPane().add(display);
		
		JLabel lblNewLabel = new JLabel(msg);
		lblNewLabel.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(window_width/2 - textPaneWidth/2, 41, textPaneWidth, 37);
		frame.getContentPane().add(lblNewLabel);

		int button_text = 1;
		for (int j = 2; j >= 0; j--) {
			for (int i = 0; i < 3; i++) {
				
				JButton btnNumpad = createNumpadButton(numpadDisplacementX + numpadDimension * i + spaceBetweenBtns * i, 
						numpadDisplacementY + numpadDimension * j, 
						numpadDimension, Integer.toString(button_text));
				numpadsBtn.add(btnNumpad);
				button_text++;
			}
		}
		
		JButton btnNumpad0 = createNumpadButton(numpadDisplacementX + numpadDimension + spaceBetweenBtns, 
				numpadDisplacementY + numpadDimension * 3, 
				numpadDimension, "0");
		numpadsBtn.add(btnNumpad0);
		
		
		JButton btnClear = new JButton("DELETE");
		btnClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (value.length() > 0)
					value = "";
				display.setText(value);
			}
		});
		btnClear.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		btnClear.setBounds(numpadDisplacementX + numpadDimension * 0 + spaceBetweenBtns * 0, numpadDisplacementY + numpadDimension * 3, numpadDimension, numpadDimension);
		frame.getContentPane().add(btnClear);

		
		JButton btnEnter = new JButton("ENTER");
		btnEnter.addActionListener(e -> {
			if (value.length() > 0) 
				parent.keypadReturnValue(Integer.valueOf(value));
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		});
		btnEnter.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		btnEnter.setBounds(numpadDisplacementX + numpadDimension * 2 + spaceBetweenBtns * 2, numpadDisplacementY + numpadDimension * 3, numpadDimension, numpadDimension);
		frame.getContentPane().add(btnEnter);
		

	}
	
	private JButton createNumpadButton(int x, int y, int dimension, String num) {
		JButton btn = new JButton(num);
		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				value += num;
				display.setText(value);
			}
		});
		btn.setFont(new Font("Lucida Grande", Font.PLAIN, 26));
		btn.setBounds(x, y, dimension, dimension);
		frame.getContentPane().add(btn);
		return btn;
	}
}