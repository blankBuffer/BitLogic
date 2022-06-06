package cas;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import cas.lang.*;
import cas.primitive.*;
import cas.special.*;
import cas.bool.*;

public class SimpleFuncs extends QuickMath{

	private static HashMap<String,Func> simpleFuncs = new HashMap<String,Func>();
	static boolean FUNCTION_UNLOCKED = false;//ability to create new functions on the fly, must be turned off during loading of CAS to prevent spelling errors
	
	public static Rule fullExpand = new Rule("full expand"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			if(e instanceof Prod){
				for(int i = 0;i<e.size();i++){
					if(e.get(i) instanceof Power){
						Power casted = (Power)e.get(i);
						if( isPositiveRealNum(casted.getExpo()) && casted.getBase() instanceof Sum){
							e.set(i, multinomial(casted.getBase(),(Num)casted.getExpo(),casInfo));
						}
					}
				}
				
				Expr result = distr(e).simplify(casInfo);
				return result;
				
			}else if(e instanceof Sum){
				Expr sum = new Sum();
				
				for(int i = 0;i<e.size();i++){
					sum.add( fullExpand.applyRuleToExpr(e.get(i), casInfo) );
				}
				sum = sum.simplify(casInfo);
				return sum;
			}else if(e instanceof Power){
				Power casted = (Power)e;
				casted.setBase(fullExpand.applyRuleToExpr(casted.getBase(), casInfo));
				if( isPositiveRealNum(casted.getExpo()) && casted.getBase() instanceof Sum){
					Expr result = multinomial(casted.getBase(),(Num)casted.getExpo(),casInfo);
					return result.simplify(casInfo);
				}
			}else if(e instanceof Div) {
				Div innerDiv = (Div)e;
				innerDiv.setNumer(fullExpand.applyRuleToExpr(innerDiv.getNumer(), casInfo));
				return distr(innerDiv).simplify(casInfo);
			}
			
			return e.simplify(casInfo);
		}
	};
	
	static Func tree = new Func("tree",1);
	static Func size = new Func("size",1);
	static Func get = new Func("get",2);
	static Func choose = new Func("choose",2);
	static Func primeFactor = new Func("primeFactor",1);
	
	static Func partialFrac = new Func("partialFrac",2);
	static Func polyDiv = new Func("polyDiv",2);
	
	static Func polyCoef = new Func("polyCoef",2);
	static Func degree = new Func("degree",2);
	static Func save = new Func("save",2);
	static Func open = new Func("open",1);
	static Func conv = new Func("conv",3);
	static Func latex = new Func("latex",1);
	static Func subst = new Func("subst",2);
	static Func eval = new Func("eval",1);
	static Func expand = new Func("expand",1);
	static Func taylor = new Func("taylor",3);
	static Func gui = new Func("gui",0);
	static Func nParams = new Func("nParams",1);
	static Func isType = new Func("isType",2);
	static Func contains = new Func("contains",2);
	static Func result = new Func("result",1);
	static Func allowAbs = new Func("allowAbs",0);
	static Func allowComplexNumbers = new Func("allowComplexNumbers",0);
	static Func setAllowAbs = new Func("setAllowAbs",1);
	static Func setAllowComplexNumbers = new Func("setAllowComplexNumbers",1);
	
	static Func sinh = new Func("sinh",1);
	static Func cosh = new Func("cosh",1);
	static Func tanh = new Func("tanh",1);
	
	static Func sec = new Func("sec",1);
	static Func csc = new Func("csc",1);
	static Func cot = new Func("cot",1);
	
	static Func extSeq = new Func("extSeq",2);
	static Func truncSeq = new Func("truncSeq",2);
	static Func subSeq = new Func("subSeq",3);
	static Func revSeq = new Func("revSeq",1);
	static Func sumSeq = new Func("sumSeq",1);
	
	static Func arcLen = new Func("arcLen",4);
	static Func repDiff = new Func("repDiff",3);
	
	static Func similar = new Func("similar",2);
	static Func fastSimilar = new Func("fastSimilar",2);
	
	static Func sortExpr = new Func("sortExpr",1);
	
	static Func delete = new Func("delete",1);
	static Func help = new Func("help",1);
	
	static Func fSolve = new Func("fSolve",2);
	
	public static void loadRules(){
		tree.simplifyChildren = false;
		tree.ruleSequence.add(new Rule("show the tree of the expression"){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				return var(f.get().toStringTree(0));
			}
		});
		
		size.simplifyChildren = false;
		size.ruleSequence.add(new Rule("get size of sub expression"){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				
				return num(f.get().size());
			}
		});
		size.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				return new ComplexFloat(owner.get().size(),0);
			}
		};
		
		get.simplifyChildren = false;
		get.ruleSequence.add(new Rule("get sub expression"){
			private static final long serialVersionUID = 1L;

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					int index = ((Num)f.get(1)).realValue.intValue();
					return f.get().get( index );
				}
		});
		get.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				int index = ((Num)owner.get(1)).realValue.intValue();
				return owner.get().get(index).convertToFloat(varDefs);
			}
		};
		
		choose.ruleSequence.add(new Rule("choose formula"){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				
				Expr n = f.get(0);
				Expr k = f.get(1);
				
				if(isPositiveRealNum(n) && isPositiveRealNum(k)){
					return num(choose( ((Num)n).realValue , ((Num)k).realValue));
				}
				return e;
			}
		});
		choose.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				double n = owner.get(0).convertToFloat(varDefs).real;
				double k = owner.get(1).convertToFloat(varDefs).real;
				return new ComplexFloat( factorial(n)/(factorial(k)*factorial(n-k)) ,0);
			}
		};
		
		primeFactor.ruleSequence.add(new Rule("prime factor an integer"){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				return primeFactor((Num)f.get());
			}
		});
		primeFactor.toFloatFunc = Func.nothingFunc;
		
		partialFrac.ruleSequence.add(new Rule("break apart polynomial ratio into a sum of inverse linear terms"){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				return partialFrac(inner,var,casInfo);
			}
		});
		partialFrac.toFloatFunc = Func.nothingFunc;
		
		polyDiv.ruleSequence.add(new Rule("polynomial division") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Var v = (Var)f.get(1);
				return polyDiv(f.get(), v, casInfo);
			}
		});
		polyDiv.toFloatFunc = Func.nothingFunc;
		
		polyCoef.ruleSequence.add(new Rule("get the coefficients of a polynomial as a list"){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				return polyExtract(inner,var,casInfo);
			}
		});
		
		degree.ruleSequence.add(new Rule("get the degree of a polynomial"){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				return num(degree(inner,var));
			}
		});
		
		save.simplifyChildren = false;
		save.ruleSequence.add(new Rule("saving expression") {
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				try {
					Expr.serializedSaveExpr(f.get(0), f.get(1).toString());
				} catch (IOException e1) {
					e1.printStackTrace();
					return var("error");
				}
				
				return var("done");
			}
		});
		
		open.simplifyChildren = false;
		open.ruleSequence.add(new Rule("opening expression") {
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				
				try {
					return Expr.serializedOpenExpr(f.get(0).toString());
				} catch (ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
				}
				
				return var("error");
			}
		});
		
		conv.simplifyChildren = false;
		conv.ruleSequence.add(new Rule("unit conversion") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				try {
					return approx(Unit.conv(f.get(0), Unit.getUnit(f.get(1).toString()), Unit.getUnit(f.get(2).toString())),exprList()).simplify(casInfo);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				return var("error");
			}
		});
		
		latex.simplifyChildren = false;
		latex.ruleSequence.add(new Rule("LaTeX language conversion") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				return var(generateLatex(f.get()));
			}
		});
		
		subst.ruleSequence.add(new Rule("substitution") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				if(f.get(1) instanceof ExprList) {
					return f.get().replace((ExprList)f.get(1)).simplify(casInfo);
				}
				return f.get().replace((Equ)f.get(1)).simplify(casInfo);
			}
		});
		
		eval.ruleSequence.add(new Rule("evaluate") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				if(f.get() instanceof Equ) {
					Equ casted = (Equ)f.get();
					
					return bool(casted.getLeftSide().exactlyEquals(casted.getRightSide()));
				}else if(f.get() instanceof Less) {
					Less casted = (Less)f.get();
					
					boolean equal = casted.getLeftSide().exactlyEquals(casted.getRightSide());
					if(casted.containsVars()) return bool(!equal);
					
					return bool(!equal && casted.getLeftSide().convertToFloat(exprList()).real < casted.getRightSide().convertToFloat(exprList()).real );
				}else if(f.get() instanceof Greater) {
					Greater casted = (Greater)f.get();
					
					boolean equal = casted.getLeftSide().exactlyEquals(casted.getRightSide());
					if(casted.containsVars()) return bool(!equal);
					
					return bool(!equal && casted.getLeftSide().convertToFloat(exprList()).real > casted.getRightSide().convertToFloat(exprList()).real );
				}else if(f.get() instanceof BoolState) {
					return f.get();
				}
				
				return f;
			}
		});
		
		expand.simplifyChildren = false;
		expand.ruleSequence.add(new Rule("expand") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				return fullExpand.applyRuleToExpr(f.get(), casInfo);
			}
		});
		expand.toFloatFunc = Func.nothingFunc;
		
		taylor.ruleSequence.add(new Rule("taylor series"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				Expr expr = f.get(0);
				
				Equ equ = (Equ)f.get(1);
				Var v = (Var)equ.getLeftSide();
				Num n = (Num)f.get(2);
				
				
				Sum out = new Sum();
				
				for(int i = 0;i<n.realValue.intValue();i++) {
					
					out.add( div(prod(expr.replace(equ),pow(sub(v,equ.getRightSide()),num(i))),num(factorial(BigInteger.valueOf(i)))));
					
					expr = diff(expr,v).simplify(casInfo);
				}
				
				return out.simplify(casInfo);
			}
		});
		
		gui.ruleSequence.add(new Rule("opens a new window") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				new ui.CalcWindow();
				
				return var("done");
			}
		});
		
		nParams.ruleSequence.add(new Rule("expected number of paramters") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				return num(getExpectectedParams(f.get().toString()));
			}
		});
		
		isType.simplifyChildren = false;
		isType.ruleSequence.add(new Rule("check type") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				return bool(f.get(0).typeName().equals(f.get(1).toString()));
			}
		});
		
		contains.simplifyChildren = false;
		contains.ruleSequence.add(new Rule("check if first argument contains the second argument"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				return bool(f.get(0).contains(f.get(1)));
			}
		});
		
		result.ruleSequence.add(StandardRules.becomeInner);
		
		allowAbs.ruleSequence.add(new Rule("are we allowing absolute values") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(casInfo.allowAbs());
			}
		});
		
		allowComplexNumbers.ruleSequence.add(new Rule("are we allowing absolute values") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(casInfo.allowComplexNumbers());
			}
		});
		
		setAllowAbs.ruleSequence.add(new Rule("are we allowing absolute values") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				casInfo.setAllowAbs(e.get().equals(BoolState.TRUE));
				return bool(casInfo.allowAbs());
			}
		});
		
		setAllowComplexNumbers.ruleSequence.add(new Rule("are we allowing absolute values") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				casInfo.setAllowComplexNumbers( e.get().equals(BoolState.TRUE));
				return bool(casInfo.allowComplexNumbers());
			}
		});
		
		sinh.ruleSequence.add(new Rule("sinh function") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return div(sub(exp(prod(num(2),e.get())),num(1)),prod(num(2),exp(e.get()))).simplify(casInfo);
			}
		});
		sinh.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;

			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				return ComplexFloat.mult(ComplexFloat.sub(ComplexFloat.exp(owner.get().convertToFloat(varDefs)),ComplexFloat.exp(ComplexFloat.neg(owner.get().convertToFloat(varDefs)))),new ComplexFloat(0.5,0));
			}
		};
		
		cosh.ruleSequence.add(new Rule("cosh function") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return div(sum(exp(prod(num(2),e.get())),num(1)),prod(num(2),exp(e.get()))).simplify(casInfo);
			}
		});
		cosh.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;

			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				return ComplexFloat.mult(ComplexFloat.add(ComplexFloat.exp(owner.get().convertToFloat(varDefs)),ComplexFloat.exp(ComplexFloat.neg(owner.get().convertToFloat(varDefs)))),new ComplexFloat(0.5,0));
			}
		};
		
		tanh.ruleSequence.add(new Rule("tanh function") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return div( sub(exp(prod(num(2),e.get())),num(1)) , sum(exp(prod(num(2),e.get())),num(1)) ).simplify(casInfo);
			}
		});
		tanh.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;

			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				ComplexFloat expSquared = ComplexFloat.exp( ComplexFloat.mult(ComplexFloat.TWO, owner.get().convertToFloat(varDefs) ) );
				return ComplexFloat.div( ComplexFloat.sub(expSquared, ComplexFloat.ONE) ,  ComplexFloat.add(expSquared, ComplexFloat.ONE) );
			}
		};
		
		sec.ruleSequence.add(new Rule("replace sec with one over cos"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return inv(cos(e.get())).simplify(casInfo);
			}
		});
		sec.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;

			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.cos(owner.get().convertToFloat(varDefs)));
			}
		};
		
		csc.ruleSequence.add(new Rule("replace csc with one over sin"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return inv(sin(e.get())).simplify(casInfo);
			}
		});
		csc.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;

			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.sin(owner.get().convertToFloat(varDefs)));
			}
		};
		
		cot.ruleSequence.add(new Rule("replace cot with one over tan"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return inv(tan(e.get())).simplify(casInfo);
			}
		});
		cot.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;

			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.tan(owner.get().convertToFloat(varDefs)));
			}
		};
		
		
		extSeq.ruleSequence.add(new Rule("extend the sequence"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				
				int needed = ((Num)e.get(1)).realValue.intValue()-e.get().size();
				
				Expr extended = next( (Sequence)e.get() , num(needed) ).simplify(casInfo);
				
				if(extended instanceof Next) {
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
		});
		truncSeq.ruleSequence.add(new Rule("truncate the sequence") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				int newSize = ((Num)e.get(1)).realValue.intValue();
				
				Sequence truncated = new Sequence();
				for(int i = 0;i<newSize;i++) {
					truncated.add(e.get(0).get(i));
				}
				return truncated;
			}
		});
		subSeq.ruleSequence.add(new Rule("get the sub sequence from start to end, end is non inclusive") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				int start = ((Num)e.get(1)).realValue.intValue();
				int end = ((Num)e.get(2)).realValue.intValue();
				
				Sequence truncated = new Sequence();
				for(int i = start;i<end;i++) {
					truncated.add(e.get(0).get(i));
				}
				return truncated;
			}
		});
		revSeq.ruleSequence.add(new Rule("revserse the sequence") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Sequence oldSeq = (Sequence)e.get(0);
				Sequence newSeq = new Sequence();
				
				for(int i = oldSeq.size()-1;i>=0;i--) {
					newSeq.add(oldSeq.get(i));
				}
				
				return newSeq;
			}
		});
		sumSeq.ruleSequence.add(new Rule("add elements of sequence"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Sequence seq = (Sequence)e.get();
				Sum sum = new Sum();
				for(int i = 0;i<seq.size();i++) {
					sum.add(seq.get(i));
				}
				return sum.simplify(casInfo);
			}
		});
		
		
		
		arcLen.ruleSequence.add(new Rule("arc-length of a function"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Expr min = e.get(0),max = e.get(1);
				Expr expr = e.get(2);
				Var v = (Var) e.get(3);
				
				return integrateOver(min,max,sqrt(sum(num(1),pow(diff(expr,v),num(2)))),v).simplify(casInfo);
			}
		});
		arcLen.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;

			@Override
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				Expr min = owner.get(0),max = owner.get(1);
				Expr expr = owner.get(2);
				Var v = (Var) owner.get(3);
				
				return integrateOver(min,max,sqrt(sum(num(1),pow(diff(expr,v),num(2)))),v).convertToFloat(varDefs);
			}
		};
		
		repDiff.ruleSequence.add(new Rule("repeated derivative"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				int amount = ((Num)e.get(2)).realValue.intValue();
				
				Expr expr = e.get(0);
				Var v = (Var)e.get(1);
				
				for(int i = 0;i<amount;i++) {
					expr = distr(diff(expr,v)).simplify(casInfo);
				}
				
				return expr;
			}
		});
		repDiff.toFloatFunc = new Func.FloatFunc() {
			private static final long serialVersionUID = 1L;
			
			//this is not based on any mathematical rigor, but it seems to somewhat work
			//time complexity of 2^n so keep n small
			ComplexFloat calcDerivRec(Expr expr,ExprList varDefs,ComplexFloat var,int n,ComplexFloat delta) {
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
			public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
				int amount = ((Num)owner.get(2)).realValue.intValue();
				Var var = (Var)owner.get(1);
				ComplexFloat equRightSideVal = null;
				Expr expr = owner.get(0);
				
				for(int i = 0;i < varDefs.size();i++) {//search for definition
					Equ equ = (Equ)varDefs.get(i);
					Var v = (Var)equ.getLeftSide();
					if(v.equals(var)) {
						equRightSideVal = ((FloatExpr)equ.getRightSide()).value;//found!
						break;
					}
				}
				
				if(equRightSideVal == null) return new ComplexFloat(0,0);
				
				ComplexFloat delta = new ComplexFloat( Math.pow( Math.abs(equRightSideVal.real)/Short.MAX_VALUE,1.0/amount ) ,0);//set original precision
				return calcDerivRec(expr,varDefs,equRightSideVal,amount,delta);
			}
		};
		
		similar.simplifyChildren = false;
		similar.ruleSequence.add(new Rule("expressions are similar"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(Rule.strictSimilarExpr(e.get(0), e.get(1)));
			}
		});
		fastSimilar.simplifyChildren = false;
		fastSimilar.ruleSequence.add(new Rule("expressions are similar computed quickly"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(Rule.fastSimilarExpr(e.get(0), e.get(1)));
			}
		});
		
		sortExpr.simplifyChildren = false;
		sortExpr.ruleSequence.add(new Rule("sort expression into cononical arangement") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				e.get().sort();
				System.out.println(e.get().flags.sorted);
				return e.get();
			}
		});
		
		delete.simplifyChildren = false;
		delete.ruleSequence.add(new Rule("delete variable or function") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Expr inner = e.get();
				
				if(inner instanceof Var)casInfo.definitions.removeVar(inner.toString());
				else if(inner instanceof Func) casInfo.definitions.removeFunc(inner.typeName());
				return Interpreter.SUCCESS;
			}
		});
		
		help.simplifyChildren = false;
		help.ruleSequence.add(new Rule("help function") {
			private static final long serialVersionUID = 1L;
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				
				return var(e.get().help());
			}
		});
		
		fSolve.ruleSequence.add(new Rule("floating point solver") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Sequence poly = polyExtract(e.get(0), (Var)e.get(1) ,casInfo);
				Sequence solutions = new Sequence();
				
				if(poly!=null) {
					ArrayList<Double> solutionsArrayList = Solve.polySolve(poly);
					for(double solution:solutionsArrayList) {
						solutions.add(floatExpr(solution));
					}
				}
				
				return solutions;
			}
		});
		
		for(String funcName :simpleFuncs.keySet()) {
			Rule.initRules(simpleFuncs.get(funcName).ruleSequence );
		}
	}
	
	private static ArrayList<String> functionNames = new ArrayList<String>();
	static HashMap<String,Integer> numberOfParams = new HashMap<String,Integer>();
	
	public static void addFunc(Func f){
		simpleFuncs.put(f.name,f);
		
	}
	
	static{
		addFunc(choose);
		addFunc(get);
		addFunc(size);
		addFunc(tree);
		addFunc(primeFactor);
		
		addFunc(partialFrac);
		addFunc(polyDiv);
		
		addFunc(polyCoef);
		addFunc(degree);
		addFunc(save);
		addFunc(open);
		addFunc(conv);
		addFunc(latex);
		addFunc(subst);
		addFunc(eval);
		addFunc(expand);
		addFunc(taylor);
		addFunc(gui);
		addFunc(nParams);
		addFunc(isType);
		addFunc(contains);
		addFunc(result);//override the simplify children bool
		addFunc(allowAbs);
		addFunc(allowComplexNumbers);
		addFunc(setAllowAbs);
		addFunc(setAllowComplexNumbers);
		
		addFunc(sinh);
		addFunc(cosh);
		addFunc(tanh);
		
		addFunc(sec);
		addFunc(csc);
		addFunc(cot);
		
		addFunc(extSeq);
		addFunc(truncSeq);
		addFunc(subSeq);
		addFunc(revSeq);
		addFunc(sumSeq);
		
		addFunc(arcLen);
		addFunc(repDiff);
		
		addFunc(similar);
		addFunc(fastSimilar);
		addFunc(sortExpr);
		
		addFunc(delete);
		addFunc(help);
		
		addFunc(fSolve);
		
		for(String s:simpleFuncs.keySet()) {
			functionNames.add(s);
		}
	}
	
	static {
		numberOfParams.put("sin", 1);
		numberOfParams.put("cos", 1);
		numberOfParams.put("tan", 1);
		numberOfParams.put("asin", 1);
		numberOfParams.put("acos", 1);
		numberOfParams.put("atan", 1);
		numberOfParams.put("sqrt", 1);
		numberOfParams.put("cbrt", 1);
		numberOfParams.put("ln", 1);
		numberOfParams.put("inv", 1);
		numberOfParams.put("gamma", 1);
		numberOfParams.put("factor", 1);
		numberOfParams.put("distr", 1);
		numberOfParams.put("exp", 1);
		numberOfParams.put("inv", 1);
		numberOfParams.put("neg", 1);
		
		numberOfParams.put("sinh", 1);
		numberOfParams.put("cosh", 1);
		numberOfParams.put("tanh", 1);
		
		numberOfParams.put("lambertW", 1);
		
		numberOfParams.put("solve", 2);
		numberOfParams.put("diff", 2);
		numberOfParams.put("range", 4);
		numberOfParams.put("integrate", 2);
		numberOfParams.put("approx", 2);
		
		numberOfParams.put("integrateOver", 4);
		numberOfParams.put("limit", 2);
		numberOfParams.put("abs", 1);
		numberOfParams.put("mat", 1);
		numberOfParams.put("transpose", 1);
		numberOfParams.put("next", 2);
		
		for(String s:numberOfParams.keySet()) {
			functionNames.add(s);
		}
	}
	
	public static boolean isFunc(String name) {
		return functionNames.contains(name);
	}
	
	public static int getExpectectedParams(String funcName) {
		Func func = simpleFuncs.get(funcName);
		if(func != null) {
			return func.numberOfParams;
		}
		
		Integer num = numberOfParams.get(funcName);
		if(num != null) {
			return num;
		}
		
		return 0;
	}
	
	public static Expr getFuncByName(String funcName,Expr... params) throws Exception {
		
		if(funcName.equals("approx")) {
			if(params.length == 1) {
				return approx(params[0],exprList());
			}else if(params.length == 2) {
				return approx(params[0],(ExprList)params[1]);
			}else {
				throw new Exception("function: "+funcName+", requires: 1 or 2 parameters");
			}
		}
		
		if(isFunc(funcName)) {
			int expectedParams = getExpectectedParams(funcName);
			if(expectedParams != params.length) {
				throw new Exception("function: "+funcName+", requires: "+expectedParams+", parameter(s)");
			}
		}
		
		Func func = simpleFuncs.get(funcName);
		if(func != null) {
			func = (Func)func.copy();
			for(Expr param:params) func.add(param);
			return func;
		}
		
		if(funcName.equals("sin")) return sin(params[0]);
		if(funcName.equals("cos")) return cos(params[0]);
		if(funcName.equals("tan")) return tan(params[0]);
		if(funcName.equals("asin")) return asin(params[0]);
		if(funcName.equals("acos")) return acos(params[0]);
		if(funcName.equals("atan")) return atan(params[0]);
		if(funcName.equals("sqrt")) return sqrt(params[0]);
		if(funcName.equals("cbrt")) return cbrt(params[0]);
		if(funcName.equals("ln")) return ln(params[0]);
		if(funcName.equals("inv")) return inv(params[0]);
		if(funcName.equals("gamma")) return gamma(params[0]);
		if(funcName.equals("factor")) return factor(params[0]);
		if(funcName.equals("distr")) return distr(params[0]);
		if(funcName.equals("exp")) return exp(params[0]);
		if(funcName.equals("inv")) return inv(params[0]);
		if(funcName.equals("neg")) return neg(params[0]);
		if(funcName.equals("lambertW")) return lambertW(params[0]);
		if(funcName.equals("abs")) return abs(params[0]);
		if(funcName.equals("mat")) return mat((Sequence)params[0]);
		if(funcName.equals("transpose")) return transpose(params[0]);
		if(funcName.equals("next")) return next((Sequence)params[0],(Num)params[1]);
		
		if(funcName.equals("gcd")) return gcd(params);
		
		if(funcName.equals("solve") && params[0] instanceof Equ) return solve((Equ)params[0],(Var)params[1]);
		if(funcName.equals("solve") && params[0] instanceof ExprList) return solve((ExprList)params[0],(ExprList)params[1]);
		
		if(funcName.equals("diff")) return diff(params[0],(Var)params[1]);
		if(funcName.equals("integrate")) return integrate(params[0],(Var)params[1]);
		
		if(funcName.equals("integrateOver")) return integrateOver(params[0],params[1],params[2],(Var)params[3]);
	
		if(funcName.equals("limit")) return limit(params[0],(Becomes)params[1]);
		if(funcName.equals("range")) return range(params[0],params[1],params[2],(Var)params[3]);
		
		if(!FUNCTION_UNLOCKED) throw new Exception("no function by the name: "+funcName);//allow making new functions on the fly
		
		
		Func blankFunc = new Func(funcName);
		for(Expr param:params) blankFunc.add(param);
		blankFunc.numberOfParams = params.length;
		
		return blankFunc;
	}
}
