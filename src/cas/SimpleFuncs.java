package cas;

import java.util.HashMap;

public class SimpleFuncs extends QuickMath{

	public static HashMap<String,Func> funcs = new HashMap<String,Func>();
	
	public static Rule fullExpand = new Rule("full expand",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		Expr applyRuleToExpr(Expr e,Settings settings){
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
					return result;
				}
			}
			
			return e;
		}
	};
	
	static Func tree = new Func("tree",1);
	static Func size = new Func("size",1);
	static Func get = new Func("get",2);
	static Func choose = new Func("choose",2);
	static Func primeFactor = new Func("primeFactor",1);
	static Func partialFrac = new Func("partialFrac",2);
	
	public static void loadRules(){
		tree.simplifyChildren = false;
		tree.ruleSequence.add(new Rule("show the tree of the expression",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			Expr applyRuleToExpr(Expr e,Settings settings){
				Func tree = null;
				if(e instanceof Func && ((Func) e).name == "tree"){
					tree = (Func)e;
				}else{
					return e;
				}
				Expr inner = tree.getParameter(0);
				
				return var(inner.printTree(0));
			}
		});
		
		size.simplifyChildren = false;
		size.ruleSequence.add(new Rule("get size of sub expression",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			Expr applyRuleToExpr(Expr e,Settings settings){
				Func size = null;
				if(e instanceof Func && ((Func) e).name == "size"){
					size = (Func)e;
				}else{
					return e;
				}
				Expr inner = size.getParameter(0);
				
				return num(inner.size());
			}
		});
		
		get.simplifyChildren = false;
		get.ruleSequence.add(new Rule("get sub expression",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

				@Override
				Expr applyRuleToExpr(Expr e,Settings settings){
					Func get = null;
					if(e instanceof Func && ((Func) e).name == "get"){
						get = (Func)e;
					}else{
						return e;
					}
					Expr inner = get.getParameter(0);
					int index = ((Num)get.getParameter(1)).realValue.intValue();
					
					return inner.get(index);
				}
		});
		
		choose.ruleSequence.add(new Rule("choose formula",Rule.UNCOMMON){
			
			private static final long serialVersionUID = 1L;

			@Override
			Expr applyRuleToExpr(Expr e,Settings settings){
				Func choose = null;
				if(e instanceof Func && ((Func) e).name == "choose"){
					choose = (Func)e;
				}else{
					return e;
				}
				
				Expr n = choose.getParameter(0);
				Expr k = choose.getParameter(1);
				
				if(isPositiveRealNum(n) && isPositiveRealNum(k)){
					return num(choose( ((Num)n).realValue , ((Num)k).realValue));
				}
				return e;
			}
		});
		
		primeFactor.ruleSequence.add(new Rule("prime factor an integer",Rule.EASY){
			private static final long serialVersionUID = 1L;

			@Override
			Expr applyRuleToExpr(Expr e,Settings settings){
				Func pf = null;
				if(e instanceof Func && ((Func) e).name == "primeFactor"){
					pf = (Func)e;
				}else{
					return e;
				}
				Expr inner = pf.getParameter(0);
				if(inner instanceof Num) return primeFactor((Num)inner);
				return inner;
			}
		});
		
		partialFrac.ruleSequence.add(new Rule("break apart polynomial ratio into a sum of inverse linear terms",Rule.VERY_EASY){
			private static final long serialVersionUID = 1L;

			@Override
			Expr applyRuleToExpr(Expr e,Settings settings){
				Func pf = null;
				if(e instanceof Func && ((Func) e).name == "partialFrac"){
					pf = (Func)e;
				}else{
					return e;
				}
				Expr inner = pf.getParameter(0);
				Var var = (Var)pf.getParameter(1);
				return partialFrac(inner,var,settings);
			}
		});
	}
	
	/*
	static Func polyCoef = new Func("polyCoef",2){
		private static final long serialVersionUID = 1L;
		@Override
		void init(){
			ruleSequence.add(new Rule("get the coefficients of a polynomial as a list",Rule.VERY_EASY){
				@Override
				Expr applyRuleToExpr(Expr e,Settings settings){
					Func pc = null;
					if(e instanceof Func && ((Func) e).name == "polyCoef"){
						pc = (Func)e;
					}else{
						return e;
					}
					Expr inner = pc.getParameter(0);
					Var var = (Var)pc.getParameter(1);
					return polyExtract(inner,var,settings);
				}
			});
		}
	};
	
	static Func degree = new Func("degree",2){
		private static final long serialVersionUID = 1L;
		@Override
		void init(){
			ruleSequence.add(new Rule("get the degree of a polynomial",Rule.VERY_EASY){
				@Override
				Expr applyRuleToExpr(Expr e,Settings settings){
					Func dg = null;
					if(e instanceof Func && ((Func) e).name == "degree"){
						dg = (Func)e;
					}else{
						return e;
					}
					Expr inner = dg.getParameter(0);
					Var var = (Var)dg.getParameter(1);
					return num(degree(inner,var));
				}
			});
		}
	};
	
	static Func partialFrac = new Func("partialFrac",2){
		private static final long serialVersionUID = -7797150002058225696L;

		@Override
		void init(){
			ruleSequence.add(new Rule("break apart polynomial ratio into a sum of inverse linear terms",Rule.VERY_EASY){
				@Override
				Expr applyRuleToExpr(Expr e,Settings settings){
					Func pf = null;
					if(e instanceof Func && ((Func) e).name == "partialFrac"){
						pf = (Func)e;
					}else{
						return e;
					}
					Expr inner = pf.getParameter(0);
					Var var = (Var)pf.getParameter(1);
					return partialFrac(inner,var,settings);
				}
			});
		}
	};
	
	static Func bigger = new Func("bigger",3){
		private static final long serialVersionUID = 5132340930485084772L;

		@Override
		void init(){
			rules.add(new Rule("choose the bigger function",Rule.VERY_EASY){
				@Override
				Expr applyRuleToExpr(Expr e,Settings settings){
					Func bg = null;
					if(e instanceof Func && ((Func) e).name == "bigger"){
						bg = (Func)e;
					}else{
						return e;
					}
					Expr a = bg.getParameter(0);
					Expr b = bg.getParameter(1);
					Var var = (Var)bg.getParameter(2);
					
					Expr out = Limit.Bigger.bigger(a, b, var);
					return out == null ? var("neither") : out;
				}
			});
		}
	};
	*/
	static{
		addFunc(choose);
		addFunc(get);
		addFunc(size);
		addFunc(tree);
		addFunc(primeFactor);
		addFunc(partialFrac);
		/*
		addFunc(polyCoef);
		addFunc(degree);
		addFunc(bigger);
		*/
	}
	
	public static void addFunc(Func f){
		funcs.put(f.name,f);
		
	}
}
