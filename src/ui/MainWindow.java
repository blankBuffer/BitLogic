package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.LineBorder;

import graphics.Plot;

class MainWindow{
	final int WINDOW_WIDTH = 800,WINDOW_HEIGHT = 400;
	Color background,foreground;
	StackEditor currentStack;
	Font font;
	final int MAX_CHARS = 1024;
	
	boolean clearTerminal = false;
	
	void setColors() {
		background = new Color(223,218,196);
		foreground = new Color(75,47,46);
	}
	
	JPanel createTerminal(JFrame window) {
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
				int quit = currentStack.command(command);
				if(quit == -1) {
					window.dispose();
				}
				
				terminalIn.setText("");
				
				String newState = "";
				if(clearTerminal) {
					newState = currentStack.getStackAsString();
				}else {
					newState = terminalOut.getText()+currentStack.getStackAsString();
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
		return terminal;
	}
	
	JPanel createOptionsMenu(JComponent bottomPanel,JPanel terminal,Plot plot) {
		JCheckBox clearTerminalCheckBox = new JCheckBox("clear terminal");
		JCheckBox showPlotCheckBox = new JCheckBox("show plot",true);
		JComboBox<String> plotMode = new JComboBox<String>(new String[] {"2D","3D"});
		
		clearTerminalCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearTerminal = clearTerminalCheckBox.isSelected();
			}
			
		});
		showPlotCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(showPlotCheckBox.isSelected()) {
					bottomPanel.removeAll();
					bottomPanel.add(createSplitView(terminal,plot));
				}else {
					bottomPanel.removeAll();
					bottomPanel.add(terminal);
				}
				
				bottomPanel.updateUI();
			}
			
		});
		
		plotMode.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotMode.getSelectedItem().equals("2D")) {
					plot.mode = Plot.MODE_2D;
				}else if(plotMode.getSelectedItem().equals("3D")) {
					plot.mode = Plot.MODE_3D;
				}
			}
			
		});
		
		JPanel options = new JPanel();
		options.setBackground(background);
		options.setLayout(new FlowLayout(FlowLayout.LEFT));
		options.add(clearTerminalCheckBox);
		options.add(showPlotCheckBox);
		options.add(plotMode);
		return options;
	}
	
	JComponent createSplitView(JPanel terminal,JPanel plot) {
		JSplitPane splitView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,terminal,plot);
		splitView.setDividerLocation(WINDOW_WIDTH/2);
		return splitView;
	}
	
	JPanel createMainPanel(JFrame window) {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		JPanel terminal = createTerminal(window);
		Plot plot = new Plot(currentStack);
		plot.setBackgroundColor(background);
		plot.setForegroundColor(foreground);
		bottomPanel.add(createSplitView(terminal,plot));
		mainPanel.add(bottomPanel,BorderLayout.CENTER);
		mainPanel.add(createOptionsMenu(bottomPanel,terminal,plot),BorderLayout.NORTH);
		return mainPanel;
	}
	
	void createWindow() {
		JFrame window = new JFrame("BitLogic");
		window.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		window.setMinimumSize(new Dimension(600,200));
		window.add(createMainPanel(window));
		window.setAlwaysOnTop(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	public MainWindow(){
		currentStack = new StackEditor();
		setColors();
		font = new Font("courier", Font.BOLD, 12);
		
		createWindow();
	}

}
