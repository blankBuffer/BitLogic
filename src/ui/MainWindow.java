package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.LineBorder;

class MainWindow{

	public MainWindow(){
		JFrame window = new JFrame("Ben's Tool Box / BitLogic ");
		Color background = new Color(32,16,0);
		Color foreground = new Color(200,100,64);
		Font font = new Font("courier", Font.BOLD, 20);
		
		StackEditor currentStackEditor = new StackEditor();
		window.setSize(800,300);
		
		JPanel mainPanel = new JPanel();
		
		mainPanel.setLayout(new BorderLayout());
		JTextArea terminalOut = new JTextArea();
		terminalOut.setBackground(background);
		terminalOut.setForeground(foreground);
		terminalOut.setLineWrap(true);
		terminalOut.setEditable(false);
		terminalOut.setFont(font);
		terminalOut.setText(UI.fancyIntro());
		
		JTextField terminalIn = new JTextField();
		terminalIn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = terminalIn.getText();
				int quit = currentStackEditor.command(command);
				if(quit == -1){
					window.dispose();
					return;
				}
				terminalIn.setText("");
				terminalOut.setText(currentStackEditor.getStackAsString());
			}
			
		});
		terminalIn.setBackground(background);
		terminalIn.setForeground(foreground);
		terminalIn.setCaretColor(foreground);
		terminalIn.setBorder(new LineBorder(foreground,1));
		terminalIn.setFont(font);
		
		mainPanel.add(terminalOut,BorderLayout.CENTER);
		mainPanel.add(terminalIn,BorderLayout.SOUTH);
		window.add(mainPanel);
		window.setAlwaysOnTop(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

}
