package cas.primitive;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

import static cas.Cas.*;

public class Approx{
	
	public static Func.FuncLoader approxLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			owner.behavior.helpMessage = "Approximates a function with variables defined.\n"
					+ "Example, approx(sin(2),{}) returns 0.9092974268256817\n"
					+ "Example, approx(sqrt(x+1),{x=2}) returns 1.7320508075688774";
			
			Rule getFloatExpr = new Rule("get float approximation"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func approx = (Func)e;
					Expr out = floatExpr(approx.get().convertToFloat((Func)approx.get(1)));
					return out;
				}
			};
			
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule(new Rule[]{
					getFloatExpr,
			},"main sequence");
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return owner.get().convertToFloat((Func)owner.get(1));//kinda pointless but whatever
				}
			};
			owner.behavior.rule.init();
		}
	};
}
