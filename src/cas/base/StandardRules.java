package cas.base;

import cas.Cas;
import cas.primitive.Num;
import cas.primitive.Var;

public class StandardRules extends Cas{
	public static Rule oddFunction = new Rule("function is odd"){
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
	
	public static Rule evenFunction = new Rule("function is even"){
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
	
	public static Rule trigCompressInner = new Rule("trig compress inside the function"){
		Rule[] cases;
		@Override
		public void init(){
			if(cases != null) return;
			cases = new Rule[]{
				new Rule("sin(x)*cos(x)->sin(2*x)/2","compressing trig"),
				new Rule("a*sin(x)*cos(x)->a*sin(2*x)/2","compressing trig"),
				new Rule("2*cos(x)^2-1->cos(2*x)","compressing trig"),
				new Rule("2*tan(x)/(1-tan(x)^2)->tan(2*x)","compressing trig"),
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
	
	public static Rule linearOperator = new Rule("operator in linear"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Expr expr = e.get();
			if(expr.typeName().equals("sum")){
				Expr depletedExpr = e.copy();
				depletedExpr.set(0, Num.ZERO);
				
				Func outSum = sum();
				
				for(int i = 0;i<expr.size();i++){
					Expr current = depletedExpr.copy();
					current.set(0,expr.get(i));
					outSum.add(current);
				}
				return outSum.simplify(casInfo);
			}
			return e;
		}
	};
	
	public static Rule pullOutConstants = new Rule("pull out constants"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Expr expr = e.get();
			Var v = e.getVar();
			
			Func resSequence = seperateByVar(expr,v);
			if(!resSequence.get(0).equals(Num.ONE)) {
				e.set(0, resSequence.get(1));
				return prod(resSequence.get(0),e).simplify(casInfo);
			}
			
			return e;
		}
	};
	
	public static Rule becomeInner = new Rule("become the argument"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			return e.get();
		}
	};
	
	public static Rule distrInner = new Rule("distribute inner"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			e.set(0, distr(e.get()).simplify(casInfo));
			return e;
		}
		
	};
	
	public static Rule factorInner = new Rule("factor inner"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			e.set(0, factor(e.get()).simplify(casInfo));
			return e;
		}
		
	};
	
	public static Rule showState = new Rule("print state"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			System.out.println(e);
			return e;
		}
	};
	
	public static Rule showMessage(String text) {
		return new Rule("show message") {
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				System.out.println(text);
				return e;
			}
		};
	}

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
