package cas.bool;

import cas.*;
import cas.primitive.*;

public class BoolTableToExpr extends Expr{
	private static final long serialVersionUID = -2170469894690582976L;
	
	public BoolTableToExpr() {}//
	public BoolTableToExpr(ExprList table, ExprList vars) {
		add(table);
		add(vars);
	}
	
	public ExprList getTable() {
		return ExprList.cast(get(0));
	}
	public ExprList getVars() {
		return ExprList.cast(get(1));
	}
	public Var getVar(int index) {
		return (Var) getVars().get(index);
	}
	
	static Rule generate = new Rule("generate the function") {
		private static final long serialVersionUID = 1L;
		
		Expr generateTerm(Becomes inOut,ExprList vars) {
			if(inOut.getRightSide().equals(BoolState.TRUE)) {
				And term = new And();
				Sequence in = (Sequence) inOut.getLeftSide();
				for(int i = 0;i<in.size();i++) {
					if(in.get(i).equals(BoolState.TRUE)) {
						term.add( vars.get(i) );
					}else {
						term.add( not(vars.get(i)) );
					}
				}
				return term;
			}
			return bool(false);//ignore false values since we are in 'or' form
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Or out = new Or();//or together all the true statements
			BoolTableToExpr casted = (BoolTableToExpr)e;
			
			for(int i = 0;i<casted.getTable().size();i++) {
				Becomes inOut = (Becomes) casted.getTable().get(i);
				
				out.add(generateTerm(inOut,casted.getVars()));
			}
			
			return out.simplify(casInfo);
		}
		
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				generate
		);
	}

	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public String help() {
		return "boolTableToExpr(exprList of in outs,exprList of vars) function\n"
				+"boolTableToExpr({[false,false]->true,[false,true]->false,[true,false]->true,[true,true]->true},{x,y})->x|~y";
		
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.ZERO;
	}

	@Override
	public String typeName() {
		return "boolTableToExpr";
	}

}
