package cas.trig;

import java.math.BigInteger;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.Settings;
import cas.StandardRules;
import cas.primitive.Div;
import cas.primitive.ExprList;
import cas.primitive.Num;
import cas.primitive.Sequence;
import cas.primitive.Sum;
import cas.primitive.Var;

public class Cos extends Expr{
	
	private static final long serialVersionUID = -529344373251624547L;
	
	static Rule cosOfArctan = new Rule("cos(atan(x))->1/sqrt(1+x^2)","cos or arctan",Rule.UNCOMMON);
	static Rule cosOfArcsin = new Rule("cos(asin(x))->sqrt(1-x^2)","cos of arcsin",Rule.UNCOMMON);
	static Rule cosOfArccos = new Rule("cos(acos(x))->x","cos of arccos",Rule.UNCOMMON);
	
	static Rule cosOfAbs = new Rule("cos(abs(x))->cos(x)","cos of arccos",Rule.UNCOMMON);

	public Cos(){}//
	public Cos(Expr a) {
		add(a);
	}
	
	static Rule unitCircle = new Rule("unit circle for cos",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Cos cos = (Cos)e;
			Var pi = pi();
			BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4),twelve = BigInteger.valueOf(12);
			Expr innerExpr = distr(cos.get()).simplify(settings);
			
			Expr out = cos;
			if(innerExpr.equals(num(0))) {
				out = num(1);
			}else if(innerExpr.equals(pi))
				out = num(-1);
			if(innerExpr instanceof Div && innerExpr.contains(pi())){
				Div frac =((Div)innerExpr).ratioOfUnitCircle();
				
				if(frac!=null) {
					
					BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
					
					numer = numer.mod(denom.multiply(BigInteger.TWO));//restrict to whole circle
					int negate = 1;
					
					if(numer.compareTo(denom) == 1) {//if we are past the top half flip over x axis
						numer = BigInteger.TWO.multiply(denom).subtract(numer);
					}
					
					if(numer.compareTo(denom.divide(BigInteger.TWO)) == 1) {//if we are past quarter circle, reflect over y axis and flip sign
						numer = denom.subtract(numer);
						negate = -negate;
					}
					
					if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) out = num(0);
					else if(numer.equals(BigInteger.ONE) && denom.equals(three)) out = inv(num(2*negate));
					else if(numer.equals(BigInteger.ONE) && denom.equals(six)) out = div(sqrt(num(3)),num(2*negate));
					else if(numer.equals(BigInteger.ONE) && denom.equals(four)) out = div(sqrt(num(2)),num(2*negate));
					else if(numer.equals(BigInteger.ONE) && denom.equals(twelve)) return div(sum(sqrt(num(6)),sqrt(num(2))),num(4));
					else if(numer.equals(BigInteger.ZERO)) out = num(negate);
					else {
						//make it into the sin version for canonical form
						out = prod(  num(negate),   sin(sum( div(prod(pi(),num(numer)),num(denom)) ,div(pi(),num(2))))   ).simplify(settings);
						//
						
					}
					
					
				}
				
			}else if(innerExpr instanceof Sum) {//cos(x-pi/4) can be turned into sin(x+7*pi/4) because sin has symmetry
				for(int i = 0;i<innerExpr.size();i++) {
					if(innerExpr.get(i) instanceof Div && !innerExpr.get(i).containsVars() && innerExpr.get(i).contains(pi)) {
						Div frac = ((Div)innerExpr.get(i)).ratioOfUnitCircle();
						
						if(frac!=null) {
							//make it into the sin version for canonical form
							out = sin(sum( innerExpr ,div(pi(),num(2)))).simplify(settings);
							//
						}
						
					}
				}
			}
			return out;
		}
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				cosOfArctan,
				cosOfArcsin,
				cosOfArccos,
				cosOfAbs,
				StandardRules.evenFunction,
				StandardRules.distrInner,
				unitCircle
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.cos(get().convertToFloat(varDefs));
	}
}
