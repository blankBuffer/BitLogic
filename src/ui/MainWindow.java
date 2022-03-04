package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.awt.datatransfer.*;

import javax.swing.*;

import cas.Rule;
import cas.graphics.ExprRender;
import cas.graphics.Plot;
import cas.programming.StackEditor;

public class MainWindow extends JFrame{
	private static final long serialVersionUID = -8082018637717346472L;
	
	final int WINDOW_WIDTH = 900,WINDOW_HEIGHT = 500;
	
	private static int WINDOW_INSTANCE_COUNT = 0;
	
	void closeWindow() {
		if(WINDOW_INSTANCE_COUNT == 1) {
			System.out.println("closing...");
			System.exit(0);
		}else {
			dispose();
			WINDOW_INSTANCE_COUNT--;
		}
	}
	
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

			BufferedImage exprImg = null;
			@Override
			public void paintComponent(Graphics g) {
				if(currentStack.size()>0) {
					exprImg = ExprRender.createImgInFrame(currentStack.last(),new Dimension(getWidth(),getHeight()),terminalOut.getBackground(),terminalOut.getForeground() );
					g.drawImage(exprImg,0,0,getWidth(),getHeight(),null);
				}else {
					g.setColor(terminalOut.getBackground());
					g.fillRect(0, 0, getWidth(), getHeight());
					exprImg = null;
				}
			}
		};
		MouseListener imageToClipBoard = new MouseListener() {
			class TransferableImage implements Transferable {
		        Image image;
		        public TransferableImage( Image image ) {
		            this.image = image;
		        }
		        @Override
				public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
		            if ( flavor.equals( DataFlavor.imageFlavor ) && image != null ) {
		                return image;
		            }
					throw new UnsupportedFlavorException( flavor );
		        }
		        @Override
				public DataFlavor[] getTransferDataFlavors() {
		            DataFlavor[] flavors = new DataFlavor[ 1 ];
		            flavors[ 0 ] = DataFlavor.imageFlavor;
		            return flavors;
		        }
		        @Override
				public boolean isDataFlavorSupported( DataFlavor flavor ) {
		            DataFlavor[] flavors = getTransferDataFlavors();
		            for ( int i = 0; i < flavors.length; i++ ) {
		                if ( flavor.equals( flavors[ i ] ) ) {
		                    return true;
		                }
		            }

		            return false;
		        }
		    }
			@Override
			public void mouseClicked(MouseEvent e) {
				if(currentStack.size() > 0) {
					BufferedImage exprImg = ExprRender.createImg(currentStack.last(), terminalOut.getBackground(),terminalOut.getForeground());
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					TransferableImage transImage = new TransferableImage(exprImg);
					clipBoard.setContents(transImage, null);
					JOptionPane.showMessageDialog(MainWindow.this, "copied image to clipboard");
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
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
				if(currentStack.command("result") == StackEditor.QUIT) closeWindow();
				
				terminalOutUpdate.actionPerformed(null);
			}
		};
		ActionListener terminalInPush = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String terminalInText = terminalIn.getText();
				
				int stackCode = currentStack.command(terminalInText);
				if(stackCode == StackEditor.QUIT) closeWindow();
				else if(stackCode == StackEditor.EDIT_REQUEST) {
					terminalIn.setText(currentStack.last().toString());
					currentStack.pop();
				}
				else if(stackCode == StackEditor.INPUT_ERROR) {}
				if(stackCode == StackEditor.FINISHED) {
					terminalIn.setText("");
				}
				terminalOutUpdate.actionPerformed(e);
			}
		};
		
		TerminalPanel(){
			
			allComponents.add(this);
			allComponents.add(terminalOut);
			allComponents.add(terminalIn);
			allComponents.add(resultButton);
			allComponents.add(resultAndPushButtons);
			allComponents.add(terminalInWithButtons);
			
			setLayout(new BorderLayout());
			terminalOut.setLineWrap(true);
			terminalOut.setEditable(false);
			terminalOut.setText(UI.fancyIntro());
			scrollableTerminalOut.setMinimumSize(new Dimension(300,200));
			resultAndPushButtons.setLayout(new FlowLayout());
			terminalInWithButtons.setLayout(new BorderLayout());
			imgExprPanel.setPreferredSize(new Dimension(400,200));
			terminalOutWithImg.setLayout(new BorderLayout());
			
			terminalIn.addActionListener(terminalInPush);
			resultButton.addActionListener(resultButtonUpdate);
			
			imgExprPanel.addMouseListener(imageToClipBoard);
			
			terminalOutWithImg.add(scrollableTerminalOut,BorderLayout.CENTER);
			terminalOutWithImg.add(imgExprPanel,BorderLayout.SOUTH);
			terminalInWithButtons.add(terminalIn,BorderLayout.CENTER);
			resultAndPushButtons.add(resultButton);
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
	
	class CASCasInfoPanel extends JPanel {
		private static final long serialVersionUID = -1314093096812714908L;
		
		JCheckBox allowComplexNumbersCheckBox = new JCheckBox("allow complex numbers",currentStack.currentCasInfo.allowComplexNumbers);
		ActionListener allowComplexNumbersUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStack.currentCasInfo.allowComplexNumbers = allowComplexNumbersCheckBox.isSelected();
			}
		};
		JCheckBox allowAbsCheckBox = new JCheckBox("allow abs(x)",currentStack.currentCasInfo.allowAbs);
		ActionListener allowAbsUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStack.currentCasInfo.allowAbs = allowAbsCheckBox.isSelected();
			}
		};
		
		CASCasInfoPanel(){
			allComponents.add(this);
			allComponents.add(allowComplexNumbersCheckBox);
			allComponents.add(allowAbsCheckBox);
			
			allowComplexNumbersCheckBox.addActionListener(allowComplexNumbersUpdate);
			allowAbsCheckBox.addActionListener(allowAbsUpdate);
			
			add(allowComplexNumbersCheckBox);
			add(allowAbsCheckBox);
		}
		
		
	}
	
	class UICasInfoPanel extends JPanel{
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
		
		UICasInfoPanel() {
			allComponents.add(this);
			allComponents.add(keepOnTopCheckBox);
			allComponents.add(clearTerminalCheckBox);
			
			keepOnTopCheckBox.addActionListener(keepOnTopUpdate);
			clearTerminalCheckBox.addActionListener(clearTerminalUpdate);
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
		tabs.addTab("CAS casInfo", new CASCasInfoPanel());
		tabs.addTab("UI casInfo", new UICasInfoPanel());//keep last
		
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
			Scanner defsReader = new Scanner(new File("resources/prefs.txt"));
			
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
	
	public static void loadRulesWindow() {
		JProgressBar progressBar = new JProgressBar(0,100);
		JFrame progressBarWindow = new JFrame();
		progressBarWindow.setLayout(new FlowLayout());
		progressBarWindow.add(new JLabel("loading rules"));
		progressBarWindow.add(progressBar);
		progressBarWindow.pack();
		progressBarWindow.setResizable(false);
		progressBarWindow.setLocationRelativeTo(null);
		progressBarWindow.setVisible(true);
		progressBarWindow.setAlwaysOnTop(true);
		Thread progressBarThread = new Thread() {
			@Override
			public void run() {
				boolean loading = true;
				while(loading) {
					
					try {
						progressBar.setValue(Rule.loadingPercent);
						if(Rule.loadingPercent == 100) loading = false;
						Thread.sleep(15);
					} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		};
		progressBarThread.start();
		Rule.loadRules();
		try {
			progressBarThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		progressBarWindow.dispose();
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
		setLocationRelativeTo(null);
		setVisible(true);
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
		});
		WINDOW_INSTANCE_COUNT++;
		loadRulesWindow();
	}

}
