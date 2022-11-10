package cas.bool;

import cas.*;
import cas.primitive.*;

public class BoolTableToExpr{
	
	
	//boolTableToExpr({[false,false]->true,[false,true]->false,[true,false]->true,[true,true]->true},{x,y})->x|~y
	public static Func.FuncLoader boolTableToExprLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			Rule generate = new Rule("generate the function") {
				Expr generateTerm(Func inOutBecomes,Func vars) {
					if(Becomes.getRightSide(inOutBecomes).equals(BoolState.TRUE)) {
						Func termAnd = and();
						Sequence in = (Sequence) Becomes.getLeftSide(inOutBecomes);
						for(int i = 0;i<in.size();i++) {
							if(in.get(i).equals(BoolState.TRUE)) {
								termAnd.add( vars.get(i) );
							}else {
								termAnd.add( not(vars.get(i)) );
							}
						}
						return termAnd;
					}
					return bool(false);//ignore false values since we are in 'or' form
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func outOr = or();//or together all the true statements
					Func castedBoolTableToExpr = (Func)e;
					
					for(int i = 0;i<getTable(castedBoolTableToExpr).size();i++) {
						Func inOutBecomes = (Func) getTable(castedBoolTableToExpr).get(i);
						
						outOr.add(generateTerm(inOutBecomes,getVars(castedBoolTableToExpr)));
					}
					return outOr.simplify(casInfo);
				}
				
			};
			owner.behavior.rule = new Rule(new Rule[]{
				generate,
			},"main sequence");
		}
	};
	
	public static Func getTable(Func boolTableToExpr) {
		return ExprSet.cast(boolTableToExpr.get(0));
	}
	public static Func getVars(Func boolTableToExpr) {
		return ExprSet.cast(boolTableToExpr.get(1));
	}

}
