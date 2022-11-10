package cas.programming;

import cas.*;
import cas.lang.Interpreter;
import cas.primitive.*;

public class Define extends Expr{
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
	
	static Rule addDefinition = new Rule("add definition") {
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
	
	static Rule mainSequenceRule = null;
	
	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
				addDefinition
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
	}

	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
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

	@Override
	public String help() {
		return ":= operator\n"
				+ "examples\n"
				+ "x:=2\n"
				+ "f(x):=x^2";
	}
}
