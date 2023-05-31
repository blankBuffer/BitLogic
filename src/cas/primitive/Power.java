package cas.primitive;
import java.math.BigInteger;

import cas.*;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.bool.BoolState;
import cas.calculus.Limit;

public class Power{
	
	public static Rule powerOfOne = new Rule("a^1->a","exponent is one");
	
	public static Func.FuncLoader loader = new Func.FuncLoader(){
		@Override
		public void load(Func owner) {
			
			Rule baseHasPower = new Rule("(a^b)^c->a^(b*c)","relaxedPower()|isType(b,num)&isType(c,num)|(comparison(a>0)&isType(a,num))","base has power");
			Rule baseHasPowerAbs = new Rule("(a^b)^c->abs(a)^(b*c)","isType(result(b/2),num)&~allowComplexNumbers()","base has power");
			Rule rootInRoot = new Rule("(a^b)^c->a^(b*c)","isType(b,div)&isType(c,div)","root in root");
			Rule expoOfZero = new Rule("a^0->1","~comparison(a=0)","exponent is zero");
			Rule isI = new Rule("sqrt(-1)->i","allowComplexNumbers()","is equal to i");
			Rule eToLn = new Rule("e^ln(a)->a","e to ln");
			Rule eToFracLnNumer = new Rule("e^(ln(a)/b)->a^(1/b)","e to fraction with ln where ln in numerator");
			Rule isEPower = new Rule("a^(b/ln(a))->e^b","can be turned into power base e");
			Rule zeroToExpo = new Rule("0^x->0","~comparison(x=0)","base is zero");
			Rule baseToLn = new Rule("a^ln(b)->e^(ln(a)*ln(b))","base not e and expo has log");
			Rule expOfLambertW = new Rule("e^(lambertW(x))->x/lambertW(x)","e to lambert w");
			Rule expOfLambertWProd = new Rule("e^(lambertW(x)*n)->x^n/lambertW(x)^n","e to lambert w product");
			Rule sqrtOneMinusSin = new Rule("sqrt(1-sin(x))->sqrt(2)*sin(pi/4-x/2)","sqrt of 1 minus sin");
			Rule sqrtOneMinusCos = new Rule("sqrt(1-cos(x))->sqrt(2)*sin(x/2)","sqrt of 1 minus cos");
			Rule sqrtOnePlusSin = new Rule("sqrt(1+sin(x))->sqrt(2)*cos(pi/4-x/2)","sqrt of 1 plus sin");
			Rule sqrtOnePlusCos = new Rule("sqrt(1+cos(x))->sqrt(2)*cos(x/2)","sqrt of 1 plus cos");
			Rule baseOfPowerIsAbsExpoEven = new Rule("abs(a)^b->a^b","~isType(result(b/2),div)&~allowComplexNumbers()","base of power is absolute value and exponent is divisible by 2");
			
			Rule fracInBase = new Rule("base is a fraction") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func power = (Func)e;
					
					boolean root = power.getExpo().isType("div");
					
					if(power.getBase().isType("div")) {
						Func innerDiv = (Func)power.getBase();
						boolean defaultCase = casInfo.allowComplexNumbers();
						boolean flipSignsAndApply = false;
						if(root && innerDiv.negative() && !defaultCase) {
							boolean numerHasVars = innerDiv.getNumer().containsVars();
							boolean denomHasVars = innerDiv.getDenom().containsVars();
							
							if(numerHasVars && !denomHasVars) {
								if(innerDiv.getDenom().negative()) {
									flipSignsAndApply = true;
								}else defaultCase = true;
							}else if(denomHasVars && !numerHasVars) {
								if(innerDiv.getNumer().negative()) {
									flipSignsAndApply = true;
								}else defaultCase = true;
							}else if(numerHasVars && denomHasVars) {
								defaultCase = true;
							}
						}else defaultCase = true;
						
						if(defaultCase) {
							return div(power(innerDiv.getNumer(),power.getExpo()),power(innerDiv.getDenom(),power.getExpo())).simplify(casInfo);
						}else if(flipSignsAndApply) {
							return div(power(neg(innerDiv.getNumer()),power.getExpo()),power(neg(innerDiv.getDenom()),power.getExpo())).simplify(casInfo);
						}
					}
					
					return power;
				}
			};
			
			Rule oneToExpo = new Rule("base is one"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func power = (Func)e;
					
					if(Limit.stripDirection(power.getBase()).equals(Num.ONE) && !power.getExpo().equals(Var.INF)){
						short direction = Limit.getDirection(power.getBase());
						
						direction = power.getExpo().negative() ? Limit.flipDirection(direction) : direction;
						
						Expr out = Limit.applyDirection( num(1),direction);
						return out;
					}
					
					return power;
				}
				
			};
			
			Rule exponentiateIntegers = new Rule("exponentiate integers"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func power = (Func)e;
					if(power.getBase() instanceof Num && power.getExpo() instanceof Num) {
						Num base = (Num)power.getBase();
						Num expo = (Num)power.getExpo();
						
						if(power.contains(Num.ZERO)) return power;
						
						if(!expo.isComplex() && expo.getRealValue().compareTo(BigInteger.valueOf(10000))==-1) {
							if(expo.signum()!=-1 ) {
								Expr result = base.pow(expo.getRealValue());
								return result;
							}
						}
						
					}
					return power;
				}
			};
			Rule negativeExpoToInv = new Rule("negative expoonent to inverse"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					if(pow.getExpo().negative()) {
						pow.setExpo(neg(pow.getExpo()));
						
						Expr result = inv( pow ).simplify(casInfo);
						return result;
					}
					return pow;
					
				}
			};
			Rule factorExponent = new Rule("factoring the exponent"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func power = (Func)e;
					
					Expr factoredExpo = factor(power.getExpo()).simplify(casInfo);
					
					if(!factoredExpo.equals(power.getExpo())){
						Expr result = power(power.getBase().copy(),factoredExpo);
						return result;
					}
					
					return power;
				}
			};
			Rule factorBase = new Rule("factoring the base"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func power = (Func)e;
					
					Expr factoredBase = factor(power.getBase()).simplify(casInfo);
					
					if(!factoredBase.equals(power.getBase())){
						Expr result = power(factoredBase,power.getExpo().copy());
						return result;
					}
					
					return power;
				}
			};
			Rule logInExpoProdToBase = new Rule("e to exponent product with single ln"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					
					if(pow.getBase().equals(Var.E)) {
						Func expoProd = null;
						Func expoDiv = null;
						if(pow.getExpo().isType("div") && ((Func)pow.getExpo()).getNumer().isType("prod") ) {
							expoDiv = (Func)pow.getExpo();
							expoProd = (Func)expoDiv.getNumer();
						}else if(pow.getExpo().isType("prod")) {
							expoProd = (Func)pow.getExpo();
						}
						
						if(expoProd != null) {
							int logCount = 0;
							int index = -1;
							
							for(int i = 0;i<expoProd.size();i++) {
								if(expoProd.get(i).isType("ln")) {
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
			Rule expoSumHasLog = new Rule("base is e and expo has sum with logs"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					if(pow.getExpo().isType("sum") && pow.getBase().equals(Var.E)) {
						Func expoSum = (Func)pow.getExpo();
						Func outerProd = prod();
						for(int i = 0;i<expoSum.size();i++) {
							if(expoSum.get(i).isType("ln")) {
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
			Rule expoHasIntegerInSum = new Rule("exponent has integer in sum and base is integer"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					if(isPositiveRealNum(pow.getBase())&& !(pow.getExpo() instanceof Num)) {
						
						pow.setExpo(distr(pow.getExpo()).simplify(casInfo));//distribute exponent
						
						//if expo is a frac turn it into a mixed fraction sum
						
						Func fracSum = null;
						if(pow.getExpo().isType("div")) fracSum = Div.mixedFraction((Func)pow.getExpo());
						if(fracSum!=null) {
							pow.setExpo(fracSum);
						}
						
						if(pow.getExpo().isType("sum")) {
							Expr expo = pow.getExpo();
							for(int i = 0;i<expo.size();i++) {
								
								if(expo.get(i) instanceof Num) {//the actual expansion
									
									Num num = (Num)expo.get(i);
									expo.remove(i);
									pow.setExpo(pow.getExpo().simplify(casInfo));
									Expr repl = prod(pow,power(pow.getBase().copy(),num)).simplify(casInfo);
									return repl;
									
								}
								
								fracSum = null;
								if(expo.get(i).isType("div")) fracSum = Div.mixedFraction((Func)expo.get(i));
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
			
			Rule perfectPowerInBase = new Rule("the base is a perfect power"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func power = (Func)e;
					
					if(power.getBase() instanceof Num && !(power.getExpo() instanceof Num)) {
						Func pp = perfectPower((Num)power.getBase());
						
						if( pp.getExpo().equals(Num.ONE) ) return power;
						
						power.setBase(pp.getBase());
						
						if(power.getExpo().isType("prod")) power.getExpo().add(pp.getExpo());
						else power.setExpo(prod(power.getExpo(),pp.getExpo()));
						
						power.setExpo(power.getExpo().simplify(casInfo));
						
					}
					
					return power;
				}
				
			};
			
			Rule productInBase = new Rule("the base is a product"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					
					if(pow.getBase().isType("prod") && !(pow.getExpo().isType("div"))) {
						Func castedProd = (Func)pow.getBase().copy();
						Func frac = null;
						if(pow.getExpo().isType("div")) frac = (Func)pow.getExpo();
						boolean createsComplexNumber = false;
						if(!casInfo.allowComplexNumbers() && frac != null && Div.isNumericalAndReal(frac)) {
							if(((Num)frac.getDenom()).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO)) createsComplexNumber = true;//root in the form (x)^(a/(2*n))
						}
						
						Func outProd = prod();
						for(int i = 0;i<castedProd.size();i++) {
							Expr expr = castedProd.get(i);
							if(createsComplexNumber && expr.negative()){
								pow.setBase(distr(pow.getBase()).simplify(casInfo));
								return pow;
							}
							outProd.add(power(expr,pow.getExpo()).simplify(casInfo) );
							castedProd.remove(i);
							i--;
						}
						if(castedProd.size() > 0) {
							pow.setBase(pow.getBase().simplify(casInfo));
							outProd.add(pow);
						}
						return outProd.simplify(casInfo);
					}
					
					return pow;
				}
				
			};
			
			Rule powersWithEpsilonOrInf = new Rule("power with epsilon or infinity"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					
					if(pow.getExpo().equals(Var.INF)){//x^inf
						if(!Limit.stripDirection(pow.getBase()).equals(Num.ONE)) {
							if (Limit.isEpsilon(pow.getBase())){
								return epsilon();
							}else if(comparison(equGreater(pow.getBase(),num(1))).simplify(casInfo).equals(BoolState.TRUE)) {
								return inf();
							}else{
								return epsilon();
							}
						}
						
					}else if(pow.getExpo().contains(Var.EPSILON) && !Limit.isEpsilon(pow.getBase()) && !Limit.isInf(pow.getBase()) && !pow.getBase().equals(Num.ONE)) {//x^(y+epsilon)
						
						short direction = Limit.getDirection(pow.getExpo());
						pow.setExpo(Limit.stripDirection(pow.getExpo()));
						
						if(direction != Limit.NONE) {
							if(comparison(equLess(pow.getBase(),num(1))).simplify(casInfo).equals(BoolState.TRUE)) {
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
			
			Rule eulersIdentity = new Rule("eulers identity") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					
					if(pow.getExpo().isType("prod") || pow.getExpo().isType("div")) {
						Func prod = null;
						Func div = null;
						if(pow.getExpo().isType("prod")) {
							prod = (Func)pow.getExpo();
							div = Div.cast(prod);
						}else {
							if( ((Func)pow.getExpo()).getNumer().isType("prod") ) {
								div = (Func) pow.getExpo();
								prod = (Func) div.getNumer();
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
			
			Rule rootNumSimp = new Rule("root of a number") {//apply roots to numbers
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					if(pow.getExpo().isType("div")) {
						Func expoDiv = (Func)pow.getExpo();
						
						if(isPositiveRealNum(expoDiv.getDenom()) && pow.getBase() instanceof Num && (isRealNum(pow.getBase()) || casInfo.allowComplexNumbers()) ) {
							Num denomNum = (Num)expoDiv.getDenom();
							
							Num baseNum = (Num)pow.getBase();
						
							//if the base is negative and the denominator is even
							
							if(isPositiveRealNum(baseNum)) {
								
								//this portion works similar to the root expand rule
								BigInteger root = denomNum.getRealValue();
								BigInteger num = baseNum.getRealValue();
								
								
								BigInteger ans = bigRoot( num , root );
								if(ans.pow(root.intValue()).equals(num)) {
									return power(num(ans),expoDiv.getNumer()).simplify(casInfo);
								}
								BigInteger factor = divisibleRoot(num, root);
								if(!factor.equals(BigInteger.ONE)) {
									BigInteger outerNum = bigRoot( factor , root );
									return prod( power(num(outerNum),expoDiv.getNumer()).simplify(casInfo), power(num(num.divide(factor)),expoDiv) );
									
								}
							}else if(casInfo.allowComplexNumbers() && denomNum.equals(Num.TWO) && expoDiv.getNumer().equals(Num.ONE)){//square root of a complex or negative number
								BigInteger sumOfSquares = baseNum.getRealValue().pow(2).add(baseNum.getImagValue().pow(2));
								BigInteger root = sumOfSquares.sqrt();
								
								//sqrt(a+b*i) -> (sqrt(sqrt(a^2+b^2)+a)+sign(b)*sqrt(sqrt(a^2+b^2)-a))/sqrt(2)
								if(root.pow(2).equals(sumOfSquares)) {
									
									Expr out = div(power(sum( sqrt( num(root.add(baseNum.getRealValue())) ) , prod(num(0,baseNum.getImagValue().signum() == -1? -1 : 1),sqrt( num(root.subtract(baseNum.getRealValue())) )) ),expoDiv.getNumer()), power(num(2),expoDiv) );
									return out.simplify(casInfo);
									
								}
								
								
							}
						}
					}
					return pow;
				}
			};
			
			Rule rootHasCancelingPower = new Rule("root has power or number inside that cancels and goes outside root") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					
					if(pow.getExpo().isType("div") && pow.getBase().isType("prod")) {
						Func baseProd = (Func)pow.getBase();
						Func expoDiv = (Func)pow.getExpo();
						
						Func outProd = prod();
						for(int i = 0;i<baseProd.size();i++) {
							if(baseProd.get(i).isType("power")) {
								Func subPow = (Func)baseProd.get(i);
								
								if(subPow.getExpo().isType("div")) {
									Func subPowExpoDiv = (Func)subPow.getExpo();
									if(subPowExpoDiv.getNumer().equals(expoDiv.getDenom())) {
										
										outProd.add(power(subPow.getBase(),div(expoDiv.getNumer(),subPowExpoDiv.getDenom())));
										
										baseProd.remove(i);
										i--;
									}
								}else {
									if(expoDiv.getDenom().equals(subPow.getExpo())) {
										
										outProd.add(power(subPow,expoDiv));
										
										baseProd.remove(i);
										i--;
									}
								}
								
							}else if(isPositiveRealNum(expoDiv.getDenom()) && isRealNum(baseProd.get(i)) ) {
								
								Expr computed = rootNumSimp.applyRuleToExpr(power(baseProd.get(i),pow.getExpo()), casInfo);
								if(computed instanceof Num) {
									baseProd.remove(i);
									i--;
									outProd.add(computed);
								}else if(computed.isType("prod")) {
									outProd.add(computed.get(0));
									baseProd.set(i, computed.get(1).get());
									i--;
								}
								
							}
						}
						if(outProd.size()>0) {
							outProd.add(pow);
							return outProd.simplify(casInfo);
						}
					}
					
					return pow;
				}
			};
			
			Rule rootInProdInRoot = new Rule("root in product in root") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					
					if(pow.getBase().isType("prod") && pow.getExpo().isType("div")) {
						Func innerProd = (Func)pow.getBase();
						
						Func outProd = prod();
						
						for(int i = 0;i<innerProd.size();i++) {
							if(innerProd.get(i).isType("power") && ((Func)innerProd.get(i)).getExpo().isType("div") ) {
								Func innerPow = (Func)innerProd.get(i);
								
								outProd.add(power(innerPow.getBase(),prod(pow.getExpo(),innerPow.getExpo())));
								innerProd.remove(i);
								i--;
							}
						}
						
						if(outProd.size()>0) {
							outProd.add(pow);
							return outProd.simplify(casInfo);
						}
						
					}
					
					return pow;
				}
			};
			
			Rule distrBaseIfRoot = new Rule("distribute base if root") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func pow = (Func)e;
					
					if(pow.getExpo().isType("div") && (pow.getBase().isType("prod") || pow.getBase().isType("sum"))) {
						pow.setBase(distr(pow.getBase()).simplify(casInfo));
					}
					
					return pow;
				}
				
			};
			
			Rule sqrtOfSqrtSum = new Rule("sqrt(k*sqrt(b)+a)->sqrt((a+sqrt(a^2-b*k^2))/2)+sqrt((a-sqrt(a^2-b*k^2))/2)*abs(k)/k","isType(result(sqrt(a^2-b*k^2)),num)","square root of a square root sum");
			
			owner.behavior.rule = new Rule(new Rule[]{
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
				baseHasPowerAbs,
				rootInRoot,
				
				eulersIdentity,
				
				baseOfPowerIsAbsExpoEven,
				
				negativeExpoToInv,
				
				eToLn,
				baseToLn,
				eToFracLnNumer,
				isEPower,
				logInExpoProdToBase,
				
				/*
				powerOfOne,
				expoOfZero,
				*/
				
				expOfLambertW,
				expOfLambertWProd,
				
				factorBase,
				
				/*
				 * baseHasPower again in case factor base has 
				 * sqrt(x^2+2*x+1) -> sqrt((x+1)^2) -> either x+1 or abs(x+1)
				 */
				///
				baseHasPower,
				baseHasPowerAbs,
				powerOfOne,
				expoOfZero,
				//
				
				
				fracInBase,
				productInBase,
				rootInProdInRoot,
				
				rootNumSimp,
				
				perfectPowerInBase,
				
				rootHasCancelingPower,
				
				distrBaseIfRoot,
				
				expoHasIntegerInSum,
				expoSumHasLog,//keep after expoHasIntegerInSum
				
				exponentiateIntegers
			},"main sequence");
			
			owner.behavior.rule.init();
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.pow(owner.getBase().convertToFloat(varDefs),owner.getExpo().convertToFloat(varDefs));
				}
			};
			
			owner.behavior.toStringMethod = new Func.ToString() {
				@Override
				public String generateString(Func owner) {
					String out = "";
					
					if(Cas.isSqrt(owner)) {//fancy and having set to true makes it faster
						out+="sqrt(";
						out+=owner.getBase().toString();
						out+=')';
					}else if(Cas.isCbrt(owner)) {
						out+="cbrt(";
						out+=owner.getBase().toString();
						out+=')';
					}else
					
					{
						boolean useParenOnBase = false;//parentheses if
						//base is a negative integer
						//base is a sum or product or power
						if(owner.getBase().isType("sum") || owner.getBase().isType("prod") || owner.getBase().isType("power") || owner.getBase().isType("div")) useParenOnBase = true;
						if(owner.getBase() instanceof Num) {
							Num baseCasted = (Num)owner.getBase();
							if(baseCasted.getRealValue().signum() == -1) useParenOnBase = true;
						}
						if(useParenOnBase) out+="(";
						out+=owner.getBase().toString();
						if(useParenOnBase) out+=")";
						out+="^";
						
						boolean useParenOnExpo = false;
						if(owner.getExpo().isType("sum") || owner.getExpo().isType("prod") || owner.getExpo().isType("power") || owner.getExpo().isType("div")) useParenOnExpo = true;
						if(useParenOnExpo) out+="(";
						out+=owner.getExpo().toString();
						if(useParenOnExpo) out+=")";
					}
					return out;
				}
			};
		}
		
	};
	
	public static Func cast(Expr e) {
		if(e.isType("power")) {
			return (Func)e;
		}
		return Cas.power(e,Cas.num(1));
	}
	
	public static Expr unCast(Expr e) {
		return powerOfOne.applyRuleToExpr(e, CasInfo.normal);
	}
}
