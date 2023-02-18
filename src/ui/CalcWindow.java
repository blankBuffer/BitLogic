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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import cas.Cas;
import cas.SimpleFuncs;
import cas.base.Expr;
import cas.base.Rule;
import cas.graphics.ExprRender;
import cas.graphics.Plot;
import cas.lang.Interpreter;
import cas.programming.StackEditor;

public class CalcWindow extends JFrame{
	private static final long serialVersionUID = -8082018637717346472L;
	
	final int WINDOW_WIDTH = 900,WINDOW_HEIGHT = 500;
	
	StackEditor currentStack;
	Plot plot;
	JPanel mainViewPanel;
	JPanel terminal;
	
	boolean clearTerminal = true;
	
	boolean KEEP_WINDOW_ON_TOP = false;
	boolean SHOW_PLOT_DEFAULT = true;
	static final int _2D = 0,_3D = 1,_COMPLEX = 2;
	int PLOT_MODE_DEFAULT = _2D;
	Color BACKGROUND_COLOR = new Color(255,255,255),FOREGROUND_COLOR = new Color(0,0,0);
	Font font = new Font(null,0,16);
	
	ArrayList<JComponent> allComponents = new ArrayList<JComponent>();
	
	class TerminalPanel extends JPanel{
		private static final long serialVersionUID = 1124640544215850600L;
		
		final int MAX_CHARS = 1024;
		JTextPane terminalOutPane = new JTextPane();
		JPanel terminalOutWithImg = new JPanel();
		JScrollPane scrollableTerminalOut = new JScrollPane(terminalOutPane);
		Expr focusedExpr = null;
		
		public void setFocusedExprDefault() {
			if(!terminalIn.getText().isEmpty()) {
				try {
					focusedExpr = Interpreter.createExpr(terminalIn.getText());
					imgExprPanel.repaint();
				}catch(Exception e1) {
				}
			}else if(currentStack.size() > 0) {
				focusedExpr = currentStack.last();
			}else{
				focusedExpr = null;
			}
			imgExprPanel.repaint();
		}
		
		JPanel imgExprPanel = new JPanel() {
			private static final long serialVersionUID = 5476956793821976634L;

			BufferedImage exprImg = null;
			@Override
			public void paintComponent(Graphics g) {
				if(focusedExpr != null) {
					try {
						exprImg = ExprRender.createImgInFrame(focusedExpr,new Dimension(getWidth(),getHeight()),terminalOutPane.getBackground(),terminalOutPane.getForeground() );
					}catch(Exception e) {
						g.setColor(terminalOutPane.getBackground());
						g.fillRect(0, 0, getWidth(), getHeight());
						exprImg = null;
					}
					g.drawImage(exprImg,0,0,getWidth(),getHeight(),null);
				}else {
					g.setColor(terminalOutPane.getBackground());
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
					BufferedImage exprImg = ExprRender.createImg(currentStack.last(), Color.gray,48);
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					TransferableImage transImage = new TransferableImage(exprImg);
					clipBoard.setContents(transImage, null);
					JOptionPane.showMessageDialog(CalcWindow.this, "copied image to clipboard");
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
			SimpleAttributeSet redText = new SimpleAttributeSet();
			SimpleAttributeSet greenText = new SimpleAttributeSet();
			{
				StyleConstants.setForeground(redText, Color.red.darker());
				StyleConstants.setForeground(greenText, Color.green.darker());
				StyleConstants.setFontSize(greenText, 28);
			}
			
			void addTextln(String s,AttributeSet set) {
				try {
					terminalOutPane.getDocument().insertString(terminalOutPane.getDocument().getLength(), s+"\n", set);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			void addTextln(String s) {
				addTextln(s,null);
			}
			
			void moveCaret() {
				terminalOutPane.setCaretPosition(terminalOutPane.getDocument().getLength());
			}
			
			JButton makeStackButton(String name,String command) {
				JButton stackButton = new JButton(name);
				stackButton.setBackground(Color.gray);
				stackButton.setForeground(Color.white);
				stackButton.setPreferredSize(new Dimension(50,18));
				
				stackButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						currentStack.command(command);
						terminalOutUpdate.actionPerformed(null);
					}
				});
				return stackButton;
			}
			
			JButton makeStackButton(String name,ActionListener l) {
				JButton stackButton = new JButton(name);
				stackButton.setBackground(Color.gray);
				stackButton.setForeground(Color.white);
				stackButton.setPreferredSize(new Dimension(50,18));
				
				stackButton.addActionListener(l);
				
				return stackButton;
			}
			
			ArrayList<ImageIcon> oldStackImages = new ArrayList<ImageIcon>();
			ArrayList<ImageIcon> stackImages = new ArrayList<ImageIcon>();
			
			class StackButtonGroup{
				JButton deleteButton;
				JButton copyButton;
				JButton resultButton;
				JButton editButton;
				JCheckBox showCheckBox;
			}
			ArrayList<StackButtonGroup> stackButtons = new ArrayList<StackButtonGroup>();
			
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					stackImages.clear();
					Color bestBlue = new Color(90,90,255);
					for(int i = 0;i<currentStack.size();i++) {
						ImageIcon imgIcn = null;
						try {
							if(currentStack.stackSequence.get(i).equals(currentStack.stackOld.get(i))) {
								imgIcn = oldStackImages.get(i);
							}
						}catch(IndexOutOfBoundsException e2) {}
						if(imgIcn == null) imgIcn = new ImageIcon( ExprRender.createImg(currentStack.stackSequence.get(i), bestBlue ,18 ) );
						stackImages.add(imgIcn);
					}
					oldStackImages = (ArrayList<ImageIcon>)stackImages.clone();
					
					if(clearTerminal) terminalOutPane.getDocument().remove(0, terminalOutPane.getDocument().getLength());
					addTextln("▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜");
					for(int i = 0;i<currentStack.size();i++) {
						addTextln("#"+(i+1)+" approx: "+currentStack.stackSequence.get(i).convertToFloat(Cas.exprSet()).toString(), redText);
						
						StackButtonGroup stackButtonGroup = new StackButtonGroup();
						
						if(i>stackButtons.size()-1) {
							stackButtonGroup.deleteButton = makeStackButton("delete","pop:"+(i+1));
							stackButtonGroup.copyButton = makeStackButton("copy","dup:"+(i+1));
							stackButtonGroup.resultButton = makeStackButton("result","result:"+(i+1));
							Integer iObj = Integer.valueOf(i);
							stackButtonGroup.editButton = makeStackButton("edit",new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									terminalIn.setText(currentStack.stackSequence.get(iObj).toString());
									currentStack.stackSequence.remove(iObj);
									terminalOutUpdate.actionPerformed(null);
								}
							});
							JCheckBox showCheckBox = new JCheckBox("show",true);
							showCheckBox.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									Expr expr = currentStack.stackSequence.get(iObj);
									
									if(expr.typeName().equals("show") || expr.typeName().equals("hide")) {
										expr = expr.get();
									}
									
									if(!showCheckBox.isSelected()) {
										try {
											expr = SimpleFuncs.getFuncByName("hide", expr);
										} catch (Exception e1) {}
									}
									currentStack.stackSequence.set(iObj,expr);
									terminalOutUpdate.actionPerformed(null);
								}
							});
							stackButtonGroup.showCheckBox = showCheckBox;
							
							stackButtons.add(stackButtonGroup);
						}else {
							stackButtonGroup = stackButtons.get(i);
						}
						moveCaret();
						terminalOutPane.insertComponent(stackButtonGroup.deleteButton);
						moveCaret();
						terminalOutPane.insertComponent(stackButtonGroup.copyButton);
						moveCaret();
						terminalOutPane.insertComponent(stackButtonGroup.resultButton);
						moveCaret();
						terminalOutPane.insertComponent(stackButtonGroup.editButton);
						moveCaret();
						terminalOutPane.insertComponent(stackButtonGroup.showCheckBox);
						moveCaret();
						
						addTextln("");
						
						addTextln( "text: "+currentStack.stackSequence.get(i), redText);
						
						ImageIcon imgIcn = stackImages.get(i);
						
						moveCaret();
						terminalOutPane.insertIcon(imgIcn);
						addTextln("");
						if(i!=currentStack.size()-1)addTextln("______________________________");
					}
					addTextln("▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟");
					addTextln(currentStack.getAlerts(),redText);
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				setFocusedExprDefault();
				
				plot.repaint();
			}
			
		};
		
		JPanel resultAndPushButtons = new JPanel();
		JButton resultButton = new JButton("result");
		ActionListener resultButtonUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(currentStack.command("result") == StackEditor.QUIT) dispose();
				
				terminalOutUpdate.actionPerformed(null);
			}
		};
		CaretListener updateViewBasedOnText = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				setFocusedExprDefault();
			}
		};
		ActionListener terminalInPush = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String terminalInText = terminalIn.getText();
				
				int stackCode = currentStack.command(terminalInText);
				if(stackCode == StackEditor.QUIT) dispose();
				else if(stackCode == StackEditor.INPUT_ERROR) {}
				if(stackCode == StackEditor.FINISHED) {
					terminalIn.setText("");
				}
				terminalOutUpdate.actionPerformed(e);
			}
		};
		
		TerminalPanel(){
			
			try {
				terminalOutPane.getDocument().insertString(terminalOutPane.getDocument().getLength(), UI.CRED+"\n", null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			terminalOutPane.setEditable(false);
			
			allComponents.add(this);
			allComponents.add(terminalOutPane);
			
			allComponents.add(terminalIn);
			allComponents.add(resultButton);
			allComponents.add(resultAndPushButtons);
			allComponents.add(terminalInWithButtons);
			
			setLayout(new BorderLayout());
			scrollableTerminalOut.setMinimumSize(new Dimension(300,200));
			resultAndPushButtons.setLayout(new FlowLayout());
			terminalInWithButtons.setLayout(new BorderLayout());
			imgExprPanel.setPreferredSize(new Dimension(400,130));
			terminalOutWithImg.setLayout(new BorderLayout());
			
			terminalIn.addActionListener(terminalInPush);
			terminalIn.addCaretListener(updateViewBasedOnText);
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
		JComboBox<String> plotModeComboBox = new JComboBox<String>(new String[] {"2D","3D","Complex"});
		ActionListener plotModeUpdate = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotModeComboBox.getSelectedItem().equals("2D")) {
					plot.mode = Plot.MODE_2D;
				}else if(plotModeComboBox.getSelectedItem().equals("3D")) {
					plot.mode = Plot.MODE_3D;
				}else if(plotModeComboBox.getSelectedItem().equals("Complex")) {
					plot.mode = Plot.MODE_COMPLEX;
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
	
	class CASOptionsPanel extends JPanel {
		private static final long serialVersionUID = -1314093096812714908L;
		
		JCheckBox allowComplexNumbersCheckBox = new JCheckBox("allow complex numbers",currentStack.currentCasInfo.allowComplexNumbers());
		ActionListener allowComplexNumbersUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStack.currentCasInfo.setAllowComplexNumbers( allowComplexNumbersCheckBox.isSelected());
			}
		};
		JCheckBox allowAbsCheckBox = new JCheckBox("allow abs(x)",currentStack.currentCasInfo.allowAbs());
		ActionListener allowAbsUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStack.currentCasInfo.setAllowAbs(allowAbsCheckBox.isSelected());
			}
		};
		
		CASOptionsPanel(){
			allComponents.add(this);
			allComponents.add(allowComplexNumbersCheckBox);
			allComponents.add(allowAbsCheckBox);
			
			allowComplexNumbersCheckBox.addActionListener(allowComplexNumbersUpdate);
			allowAbsCheckBox.addActionListener(allowAbsUpdate);
			
			add(allowComplexNumbersCheckBox);
			add(allowAbsCheckBox);
		}
		
		
	}
	
	class UIOptionsPanel extends JPanel{
		private static final long serialVersionUID = -4037663936986002307L;
		
		JCheckBox keepOnTopCheckBox = new JCheckBox("window on top",KEEP_WINDOW_ON_TOP);
		ActionListener keepOnTopUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = keepOnTopCheckBox.isSelected();
				CalcWindow.this.setAlwaysOnTop(selected);
				KEEP_WINDOW_ON_TOP = selected;
			}
		};
		JCheckBox clearTerminalCheckBox = new JCheckBox("clear terminal",clearTerminal);
		ActionListener clearTerminalUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearTerminal = clearTerminalCheckBox.isSelected();
			}
		};
		JLabel scrollSpeedLabel = new JLabel("zoom scroll speed");
		JSlider scrollSpeedSlider = new JSlider(1,100,(int)(plot.scrollSpeed*400.0));
		ChangeListener scrollSpeedChange = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				plot.scrollSpeed = scrollSpeedSlider.getValue()/400.0;
			}
		};
		
		JPanel themeSetter = new JPanel();
		JLabel backgroundLabel = new JLabel("background");
		JTextField br = new JTextField(String.valueOf(BACKGROUND_COLOR.getRed()),3),bg = new JTextField(String.valueOf(BACKGROUND_COLOR.getGreen()),3),bb = new JTextField(String.valueOf(BACKGROUND_COLOR.getBlue()),3);
		JLabel foregroundLabel = new JLabel("foreground");
		JTextField fr = new JTextField(String.valueOf(FOREGROUND_COLOR.getRed()),3),fg = new JTextField(String.valueOf(FOREGROUND_COLOR.getGreen()),3),fb = new JTextField(String.valueOf(FOREGROUND_COLOR.getBlue()),3);
		JButton setTheme = new JButton("set theme");
		ActionListener themeUpdate = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color background = new Color(Integer.parseInt(br.getText()),Integer.parseInt(bg.getText()),Integer.parseInt(bb.getText()));
				Color foreground = new Color(Integer.parseInt(fr.getText()),Integer.parseInt(fg.getText()),Integer.parseInt(fb.getText()));
				
				for(JComponent c:allComponents) {
					c.setBackground(background);
					c.setForeground(foreground);
					if(c instanceof JTextPane) c.setFont(new Font(ExprRender.FONT,0,16));
					else c.setFont(font);
					
					if(c instanceof JTextField) ((JTextField)c).setCaretColor(foreground);
				}
			}
		};
		
		UIOptionsPanel() {
			allComponents.add(this);
			allComponents.add(keepOnTopCheckBox);
			allComponents.add(scrollSpeedLabel);
			allComponents.add(scrollSpeedSlider);
			allComponents.add(clearTerminalCheckBox);
			
			keepOnTopCheckBox.addActionListener(keepOnTopUpdate);
			clearTerminalCheckBox.addActionListener(clearTerminalUpdate);
			setTheme.addActionListener(themeUpdate);
			scrollSpeedSlider.addChangeListener(scrollSpeedChange);
			
			scrollSpeedSlider.setPaintTicks(true);
			scrollSpeedSlider.setSnapToTicks(true);
			scrollSpeedSlider.setMajorTickSpacing(10);
			
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
			add(scrollSpeedLabel);
			add(scrollSpeedSlider);
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
		
		tabs.addTab("Main View",mainViewPanel);
		tabs.addTab("Graphics", new GraphicsPanel());
		tabs.addTab("CAS Options", new CASOptionsPanel());
		tabs.addTab("UI Options", new UIOptionsPanel());//keep last
		
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
			KEEP_WINDOW_ON_TOP = getBoolInLine(defsReader.nextLine());
			SHOW_PLOT_DEFAULT = getBoolInLine(defsReader.nextLine());
			PLOT_MODE_DEFAULT = getIntInLine(defsReader.nextLine());
			
			BACKGROUND_COLOR = new Color(getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()));
			FOREGROUND_COLOR = new Color(getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()),getIntInLine(defsReader.nextLine()));
			
			defsReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadRulesWindow() {
		JProgressBar progressBar = new JProgressBar(0,100);
		JFrame progressBarWindow = new JFrame("loading rules");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		try {
			panel.add(new JLabel(new ImageIcon(ImageIO.read(new File("resources/BitLogicLogo_tiny.jpg")))));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		panel.add(progressBar);
		progressBarWindow.add(panel);
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
						progressBar.setValue((int)Rule.getLoadingPercent());
						if(Rule.getLoadingPercent() == 100) loading = false;
						Thread.sleep(15);
					} catch (InterruptedException e) {e.printStackTrace();}
				}
				progressBarWindow.dispose();
			}
		};
		progressBarThread.start();
		Thread rulesLoader = new Thread() {
			@Override
			public void run() {
				Rule.loadCompileSimplifyRules();
			}
		};
		rulesLoader.start();
	}

	public CalcWindow(){
		super("BitLogic "+UI.VERSION);
		currentStack = new StackEditor();
		plot = new Plot(currentStack);
		mainViewPanel = new JPanel();
		mainViewPanel.setLayout(new BorderLayout());
		terminal = new TerminalPanel();
		
		loadPrefs();
		
		setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		setMinimumSize(new Dimension(600,200));
		add(createMainContainer());
		setLocationRelativeTo(null);
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
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
		
		setVisible(true);
		
		loadRulesWindow();
	}

}
