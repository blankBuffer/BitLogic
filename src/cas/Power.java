package cas;
import java.math.BigInteger;

public class Power extends Expr{
	
	private static final long serialVersionUID = 3916987907762535821L;
	
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
	
	Power(){}//
	
	public Power(Expr base,Expr expo) {
		add(base);
		add(expo);
	}
	
	private static Rule baseHasPower = new Rule("(a^b)^c=a^(b*c)","base has power",Rule.EASY);
	private static Rule expoOfZero = new Rule("a^0=1","exponent is zero",Rule.VERY_EASY);
	private static Rule eToLn = new Rule("e^ln(a)=a","e to ln",Rule.EASY);
	private static Rule eToFracLn = new Rule("e^(ln(a)/b)=a^(1/b)","e to fraction with ln",Rule.UNCOMMON);
	private static Rule oneToExpo = new Rule("1^x=1","base is one",Rule.VERY_EASY);
	private static Rule zeroToExpo = new Rule("0^x=0","base is zero",Rule.VERY_EASY);
	private static Rule baseToLn = new Rule("a^ln(b)=e^(ln(a)*ln(b))","base not e and expo has log",Rule.UNCOMMON);
	private static Rule expOfLambertW = new Rule("e^(w(x))=x/w(x)","e to lambert w",Rule.UNCOMMON);
	private static Rule expOfLambertWProd = new Rule("e^(w(x)*n)=x^n/w(x)^n","e to lambert w product",Rule.TRICKY);
	private static Rule powerOfOne = new Rule("a^1=a","exponent is one",Rule.VERY_EASY);
	private static Rule fracInBase = new Rule("(a/b)^n=a^n/b^n","base is a fraction",Rule.EASY);
	
	private static Rule exponentiateIntegers = new Rule("exponentiate integers",Rule.VERY_EASY){
		@Override
		public void init(){
			example = "2^3=8";
		}
		
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power power = null;
			if(e instanceof Power){
				power = (Power)e;
			}else{
				return e;
			}
			if(power.getBase() instanceof Num && power.getExpo() instanceof Num) {
				Num base = (Num)power.getBase();
				Num expo = (Num)power.getExpo();
				
				if(!expo.isComplex() && expo.realValue.compareTo(BigInteger.valueOf(10000))==-1) {
					if(expo.signum()!=-1 ) {
						Expr result = base.pow(expo.realValue);
						verboseMessage(e,result);
						return result;
					}
				}
				
			}
			return power;
		}
	};
	private static Rule negativeExpoToInv = new Rule("negative expoonent to inverse",Rule.EASY){
		@Override
		public void init(){
			example = "a^(-x)=1/a^x";
		}
		
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power pow = null;
			if(e instanceof Power){
				pow = (Power)e;
			}else{
				return e;
			}
			Expr original = e.copy();
			if(pow.getExpo().negative()) {
				pow.setExpo(neg(pow.getExpo()));
				
				Expr result = inv( pow ).simplify(settings);
				verboseMessage(original,result);
				return result;
			}
			return pow;
			
		}
	};
	private static Rule factorExponent = new Rule("factoring the exponent",Rule.TRICKY){
		@Override
		public void init(){
			example = "a^(2*b+2*c)=a^(2*(b+c))";
		}
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power power = null;
			if(e instanceof Power){
				power = (Power)e;
			}else{
				return e;
			}
			
			Expr factoredExpo = factor(power.getExpo()).simplify(settings);
			
			if(!factoredExpo.equalStruct(power.getExpo())){
				Expr result = pow(power.getBase().copy(),factoredExpo);
				verboseMessage(e,result);
				return result;
			}
			
			return power;
		}
	};
	private static Rule factorBase = new Rule("factoring the base",Rule.TRICKY){
		@Override
		public void init(){
			example = "(a+a*b)^x=((1+b)*a)^x";
		}
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power power = null;
			if(e instanceof Power){
				power = (Power)e;
			}else{
				return e;
			}
			
			Expr factoredBase = factor(power.getBase()).simplify(settings);
			
			if(!factoredBase.equalStruct(power.getBase())){
				Expr result = pow(factoredBase,power.getExpo().copy());
				verboseMessage(e,result);
				return result;
			}
			
			return power;
		}
	};
	private static Rule logInExpoProdToBase = new Rule("e to exponent product with single ln",Rule.UNCOMMON){
		String standardExample = "e^(ln(x)*b)=x^b";
		String divExample = "e^((ln(x)*b)/c)=x^(b/c)";
		
		@Override
		public void init(){
			example = "e^(ln(x)*b)=x^b";
		}
		
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power pow = null;
			if(e instanceof Power){
				pow = (Power)e;
			}else{
				return e;
			}
			
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
					
					Expr original = e.copy();
					
					pow.setBase(expoProd.get(index).get());
					expoProd.remove(index);
					
					if(expoDiv!=null) {
						example = divExample;
						pow.setExpo(div(expoProd,expoDiv.getDenom()).simplify(settings));
						verboseMessage(original,pow);
					}else {
						example = standardExample;
						pow.setExpo(expoProd.simplify(settings));
						verboseMessage(original,pow);
					}
				}
				
			}
			
			return pow;
		}
	};
	private static Rule expoSumHasLog = new Rule("base is e and expo has sum with logs",Rule.TRICKY){
		
		@Override
		public void init(){
			example = "e^(ln(x)+y)=x*e^y";
		}
		
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power pow = null;
			if(e instanceof Power){
				pow = (Power)e;
			}else{
				return e;
			}
			if(pow.getExpo() instanceof Sum && pow.getBase().equalStruct(e())) {
				Sum expoSum = (Sum)pow.getExpo();
				Prod outerProd = new Prod();
				Expr original = e.copy();
				for(int i = 0;i<expoSum.size();i++) {
					if(expoSum.get(i) instanceof Log) {
						outerProd.add(expoSum.get(i).get());
						expoSum.remove(i);
						i--;
					}
				}
				
				if(outerProd.size()>0) {
					outerProd.add(pow);
					Expr result = outerProd.simplify(settings);
					verboseMessage(original,result);
					return result;
				}
			}
			return pow;
			
		}
		
	};
	private static Rule expoHasIntegerInSum = new Rule("exponent has integer in sum and base is integer",Rule.TRICKY){
		
		@Override
		public void init(){
			example = "2^(x+5/2)=4*2^(1/2+x)";
		}
		
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power pow = null;
			if(e instanceof Power){
				pow = (Power)e;
			}else{
				return e;
			}
			if(pow.getBase() instanceof Num && !(pow.getExpo() instanceof Num)) {
				
				Expr original = e.copy();
				
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
							verboseMessage(original,repl);
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
		
		String typicalCase = "54^(x/3)=3^x*2^(x/3)";
		String baseNegativeOne = "(-1)^(x/3)=(-1)^x";
		String negativeBase = "(-16)^(x/3)=(-1)^x*2^((4*x)/3)";
		
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power pow = null;
			if(e instanceof Power){
				pow = (Power)e;
			}else{
				return e;
			}
			
			if(pow.getBase() instanceof Num) {
				Num numBase = (Num)pow.getBase();
				BigInteger den = null;
				Expr numerOfFrac = null;
				
				if(pow.getExpo() instanceof Div) {
					numerOfFrac = ((Div)pow.getExpo()).getNumer();
					Expr denomOfFrac = ((Div)pow.getExpo()).getDenom();
					
					Num denNum = (Num)denomOfFrac.getCoefficient();
					
					if(!denNum.isComplex() && !denNum.equalStruct(Num.ONE)) den = denNum.realValue;//denominator captured
					
				}
				
				BigInteger negOne = BigInteger.valueOf(-1);
				
				Expr original = e.copy();
				
				if(numBase.realValue.signum() == -1 && !numBase.realValue.equals(negOne)) {//if the base is negative and not negative one
					pow.setBase(prod(num(-1),num(numBase.realValue.abs())));//split and let let the rule "productInBase" handle this
					example = negativeBase;
					verboseMessage(original,pow);
					return pow;
				}
				if(den!=null) {
					
					if(numBase.realValue.equals(negOne)) {//handle odd denominators with base negative one. Example (-1)^(x/3) -> (-1)^x
						if(!den.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
							Expr result = pow(num(-1),numerOfFrac);
							example = baseNegativeOne;
							verboseMessage(original,result);
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
						example = typicalCase;
						pow.setBase(newProd);//split the base with the root-able component and let let the rule "productInBase" handle this
						verboseMessage(original,pow);
					}
					
				}
				
			}
			return pow;
		}
	};
	
	private static Rule perfectPowerInBase = new Rule("the base is a perfect power",Rule.UNCOMMON){
		
		@Override
		public void init(){
			example = "25^x=5^(2*x)";
		}
		
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power power = null;
			if(e instanceof Power){
				power = (Power)e;
			}else{
				return e;
			}
			
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
		
		@Override
		public void init(){
			example = "(a*b)^x=a^x*b^x";
		}
		
		@Override
		Expr applyRuleToExpression(Expr e,Settings settings){
			Power pow = null;
			if(e instanceof Power){
				pow = (Power)e;
			}else{
				return e;
			}
			
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
					Expr expr = casted.get(i);
					if(createsComplexNumber && expr.negative()) return pow;
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
	
	Rule[] ruleSequence = {
			factorExponent,
			baseHasPower,
			negativeExpoToInv,
			oneToExpo,
			fracInBase,
			logInExpoProdToBase,
			expOfLambertW,
			expOfLambertWProd,
			rootExpand,
			perfectPowerInBase,
			productInBase,
			factorBase,
			productInBase,//second time
			expoHasIntegerInSum,
			expoSumHasLog,
			exponentiateIntegers,
			expoOfZero,
			zeroToExpo,
			powerOfOne,
			eToLn,
			eToFracLn,
			baseToLn,
			};

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		for (Rule r:ruleSequence){
			toBeSimplified = r.applyRuleToExpression(toBeSimplified, settings);
		}
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
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
	
	public static Power cast(Expr e) {
		if(e instanceof Power) {
			return (Power)e;
		}
		return pow(e,num(1));
	}
	
	public static Expr unCast(Expr e) {
		return powerOfOne.applyRuleToExpression(e, Settings.normal);
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.pow(getBase().convertToFloat(varDefs), getExpo().convertToFloat(varDefs));
	}

}
