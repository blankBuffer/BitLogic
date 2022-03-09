package cas.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import cas.*;
import cas.primitive.*;
import cas.bool.*;
import cas.calculus.*;
import cas.matrix.*;

/*
 * renders expressions into images in pretty way ,built using unicode characters
 */
public class ExprRender extends QuickMath{//sort of a wrap of the image type but keeps track of fraction bar heights
	
	static class ExprImg{
		private int width,height,fractionBar;
		/*
		 * the fraction bar is defined as the height from the top to the fraction line plus the font size/2
		 * the reason for adding the font size/2 is because suppose we render 2/3+1
		 * 
		 *  2               2   + 1
		 * --- + 1 and not ---
		 *  3               3
		 *  
		 *  the '+' and '1' are drawn slightly below the --- line and sit half a character below it, looks nicer this way
		*/
		private BufferedImage exprImg = null;
		
		private Graphics2D oldGraphics = null;
		public Graphics2D graphics = null;
		
		public ExprImg(Graphics2D oldGraphics) {
			this.oldGraphics = oldGraphics;
		}
		
		private ExprImg newExprImg() {
			return new ExprImg(oldGraphics);
		}
		
		private Rectangle2D getStringSize(String s,int fontSize) {
			Font oldFont = oldGraphics.getFont();
			if(oldFont.getSize() != fontSize) oldGraphics.setFont(new Font(oldGraphics.getFont().getFontName(),0,fontSize));
			Rectangle2D out =  oldGraphics.getFontMetrics().getStringBounds(s, oldGraphics);
			oldGraphics.setFont(oldFont);
			return out;
		}
		
		static HashMap<String,String> nameExchange = new HashMap<String,String>();//use cooler symbols
		static {
			nameExchange.put("~","¬");
			nameExchange.put("or","∨");
			nameExchange.put("&", "∧");
			nameExchange.put("pi", "π");
			nameExchange.put("mu", "µ");
			nameExchange.put("beta", "β");
			nameExchange.put("alpha", "α");
			nameExchange.put("theta", "θ");
			nameExchange.put("delta", "Δ");
			nameExchange.put("lambda", "λ");
			nameExchange.put("epsilon","ε");
			nameExchange.put("zeta","ζ");
			nameExchange.put("sigma", "σ");
			nameExchange.put("tau", "τ");
			nameExchange.put("phi", "φ");
			nameExchange.put("psi", "ψ");
			nameExchange.put("omega", "ω");
		}
		public void makeString(String s,int fontSize) {//creates basic text image
			s = s.replace('*', '·');
			s = nameExchange.getOrDefault(s, s);
			
			Rectangle2D bounds = getStringSize(s,fontSize);
			setWidth((int)bounds.getWidth());
			setHeight((int)bounds.getHeight());
			setFractionBar(getHeight());
			initImg(fontSize);
			graphics.drawString(s,0,(int)(getHeight()*0.8));
		}
		public void makeString(String s) {//creates basic text image
			makeString(s,fontSize());
		}
		
		public int getFontWidth() {
			return fontSize()/2;
		}
		
		public int fontSize() {
			return oldGraphics.getFont().getSize();
		}
		
		public void drawImage(ExprImg eImg,int x,int y,int wid,int hei) {
			graphics.drawImage(eImg.getImage(), x, y, wid, hei, null);
		}
		
		public void makeParen(ExprImg eImg) {//makes image and puts parenthesis around e
			
			setHeight(Math.max(eImg.getHeight(),fontSize()));
			
			ExprImg leftParenImg = newExprImg();
			leftParenImg.makeString("(",getHeight());
			ExprImg rightParenImg = newExprImg();
			rightParenImg.makeString(")",getHeight());
			
			setWidth(getFontWidth()*2+eImg.getWidth());
			
			setFractionBar( Math.min(eImg.getFractionBar(),leftParenImg.getFractionBar()) );
			initImg();
			drawImage(leftParenImg,0,0,getFontWidth(),getHeight());
			drawImage(eImg,getFontWidth(),0,eImg.getWidth(),eImg.getHeight());
			drawImage(rightParenImg,getWidth()-getFontWidth(),0,getFontWidth(),getHeight());
		}
		
		public void makeParen(Expr e) {//makes image and puts parenthesis around e
			ExprImg eImg = newExprImg();
			eImg.makeExpr(e);
			makeParen(eImg);
		}
		
		public void makeImgSeries(ExprImg[] imgs) {
			{
				int fractionBar = 0;
				for(int i = 0;i<imgs.length;i++) {
					fractionBar = Math.max(fractionBar,imgs[i].getFractionBar());
				}
				setFractionBar(fractionBar);
				int width = 0;
				int height = 0;
				for(int i = 0;i<imgs.length;i++) {
					width+=imgs[i].getWidth();
					height = Math.max(height,imgs[i].getBelowFractionBar());
				}
				height+=fractionBar;
				setWidth(width);
				setHeight(height);
			}
			
			initImg();
			
			int currentX = 0;
			for(int i = 0;i<imgs.length;i++) {
				ExprImg cI = imgs[i];
				drawImage(cI,currentX, getFractionBar()-cI.getFractionBar() ,cI.getWidth(),cI.getHeight());
				currentX+=cI.getWidth();
			}
		}
		
		public void makeCommaList(Expr e) {
			if(e.size() == 0) return;
			ExprImg commaImg = newExprImg();
			commaImg.makeString(",");
			
			ArrayList<ExprImg> imgs = new ArrayList<ExprImg>();
			
			for(int i = 0;i<e.size();i++) {
				ExprImg cI = newExprImg();
				cI.makeExpr(e.get(i));
				imgs.add(cI);
				if(i!=e.size()-1) {
					imgs.add(commaImg);
				}
			}
			ExprImg[] exprImgs = new ExprImg[imgs.size()];
			for(int i = 0;i<imgs.size();i++)exprImgs[i] = imgs.get(i);
					
			makeImgSeries(exprImgs);
		}
		
		public void makeExpr(Expr e) {//create image from expression
			if(e instanceof Var || e instanceof Num || e instanceof FloatExpr || e instanceof BoolState) {
				makeString(e.toString());
			}else if(e instanceof Power) {
				Power pow = (Power)e;
				
				ExprImg baseImg = newExprImg();
				
				if(Rule.fastSimilarStruct(sqrtObj,e)) {
					baseImg.makeExpr(pow.getBase());
					
					
					
					int smallShift = fontSize()/6;
					setHeight(baseImg.getHeight()+smallShift*2);
					
					int sqrtWid = getFontWidth();
					
					setWidth(sqrtWid+baseImg.getWidth());
					
					setFractionBar(baseImg.getFractionBar()+smallShift*2);
					
					int overBarHeight = Math.max(fontSize()/12,2);
					
					initImg();
					
					{//draw sqrt section
						graphics.setStroke(new BasicStroke(overBarHeight));
						graphics.drawLine(0, getHeight()/2, sqrtWid/2, getHeight()-smallShift);
						graphics.drawLine(sqrtWid/2, getHeight()-smallShift,sqrtWid,smallShift);
					}
					drawImage(baseImg,sqrtWid,smallShift*2,baseImg.getWidth(),baseImg.getHeight());
					graphics.fillRect(sqrtWid,smallShift,getWidth(),overBarHeight);
				}else {
					if(pow.getBase() instanceof Prod || pow.getBase() instanceof Sum || pow.getBase() instanceof Div || pow.getBase() instanceof Power || (pow.getBase() instanceof Num && pow.getBase().negative()) ) {
						baseImg.makeParen(pow.getBase());
					}else {
						baseImg.makeExpr(pow.getBase());
					}
					ExprImg expoImg = newExprImg();
					expoImg.makeExpr(pow.getExpo());
					
					setWidth(baseImg.getWidth()+expoImg.getWidth()*3/4);
					setHeight(baseImg.getHeight()+expoImg.getHeight()*3/4-fontSize()/2);
					setFractionBar(getHeight()-baseImg.getHeight()+baseImg.getFractionBar());
					initImg();
					
					drawImage(baseImg,0,getHeight()-baseImg.getHeight(),baseImg.getWidth(),baseImg.getHeight());
					drawImage(expoImg,getWidth()-expoImg.getWidth()*3/4,0,expoImg.getWidth()*3/4,expoImg.getHeight()*3/4);
				}
			}else if(e instanceof Div) {
				Div div = (Div)e;
				
				ExprImg numerImg = newExprImg();
				numerImg.makeExpr(div.getNumer());
				ExprImg denomImg = newExprImg();
				denomImg.makeExpr(div.getDenom());
				
				int fractionSpacerHeight = Math.max(fontSize()/12,2);
				int max = Math.max( numerImg.getWidth() , denomImg.getWidth() );
				int fractionSpacerWidth = max;
				
				setWidth( max+fontSize()/2 );
				setHeight( numerImg.getHeight()+fractionSpacerHeight+denomImg.getHeight() );
				setFractionBar(numerImg.getHeight()+fontSize()/2);
				
				initImg();
				drawImage(numerImg,getWidth()/2-numerImg.getWidth()/2,0,numerImg.getWidth(),numerImg.getHeight());
				graphics.fillRect(getWidth()/2-fractionSpacerWidth/2, numerImg.getHeight(), fractionSpacerWidth, fractionSpacerHeight);
				drawImage(denomImg,getWidth()/2-denomImg.getWidth()/2,getHeight()-denomImg.getHeight(),denomImg.getWidth(),denomImg.getHeight());
			}else if(e instanceof Equ) {
				Equ equ = (Equ)e;
				
				ExprImg equImg = newExprImg();
				if(equ.type == Equ.EQUALS)equImg.makeString("=");
				if(equ.type == Equ.GREATER)equImg.makeString(">");
				if(equ.type == Equ.LESS)equImg.makeString("<");
				
				ExprImg leftImg = newExprImg();
				leftImg.makeExpr(equ.getLeftSide());
				ExprImg rightImg = newExprImg();
				rightImg.makeExpr(equ.getRightSide());
				
				makeImgSeries(new ExprImg[]{leftImg,equImg,rightImg});
			}else if(e instanceof Sum) {
				ExprImg plusImg = newExprImg();
				plusImg.makeString("+");
				ExprImg minusImg = newExprImg();
				minusImg.makeString("-");
				
				ArrayList<ExprImg> imgs = new ArrayList<ExprImg>();
				
				ExprImg firstElImg = newExprImg();
				firstElImg.makeExpr(e.get(0));
				imgs.add(firstElImg);
				for(int i = 1;i<e.size();i++) {
					boolean negative = false;
					Expr absElement = e.get(i).copy();
					if(absElement instanceof Num) {
						Num innerNum = (Num)absElement;
						if(!innerNum.isComplex() && innerNum.negative()) {
							absElement = innerNum.negate();
							negative = true;
						}
					}else if(absElement instanceof Prod) {
						for(int j = 0;j<absElement.size();j++) {
							if(absElement.get(j) instanceof Num) {
								Num innerNum = (Num)absElement.get(j);
								
								if(!innerNum.isComplex() && innerNum.negative()) {
									absElement.set(j, innerNum.negate());
									negative = true;
									if(absElement.get(j).equals(Num.ONE)) {
										absElement.remove(j);
									}
								}
								
								break;
							}
						}
					}
					if(negative) {
						imgs.add(minusImg);
					}else {
						imgs.add(plusImg);
					}
					ExprImg absImg = newExprImg();
					absImg.makeExpr(absElement);
					imgs.add(absImg);
				}

				ExprImg[] exprImgs = new ExprImg[imgs.size()];
				for(int i = 0;i<imgs.size();i++)exprImgs[i] = imgs.get(i);
						
				makeImgSeries(exprImgs);
			}else if(e instanceof Prod) {
				Prod prodCopy = (Prod)e.copy();
				
				ExprImg multImg = newExprImg();
				multImg.makeString("*");
				
				int numIndex = -1;
				for(int i = 0;i<e.size();i++) {
					if(e.get(i) instanceof Num) {
						numIndex = i;
						break;
					}
				}
				
				ExprImg minusImg = null;
				if(numIndex != -1) {
					Expr temp = prodCopy.get(0);
					prodCopy.set(0, prodCopy.get(numIndex));
					prodCopy.set(numIndex, temp);
					if(prodCopy.get(0).equals(Num.NEG_ONE)) {
						prodCopy.remove(0);
						minusImg = newExprImg();
						minusImg.makeString("-");
					}
				}
				ArrayList<ExprImg> imgs = new ArrayList<ExprImg>();
				if(minusImg!= null) imgs.add(minusImg);
				
				for(int i = 0;i<prodCopy.size();i++) {
					ExprImg imgEl = newExprImg();
					if(prodCopy.get(i) instanceof Sum) imgEl.makeParen(prodCopy.get(i));
					else imgEl.makeExpr(prodCopy.get(i));
					imgs.add(imgEl);
					if(i!=prodCopy.size()-1) imgs.add(multImg);
				}
				
				ExprImg[] exprImgs = new ExprImg[imgs.size()];
				for(int i = 0;i<imgs.size();i++)exprImgs[i] = imgs.get(i);
						
				makeImgSeries(exprImgs);
			}else if(e instanceof ExprList || e instanceof Sequence) {
				ExprImg leftBrac = newExprImg();
				ExprImg rightBrac = newExprImg();
				
				
				ExprImg parameters = newExprImg();
				parameters.makeCommaList(e);
				
				setHeight(Math.max(parameters.getHeight(),fontSize()));
				
				if(e instanceof ExprList) {
					leftBrac.makeString("[",getHeight());
					rightBrac.makeString("]",getHeight());
				}else {
					leftBrac.makeString("{",getHeight());
					rightBrac.makeString("}",getHeight());
				}
				
				setWidth(getFontWidth()+parameters.getWidth()+getFontWidth());
				
				setFractionBar(Math.max(parameters.getFractionBar(),leftBrac.getFractionBar()));
				
				initImg();
				drawImage(leftBrac,0,0,getFontWidth(),getHeight());
				drawImage(parameters,getFontWidth(),0,parameters.getWidth(),getHeight());
				drawImage(rightBrac,getWidth()-getFontWidth(),0,getFontWidth(),getHeight());
				
			}else if(e instanceof And) {
				ExprImg andImg = newExprImg();
				andImg.makeString("&");
				
				
				ArrayList<ExprImg> imgs = new ArrayList<ExprImg>();
				for(int i = 0;i<e.size();i++) {
					ExprImg imgEl = newExprImg();
					
					if(e.get(i) instanceof Or) imgEl.makeParen(e.get(i));
					else imgEl.makeExpr(e.get(i));
					
					imgs.add(imgEl);
					
					if(i != e.size()-1) imgs.add(andImg);
				}
				
				ExprImg[] exprImgs = new ExprImg[imgs.size()];
				for(int i = 0;i<imgs.size();i++)exprImgs[i] = imgs.get(i);
						
				makeImgSeries(exprImgs);
			}else if(e instanceof Or) {
				ExprImg orImg = newExprImg();
				orImg.makeString("or");
				
				
				ArrayList<ExprImg> imgs = new ArrayList<ExprImg>();
				for(int i = 0;i<e.size();i++) {
					ExprImg imgEl = newExprImg();
					
					imgEl.makeExpr(e.get(i));
					
					imgs.add(imgEl);
					
					if(i != e.size()-1) imgs.add(orImg);
				}
				
				ExprImg[] exprImgs = new ExprImg[imgs.size()];
				for(int i = 0;i<imgs.size();i++)exprImgs[i] = imgs.get(i);
						
				makeImgSeries(exprImgs);
			}else if(e instanceof Not) {
				ExprImg notImg = newExprImg();
				notImg.makeString("~");
				
				ExprImg exprImg = newExprImg();
				
				if(e.get() instanceof Var) exprImg.makeExpr(e.get());
				else exprImg.makeParen(e.get());
				
				makeImgSeries(new ExprImg[] {notImg,exprImg});
			}else if(e instanceof Abs) {
				ExprImg eImg = newExprImg();
				eImg.makeExpr(e.get());
				
				ExprImg verticleBar = newExprImg();
				verticleBar.makeString("|");
				
				setWidth(verticleBar.getWidth()*2+eImg.getWidth());
				setHeight(eImg.getHeight());
				setFractionBar(eImg.getFractionBar());
				
				initImg();
				
				drawImage(verticleBar,0,0,verticleBar.getWidth(),getHeight());
				drawImage(eImg,verticleBar.getWidth(),0,eImg.getWidth(),getHeight());
				drawImage(verticleBar,getWidth()-verticleBar.getWidth(),0,verticleBar.getWidth(),getHeight());
			}else if(e instanceof Integrate) {
				
				ExprImg diffImg = newExprImg();
				diffImg.makeString("∂");
				
				Integrate integ = (Integrate)e;
				
				ExprImg varImg = newExprImg();
				varImg.makeString(integ.getVar().toString());
				
				ExprImg eImg = newExprImg();
				eImg.makeExpr(integ.get());
				
				setHeight(eImg.getHeight());
				
				ExprImg leftBracket = newExprImg();
				leftBracket.makeString("[",getHeight());
				ExprImg rightBracket = newExprImg();
				rightBracket.makeString("]",getHeight());
				
				setWidth(getHeight()/2+getFontWidth()+eImg.getWidth()+getFontWidth()+diffImg.getWidth()+varImg.getWidth());
				
				setFractionBar(eImg.getFractionBar());
				
				ExprImg integralImg = newExprImg();
				integralImg.makeString("∫", getHeight() );
				
				initImg();
				int currentX = 0;
				drawImage(integralImg,currentX,0,getHeight()/2,getHeight());
				currentX+=getHeight()/2;
				drawImage(leftBracket,currentX,0,getFontWidth(),getHeight());
				currentX+=getFontWidth();
				
				drawImage(eImg,currentX,0,eImg.getWidth(),getHeight());
				currentX+=eImg.getWidth();
				drawImage(rightBracket,currentX,0,getFontWidth(),getHeight());
				currentX+=getFontWidth();
				
				drawImage(diffImg,currentX,getFractionBar()-diffImg.getHeight(),diffImg.getWidth(),diffImg.getHeight());
				currentX+=diffImg.getWidth();
				drawImage(varImg,currentX,getFractionBar()-varImg.getHeight(),varImg.getWidth(),varImg.getHeight());
				
			}else if(e instanceof IntegrateOver) {
				
				ExprImg diffImg = newExprImg();
				diffImg.makeString("∂");
				
				IntegrateOver integ = (IntegrateOver)e;
				
				ExprImg varImg = newExprImg();
				varImg.makeString(integ.getVar().toString());
				
				ExprImg eImg = newExprImg();
				eImg.makeExpr(integ.getExpr());
				
				ExprImg minImg = newExprImg();
				minImg.makeExpr(integ.getMin());
				
				ExprImg maxImg = newExprImg();
				maxImg.makeExpr(integ.getMax());
				
				setHeight(Math.max(eImg.getHeight(),minImg.getHeight()*3/4+maxImg.getHeight()*3/4)+fontSize());
				ExprImg leftBracket = newExprImg();
				leftBracket.makeString("[",getHeight());
				ExprImg rightBracket = newExprImg();
				rightBracket.makeString("]",getHeight());
				setWidth(getHeight()/2+Math.max(maxImg.getWidth()*3/4,minImg.getWidth()*3/4)+getFontWidth()+eImg.getWidth()+getFontWidth()+diffImg.getWidth()+varImg.getWidth());
				
				setFractionBar(getHeight()-eImg.getBelowFractionBar());
				
				ExprImg integralImg = newExprImg();
				integralImg.makeString("∫",getHeight());
				
				initImg();
				int currentX = 0;
				drawImage(integralImg,currentX,0,getHeight()/2,getHeight());
				currentX+=getHeight()/2;
				
				
				drawImage(minImg,currentX,getHeight()-minImg.getHeight()*3/4,minImg.getWidth()*3/4,minImg.getHeight()*3/4);
				drawImage(maxImg,currentX,0,maxImg.getWidth()*3/4,maxImg.getHeight()*3/4);
				currentX+=Math.max(maxImg.getWidth()*3/4,minImg.getWidth()*3/4);
				
				
				drawImage(leftBracket,currentX,0,getFontWidth(),getHeight());
				currentX+=getFontWidth();
				
				drawImage(eImg,currentX,getHeight()-eImg.getHeight(),eImg.getWidth(),eImg.getHeight());
				currentX+=eImg.getWidth();
				drawImage(rightBracket,currentX,0,getFontWidth(),getHeight());
				currentX+=getFontWidth();
				
				drawImage(diffImg,currentX,getFractionBar()-diffImg.getHeight(),diffImg.getWidth(),diffImg.getHeight());
				currentX+=diffImg.getWidth();
				drawImage(varImg,currentX,getFractionBar()-varImg.getHeight(),varImg.getWidth(),varImg.getHeight());
				
			}else if(e instanceof Becomes) {
				Becomes becomes = (Becomes)e;
				
				ExprImg leftSide = newExprImg();
				leftSide.makeExpr(becomes.getLeftSide());
				ExprImg rightSide = newExprImg();
				rightSide.makeExpr(becomes.getRightSide());
				
				ExprImg arrowImg = newExprImg();
				arrowImg.makeString(" → ");
				
				makeImgSeries(new ExprImg[] {leftSide,arrowImg,rightSide  });
			}else if(e instanceof Diff) {
				
				ExprImg diffImg = newExprImg();
				diffImg.makeExpr( div(var("∂"),var("∂"+nameExchange.getOrDefault(e.getVar().toString(), e.getVar().toString()))) );
				
				ExprImg eImg = newExprImg();
				
				eImg.makeExpr(e.get());
				
				int fractionBar = Math.max(eImg.getFractionBar(),diffImg.getFractionBar());
				int belowFractionBar = Math.max(eImg.getBelowFractionBar(), diffImg.getBelowFractionBar());
				
				setHeight(fractionBar+belowFractionBar);
				
				ExprImg leftBracket = newExprImg();
				leftBracket.makeString("[",getHeight());
				ExprImg rightBracket = newExprImg();
				rightBracket.makeString("]",getHeight());
				
				setWidth(diffImg.getWidth()+getFontWidth()+eImg.getWidth()+getFontWidth());
				setFractionBar(fractionBar);
				
				initImg();
				
				int currentX = 0;
				drawImage(diffImg, currentX, getFractionBar()-diffImg.getFractionBar(), diffImg.getWidth(), diffImg.getHeight());
				currentX+=diffImg.getWidth();
				drawImage(leftBracket,currentX,0,getFontWidth(),getHeight());
				currentX+=getFontWidth();
				drawImage(eImg,currentX,getFractionBar()-eImg.getFractionBar(),eImg.getWidth(),eImg.getHeight());
				currentX+=eImg.getWidth();
				drawImage(rightBracket,currentX,0,getFontWidth(),getHeight());
				
			}else if(e instanceof Mat && ((Mat)e).correctFormat() ) {
				Mat mat = (Mat)e;
				
				int spacerSize = fontSize()/2;
				
				ExprImg leftBracket = newExprImg();
				leftBracket.makeString("[");
				ExprImg rightBracket = newExprImg();
				rightBracket.makeString("]");
				
				ExprImg[][] imgs = new ExprImg[mat.rows()][mat.cols()];
				int[] maxWidth = new int[mat.cols()];
				int[] maxHeight = new int[mat.rows()];
				
				for(int row = 0;row<mat.rows();row++) {
					for(int col = 0;col<mat.cols();col++) {
						
						imgs[row][col] = newExprImg();
						imgs[row][col].makeExpr(mat.getElement(row, col));
						maxWidth[col] = Math.max(maxWidth[col], imgs[row][col].getWidth());
						maxHeight[row] = Math.max(maxHeight[row], imgs[row][col].getHeight());
						
					}
				}
				
				int widthOfGrid = Math.max(spacerSize*(mat.cols()-1),0);
				for(int i = 0;i<mat.cols();i++) widthOfGrid+=maxWidth[i];
				setWidth(leftBracket.getWidth()+widthOfGrid+rightBracket.getWidth());
				
				int HeightOfGrid = spacerSize*(mat.rows()-1);
				for(int i = 0;i<mat.rows();i++) HeightOfGrid+=maxHeight[i];
				setHeight(Math.max(HeightOfGrid,leftBracket.getHeight()));
				
				setFractionBar(getHeight()/2+spacerSize);
				
				initImg();
				
				drawImage(leftBracket,0,0,leftBracket.getWidth(),getHeight());
				
				int y = 0;
				for(int row = 0;row<mat.rows();row++) {
					int x = leftBracket.getWidth();
					for(int col = 0;col<mat.cols();col++) {
						ExprImg i = imgs[row][col];
						drawImage(i,x+maxWidth[col]/2-i.getWidth()/2 ,y+maxHeight[row]/2-i.getHeight()/2 ,i.getWidth(),i.getHeight());
						x+=maxWidth[col]+spacerSize;
					}
					y+=maxHeight[row]+spacerSize;
				}
				
				drawImage(rightBracket,getWidth()-rightBracket.getWidth(),0,rightBracket.getWidth(),getHeight());
				
			}else if(e instanceof Dot) {
				ExprImg multImg = newExprImg();
				multImg.makeString("×");
				
				
				ExprImg[] imgSeries = new ExprImg[e.size()+e.size()-1];
				for(int i = 0;i<imgSeries.length;i+=2) {
					imgSeries[i] = newExprImg();
					imgSeries[i].makeExpr(e.get(i/2));
					if(i != imgSeries.length-1) {
						imgSeries[i+1] = multImg;
					}
				}
				
				makeImgSeries(imgSeries);
			}else {
				ExprImg functionName = newExprImg();
				functionName.makeString(e.typeName());
				ExprImg parameters = newExprImg();
				parameters.makeCommaList(e);
				ExprImg parenParams = newExprImg();
				parenParams.makeParen(parameters);
				
				ExprImg[] exprImgs = new ExprImg[]{functionName,parenParams};
				makeImgSeries(exprImgs);
			}
		}
		
		public void initImg(int fontSize) {//width and height must be set before this call
			exprImg = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			graphics = exprImg.createGraphics();
			graphics.setColor(oldGraphics.getColor());
			graphics.setFont(new Font(oldGraphics.getFont().getFontName(),0,fontSize));
			graphics.setRenderingHints(oldGraphics.getRenderingHints());
		}
		
		public void initImg() {
			initImg(fontSize());
		}
		
		public void setWidth(int width) {
			this.width = width;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getWidth() {
			return width;
		}
		public int getHeight(){
			return height;
		}
		public int getFractionBar() {
			return fractionBar;
		}
		public int getBelowFractionBar() {
			return height-fractionBar;
		}
		public void setFractionBar(int fractionBar) {
			this.fractionBar = fractionBar;
		}
		public BufferedImage getImage() {
			return exprImg;
		}
	}
	
	public static BufferedImage createImg(Expr e,Color text,int fontSize) {//this returns the image with the expression fitting the space as best as possible
		BufferedImage nothing = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);//need a way to obtain a graphics 2d object
		Graphics2D g = nothing.createGraphics();
		g.setColor(text);
		g.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
		g.setFont(new Font("courier new",0,fontSize));
		
		ExprImg exprImgObj = new ExprImg(g);
		exprImgObj.makeExpr(e);
		BufferedImage exprImg = exprImgObj.getImage();
		
		return exprImg;
	}
	
	public static BufferedImage createImgInFrame(Expr e,Dimension imageSize,Color background,Color text) {//makes an image and puts the expression into a specified frame with the image size parameter
		BufferedImage image = new BufferedImage(imageSize.width,imageSize.height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(background);
		g.fillRect(0, 0, imageSize.width, imageSize.height);
		g.setColor(text);
		
		if(e instanceof Var && e.toString().contains("\n")) {
			return image;
		}
		
		g.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
		
		g.setFont(new Font("courier new",0,48));
		
		//fitting expression image into desired space and centering it
		ExprImg exprImgObj = new ExprImg(g);
		exprImgObj.makeExpr(e);
		BufferedImage exprImg = exprImgObj.getImage();
		
		int imgWid = 0,imgHei = 0;
		double imageRatio = imageSize.getWidth()/imageSize.getHeight();
		double exprImgRatio = (double)exprImg.getWidth()/exprImg.getHeight();
		
		if(imageRatio<exprImgRatio) {
			double ratio = imageSize.getWidth()/exprImg.getWidth();
			imgWid = (int)(ratio*exprImg.getWidth());
			imgHei = (int)(ratio*exprImg.getHeight());
		}else {
			double ratio = imageSize.getHeight()/exprImg.getHeight();
			imgWid = (int)(ratio*exprImg.getWidth());
			imgHei = (int)(ratio*exprImg.getHeight());
		}
		
		g.drawImage(exprImg,imageSize.width/2-imgWid/2,imageSize.height/2-imgHei/2,imgWid, imgHei, null);
		
		return image;
	}
	
	
	
}
