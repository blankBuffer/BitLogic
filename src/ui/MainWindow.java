package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.*;

import cas.Expr;
import cas.ExprRender;
import graphics.Plot;

public class MainWindow{
	final int WINDOW_WIDTH = 900,WINDOW_HEIGHT = 500;
	//Color background,foreground;
	StackEditor currentStack;
	final int MAX_CHARS = 1024;
	
	boolean clearTerminal = true;
	boolean keepWindowOnTop = false;
	boolean showPlot = true;
	static final int _2D = 0,_3D = 1;
	int plotMode = _2D;
	Color background = new Color(255,255,255),foreground = new Color(0,0,0);
	Font font = new Font("Courier",0,12);
	
	JPanel createTerminal(JFrame window,Plot plot,ArrayList<JComponent> allComponents) {
		JPanel terminal = new JPanel();
		terminal.setLayout(new BorderLayout());
		JTextArea terminalOut = new JTextArea();
		allComponents.add(terminalOut);
		terminalOut.setLineWrap(true);
		terminalOut.setEditable(false);
		terminalOut.setText(UI.fancyIntro());
		JScrollPane scrollableTerminalOut = new JScrollPane(terminalOut);
		scrollableTerminalOut.setMinimumSize(new Dimension(300,200));
		
		
		JTextField terminalIn = new JTextField();
		
		JPanel imgExprPanel = new JPanel() {
			private static final long serialVersionUID = 5476956793821976634L;

			@Override
			public void paintComponent(Graphics g) {
				if(currentStack.size()>0) {
					BufferedImage image = ExprRender.createImg(currentStack.last(),new Dimension(getWidth(),getHeight()),terminalOut.getBackground(),terminalOut.getForeground() );
					g.drawImage(image,0,0,getWidth(),getHeight(),null);
				}else {
					g.setColor(terminalOut.getBackground());
					g.fillRect(0, 0, getWidth(), getHeight());
				}
			}
		};
		imgExprPanel.setPreferredSize(new Dimension(400,100));
		
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
				
				imgExprPanel.repaint();
				
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
		
		JPanel terminalOutWithImg = new JPanel();
		terminalOutWithImg.setLayout(new BorderLayout());
		terminalOutWithImg.add(scrollableTerminalOut,BorderLayout.CENTER);
		
		terminalOutWithImg.add(imgExprPanel,BorderLayout.SOUTH);
		
		terminal.add(terminalOutWithImg,BorderLayout.CENTER);
		
		
		JPanel terminalInWithButton = new JPanel();
		
		terminalInWithButton.setLayout(new BorderLayout());
		terminalInWithButton.add(terminalIn,BorderLayout.CENTER);
		allComponents.add(terminalInWithButton);
		
		JPanel terminalInButtons = new JPanel();
		terminalInButtons.setLayout(new FlowLayout());
		terminalInButtons.add(resultButton);
		terminalInButtons.add(pushButton);
		allComponents.add(terminalInButtons);
		
		terminalInWithButton.add(terminalInButtons,BorderLayout.EAST);
		
		terminal.add(terminalInWithButton,BorderLayout.SOUTH);
		return terminal;
	}
	
	JPanel createSettingsMenu(JFrame window,ArrayList<JComponent> allComponents) {
		//
		JPanel settingsPanel = new JPanel();
		allComponents.add(settingsPanel);
		
		//keep on top button
		JCheckBox keepOnTop = new JCheckBox("on top",keepWindowOnTop);
		allComponents.add(keepOnTop);
		ActionListener keepOnTopUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.setAlwaysOnTop(keepOnTop.isSelected());
			}
		};
		keepOnTop.addActionListener(keepOnTopUpdate);
		keepOnTopUpdate.actionPerformed(null);
		settingsPanel.add(keepOnTop);
		//
		
		JCheckBox clearTerminalCheckBox = new JCheckBox("clear terminal",clearTerminal);
		allComponents.add(clearTerminalCheckBox);
		clearTerminalCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearTerminal = clearTerminalCheckBox.isSelected();
			}
		});
		settingsPanel.add(clearTerminalCheckBox);
		
		JButton clearCache = new JButton("clear cache");
		allComponents.add(clearCache);
		clearCache.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Expr.clearCache();
			}
		});
		settingsPanel.add(clearCache);
		
		JPanel themeSetter = new JPanel();
		JLabel backgroundLabel = new JLabel("background");
		JTextField br = new JTextField(String.valueOf(background.getRed()),3),bg = new JTextField(String.valueOf(background.getGreen()),3),bb = new JTextField(String.valueOf(background.getBlue()),3);
		themeSetter.add(backgroundLabel);
		themeSetter.add(br);
		themeSetter.add(bg);
		themeSetter.add(bb);
		JLabel foregroundLabel = new JLabel("foreground");
		JTextField fr = new JTextField(String.valueOf(foreground.getRed()),3),fg = new JTextField(String.valueOf(foreground.getGreen()),3),fb = new JTextField(String.valueOf(foreground.getBlue()),3);
		themeSetter.add(foregroundLabel);
		themeSetter.add(fr);
		themeSetter.add(fg);
		themeSetter.add(fb);
		//set theme
		JButton setTheme = new JButton("set theme");
		ActionListener updateTheme = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color background = new Color(Integer.parseInt(br.getText()),Integer.parseInt(bg.getText()),Integer.parseInt(bb.getText()));
				Color foreground = new Color(Integer.parseInt(fr.getText()),Integer.parseInt(fg.getText()),Integer.parseInt(fb.getText()));
				
				for(JComponent c:allComponents) {
					c.setBackground(background);
					c.setForeground(foreground);
					c.setFont(font);
					
					if(c instanceof JTextField) ((JTextField)c).setCaretColor(foreground);
				}
			}
		};
		setTheme.addActionListener(updateTheme);
		updateTheme.actionPerformed(null);
		themeSetter.add(setTheme);
		
		settingsPanel.add(themeSetter);
		
		return settingsPanel;
	}
	
	JPanel createGraphicsMenu(JComponent bottomPanel,JPanel terminal,Plot plot,ArrayList<JComponent> allComponents) {
		
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
		//show plot
		JCheckBox showPlotCheckBox = new JCheckBox("show plot",showPlot);
		allComponents.add(showPlotCheckBox);
		ActionListener showPlotAction = new ActionListener() {

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
			
		};
		showPlotAction.actionPerformed(null);
		showPlotCheckBox.addActionListener(showPlotAction);
		//plot mode
		JComboBox<String> plotModeComboBox = new JComboBox<String>(new String[] {"2D","3D"});
		plotModeComboBox.setSelectedIndex(plotMode);
		allComponents.add(plotModeComboBox);
		ActionListener updatePlotMode = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotModeComboBox.getSelectedItem().equals("2D")) {
					plot.mode = Plot.MODE_2D;
				}else if(plotModeComboBox.getSelectedItem().equals("3D")) {
					plot.mode = Plot.MODE_3D;
				}
				plot.repaint();
			}
			
		};
		plotModeComboBox.addActionListener(updatePlotMode);
		updatePlotMode.actionPerformed(null);
		//
		
		JPanel graphicsOptions = new JPanel();
		
		allComponents.add(graphicsOptions);
		graphicsOptions.setLayout(new FlowLayout());
		graphicsOptions.add(showPlotCheckBox);
		graphicsOptions.add(plotModeComboBox);
		
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
	
	static void setUIStyle() {
		try {
			UIManager.setLookAndFeel(
			        UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static boolean getBoolInLine(String line) {
		return Boolean.valueOf(line.substring(line.lastIndexOf(':')+1));
	}
	static int getIntInLine(String line) {
		return Integer.valueOf(line.substring(line.lastIndexOf(':')+1));
	}
	
	void loadPrefs() {
		try {
			Scanner defsReader = new Scanner(new File("prefs.txt"));
			
			clearTerminal = getBoolInLine(defsReader.nextLine());
			keepWindowOnTop = getBoolInLine(defsReader.nextLine());
			showPlot = getBoolInLine(defsReader.nextLine());
			plotMode = getIntInLine(defsReader.nextLine());
			
			background = new Color(getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()));
			foreground = new Color(getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()));
			
			defsReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public MainWindow(){
		setUIStyle();
		currentStack = new StackEditor();
		loadPrefs();
		createWindow();
	}
	
	public MainWindow(StackEditor stackEditor) {
		setUIStyle();
		currentStack = stackEditor;
		loadPrefs();
		createWindow();
	}

}
