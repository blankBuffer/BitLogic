package cas.primitive;
import java.math.BigInteger;

import cas.*;
import cas.calculus.Limit;

public class Power extends Expr{
	
	private static final long serialVersionUID = 3916987907762535821L;
	
	
	
	public void setBase(Expr base) {
		set(0, base);
	}
	public void setExpo(Expr expo) {
		set(1, expo);
	}
	
	public Expr getBase() {
		return get(0);
	}
	public Expr getExpo() {
		return get(1);
	}
	
	public Power(){}//
	
	public Power(Expr base,Expr expo) {
		add(base);
		add(expo);
	}
	
	private static Rule baseHasPower = new Rule("(a^b)^c->a^(b*c)","~(isType(result(b/2),num)&allowAbs())","base has power",Rule.EASY);
	private static Rule baseHasPowerAbs = new Rule("(a^b)^c->abs(a)^(b*c)","isType(result(b/2),num)&allowAbs()","base has power",Rule.EASY);
	
	private static Rule expoOfZero = new Rule("a^0->1","~eval(a=0)","exponent is zero",Rule.VERY_EASY);
	private static Rule isI = new Rule("sqrt(-1)->i","allowComplexNumbers()","is equal to i",Rule.EASY);
	private static Rule eToLn = new Rule("e^ln(a)->a","e to ln",Rule.EASY);
	private static Rule eToFracLn = new Rule("e^(ln(a)/b)->a^(1/b)","e to fraction with ln",Rule.UNCOMMON);
	private static Rule zeroToExpo = new Rule("0^x->0","~eval(x=0)","base is zero",Rule.VERY_EASY);
	private static Rule baseToLn = new Rule("a^ln(b)->e^(ln(a)*ln(b))","base not e and expo has log",Rule.UNCOMMON);
	private static Rule expOfLambertW = new Rule("e^(lambertW(x))->x/lambertW(x)","e to lambert w",Rule.UNCOMMON);
	private static Rule expOfLambertWProd = new Rule("e^(lambertW(x)*n)->x^n/lambertW(x)^n","e to lambert w product",Rule.TRICKY);
	private static Rule powerOfOne = new Rule("a^1->a","exponent is one",Rule.VERY_EASY);
	private static Rule fracInBase = new Rule("(a/b)^n->a^n/b^n","base is a fraction",Rule.EASY);
	private static Rule sqrtOneMinusSin = new Rule("sqrt(1-sin(x))->sqrt(2)*sin(pi/4-x/2)","sqrt of 1 minus sin",Rule.UNCOMMON);
	private static Rule sqrtOneMinusCos = new Rule("sqrt(1-cos(x))->sqrt(2)*sin(x/2)","sqrt of 1 minus cos",Rule.UNCOMMON);
	private static Rule sqrtOnePlusSin = new Rule("sqrt(1+sin(x))->sqrt(2)*cos(pi/4-x/2)","sqrt of 1 plus sin",Rule.UNCOMMON);
	private static Rule sqrtOnePlusCos = new Rule("sqrt(1+cos(x))->sqrt(2)*cos(x/2)","sqrt of 1 plus cos",Rule.UNCOMMON);
	private static Rule baseOfPowerIsAbsExpoEven = new Rule("abs(a)^b->a^b","~isType(result(b/2),div)","base of power is absolute value and exponent is divisible by 2",Rule.TRICKY);
	
	private static Rule oneToExpo = new Rule("base is one",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power power = (Power)e;
			
			if(Limit.stripDirection(power.getBase()).equals(Num.ONE) && !power.getExpo().equals(inf())){
				short direction = Limit.getDirection(power.getBase());
				
				direction = power.getExpo().negative() ? Limit.flipDirection(direction) : direction;
				
				Expr out = Limit.applyDirection( num(1),direction);
				return out;
			}
			
			return power;
		}
		
	};
	
	private static Rule exponentiateIntegers = new Rule("exponentiate integers",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power power = (Power)e;
			if(power.getBase() instanceof Num && power.getExpo() instanceof Num) {
				Num base = (Num)power.getBase();
				Num expo = (Num)power.getExpo();
				
				if(power.contains(Num.ZERO)) return power;
				
				if(!expo.isComplex() && expo.realValue.compareTo(BigInteger.valueOf(10000))==-1) {
					if(expo.signum()!=-1 ) {
						Expr result = base.pow(expo.realValue);
						return result;
					}
				}
				
			}
			return power;
		}
	};
	private static Rule negativeExpoToInv = new Rule("negative expoonent to inverse",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power pow = (Power)e;
			if(pow.getExpo().negative()) {
				pow.setExpo(neg(pow.getExpo()));
				
				Expr result = inv( pow ).simplify(settings);
				return result;
			}
			return pow;
			
		}
	};
	private static Rule factorExponent = new Rule("factoring the exponent",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power power = (Power)e;
			
			Expr factoredExpo = factor(power.getExpo()).simplify(settings);
			
			if(!factoredExpo.equals(power.getExpo())){
				Expr result = pow(power.getBase().copy(),factoredExpo);
				return result;
			}
			
			return power;
		}
	};
	private static Rule factorBase = new Rule("factoring the base",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power power = (Power)e;
			
			Expr factoredBase = factor(power.getBase()).simplify(settings);
			
			if(!factoredBase.equals(power.getBase())){
				Expr result = pow(factoredBase,power.getExpo().copy());
				return result;
			}
			
			return power;
		}
	};
	private static Rule logInExpoProdToBase = new Rule("e to exponent product with single ln",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;

		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power pow = (Power)e;
			
			if(pow.getBase().equals(e())) {
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
						if(expoProd.get(i) instanceof Ln) {
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
	};
	private static Rule expoSumHasLog = new Rule("base is e and expo has sum with logs",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power pow = (Power)e;
			if(pow.getExpo() instanceof Sum && pow.getBase().equals(e())) {
				Sum expoSum = (Sum)pow.getExpo();
				Prod outerProd = new Prod();
				for(int i = 0;i<expoSum.size();i++) {
					if(expoSum.get(i) instanceof Ln) {
						outerProd.add(expoSum.get(i).get());
						expoSum.remove(i);
						i--;
					}
				}
				
				if(outerProd.size()>0) {
					outerProd.add(pow);
					Expr result = outerProd.simplify(settings);
					return result;
				}
			}
			return pow;
			
		}
		
	};
	private static Rule expoHasIntegerInSum = new Rule("exponent has integer in sum and base is integer",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power pow = (Power)e;
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
							Expr repl = prod(pow,pow(pow.getBase().copy(),num)).simplify(settings);
							return repl;
							
						}
						
						fracSum = null;
						if(expo.get(i) instanceof Div) fracSum = Div.mixedFraction((Div)expo.get(i));
						if(fracSum!=null) {//if expo is a frac turn it into a mixed fraction sum
							expo.set(i, fracSum.get(1));//fractional component
							expo.add(fracSum.get(0));//integer component
						}
						
					}
				}
			}
			return pow;
		}
		
	};
	
	private static Rule rootExpand = new Rule("break apart the base into rootable parts",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power pow = (Power)e;
			
			if(pow.getBase() instanceof Num) {
				Num numBase = (Num)pow.getBase();
				BigInteger den = null;
				Expr numerOfFrac = null;
				
				if(pow.getExpo() instanceof Div) {
					numerOfFrac = ((Div)pow.getExpo()).getNumer();
					Expr denomOfFrac = ((Div)pow.getExpo()).getDenom();
					
					Num denNum = (Num)denomOfFrac.getCoefficient();
					
					if(!denNum.isComplex() && !denNum.equals(Num.ONE)) den = denNum.realValue;//denominator captured
					
				}
				
				BigInteger negOne = BigInteger.valueOf(-1);
				
				if(numBase.realValue.signum() == -1 && !numBase.realValue.equals(negOne)) {//if the base is negative and not negative one
					pow.setBase(prod(num(-1),num(numBase.realValue.abs())));//split and let let the rule "productInBase" handle this
					return pow;
				}
				if(den!=null) {
					
					if(numBase.realValue.equals(negOne)) {//handle odd denominators with base negative one. Example (-1)^(x/3) -> (-1)^x
						if(!den.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
							Expr result = pow(num(-1),numerOfFrac);
							return result;
						}
					}
					
					BigInteger i = BigInteger.TWO;//base incremental value
					
					BigInteger leftOver = numBase.realValue;
					
					BigInteger currentPower = i.pow(Math.abs(den.intValue()));//first test
					
					BigInteger product = BigInteger.ONE;
					
					int denVal = Math.abs(den.intValue());
					
					while(leftOver.shiftRight(1).compareTo(currentPower) != -1  ) {//a number can only be divisible by a perfect square that is half its size(simply a conjecture). example 50 is divisible by 25 which is half its size
						
						
						while(leftOver.mod(currentPower).equals(BigInteger.ZERO)) {
							leftOver = leftOver.divide(currentPower);
							product = product.multiply(currentPower);//Accumulative product that is root-able
						}
						
						i = i.add(BigInteger.ONE);//increment
						if(i.intValue()>100000) break;//give up
						currentPower = i.pow(denVal);//update currentPower value
					}
					
					
					if(!product.equals(BigInteger.ONE) && !leftOver.equals(BigInteger.ONE)) {
						Prod newProd = prod(num(product),num(leftOver));
						pow.setBase(newProd);//split the base with the root-able component and let let the rule "productInBase" handle this
					}
					
				}
				
			}
			return pow;
		}
	};
	
	private static Rule perfectPowerInBase = new Rule("the base is a perfect power",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power power = (Power)e;
			
			if(power.getBase() instanceof Num && !(power.getExpo() instanceof Num)) {
				Power pp = perfectPower((Num)power.getBase());
				
				if( ((Num)pp.getExpo()).realValue.equals(BigInteger.ONE) ) return power;
				
				power.setBase(pp.getBase());
				
				if(power.getExpo() instanceof Prod) power.getExpo().add(pp.getExpo());
				else power.setExpo(prod(power.getExpo(),pp.getExpo()));
				
				power.setExpo(power.getExpo().simplify(settings));
				
			}
			
			return power;
		}
		
	};
	
	private static Rule productInBase = new Rule("the base is a product",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power pow = (Power)e;
			
			if(pow.getBase() instanceof Prod) {
				Prod casted = (Prod)pow.getBase().copy();
				Div frac = null;
				if(pow.getExpo() instanceof Div) frac = (Div)pow.getExpo();
				boolean createsComplexNumber = false;
				if(!settings.allowComplexNumbers && frac != null && frac.isNumericalAndReal()) {
					if(((Num)frac.getDenom()).realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO)) createsComplexNumber = true;//root in the form (x)^(a/(2*n))
				}
				
				Prod out = new Prod();
				for(int i = 0;i<casted.size();i++) {
					Expr expr = casted.get(i);
					if(createsComplexNumber && expr.negative()){
						pow.setBase(distr(pow.getBase()).simplify(settings));
						return pow;
					}
					out.add(pow(expr,pow.getExpo()).simplify(settings) );
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
		
	};
	
	static Rule powersWithEpsilonOrInf = new Rule("power with epsilon or infinity",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power pow = (Power)e;
			
			if(pow.getBase().equals(epsilon()) && !pow.getExpo().negative() && !Limit.zeroOrEpsilon(pow.getExpo())){
				Expr out = epsilon();
				
				return out;
			}else if(pow.getBase().equals(inf()) && !pow.getExpo().negative() && !Limit.zeroOrEpsilon(pow.getExpo())){
				Expr out = inf();
				
				return out;
			}else if(pow.getExpo().equals(inf())){
				Expr baseMinusOne = factor(sub(pow.getBase(),Num.ONE)).simplify(settings);
				
				if(!baseMinusOne.negative() && !Limit.zeroOrEpsilon(baseMinusOne) || pow.getBase().equals(e()) || pow.getBase().equals(pi())){
					Expr out = inf();
					
					return out;
				}
				
			}
			
			return pow;
		}
	};
	
	static Rule eulersIdentity = new Rule("eulers identity",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Power pow = (Power)e;
			
			if(pow.getExpo() instanceof Prod || pow.getExpo() instanceof Div) {
				Prod prod = null;
				Div div = null;
				if(pow.getExpo() instanceof Prod) {
					prod = (Prod)pow.getExpo();
					div = Div.cast(prod);
				}else {
					if( ((Div)pow.getExpo()).getNumer() instanceof Prod ) {
						div = (Div) pow.getExpo();
						prod = (Prod) div.getNumer();
					}else {
						return pow;
					}
				}
				
				boolean hasI = false;
				for(int i = 0;i<prod.size();i++) {
					if(prod.get(i).equals(Num.I)) {
						hasI = true;
						prod.remove(i);
						break;		
					}
				}
				if(hasI) return sum(cos(div),prod(num(0,1),sin(div))).simplify(settings);
			}
			
			return pow;
		}
		
	};
	
	static Rule sqrtOfSqrtSum = new Rule("sqrt(k*sqrt(b)+a)->sqrt((a+sqrt(a^2-b*k^2))/2)+sqrt((a-sqrt(a^2-b*k^2))/2)*sign(k)","isType(result(sqrt(a^2-b*k^2)),num)","square root of a square root sum",Rule.UNCOMMON);
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				isI,
				powersWithEpsilonOrInf,
				sqrtOneMinusSin,
				sqrtOneMinusCos,
				sqrtOnePlusSin,
				sqrtOnePlusCos,
				sqrtOfSqrtSum,
				factorExponent,
				baseHasPower,
				eulersIdentity,
				baseHasPowerAbs,
				baseOfPowerIsAbsExpoEven,
				negativeExpoToInv,
				oneToExpo,
				logInExpoProdToBase,
				expOfLambertW,
				expOfLambertWProd,
				rootExpand,
				perfectPowerInBase,
				productInBase,
				factorBase,
				fracInBase,
				productInBase,//second time
				expoHasIntegerInSum,
				expoSumHasLog,
				exponentiateIntegers,
				expoOfZero,
				zeroToExpo,
				powerOfOne,
				eToLn,
				eToFracLn,
				baseToLn
			);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	@Override
	public String toString() {
		String out = "";
		
		if(Rule.fastSimilarStruct(sqrtObj,this)) {//fancy and having set to true makes it faster
			out+="sqrt(";
			out+=getBase().toString();
			out+=')';
		}else if(Rule.fastSimilarStruct(cbrtObj,this)) {
			out+="cbrt(";
			out+=getBase().toString();
			out+=')';
		}else
		
		{
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
	
	public static Power cast(Expr e) {
		if(e instanceof Power) {
			return (Power)e;
		}
		return pow(e,num(1));
	}
	
	public static Expr unCast(Expr e) {
		return powerOfOne.applyRuleToExpr(e, Settings.normal);
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.pow(getBase().convertToFloat(varDefs), getExpo().convertToFloat(varDefs));
	}

}
