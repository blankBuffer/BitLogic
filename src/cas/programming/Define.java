package cas.programming;

import cas.Cas;
import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.primitive.*;

public class Define{
	
	public static Func.FuncLoader defineLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			
			Rule addDefinition = new Rule("add definition") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func define = (Func)e;
					if(Define.getLeftSide(define) instanceof Var) {
						casInfo.definitions.defineVar(equ(Define.getLeftSide(define),Define.getRightSide(define).simplify(casInfo)));
					}else if(Define.getLeftSide(define) instanceof Func) {
						Rule rule = new Rule(Cas.becomes(Define.getLeftSide(define),Define.getRightSide(define)),"function rule");
						rule.init();
						
						String name = Define.getLeftSide(define).typeName();
						
						casInfo.definitions.defineFunc(name, rule);
					}
					return Var.SUCCESS;
				}
				
			};
			
			owner.behavior.rule = new Rule(new Rule[] {
					addDefinition
			},"main sequence");
			
			owner.behavior.simplifyChildren = false;
			
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					String out = "";
					out+=Define.getLeftSide(owner);
					out+=":=";
					out+=Define.getRightSide(owner);
					return out;
				}
			};
		}
		
	};
	
	public static Expr getLeftSide(Func define) {
		return define.get(0);
	}
	
	public static Expr getRightSide(Func define) {
		return define.get(1);
	}
}
