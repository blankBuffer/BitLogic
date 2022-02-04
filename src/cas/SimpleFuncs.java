package cas;

import java.io.IOException;
import java.util.HashMap;

public class SimpleFuncs extends QuickMath{

	private static HashMap<String,Func> simpleFuncs = new HashMap<String,Func>();
	
	public static Rule fullExpand = new Rule("full expand",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			if(e instanceof Prod){
				boolean changed = false;
				for(int i = 0;i<e.size();i++){
					if(e.get(i) instanceof Power){
						Power casted = (Power)e.get(i);
						if( isPositiveRealNum(casted.getExpo()) && casted.getBase() instanceof Sum){
							e.set(i, multinomial(casted.getBase(),(Num)casted.getExpo(),settings));
							changed = true;
						}
					}
				}
				if(changed){
					Expr result = distr(e).simplify(settings);
					return result;
				}
			}else if(e instanceof Sum){
				Expr sum = new Sum();
				
				for(int i = 0;i<e.size();i++){
					sum.add( fullExpand.applyRuleToExpr(e.get(i), settings) );
				}
				sum = sum.simplify(settings);
				return sum;
			}else if(e instanceof Power){
				Power casted = (Power)e;
				if( isPositiveRealNum(casted.getExpo()) && casted.getBase() instanceof Sum){
					Expr result = multinomial(casted.getBase(),(Num)casted.getExpo(),settings);
					return result.simplify(settings);
				}
			}
			
			return e;
		}
	};
	
	static Func tree = new Func("tree");
	static Func size = new Func("size");
	static Func get = new Func("get");
	static Func choose = new Func("choose");
	static Func primeFactor = new Func("primeFactor");
	static Func partialFrac = new Func("partialFrac");
	static Func polyCoef = new Func("polyCoef");
	static Func degree = new Func("degree");
	static Func bigger = new Func("bigger");
	static Func save = new Func("save");
	static Func open = new Func("open");
	static Func conv = new Func("conv");
	static Func mathML = new Func("mathML");
	static Func subst = new Func("subst");
	static Func eval = new Func("eval");
	static Func expand = new Func("expand");
	
	public static void loadRules(){
		tree.simplifyChildren = false;
		tree.ruleSequence.add(new Rule("show the tree of the expression",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				return var(f.get().toStringTree(0));
			}
		});
		
		size.simplifyChildren = false;
		size.ruleSequence.add(new Rule("get size of sub expression",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				
				return num(f.get().size());
			}
		});
		
		get.simplifyChildren = false;
		get.ruleSequence.add(new Rule("get sub expression",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					Func f = (Func)e;
					
					return f.get().get( ((Num)f.get(1)).realValue.intValue() );
				}
		});
		
		choose.ruleSequence.add(new Rule("choose formula",Rule.UNCOMMON){
			
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				
				Expr n = f.get(0);
				Expr k = f.get(1);
				
				if(isPositiveRealNum(n) && isPositiveRealNum(k)){
					return num(choose( ((Num)n).realValue , ((Num)k).realValue));
				}
				return e;
			}
		});
		
		primeFactor.ruleSequence.add(new Rule("prime factor an integer",Rule.EASY){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				return primeFactor((Num)f.get());
			}
		});
		
		partialFrac.ruleSequence.add(new Rule("break apart polynomial ratio into a sum of inverse linear terms",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				return partialFrac(inner,var,settings);
			}
		});
		
		polyCoef.ruleSequence.add(new Rule("get the coefficients of a polynomial as a list",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				return polyExtract(inner,var,settings);
			}
		});
		
		degree.ruleSequence.add(new Rule("get the degree of a polynomial",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				return num(degree(inner,var));
			}
		});
		
		bigger.ruleSequence.add(new Rule("choose the bigger function",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				Expr a = f.get(0);
				Expr b = f.get(1);
				Var var = (Var)f.get(2);
				
				Expr out = Limit.Bigger.bigger(a, b, var);
				return out == null ? var("neither") : out;
			}
		});
		
		save.simplifyChildren = false;
		save.ruleSequence.add(new Rule("saving expression",Rule.VERY_EASY) {
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				try {
					Expr.saveExpr(f.get(0), f.get(1).toString());
				} catch (IOException e1) {
					e1.printStackTrace();
					return var("error");
				}
				
				return var("done");
			}
		});
		
		open.simplifyChildren = false;
		open.ruleSequence.add(new Rule("opening expression",Rule.VERY_EASY) {
			private static final long serialVersionUID = 1L;

			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings){
				Func f = (Func)e;
				
				try {
					return Expr.openExpr(f.get(0).toString());
				} catch (ClassNotFoundException | IOException e1) {
					e1.printStackTrace();
				}
				
				return var("error");
			}
		});
		
		conv.simplifyChildren = false;
		conv.ruleSequence.add(new Rule("unit conversion",Rule.EASY) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings) {
				Func f = (Func)e;
				
				try {
					return approx(Unit.conv(f.get(0), Unit.getUnit(f.get(1).toString()), Unit.getUnit(f.get(2).toString())),exprList()).simplify(settings);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				return var("error");
			}
		});
		
		mathML.simplifyChildren = false;
		mathML.ruleSequence.add(new Rule("math markup language conversion",Rule.EASY) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings) {
				Func f = (Func)e;
				
				return var(generateMathML(f.get()));
			}
		});
		
		subst.ruleSequence.add(new Rule("substitution",Rule.EASY) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings) {
				Func f = (Func)e;
				if(f.get(1) instanceof ExprList) {
					return f.get().replace((ExprList)f.get(1)).simplify(settings);
				}
				return f.get().replace((Equ)f.get(1)).simplify(settings);
			}
		});
		
		eval.ruleSequence.add(new Rule("evaluate",Rule.EASY) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings) {
				Func f = (Func)e;
				
				if(f.get() instanceof Equ) {
					Equ casted = (Equ)f.get();
					boolean equal = casted.getLeftSide().exactlyEquals(casted.getRightSide());
					
					if(casted.type == Equ.EQUALS) {
						return bool(equal);
					}
					if(casted.containsVars()) return bool(true);
					if(casted.type == Equ.GREATER) {
						return bool(!equal && casted.getLeftSide().convertToFloat(exprList()).real > casted.getRightSide().convertToFloat(exprList()).real );
					}
					if(casted.type == Equ.LESS) {
						return bool(!equal && casted.getLeftSide().convertToFloat(exprList()).real < casted.getRightSide().convertToFloat(exprList()).real );
					}
				}
				
				return f;
			}
		});
		
		expand.simplifyChildren = false;
		expand.ruleSequence.add(new Rule("expand",Rule.TRICKY) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Expr applyRuleToExpr(Expr e,Settings settings) {
				Func f = (Func)e;
				return fullExpand.applyRuleToExpr(f.get(), settings);
			}
		});
	}
	static{
		addFunc(choose);
		addFunc(get);
		addFunc(size);
		addFunc(tree);
		addFunc(primeFactor);
		addFunc(partialFrac);
		addFunc(polyCoef);
		addFunc(degree);
		addFunc(bigger);
		addFunc(save);
		addFunc(open);
		addFunc(conv);
		addFunc(mathML);
		addFunc(subst);
		addFunc(eval);
		addFunc(expand);
	}
	
	public static Expr getFuncByName(String funcName,Defs defs,Expr... params) {
		Expr func = simpleFuncs.get(funcName);
		if(func != null) {
			func = func.copy();
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
		if(funcName.equals("gamma")) return gamma(params[0]);
		if(funcName.equals("factor")) return factor(params[0]);
		if(funcName.equals("distr")) return distr(params[0]);
		if(funcName.equals("solve")) return solve((Equ)params[0],(Var)params[1]);
		if(funcName.equals("exp")) return exp(params[0]);
		if(funcName.equals("inv")) return inv(params[0]);
		if(funcName.equals("sinh")) return sinh(params[0]);
		if(funcName.equals("cosh")) return cosh(params[0]);
		if(funcName.equals("tanh")) return tanh(params[0]);
		if(funcName.equals("approx")) {
			if(params.length == 1) {
				return approx(params[0],exprList());
			}
			return approx(params[0],(ExprList)params[1]);
		}
		if(funcName.equals("lambertW")) return lambertW(params[0]);
		if(funcName.equals("diff")) return diff(params[0],(Var)params[1]);
		if(funcName.equals("integrate")) return integrate(params[0],(Var)params[1]);
		if(funcName.equals("integrateOver")) return integrateOver(params[0],params[1],params[2],(Var)params[3]);
		if(funcName.equals("limit")) return limit(params[0],(Var)params[1],params[2]);
		
		Func f = defs.getFunc(funcName);
		if(f != null) {
			for(Expr param:params) f.add(param);
			return f;
		}
		
		Func blankFunc = new Func(funcName);
		for(Expr param:params) blankFunc.add(param);
		
		return blankFunc;
	}
	
	public static void addFunc(Func f){
		simpleFuncs.put(f.name,f);
		
	}
}
