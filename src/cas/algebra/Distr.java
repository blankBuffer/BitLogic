package cas.algebra;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.base.StandardRules;
import cas.primitive.Num;
import cas.primitive.Prod;

public class Distr{
	
	public static Func.FuncLoader distrLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			
			Rule generalDistr = new Rule("general distribution"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {//2*(x+y) -> 2*x+2*y
					Func distr = (Func)e;
					
					
					Expr expr = distr.get().copy();
					if(expr.typeName().equals("prod")) {
						
						Expr theSum = null;
						Func prod = null;
						for(int i = 0;i<expr.size();i++) {
							if(expr.get(i).typeName().equals("sum")) {
								theSum = expr.get(i).copy();
								prod = (Func)expr.copy();
								prod.remove(i);
								break;
							}else if(expr.get(i).typeName().equals("power")) {
								Func innerPow = (Func)expr.get(i);
								if(innerPow.getExpo().equals(Num.TWO) && innerPow.getBase().typeName().equals("sum") && innerPow.getBase().size() == 2) {
									Func baseSum = (Func)innerPow.getBase();
									theSum = sum( power(baseSum.get(0),num(2)) , prod(num(2),baseSum.get(0),baseSum.get(1)) , power(baseSum.get(1),num(2)) );
									prod = (Func)expr.copy();
									prod.remove(i);
									break;
								}
							}
						}
						if(theSum != null) {
							for(int i = 0;i<theSum.size();i++) {
								theSum.set(i, distr(Prod.combine(prod,theSum.get(i))));
							}
							return theSum.simplify(casInfo);
						}
					}else if(expr.typeName().equals("div")) {//(x+y)/3 -> x/3+y/3
						Func casted = (Func)expr;
						casted.setNumer(distr(casted.getNumer()).simplify(casInfo));
						if(casted.getNumer().typeName().equals("sum")) {
							for (int i = 0;i < casted.getNumer().size();i++) {
								casted.getNumer().set(i, div(casted.getNumer().get(i),casted.getDenom().copy()));
							}
							return casted.getNumer().simplify(casInfo);
							
						}
						
					}else if(expr.typeName().equals("sum")) {
						
						for(int i = 0;i<expr.size();i++) {
							expr.set(i, distr(expr.get(i)));
						}
						
						
					}else if(expr.typeName().equals("power")) {
						Func innerPow = (Func)expr;
						if(innerPow.getExpo().equals(Num.TWO) && innerPow.getBase().typeName().equals("sum") && innerPow.getBase().size() == 2) {
							Func baseSum = (Func)innerPow.getBase();
							expr = sum( power(baseSum.get(0),num(2)) , prod(num(2),baseSum.get(0),baseSum.get(1)) , power(baseSum.get(1),num(2)) );
						}
					}
					
					expr = expr.simplify(casInfo);
					distr.set(0, expr);
					return distr;
				}
			};
			
			owner.behavior.simplifyChildren = false;
			
			owner.behavior.rule = new Rule(new Rule[]{
					generalDistr,
					StandardRules.becomeInner
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return owner.get().convertToFloat(varDefs);
				}
			};
		}
	};
	
	
	public static Func.FuncLoader expandLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			
			Rule expandRule = new Rule("full expand"){

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func expand = (Func)e;
					if(expand.get().typeName().equals("prod")){
						for(int i = 0;i<expand.get().size();i++){
							if(expand.get().get(i).typeName().equals("power")){
								Func castedPow = (Func)expand.get().get(i);
								if( isPositiveRealNum(castedPow.getExpo()) && castedPow.getBase().typeName().equals("sum")){
									expand.get().set(i, multinomial(castedPow.getBase(),(Num)castedPow.getExpo(),casInfo));
								}
							}
						}
						
						Expr result = distr(expand.get()).simplify(casInfo);
						return result;
						
					}else if(expand.get().typeName().equals("sum")){
						Expr sum = sum();
						
						for(int i = 0;i<expand.get().size();i++){
							sum.add( expand( expand.get().get(i)).simplify(casInfo) );
						}
						sum = sum.simplify(casInfo);
						return sum;
					}else if(expand.get().typeName().equals("power")){
						Func casted = (Func)expand.get();
						casted.setBase(expand(casted.getBase()).simplify(casInfo));
						if( isPositiveRealNum(casted.getExpo()) && casted.getBase().typeName().equals("sum")){
							Expr result = multinomial(casted.getBase(),(Num)casted.getExpo(),casInfo);
							return result.simplify(casInfo);
						}
					}else if(expand.get().typeName().equals("div")) {
						Func innerDiv = (Func)expand.get();
						innerDiv.setNumer(expand(innerDiv.getNumer()).simplify(casInfo));
						return distr(innerDiv).simplify(casInfo);
					}
					
					return expand.get().simplify(casInfo);
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[] {
					expandRule,
			},"main sequence");
		}
	};

}
