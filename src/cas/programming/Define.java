package cas.programming;

import cas.*;
import cas.lang.Interpreter;
import cas.primitive.*;

public class Define extends Expr{
	private static final long serialVersionUID = 1448193931833648039L;

	public Define() {
		simplifyChildren = false;
	}//
	
	public Define(Expr toBeAssigned,Expr assignment) {
		add(toBeAssigned);
		add(assignment);
		simplifyChildren = false;
	}
	
	public Expr getLeftSide() {
		return get(0);
	}
	
	public Expr getRightSide() {
		return get(1);
	}
	
	static Rule addDefinition = new Rule("add definition",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Define def = (Define)e;
			if(def.getLeftSide() instanceof Var) {
				casInfo.definitions.defineVar(equ(def.getLeftSide(),def.getRightSide().simplify(casInfo)));
			}else if(def.getLeftSide() instanceof Func) {
				casInfo.definitions.addFuncRule(becomes(def.getLeftSide(),def.getRightSide()));
			}
			return Interpreter.SUCCESS;
		}
		
	};
	
	static Sequence ruleSequence;
	
	public static void loadRules() {
		ruleSequence = sequence(
				addDefinition
		);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}
	
	@Override
	public String toString() {
		String out = "";
		out+=getLeftSide();
		out+=":=";
		out+=getRightSide();
		return out;
	}
	
	@Override
	public String typeName() {
		return "define";
	}
}
