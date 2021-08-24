import cas.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class Plot extends QuickMath{
	double xMin = -10,xMax = 10,yMin = -10,yMax = 10;
	int mouseX,mouseY,pMouseX,pMouseY;
	boolean mousePressed;
	JPanel panel = null;
	
	double convertToInternalPositionX(int external) {
		return (((double)external)/(double)panel.getWidth())*(xMax-xMin)+xMin;
	}
	double convertToInternalPositionY(int external) {
		return (((double)panel.getHeight()-(double)external)/(double)panel.getHeight())*(yMax-yMin)+yMin;
	}
	int convertToExternalPositionY(double internal) {
		if(Double.isNaN(internal)) internal = 0;
		return (int)(panel.getHeight()-(internal-yMin)/(yMax-yMin)*panel.getHeight());
	}
	int convertToExternalPositionX(double internal) {
		return (int)((internal-xMin)/(xMax-xMin)*panel.getWidth());
	}
	
	Color randomColor(long seed) {
		Random random = new Random(seed);
		int strongest = (int)(random.nextDouble()*3);//makes it more colorful hopefully
		int[] component = new int[3];
		for(int i = 0;i<3;i++) component[i] = (int)(random.nextDouble()*128.0);
		component[strongest]+=127;
		return new Color(component[0],component[1],component[2],128);
	}
	
	void basicPlot(Graphics g,Expr expr) {
		double beforeY = 0;
		
		Equ varDef = equ(var("x"),floatExpr(0));
		ExprList varDefs = new ExprList();
		varDefs.add(varDef);
		
		g.setColor(randomColor(expr.generateHash()));
		for(int i = 0;i<panel.getWidth();i+=2) {
			double x = convertToInternalPositionX(i);
			((FloatExpr)varDef.getRightSide()).value = x;
			double y = convertToExternalPositionY(expr.convertToFloat(varDefs));
			g.drawLine(i-1, (int)beforeY, i, (int)y);
			beforeY = y;
		}
	}
	
	void equPlot(Graphics g,Equ equ,int detail) {
		Equ xDef = equ(var("x"),floatExpr(0)),yDef = equ(var("y"),floatExpr(0));
		ExprList varDefs = new ExprList();
		varDefs.add(xDef);
		varDefs.add(yDef);
		
		g.setColor(randomColor(equ.generateHash()));
		
		for(int i = 0;i<panel.getWidth();i+=detail) {
			for(int j = 0;j<panel.getHeight();j+=detail) {
				((FloatExpr)xDef.getRightSide()).value = convertToInternalPositionX(i);
				((FloatExpr)yDef.getRightSide()).value = convertToInternalPositionY(j);
				double originalRes = equ.getLeftSide().convertToFloat(varDefs)-equ.getRightSide().convertToFloat(varDefs);
				
				boolean diff = false;
				outer:for(int k = -2;k<=2;k+=2) {
					for(int l = -2;l<=2;l+=2) {
						if(l == 0 && k == 0) continue;
						((FloatExpr)xDef.getRightSide()).value = convertToInternalPositionX(i+k);
						((FloatExpr)yDef.getRightSide()).value = convertToInternalPositionY(j+l);
						double testRes = equ.getLeftSide().convertToFloat(varDefs)-equ.getRightSide().convertToFloat(varDefs);
						if((testRes<0) != (originalRes<0)) {
							diff = true;
							break outer;
						}
					}
				}
				if(diff) g.fillRect(i-detail/2, j-detail/2, detail, detail);
			}
		}
	}
	
	BufferedImage graphImage;
	
	void renderGraph(Graphics graphics,DefaultListModel<Expr> stack) {
		if(needsRepaint) {
			graphImage = new BufferedImage(panel.getWidth(),panel.getHeight(),BufferedImage.TYPE_INT_RGB);
			Graphics g = graphImage.createGraphics();
			
			Graphics2D g2 = (Graphics2D)g;
			
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
			
			g2.setStroke(new BasicStroke(4));
			for(int i = 0;i<stack.size();i++) {
				Expr expr = stack.get(i);
				if(expr instanceof Equ) {
					Equ casted = (Equ)expr;
					equPlot(g,casted,4);
				}else {
					basicPlot(g,expr);
				}
			}
			DecimalFormat numberFormat = new DecimalFormat("#.00");
			//draw coordinate lines 1
			for(int k = 0;k<2;k++) {
				
				double scale = Math.pow( 5.0 , Math.floor(Math.log(xMax-xMin)/Math.log( 5.0) )-k);
				g2.setStroke(new BasicStroke(3-k*2));
				g.setColor(Color.GRAY);
				for(double i =  Math.floor(xMin/scale)*scale;i<=xMax;i+=scale) {
					int xLine = convertToExternalPositionX(i);
					g.drawLine(xLine, 0, xLine, panel.getHeight());
					if(k==0) g.drawString(""+numberFormat.format(i), xLine+10,20);
				}
				for(double i = Math.floor(yMin/scale)*scale;i<=yMax;i+=scale) {
					int yLine = convertToExternalPositionY(i);
					g.drawLine(0,yLine, panel.getWidth(), yLine);
					if(k==0) g.drawString(""+numberFormat.format(i),10,yLine-10);
				}
			}
			
			g2.setStroke(new BasicStroke(4));
			//draw x and y axis
			int xAxisLocation = convertToExternalPositionX(0);
			int yAxisLocation = convertToExternalPositionY(0);
			g.setColor(Color.BLACK);
			g.drawLine(xAxisLocation, 0, xAxisLocation, panel.getHeight());
			g.drawLine(0,yAxisLocation, panel.getWidth(), yAxisLocation);
			
			g.dispose();
			needsRepaint = false;
		}
		graphics.drawImage(graphImage,0,0,null);
		graphics.setColor(Color.BLACK);
		graphics.drawString("x: "+(float)convertToInternalPositionX(mouseX), mouseX+40, mouseY);
		graphics.drawString("y: "+(float)convertToInternalPositionY(mouseY), mouseX+40, mouseY+20);
	}
	boolean needsRepaint = true;
	public Plot(DefaultListModel<Expr> stack) {
		panel = new JPanel() {
			private static final long serialVersionUID = 4523041779264530987L;

			@Override
			public void paintComponent(Graphics g) {
				renderGraph(g,stack);
			}
		};
		
		JFrame window = new JFrame("plot");
		window.setSize(600, 600);
		window.add(panel);
		window.setVisible(true);
		panel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() != MouseEvent.BUTTON1) window.setVisible(false);
				mousePressed = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		panel.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double scrollAmount = e.getPreciseWheelRotation();
				double slower = 1.0/25.0;
				xMin = xMin+(convertToInternalPositionX(mouseX)-xMin)*(scrollAmount*slower);
				xMax = xMax-(xMax-convertToInternalPositionX(mouseX))*(scrollAmount*slower);
				
				yMin = yMin+(convertToInternalPositionY(mouseY)-yMin)*(scrollAmount*slower);
				yMax = yMax-(yMax-convertToInternalPositionY(mouseY))*(scrollAmount*slower);
				needsRepaint = true;
			}
			
		});
		ListDataListener dataListener = new ListDataListener() {

			@Override
			public void intervalAdded(ListDataEvent e) {
				needsRepaint = true;
				panel.repaint();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				needsRepaint = true;
				panel.repaint();
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				needsRepaint = true;
				panel.repaint();
				
			}
			
		};
		stack.addListDataListener(dataListener);
		new Thread() {
			@Override
			public void run() {
				try {
					while(true) {
						sleep(1000/60);
						Point mousePosition = panel.getMousePosition();
						if(mousePosition != null) {
							pMouseX = mouseX;
							pMouseY = mouseY;
							mouseX = mousePosition.x;
							mouseY = mousePosition.y;
							
							if(mousePressed) {
								double panX = (pMouseX-mouseX)/(double)panel.getWidth()*(xMax-xMin);
								double panY = (pMouseY-mouseY)/(double)panel.getHeight()*(yMax-yMin);
								xMin+=panX;
								xMax+=panX;
								yMin-=panY;
								yMax-=panY;
								needsRepaint = true;
							}
							panel.repaint();
						}
						if(!window.isVisible()) break;
					}
				}catch(Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				stack.removeListDataListener(dataListener);
			}
		}.start();
	}
}
