package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.LineBorder;

import graphics.Plot;

class MainWindow{

	public MainWindow(){
		int WINDOW_WIDTH = 800,WINDOW_HEIGHT = 400;
		
		JFrame window = new JFrame("Ben's Tool Box / BitLogic ");
		Color background = new Color(223,218,196);
		Color foreground = new Color(75,47,46);
		Font font = new Font("courier", Font.BOLD, 12);
		int MAX_CHARS = 1024;
		
		JCheckBox clearTerm = new JCheckBox("clear terminal");
		
		StackEditor currentStackEditor = new StackEditor();
		window.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		window.setMinimumSize(new Dimension(600,200));
		
		JPanel terminal = new JPanel();
		
		terminal.setLayout(new BorderLayout());
		JTextArea terminalOut = new JTextArea();
		terminalOut.setBackground(background);
		terminalOut.setForeground(foreground);
		terminalOut.setLineWrap(true);
		terminalOut.setEditable(false);
		terminalOut.setFont(font);
		terminalOut.setText(UI.fancyIntro());
		JScrollPane scrollableTerminalOut = new JScrollPane(terminalOut);
		scrollableTerminalOut.setMinimumSize(new Dimension(300,200));
		
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
				
				String newState = "";
				if(clearTerm.isSelected()) {
					newState = currentStackEditor.getStackAsString();
				}else {
					newState = terminalOut.getText()+currentStackEditor.getStackAsString();
					newState = newState.substring(Math.max(0,newState.length()-MAX_CHARS));
				}
				
				terminalOut.setText(newState);
				terminalOut.setCaretPosition(terminalOut.getText().length());
			}
			
		});
		terminalIn.setBackground(background);
		terminalIn.setForeground(foreground);
		terminalIn.setCaretColor(foreground);
		terminalIn.setBorder(new LineBorder(foreground,4));
		terminalIn.setFont(font);
		
		terminal.add(scrollableTerminalOut,BorderLayout.CENTER);
		terminal.add(terminalIn,BorderLayout.SOUTH);
		
		Plot plot = new Plot(currentStackEditor);
		plot.setBackgroundColor(background);
		plot.setForegroundColor(foreground);
		JPanel plotJPanel = plot.getJPanel();
		terminal.add(plotJPanel,BorderLayout.EAST);
		
		JSplitPane splitView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,terminal,plotJPanel);
		
		JPanel options = new JPanel();
		options.setBackground(background);
		options.setLayout(new FlowLayout(FlowLayout.LEFT));
		options.add(clearTerm);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(splitView,BorderLayout.CENTER);
		mainPanel.add(options,BorderLayout.NORTH);
		
		window.add(mainPanel);
		splitView.setDividerLocation(WINDOW_WIDTH/2);
		window.setAlwaysOnTop(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

}
