package cas.calculus;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;

import java.math.BigInteger;

import cas.CasInfo;
import cas.StandardRules;
import cas.bool.BoolState;
import cas.primitive.*;

public class Limit extends Expr{
	private static final long serialVersionUID = 3302019973998257065L;
	public static final short RIGHT = 1,LEFT = -1,NONE = 0;
	
	public Limit(){}//
	
	public Limit(Expr e,Becomes becomes){
		add(e);
		add(becomes);
	}
	
	@Override
	public Var getVar(){
		return (Var) get(1).get(0);
	}
	
	public Expr getExpr(){
		return get();
	}
	
	public void setExpr(Expr e){
		set(0,e);
	}
	
	public Expr getValue(){
		return get(1).get(1);
	}
	
	public Becomes getApproaches() {
		return (Becomes)get(1);
	}
	
	
	public short getDirection(){
		return getDirection(getValue());
	}
	
	
	public static short flipDirection(short direction){
		return (short)(-direction);
	}
	
	
	//returns the direction of an expression right or left
	public static short getDirection(Expr e){
		if(e instanceof Sum){
			Sum innerSum = (Sum)e;
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
		if(e instanceof Sum){
			Sum innerSum = (Sum)e;
			for(int i = 0;i<innerSum.size();i++){
				if(innerSum.get(i).equals(Var.EPSILON)){
					Sum out = (Sum)e.copy();
					out.remove(i);
					return Sum.unCast(out);
				}else if(innerSum.get(i).equals(Var.NEG_EPSILON)){
					Sum out = (Sum)e.copy();
					out.remove(i);
					return Sum.unCast(out);
				}
			}
		}else if(e.equals(Var.EPSILON)){
			return num(0);
		}else if(e.equals(Var.NEG_EPSILON)){
			return num(0);
		}
		return e.copy();
	}
	
	public static Expr applyDirection(Expr e,short direction){//modifies input
		Expr epsilonAdder = direction == LEFT ? neg(epsilon()) : (  direction == RIGHT ? epsilon() : null);
		
		if(epsilonAdder != null){
			if(e instanceof Sum){
				e.add(epsilonAdder);
				return e;
			}
			return sum(epsilonAdder,e);
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
	/*
	 * puts the value directly, last resort
	 */
	static Rule directSubst = new Rule("direct substitution"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Limit lim = (Limit)e;
			return lim.getExpr().replace(equ(lim.getVar(),lim.getValue())).simplify(casInfo);
		}
	};
	
	/*
	 * the mechanism works by this principal limit(a^(b/c)/d,) can be transformed into limit(a^b/d^c,)^c if c is a constant
	 * lhopitals rule will work better if its not a root as it avoid infinite recursion
	 */
	static Rule limitOfDivWithRoot = new Rule("limit of division where either numerator of denominator is a root function") {
		private static final long serialVersionUID = 1L;
		
		Expr rootExpr;
		Expr rootCondition;
		
		@Override
		public void init(){
			rootExpr = createExpr("a^(b/c)");
			rootCondition = createExpr("~contains(c,x)");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Limit lim = (Limit)e;
			
			if(lim.getExpr() instanceof Div) {
				Div innerDiv = (Div)lim.getExpr();
				
				boolean numerIsRoot = Rule.similarWithCondition(rootExpr,innerDiv.getNumer(),rootCondition.replace(equ(lim.getVar(),var("x"))));
				boolean denomIsRoot = Rule.similarWithCondition(rootExpr,innerDiv.getDenom(),rootCondition.replace(equ(lim.getVar(),var("x"))));
				
				int sign = 1;
				
				Expr rootExpr = null;
				if(numerIsRoot && !denomIsRoot){//numer case
					rootExpr = ((Div)((Power)innerDiv.getNumer()).getExpo()).getDenom();
					
					Expr denomAtInf = limit(innerDiv.getDenom(),lim.getApproaches()).simplify(casInfo);
					if(isRealNum(rootExpr) && ((Num)rootExpr).realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO) && eval(equLess(denomAtInf,num(0))).simplify(casInfo).equals(BoolState.TRUE) ) {
						sign = -1;
					}
					
				}else if(!numerIsRoot && denomIsRoot) {//denom case
					rootExpr = ((Div)((Power)innerDiv.getDenom()).getExpo()).getDenom();
					
					Expr numerAtInf = limit(innerDiv.getNumer(),lim.getApproaches()).simplify(casInfo);
					if(isRealNum(rootExpr) && ((Num)rootExpr).realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO) && eval(equLess(numerAtInf,num(0))).simplify(casInfo).equals(BoolState.TRUE) ) {
						sign = -1;
					}
					
				}else if(numerIsRoot && denomIsRoot) {
					Expr numerRoot = ((Div)((Power)innerDiv.getNumer()).getExpo()).getDenom();
					Expr denomRoot = ((Div)((Power)innerDiv.getDenom()).getExpo()).getDenom();
					
					if(isRealNum(numerRoot) && isRealNum(denomRoot)) {
						rootExpr = num(gcm( ((Num)numerRoot).realValue , ((Num)denomRoot).realValue ));
					}else {
						rootExpr = prod(numerRoot,denomRoot);
					}
				}
				
				if(rootExpr != null) {
					lim.setExpr( pow(lim.getExpr(),rootExpr) );
					return prod(pow(limit(lim.getExpr(),lim.getApproaches()),inv(rootExpr)),num(sign)).simplify(casInfo);
				}
			}
			
			return lim;
		}
	};
	
	/*
	 * if there is a factor-able exponent in the limit that is constant
	 * limit(a^b/c^b,) can be turned into limit(a/c,)^b if b is constant
	 * 
	 * we still in general want to factor the exponent so lhoptials rule works better
	 */
	static Rule commonExpoDiv = new Rule("common exponent for division limit"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Limit lim = (Limit)e;
			
			if(lim.getExpr() instanceof Div) {
				Div innerDiv = (Div)lim.getExpr();
				if(innerDiv.getNumer() instanceof Power && innerDiv.getDenom() instanceof Power) {
					Power numerPower = (Power)innerDiv.getNumer();
					Power denomPower = (Power)innerDiv.getDenom();
					
					Expr gcd = gcd(numerPower.getExpo(),denomPower.getExpo()).simplify(casInfo);
					
					if(gcd.equals(Num.ONE)) return lim;
					
					innerDiv.flags.simple = false;
					
					if(gcd.contains(lim.getVar())) {
						numerPower.setExpo(div(numerPower.getExpo(),gcd).simplify(casInfo));
						denomPower.setExpo(div(denomPower.getExpo(),gcd).simplify(casInfo));
						
						lim.setExpr(pow(div(numerPower,denomPower),gcd));
						return lim;
					}
					numerPower.setExpo( div(numerPower.getExpo(),gcd).simplify(casInfo) );
					denomPower.setExpo( div(denomPower.getExpo(),gcd).simplify(casInfo) );
					
					Expr out = pow(lim,gcd);
					return out.simplify(casInfo);
					
				}
			}
			return lim;
		}
	};
	
	/*
	 * if the limit of the numerator and denominator is epsilon/epsilon or inf/inf then take the derivative of
	 * the numerator and denominator and try again
	 */
	static Rule lhopitalsRuleDiv = new Rule("lhopitals rule with division") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Limit lim = (Limit)e;
			
			if(lim.getExpr() instanceof Div) {
				Div innerDiv = (Div)lim.getExpr();
				
				Expr limOfNumer = limit(innerDiv.getNumer(),lim.getApproaches()).simplify(casInfo);
				Expr limOfDenom = limit(innerDiv.getDenom(),lim.getApproaches()).simplify(casInfo);
				
				if(zeroOrEpsilon(limOfNumer) && zeroOrEpsilon(limOfDenom) || isInf(limOfNumer) && isInf(limOfDenom)) {
					Expr diffNumer = diff(innerDiv.getNumer(),lim.getVar()).simplify(casInfo);
					Expr diffDenom = diff(innerDiv.getDenom(),lim.getVar()).simplify(casInfo);
					
					Expr out = limit(div(diffNumer,diffDenom),lim.getApproaches());
					return out.simplify(casInfo);
					
				}
				innerDiv.setNumer(limOfNumer);
				innerDiv.setDenom(limOfDenom);
				
				
			}
			
			return lim;
		}
	};
	
	//if the numerator has a larger degree 
	static Rule differentDegreePolysInDiv = new Rule("different degrees in division") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Limit lim = (Limit)e;
			
			if(lim.getExpr() instanceof Div && isInf(lim.getValue())) {
				Div innerDiv = (Div)lim.getExpr();
				
				BigInteger numerDegree = degree(innerDiv.getNumer(),lim.getVar());
				if(numerDegree.signum() != 1) return lim;
				BigInteger denomDegree = degree(innerDiv.getDenom(),lim.getVar());
				if(denomDegree.signum() != 1) return lim;
				
				int comparison = numerDegree.compareTo(denomDegree);
				
				Expr out = null;
				if(comparison == 1) {
					out = limit(innerDiv.getNumer(),lim.getApproaches());
				}else if(comparison == -1) {
					out = inv(limit(innerDiv.getDenom(),lim.getApproaches()));
				}
				if(out != null) return out.simplify(casInfo);
			}
			
			return lim;
		}
	};
	
	//remove all parts that are smaller degree in the sum
	static Rule polyCase = new Rule("limit of a polynomial") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Limit lim = (Limit)e;
			
			if(isPolynomialUnstrict(lim.getExpr(), lim.getVar()) && isInf(lim.getValue())) {
				lim.setExpr(expand(lim.getExpr()).simplify(casInfo));
				
				BigInteger degree = degree(lim.getExpr(),lim.getVar());
				
				if(lim.getExpr() instanceof Sum) {
					
					Sum innerSum = (Sum)lim.getExpr();
					for(int i = 0;i<innerSum.size();i++) {
						if(degree(innerSum.get(i),lim.getVar()).equals(degree)) {
							return limit(innerSum.get(i),lim.getApproaches()).simplify(casInfo);
						}
					}
					
				}
				
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
	 */
	static Rule crazyRootSubtraction = new Rule("special subtraction of two roots with polynomials inside") {
		private static final long serialVersionUID = 1L;
		
		Expr rootForm;
		Expr condition;
		
		@Override
		public void init() {
			rootForm = createExpr("a^(1/n)");
			condition = createExpr("isType(a,sum)&isType(n,num)");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Limit lim = (Limit)e;
			
			if(isInf(lim.getValue()) && lim.getExpr() instanceof Sum) {
				Sum innerSum = (Sum)lim.getExpr();
				
				for(int i = 0;i<innerSum.size();i++) {
					Sequence parts = seperateByVar(innerSum.get(i),lim.getVar());
					if(Rule.similarWithCondition(rootForm, parts.get(1), condition)) {
						Power inner = (Power)parts.get(1);
						Sequence innerPoly = polyExtract(inner.getBase(),lim.getVar(),casInfo);
						if(innerPoly == null) return lim;
						Num n = (Num) ((Div)inner.getExpo()).getDenom();
						if(!isPositiveRealNum(n)) return lim;
						if(innerPoly.size()-1 == n.realValue.intValue()) {
							Expr a = innerPoly.get(innerPoly.size()-1);
							Expr b = innerPoly.get(innerPoly.size()-2);
							
							Expr repl = null;
							
							if(lim.getValue().equals(Var.INF) || n.realValue.mod(BigInteger.TWO).equals(BigInteger.ONE)) {
								repl = sum(prod(pow(a,inv(n)),lim.getVar(),parts.get(0)), div(prod(b,pow(a,inv(n)),parts.get(0)),prod(a,n)) );
							}else {
								repl = sum(prod(num(-1),pow(a,inv(n)),lim.getVar(),parts.get(0)), div(prod(num(-1),b,pow(a,inv(b)),parts.get(0)),prod(a,n)) );
							}
							
							innerSum.set(i, repl);
						}
					}
				}
				lim.setExpr(innerSum.simplify(casInfo));
			}
			return lim;
		}
		
	};
	
	static Rule lhopitalsRulePow = new Rule("lhopitals rule with powers") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Limit lim = (Limit)e;
			if(lim.getExpr() instanceof Power) {
				Power innerPow = (Power)lim.getExpr();
				
				Expr limOfBase = limit(innerPow.getBase(),lim.getApproaches()).simplify(casInfo);
				Expr limOfExpo = limit(innerPow.getExpo(),lim.getApproaches()).simplify(casInfo);
				
				if( (zeroOrEpsilon(limOfBase) || isInf(limOfBase)) && zeroOrEpsilon(limOfExpo) || stripDirection(limOfBase).equals(Num.ONE)) {
					
					Expr diffLogBase = diff(ln(innerPow.getBase()),lim.getVar()).simplify(casInfo);
					Expr diffInvExpo = diff(inv(innerPow.getExpo()),lim.getVar()).simplify(casInfo);
					
					return exp(limit(div(diffLogBase,diffInvExpo),lim.getApproaches())).simplify(casInfo);
					
				}
				innerPow.setBase(limOfBase);
				innerPow.setExpo(limOfExpo);
			}
			return lim;
		}
		
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				StandardRules.pullOutConstants,
				commonExpoDiv,
				lhopitalsRulePow,
				limitOfDivWithRoot,
				differentDegreePolysInDiv,
				polyCase,
				crazyRootSubtraction,
				lhopitalsRuleDiv,
				directSubst
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat(varDefs);
	}

	@Override
	public String typeName() {
		return "limit";
	}
}
