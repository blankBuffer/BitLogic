package cas.programming;

import cas.*;
import cas.bool.BoolState;
import cas.primitive.ExprList;
import cas.primitive.Sequence;

public class Ternary extends Expr{
	private static final long serialVersionUID = 6074945872809841123L;

	public Ternary() {
		simplifyChildren = false;
	}//
	
	public Ternary(Expr toBeEvaled,Expr ifTrue,Expr ifFalse) {
		simplifyChildren = false;
		add(toBeEvaled);
		add(ifTrue);
		add(ifFalse);
	}
	
	public Expr ifTrue() {
		return get(1);
	}
	public Expr ifFalse() {
		return get(2);
	}
	
	static Rule ternaryOperation = new Rule("the ternary operator",Rule.VERY_EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Ternary tern = (Ternary)e;
			
			if(tern.get().simplify(casInfo).equals(BoolState.TRUE)) {
				return tern.ifTrue().simplify(casInfo);
			}
			return tern.ifFalse().simplify(casInfo);
		}
		
	};
	
	static Sequence ruleSequence;
	
	public static void loadRules() {
		ruleSequence = sequence(
			ternaryOperation
		);
	}

	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	@Override
	public String toString() {
		String out = "";
		out+=get();
		out+="?";
		out+=ifTrue();
		out+=":";
		out+=ifFalse();
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

	@Override
	public String typeName() {
		return "ternary";
	}
}
