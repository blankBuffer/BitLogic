package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

import graphics.Plot;

class MainWindow{
	final int WINDOW_WIDTH = 900,WINDOW_HEIGHT = 500;
	//Color background,foreground;
	StackEditor currentStack;
	final int MAX_CHARS = 1024;
	
	static final boolean CLEAR_DEFAULT = true;
	boolean clearTerminal = CLEAR_DEFAULT;
	
	
	JPanel createTerminal(JFrame window,Plot plot,ArrayList<JComponent> allComponents) {
		Font font = new Font("Courier",0,12);
		JPanel terminal = new JPanel();
		terminal.setLayout(new BorderLayout());
		JTextArea terminalOut = new JTextArea();
		allComponents.add(terminalOut);
		terminalOut.setLineWrap(true);
		terminalOut.setEditable(false);
		terminalOut.setFont(font);
		terminalOut.setText(UI.fancyIntro());
		JScrollPane scrollableTerminalOut = new JScrollPane(terminalOut);
		scrollableTerminalOut.setMinimumSize(new Dimension(300,200));
		
		JTextArea workArea = new JTextArea("work area",5,100);
		allComponents.add(workArea);
		workArea.setLineWrap(true);
		workArea.setFont(font);
		
		JScrollPane scrollWorkArea = new JScrollPane(workArea);
		
		JTextField terminalIn = new JTextField("type here");
		
		ActionListener terminalOutUpdate = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				
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
			
		};
		
		ActionListener terminalInPush = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = terminalIn.getText();
				int quit = currentStack.command(command);
				if(quit == -1) {
					window.dispose();
				}
				terminalIn.setText("");
				terminalOutUpdate.actionPerformed(e);
			}
		};
		
		
		terminalIn.addActionListener(terminalInPush);
		allComponents.add(terminalIn);
		
		JButton resultButton = new JButton("result");
		allComponents.add(resultButton);
		resultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStack.command("result");
				terminalOutUpdate.actionPerformed(null);
			}
		});
		
		JButton pushButton = new JButton("push");
		allComponents.add(pushButton);
		
		pushButton.addActionListener(terminalInPush);
		
		
		JSplitPane terminalOutAndWorkArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollWorkArea,scrollableTerminalOut);
		
		terminal.add(terminalOutAndWorkArea,BorderLayout.CENTER);
		
		
		JPanel terminalInWithButton = new JPanel();
		
		terminalInWithButton.setLayout(new BorderLayout());
		terminalInWithButton.add(terminalIn,BorderLayout.CENTER);
		
		JPanel terminalInButtons = new JPanel();
		terminalInButtons.setLayout(new FlowLayout());
		terminalInButtons.add(resultButton);
		terminalInButtons.add(pushButton);
		
		terminalInWithButton.add(terminalInButtons,BorderLayout.EAST);
		
		terminal.add(terminalInWithButton,BorderLayout.SOUTH);
		return terminal;
	}
	
	JPanel createSettingsMenu(JFrame window,ArrayList<JComponent> allComponents) {
		JPanel settingsPanel = new JPanel();
		allComponents.add(settingsPanel);
		
		JCheckBox keepOnTop = new JCheckBox("on top",false);
		allComponents.add(keepOnTop);
		keepOnTop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.setAlwaysOnTop(keepOnTop.isSelected());
			}
		});
		settingsPanel.add(keepOnTop);
		
		JCheckBox clearTerminalCheckBox = new JCheckBox("clear terminal",CLEAR_DEFAULT);
		allComponents.add(clearTerminalCheckBox);
		clearTerminalCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearTerminal = clearTerminalCheckBox.isSelected();
			}
			
		});
		settingsPanel.add(clearTerminalCheckBox);
		
		JPanel themeSetter = new JPanel();
		JLabel backgroundLabel = new JLabel("background");
		JTextField br = new JTextField("255",3),bg = new JTextField("255",3),bb = new JTextField("255",3);
		themeSetter.add(backgroundLabel);
		themeSetter.add(br);
		themeSetter.add(bg);
		themeSetter.add(bb);
		JLabel foregroundLabel = new JLabel("foreground");
		JTextField fr = new JTextField("0",3),fg = new JTextField("0",3),fb = new JTextField("0",3);
		themeSetter.add(foregroundLabel);
		themeSetter.add(fr);
		themeSetter.add(fg);
		themeSetter.add(fb);
		
		JButton setTheme = new JButton("set theme");
		setTheme.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color background = new Color(Integer.parseInt(br.getText()),Integer.parseInt(bg.getText()),Integer.parseInt(bb.getText()));
				Color foreground = new Color(Integer.parseInt(fr.getText()),Integer.parseInt(fg.getText()),Integer.parseInt(fb.getText()));
				
				for(JComponent c:allComponents) {
					c.setBackground(background);
					c.setForeground(foreground);
				}
			}
		});
		themeSetter.add(setTheme);
		
		settingsPanel.add(themeSetter);
		
		return settingsPanel;
	}
	
	JPanel createGraphicsMenu(JComponent bottomPanel,JPanel terminal,Plot plot,ArrayList<JComponent> allComponents) {
		JCheckBox showPlotCheckBox = new JCheckBox("show plot",true);
		allComponents.add(showPlotCheckBox);
		JComboBox<String> plotMode = new JComboBox<String>(new String[] {"2D","3D"});
		allComponents.add(plotMode);
		
		JButton getWindow = new JButton("get window");
		allComponents.add(getWindow);
		
		JButton setWindow = new JButton("set window");
		allComponents.add(setWindow);
		
		JButton reset = new JButton("reset");
		allComponents.add(reset);
		
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
			allComponents.add(c);
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
		
		JPanel graphicsOptions = new JPanel();
		
		allComponents.add(graphicsOptions);
		graphicsOptions.setLayout(new FlowLayout());
		graphicsOptions.add(showPlotCheckBox);
		graphicsOptions.add(plotMode);
		
		JPanel windowParams = new JPanel();
		allComponents.add(windowParams);
		windowParams.setLayout(new FlowLayout());
		for(JComponent c:domainControl) {
			windowParams.add(c);
		}
		graphicsOptions.add(windowParams);
		graphicsOptions.add(getWindow);
		graphicsOptions.add(setWindow);
		graphicsOptions.add(reset);
		
		return graphicsOptions;
	}
	
	JComponent createSplitView(JPanel terminal,JPanel plot) {
		JSplitPane splitView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,terminal,plot);
		splitView.setDividerLocation(WINDOW_WIDTH/2);
		return splitView;
	}
	
	JPanel createMainPanel(JFrame window) {
		ArrayList<JComponent> allComponents = new ArrayList<JComponent>();
		
		JTabbedPane tabs = new JTabbedPane();
		allComponents.add(tabs);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		allComponents.add(mainPanel);
		mainPanel.add(tabs);
		Plot plot = new Plot(currentStack);
		allComponents.add(plot);
		JPanel terminal = createTerminal(window,plot,allComponents);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(createSplitView(terminal,plot));
		tabs.addTab("main view", bottomPanel);
		tabs.addTab("graphics", createGraphicsMenu(bottomPanel,terminal,plot,allComponents));
		tabs.addTab("settings", createSettingsMenu(window,allComponents));
		
		return mainPanel;
	}
	
	void createWindow() {
		JFrame window = new JFrame("BitLogic");
		window.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		window.setMinimumSize(new Dimension(600,200));
		window.add(createMainPanel(window));
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
		
		createWindow();
	}

}
