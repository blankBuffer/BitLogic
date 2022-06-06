package cas.trig;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.StandardRules;
import cas.primitive.ExprList;
import cas.primitive.Sequence;

public class Acos extends Expr{
	
	private static final long serialVersionUID = 3855238699397076495L;

	public Acos(){}
	public Acos(Expr expr) {
		add(expr);
	}
	
	static Rule containsInverse = new Rule("acos(cos(x))->x","acos contains inverse");
	static Rule containsSin = new Rule("acos(sin(x))->-x+pi/2","acos contains inverse");
	
	static Rule negativeInner = new Rule("arccos of negative value"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Acos acos = (Acos)e;
			if(acos.get().negative()){
				Expr result = sum(neg(acos( neg(acos.get()).simplify(casInfo) )),pi());
				return result.simplify(casInfo);
			}
			return acos;
		}
	};
	
	static Rule inverseUnitCircle = new Rule(new Rule[]{
			new Rule("acos(0)->pi/2","arccos of zero"),
			new Rule("acos(1)->0","arccos of one"),
			new Rule("acos(sqrt(2)/2)->pi/4","arccos of root 2 over 2"),
			new Rule("acos(1/2)->pi/3","arccos of a half"),
			new Rule("acos(sqrt(3)/2)->pi/6","arccos of root 3 over 2"),
	},"unit circle for arccos");
	
	static Rule arccosWithSqrt = new Rule(new Rule[]{
			new Rule("acos(sqrt(a*x+b)/c)->asin((c^2-2*a*x-2*b)/c^2)/2+pi/4","arcsin with square root"),
			new Rule("acos(sqrt(x+b)/c)->asin((c^2-2*x-2*b)/c^2)/2+pi/4","arcsin with square root"),
			new Rule("acos(sqrt(a*x+b))->asin(1-2*a*x-2*b)/2+pi/4","arcsin with square root"),
			new Rule("acos(sqrt(x+b))->asin(1-2*x-2*b)/2+pi/4","arcsin with square root"),
		},"arcsin with square root");
	
	static Sequence ruleSequence = null;
	
	public static void loadRules() {
		ruleSequence = sequence(
				StandardRules.trigCompressInner,
				negativeInner,
				arccosWithSqrt,
				containsInverse,
				containsSin,
				inverseUnitCircle
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence(){
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.acos(get().convertToFloat(varDefs));
	}
	
	@Override
	public String typeName() {
		return "acos";
	}
	@Override
	public String help() {
		return "acos(x) is the arc cosine function\n"
				+ "examples\n"
				+ "acos(0)->pi/2\n"
				+ "acos(cos(x))->x";
	}
}
