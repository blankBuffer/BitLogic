package cas;

import java.util.HashMap;

public class SimpleFuncs extends QuickMath{
	public static HashMap<String,Func> funcs = new HashMap<String,Func>();
	
	static Func choose = new Func("choose",2){
		private static final long serialVersionUID = 1L;
		@Override
		void init(){
			rules.add(new Rule("choose formula",Rule.UNCOMMON){
				@Override
				public void init(){
					example = "choose(4,2)=6";
				}
				
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
		}
	};
	static Func expand = new Func("expand",1){
		private static final long serialVersionUID = 1L;
		@Override
		void init(){
			rules.add(new Rule("full expand",Rule.TRICKY){
				@Override
				public void init(){
					example = "expand((x+3)^3)=27+27*x+9*x^2+x^3";
				}
				
				@Override
				Expr applyRuleToExpr(Expr e,Settings settings){
					Func expand = null;
					if(e instanceof Func && ((Func) e).name == "expand"){
						expand = (Func)e;
					}else{
						return e;
					}
					
					Expr inner = expand.getParameter(0);
					
					if(inner instanceof Prod){
						Expr original = e.copy();
						boolean changed = false;
						for(int i = 0;i<inner.size();i++){
							if(inner.get(i) instanceof Power){
								Power casted = (Power)inner.get(i);
								if( isPositiveRealNum(casted.getExpo()) && casted.getBase() instanceof Sum){
									inner.set(i, multinomial(casted.getBase(),(Num)casted.getExpo(),settings));
									changed = true;
								}
							}
						}
						if(changed){
							Expr result = distr(inner).simplify(settings);
							verboseMessage(original,result);
							return result;
						}
					}else if(inner instanceof Power){
						Power casted = (Power)inner;
						if( isPositiveRealNum(casted.getExpo()) && casted.getBase() instanceof Sum){
							Expr result = multinomial(casted.getBase(),(Num)casted.getExpo(),settings);
							verboseMessage(e,result);
							return result;
						}
					}
					
					return inner;
				}
			});
		}
	};
	
	static Func get = new Func("get",2){
		private static final long serialVersionUID = 1L;
		@Override
		void init(){
			rules.add(new Rule("get sub expression",Rule.VERY_EASY){
				@Override
				public void init(){
					example = "get(a+b,0)=a";
				}
				
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
		}
	};
	
	static Func size = new Func("size",1){
		private static final long serialVersionUID = 1L;
		@Override
		void init(){
			rules.add(new Rule("get size of sub expression",Rule.VERY_EASY){
				@Override
				public void init(){
					example = "size(a+b+c)=3";
				}
				
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
		}
	};
	
	static Func tree = new Func("tree",1){
		private static final long serialVersionUID = 1L;
		@Override
		void init(){
			rules.add(new Rule("show the tree of the expression",Rule.VERY_EASY){
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
		}
	};
	
	static{
		addFunc(choose);
		addFunc(expand);
		addFunc(get);
		addFunc(size);
		addFunc(tree);
	}
	
	public static void addFunc(Func f){
		funcs.put(f.name,f);
		
	}
}
