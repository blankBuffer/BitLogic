package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.Cas;

/*
 * this is a list where the order does not matter and will remove repeats of elements when simplified
 * note the typename is "set"
 */
public class ExprSet{
	
	public static Func.FuncLoader exprSetLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			Rule removeRepeats = new Rule("remove repeats"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func set = (Func)e;
					
					for(int i = 0;i<set.size();i++) {
						for(int j = i+1;j<set.size();j++) {
							if(set.get(i).equals(set.get(j))) {
								set.remove(j);
								j--;
							}
						}
					}
					return set;
				}
			};
			
			Rule setContainsSet = new Rule("list contains list") {
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func set = (Func)e;
					
					Func outSet = exprSet();
					
					for(int i = 0;i<set.size();i++) {
						if(set.get(i).typeName().equals("set")) {
							Func subSet = (Func)set.get(i);
							for(int j = 0;j<subSet.size();j++) {
								outSet.add(subSet.get(j));
							}
						}else {
							outSet.add(set.get(i));
						}
					}
					
					return outSet;
				}
			};
			
			owner.behavior.toStringMethod = new Func.ToString() {
				@Override
				public String generateString(Func owner) {
					String out = "";
					out+='{';
					for(int i = 0;i<owner.size();i++) {
						out+=owner.get(i).toString();
						if(i != owner.size()-1) out+=',';
					}
					out+='}';
					return out;
				}
			};
			
			
			owner.behavior.commutative = true;
			owner.behavior.rule = new Rule(new Rule[] {
					setContainsSet,
					removeRepeats,
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					if(owner.size()==0) return new ComplexFloat(0,0);
					return owner.get().convertToFloat(varDefs);
				}
			};
		}
	};
	
	public static Func cast(Expr e) {
		if(e == null) return Cas.exprSet();
		if(e.typeName().equals("set")) return (Func)e;
		if(e instanceof Params || e instanceof Sequence) {
			Func out = Cas.exprSet();
			for(int i = 0;i<e.size();i++) {
				out.add(e.get(i));
			}
			return out;
		}
		return Cas.exprSet(e);
	}
	
	static Rule mainSequenceRule = null;

	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
				
		},"main sequence");
		mainSequenceRule.init();
	}

}
