package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class AppChooser extends JFrame{
	private static final long serialVersionUID = -3361773067589558389L;
	
	JButton quitButton = new JButton("quit");
	JButton startMainCalculator = new JButton("start main calculator");
	JButton startDrawingBoard = new JButton("start drawing board");
	JButton startTriangleSolver = new JButton("start triangle solver");
	
	public AppChooser() {
		super("choose Application");
		UI.WINDOW_COUNT++;
		
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.setSize(300, 300);
		this.setLocationRelativeTo(null);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		mainPanel.add(quitButton,BorderLayout.PAGE_END);
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {System.exit(0);}
		});
		
		JPanel appChoicePanel = new JPanel();
		appChoicePanel.setLayout(new FlowLayout());
		
		appChoicePanel.add(startMainCalculator);
		startMainCalculator.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CalcWindow.loadCasWindow();
			}
		});
		appChoicePanel.add(startDrawingBoard);
		startDrawingBoard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DrawingBoard();
			}
		});
		appChoicePanel.add(startTriangleSolver);
		startTriangleSolver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new TriangleSolver();
			}
		});
		
		mainPanel.add(appChoicePanel,BorderLayout.CENTER);
		
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {}

			@Override
			public void windowClosing(WindowEvent e) {
				UI.WINDOW_COUNT--;
				AppChooser.this.dispose();
			}

			@Override
			public void windowDeactivated(WindowEvent e) {}

			@Override
			public void windowDeiconified(WindowEvent e) {}

			@Override
			public void windowIconified(WindowEvent e) {}

			@Override
			public void windowOpened(WindowEvent e) {}
		});
		
		this.add(mainPanel);
		this.setResizable(false);
		this.setVisible(true);
	}
}
