package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class TriangleSolver extends JFrame{
	private static final long serialVersionUID = 6015709278345259540L;
	boolean radiansMode = true;

	public TriangleSolver() {
		super("triangle solver");
		setSize(600,600);
		
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent e) {
			}
			@Override
			public void windowClosed(WindowEvent e) {
			}
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			@Override
			public void windowIconified(WindowEvent e) {
			}
			@Override
			public void windowOpened(WindowEvent e) {
			}
		});
		this.setLayout(new BorderLayout());
		
		JPanel trianglePanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				g2.setStroke(new BasicStroke(5));
				g.drawLine(getWidth()/4,getHeight()*3/4,getWidth()*3/4, getHeight()*3/4);
				g.drawLine(getWidth()/4,getHeight()*3/4,getWidth()*2/5, getHeight()/4);
				g.drawLine(getWidth()*3/4, getHeight()*3/4,getWidth()*2/5, getHeight()/4);
				
				g2.setFont(new Font(null,Font.PLAIN,20));
				
				g2.drawString("a-angle", getWidth()/4-64,getHeight()*3/4+32);
				g2.drawString("b-angle", getWidth()*3/4, getHeight()*3/4+32);
				g2.drawString("c-angle", getWidth()*2/5-32, getHeight()/4-10);
				
				g2.drawString("C-side", getWidth()/2-32,getHeight()*3/4+32);
				g2.drawString("A-side", getWidth()*23/40 , getHeight()/2-16);
				g2.drawString("B-side", (getWidth()/4+getWidth()*2/5)/2-64, getHeight()/2-16);
			}
		};
		add(trianglePanel,BorderLayout.CENTER);
		JPanel angleFields = new JPanel();
		angleFields.setLayout(new BoxLayout(angleFields,BoxLayout.Y_AXIS));
		
		JTextField aAngleField = new JTextField();
		angleFields.add(new JLabel("a-angle:"));
		angleFields.add(aAngleField);
		JTextField bAngleField = new JTextField();
		angleFields.add(new JLabel("b-angle:"));
		angleFields.add(bAngleField);
		JTextField cAngleField = new JTextField();
		angleFields.add(new JLabel("c-angle:"));
		angleFields.add(cAngleField);
		
		JPanel sideFields = new JPanel();
		sideFields.setLayout(new BoxLayout(sideFields,BoxLayout.Y_AXIS));
		
		JTextField aSideField = new JTextField();
		sideFields.add(new JLabel("A-side:"));
		sideFields.add(aSideField);
		JTextField bSideField = new JTextField();
		sideFields.add(new JLabel("B-side:"));
		sideFields.add(bSideField);
		JTextField cSideField = new JTextField();
		sideFields.add(new JLabel("C-side:"));
		sideFields.add(cSideField);
		
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel,BoxLayout.X_AXIS));
		inputPanel.add(angleFields);
		inputPanel.add(sideFields);
		
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel,BoxLayout.Y_AXIS));
		
		JRadioButton radiansRadioButton = new JRadioButton("radians",radiansMode);
		radiansRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!radiansMode) {
					radiansMode = true;
					if(!aAngleField.getText().isEmpty()) aAngleField.setText(String.valueOf(Math.toRadians(Double.parseDouble(aAngleField.getText()))));
					if(!bAngleField.getText().isEmpty()) bAngleField.setText(String.valueOf(Math.toRadians(Double.parseDouble(bAngleField.getText()))));
					if(!cAngleField.getText().isEmpty()) cAngleField.setText(String.valueOf(Math.toRadians(Double.parseDouble(cAngleField.getText()))));
				}
			}
		});
		JRadioButton degreesRadioButton = new JRadioButton("degrees",!radiansMode);
		degreesRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(radiansMode) {
					radiansMode = false;
					if(!aAngleField.getText().isEmpty()) aAngleField.setText(String.valueOf(Math.toDegrees(Double.parseDouble(aAngleField.getText()))));
					if(!bAngleField.getText().isEmpty()) bAngleField.setText(String.valueOf(Math.toDegrees(Double.parseDouble(bAngleField.getText()))));
					if(!cAngleField.getText().isEmpty()) cAngleField.setText(String.valueOf(Math.toDegrees(Double.parseDouble(cAngleField.getText()))));
				}
			}
		});
		
		ButtonGroup radianDegreesGroup = new ButtonGroup();
		radianDegreesGroup.add(radiansRadioButton);
		radianDegreesGroup.add(degreesRadioButton);
		
		optionsPanel.add(radiansRadioButton);
		optionsPanel.add(degreesRadioButton);
		
		JButton clearButton = new JButton("clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aAngleField.setText("");
				bAngleField.setText("");
				cAngleField.setText("");
				
				aSideField.setText("");
				bSideField.setText("");
				cSideField.setText("");
			}
		});
		optionsPanel.add(clearButton);
		
		inputPanel.add(optionsPanel);
		
		JLabel areaLabel = new JLabel("Area = ");
		
		JButton solveButton = new JButton("solve");
		solveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String aAngleText = aAngleField.getText();
				String bAngleText = bAngleField.getText();
				String cAngleText = cAngleField.getText();
				
				String aSideText = aSideField.getText();
				String bSideText = bSideField.getText();
				String cSideText = cSideField.getText();
				
				
				boolean aAngleEmpty = aAngleText.isEmpty();
				boolean bAngleEmpty = bAngleText.isEmpty();
				boolean cAngleEmpty = cAngleText.isEmpty();
				
				boolean aSideEmpty = aSideText.isEmpty();
				boolean bSideEmpty = bSideText.isEmpty();
				boolean cSideEmpty = cSideText.isEmpty();
				
				double aAngle = 0, bAngle = 0,cAngle = 0;
				double aSide = 0, bSide = 0,cSide = 0;
				
				if(!aAngleEmpty) aAngle = radiansMode ? Double.parseDouble(aAngleText) : Math.toRadians(Double.parseDouble(aAngleText));
				if(!bAngleEmpty) bAngle = radiansMode ? Double.parseDouble(bAngleText) : Math.toRadians(Double.parseDouble(bAngleText));
				if(!cAngleEmpty) cAngle = radiansMode ? Double.parseDouble(cAngleText) : Math.toRadians(Double.parseDouble(cAngleText));
				
				if(!aSideEmpty) aSide = Double.parseDouble(aSideText);
				if(!bSideEmpty) bSide = Double.parseDouble(bSideText);
				if(!cSideEmpty) cSide = Double.parseDouble(cSideText);
				
				//cosine law
				if(cAngleEmpty && !aSideEmpty && !bSideEmpty && !cSideEmpty) {//c_angle=acos((a^2+b^2-c^2)/(2*a*b))
					cAngle = Math.acos( (aSide*aSide+bSide*bSide-cSide*cSide)/(2*aSide*bSide) );
					cAngleEmpty = false;
				}
				if(bAngleEmpty && !aSideEmpty && !bSideEmpty && !cSideEmpty) {
					bAngle = Math.acos( (aSide*aSide+cSide*cSide-bSide*bSide)/(2*aSide*cSide) );
					bAngleEmpty = false;
				}
				if(aAngleEmpty && !aSideEmpty && !bSideEmpty && !cSideEmpty) {
					aAngle = Math.acos( (bSide*bSide+cSide*cSide-aSide*aSide)/(2*bSide*cSide) );
					aAngleEmpty = false;
				}
				//law of sines
				if(aAngleEmpty && !aSideEmpty && !bAngleEmpty && !bSideEmpty) {
					aAngle = Math.asin(Math.sin(bAngle)/bSide*aSide);
					aAngleEmpty = false;
				}
				if(aAngleEmpty && !aSideEmpty && !cAngleEmpty && !cSideEmpty) {
					aAngle = Math.asin(Math.sin(cAngle)/cSide*aSide);
					aAngleEmpty = false;
				}
				
				if(bAngleEmpty && !bSideEmpty && !aAngleEmpty && !aSideEmpty) {
					bAngle = Math.asin(Math.sin(aAngle)/aSide*bSide);
					bAngleEmpty = false;
				}
				if(bAngleEmpty && !bSideEmpty && !cAngleEmpty && !cSideEmpty) {
					bAngle = Math.asin(Math.sin(cAngle)/cSide*bSide);
					bAngleEmpty = false;
				}
				
				if(cAngleEmpty && !cSideEmpty && !bAngleEmpty && !bSideEmpty) {
					cAngle = Math.asin(Math.sin(bAngle)/bSide*cSide);
					cAngleEmpty = false;
				}
				if(cAngleEmpty && !cSideEmpty && !aAngleEmpty && !aSideEmpty) {
					cAngle = Math.asin(Math.sin(aAngle)/aSide*cSide);
					cAngleEmpty = false;
				}
				//sum of angles
				if(!aAngleEmpty && !bAngleEmpty && cAngleEmpty) {
					cAngle = Math.PI-aAngle-bAngle;
					cAngleEmpty = false;
				}else if(!aAngleEmpty && bAngleEmpty && !cAngleEmpty) {
					bAngle = Math.PI-aAngle-cAngle;
					bAngleEmpty = false;
				}else if(aAngleEmpty && !bAngleEmpty && !cAngleEmpty) {
					aAngle = Math.PI-bAngle-cAngle;
					aAngleEmpty = false;
				}
				
				//solve sides
				if(!aSideEmpty) {
					double ratio = aSide/Math.sin(aAngle);
					if(bSideEmpty) {
						bSide = ratio*Math.sin(bAngle);
						bSideEmpty = false;
					}
					if(cSideEmpty) {
						cSide = ratio*Math.sin(cAngle);
						cSideEmpty = false;
					}
				}
				if(!bSideEmpty) {
					double ratio = bSide/Math.sin(bAngle);
					if(aSideEmpty) {
						aSide = ratio*Math.sin(aAngle);
						aSideEmpty = false;
					}
					if(cSideEmpty) {
						cSide = ratio*Math.sin(cAngle);
						cSideEmpty = false;
					}
				}
				if(!cSideEmpty) {
					double ratio = cSide/Math.sin(cAngle);
					if(bSideEmpty) {
						bSide = ratio*Math.sin(bAngle);
						bSideEmpty = false;
					}
					if(aSideEmpty) {
						aSide = ratio*Math.sin(aAngle);
						aSideEmpty = false;
					}
				}
				
				aAngleField.setText(String.valueOf( radiansMode ? aAngle : Math.toDegrees(aAngle) ));
				bAngleField.setText(String.valueOf( radiansMode ? bAngle : Math.toDegrees(bAngle) ));
				cAngleField.setText(String.valueOf( radiansMode ? cAngle : Math.toDegrees(cAngle) ));
				
				aSideField.setText(String.valueOf(aSide));
				bSideField.setText(String.valueOf(bSide));
				cSideField.setText(String.valueOf(cSide));
				
				double p = (aSide+bSide+cSide)/2;
				areaLabel.setText("Area = "+String.valueOf( Math.sqrt( p*(p-aSide)*(p-bSide)*(p-cSide) ) ));
			}
		});
		
		optionsPanel.add(solveButton);
		
		add(areaLabel,BorderLayout.SOUTH);
		add(inputPanel,BorderLayout.NORTH);
		setVisible(true);
	}
}
