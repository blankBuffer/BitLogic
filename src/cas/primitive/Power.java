package cas.primitive;
import java.math.BigInteger;

import cas.*;
import cas.bool.BoolState;
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
	
	private static Rule baseHasPower = new Rule("(a^b)^c->a^(b*c)","isType(b,num)&isType(c,num)|eval(a>0)","base has power");
	private static Rule baseHasPowerAbs = new Rule("(a^b)^c->abs(a)^(b*c)","isType(result(b/2),num)&~allowComplexNumbers()","base has power");
	
	private static Rule expoOfZero = new Rule("a^0->1","~eval(a=0)","exponent is zero");
	private static Rule isI = new Rule("sqrt(-1)->i","allowComplexNumbers()","is equal to i");
	private static Rule eToLn = new Rule("e^ln(a)->a","e to ln");
	private static Rule eToFracLn = new Rule("e^(ln(a)/b)->a^(1/b)","e to fraction with ln");
	private static Rule zeroToExpo = new Rule("0^x->0","~eval(x=0)","base is zero");
	private static Rule baseToLn = new Rule("a^ln(b)->e^(ln(a)*ln(b))","base not e and expo has log");
	private static Rule expOfLambertW = new Rule("e^(lambertW(x))->x/lambertW(x)","e to lambert w");
	private static Rule expOfLambertWProd = new Rule("e^(lambertW(x)*n)->x^n/lambertW(x)^n","e to lambert w product");
	private static Rule powerOfOne = new Rule("a^1->a","exponent is one");
	private static Rule fracInBase = new Rule("(a/b)^n->a^n/b^n","base is a fraction");
	private static Rule sqrtOneMinusSin = new Rule("sqrt(1-sin(x))->sqrt(2)*sin(pi/4-x/2)","sqrt of 1 minus sin");
	private static Rule sqrtOneMinusCos = new Rule("sqrt(1-cos(x))->sqrt(2)*sin(x/2)","sqrt of 1 minus cos");
	private static Rule sqrtOnePlusSin = new Rule("sqrt(1+sin(x))->sqrt(2)*cos(pi/4-x/2)","sqrt of 1 plus sin");
	private static Rule sqrtOnePlusCos = new Rule("sqrt(1+cos(x))->sqrt(2)*cos(x/2)","sqrt of 1 plus cos");
	private static Rule baseOfPowerIsAbsExpoEven = new Rule("abs(a)^b->a^b","~isType(result(b/2),div)&~allowComplexNumbers()","base of power is absolute value and exponent is divisible by 2");
	
	private static Rule oneToExpo = new Rule("base is one"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power power = (Power)e;
			
			if(Limit.stripDirection(power.getBase()).equals(Num.ONE) && !power.getExpo().equals(Var.INF)){
				short direction = Limit.getDirection(power.getBase());
				
				direction = power.getExpo().negative() ? Limit.flipDirection(direction) : direction;
				
				Expr out = Limit.applyDirection( num(1),direction);
				return out;
			}
			
			return power;
		}
		
	};
	
	private static Rule exponentiateIntegers = new Rule("exponentiate integers"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
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
	private static Rule negativeExpoToInv = new Rule("negative expoonent to inverse"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			if(pow.getExpo().negative()) {
				pow.setExpo(neg(pow.getExpo()));
				
				Expr result = inv( pow ).simplify(casInfo);
				return result;
			}
			return pow;
			
		}
	};
	private static Rule factorExponent = new Rule("factoring the exponent"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power power = (Power)e;
			
			Expr factoredExpo = factor(power.getExpo()).simplify(casInfo);
			
			if(!factoredExpo.equals(power.getExpo())){
				Expr result = pow(power.getBase().copy(),factoredExpo);
				return result;
			}
			
			return power;
		}
	};
	private static Rule factorBase = new Rule("factoring the base"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power power = (Power)e;
			
			Expr factoredBase = factor(power.getBase()).simplify(casInfo);
			
			if(!factoredBase.equals(power.getBase())){
				Expr result = pow(factoredBase,power.getExpo().copy());
				return result;
			}
			
			return power;
		}
	};
	private static Rule logInExpoProdToBase = new Rule("e to exponent product with single ln"){
		private static final long serialVersionUID = 1L;

		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			
			if(pow.getBase().equals(Var.E)) {
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
						pow.setExpo(div(expoProd,expoDiv.getDenom()).simplify(casInfo));
					}else {
						pow.setExpo(expoProd.simplify(casInfo));
					}
				}
				
			}
			
			return pow;
		}
	};
	private static Rule expoSumHasLog = new Rule("base is e and expo has sum with logs"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			if(pow.getExpo() instanceof Sum && pow.getBase().equals(Var.E)) {
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
					Expr result = outerProd.simplify(casInfo);
					return result;
				}
			}
			return pow;
			
		}
		
	};
	private static Rule expoHasIntegerInSum = new Rule("exponent has integer in sum and base is integer"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			if(pow.getBase() instanceof Num && !(pow.getExpo() instanceof Num)) {
				
				pow.setExpo(distr(pow.getExpo()).simplify(casInfo));//distribute exponent
				
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
							pow.setExpo(pow.getExpo().simplify(casInfo));
							Expr repl = prod(pow,pow(pow.getBase().copy(),num)).simplify(casInfo);
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
	
	private static Rule perfectPowerInBase = new Rule("the base is a perfect power"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power power = (Power)e;
			
			if(power.getBase() instanceof Num && !(power.getExpo() instanceof Num)) {
				Power pp = perfectPower((Num)power.getBase());
				
				if( pp.getExpo().equals(Num.ONE) ) return power;
				
				power.setBase(pp.getBase());
				
				if(power.getExpo() instanceof Prod) power.getExpo().add(pp.getExpo());
				else power.setExpo(prod(power.getExpo(),pp.getExpo()));
				
				power.setExpo(power.getExpo().simplify(casInfo));
				
			}
			
			return power;
		}
		
	};
	
	private static Rule productInBase = new Rule("the base is a product"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			
			if(pow.getBase() instanceof Prod && !(pow.getExpo() instanceof Div)) {
				Prod casted = (Prod)pow.getBase().copy();
				Div frac = null;
				if(pow.getExpo() instanceof Div) frac = (Div)pow.getExpo();
				boolean createsComplexNumber = false;
				if(!casInfo.allowComplexNumbers() && frac != null && frac.isNumericalAndReal()) {
					if(((Num)frac.getDenom()).realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO)) createsComplexNumber = true;//root in the form (x)^(a/(2*n))
				}
				
				Prod out = new Prod();
				for(int i = 0;i<casted.size();i++) {
					Expr expr = casted.get(i);
					if(createsComplexNumber && expr.negative()){
						pow.setBase(distr(pow.getBase()).simplify(casInfo));
						return pow;
					}
					out.add(pow(expr,pow.getExpo()).simplify(casInfo) );
					casted.remove(i);
					i--;
				}
				if(casted.size() > 0) {
					pow.setBase(pow.getBase().simplify(casInfo));
					out.add(pow);
				}
				return out.simplify(casInfo);
			}
			
			return pow;
		}
		
	};
	
	static Rule powersWithEpsilonOrInf = new Rule("power with epsilon or infinity"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			
			if(pow.getExpo().equals(Var.INF)){//x^inf
				if(!Limit.stripDirection(pow.getBase()).equals(Num.ONE)) {
					if(eval(equGreater(pow.getBase(),num(1))).simplify(casInfo).equals(BoolState.TRUE)) {
						return inf();
					}
				}
				
			}else if(pow.getExpo().contains(Var.EPSILON) && !Limit.isEpsilon(pow.getBase()) && !Limit.isInf(pow.getBase()) && !pow.getBase().equals(Num.ONE)) {//x^(y+epsilon)
				
				short direction = Limit.getDirection(pow.getExpo());
				pow.setExpo(Limit.stripDirection(pow.getExpo()));
				
				if(direction != Limit.NONE) {
					if(eval(equLess(pow.getBase(),num(1))).simplify(casInfo).equals(BoolState.TRUE)) {
						direction = (short) -direction;
					}
					return Limit.applyDirection(pow, direction).simplify(casInfo);
				}
			}else if(pow.getBase().equals(Var.EPSILON) && !pow.getExpo().negative() ) {
				return epsilon();
			}else if(!pow.getBase().contains(Var.INF) && !pow.getExpo().negative() && !Limit.zeroOrEpsilon(pow.getExpo())){//(x+epsilon)^y cases
				
				
				if(Limit.zeroOrEpsilon(pow.getBase())) return pow;//let product in base separate out the negative
				
				short direction = Limit.getDirection(pow.getBase());
				if(direction == Limit.NONE) return pow;
				
				pow.setBase(Limit.stripDirection(pow.getBase()));
				
				if(pow.getBase().negative()) direction = (short) -direction;//(-2-epsilon)^3 -> -8+epsilon
				
				return Limit.applyDirection(pow.simplify(casInfo), direction);
			}else if(pow.getBase().equals(Var.INF) && !pow.getExpo().negative() && !Limit.zeroOrEpsilon(pow.getExpo())){//inf^x
				return inf();
			}
			
			return pow;
		}
	};
	
	static Rule eulersIdentity = new Rule("eulers identity") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
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
				if(hasI) return sum(cos(div),prod(num(0,1),sin(div))).simplify(casInfo);
			}
			
			return pow;
		}
		
	};
	
	static Rule rootHasCancelingPower = new Rule("root has power or number inside that cancels and goes outside root") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			
			if(pow.getExpo() instanceof Div && pow.getBase() instanceof Prod) {
				Prod baseProd = (Prod)pow.getBase();
				Div expoDiv = (Div)pow.getExpo();
				
				Prod out = new Prod();
				for(int i = 0;i<baseProd.size();i++) {
					if(baseProd.get(i) instanceof Power) {
						Power subPow = (Power)baseProd.get(i);
						
						if(subPow.getExpo() instanceof Div) {
							Div subPowExpoDiv = (Div)subPow.getExpo();
							if(subPowExpoDiv.getNumer().equals(expoDiv.getDenom())) {
								
								out.add(pow(subPow.getBase(),div(expoDiv.getNumer(),subPowExpoDiv.getDenom())));
								
								baseProd.remove(i);
								i--;
							}
						}else {
							if(expoDiv.getDenom().equals(subPow.getExpo())) {
								
								out.add(pow(subPow,expoDiv));
								
								baseProd.remove(i);
								i--;
							}
						}
						
					}else if(isPositiveRealNum(expoDiv.getDenom()) && isRealNum(baseProd.get(i)) ) {
						
						Expr computed = rootNumSimp.applyRuleToExpr(pow(baseProd.get(i),pow.getExpo()), casInfo);
						if(computed instanceof Num) {
							baseProd.remove(i);
							i--;
							out.add(computed);
						}else if(computed instanceof Prod) {
							out.add(computed.get(0));
							baseProd.set(i, computed.get(1).get());
							i--;
						}
						
					}
				}
				if(out.size()>0) {
					out.add(pow);
					return out.simplify(casInfo);
				}
			}
			
			return pow;
		}
	};
	
	static Rule rootNumSimp = new Rule("root of a number") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			if(pow.getExpo() instanceof Div) {
				Div expoDiv = (Div)pow.getExpo();
				
				if(isPositiveRealNum(expoDiv.getDenom()) && pow.getBase() instanceof Num && (isRealNum(pow.getBase()) || casInfo.allowComplexNumbers()) ) {
					Num denomNum = (Num)expoDiv.getDenom();
					
					Num baseNum = (Num)pow.getBase();
				
					//if the base is negative and the denominator is even
					
					if(isPositiveRealNum(baseNum)) {
						
						//this portion works similar to the root expand rule
						BigInteger root = denomNum.realValue;
						BigInteger num = baseNum.realValue;
						
						
						BigInteger ans = bigRoot( num , root );
						if(ans.pow(root.intValue()).equals(num)) {
							return pow(num(ans),expoDiv.getNumer()).simplify(casInfo);
						}
						BigInteger factor = divisibleRoot(num, root);
						if(!factor.equals(BigInteger.ONE)) {
							BigInteger outerNum = bigRoot( factor , root );
							return prod( pow(num(outerNum),expoDiv.getNumer()).simplify(casInfo), pow(num(num.divide(factor)),expoDiv) );
							
						}
					}else if(casInfo.allowComplexNumbers() && denomNum.equals(Num.TWO)){//square root of a complex or negative number
						BigInteger sumOfSquares = baseNum.realValue.pow(2).add(baseNum.imagValue.pow(2));
						BigInteger root = sumOfSquares.sqrt();
						
						//sqrt(a+b*i) -> (sqrt(sqrt(a^2+b^2)+a)+sign(b)*sqrt(sqrt(a^2+b^2)-a))/sqrt(2)
						if(root.pow(2).equals(sumOfSquares)) {
							
							Expr out = div(pow(sum( sqrt( num(root.add(baseNum.realValue)) ) , prod(num(0,baseNum.imagValue.signum() == -1? -1 : 1),sqrt( num(root.subtract(baseNum.realValue)) )) ),expoDiv.getNumer()), pow(num(2),expoDiv) );
							return out.simplify(casInfo);
							
						}
						
						
					}
				}
			}
			return pow;
		}
	};
	
	static Rule rootInRoot = new Rule("root in root") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			
			if(pow.getBase() instanceof Prod && pow.getExpo() instanceof Div) {
				Prod innerProd = (Prod)pow.getBase();
				
				Prod out = new Prod();
				
				for(int i = 0;i<innerProd.size();i++) {
					if(innerProd.get(i) instanceof Power && ((Power)innerProd.get(i)).getExpo() instanceof Div ) {
						Power innerPow = (Power)innerProd.get(i);
						
						out.add(pow(innerPow.getBase(),prod(pow.getExpo(),innerPow.getExpo())));
						innerProd.remove(i);
						i--;
					}
				}
				
				if(out.size()>0) {
					out.add(pow);
					return out.simplify(casInfo);
				}
				
			}
			
			return pow;
		}
	};
	
	static Rule distrBaseIfRoot = new Rule("distribute base if root") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Power pow = (Power)e;
			
			if(pow.getExpo() instanceof Div && (pow.getBase() instanceof Prod || pow.getBase() instanceof Sum)) {
				pow.setBase(distr(pow.getBase()).simplify(casInfo));
			}
			
			return pow;
		}
		
	};
	
	static Rule sqrtOfSqrtSum = new Rule("sqrt(k*sqrt(b)+a)->sqrt((a+sqrt(a^2-b*k^2))/2)+sqrt((a-sqrt(a^2-b*k^2))/2)*abs(k)/k","isType(result(sqrt(a^2-b*k^2)),num)","square root of a square root sum");
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				isI,
				powersWithEpsilonOrInf,
				
				zeroToExpo,
				oneToExpo,
				
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
				
				powerOfOne,
				expoOfZero,
				
				negativeExpoToInv,
				
				eToLn,
				baseToLn,
				eToFracLn,
				logInExpoProdToBase,
				
				expOfLambertW,
				expOfLambertWProd,
				
				factorBase,
				fracInBase,
				productInBase,
				rootInRoot,
				
				rootNumSimp,
				
				perfectPowerInBase,
				
				rootHasCancelingPower,
				
				distrBaseIfRoot,
				
				expoHasIntegerInSum,
				expoSumHasLog,//keep after expoHasIntegerInSum
				
				exponentiateIntegers
				
				
				
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
		
		if(Rule.fastSimilarExpr(sqrtObj,this)) {//fancy and having set to true makes it faster
			out+="sqrt(";
			out+=getBase().toString();
			out+=')';
		}else if(Rule.fastSimilarExpr(cbrtObj,this)) {
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
		return powerOfOne.applyRuleToExpr(e, CasInfo.normal);
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.pow(getBase().convertToFloat(varDefs), getExpo().convertToFloat(varDefs));
	}

	@Override
	public String typeName() {
		return "power";
	}
}
