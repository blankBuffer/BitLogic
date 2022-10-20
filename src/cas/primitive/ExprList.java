package cas.primitive;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;

/*
 * this is a list where the order does not matter and will remove repeats of elements when simplified
 */
public class ExprList extends Expr{

	public ExprList(){
	}
	
	@Override
	public boolean isCommutative(){
		return true;
	}
	
	static Rule removeRepeats = new Rule("remove repeats"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			ExprList list = (ExprList)e;
			
			for(int i = 0;i<list.size();i++) {
				for(int j = i+1;j<list.size();j++) {
					if(list.get(i).equals(list.get(j))) {
						list.remove(j);
						j--;
					}
				}
			}
			return list;
		}
	};
	
	static Rule listContainsList = new Rule("list contains list") {
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			ExprList list = (ExprList)e;
			
			ExprList outList = new ExprList();
			
			for(int i = 0;i<list.size();i++) {
				if(list.get(i) instanceof ExprList) {
					ExprList subList = (ExprList)list.get(i);
					for(int j = 0;j<subList.size();j++) {
						outList.add(subList.get(j));
					}
				}else {
					outList.add(list.get(i));
				}
			}
			
			return outList;
		}
	};

	@Override
	public String toString() {
		String out = "";
		out+='{';
		for(int i = 0;i<size();i++) {
			out+=get(i).toString();
			if(i != size()-1) out+=',';
		}
		out+='}';
		return out;
	}
	
	public static ExprList cast(Expr e) {
		if(e == null) return exprList();
		if(e instanceof ExprList) return (ExprList)e;
		if(e instanceof Params || e instanceof Sequence) {
			ExprList out = new ExprList();
			for(int i = 0;i<e.size();i++) {
				out.add(e.get(i));
			}
			return out;
		}
		return exprList(e);
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		if(size()==0) return new ComplexFloat(0,0);
		return get().convertToFloat(varDefs);
	}
	
	static Rule mainSequenceRule = null;

	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
				listContainsList,
				removeRepeats,
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
	}
	
	@Override
	public String typeName() {
		return "exprList";
	}

	@Override
	public String help() {
		return "list expression contains unique expressions\n"
				+ "examples\n"
				+ "{2,3}\n"
				+ "solve(x^2=2,x)->{x=-sqrt(2),x=sqrt(2)}";
	}

}
