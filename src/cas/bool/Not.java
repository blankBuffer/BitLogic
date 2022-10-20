package cas.bool;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.primitive.ExprList;
import cas.primitive.Func;
import cas.primitive.Var;

public class Not{
	
	public static Func.FuncLoader notLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			Rule isTrue = new Rule("~true->false","true case");
			Rule isFalse = new Rule("~false->true","false case");
			Rule containsNot = new Rule("~~x->x","contains not");
			Rule demorgan = new Rule("demorgan"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func not = (Func)e;
					Expr result = (not.get().typeName().equals("or") ? and() :(not.get().typeName().equals("and") ? or() :null));
					if(result != null){
						
						for(int i = 0;i<not.get().size();i++){
							result.add(not(not.get().get(i)));
						}
						result = result.simplify(casInfo);
						return result;
					}
					return not;
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
				isTrue,
				isFalse,
				containsNot,
				demorgan
			},"main sequence");
			owner.behavior.rule.init();
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
					boolean state = Math.abs(owner.get().convertToFloat(varDefs).real) < 0.5;
					double res = state ? 1.0 : 0.0;
					return new ComplexFloat(res,0);
				}
			};
			
			owner.behavior.toStringMethod = new Func.ToString() {
				@Override
				public String generateString(Func owner) {
					String out = "";
					out+="~";
					boolean paren = !(owner.get() instanceof Var);
					if(paren) out+="(";
					out+=owner.get();
					if(paren) out+=")";
					return out;
				}
			};
		}
	};
}
