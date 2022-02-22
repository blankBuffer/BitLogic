package cas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

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
		
		private Rectangle2D getStringSize(String s) {
			return oldGraphics.getFontMetrics().getStringBounds(s, oldGraphics);
		}
		
		static HashMap<String,String> nameExchange = new HashMap<String,String>();//use cooler symbols
		static {
			nameExchange.put("~","¬");
			nameExchange.put("|","∨");
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
			nameExchange.put("integrate", "∫");
			nameExchange.put("integrateOver", "∫");
			nameExchange.put("diff", "∂");
		}
		public void makeString(String s) {//creates basic text image
			s = s.replace('*', '·');
			s = nameExchange.getOrDefault(s, s);
			
			Rectangle2D bounds = getStringSize(s);
			setWidth((int)bounds.getWidth());
			setHeight((int)bounds.getHeight());
			setFractionBar(getHeight());
			initImg();
			graphics.drawString(s,0,(int)(getHeight()*0.8));
		}
		
		public void makeParen(ExprImg eImg) {//makes image and puts parenthesis around e
			ExprImg leftParenImg = newExprImg();
			leftParenImg.makeString("(");
			ExprImg rightParenImg = newExprImg();
			rightParenImg.makeString(")");
			
			setWidth(leftParenImg.getWidth()+eImg.getWidth()+rightParenImg.getWidth());
			setHeight(Math.max(eImg.getHeight(),leftParenImg.getHeight()));
			setFractionBar( Math.max(eImg.getFractionBar(),leftParenImg.getFractionBar()) );
			
			initImg();
			graphics.drawImage(leftParenImg.getImage(),0,0,leftParenImg.getWidth(),getHeight(),null);
			graphics.drawImage(eImg.getImage(),leftParenImg.getWidth(),0,eImg.getWidth(),eImg.getHeight(),null);
			graphics.drawImage(rightParenImg.getImage(),getWidth()-rightParenImg.getWidth(),0,rightParenImg.getWidth(),getHeight(),null);
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
				graphics.drawImage(cI.getImage(),currentX, getFractionBar()-cI.getFractionBar() ,cI.getWidth(),cI.getHeight(),null);
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
					
					ExprImg sqrtImg = newExprImg();
					sqrtImg.makeString("√");
					
					setWidth(sqrtImg.getWidth()+baseImg.getWidth());
					int smallShift = oldGraphics.getFont().getSize()/6;
					setHeight(baseImg.getHeight()+smallShift);
					setFractionBar(baseImg.getFractionBar()+smallShift);
					
					int overBarHeight = oldGraphics.getFont().getSize()/12;
					
					initImg();
					
					graphics.drawImage(sqrtImg.getImage(),0,0,sqrtImg.getWidth(),getHeight(),null);
					graphics.drawImage(baseImg.getImage(),sqrtImg.getWidth(),smallShift,baseImg.getWidth(),baseImg.getHeight(),null);
					graphics.fillRect(sqrtImg.getWidth(),0,getWidth(),overBarHeight);
				}else {
					if(pow.getBase() instanceof Prod || pow.getBase() instanceof Sum || pow.getBase() instanceof Div || pow.getBase() instanceof Power || (pow.getBase() instanceof Num && pow.getBase().negative()) ) {
						baseImg.makeParen(pow.getBase());
					}else {
						baseImg.makeExpr(pow.getBase());
					}
					ExprImg expoImg = newExprImg();
					expoImg.makeExpr(pow.getExpo());
					
					setWidth(baseImg.getWidth()+expoImg.getWidth());
					setHeight(baseImg.getHeight()+expoImg.getHeight()*3/4-oldGraphics.getFont().getSize()/2);
					setFractionBar(getHeight()-baseImg.getHeight()+baseImg.getFractionBar());
					initImg();
					
					graphics.drawImage(baseImg.getImage(),0,getHeight()-baseImg.getHeight(),baseImg.getWidth(),baseImg.getHeight(),null);
					graphics.drawImage(expoImg.getImage(),getWidth()-expoImg.getWidth(),0,expoImg.getWidth()*3/4,expoImg.getHeight()*3/4,null);
				}
			}else if(e instanceof Div) {
				Div div = (Div)e;
				
				ExprImg numerImg = newExprImg();
				numerImg.makeExpr(div.getNumer());
				ExprImg denomImg = newExprImg();
				denomImg.makeExpr(div.getDenom());
				
				int fractionSpacerHeight = oldGraphics.getFont().getSize()/12;
				int max = Math.max( numerImg.getWidth() , denomImg.getWidth() );
				int fractionSpacerWidth = max;
				
				setWidth( max+oldGraphics.getFont().getSize()/2 );
				setHeight( numerImg.getHeight()+fractionSpacerHeight+denomImg.getHeight() );
				setFractionBar(numerImg.getHeight()+oldGraphics.getFont().getSize()/2);
				
				initImg();
				graphics.drawImage(numerImg.getImage(),getWidth()/2-numerImg.getWidth()/2,0,numerImg.getWidth(),numerImg.getHeight(),null);
				graphics.fillRect(getWidth()/2-fractionSpacerWidth/2, numerImg.getHeight(), fractionSpacerWidth, fractionSpacerHeight);
				graphics.drawImage(denomImg.getImage(),getWidth()/2-denomImg.getWidth()/2,getHeight()-denomImg.getHeight(),denomImg.getWidth(),denomImg.getHeight(),null);
			}else if(e instanceof Equ) {
				Equ equ = (Equ)e;
				
				ExprImg equImg = newExprImg();
				equImg.makeString("=");
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
					boolean negative = e.get(i).negative();
					Expr absElement = e.get(i).strangeAbs(Settings.normal);
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
			}else if(e instanceof ExprList) {
				ExprImg leftBrac = newExprImg();
				leftBrac.makeString("[");
				ExprImg rightBrac = newExprImg();
				rightBrac.makeString("]");
				
				ExprImg parameters = newExprImg();
				parameters.makeCommaList(e);
				
				setWidth(leftBrac.getWidth()+parameters.getWidth()+rightBrac.getWidth());
				setHeight(parameters.getHeight());
				setFractionBar(parameters.getFractionBar());
				
				initImg();
				graphics.drawImage(leftBrac.getImage(),0,0,leftBrac.getWidth(),getHeight(),null);
				graphics.drawImage(parameters.getImage(),leftBrac.getWidth(),0,parameters.getWidth(),getHeight(),null);
				graphics.drawImage(rightBrac.getImage(),getWidth()-rightBrac.getWidth(),0,rightBrac.getWidth(),getHeight(),null);
				
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
				orImg.makeString("|");
				
				
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
		
		public void initImg() {//width and height must be set before this call
			exprImg = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			graphics = exprImg.createGraphics();
			graphics.setColor(oldGraphics.getColor());
			graphics.setFont(oldGraphics.getFont());
			graphics.setRenderingHints(oldGraphics.getRenderingHints());
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
	
	public static BufferedImage createImg(Expr e,Color background,Color text) {//this returns the image with the expression fitting the space as best as possible
		BufferedImage defaultImageSize = new BufferedImage(256*4,256,BufferedImage.TYPE_INT_RGB);
		Graphics2D g = defaultImageSize.createGraphics();
		g.setColor(background);
		g.fillRect(0, 0, defaultImageSize.getWidth(), defaultImageSize.getHeight());
		g.setColor(text);
		
		g.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
		
		g.setFont(new Font("courier new",0,48));
		
		ExprImg exprImgObj = new ExprImg(g);
		exprImgObj.makeExpr(e);
		BufferedImage exprImg = exprImgObj.getImage();
		
		int imgWid = 0,imgHei = 0;
		double imageRatio = defaultImageSize.getWidth()/defaultImageSize.getHeight();
		double exprImgRatio = (double)exprImg.getWidth()/exprImg.getHeight();
		
		if(imageRatio<exprImgRatio) {
			double ratio =(double)defaultImageSize.getWidth()/exprImg.getWidth();
			imgWid = (int)(ratio*exprImg.getWidth());
			imgHei = (int)(ratio*exprImg.getHeight());
		}else {
			double ratio = (double)defaultImageSize.getHeight()/exprImg.getHeight();
			imgWid = (int)(ratio*exprImg.getWidth());
			imgHei = (int)(ratio*exprImg.getHeight());
		}
		
		g.drawImage(exprImg,0,0,imgWid, imgHei, null);
		
		return defaultImageSize.getSubimage(0, 0, imgWid, imgHei);
	}
	
	public static BufferedImage createImgInFrame(Expr e,Dimension imageSize,Color background,Color text) {//makes an image and puts the expression into a specified frame with the image size parameter
		BufferedImage image = new BufferedImage(imageSize.width,imageSize.height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(background);
		g.fillRect(0, 0, imageSize.width, imageSize.height);
		g.setColor(text);
		
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
