package cas;
import java.math.BigInteger;

public class Power extends Expr{
	
	private static final long serialVersionUID = 3916987907762535821L;
	static Equ baseHasPower = (Equ) createExpr("(a^b)^c=a^(b*c)");
	static Equ powerOfZero = (Equ) createExpr("a^0=1");
	static Equ powerOfOne = (Equ) createExpr("a^1=a");
	static Equ eToLn = (Equ) createExpr("e^ln(a)=a");
	static Equ eToFracLn = (Equ) createExpr("e^(ln(a)/b)=a^(1/b)");
	static Equ oneToExpo = (Equ) createExpr("1^x=1");
	static Equ zeroToExpo = (Equ) createExpr("0^x=0");
	static Equ baseToLn = (Equ) createExpr("a^ln(b)=e^(ln(a)*ln(b))");
	
	void setBase(Expr base) {
		set(0, base);
	}
	void setExpo(Expr expo) {
		set(1, expo);
	}
	
	Expr getBase() {
		return get(0);
	}
	Expr getExpo() {
		return get(1);
	}
	
	public Power(Expr base,Expr expo) {
		add(base);
		add(expo);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		((Power)toBeSimplified).setExpo( factor(((Power)toBeSimplified).getExpo()).simplify(settings) );//factor expo
		
		toBeSimplified = toBeSimplified.modifyFromExample(baseHasPower,settings);//(a^b)^c -> a^(b*c)
		
		if(toBeSimplified instanceof Power) toBeSimplified = negativeExpoToInv((Power)toBeSimplified,settings);
		
		if(toBeSimplified instanceof Power) toBeSimplified = toBeSimplified.modifyFromExample(oneToExpo,settings);//1^x -> x
		
		if(toBeSimplified instanceof Power) toBeSimplified = logToBase((Power)toBeSimplified,settings);//e^(ln(x)*b)->x^b
		
		if(toBeSimplified instanceof Power) toBeSimplified = rootExpand((Power)toBeSimplified);//12^(x/2) find divisible squares in this case to -> (4*2)^x/2, also works with cubes and stuff
		
		if(toBeSimplified instanceof Power) if(!(getExpo() instanceof Num)) perfectPowerBase((Power)toBeSimplified,settings);//25^x -> 5^(2*x)
		
		if(toBeSimplified instanceof Power) if( ((Power)toBeSimplified).getBase() instanceof Sum ) ((Power)toBeSimplified).setBase( factor(((Power)toBeSimplified).getBase()).simplify(settings) );//factor base
		
		if(toBeSimplified instanceof Power) toBeSimplified = productInBase((Power)toBeSimplified,settings);//(x*y)^z -> x^z*y^z
		
		if(toBeSimplified instanceof Power) toBeSimplified = expoHasIntegerInSum((Power)toBeSimplified,settings);//2^(x+3) -> 2^x*8 also 2^(x+5/3) the 5/3 turns into mixed fraction
		
		if(toBeSimplified instanceof Power) toBeSimplified = expoSumHasLog((Power)toBeSimplified,settings);//e^(ln(x)+a)->x*e^a
		
		if(toBeSimplified instanceof Power) toBeSimplified = exponentiateIntegers((Power)toBeSimplified);//2^3 = 8
		
		if(toBeSimplified instanceof Power) toBeSimplified = toBeSimplified.modifyFromExample(powerOfZero,settings);//a^0 -> 1
		
		if(toBeSimplified instanceof Power) toBeSimplified = toBeSimplified.modifyFromExample(zeroToExpo,settings);//0^x -> 0
		
		if(toBeSimplified instanceof Power) toBeSimplified = Power.unCast(toBeSimplified);//a^1 -> a
		
		if(toBeSimplified instanceof Power) toBeSimplified = toBeSimplified.modifyFromExample(eToLn,settings);//e^ln(a) -> a
		if(toBeSimplified instanceof Power) toBeSimplified = toBeSimplified.modifyFromExample(eToFracLn,settings);//e^(ln(a)/b) -> a^(1/b)
		
		if(toBeSimplified instanceof Power) toBeSimplified = toBeSimplified.modifyFromExample(baseToLn,settings);//a^ln(b) -> e^(ln(a)*ln(b))
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	Expr negativeExpoToInv(Power pow,Settings settings) {
		if(pow.getExpo().negative()) {
			pow.setExpo(neg(pow.getExpo()));
			return inv( pow ).simplify(settings);
		}
		return pow;
	}
	
	Expr logToBase(Power pow,Settings settings) {//e^(ln(x)*b)->x^b also works with exponents that are a fraction
		if(pow.getBase() instanceof E) {
			Prod expoProd = null;
			Div expoDiv = null;
			if(pow.getExpo() instanceof Div && ((Div)pow.getExpo()).getNumer() instanceof Prod ) {
				expoDiv = (Div)pow.getExpo();
				expoProd = (Prod)expoDiv.getNumer();
			}else if(pow.getExpo() instanceof Prod) {
				expoProd = (Prod)pow.getExpo();
			}
			
			if(expoProd != null) {
				int logCount = 0;
				int index = -1;
				
				for(int i = 0;i<expoProd.size();i++) {
					if(expoProd.get(i) instanceof Log) {
						if(logCount != 0) return pow;
						logCount++;
						index = i;
					}
				}
				
				if(index == -1) return pow;
				
				pow.setBase(expoProd.get(index).get());
				expoProd.remove(index);
				
				if(expoDiv!=null) {
					pow.setExpo(div(expoProd,expoDiv.getDenom()).simplify(settings));
				}else {
					pow.setExpo(expoProd.simplify(settings));
				}
			}
			
		}
		
		return pow;
	}
	
	Expr expoSumHasLog(Power pow,Settings settings) {
		if(pow.getExpo() instanceof Sum && pow.getBase().equalStruct(e())) {
			Sum expoSum = (Sum)pow.getExpo();
			Prod outerProd = new Prod();
			
			for(int i = 0;i<expoSum.size();i++) {
				if(expoSum.get(i) instanceof Log) {
					outerProd.add(expoSum.get(i).get());
					expoSum.remove(i);
					i--;
				}
			}
			
			if(outerProd.size()>0) {
				outerProd.add(pow);
				return outerProd.simplify(settings);
			}
		}
		return pow;
	}
	
	Expr expoHasIntegerInSum(Power pow,Settings settings) {
		
		if(pow.getBase() instanceof Num && !(pow.getExpo() instanceof Num)) {
			
			pow.setExpo(distr(pow.getExpo()).simplify(settings));//distribute exponent
			
			//if expo is a frac turn it into a mixed fraction sum
			
			Sum fracSum = null;
			if(pow.getExpo() instanceof Div) fracSum = Div.mixedFraction((Div)pow.getExpo());
			if(fracSum!=null) {
				pow.setExpo(fracSum);
			}
			
			if(pow.getExpo() instanceof Sum) {
				Expr expo = pow.getExpo();
				for(int i = 0;i<expo.size();i++) {
					
					if(expo.get(i) instanceof Num) {//the actual expansion
						
						Num num = (Num)expo.get(i);
						expo.remove(i);
						pow.setExpo(pow.getExpo().simplify(settings));
						Expr repl = prod(pow,pow(pow.getBase().copy(),num));
						return repl.simplify(settings);
						
					}
					
					fracSum = null;
					if(expo.get(i) instanceof Div) fracSum = Div.mixedFraction((Div)expo.get(i));
					if(fracSum!=null) {//if expo is a frac turn it into a mixed fraction sum
						expo.set(i, fracSum.get(1));
						expo.add(fracSum.get(0));
					}
					
				}
			}
		}
		return pow;
	}
	
	Expr rootExpand(Power pow) {
		if(pow.getBase() instanceof Num) {
			Num numBase = (Num)pow.getBase();
			BigInteger den = null;
			
			if(pow.getExpo() instanceof Div) {
				Expr e = ((Div)pow.getExpo()).getDenom();
				Num denNum = (Num)e.getCoefficient();
				
				if(!denNum.isComplex() && !denNum.equalStruct(Num.ONE)) den = denNum.realValue;
				
			}
			
			BigInteger negOne = BigInteger.valueOf(-1);
			
			if(numBase.realValue.signum() == -1 && !numBase.realValue.equals(negOne)) {
				pow.setBase(prod(num(-1),num(numBase.realValue.abs())));
				return pow;
			}
			if(den!=null) {
				
				if(numBase.realValue.equals(negOne)) {
					if(!den.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
						return num(-1);
					}
				}
				
				BigInteger i = BigInteger.TWO;
				
				BigInteger leftOver = numBase.realValue;
				
				BigInteger currentPower = i.pow(Math.abs(den.intValue()));
				
				BigInteger product = BigInteger.ONE;
				
				int denVal = Math.abs(den.intValue());
				
				while(leftOver.shiftRight(1).compareTo(currentPower) != -1  ) {//a number can only be divisible by a perfect square that is half its size(I think). example 50 is divisible by 25 which is half its size
					
					
					while(leftOver.mod(currentPower).equals(BigInteger.ZERO)) {
						leftOver = leftOver.divide(currentPower);
						product = product.multiply(currentPower);
					}
					
					i = i.add(BigInteger.ONE);
					if(i.intValue()>100000) break;//give up
					currentPower = i.pow(denVal);
				}
				
				
				if(!product.equals(BigInteger.ONE) && !leftOver.equals(BigInteger.ONE)) {
					Prod newProd = prod(num(product),num(leftOver));
				
					pow.setBase(newProd);
				}
				
			}
			
		}
		return pow;
	}

	Expr productInBase(Power pow,Settings settings) {
		if(pow.getBase() instanceof Prod) {
			Prod casted = (Prod)pow.getBase().copy();
			Div frac = null;
			if(pow.getExpo() instanceof Div) frac = (Div)pow.getExpo();
			boolean createsComplexNumber = false;
			if(!settings.allowComplexNumbers && frac != null && frac.isNumericalAndReal()) {
				if(((Num)frac.getDenom()).realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO)) createsComplexNumber = true;
			}
			
			Prod out = new Prod();
			for(int i = 0;i<casted.size();i++) {
				Expr e = casted.get(i);
				if(createsComplexNumber && e.negative()) return pow;
				out.add(pow(e,pow.getExpo()).simplify(settings) );
				casted.remove(i);
				i--;
			}
			if(casted.size() > 0) {
				pow.setBase(pow.getBase().simplify(settings));
				out.add(pow);
			}
			return out.simplify(settings);
		}
			
			
		return pow;
	}
	
	void perfectPowerBase(Power power,Settings settings) {
		if(power.getBase() instanceof Num) {
			Power pp = perfectPower((Num)power.getBase());
			
			if( ((Num)pp.getExpo()).realValue.equals(BigInteger.ONE) ) return;
			
			power.setBase(pp.getBase());
			
			if(power.getExpo() instanceof Prod) power.getExpo().add(pp.getExpo());
			else power.setExpo(prod(power.getExpo(),pp.getExpo()));
			
			power.setExpo(power.getExpo().simplify(settings));
			
		}
	}
	
	Expr exponentiateIntegers(Power power) {
		if(power.getBase() instanceof Num && power.getExpo() instanceof Num) {
			Num base = (Num)power.getBase();
			Num expo = (Num)power.getExpo();
			
			if(base.equals(Num.NEG_ONE) && expo.equals(Num.NEG_ONE)) {
				return num(-1);
			}else if(!expo.isComplex()) {
				if(expo.signum()!=-1) {
					return base.pow(expo.realValue);
				}
				return inv(base.pow(expo.realValue.negate()));
			}
			
		}
		return power;
	}

	@Override
	public Expr copy() {
		Power out = new Power(getBase().copy(),getExpo().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		if(sqrtObj.fastSimilarStruct(this)) {//fancy and having set to true makes it faster
			out+="sqrt(";
			out+=getBase().toString();
			out+=')';
		}else if(cbrtObj.fastSimilarStruct(this)) {
			out+="cbrt(";
			out+=getBase().toString();
			out+=')';
		}else {
			boolean useParenOnBase = false;//parentheses if
			//base is a negative integer
			//base is a sum or product or power
			if(getBase() instanceof Sum || getBase() instanceof Prod || getBase() instanceof Power || getBase() instanceof Div) useParenOnBase = true;
			if(getBase() instanceof Num) {
				Num baseCasted = (Num)getBase();
				if(baseCasted.realValue.signum() == -1) useParenOnBase = true;
			}
			if(useParenOnBase) out+="(";
			out+=getBase().toString();
			if(useParenOnBase) out+=")";
			out+="^";
			
			boolean useParenOnExpo = false;
			if(getExpo() instanceof Sum || getExpo() instanceof Prod || getExpo() instanceof Power || getExpo() instanceof Div) useParenOnExpo = true;
			if(useParenOnExpo) out+="(";
			out+=getExpo().toString();
			if(useParenOnExpo) out+=")";
		}
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Power) {
			Power otherCasted = (Power)other;
			return getBase().equalStruct(otherCasted.getBase()) && getExpo().equalStruct(otherCasted.getExpo());
		}
		return false;
	}
	@Override
	boolean similarStruct(Expr other,boolean checked) {
		if(other instanceof Power) {
			if(!checked) if(checkForMatches(other) == false) return false;
			Power otherCasted = (Power)other;
			boolean similarBase = false,similarExpo = false;
			if(getBase().fastSimilarStruct(otherCasted.getBase())) similarBase = true;
			if(getExpo().fastSimilarStruct(otherCasted.getExpo())) similarExpo = true;
			
			
			if(similarBase && similarExpo) return true;
		}
		return false;
	}
	
	public static Power cast(Expr e) {
		if(e instanceof Power) {
			return (Power)e;
		}
		return pow(e,num(1));
	}
	
	public static Expr unCast(Expr e) {
		return e.modifyFromExample(powerOfOne, Settings.normal);
	}
	@Override
	public long generateHash() {
		return (getBase().generateHash()+87234*getExpo().generateHash())-8176428751232101230L;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.pow(getBase().convertToFloat(varDefs), getExpo().convertToFloat(varDefs));
	}

}
