package cas;

import java.awt.image.BufferedImage;

public class ImgExpr extends Expr{//behaves like variables

	private static final long serialVersionUID = 5555283885953176775L;
	public BufferedImage image = null;
	
	public ImgExpr(BufferedImage img) {
		image = img;
	}

	@Override
	public Expr simplify(Settings settings) {
		return copy();
	}

	@Override
	public Expr copy() {
		return new ImgExpr(image);
	}

	@Override
	public String toString() {
		return image.toString();
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof ImgExpr){
			return ((ImgExpr)other).image == image;//only comparing if same image object
		}
		return false;
	}

	@Override
	public long generateHash() {
		return image.hashCode()+8760341908762341234L;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		return copy();
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		return true;
	}
	
}
