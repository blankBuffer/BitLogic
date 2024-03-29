package cas.graphics;
import cas.*;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.primitive.*;
import cas.programming.StackEditor;

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
	
	int mouseX,mouseY,pMouseX,pMouseY,startMouseX,startMouseY;
	private boolean mousePressed = false;
	private Color backgroundColor = Color.WHITE,foregroundColor = Color.DARK_GRAY;
	StackEditor stackEditor;
	public static final int MODE_2D = 0,MODE_3D = 1,MODE_COMPLEX = 2;
	public int mode = MODE_2D;
	public double scrollSpeed = 0.15;
	
	public static class PlotWindowParams{
		
		public double xMin,xMax,yMin,yMax,zMin,zMax,zRot,xRot;
		public PlotWindowParams() {
			xMin = -10;
			xMax = 10;
			yMin = -10;
			yMax = 10;
			zMin = -10;
			zMax = 10;
			zRot = -0.4;
			xRot = -0.4;
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
		int index = (int)(random.nextDouble()*3);//makes it more colorful hopefully
		int[] component = new int[3];
		
		component[index]= 255;
		component[(index+1)%3] = 0;
		component[(index+2)%3] = random.nextInt(256);
		
		return new Color(component[0],component[1],component[2]);
	}
	
	
	static final int IN_TERMS_OF_X = 0,IN_TERMS_OF_Y = 1;
	static final int EQU = 0, GREATER = 1,LESS = -1;
	private static final Var Y = Cas.var("y"),X = Cas.var("x");
	
	static void basicPlot2D(Graphics g,Expr expr,PlotWindowParams plotWindowParams,Dimension windowSize,int varChoice,int equType) {//plots basic functions in terms  of x
		
		int step = 2;
		
		if(varChoice == IN_TERMS_OF_X) {
			double beforeY = 0;
			Func varDefEqu = Cas.equ(Cas.var("x"),Cas.floatExpr(0));
			Func varDefsSet = Cas.exprSet();
			varDefsSet.add(varDefEqu);
			
			double x = convertToInternalPositionX(0,plotWindowParams,windowSize);
			((FloatExpr)Equ.getRightSide(varDefEqu)).value.set(new ComplexFloat(x,0));
			beforeY = convertToExternalPositionY(expr.convertToFloat(varDefsSet).real ,plotWindowParams,windowSize);
			
			for(int i = 0;i<windowSize.getWidth();i+=step) {
				x = convertToInternalPositionX(i,plotWindowParams,windowSize);
				((FloatExpr)Equ.getRightSide(varDefEqu)).value.set(new ComplexFloat(x,0));
				double y = convertToExternalPositionY(expr.convertToFloat(varDefsSet).real ,plotWindowParams,windowSize);
				
				if(equType == EQU) g.drawLine(i-1, (int)beforeY, i, (int)y);
				else if(equType == GREATER) g.fillRect(i-1, 0, step, (int)y);
				else if(equType == LESS) g.fillRect(i-1, (int)y, step, windowSize.height-(int)y);
				
				beforeY = y;
			}
		}else if(varChoice == IN_TERMS_OF_Y){
			double beforeX = 0;
			Func varDefEqu = Cas.equ(Cas.var("y"),Cas.floatExpr(0));
			Func varDefsSet = Cas.exprSet();
			varDefsSet.add(varDefEqu);
			
			for(int i = 0;i<windowSize.getHeight();i+=2) {
				double y = convertToInternalPositionY(i,plotWindowParams,windowSize);
				((FloatExpr)Equ.getRightSide(varDefEqu)).value.set(new ComplexFloat(y,0));
				double x = convertToExternalPositionX(expr.convertToFloat(varDefsSet).real ,plotWindowParams,windowSize);
				
				if(equType == EQU) g.drawLine((int)beforeX,i-1, (int)x,i);
				else if(equType == GREATER) g.fillRect(0,i-1, (int)x,step);
				else if(equType == LESS) g.fillRect((int)x,i-1, windowSize.width-(int)x,step);
				beforeX = x;
			}
		}
	}
	
	static void equPlot2D(Graphics g,Expr expr,PlotWindowParams plotWindowParams,Dimension windowSize,int detail) {//plots equations with x and y
		Func xDefEqu = Cas.equ(Cas.var("x"),Cas.floatExpr(0)),yDefEqu = Cas.equ(Cas.var("y"),Cas.floatExpr(0));
		Func varDefsSet = Cas.exprSet();
		varDefsSet.add(xDefEqu);
		varDefsSet.add(yDefEqu);
		
		Expr leftSide = Algorithms.getLeftSideGeneric(expr);
		Expr rightSide = Algorithms.getRightSideGeneric(expr);
		
		int equType = EQU;
		if(expr.isType("greater")) equType = GREATER;
		if(expr.isType("less")) equType = LESS;
		
		for(int i = 0;i<windowSize.getWidth();i+=detail) {
			for(int j = 0;j<windowSize.getHeight();j+=detail) {
				((FloatExpr)Equ.getRightSide(xDefEqu)).value.set(new ComplexFloat(convertToInternalPositionX(i,plotWindowParams,windowSize),0));
				((FloatExpr)Equ.getRightSide(yDefEqu)).value.set(new ComplexFloat(convertToInternalPositionY(j,plotWindowParams,windowSize),0));
				double originalRes = leftSide.convertToFloat(varDefsSet).real-rightSide.convertToFloat(varDefsSet).real;
				boolean showPixel = false;
				
				if(equType == EQU) {
					boolean diff = false;
					outer:for(int k = -2;k<=2;k+=2) {
						for(int l = -2;l<=2;l+=2) {
							if(l == 0 && k == 0) continue;
							((FloatExpr)Equ.getRightSide(xDefEqu)).value.set(new ComplexFloat(convertToInternalPositionX(i+k,plotWindowParams,windowSize),0));
							((FloatExpr)Equ.getRightSide(yDefEqu)).value.set(new ComplexFloat(convertToInternalPositionY(j+l,plotWindowParams,windowSize),0));
							double testRes = leftSide.convertToFloat(varDefsSet).real-rightSide.convertToFloat(varDefsSet).real;
							if((testRes<0) != (originalRes<0)) {
								diff = true;
								break outer;
							}
						}
					}
					if(diff) showPixel = true;
				}else if(equType == GREATER) {
					if(originalRes>0) {
						 showPixel = true;
					}
				}else if(equType == LESS) {
					if(originalRes<0) {
						 showPixel = true;
					}
				}
				
				if(showPixel) g.fillRect(i-detail/2, j-detail/2, detail, detail);
			}
		}
	}
	
	static void renderPlots2D(Graphics g,Func stackSequence,PlotWindowParams plotWindowParams,Dimension windowSize,int detail){//renders all the lines and geometry
		
		for(int i = 0;i<stackSequence.size();i++) {
			Expr expr = stackSequence.get(i);
			
			if(expr.isType("hide")) continue;
			if(expr.isType("show")) expr = expr.get();
			
			g.setColor(randomColor(expr.hashCode()));
			
			if(expr.isType("equ") || expr.isType("greater") || expr.isType("less")) {
				
				int equType = EQU;
				if(expr.isType("greater")) equType = GREATER;
				if(expr.isType("less")) equType = LESS;
				
				if(Algorithms.getLeftSideGeneric(expr).equals(Y)) {
					basicPlot2D(g,Algorithms.getRightSideGeneric(expr),plotWindowParams,windowSize,IN_TERMS_OF_X,equType);
				}else if(Algorithms.getLeftSideGeneric(expr).equals(X)) {
					basicPlot2D(g,Algorithms.getRightSideGeneric(expr),plotWindowParams,windowSize,IN_TERMS_OF_Y,equType);
				}else {
					equPlot2D(g,expr,plotWindowParams,windowSize,detail);
				}
			}else if(expr.isType("set") || expr.isType("sequence")){
				Func subSequence = Sequence.cast(expr);
				renderPlots2D(g,subSequence,plotWindowParams,windowSize,detail);
			}else if(expr.isType("point")) {
				double x = expr.get(0).convertToFloat(Cas.exprSet()).real;
				double y = expr.get(1).convertToFloat(Cas.exprSet()).real;
				int extX = convertToExternalPositionX(x,plotWindowParams,windowSize );
				int extY = convertToExternalPositionY(y,plotWindowParams,windowSize );
				
				g.fillOval(extX-4, extY-4, 8, 8);
				
			}else {
				basicPlot2D(g,expr,plotWindowParams,windowSize,IN_TERMS_OF_X,EQU);
			}
		}
	}
	
	public static void drawCoordinateLines(Graphics2D g2,PlotWindowParams plotWindowParams,Dimension windowSize,Color foregroundColor) {
		g2.setStroke(new BasicStroke(4));
		
		DecimalFormat numberFormat = new DecimalFormat("#.000");
		
		g2.setColor(foregroundColor);
		//draw coordinate lines
		for(int k = 0;k<2;k++) {
			
			double scale = Math.pow( 5.0 , Math.floor(Math.log(plotWindowParams.xMax-plotWindowParams.xMin)/Math.log( 5.0) )-k);
			if(k == 0) g2.setStroke(new BasicStroke(2));
			if(k == 1) g2.setStroke(new BasicStroke(1));
			for(double i =  Math.floor(plotWindowParams.xMin/scale)*scale;i<=plotWindowParams.xMax;i+=scale) {
				int xLine = convertToExternalPositionX(i,plotWindowParams,windowSize);
				g2.drawLine(xLine, 0, xLine, (int)windowSize.getHeight());
				if(k==0) g2.drawString(""+numberFormat.format(i), xLine+10,20);
			}
			for(double i = Math.floor(plotWindowParams.yMin/scale)*scale;i<=plotWindowParams.yMax;i+=scale) {
				int yLine = convertToExternalPositionY(i,plotWindowParams,windowSize);
				g2.drawLine(0,yLine, (int)windowSize.getWidth(), yLine);
				if(k==0) g2.drawString(""+numberFormat.format(i),10,yLine-10);
			}
		}
		
		g2.setStroke(new BasicStroke(4));
		//draw x and y axis
		int xAxisLocation = convertToExternalPositionX(0,plotWindowParams,windowSize);
		int yAxisLocation = convertToExternalPositionY(0,plotWindowParams,windowSize);
		g2.drawLine(xAxisLocation, 0, xAxisLocation, (int)windowSize.getHeight());
		g2.drawLine(0,yAxisLocation, (int)windowSize.getWidth(), yAxisLocation);
	}
	
	public static BufferedImage renderGraph2D(Func stackSequence,PlotWindowParams plotWindowParams,Dimension windowSize,Color backgroundColor,Color foregroundColor,int detail) {
		BufferedImage out = new BufferedImage((int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		Graphics g = out.createGraphics();
		Graphics2D g2 = (Graphics2D)g;
		
		g.setColor(backgroundColor);
		g.fillRect(0, 0, (int)windowSize.getWidth(), (int)windowSize.getHeight());
		
		g2.setStroke(new BasicStroke(4));
		
		
		renderPlots2D(g,stackSequence,plotWindowParams,windowSize,detail);
		
		drawCoordinateLines(g2,plotWindowParams,windowSize,foregroundColor);
		
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
	
	public static BufferedImage renderGraph3D(Func stackSequence,PlotWindowParams plotWindowParams,Dimension windowSize,Color backgroundColor,Color foregroundColor,int resolution) {
		BufferedImage out = new BufferedImage((int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		Graphics g = out.createGraphics();
		
		g.setColor(backgroundColor);
		g.fillRect(0, 0, (int)windowSize.getWidth(), (int)windowSize.getHeight());
		
		if(stackSequence.size()>0) {
			
			g.setColor(foregroundColor);
			
			Func xDefEqu = Cas.equ(Cas.var("x"),Cas.floatExpr(0)),yDefEqu = Cas.equ(Cas.var("y"),Cas.floatExpr(0));
			Func varDefsSet = Cas.exprSet();
			varDefsSet.add(xDefEqu);
			varDefsSet.add(yDefEqu);
			
			//generate points
			int xRes = resolution,yRes = resolution;
			Point[][] points = new Point[yRes][xRes];
			
			Expr function = stackSequence.get(stackSequence.size()-1);
			//calculate function
			for(int yIndex = 0;yIndex < yRes;yIndex++) {
				double y = (plotWindowParams.yMax-plotWindowParams.yMin)*((double)yIndex/(yRes-1))+plotWindowParams.yMin;
				for(int xIndex = 0;xIndex < xRes;xIndex++) {
					double x = (plotWindowParams.xMax-plotWindowParams.xMin)*((double)xIndex/(xRes-1))+plotWindowParams.xMin;
					
					
					((FloatExpr)Equ.getRightSide(xDefEqu)).value.set(new ComplexFloat(x,0));
					((FloatExpr)Equ.getRightSide(yDefEqu)).value.set(new ComplexFloat(y,0));
					
					double z = function.convertToFloat(varDefsSet).real;
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
					
					yPos+=3.5;//shift away from camera
					
					Point p = points[yIndex][xIndex];
					
					p.x = 2*windowSize.width*xPos/yPos;//points further away get closer to horizon point
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
					
					int brightness = (int) Math.min(Math.max(128.0-(p1.y-3.5)*256.0, 64),255);
					Color color = new Color( brightness,brightness,brightness );
					
					if((xIndex/2+yIndex/2)%2==0) color = color.darker();
					
					if(p1.y>0 && p2.y>0 && p3.y>0 && p4.y>0) {
						quads.add(new Quad(new int[]{(int) p1.x,(int) p2.x,(int) p3.x,(int) p4.x}, new int[]{(int) p1.z,(int) p2.z,(int) p3.z,(int) p4.z}, Math.max(Math.max(p1.y,p2.y),p3.y) ,color));
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
	
	public static BufferedImage renderGraphComplex(Func stackSequence,PlotWindowParams plotWindowParams,Dimension windowSize,int detail) {
		BufferedImage out = new BufferedImage((int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics g = out.createGraphics();
		
		if(stackSequence.size() > 0) {

			Expr expr = stackSequence.get(stackSequence.size()-1);
			
			Func varDefEqu = Cas.equ(Cas.var("z"),Cas.floatExpr(0));
			Func varDefsSet = Cas.exprSet();
			varDefsSet.add(varDefEqu);
			
			for(int pixelX = 0;pixelX < windowSize.width;pixelX+=detail) {
				for(int pixelY = 0;pixelY < windowSize.height;pixelY+=detail) {
					
					double real = convertToInternalPositionX(pixelX,plotWindowParams,windowSize);
					double imag = convertToInternalPositionY(pixelY,plotWindowParams,windowSize);
					
					((FloatExpr)Equ.getRightSide(varDefEqu)).value.set(new ComplexFloat(real,imag));
					ComplexFloat result = expr.convertToFloat(varDefsSet);
					
					double mag = ComplexFloat.mag(result).real;
					double angle = ComplexFloat.angle(result).real;
					
					{//squeeze
						mag = mag/(mag+1.0);
						angle = (angle%(2*Math.PI))/(2*Math.PI);
					}
					
					Color pixelColor = Color.getHSBColor((float)angle, 1.0f, (float)mag);
					g.setColor(pixelColor);
					g.fillRect(pixelX, pixelY, detail, detail);
					
				}
			}
		}
		g.dispose();
		return out;
	}
	
	BufferedImage graphImage = null;//cached plot image so it does not redraw when nothing changes
	int lastGraphHash = 0;
	
	void renderGraphWithMouse(Graphics graphics,Func stackSequence,int mode) {//everything, the background the plots
		//create hash
		int stackHash = stackSequence.hashCode();
		
		/*
		stackHash+=Double.hashCode(plotParams.xMin)*908305804;
		stackHash+=Double.hashCode(plotParams.xMax)*879128337;
		stackHash+=Double.hashCode(plotParams.yMin)*281830831;
		stackHash+=Double.hashCode(plotParams.yMax)*897672383;
		stackHash+=Double.hashCode(plotParams.zMin)*563629913;
		stackHash+=Double.hashCode(plotParams.zMax)*729783957;
		*/
		stackHash+=getWidth();
		stackHash+=getHeight();
		
		if(stackHash != lastGraphHash) {//stack changed can't re-use graphImage
			graphImage = null;
			lastGraphHash = stackHash;
		}
		int defaultDetail = 4;
		int defaultResolution = 48;
		if(mode == MODE_2D) {
			if(graphImage == null) graphImage = renderGraph2D(stackSequence,plotParams,getSize(),backgroundColor,foregroundColor,defaultDetail);
			
			int fastPanX = 0,fastPanY = 0;
			if(mousePressed) {
				fastPanX = mouseX-startMouseX;
				fastPanY = mouseY-startMouseY;
				
				BufferedImage backgroundImage = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = (Graphics2D)backgroundImage.createGraphics();
				g2.setColor(backgroundColor);
				g2.fillRect(0, 0,getWidth() ,getHeight() );
				drawCoordinateLines(g2,plotParams,getSize(),foregroundColor);
				g2.dispose();
				graphics.drawImage(backgroundImage,0, 0,null);
			}
			
			graphics.drawImage(graphImage,fastPanX,fastPanY,null);
			graphics.setColor(foregroundColor);
			graphics.drawString("x: "+(float)convertToInternalPositionX(mouseX,plotParams,getSize()), mouseX+40, mouseY);
			graphics.drawString("y: "+(float)convertToInternalPositionY(mouseY,plotParams,getSize()), mouseX+40, mouseY+20);
		}else if(mode == MODE_3D) {
			if(graphImage == null) graphImage = renderGraph3D(stackSequence,plotParams,getSize(),backgroundColor,foregroundColor,defaultResolution);
			graphics.drawImage(graphImage,0,0,null);
		}else if(mode == MODE_COMPLEX) {
			if(graphImage == null) graphImage = renderGraphComplex(stackSequence,plotParams,getSize(),defaultDetail);
		
			graphics.drawImage(graphImage,0,0,null);
			graphics.drawString("R: "+(float)convertToInternalPositionX(mouseX,plotParams,getSize()), mouseX+40, mouseY);
			graphics.drawString("I: "+(float)convertToInternalPositionY(mouseY,plotParams,getSize()), mouseX+40, mouseY+20);
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		renderGraphWithMouse(g,stackEditor.stackSequence,mode);
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
				startMouseX = mouseX;
				startMouseY = mouseY;
				mousePressed = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
				graphImage = null;
				repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		addMouseMotionListener(new MouseMotionListener() {//mouse panning around graph

			void update(MouseEvent e) {
				pMouseX = mouseX;
				pMouseY = mouseY;
				mouseX = e.getX();
				mouseY = e.getY();
				
				
				if(mousePressed) {
					if(mode == MODE_2D || mode == MODE_COMPLEX) {
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
		addMouseWheelListener(new MouseWheelListener() {//scroll to zoom in and out

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double scrollAmount = e.getPreciseWheelRotation();
				if(mode == MODE_2D || mode == MODE_COMPLEX) {
					plotParams.xMin = plotParams.xMin+(convertToInternalPositionX(mouseX,plotParams,getSize())-plotParams.xMin)*(scrollAmount*scrollSpeed);
					plotParams.xMax = plotParams.xMax-(plotParams.xMax-convertToInternalPositionX(mouseX,plotParams,getSize()))*(scrollAmount*scrollSpeed);
					
					plotParams.yMin = plotParams.yMin+(convertToInternalPositionY(mouseY,plotParams,getSize())-plotParams.yMin)*(scrollAmount*scrollSpeed);
					plotParams.yMax = plotParams.yMax-(plotParams.yMax-convertToInternalPositionY(mouseY,plotParams,getSize()))*(scrollAmount*scrollSpeed);
				}else if(mode == MODE_3D) {
					plotParams.xMin*=(1.0-scrollAmount*scrollSpeed);
					plotParams.xMax*=(1.0-scrollAmount*scrollSpeed);
					
					plotParams.yMin*=(1.0-scrollAmount*scrollSpeed);
					plotParams.yMax*=(1.0-scrollAmount*scrollSpeed);
					
					plotParams.zMin*=(1.0-scrollAmount*scrollSpeed);
					plotParams.zMax*=(1.0-scrollAmount*scrollSpeed);
					
				}
				graphImage = null;
				repaint();
			}
			
		});
		
	}
	
	@Override
	public void setBackground(Color color) {
		backgroundColor = color;
	}
	@Override
	public void setForeground(Color color) {
		foregroundColor = color;
	}
}
