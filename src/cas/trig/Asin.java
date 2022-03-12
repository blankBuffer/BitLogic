package cas.trig;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.StandardRules;
import cas.primitive.ExprList;
import cas.primitive.Sequence;

public class Asin extends Expr{
	
	private static final long serialVersionUID = 8245957240404627757L;
	
	static Rule asinSinCase = new Rule("asin(sin(x))->x","arcsin of the sin");
	static Rule asinCosCase = new Rule("asin(cos(x))->-x+pi/2","arcsin of cosine");

	static Rule inverseUnitCircle = new Rule("asin unit circle"){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
				new Rule("asin(0)->0","arcsin of zero"),
				new Rule("asin(1)->pi/2","arcsin of one"),
				new Rule("asin(sqrt(2)/2)->pi/4","arcsin of root 2 over 2"),
				new Rule("asin(1/2)->pi/6","arcsin of a half"),
				new Rule("asin(sqrt(3)/2)->pi/3","arcsin of root 3 over 2"),
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, casInfo);
			}
			return e;
		}
		
	};
	
	static Rule arcsinWithSqrt = new Rule("arcsin with square root"){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
				new Rule("asin(sqrt(a*x+b)/c)->asin((c^2-2*a*x-2*b)/c^2)/-2+pi/4","arcsin with square root"),
				new Rule("asin(sqrt(x+b)/c)->asin((c^2-2*x-2*b)/c^2)/-2+pi/4","arcsin with square root"),
				new Rule("asin(sqrt(a*x+b))->asin(1-2*a*x-2*b)/-2+pi/4","arcsin with square root"),
				new Rule("asin(sqrt(x+b))->asin(1-2*x-2*b)/-2+pi/4","arcsin with square root"),
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, casInfo);
			}
			return e;
		}
	};
	
	public Asin(){}//
	public Asin(Expr expr) {
		add(expr);
	}
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				StandardRules.trigCompressInner,
				StandardRules.oddFunction,
				arcsinWithSqrt,
				asinSinCase,
				asinCosCase,
				inverseUnitCircle
		);
		Rule.initRules(ruleSequence);
	}
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.asin(get().convertToFloat(varDefs));
	}
	
	@Override
	public String typeName() {
		return "asin";
	}

}
