package cas.programming;

import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.bool.BoolState;

public class Ternary{
	
	public static Func.FuncLoader ternaryLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule(new Rule[] {
					ternaryOperation,
			},"main sequence");
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					String out = "";
					out+=owner.get();
					out+="?";
					out+=ifTrue(owner);
					out+=":";
					out+=ifFalse(owner);
					return out;
				}
			};
		}
	};
	
	public static Expr ifTrue(Func ternary) {
		return ternary.get(1);
	}
	public static Expr ifFalse(Func ternary) {
		return ternary.get(2);
	}
	
	static Rule ternaryOperation = new Rule("the ternary operator") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func tern = (Func)e;
			
			if(tern.get().simplify(casInfo).equals(BoolState.TRUE)) {
				return ifTrue(tern).simplify(casInfo);
			}
			return ifFalse(tern).simplify(casInfo);
		}
		
	};
}
