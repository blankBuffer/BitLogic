package cas;

import java.math.BigInteger;

public class Cos extends Expr{
	
	private static final long serialVersionUID = -529344373251624547L;

	public Cos(Expr a) {
		add(a);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		if(toBeSimplified instanceof Cos) toBeSimplified.set(0,factor(toBeSimplified.get()).simplify(settings));
		if(toBeSimplified instanceof Cos) toBeSimplified.set(0, toBeSimplified.get().abs(settings));
		if(toBeSimplified instanceof Cos) toBeSimplified.set(0,distr(toBeSimplified.get()).simplify(settings));
		
		if(toBeSimplified instanceof Cos) toBeSimplified = unitCircle((Cos)toBeSimplified);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	public Expr unitCircle(Cos cos) {
		Pi pi = new Pi();
		BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4);
		Expr innerExpr = cos.get();
		if(innerExpr.equalStruct(num(0))) {
			return num(1);
		}else if(innerExpr instanceof Pi)
			return num(-1);
		if(innerExpr instanceof Div && innerExpr.contains(pi())){
			Div frac =((Div)innerExpr).ratioOfUnitCircle();
			
			if(frac!=null) {
				
				BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
				
				numer = numer.mod(denom.multiply(BigInteger.TWO));
				int negate = 1;
				
				if(numer.compareTo(denom) == 1) {
					numer = numer.mod(denom);
				}
				
				if(numer.compareTo(denom.divide(BigInteger.TWO)) == 1) {
					negate = -1;
					numer = denom.subtract(numer);
				}
				
				if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) return num(0);
				else if(numer.equals(BigInteger.ONE) && denom.equals(three)) return inv(num(2*negate));
				else if(numer.equals(BigInteger.ONE) && denom.equals(six)) return div(sqrt(num(3)),num(2*negate));
				else if(numer.equals(BigInteger.ONE) && denom.equals(four)) return div(sqrt(num(2)),num(2*negate));
				else if(numer.equals(BigInteger.ZERO)) return num(negate);
				else {
					if(negate == -1) {
						return neg(sin(div(prod(pi(),num(numer)),inv(num(denom))).simplify(Settings.normal)));
					}else {
						return sin(div(prod(pi(),num(numer)),inv(num(denom))).simplify(Settings.normal));
					}
				}
				
				
			}
			
		}else if(innerExpr instanceof Sum) {//sin(x-pi/4) can be turned into sin(x+7*pi/4) because sin has symmetry
			for(int i = 0;i<innerExpr.size();i++) {
				if(innerExpr.get(i) instanceof Div && !innerExpr.get(i).containsVars() && innerExpr.get(i).contains(pi)) {
					Div frac = ((Div)innerExpr.get(i)).ratioOfUnitCircle();
					
					if(frac!=null) {
						BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
						
						numer = numer.mod(denom.multiply(BigInteger.TWO));//to do this we take the mod
						
						if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) {//cos(x+pi/2) = -sin(x)
							innerExpr.remove(i);
							return neg(sin(innerExpr.simplify(Settings.normal)));
						}else if(numer.equals(three) && denom.equals(BigInteger.TWO)) {
							innerExpr.remove(i);
							return sin(innerExpr.simplify(Settings.normal));
						}
						
						innerExpr.set(i,  div(prod(num(numer),pi()),num(denom)) );
						cos.set(0, innerExpr.simplify(Settings.normal));
						
					}
					
				}
			}
		}
		return cos;
	}

	@Override
	public Expr copy() {
		Cos out = new Cos(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="cos(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Cos) {
			if(other.get().equalStruct(get())) return true;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+8236910273651944021L;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		return new Cos(get().replace(equs));
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Cos) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.cos(get().convertToFloat(varDefs));
	}
}
