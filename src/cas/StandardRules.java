package cas;

import cas.primitive.Num;
import cas.primitive.Sequence;
import cas.primitive.Sum;
import cas.primitive.Var;

public class StandardRules extends QuickMath{
	public static Rule oddFunction = new Rule("function is odd",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			e.set(0,factor(e.get()).simplify(casInfo));
			if(e.get().negative()) {
				Expr newInner = neg(e.get()).simplify(casInfo);
				e.set(0, newInner);
				Expr out = neg(e);
				return out.simplify(casInfo);
			}
			
			return e;
		}
	};
	
	public static Rule evenFunction = new Rule("function is even",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			
			e.set(0,factor(e.get()).simplify(casInfo));
			if(e.get().negative()) {
				Expr newInner = neg(e.get()).simplify(casInfo);
				e.set(0, newInner);
				
				return e.simplify(casInfo);
			}
			
			return e;
		}
	};
	
	public static Rule trigCompressInner = new Rule("trig compress inside the function",Rule.CHALLENGING){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			if(cases != null) return;
			cases = new Rule[]{
				new Rule("sin(x)*cos(x)->sin(2*x)/2","compressing trig",Rule.EASY),
				new Rule("a*sin(x)*cos(x)->a*sin(2*x)/2","compressing trig",Rule.EASY),
				new Rule("2*cos(x)^2-1->cos(2*x)","compressing trig",Rule.EASY),
				new Rule("2*tan(x)/(1-tan(x)^2)->tan(2*x)","compressing trig",Rule.EASY),
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Expr inner = e.get();
			
			for(Rule r:cases){
				inner = r.applyRuleToExpr(inner, casInfo);
			}
			
			e.set(0, inner);
			
			return e;
		}
	};
	
	public static Rule linearOperator = new Rule("operator in linear",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Expr expr = e.get();
			if(expr instanceof Sum){
				Expr depletedExpr = e.copy();
				depletedExpr.set(0, Num.ZERO);
				
				Sum out = new Sum();
				
				for(int i = 0;i<expr.size();i++){
					Expr current = depletedExpr.copy();
					current.set(0,expr.get(i));
					out.add(current);
				}
				return out.simplify(casInfo);
			}
			return e;
		}
	};
	
	public static Rule pullOutConstants = new Rule("pull out constants",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Expr expr = e.get();
			Var v = e.getVar();
			
			Sequence res = seperateByVar(expr,v);
			if(!res.get(0).equals(Num.ONE)) {
				e.set(0, res.get(1));
				return prod(res.get(0),e).simplify(casInfo);
			}
			
			return e;
		}
	};
	
	public static Rule becomeInner = new Rule("become the argument",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			return e.get();
		}
	};
	
	public static Rule distrInner = new Rule("distribute inner",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			e.set(0, distr(e.get()).simplify(casInfo));
			return e;
		}
		
	};
	
	public static Rule factorInner = new Rule("factor inner",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			e.set(0, factor(e.get()).simplify(casInfo));
			return e;
		}
		
	};
	
	public static Rule showState = new Rule("print state",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			System.out.println(e);
			return e;
		}
	};

	public static void loadRules() {
		oddFunction.init();
		evenFunction.init();
		trigCompressInner.init();
		linearOperator.init();
		pullOutConstants.init();
		becomeInner.init();
		distrInner.init();
		factorInner.init();
		showState.init();
	}
	
}
