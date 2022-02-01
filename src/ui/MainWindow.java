package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import graphics.Plot;

class MainWindow{
	final int WINDOW_WIDTH = 900,WINDOW_HEIGHT = 500;
	Color background,foreground;
	StackEditor currentStack;
	Font font;
	final int MAX_CHARS = 1024;
	
	static final boolean CLEAR_DEFAULT = true;
	boolean clearTerminal = CLEAR_DEFAULT;
	
	void setColors() {
		background = new Color(223,218,196);
		foreground = new Color(75,47,46);
	}
	
	JPanel createTerminal(JFrame window,Plot plot) {
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
		
		JTextArea workArea = new JTextArea("work area",5,100);
		workArea.setBackground(background);
		workArea.setForeground(foreground);
		workArea.setLineWrap(true);
		workArea.setFont(font);
		
		JScrollPane scrollWorkArea = new JScrollPane(workArea);
		
		JTextField terminalIn = new JTextField("type here");
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
				
				plot.repaint();
			}
			
		});
		terminalIn.setBackground(background);
		terminalIn.setForeground(foreground);
		terminalIn.setCaretColor(foreground);
		terminalIn.setFont(font);
		
		JSplitPane terminalOutAndWorkArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollWorkArea,scrollableTerminalOut);
		
		terminal.add(terminalOutAndWorkArea,BorderLayout.CENTER);
		terminal.add(terminalIn,BorderLayout.SOUTH);
		return terminal;
	}
	
	JPanel createOptionsMenu(JComponent bottomPanel,JPanel terminal,Plot plot) {
		Font smallFont = new Font("courier", Font.BOLD, 8);
		JCheckBox clearTerminalCheckBox = new JCheckBox("clear terminal",CLEAR_DEFAULT);
		clearTerminalCheckBox.setFont(smallFont);
		clearTerminalCheckBox.setBackground(background);
		JCheckBox showPlotCheckBox = new JCheckBox("show plot",true);
		showPlotCheckBox.setFont(smallFont);
		showPlotCheckBox.setBackground(background);
		JComboBox<String> plotMode = new JComboBox<String>(new String[] {"2D","3D"});
		plotMode.setBackground(background);
		plotMode.setFont(smallFont);
		
		JButton getWindow = new JButton("get window");
		getWindow.setFont(smallFont);
		getWindow.setBackground(background);
		
		JButton setWindow = new JButton("set window");
		setWindow.setFont(smallFont);
		setWindow.setBackground(background);
		
		JButton reset = new JButton("reset");
		reset.setFont(smallFont);
		reset.setBackground(background);
		
		int chars = 6;
		
		JLabel xMinLabel = new JLabel("x-Min");
		JTextField xMin = new JTextField("-10.0",chars);
		
		JLabel xMaxLabel = new JLabel("x-Max");
		JTextField xMax = new JTextField("10.0",chars);
		
		JLabel yMinLabel = new JLabel("y-Min");
		JTextField yMin = new JTextField("-10.0",chars);
		
		JLabel yMaxLabel = new JLabel("y-Max");
		JTextField yMax = new JTextField("10.0",chars);
		
		JLabel zMinLabel = new JLabel("z-Min");
		JTextField zMin = new JTextField("-10.0",chars);
		
		JLabel zMaxLabel = new JLabel("z-Max");
		JTextField zMax = new JTextField("10.0",chars);
		
		JComponent[] domainControl = new JComponent[]{xMinLabel,xMin,xMaxLabel,xMax,yMinLabel,yMin,yMaxLabel,yMax,zMinLabel,zMin,zMaxLabel,zMax};
		
		for(JComponent c:domainControl) {
			c.setFont(smallFont);
			c.setBackground(background);
		}
		
		setWindow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plot.plotParams.set(Double.parseDouble(xMin.getText()),Double.parseDouble(xMax.getText()),Double.parseDouble(yMin.getText()),Double.parseDouble(yMax.getText()),Double.parseDouble(zMin.getText()),Double.parseDouble(zMax.getText()));
				plot.repaint();
			}
		});
		
		getWindow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xMin.setText(Double.toString(plot.plotParams.xMin));
				xMin.setCaretPosition(0);
				xMax.setText(Double.toString(plot.plotParams.xMax));
				xMax.setCaretPosition(0);
				
				yMin.setText(Double.toString(plot.plotParams.yMin));
				yMin.setCaretPosition(0);
				yMax.setText(Double.toString(plot.plotParams.yMax));
				yMax.setCaretPosition(0);
				
				zMin.setText(Double.toString(plot.plotParams.zMin));
				zMin.setCaretPosition(0);
				zMax.setText(Double.toString(plot.plotParams.zMax));
				zMax.setCaretPosition(0);
			}
		});
		
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plot.plotParams.xMin = -10;
				plot.plotParams.xMax = 10;
				
				plot.plotParams.yMin = -10;
				plot.plotParams.yMax = 10;
				
				plot.plotParams.zMin = -10;
				plot.plotParams.zMax = 10;
				
				plot.repaint();
			}
		});
		
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
					plot.setVisible(true);
					bottomPanel.removeAll();
					bottomPanel.add(createSplitView(terminal,plot));
				}else {
					plot.setVisible(false);
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
				plot.repaint();
			}
			
		});
		
		JPanel options = new JPanel();
		options.setBackground(background);
		options.setLayout(new FlowLayout(FlowLayout.LEFT));
		options.add(clearTerminalCheckBox);
		options.add(showPlotCheckBox);
		options.add(plotMode);
		
		for(JComponent c:domainControl) {
			options.add(c);
		}
		options.add(getWindow);
		options.add(setWindow);
		options.add(reset);
		
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
		Plot plot = new Plot(currentStack);
		JPanel terminal = createTerminal(window,plot);
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
		try {
			UIManager.setLookAndFeel(
			        UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currentStack = new StackEditor();
		setColors();
		font = new Font("courier", Font.BOLD, 12);
		
		createWindow();
	}

}
