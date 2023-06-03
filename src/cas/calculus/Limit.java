package cas.calculus;

import java.math.BigInteger;

import cas.Cas;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.base.StandardRules;
import cas.bool.BoolState;
import cas.primitive.*;

public class Limit{
	public static final short RIGHT = 1,LEFT = -1,NONE = 0;
	
	public static Var getVar(Func limit) {
		return (Var) limit.get(1).get(0);
	}
	public static Expr getValue(Func limit) {
		return limit.get(1).get(1);
	}
	
	public static Func.FuncLoader limitLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			
			owner.behavior.helpMessage = "Take the limit of an expression.\n"
					+ "Example, limit(sin(x)/x,x->0) returns 1\n"
					+ "If you want the limit from a particular direction use the epsilon variable.\n"
					+ "Example limit(abs(x)/x,x->-epsilon) returns -1";
			
			/*
			 * puts the value directly, last resort
			 */
			Rule directSubst = new Rule("direct substitution"){

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					return lim.get().replace(equ(lim.getVar(),Limit.getValue(lim))).simplify(casInfo);
				}
			};
			
			/*
			 * if there is a factor-able exponent in the limit that is constant
			 * limit(a^b/c^b,) can be turned into limit(a/c,)^b if b is constant
			 * 
			 * we still in general want to factor the exponent so lhoptials rule works better
			 */
			Rule commonExpoDiv = new Rule("common exponent for division limit"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					
					if(lim.get().isType("div")) {
						Func innerDiv = (Func)lim.get();
						if(innerDiv.getNumer().isType("power") && innerDiv.getDenom().isType("power")) {
							Func numerPower = (Func)innerDiv.getNumer();
							Func denomPower = (Func)innerDiv.getDenom();
							
							Expr gcd = gcd(numerPower.getExpo(),denomPower.getExpo()).simplify(casInfo);
							
							if(gcd.equals(Num.ONE)) return lim;
							
							innerDiv.setSimpleSingleNode(false);
							
							if(gcd.contains(lim.getVar())) {
								numerPower.setExpo(div(numerPower.getExpo(),gcd).simplify(casInfo));
								denomPower.setExpo(div(denomPower.getExpo(),gcd).simplify(casInfo));
								
								lim.set(0,power(div(numerPower,denomPower),gcd));
								return lim;
							}
							numerPower.setExpo( div(numerPower.getExpo(),gcd).simplify(casInfo) );
							denomPower.setExpo( div(denomPower.getExpo(),gcd).simplify(casInfo) );
							
							Expr out = power(lim,gcd);
							return out.simplify(casInfo);
							
						}
					}
					return lim;
				}
			};
			
			/*
			 * A basic heuristic to figuring out which function grows faster
			 */
			Rule biggerFuncCalc = new Rule("function growth comparison"){
				boolean posLinFunc(Expr e,Var v,CasInfo casInfo){
					return degree(e,v).equals(BigInteger.ONE) &&
							comparison(equGreater(getLeadingCoef(e,v,casInfo),num(0))).simplify(casInfo).equals(BoolState.TRUE);
				}
				
				int UNKNOWN = -1,CONST = 0,POLY = 1,EXP = 2,SUPER = 3;//different classes of size
				
				int getSizeRank(Expr e,Var v,CasInfo casInfo){
					e = stripNonVarPartsFromProd(e,v);
					if(!e.contains(v)) return CONST;
					if(isPolynomialUnstrict(e, v)) return POLY;
					if(e.isType("power") && !e.get(0).contains(v) && posLinFunc(e.get(1), v,casInfo) ) return EXP;
					if(e.isType("power") && posLinFunc(e.get(0),v,casInfo) && posLinFunc(e.get(1),v,casInfo)) return SUPER;
					return UNKNOWN;//cannot compare too complicated
				}
				
				int compare(Expr a,Expr b,Var v,CasInfo casInfo){//a>b 1 a<b -1 unkown 0
					
					int aRank = getSizeRank(a,v,casInfo);
					if(aRank == UNKNOWN) return 0;
					
					int bRank = getSizeRank(b,v,casInfo);
					if(bRank == UNKNOWN) return 0;
					
					return Integer.compare(aRank, bRank);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					Var v = lim.getVar();
					if(Limit.getValue(lim).equals(Var.INF)){
						if(lim.get().isType("div")){
							Func castedDiv = (Func)lim.get();
							int comparison = compare(castedDiv.getNumer(),castedDiv.getDenom(),v,casInfo);
							if(comparison == 1) return inf();
							else if(comparison == -1) return epsilon();
						}else if(lim.get().isType("sum")){//sum where one term grows much faster than the rest
							Func castedSum = (Func)lim.get();
							Expr biggest = castedSum.get(0);
							
							for(int i = 1;i<castedSum.size();i++){
								Expr current = castedSum.get(i);
								int comparison = compare(biggest,current,v,casInfo);
								if(comparison == 0) return e;
								if(comparison == -1){
									biggest = current;
								}
							}
							
							return limit(biggest,Limit.getApproaches(lim)).simplify(casInfo);
						}
					}
					return e;
				}
			};
			
			Rule lhopitalsRulePow = new Rule("lhopitals rule with powers") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					if(lim.get().isType("power")) {
						Func innerPow = (Func)lim.get();
						
						Expr limOfBase = limit(innerPow.getBase(),Limit.getApproaches(lim)).simplify(casInfo);
						Expr limOfExpo = limit(innerPow.getExpo(),Limit.getApproaches(lim)).simplify(casInfo);
						
						if( (zeroOrEpsilon(limOfBase) || isInf(limOfBase)) && zeroOrEpsilon(limOfExpo) || stripDirection(limOfBase).equals(Num.ONE)) {
							
							Expr diffLogBase = diff(ln(innerPow.getBase()),lim.getVar()).simplify(casInfo);
							Expr diffInvExpo = diff(inv(innerPow.getExpo()),lim.getVar()).simplify(casInfo);
							
							return exp(limit(div(diffLogBase,diffInvExpo),Limit.getApproaches(lim))).simplify(casInfo);
							
						}
						innerPow.setBase(limOfBase);
						innerPow.setExpo(limOfExpo);
					}
					return lim;
				}
				
			};
			
			/*
			 * the mechanism works by this principal limit(a^(b/c)/d,) can be transformed into limit(a^b/d^c,)^c if c is a constant
			 * lhopitals rule will work better if its not a root as it avoid infinite recursion
			 */
			Rule limitOfDivWithRoot = new Rule("limit of division where either numerator of denominator is a root function") {
				Expr rootExpr;
				Expr rootCondition;
				
				@Override
				public void init(){
					rootExpr = createExpr("a^(b/c)");
					rootCondition = createExpr("~contains(c,x)");
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					
					if(lim.get().isType("div")) {
						Func innerDiv = (Func)lim.get();
						
						boolean numerIsRoot = Rule.similarWithCondition(rootExpr,innerDiv.getNumer(),rootCondition.replace(equ(lim.getVar(),var("x"))));
						boolean denomIsRoot = Rule.similarWithCondition(rootExpr,innerDiv.getDenom(),rootCondition.replace(equ(lim.getVar(),var("x"))));
						
						int sign = 1;
						
						Expr rootExpr = null;
						if(numerIsRoot && !denomIsRoot){//numer case
							rootExpr = ((Func)((Func)innerDiv.getNumer()).getExpo()).getDenom();
							
							Expr denomAtInf = limit(innerDiv.getDenom(),Limit.getApproaches(lim)).simplify(casInfo);
							if(isRealNum(rootExpr) && ((Num)rootExpr).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO) && comparison(equLess(denomAtInf,num(0))).simplify(casInfo).equals(BoolState.TRUE) ) {
								sign = -1;
							}
							
						}else if(!numerIsRoot && denomIsRoot) {//denom case
							rootExpr = ((Func)((Func)innerDiv.getDenom()).getExpo()).getDenom();
							
							Expr numerAtInf = limit(innerDiv.getNumer(),Limit.getApproaches(lim)).simplify(casInfo);
							if(isRealNum(rootExpr) && ((Num)rootExpr).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO) && comparison(equLess(numerAtInf,num(0))).simplify(casInfo).equals(BoolState.TRUE) ) {
								sign = -1;
							}
							
						}else if(numerIsRoot && denomIsRoot) {
							Expr numerRoot = ((Func)((Func)innerDiv.getNumer()).getExpo()).getDenom();
							Expr denomRoot = ((Func)((Func)innerDiv.getDenom()).getExpo()).getDenom();
							
							if(isRealNum(numerRoot) && isRealNum(denomRoot)) {
								rootExpr = num(gcm( ((Num)numerRoot).getRealValue() , ((Num)denomRoot).getRealValue() ));
							}else {
								rootExpr = prod(numerRoot,denomRoot);
							}
						}
						
						if(rootExpr != null) {
							lim.set(0, power(lim.get(),rootExpr) );
							return prod(power(limit(lim.get(),Limit.getApproaches(lim)),inv(rootExpr)),num(sign)).simplify(casInfo);
						}
					}
					
					return lim;
				}
			};
			
			//if the numerator has a larger degree 
			Rule differentDegreePolysInDiv = new Rule("different degrees in division") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					
					Var v = lim.getVar();
					
					if(lim.get().isType("div") && isInf(Limit.getValue(lim))) {
						Func innerDiv = (Func)lim.get();
						
						BigInteger numerDegree = degree(innerDiv.getNumer(),v);
						if(numerDegree.signum() != 1) return lim;
						BigInteger denomDegree = degree(innerDiv.getDenom(),v);
						if(denomDegree.signum() != 1) return lim;
						
						int comparison = numerDegree.compareTo(denomDegree);
						
						Expr out = null;
						if(comparison == 1) {
							out = limit(innerDiv.getNumer(),Limit.getApproaches(lim));
						}else if(comparison == -1) {
							out = inv(limit(innerDiv.getDenom(),Limit.getApproaches(lim)));
						}else  if(comparison == 0){
							out = div(getLeadingCoef(innerDiv.getNumer(),v,casInfo),getLeadingCoef(innerDiv.getDenom(),v,casInfo));
						}
						return out.simplify(casInfo);
					}
					
					return lim;
				}
			};
			
			//remove all parts that are smaller degree in the sum
			Rule polyCase = new Rule("limit of a polynomial") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					
					if(isPolynomialUnstrict(lim.get(), lim.getVar()) && isInf(Limit.getValue(lim))) {
						
						Var v = lim.getVar();
						Num degree = num(degree(lim.get(),v));
						Expr coeff = getLeadingCoef(lim.get(),v,casInfo);
						
						return prod(coeff,power(v,degree)).replace(equ(v,Limit.getValue(lim))).simplify(casInfo);
						
					}
					
					return lim;
				}
			};
			
			/*
			 * the transformation is that any element in the sum in the form of
			 * (a*x^n+b*x^(n-1)+...)^(1/n)
			 * can be re written as
			 * a^(1/n)*x+b*a^(1/n)/(a*n)
			 * when x->inf
			 * 
			 * if x->-inf
			 * 
			 * then when n is odd use the transformation above but when n is even
			 * -a^(1/n)*x-b*a^(1/n)/(a*n)
			 * 
			 * the first equation converges to that linear equation
			 * 
			 * the ... terms are the lower order polynomial parts. They don't effect the result
			 * 
			 * This is useful for the case where limit(sqrt(x^2+3*x)-x,x->inf) = 3/2
			 * 
			 */
			Rule rootRewrite = new Rule("special way to write roots when they approach infinity") {
				Expr rootForm;
				Expr condition;
				
				@Override
				public void init() {
					rootForm = createExpr("a^(1/n)");
					condition = createExpr("isType(a,sum)&isType(n,num)");
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					
					if(isInf(Limit.getValue(lim)) && lim.get().isType("sum")) {
						Func innerSum = (Func)lim.get();
						
						for(int i = 0;i<innerSum.size();i++) {
							Func partsSequence = seperateByVar(innerSum.get(i),lim.getVar());
							if(Rule.similarWithCondition(rootForm, partsSequence.get(1), condition)) {
								Func innerPow = (Func)partsSequence.get(1);
								Func innerPolySequence = polyExtract(innerPow.getBase(),lim.getVar(),casInfo);
								if(innerPolySequence == null) return lim;
								Num n = (Num) ((Func)innerPow.getExpo()).getDenom();
								if(!isPositiveRealNum(n)) return lim;
								if(innerPolySequence.size()-1 == n.getRealValue().intValue()) {
									Expr a = innerPolySequence.get(innerPolySequence.size()-1);
									Expr b = innerPolySequence.get(innerPolySequence.size()-2);
									
									Expr repl = null;
									
									if(Limit.getValue(lim).equals(Var.INF) || n.getRealValue().mod(BigInteger.TWO).equals(BigInteger.ONE)) {
										repl = sum(prod(power(a,inv(n)),lim.getVar(),partsSequence.get(0)), div(prod(b,power(a,inv(n)),partsSequence.get(0)),prod(a,n)) );
									}else {
										repl = sum(prod(num(-1),power(a,inv(n)),lim.getVar(),partsSequence.get(0)), div(prod(num(-1),b,power(a,inv(b)),partsSequence.get(0)),prod(a,n)) );
									}
									innerSum.set(i, repl);
								}
							}
						}
						lim.set(0,innerSum.simplify(casInfo));
					}
					return lim;
				}
				
			};
			
			/*
			 * convert the gamma function to the sterling approximation so that if there is a ration of the two it can be calculated
			 */
			Rule sterlingTransformation = new Rule("sterling approximation for gamma") {
				Expr toSterling;
				
				@Override
				public void init() {
					toSterling = createExpr("((x-1)^((2*x-1)/2)*sqrt(2*pi))/e^(x-1)");
				}
				
				public Expr replace(Expr e) {
					boolean changed = false;
					if(e.isType("gamma")) {
						return toSterling.replace(equ(var("x"),e.get()));
					}else if(e.isType("prod")) {
						for(int i = 0;i<e.size();i++) {
							if(e.get(i).isType("gamma")) {
								e.set(i, toSterling.replace(equ(var("x"),e.get(i).get())));
								changed = true;
							}else if(e.get(i).isType("power")) {
								Func innerPower = (Func)e.get(i);
								if(innerPower.getBase().isType("gamma")) {
									changed = true;
									innerPower.setBase(toSterling.replace(equ(var("x"),innerPower.getBase().get())));
								}
							}
						}
						if(changed) return e;
					}else if(e.isType("power")) {
						Func innerPower = (Func)e;
						if(innerPower.getBase().isType("gamma")) {
							changed = true;
							innerPower.setBase(toSterling.replace(equ(var("x"),innerPower.getBase().get())));
						}
						if(changed) return e;
					}
					return null;
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					
					if(!Limit.getValue(lim).equals(Var.INF)) return lim;
					
					if(lim.get().isType("div")) {
						boolean changed = false;
						Func innerDiv = (Func)lim.get();
						
						Expr newNumer = replace(innerDiv.getNumer());
						if(newNumer != null) {
							innerDiv.setNumer(newNumer);
							changed = true;
						}
						Expr newDenom = replace(innerDiv.getDenom());
						if(newDenom != null) {
							innerDiv.setDenom(newDenom);
							changed = true;
						}
						
						if(changed)lim.set(0,lim.get().simplify(casInfo));//if it was applied simplify the expression
						
					}
					return lim;
				}
			};
			
			/*
			 * if the limit of the numerator and denominator is epsilon/epsilon or inf/inf then take the derivative of
			 * the numerator and denominator and try again
			 */
			Rule lhopitalsRuleDiv = new Rule("lhopitals rule with division") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func lim = (Func)e;
					
					if(lim.get().isType("div")) {
						Func innerDiv = (Func)lim.get();
						
						Expr limOfNumer = limit(innerDiv.getNumer(),Limit.getApproaches(lim)).simplify(casInfo);
						Expr limOfDenom = limit(innerDiv.getDenom(),Limit.getApproaches(lim)).simplify(casInfo);
						
						if(zeroOrEpsilon(limOfNumer) && zeroOrEpsilon(limOfDenom) || isInf(limOfNumer) && isInf(limOfDenom)) {
							Expr diffNumer = diff(innerDiv.getNumer(),lim.getVar()).simplify(casInfo);
							Expr diffDenom = diff(innerDiv.getDenom(),lim.getVar()).simplify(casInfo);
							
							Expr out = limit(div(diffNumer,diffDenom),Limit.getApproaches(lim));
							return out.simplify(casInfo);
							
						}
						innerDiv.setNumer(limOfNumer);
						innerDiv.setDenom(limOfDenom);
						
						
					}
					
					return lim;
				}
			};
			
			Rule absCases = new Rule(new Rule[] {
					new Rule("limit(abs(x)/x,x->epsilon)->1","simple abs case"),
					new Rule("limit(abs(x)/x,x->-epsilon)->-1","simple abs case")
			}, "abs cases");
			
			owner.behavior.rule = new Rule(new Rule[] {
					StandardRules.pullOutConstants,
					commonExpoDiv,
					biggerFuncCalc,
					lhopitalsRulePow,
					limitOfDivWithRoot,
					differentDegreePolysInDiv,
					polyCase,
					rootRewrite,
					sterlingTransformation,
					absCases,
					lhopitalsRuleDiv,
					directSubst
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return owner.get().convertToFloat(varDefs);
				}
			};
		}
	};
	
	public static Func getApproaches(Func lim) {//returns becomes
		return (Func)lim.get(1);
	}
	
	
	public static short getDirection(Func lim){
		return getDirection(Limit.getValue(lim));
	}
	
	
	public static short flipDirection(short direction){
		return (short)(-direction);
	}
	
	
	//returns the direction of an expression right or left
	public static short getDirection(Expr e){
		if(e.isType("sum")){
			Func innerSum = (Func)e;
			for(int i = 0;i<innerSum.size();i++){
				if(innerSum.get(i).equals(Var.EPSILON)){
					return RIGHT;
				}else if(innerSum.get(i).equals(Var.NEG_EPSILON)){
					return LEFT;
				}
			}
		}else if(e.equals(Var.EPSILON)){
			return RIGHT;
		}else if(e.equals(Var.NEG_EPSILON)){
			return LEFT;
		}
		return NONE;
	}
	
	
	public static Expr stripDirection(Expr e){//does not modify input
		if(e.isType("sum")){
			Func innerSum = (Func)e;
			for(int i = 0;i<innerSum.size();i++){
				if(innerSum.get(i).equals(Var.EPSILON)){
					Func outSum = (Func)e.copy();
					outSum.remove(i);
					return Sum.unCast(outSum);
				}else if(innerSum.get(i).equals(Var.NEG_EPSILON)){
					Func outSum = (Func)e.copy();
					outSum.remove(i);
					return Sum.unCast(outSum);
				}
			}
		}else if(e.equals(Var.EPSILON)){
			return Cas.num(0);
		}else if(e.equals(Var.NEG_EPSILON)){
			return Cas.num(0);
		}
		return e.copy();
	}
	
	public static Expr applyDirection(Expr e,short direction){//modifies input
		Expr epsilonAdder = direction == LEFT ? Var.NEG_EPSILON.copy() : (  direction == RIGHT ? Cas.epsilon() : null);
		
		if(epsilonAdder != null){
			if(e.isType("sum")){
				e.add(epsilonAdder);
				return e;
			}
			return Cas.sum(epsilonAdder,e);
		}
		return e;
	}
	
	public static boolean zeroOrEpsilon(Expr e){
		return e.equals(Num.ZERO) || isEpsilon(e);
	}
	
	
	public static boolean isEpsilon(Expr e){
		return e.equals(Var.EPSILON) || e.equals(Var.NEG_EPSILON);
	}
	public static boolean isInf(Expr e){
		return e.equals(Var.INF) || e.equals(Var.NEG_INF);
	}
}
