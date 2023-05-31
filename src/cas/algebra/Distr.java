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
	
	/*
	 * distribute function aka FOIL
	 * simple examples
	 * 
	 * 2*(x+y) -> 2*x+2*y
	 * (x+y)/3 -> x/3+y/3
	 */
	
	public static Func.FuncLoader distrLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			
			Rule generalDistr = new Rule("general distribution"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {//2*(x+y) -> 2*x+2*y
					Func distr = (Func)e;//rename for simplicity and cast to Func
					
					
					//get a copy of the parameter of distr, set value to what output should be
					Expr expr = distr.get().copy();
					
					if(expr.isType("prod")) {//if given a product
						
						/*
						 * we search for a sum in the product
						 * then we multiply each sum element by what is outside the 
						 * sum
						 * 
						 * k*(a+b) -> k*a+k*b
						 */
						
						Expr theSum = null;//sum which we search for
						Func prod = null;//prod is the stuff outside not including the sum
						for(int i = 0;i<expr.size();i++) {
							if(expr.get(i).isType("sum")) {
								theSum = expr.get(i).copy();
								prod = (Func)expr.copy();
								prod.remove(i);
								break;
							}
							/*
							 * I also expand simple powers with exponent 2 with only two elements in the base
							 * ,basically if its in this form (a+b)^2
							 */
							else if(expr.get(i).isType("power")) {
								Func innerPow = (Func)expr.get(i);
								if(innerPow.getExpo().equals(Num.TWO) && innerPow.getBase().isType("sum") && innerPow.getBase().size() == 2) {
									Func baseSum = (Func)innerPow.getBase();
									//the following line is just (a+b)^2 = a^2+2*a*b+b^2
									theSum = sum( power(baseSum.get(0),num(2)) , prod(num(2),baseSum.get(0),baseSum.get(1)) , power(baseSum.get(1),num(2)) );
									prod = (Func)expr.copy();
									prod.remove(i);
									break;
								}
							}
						}
						if(theSum != null) {//if a sum is found then we distribute
							for(int i = 0;i<theSum.size();i++) {
								theSum.set(i, distr(Prod.combine(prod,theSum.get(i))));//multiply each term
							}
							expr = theSum.simplify(casInfo);//simplify and set expr
						}
					}
					/*
					 * the next case is basically expanding fractions.
					 * it is the same thing as expanding products except we check if
					 *  the numerator is a sum or simple power (a+b)^2
					 */
					else if(expr.isType("div")) {//if given a fraction (x+y)/3 -> x/3+y/3
						Func casted = (Func)expr;
						casted.setNumer(distr(casted.getNumer()).simplify(casInfo));//distribute the numerator
						if(casted.getNumer().isType("sum")) {//Separate fraction into sum
							for (int i = 0;i < casted.getNumer().size();i++) {
								casted.getNumer().set(i, div(casted.getNumer().get(i),casted.getDenom().copy()));
							}
							expr = casted.getNumer().simplify(casInfo);
							
						}
						
					}
					/*
					 * distribute each thing in the sum
					 * for example
					 * if the user prompts distr((a+b)^2+d) it would become a^2+2*a*b+b^2+d
					 */
					else if(expr.isType("sum")) {
						
						for(int i = 0;i<expr.size();i++) {
							expr.set(i, distr(expr.get(i)));
						}
						
					}
					/*
					 * I also expand simple powers with exponent 2 with only two elements in the base
					 * ,basically if its in this form (a+b)^2
					 * 
					 * example
					 * 
					 * distr((a+b)^2) -> a^2+2*a*b+b^2
					 * 
					 */
					else if(expr.isType("power")) {
						Func innerPow = (Func)expr;
						if(innerPow.getExpo().equals(Num.TWO) && innerPow.getBase().isType("sum") && innerPow.getBase().size() == 2) {
							Func baseSum = (Func)innerPow.getBase();
							//the following line is just (a+b)^2 = a^2+2*a*b+b^2
							expr = sum( power(baseSum.get(0),num(2)) , prod(num(2),baseSum.get(0),baseSum.get(1)) , power(baseSum.get(1),num(2)) );
						}
					}
					
					//we update the inside with the new form
					expr = expr.simplify(casInfo);
					distr.set(0, expr);
					
					/*
					 * the reason for not simply returning expr is because 
					 * there are possibly more rules to be run. The next rules will only
					 * run if it maintains the form
					 * 
					 * for example becomeInner will be called next
					 */

					return distr;
				}
			};
			
			owner.behavior.simplifyChildren = false;
			
			owner.behavior.rule = new Rule(new Rule[]{
					generalDistr,
					StandardRules.becomeInner//keep last
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return owner.get().convertToFloat(varDefs);
				}
			};
		}
	};
	
	/*
	 * expand is distr but does more
	 * it expands expression to the biggest possible form
	 * things like (a+b+c+...)^n is fully foiled out using fast expansion algorithm
	 */
	
	public static Func.FuncLoader expandLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			
			Rule expandRule = new Rule("full expand"){
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func expand = (Func)e;
					/*
					 * does similar thing as in distr except when
					 * it hits a power it does full expand
					 */
					
					//get a copy of the parameter of distr, set value to what output should be
					Expr expr = expand.get().copy();
					
					if(expr.isType("prod")){
						for(int i = 0;i<expr.size();i++){
							if(expr.get(i).isType("power")){
								Func castedPow = (Func)expr.get(i);
								if( isPositiveRealNum(castedPow.getExpo()) && castedPow.getBase().isType("sum")){
									expr.set(i, multinomial(castedPow.getBase(),(Num)castedPow.getExpo(),casInfo));
								}
							}
						}
						
						expr = distr(expr);
						
					}
					/*
					 * expand every element in the sum
					 */
					else if(expr.isType("sum")){
						Expr sum = sum();
						
						for(int i = 0;i<expr.size();i++){
							sum.add( expand( expr.get(i)).simplify(casInfo) );
						}
						expr = sum;
					}
					/*
					 * use multinomial expansion algorithm. Located in cas.Cas
					 */
					else if(expr.isType("power")){
						Func castedPow = (Func)expr;
						castedPow.setBase(expand(castedPow.getBase()).simplify(casInfo));
						if( isPositiveRealNum(castedPow.getExpo()) && castedPow.getBase().isType("sum")){
							expr = multinomial(castedPow.getBase(),(Num)castedPow.getExpo(),casInfo);
						}
						
					}else if(expr.isType("div")) {//expand only numerator of fraction
						Func innerDiv = (Func)expr;
						innerDiv.setNumer(expand(innerDiv.getNumer()).simplify(casInfo));
						expr = distr(innerDiv);
					}
					
					expr = expr.simplify(casInfo);
					
					expand.set(0, expr);
					
					return expand;
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[] {
					expandRule,
					StandardRules.becomeInner//keep last
			},"main sequence");
		}
	};

}
