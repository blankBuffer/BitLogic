package cas;
import java.math.BigInteger;

public class Tan extends Expr{
	
	private static final long serialVersionUID = -2282985074053649819L;

	static Equ containsInverse = (Equ)createExpr("tan(atan(x))=x");
	static Equ tanOfArcsin = (Equ)createExpr("tan(asin(x))=x/sqrt(1-x^2)");
	static Equ tanOfArccos = (Equ)createExpr("tan(acos(x))=sqrt(1-x^2)/x");
	
	public Tan(Expr a) {
		add(a);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		toBeSimplified = toBeSimplified.modifyFromExample(containsInverse, settings);
		toBeSimplified = toBeSimplified.modifyFromExample(tanOfArcsin, settings);
		toBeSimplified = toBeSimplified.modifyFromExample(tanOfArccos, settings);
		
		if(toBeSimplified instanceof Tan) {
			toBeSimplified.set(0,factor(toBeSimplified.get()).simplify(settings));
			if(toBeSimplified.get().negative()) {
				toBeSimplified = prod(num(-1),tan(toBeSimplified.get().abs(settings)).simplify(settings) );
			}
		}
		if(toBeSimplified instanceof Tan) toBeSimplified.set(0,distr(toBeSimplified.get()).simplify(settings));
		
		if(toBeSimplified instanceof Tan) toBeSimplified = unitCircle((Tan)toBeSimplified);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	public Expr unitCircle(Tan tan) {
		Pi pi = new Pi();
		BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4),five = BigInteger.valueOf(5);
		
		Expr innerExpr = tan.get();
		if(innerExpr.equalStruct(num(BigInteger.ZERO)) || innerExpr.equalStruct(pi)) {
			return num(0);
		}if(innerExpr instanceof Div && innerExpr.contains(pi())){
			Div frac = ((Div)innerExpr).ratioOfUnitCircle();
			
			if(frac!=null) {
				BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
				
				numer = numer.mod(denom);//we take the mod since it repeats in circle
				
				
				if(numer.equals(BigInteger.ONE) && denom.equals(four)) return num(1);
				else if(numer.equals(BigInteger.ONE) && denom.equals(three)) return sqrt(num(3));
				else if(numer.equals(BigInteger.ONE) && denom.equals(six)) return div(sqrt(num(3)),num(3));
				else if(numer.equals(BigInteger.TWO) && denom.equals(three)) return neg(sqrt(num(3)));
				else if(numer.equals(three) && denom.equals(four)) return num(-1);
				else if(numer.equals(five) && denom.equals(six)) return div(sqrt(num(3)),num(-3));
				else if(numer.equals(BigInteger.ZERO)) return num(0);
				else {
					int negate = 1;
					if(numer.compareTo(denom.divide(BigInteger.TWO)) == 1) {
						numer = denom.subtract(numer);
						negate = -1;
					}
					
					if(negate == -1) {
						return neg(tan(div(prod(pi(),num(numer)),num(denom)).simplify(Settings.normal)));
					}
					return tan(div(prod(pi(),num(numer)),num(denom)).simplify(Settings.normal));
				}
				
			}
		}else if(innerExpr instanceof Sum){
			for(int i = 0;i<innerExpr.size();i++) {
				if(!innerExpr.get(i).containsVars() && innerExpr.get(i).contains(pi)) {
					
					Div frac = ((Div)innerExpr.get(i)).ratioOfUnitCircle();
					
					if(frac!=null) {
						
						BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
						
						numer = numer.mod(denom);//to do this we take the mod
						
						innerExpr.set(i,  div(prod(num(numer),pi()),num(denom)) );
						tan.set(0, innerExpr.simplify(Settings.normal));
						
					}
					
				}
			}
		}
		
		return tan;
	}

	@Override
	public Expr copy() {
		Tan out = new Tan(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="tan(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Tan) {
			if(other.get().equalStruct(get())) return true;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+927142837462378103L;
	}
	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Tan) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.tan(get().convertToFloat(varDefs));
	}

}
