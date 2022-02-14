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
import cas.StackEditor;
import graphics.Plot;

public class MainWindow extends JFrame{
	private static final long serialVersionUID = -8082018637717346472L;
	
	final int WINDOW_WIDTH = 900,WINDOW_HEIGHT = 500;
	
	StackEditor currentStack;
	Plot plot;
	JPanel mainViewPanel;
	JPanel terminal;
	
	boolean clearTerminal = true;
	
	boolean KEEP_WINDOW_ON_TOP_DEFAULT = false;
	boolean SHOW_PLOT_DEFAULT = true;
	static final int _2D = 0,_3D = 1;
	int PLOT_MODE_DEFAULT = _2D;
	Color BACKGROUND_COLOR_DEFAULT = new Color(255,255,255),FOREGROUND_COLOR_DEFAULT = new Color(0,0,0);
	Font font = new Font("courier new",0,16);
	
	ArrayList<JComponent> allComponents = new ArrayList<JComponent>();
	
	class TerminalPanel extends JPanel{
		private static final long serialVersionUID = 1124640544215850600L;
		
		final int MAX_CHARS = 1024;
		JTextArea terminalOut = new JTextArea();
		JPanel terminalOutWithImg = new JPanel();
		JScrollPane scrollableTerminalOut = new JScrollPane(terminalOut);
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
		JPanel terminalInWithButtons = new JPanel();
		JTextField terminalIn = new JTextField();
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
		JPanel resultAndPushButtons = new JPanel();
		JButton resultButton = new JButton("result");
		ActionListener resultButtonUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStack.command("result");
				terminalOutUpdate.actionPerformed(null);
			}
		};
		JButton pushButton = new JButton("push");
		ActionListener terminalInPush = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = terminalIn.getText();
				int quit = currentStack.command(command);
				if(quit == -1) {
					MainWindow.this.dispose();
				}
				terminalIn.setText("");
				terminalOutUpdate.actionPerformed(e);
			}
		};
		
		TerminalPanel(){
			
			allComponents.add(this);
			allComponents.add(terminalOut);
			allComponents.add(terminalIn);
			allComponents.add(resultButton);
			allComponents.add(pushButton);
			allComponents.add(resultAndPushButtons);
			allComponents.add(terminalInWithButtons);
			
			setLayout(new BorderLayout());
			terminalOut.setLineWrap(true);
			terminalOut.setEditable(false);
			terminalOut.setText(UI.fancyIntro());
			scrollableTerminalOut.setMinimumSize(new Dimension(300,200));
			resultAndPushButtons.setLayout(new FlowLayout());
			terminalInWithButtons.setLayout(new BorderLayout());
			imgExprPanel.setPreferredSize(new Dimension(400,100));
			terminalOutWithImg.setLayout(new BorderLayout());
			
			terminalIn.addActionListener(terminalInPush);
			resultButton.addActionListener(resultButtonUpdate);
			pushButton.addActionListener(terminalInPush);
			
			terminalOutWithImg.add(scrollableTerminalOut,BorderLayout.CENTER);
			terminalOutWithImg.add(imgExprPanel,BorderLayout.SOUTH);
			terminalInWithButtons.add(terminalIn,BorderLayout.CENTER);
			resultAndPushButtons.add(resultButton);
			resultAndPushButtons.add(pushButton);
			terminalInWithButtons.add(resultAndPushButtons,BorderLayout.EAST);
			
			add(terminalOutWithImg,BorderLayout.CENTER);
			add(terminalInWithButtons,BorderLayout.SOUTH);
		}
		
	}
	
	class GraphicsPanel extends JPanel{
		private static final long serialVersionUID = 845735191834622299L;
		
		JCheckBox showPlotCheckBox = new JCheckBox("show plot",SHOW_PLOT_DEFAULT);
		ActionListener showPlotAction = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(showPlotCheckBox.isSelected()) {
					plot.setVisible(true);
					mainViewPanel.removeAll();
					JSplitPane splitView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,terminal,plot);
					splitView.setDividerLocation(WINDOW_WIDTH/2);
					mainViewPanel.add(splitView);
				}else {
					plot.setVisible(false);
					mainViewPanel.removeAll();
					mainViewPanel.add(terminal);
				}
				
				mainViewPanel.updateUI();
			}
			
		};
		JComboBox<String> plotModeComboBox = new JComboBox<String>(new String[] {"2D","3D"});
		ActionListener plotModeUpdate = new ActionListener() {

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
		final int TEXT_FIELD_CHARS = 6;
		JPanel windowParams = new JPanel();
		JLabel xMinLabel = new JLabel("x-Min");
		JTextField xMin = new JTextField("-10.0",TEXT_FIELD_CHARS);
		JLabel xMaxLabel = new JLabel("x-Max");
		JTextField xMax = new JTextField("10.0",TEXT_FIELD_CHARS);
		JLabel yMinLabel = new JLabel("y-Min");
		JTextField yMin = new JTextField("-10.0",TEXT_FIELD_CHARS);
		JLabel yMaxLabel = new JLabel("y-Max");
		JTextField yMax = new JTextField("10.0",TEXT_FIELD_CHARS);
		JLabel zMinLabel = new JLabel("z-Min");
		JTextField zMin = new JTextField("-10.0",TEXT_FIELD_CHARS);
		JLabel zMaxLabel = new JLabel("z-Max");
		JTextField zMax = new JTextField("10.0",TEXT_FIELD_CHARS);
		JButton getWindow = new JButton("get window");
		ActionListener getWindowUpdate = new ActionListener() {
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
		};
		JButton setWindow = new JButton("set window");
		ActionListener setWindowUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plot.plotParams.set(Double.parseDouble(xMin.getText()),Double.parseDouble(xMax.getText()),Double.parseDouble(yMin.getText()),Double.parseDouble(yMax.getText()),Double.parseDouble(zMin.getText()),Double.parseDouble(zMax.getText()));
				plot.repaint();
			}
		};
		JButton resetWindow = new JButton("reset");
		ActionListener resetWindowUpdate = new ActionListener() {
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
		};
		
		GraphicsPanel() {
			
			allComponents.add(this);
			allComponents.add(showPlotCheckBox);
			allComponents.add(plotModeComboBox);
			allComponents.add(windowParams);
			allComponents.add(xMinLabel);
			allComponents.add(xMin);
			allComponents.add(xMaxLabel);
			allComponents.add(xMax);
			allComponents.add(yMinLabel);
			allComponents.add(yMin);
			allComponents.add(yMaxLabel);
			allComponents.add(yMax);
			allComponents.add(zMinLabel);
			allComponents.add(zMin);
			allComponents.add(zMaxLabel);
			allComponents.add(zMax);
			allComponents.add(getWindow);
			allComponents.add(setWindow);
			allComponents.add(resetWindow);
			
			setLayout(new FlowLayout());
			windowParams.setLayout(new FlowLayout());
			
			showPlotCheckBox.addActionListener(showPlotAction);
			plotModeComboBox.addActionListener(plotModeUpdate);
			getWindow.addActionListener(getWindowUpdate);
			setWindow.addActionListener(setWindowUpdate);
			resetWindow.addActionListener(resetWindowUpdate);
			
			showPlotAction.actionPerformed(null);
			plotModeComboBox.setSelectedIndex(PLOT_MODE_DEFAULT);
			plotModeUpdate.actionPerformed(null);
			
			
			windowParams.add(xMinLabel);
			windowParams.add(xMin);
			windowParams.add(xMaxLabel);
			windowParams.add(xMax);
			windowParams.add(yMinLabel);
			windowParams.add(yMin);
			windowParams.add(yMaxLabel);
			windowParams.add(yMax);
			windowParams.add(zMinLabel);
			windowParams.add(zMin);
			windowParams.add(zMaxLabel);
			windowParams.add(zMax);
			
			add(showPlotCheckBox);
			add(plotModeComboBox);
			add(windowParams);
			add(getWindow);
			add(setWindow);
			add(resetWindow);
		}
	}
	
	class SettingsPanel extends JPanel{
		private static final long serialVersionUID = -4037663936986002307L;
		
		JCheckBox keepOnTopCheckBox = new JCheckBox("window on top",KEEP_WINDOW_ON_TOP_DEFAULT);
		ActionListener keepOnTopUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainWindow.this.setAlwaysOnTop(keepOnTopCheckBox.isSelected());
			}
		};
		JCheckBox clearTerminalCheckBox = new JCheckBox("clear terminal",clearTerminal);
		ActionListener clearTerminalUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearTerminal = clearTerminalCheckBox.isSelected();
			}
		};
		JButton clearCache = new JButton("clear cache");
		ActionListener clearCacheUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Expr.clearCache();
			}
		};
		JPanel themeSetter = new JPanel();
		JLabel backgroundLabel = new JLabel("background");
		JTextField br = new JTextField(String.valueOf(BACKGROUND_COLOR_DEFAULT.getRed()),3),bg = new JTextField(String.valueOf(BACKGROUND_COLOR_DEFAULT.getGreen()),3),bb = new JTextField(String.valueOf(BACKGROUND_COLOR_DEFAULT.getBlue()),3);
		JLabel foregroundLabel = new JLabel("foreground");
		JTextField fr = new JTextField(String.valueOf(FOREGROUND_COLOR_DEFAULT.getRed()),3),fg = new JTextField(String.valueOf(FOREGROUND_COLOR_DEFAULT.getGreen()),3),fb = new JTextField(String.valueOf(FOREGROUND_COLOR_DEFAULT.getBlue()),3);
		JButton setTheme = new JButton("set theme");
		ActionListener themeUpdate = new ActionListener() {
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
		
		SettingsPanel() {
			allComponents.add(this);
			allComponents.add(keepOnTopCheckBox);
			allComponents.add(clearTerminalCheckBox);
			allComponents.add(clearCache);
			
			keepOnTopCheckBox.addActionListener(keepOnTopUpdate);
			clearTerminalCheckBox.addActionListener(clearTerminalUpdate);
			clearCache.addActionListener(clearCacheUpdate);
			setTheme.addActionListener(themeUpdate);
			
			keepOnTopUpdate.actionPerformed(null);
			themeUpdate.actionPerformed(null);
			
			themeSetter.add(backgroundLabel);
			themeSetter.add(br);
			themeSetter.add(bg);
			themeSetter.add(bb);
			themeSetter.add(foregroundLabel);
			themeSetter.add(fr);
			themeSetter.add(fg);
			themeSetter.add(fb);
			themeSetter.add(setTheme);
			
			add(keepOnTopCheckBox);
			add(clearTerminalCheckBox);
			add(clearCache);
			add(themeSetter);
		}
		
	}
	
	JPanel createMainContainer() {
		JPanel mainContainer = new JPanel();
		allComponents.add(mainContainer);
		mainContainer.setLayout(new BorderLayout());
		
		JTabbedPane tabs = new JTabbedPane();
		allComponents.add(tabs);
		mainContainer.add(tabs,BorderLayout.CENTER);
		
		allComponents.add(plot);
		
		tabs.addTab("main view",mainViewPanel);
		tabs.addTab("graphics", new GraphicsPanel());
		tabs.addTab("settings", new SettingsPanel());
		
		return mainContainer;
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
			KEEP_WINDOW_ON_TOP_DEFAULT = getBoolInLine(defsReader.nextLine());
			SHOW_PLOT_DEFAULT = getBoolInLine(defsReader.nextLine());
			PLOT_MODE_DEFAULT = getIntInLine(defsReader.nextLine());
			
			BACKGROUND_COLOR_DEFAULT = new Color(getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()));
			FOREGROUND_COLOR_DEFAULT = new Color(getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()));
			
			defsReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public MainWindow(){
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		currentStack = new StackEditor();
		plot = new Plot(currentStack);
		mainViewPanel = new JPanel();
		mainViewPanel.setLayout(new BorderLayout());
		terminal = new TerminalPanel();
		
		loadPrefs();
		
		setName("BitLogic");
		setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		setMinimumSize(new Dimension(600,200));
		add(createMainContainer());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
