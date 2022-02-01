package graphics;
import cas.*;
import ui.StackEditor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;

public class Plot extends JPanel{
	private static final long serialVersionUID = 2751307762529372797L;

	public PlotWindowParams plotParams = new PlotWindowParams();
	
	int mouseX,mouseY,pMouseX,pMouseY;
	boolean mousePressed;
	private Color backgroundColor = Color.DARK_GRAY,foregroundColor = Color.LIGHT_GRAY;
	StackEditor stackEditor;
	public static final int MODE_2D = 0,MODE_3D = 1;
	public int mode = MODE_2D;
	
	public static class PlotWindowParams{
		
		public double xMin,xMax,yMin,yMax,zMin,zMax,zRot,xRot;
		public PlotWindowParams() {
			xMin = -10;
			xMax = 10;
			yMin = -10;
			yMax = 10;
			zMin = -10;
			zMax = 10;
		}
		public PlotWindowParams(double xMin,double xMax,double yMin,double yMax) {
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
		}
		public PlotWindowParams(double xMin,double xMax,double yMin,double yMax,double zMin,double zMax) {
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
			this.zMin = zMin;
			this.zMax = zMax;
		}
		
		public void set(double xMin,double xMax,double yMin,double yMax,double zMin,double zMax) {
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
			this.zMin = zMin;
			this.zMax = zMax;
		}
		
		public void set(double xMin,double xMax,double yMin,double yMax) {
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
		for(int i = 0;i<3;i++) component[i] = (int)(random.nextDouble()*180.0);
		component[strongest]= 180;
		return new Color(component[0],component[1],component[2]);
	}
	
	
	static int IN_TERMS_OF_X = 0,IN_TERMS_OF_Y = 1;
	
	static void basicPlot2D(Graphics g,Expr expr,int mode,PlotWindowParams plotWindowParams,Dimension windowSize) {//plots basic functions in terms  of x
		if(mode == IN_TERMS_OF_X) {
			double beforeY = 0;
			Equ varDef = QuickMath.equ(QuickMath.var("x"),QuickMath.floatExpr(0));
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
			Equ varDef = QuickMath.equ(QuickMath.var("y"),QuickMath.floatExpr(0));
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
	
	static void equPlot2D(Graphics g,Equ equ,int detail,PlotWindowParams plotWindowParams,Dimension windowSize) {//plots equations with x and y
		Equ xDef = QuickMath.equ(QuickMath.var("x"),QuickMath.floatExpr(0)),yDef = QuickMath.equ(QuickMath.var("y"),QuickMath.floatExpr(0));
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
	
	static void renderPlots2D(Graphics g,ExprList stack,PlotWindowParams plotWindowParams,Dimension windowSize){//renders all the lines and geometry
		for(int i = 0;i<stack.size();i++) {
			Expr expr = stack.get(i);
			if(expr instanceof Equ) {
				Equ casted = (Equ)expr;
				
				if(casted.getLeftSide().equals(y)) {
					basicPlot2D(g,casted.getRightSide(),IN_TERMS_OF_X,plotWindowParams,windowSize);
				}else if(casted.getLeftSide().equals(x)) {
					basicPlot2D(g,casted.getRightSide(),IN_TERMS_OF_Y,plotWindowParams,windowSize);
				}else {
					equPlot2D(g,casted,4,plotWindowParams,windowSize);
				}
			}else if(expr instanceof ExprList){
				ExprList subList = new ExprList();
				for(int j = 0;j<expr.size();j++) {
					subList.add( expr.get(j));
				}
				renderPlots2D(g,subList,plotWindowParams,windowSize);
			}else {
				basicPlot2D(g,expr,IN_TERMS_OF_X,plotWindowParams,windowSize);
			}
		}
	}
	
	public static BufferedImage renderGraph2D(ExprList stack,PlotWindowParams plotWindowParams,Dimension windowSize,Color backgroundColor,Color foregroundColor) {
		BufferedImage out = new BufferedImage((int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		Graphics g = out.createGraphics();
		Graphics2D g2 = (Graphics2D)g;
		
		g.setColor(backgroundColor);
		g.fillRect(0, 0, (int)windowSize.getWidth(), (int)windowSize.getHeight());
		
		g2.setStroke(new BasicStroke(4));
		
		
		renderPlots2D(g,stack,plotWindowParams,windowSize);
		DecimalFormat numberFormat = new DecimalFormat("#.000");
		
		g.setColor(foregroundColor);
		//draw coordinate lines 1
		for(int k = 0;k<2;k++) {
			
			double scale = Math.pow( 5.0 , Math.floor(Math.log(plotWindowParams.xMax-plotWindowParams.xMin)/Math.log( 5.0) )-k);
			if(k == 0) g2.setStroke(new BasicStroke(2));
			if(k == 1) g2.setStroke(new BasicStroke(1));
			for(double i =  Math.floor(plotWindowParams.xMin/scale)*scale;i<=plotWindowParams.xMax;i+=scale) {
				int xLine = convertToExternalPositionX(i,plotWindowParams,windowSize);
				g.drawLine(xLine, 0, xLine, (int)windowSize.getHeight());
				if(k==0) g.drawString(""+numberFormat.format(i), xLine+10,20);
			}
			for(double i = Math.floor(plotWindowParams.yMin/scale)*scale;i<=plotWindowParams.yMax;i+=scale) {
				int yLine = convertToExternalPositionY(i,plotWindowParams,windowSize);
				g.drawLine(0,yLine, (int)windowSize.getWidth(), yLine);
				if(k==0) g.drawString(""+numberFormat.format(i),10,yLine-10);
			}
		}
		
		g2.setStroke(new BasicStroke(4));
		//draw x and y axis
		int xAxisLocation = convertToExternalPositionX(0,plotWindowParams,windowSize);
		int yAxisLocation = convertToExternalPositionY(0,plotWindowParams,windowSize);
		g.drawLine(xAxisLocation, 0, xAxisLocation, (int)windowSize.getHeight());
		g.drawLine(0,yAxisLocation, (int)windowSize.getWidth(), yAxisLocation);
		
		g.dispose();
		
		return out;
	}
	
	static class Point{
		double x,y,z;
		Point(double x,double y,double z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	static class Quad implements Comparable<Quad>{
		int[] x,z;
		double y;
		Color color;
		Quad(int[] x,int[] z,double y,Color color) {
			this.x = x;
			this.z = z;
			this.y = y;
			this.color = color;
		}
		@Override
		public int compareTo(Quad o) {
			return Double.compare(y, o.y);
		}
		public void render(Graphics g) {
			g.setColor(color);
			g.fillPolygon(x, z, 4);
		}
	}
	
	public static BufferedImage renderGraph3D(ExprList stack,PlotWindowParams plotWindowParams,Dimension windowSize,Color backgroundColor,Color foregroundColor,int resolution) {
		BufferedImage out = new BufferedImage((int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		Graphics g = out.createGraphics();
		
		g.setColor(backgroundColor);
		g.fillRect(0, 0, (int)windowSize.getWidth(), (int)windowSize.getHeight());
		
		if(stack.size()>0) {
			
			g.setColor(foregroundColor);
			
			Equ xDef = QuickMath.equ(QuickMath.var("x"),QuickMath.floatExpr(0)),yDef = QuickMath.equ(QuickMath.var("y"),QuickMath.floatExpr(0));
			ExprList varDefs = new ExprList();
			varDefs.add(xDef);
			varDefs.add(yDef);
			
			//generate points
			int xRes = resolution,yRes = resolution;
			Point[][] points = new Point[yRes][xRes];
			
			Expr function = stack.get();
			//calculate function
			for(int yIndex = 0;yIndex < yRes;yIndex++) {
				double y = (plotWindowParams.yMax-plotWindowParams.yMin)*((double)yIndex/(yRes-1))+plotWindowParams.yMin;
				for(int xIndex = 0;xIndex < xRes;xIndex++) {
					double x = (plotWindowParams.xMax-plotWindowParams.xMin)*((double)xIndex/(xRes-1))+plotWindowParams.xMin;
					
					
					((FloatExpr)xDef.getRightSide()).value.set(new ComplexFloat(x,0));
					((FloatExpr)yDef.getRightSide()).value.set(new ComplexFloat(y,0));
					
					double z = function.convertToFloat(varDefs).real;
					points[yIndex][xIndex] = new Point(x,y,z);
					
				}
				
			}
			
			//rotational transformation and scaling
			double cosZ = Math.cos(plotWindowParams.zRot);
			double sinZ = Math.sin(plotWindowParams.zRot);
			
			double cosX = Math.cos(plotWindowParams.xRot);
			double sinX = Math.sin(plotWindowParams.xRot);
			g.translate(windowSize.width/2, windowSize.height/2);
			for(int yIndex = 0 ;yIndex < yRes;yIndex++) {
				for(int xIndex = 0;xIndex < xRes;xIndex++) {
					double xPos = points[yIndex][xIndex].x/(plotWindowParams.xMax-plotWindowParams.xMin);
					double yPos = points[yIndex][xIndex].y/(plotWindowParams.yMax-plotWindowParams.yMin);
					double zPos = (-points[yIndex][xIndex].z)/(plotWindowParams.zMax-plotWindowParams.zMin);
					
					double newXPos = cosZ*xPos-sinZ*yPos;
					double newYPos = cosZ*yPos+sinZ*xPos;
					
					xPos = newXPos;
					yPos = newYPos;
					
					
					newYPos = cosX*yPos-sinX*zPos;
					double newZPos = cosX*zPos+sinX*yPos;
					
					yPos = newYPos;
					zPos = newZPos;
					
					yPos+=3.5;//shift
					
					Point p = points[yIndex][xIndex];
					
					p.x = 2*windowSize.width*xPos/yPos;
					p.y = yPos;
					p.z = 2*windowSize.width*zPos/yPos;
				}
			}

			ArrayList<Quad> quads = new ArrayList<Quad>();
			
			//create triangles
			for(int yIndex = 0 ;yIndex < yRes-1;yIndex++) {
				for(int xIndex = 0;xIndex < xRes-1;xIndex++) {
					
					Point p1 = points[yIndex][xIndex];
					Point p2 = points[yIndex+1][xIndex];
					Point p3 = points[yIndex+1][xIndex+1];
					Point p4 = points[yIndex][xIndex+1];
					
					int brightness = (int)(((double)xIndex/xRes)*200.0+55.0);
					Color color = new Color( brightness,brightness,brightness/2 );
					
					if((xIndex+yIndex)%2==0) color = color.darker();
					
					if(p1.y>0 && p2.y>0 && p3.y>0 && p4.y>0) {
						quads.add(new Quad(new int[]{(int) p1.x,(int) p2.x,(int) p3.x,(int) p4.x}, new int[]{(int) p1.z,(int) p2.z,(int) p3.z,(int) p4.z}, p1.y+p3.y ,color));
					}
					
				}
			}
			
			Collections.sort(quads);
			
			for(int i = quads.size()-1;i>=0;i--) {
				Quad q = quads.get(i);
				q.render(g);
			}
			
		}
		
		g.dispose();
		return out;
	}
	
	private static Var y = QuickMath.var("y"),x = QuickMath.var("x");
	void renderGraphWithMouse(Graphics graphics,ExprList stack,int mode) {//everything, the background the plots
		BufferedImage graphImage = null;
		if(mode == MODE_2D) {
			graphImage = renderGraph2D(stack,plotParams,getSize(),backgroundColor,foregroundColor);
		
			graphics.drawImage(graphImage,0,0,null);
			graphics.setColor(foregroundColor);
			graphics.drawString("x: "+(float)convertToInternalPositionX(mouseX,plotParams,getSize()), mouseX+40, mouseY);
			graphics.drawString("y: "+(float)convertToInternalPositionY(mouseY,plotParams,getSize()), mouseX+40, mouseY+20);
		}else if(mode == MODE_3D) {
			graphImage = renderGraph3D(stack,plotParams,getSize(),backgroundColor,foregroundColor,48);
			graphics.drawImage(graphImage,0,0,null);
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		renderGraphWithMouse(g,stackEditor.stack,mode);
	}
	
	public Plot(StackEditor stackEditor) {
		this.stackEditor = stackEditor;
		
		setMinimumSize(new Dimension(200,200));
		
		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
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
		addMouseMotionListener(new MouseMotionListener() {

			void update(MouseEvent e) {
				pMouseX = mouseX;
				pMouseY = mouseY;
				mouseX = e.getX();
				mouseY = e.getY();
				
				if(mousePressed) {
					if(mode == MODE_2D) {
						double panX = (pMouseX-mouseX)/(double)getWidth()*(plotParams.xMax-plotParams.xMin);
						double panY = (pMouseY-mouseY)/(double)getHeight()*(plotParams.yMax-plotParams.yMin);
						plotParams.xMin+=panX;
						plotParams.xMax+=panX;
						plotParams.yMin-=panY;
						plotParams.yMax-=panY;
					}else if(mode == MODE_3D) {
						plotParams.zRot-=((double)pMouseX-mouseX)/100.0;
						plotParams.xRot+=((double)pMouseY-mouseY)/100.0;
					}
				}
				
				repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				update(e);
				
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				update(e);
			}
			
		});
		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double scrollAmount = e.getPreciseWheelRotation();
				double slower = 1.0/25.0;
				if(mode == MODE_2D) {
					plotParams.xMin = plotParams.xMin+(convertToInternalPositionX(mouseX,plotParams,getSize())-plotParams.xMin)*(scrollAmount*slower);
					plotParams.xMax = plotParams.xMax-(plotParams.xMax-convertToInternalPositionX(mouseX,plotParams,getSize()))*(scrollAmount*slower);
					
					plotParams.yMin = plotParams.yMin+(convertToInternalPositionY(mouseY,plotParams,getSize())-plotParams.yMin)*(scrollAmount*slower);
					plotParams.yMax = plotParams.yMax-(plotParams.yMax-convertToInternalPositionY(mouseY,plotParams,getSize()))*(scrollAmount*slower);
				}else if(mode == MODE_3D) {
					plotParams.xMin*=(1.0-scrollAmount*slower);
					plotParams.xMax*=(1.0-scrollAmount*slower);
					
					plotParams.yMin*=(1.0-scrollAmount*slower);
					plotParams.yMax*=(1.0-scrollAmount*slower);
					
					plotParams.zMin*=(1.0-scrollAmount*slower);
					plotParams.zMax*=(1.0-scrollAmount*slower);
					
				}
				
				repaint();
			}
			
		});
		
	}
	
	public void setBackgroundColor(Color color) {
		backgroundColor = color;
	}
	public void setForegroundColor(Color color) {
		foregroundColor = color;
	}
}
