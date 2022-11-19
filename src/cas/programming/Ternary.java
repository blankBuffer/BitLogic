package cas.programming;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.bool.BoolState;

public class Ternary extends Expr{
	public Ternary() {
		//simplifyChildren = false;
	}//
	
	public Ternary(Expr toBeEvaled,Expr ifTrue,Expr ifFalse) {
		//simplifyChildren = false;
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
	
	static Rule ternaryOperation = new Rule("the ternary operator") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Ternary tern = (Ternary)e;
			
			if(tern.get().simplify(casInfo).equals(BoolState.TRUE)) {
				return tern.ifTrue().simplify(casInfo);
			}
			return tern.ifFalse().simplify(casInfo);
		}
		
	};
	
	static Rule mainSequenceRule = null;

	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
				ternaryOperation
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
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
	public ComplexFloat convertToFloat(Func varDefs) {
		return ComplexFloat.ZERO;
	}

	@Override
	public String typeName() {
		return "ternary";
	}

	@Override
	public String help() {
		return "condition?if_true:if_false operator\n"
				+ "examples\n"
				+ "2+3->5\n"
				+ "x+x+y->2*x+y";
	}
}
