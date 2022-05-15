package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class AppChooser extends JFrame{
	private static final long serialVersionUID = -3361773067589558389L;
	
	JPanel mainPanel = new JPanel();
	JButton quitButton = new JButton("quit");
	JButton startMainCalculator = new JButton("start main calculator");
	
	public AppChooser() {
		super("choose Application");
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.setSize(300, 300);
		this.setLocationRelativeTo(null);
		this.add(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		
		mainPanel.add(quitButton,BorderLayout.PAGE_END);
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {System.exit(0);}
		});
		
		mainPanel.add(startMainCalculator,BorderLayout.PAGE_START);
		startMainCalculator.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new CalcWindow();
			}
		});
		
		this.setVisible(true);
	}
}
