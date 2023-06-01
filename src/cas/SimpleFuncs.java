package cas;

import java.math.BigInteger;
import java.util.ArrayList;

import cas.lang.*;
import cas.primitive.*;
import cas.algebra.Solve;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.FunctionsLoader;
import cas.base.Rule;
import cas.base.StandardRules;
import cas.bool.*;

public class SimpleFuncs extends Cas{
	
	public static Func.FuncLoader treeLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("show the tree of the expression"){
				

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					return var(f.get().toStringTree(0));
				}
			};
			owner.behavior.simplifyChildren = false;
		}
	};
	
	public static Func.FuncLoader sizeLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("get size of sub expression"){
				

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					
					return num(f.get().size());
				}
			};
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return new ComplexFloat(owner.get().size(),0);
				}
			};
			
			owner.behavior.simplifyChildren = false;
		}
	};
	
	public static Func.FuncLoader getLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("get sub expression"){
				

					@Override
					public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
						Func f = (Func)e;
						int index = ((Num)f.get(1)).getRealValue().intValue();
						return f.get().get( index );
					}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					int index = ((Num)owner.get(1)).getRealValue().intValue();
					return owner.get().get(index).convertToFloat(varDefs);
				}
			};
		}
	};
	
	public static Func.FuncLoader chooseLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("choose formula"){
				

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					
					Expr n = f.get(0);
					Expr k = f.get(1);
					
					if(isPositiveRealNum(n) && isPositiveRealNum(k)){
						return num(choose( ((Num)n).getRealValue() , ((Num)k).getRealValue()));
					}
					return e;
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					double n = owner.get(0).convertToFloat(varDefs).real;
					double k = owner.get(1).convertToFloat(varDefs).real;
					return new ComplexFloat( factorial(n)/(factorial(k)*factorial(n-k)) ,0);
				}
			};
		}
	};
	
	public static Func.FuncLoader primeFactorLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("prime factor an integer"){
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					return primeFactor((Num)f.get());
				}
			};
		}
	};
	
	public static Func.FuncLoader partialFracLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("break apart polynomial ratio into a sum of inverse linear terms"){
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					Expr inner = f.get(0);
					Var var = (Var)f.get(1);
					return partialFrac(inner,var,casInfo);
				}
			};
		}
	};
	
	public static Func.FuncLoader polyDivLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("polynomial division") {
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					Var v = (Var)f.get(1);
					return polyDiv(f.get(), v, casInfo);
				}
			};
		}
	};
	
	public static Func.FuncLoader polyCoefLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("get the coefficients of a polynomial as a list"){
				

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					Expr inner = f.get(0);
					Var var = (Var)f.get(1);
					Expr ans = polyExtract(inner,var,casInfo);
					if(ans == null) return error();
					return ans;
				}
			};
		}
	};
	
	public static Func.FuncLoader degreeLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("get the degree of a polynomial"){
				

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					Expr inner = f.get(0);
					Var var = (Var)f.get(1);
					Expr ans = num(degree(inner,var));
					if(ans.equals(Num.NEG_ONE)) return error();
					return ans;
				}
			};
		}
	};
	
	public static Func.FuncLoader leadingCoefLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("get the leading coefficient of a polynomial"){
				

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					Expr inner = f.get(0);
					Var var = (Var)f.get(1);
					Expr ans = getLeadingCoef(inner,var,casInfo);
					if(ans == null) return error();
					return ans;
				}
			};
		}
	};
	
	public static Func.FuncLoader convLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("unit conversion") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func f = (Func)e;
					
					try {
						return approx(Unit.conv(f.get(0), Unit.getUnit(f.get(1).toString()), Unit.getUnit(f.get(2).toString())),exprSet()).simplify(casInfo);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					return var("error");
				}
			};
		}
	};
	
	public static Func.FuncLoader latexLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("LaTeX language conversion") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func f = (Func)e;
					
					return var(generateLatex(f.get()));
				}
			};
		}
	};
	
	public static Func.FuncLoader substLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("substitution") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func f = (Func)e;
					if(f.get(1).isType("set")) {
						return f.get().replace((Func)f.get(1)).simplify(casInfo);
					}
					return f.get().replace((Func)f.get(1)).simplify(casInfo);
				}
			};
		}
	};
	
	
	public static Func.FuncLoader comparisonLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("comparison") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func f = (Func)e;
					
					if(f.get().isType("equ")) {
						Func castedEqu = (Func)f.get();
						
						return bool(Equ.getLeftSide(castedEqu).exactlyEquals(Equ.getRightSide(castedEqu)));
					}else if(f.get().isType("less")) {
						Func castedLess = (Func)f.get();
						
						boolean equal = Less.getLeftSide(castedLess).exactlyEquals(Less.getRightSide(castedLess));
						if(castedLess.containsVars()) return bool(!equal);
						
						return bool(!equal && Less.getLeftSide(castedLess).convertToFloat(exprSet()).real < Less.getRightSide(castedLess).convertToFloat(exprSet()).real );
					}else if(f.get().isType("greater")) {
						Func castedGreater = (Func)f.get();
						
						boolean equal = Greater.getLeftSide(castedGreater).exactlyEquals(Greater.getRightSide(castedGreater));
						if(castedGreater.containsVars()) return bool(!equal);
						
						return bool(!equal && Greater.getLeftSide(castedGreater).convertToFloat(exprSet()).real > Greater.getRightSide(castedGreater).convertToFloat(exprSet()).real );
					}else if(f.get() instanceof BoolState) {
						return f.get();
					}
					
					return f;
				}
			};
		}
	};
	
	public static Func.FuncLoader taylorLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("taylor series"){
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func f = (Func)e;
					
					Expr expr = f.get(0);
					
					Func equ = (Func)f.get(1);
					Var v = (Var)Equ.getLeftSide(equ);
					Num n = (Num)f.get(2);
					
					
					Func outSum = sum();
					
					for(int i = 0;i<n.getRealValue().intValue();i++) {
						
						outSum.add( div(prod(expr.replace(equ),power(sub(v,Equ.getRightSide(equ)),num(i))),num(factorial(BigInteger.valueOf(i)))));
						
						expr = diff(expr,v).simplify(casInfo);
					}
					
					return outSum.simplify(casInfo);
				}
			};
		}
	};
	
	public static Func.FuncLoader guiLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("opens a new window") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					new ui.CalcWindow();
					
					return var("done");
				}
			};
		}
	};
	
	public static Func.FuncLoader nParamsLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("expected number of paramters") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func f = (Func)e;
					
					return num(FunctionsLoader.getExpectedParams(f.get().toString()));
				}
			};
		}
	};
	
	public static Func.FuncLoader isTypeLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("check type") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func f = (Func)e;
					
					return bool(f.get(0).isType(f.get(1).toString()));
				}
			};
		}
	};
	
	public static Func.FuncLoader containsLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("check if first argument contains the second argument"){
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func f = (Func)e;
					
					return bool(f.get(0).contains(f.get(1)));
				}
			};
		}
	};
	
	public static Func.FuncLoader resultLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = StandardRules.becomeInner;
		}
	};
	
	public static Func.FuncLoader allowAbsLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("are we allowing absolute values") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return bool(casInfo.allowAbs());
				}
			};
		}
	};
	
	public static Func.FuncLoader allowComplexNumbersLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("are we allowing absolute values") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return bool(casInfo.allowComplexNumbers());
				}
			};
		}
	};
	
	public static Func.FuncLoader singleSolutionModeLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("should solve only return one solution") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return bool(casInfo.singleSolutionMode());
				}
			};
		}
	};
	
	public static Func.FuncLoader factorIrrationalRootsLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("allow irrational roots in factoring") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return bool(casInfo.factorIrrationalRoots());
				}
			};
		}
	};
	
	public static Func.FuncLoader setAllowAbsLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("are we allowing absolute values") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					casInfo.setAllowAbs(e.get().equals(BoolState.TRUE));
					return bool(casInfo.allowAbs());
				}
			};
		}
	};
	public static Func.FuncLoader setAllowComplexNumbersLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("are we allowing absolute values") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					casInfo.setAllowComplexNumbers( e.get().equals(BoolState.TRUE));
					return bool(casInfo.allowComplexNumbers());
				}
			};
		}
	};
	public static Func.FuncLoader setSingleSolutionModeLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("set if solve should return one solution") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					casInfo.setSingleSolutionMode( e.get().equals(BoolState.TRUE));
					return bool(casInfo.singleSolutionMode());
				}
			};
		}
	};
	public static Func.FuncLoader setFactorIrrationalRootsLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("should solve only return one solution") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					casInfo.setFactorIrrationalRoots( e.get().equals(BoolState.TRUE));
					return bool(casInfo.factorIrrationalRoots());
				}
			};
		}
	};
	public static Func.FuncLoader relaxedPowerLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("simplify (a^b)^c to a^(b*c) always") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return bool(casInfo.relaxedPower());
				}
			};
		}
	};
	public static Func.FuncLoader setRelaxedPowerLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("simplify (a^b)^c to a^(b*c) always") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					casInfo.setRelaxedPower(e.get().equals(BoolState.TRUE));
					return bool(casInfo.relaxedPower());
				}
			};
		}
	};
	public static Func.FuncLoader sinhLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("sinh function") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return div(sub(exp(prod(num(2),e.get())),num(1)),prod(num(2),exp(e.get()))).simplify(casInfo);
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				

				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.mult(ComplexFloat.sub(ComplexFloat.exp(owner.get().convertToFloat(varDefs)),ComplexFloat.exp(ComplexFloat.neg(owner.get().convertToFloat(varDefs)))),new ComplexFloat(0.5,0));
				}
			};
		}
	};
	public static Func.FuncLoader coshLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("cosh function") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return div(sum(exp(prod(num(2),e.get())),num(1)),prod(num(2),exp(e.get()))).simplify(casInfo);
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				

				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.mult(ComplexFloat.add(ComplexFloat.exp(owner.get().convertToFloat(varDefs)),ComplexFloat.exp(ComplexFloat.neg(owner.get().convertToFloat(varDefs)))),new ComplexFloat(0.5,0));
				}
			};
		}
	};
	public static Func.FuncLoader tanhLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("tanh function") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return div( sub(exp(prod(num(2),e.get())),num(1)) , sum(exp(prod(num(2),e.get())),num(1)) ).simplify(casInfo);
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				

				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					ComplexFloat expSquared = ComplexFloat.exp( ComplexFloat.mult(ComplexFloat.TWO, owner.get().convertToFloat(varDefs) ) );
					return ComplexFloat.div( ComplexFloat.sub(expSquared, ComplexFloat.ONE) ,  ComplexFloat.add(expSquared, ComplexFloat.ONE) );
				}
			};
		}
	};
	public static Func.FuncLoader secLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("replace sec with one over cos"){
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return inv(cos(e.get())).simplify(casInfo);
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				

				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.cos(owner.get().convertToFloat(varDefs)));
				}
			};
		}
	};
	
	public static Func.FuncLoader cscLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("replace csc with one over sin"){
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return inv(sin(e.get())).simplify(casInfo);
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				

				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.sin(owner.get().convertToFloat(varDefs)));
				}
			};
		}
	};
	public static Func.FuncLoader cotLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("replace cot with one over tan"){
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return inv(tan(e.get())).simplify(casInfo);
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				

				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.tan(owner.get().convertToFloat(varDefs)));
				}
			};
		}
	};
	public static Func.FuncLoader extSeqLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("extend the sequence"){
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					
					int needed = ((Num)e.get(1)).getRealValue().intValue()-e.get().size();
					
					Expr extended = next( (Func)e.get() , num(needed) ).simplify(casInfo);
					
					if(extended.isType("next")) {
						for(int i = 0;i<needed;i++) {
							e.get().add( e.get(0).get(i%e.get(0).size()) );
						}
					}else {
						for(int i = 0;i<needed;i++) {
							e.get().add(extended.get(i));
						}
					}
					
					return e.get();
				}
			};
		}
	};
	public static Func.FuncLoader truncSeqLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("truncate the sequence") {
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					int newSize = ((Num)e.get(1)).getRealValue().intValue();
					
					Func truncatedSequence = sequence();
					for(int i = 0;i<newSize;i++) {
						truncatedSequence.add(e.get(0).get(i));
					}
					return truncatedSequence;
				}
			};
		}
	};
	public static Func.FuncLoader subSeqLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("get the sub sequence from start to end, end is non inclusive") {
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					int start = ((Num)e.get(1)).getRealValue().intValue();
					int end = ((Num)e.get(2)).getRealValue().intValue();
					
					Func truncatedSequence = sequence();
					for(int i = start;i<end;i++) {
						truncatedSequence.add(e.get(0).get(i));
					}
					return truncatedSequence;
				}
			};
		}
	};
	public static Func.FuncLoader revSeqLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("revserse the sequence") {
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func oldSequence = (Func)e.get(0);
					Func newSequence = sequence();
					
					for(int i = oldSequence.size()-1;i>=0;i--) {
						newSequence.add(oldSequence.get(i));
					}
					
					return newSequence;
				}
			};
		}
	};
	public static Func.FuncLoader sumSeqLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("add elements of sequence"){
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func sequence = (Func)e.get();
					Func sum = sum();
					for(int i = 0;i<sequence.size();i++) {
						sum.add(sequence.get(i));
					}
					return sum.simplify(casInfo);
				}
			};
		}
	};
	public static Func.FuncLoader arcLenLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("arc-length of a function"){
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Expr min = e.get(0),max = e.get(1);
					Expr expr = e.get(2);
					Var v = (Var) e.get(3);
					
					return integrateOver(min,max,sqrt(sum(num(1),power(diff(expr,v),num(2)))),v).simplify(casInfo);
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				

				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					Expr min = owner.get(0),max = owner.get(1);
					Expr expr = owner.get(2);
					Var v = (Var) owner.get(3);
					
					return integrateOver(min,max,sqrt(sum(num(1),power(diff(expr,v),num(2)))),v).convertToFloat(varDefs);
				}
			};
		}
	};
	public static Func.FuncLoader repDiffLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("repeated derivative"){
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					int amount = ((Num)e.get(2)).getRealValue().intValue();
					
					Expr expr = e.get(0);
					Var v = (Var)e.get(1);
					
					for(int i = 0;i<amount;i++) {
						expr = distr(diff(expr,v)).simplify(casInfo);
					}
					
					return expr;
				}
			};
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				
				//this is not based on any mathematical rigor, but it seems to somewhat work
				//time complexity of 2^n so keep n small
				ComplexFloat calcDerivRec(Expr expr,Func varDefs,ComplexFloat var,int n,ComplexFloat delta) {
					if(n > 8) return new ComplexFloat(0,0);//does not work well beyond this point
					if(n == 0) return expr.convertToFloat(varDefs);
					
					ComplexFloat deltaOver2 = new ComplexFloat(delta.real/2,0);
					
					if(n==1) {
						
						var.set( ComplexFloat.sub(var, deltaOver2) );//subtract delta/2
						ComplexFloat y0 = expr.convertToFloat(varDefs);
						var.set( ComplexFloat.add(var, delta) );//add delta
						ComplexFloat y1 = expr.convertToFloat(varDefs);
						
						ComplexFloat slope = ComplexFloat.div((ComplexFloat.sub(y1, y0)),delta);
						return slope;
					}
					
					ComplexFloat originalVal = new ComplexFloat(var);
					ComplexFloat newDelta = new ComplexFloat(delta.real/2.0,0);//increase precision on lower order derivatives
					
					var.set( ComplexFloat.sub(var, deltaOver2) );
					ComplexFloat y0Der = calcDerivRec(expr,varDefs,var,n-1,newDelta);
					var.set( ComplexFloat.add(originalVal, deltaOver2 ) );//add delta/2 to the original x
					ComplexFloat y1Der = calcDerivRec(expr,varDefs,var,n-1,newDelta);
					
					ComplexFloat slope = ComplexFloat.div((ComplexFloat.sub(y1Der, y0Der)),delta);
					return slope;
					
				}

				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					int amount = ((Num)owner.get(2)).getRealValue().intValue();
					Var var = (Var)owner.get(1);
					ComplexFloat equRightSideVal = null;
					Expr expr = owner.get(0);
					
					for(int i = 0;i < varDefs.size();i++) {//search for definition
						Func equ = (Func)varDefs.get(i);
						Var v = (Var)Equ.getLeftSide(equ);
						if(v.equals(var)) {
							equRightSideVal = ((FloatExpr)Equ.getRightSide(equ)).value;//found!
							break;
						}
					}
					
					if(equRightSideVal == null) return new ComplexFloat(0,0);
					
					ComplexFloat delta = new ComplexFloat( Math.pow( Math.abs(equRightSideVal.real)/Short.MAX_VALUE,1.0/amount ) ,0);//set original precision
					return calcDerivRec(expr,varDefs,equRightSideVal,amount,delta);
				}
			};
		}
	};
	public static Func.FuncLoader similarLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("expressions are similar"){
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return bool(Rule.strictSimilarExpr(e.get(0), e.get(1)));
				}
			};
		}
	};
	
	public static Func.FuncLoader fastSimilarLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("expressions are similar computed quickly"){
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					return bool(Rule.fastSimilarExpr(e.get(0), e.get(1)));
				}
			};
		}
	};
	public static Func.FuncLoader sortExprLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("sort expression into cononical arangement") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					e.get().sort();
					System.out.println(e.get().flags.sorted);
					return e.get();
				}
			};
		}
	};
	
	public static Func.FuncLoader deleteVarLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("delete variable or function") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Expr inner = e.get();
					casInfo.definitions.removeVar(inner.toString());
					return Var.SUCCESS;
				}
			};
		}
	};
	
public static Func.FuncLoader deleteFuncLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("delete variable or function") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Expr inner = e.get();
					casInfo.definitions.removeFunc(inner.toString());
					return Var.SUCCESS;
				}
			};
		}
	};
	
	public static Func.FuncLoader helpLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("help function") {
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					String toSearch = e.get().toString();
					Func function = FunctionsLoader.funcs.getOrDefault(toSearch, null);
					String message = function != null ? function.help() : "function not found!";
					
					String functionName = "Function Name: "+function.behavior.name;
					String nParamsLine = "Number of parameters: "+( function.behavior.numOfParams == FunctionsLoader.N_PARAMETERS ? "unlimited" : function.behavior.numOfParams );
					
					return var(functionName+"\n"+nParamsLine+"\n"+message);
				}
			};
		}
	};
	public static Func.FuncLoader fSolveLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule("floating point solver for polynomials") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func polySequence = polyExtract(e.get(0), (Var)e.get(1) ,casInfo);
					Func solutionsSet = exprSet();
					
					if(polySequence!=null) {
						ArrayList<Double> solutionsArrayList = Solve.polySolve(polySequence);
						for(double solution:solutionsArrayList) {
							solutionsSet.add(floatExpr(solution));
						}
					}
					
					return solutionsSet;
				}
			};
		}
	};
	public static Func.FuncLoader fastEqualsLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.simplifyChildren = false;
			owner.behavior.rule = new Rule("fast comparison") {
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					
					return bool(e.get(0).equals(e.get(1)));
				}
			};
		}
	};
}
