package graphics;
import cas.*;
import ui.StackEditor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class Plot extends QuickMath{
	PlotWindowParams plotParams = new PlotWindowParams();
	Dimension windowSize = new Dimension(600,600);
	
	int mouseX,mouseY,pMouseX,pMouseY;
	boolean mousePressed;
	JPanel panel = null;
	
	public static class PlotWindowParams{
		
		double xMin,xMax,yMin,yMax;
		public PlotWindowParams() {
			xMin = -10;
			xMax = 10;
			yMin = -10;
			yMax = 10;
		}
		public PlotWindowParams(double xMin,double xMax,double yMin,double yMax) {
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
		}
	}
	
	static double convertToInternalPositionX(int external,PlotWindowParams plotWindowParams,Dimension windowSize) {
		return ((external)/windowSize.getWidth())*(plotWindowParams.xMax-plotWindowParams.xMin)+plotWindowParams.xMin;
	}
	static double convertToInternalPositionY(int external,PlotWindowParams plotWindowParams,Dimension windowSize) {
		return ((windowSize.getHeight()-external)/windowSize.getHeight())*(plotWindowParams.yMax-plotWindowParams.yMin)+plotWindowParams.yMin;
	}
	static int convertToExternalPositionY(double internal,PlotWindowParams plotWindowParams,Dimension windowSize) {
		if(Double.isNaN(internal)) internal = 0;
		return (int)(windowSize.getHeight()-(internal-plotWindowParams.yMin)/(plotWindowParams.yMax-plotWindowParams.yMin)*windowSize.getHeight());
	}
	static int convertToExternalPositionX(double internal,PlotWindowParams plotWindowParams,Dimension windowSize) {
		return (int)((internal-plotWindowParams.xMin)/(plotWindowParams.xMax-plotWindowParams.xMin)*windowSize.getWidth());
	}
	
	static Color randomColor(int seed) {
		Random random = new Random(seed);
		int strongest = (int)(random.nextDouble()*3);//makes it more colorful hopefully
		int[] component = new int[3];
		for(int i = 0;i<3;i++) component[i] = (int)(random.nextDouble()*256.0);
		component[strongest]= 255;
		return new Color(component[0],component[1],component[2]);
	}
	
	
	static int IN_TERMS_OF_X = 0,IN_TERMS_OF_Y = 1;
	
	static void basicPlot(Graphics g,Expr expr,int mode,PlotWindowParams plotWindowParams,Dimension windowSize) {//plots basic functions in terms  of x
		if(mode == IN_TERMS_OF_X) {
			double beforeY = 0;
			Equ varDef = equ(var("x"),floatExpr(0));
			ExprList varDefs = new ExprList();
			varDefs.add(varDef);
			
			g.setColor(randomColor(expr.hashCode()));
			for(int i = 0;i<windowSize.getWidth();i+=2) {
				double x = convertToInternalPositionX(i,plotWindowParams,windowSize);
				((FloatExpr)varDef.getRightSide()).value.set(new ComplexFloat(x,0));
				double y = convertToExternalPositionY(expr.convertToFloat(varDefs).real ,plotWindowParams,windowSize);
				g.drawLine(i-1, (int)beforeY, i, (int)y);
				beforeY = y;
			}
		}else if(mode == IN_TERMS_OF_Y){
			double beforeX = 0;
			Equ varDef = equ(var("y"),floatExpr(0));
			ExprList varDefs = new ExprList();
			varDefs.add(varDef);
			g.setColor(randomColor(expr.hashCode()));
			for(int i = 0;i<windowSize.getHeight();i+=2) {
				double y = convertToInternalPositionY(i,plotWindowParams,windowSize);
				((FloatExpr)varDef.getRightSide()).value.set(new ComplexFloat(y,0));
				double x = convertToExternalPositionX(expr.convertToFloat(varDefs).real ,plotWindowParams,windowSize);
				g.drawLine((int)beforeX,i-1, (int)x,i);
				beforeX = x;
			}
		}
	}
	
	static void equPlot(Graphics g,Equ equ,int detail,PlotWindowParams plotWindowParams,Dimension windowSize) {//plots equations with x and y
		Equ xDef = equ(var("x"),floatExpr(0)),yDef = equ(var("y"),floatExpr(0));
		ExprList varDefs = new ExprList();
		varDefs.add(xDef);
		varDefs.add(yDef);
		
		g.setColor(randomColor(equ.hashCode()));
		
		for(int i = 0;i<windowSize.getWidth();i+=detail) {
			for(int j = 0;j<windowSize.getHeight();j+=detail) {
				((FloatExpr)xDef.getRightSide()).value.set(new ComplexFloat(convertToInternalPositionX(i,plotWindowParams,windowSize),0));
				((FloatExpr)yDef.getRightSide()).value.set(new ComplexFloat(convertToInternalPositionY(j,plotWindowParams,windowSize),0));
				double originalRes = equ.getLeftSide().convertToFloat(varDefs).real-equ.getRightSide().convertToFloat(varDefs).real;
				
				boolean diff = false;
				outer:for(int k = -2;k<=2;k+=2) {
					for(int l = -2;l<=2;l+=2) {
						if(l == 0 && k == 0) continue;
						((FloatExpr)xDef.getRightSide()).value.set(new ComplexFloat(convertToInternalPositionX(i+k,plotWindowParams,windowSize),0));
						((FloatExpr)yDef.getRightSide()).value.set(new ComplexFloat(convertToInternalPositionY(j+l,plotWindowParams,windowSize),0));
						double testRes = equ.getLeftSide().convertToFloat(varDefs).real-equ.getRightSide().convertToFloat(varDefs).real;
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
	
	static void renderPlots(Graphics g,ArrayList<Expr> stack,PlotWindowParams plotWindowParams,Dimension windowSize){//renders all the lines and geometry
		for(int i = 0;i<stack.size();i++) {
			Expr expr = stack.get(i);
			if(expr instanceof Equ) {
				Equ casted = (Equ)expr;
				
				if(casted.getLeftSide().equals(y)) {
					basicPlot(g,casted.getRightSide(),IN_TERMS_OF_X,plotWindowParams,windowSize);
				}else if(casted.getLeftSide().equals(x)) {
					basicPlot(g,casted.getRightSide(),IN_TERMS_OF_Y,plotWindowParams,windowSize);
				}else {
					equPlot(g,casted,4,plotWindowParams,windowSize);
				}
			}else if(expr instanceof ExprList){
				ArrayList<Expr> subList = new ArrayList<Expr>();
				for(int j = 0;j<expr.size();j++) {
					subList.add( expr.get(j));
				}
				renderPlots(g,subList,plotWindowParams,windowSize);
			}else {
				basicPlot(g,expr,IN_TERMS_OF_X,plotWindowParams,windowSize);
			}
		}
	}
	
	public static BufferedImage renderGraph(ArrayList<Expr> stack,PlotWindowParams plotWindowParams,Dimension windowSize) {
		BufferedImage out = new BufferedImage((int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		Graphics g = out.createGraphics();
		Graphics2D g2 = (Graphics2D)g;
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, (int)windowSize.getWidth(), (int)windowSize.getHeight());
		
		g2.setStroke(new BasicStroke(4));
		
		
		renderPlots(g,stack,plotWindowParams,windowSize);
		DecimalFormat numberFormat = new DecimalFormat("#.00");
		//draw coordinate lines 1
		for(int k = 0;k<2;k++) {
			
			double scale = Math.pow( 5.0 , Math.floor(Math.log(plotWindowParams.xMax-plotWindowParams.xMin)/Math.log( 5.0) )-k);
			g2.setStroke(new BasicStroke(3-k*2));
			for(double i =  Math.floor(plotWindowParams.xMin/scale)*scale;i<=plotWindowParams.xMax;i+=scale) {
				int xLine = convertToExternalPositionX(i,plotWindowParams,windowSize);
				g.setColor(Color.GRAY);
				g.drawLine(xLine, 0, xLine, (int)windowSize.getHeight());
				g.setColor(Color.WHITE);
				if(k==0) g.drawString(""+numberFormat.format(i), xLine+10,20);
			}
			for(double i = Math.floor(plotWindowParams.yMin/scale)*scale;i<=plotWindowParams.yMax;i+=scale) {
				int yLine = convertToExternalPositionY(i,plotWindowParams,windowSize);
				g.setColor(Color.GRAY);
				g.drawLine(0,yLine, (int)windowSize.getWidth(), yLine);
				g.setColor(Color.WHITE);
				if(k==0) g.drawString(""+numberFormat.format(i),10,yLine-10);
			}
		}
		
		g2.setStroke(new BasicStroke(4));
		//draw x and y axis
		int xAxisLocation = convertToExternalPositionX(0,plotWindowParams,windowSize);
		int yAxisLocation = convertToExternalPositionY(0,plotWindowParams,windowSize);
		g.setColor(Color.white);
		g.drawLine(xAxisLocation, 0, xAxisLocation, (int)windowSize.getHeight());
		g.drawLine(0,yAxisLocation, (int)windowSize.getWidth(), yAxisLocation);
		
		g.dispose();
		
		return out;
	}
	
	public static BufferedImage renderGraph(Expr e,PlotWindowParams plotWindowParams,Dimension windowSize) {
		ArrayList<Expr> stack = new ArrayList<Expr>();
		stack.add(e);
		return renderGraph(stack,plotWindowParams,windowSize);
	}
	
	BufferedImage graphImage;
	
	private static Var y = var("y"),x = var("x");
	void renderGraph(Graphics graphics,ArrayList<Expr> stack) {//everything, the background the plots
		if(needsRepaint) {
			graphImage = renderGraph(stack,plotParams,windowSize);
			needsRepaint = false;
		}
		graphics.drawImage(graphImage,0,0,null);
		
		graphics.setColor(Color.WHITE);
		graphics.drawString("x: "+(float)convertToInternalPositionX(mouseX,plotParams,windowSize), mouseX+40, mouseY);
		graphics.drawString("y: "+(float)convertToInternalPositionY(mouseY,plotParams,windowSize), mouseX+40, mouseY+20);
	}
	
	void createSliders(StackEditor stackEditor,JPanel slidersContainer) {
		for(int i = 0;i<stackEditor.currentDefs.varsArrayList.size();i++) {
			String name = ((Var)stackEditor.currentDefs.varsArrayList.get(i).getLeftSide()).name;
			JLabel label = new JLabel( name );
			slidersContainer.add(label);
			
			JSlider sliderTest = new JSlider(JSlider.HORIZONTAL,-10,10,0);
			sliderTest.setMajorTickSpacing(5);
			sliderTest.setMinorTickSpacing(1);
			sliderTest.setPaintTicks(true);
			sliderTest.setPaintLabels(true);
			sliderTest.setSnapToTicks(true);
			slidersContainer.add(sliderTest);
			
			sliderTest.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					stackEditor.currentDefs.changeVar(name, new FloatExpr(sliderTest.getValue()));
					needsRepaint = true;
					panel.repaint();
				}
				
			});
			
		}
	}
	
	volatile boolean needsRepaint = true;
	public Plot(StackEditor stackEditor) {
		panel = new JPanel() {
			private static final long serialVersionUID = 4523041779264530987L;

			@Override
			public void paintComponent(Graphics g) {
				ArrayList<Expr> stackClone = new ArrayList<Expr>();
				for(int i = 0;i<stackEditor.stack.size();i++) {
					stackClone.add(stackEditor.stack.get(i));
				}
				renderGraph(g,stackClone);
			}
		};
		
		JFrame window = new JFrame("plot");
		window.setSize(windowSize);
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
				plotParams.xMin = plotParams.xMin+(convertToInternalPositionX(mouseX,plotParams,windowSize)-plotParams.xMin)*(scrollAmount*slower);
				plotParams.xMax = plotParams.xMax-(plotParams.xMax-convertToInternalPositionX(mouseX,plotParams,windowSize))*(scrollAmount*slower);
				
				plotParams.yMin = plotParams.yMin+(convertToInternalPositionY(mouseY,plotParams,windowSize)-plotParams.yMin)*(scrollAmount*slower);
				plotParams.yMax = plotParams.yMax-(plotParams.yMax-convertToInternalPositionY(mouseY,plotParams,windowSize))*(scrollAmount*slower);
				needsRepaint = true;
			}
			
		});
		ListDataListener dataListener = new ListDataListener() {

			void reset() {
				needsRepaint = true;
				panel.repaint();
			}
			
			@Override
			public void intervalAdded(ListDataEvent e) {
				reset();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				reset();
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				reset();
			}
			
		};
		stackEditor.stack.addListDataListener(dataListener);
		stackEditor.currentDefs.functionsArrayList.addListDataListener(dataListener);
		stackEditor.currentDefs.varsArrayList.addListDataListener(dataListener);
		new Thread() {
			@Override
			public void run() {
				try {
					while(true) {
						sleep(1000/60);
						Point mousePosition = panel.getMousePosition();
						if(mousePosition != null) {
							windowSize = window.getSize();
							pMouseX = mouseX;
							pMouseY = mouseY;
							mouseX = mousePosition.x;
							mouseY = mousePosition.y;
							
							if(mousePressed) {
								double panX = (pMouseX-mouseX)/(double)panel.getWidth()*(plotParams.xMax-plotParams.xMin);
								double panY = (pMouseY-mouseY)/(double)panel.getHeight()*(plotParams.yMax-plotParams.yMin);
								plotParams.xMin+=panX;
								plotParams.xMax+=panX;
								plotParams.yMin-=panY;
								plotParams.yMax-=panY;
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
				window.dispose();
				stackEditor.stack.removeListDataListener(dataListener);
				stackEditor.currentDefs.functionsArrayList.removeListDataListener(dataListener);
				stackEditor.currentDefs.varsArrayList.removeListDataListener(dataListener);
			}
		}.start();
	}
}
