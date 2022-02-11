package cas;

public class ExprList extends Expr{

	private static final long serialVersionUID = 4631446864313039932L;
	
	public ExprList(){
		commutative = true;
	}
	static Rule removeRepeats = new Rule("remove repeats",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
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
	
	static Rule alone = new Rule("alone list",Rule.VERY_EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			ExprList list = (ExprList)e;
			if(list.size() == 1) {//if its only one element 
				return list.get(0);
			}else if(list.size() == 0) {//if its empty return 0
				return num(0);
			}
			return list;
		}
	};
	
	static Rule listContainsList = new Rule("list contains list",Rule.VERY_EASY) {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
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
		out+='[';
		for(int i = 0;i<size();i++) {
			out+=get(i).toString();
			if(i != size()-1) out+=',';
		}
		out+=']';
		return out;
	}
	
	public static ExprList cast(Expr e) {
		if(e instanceof ExprList) return (ExprList)e;
		ExprList out = new ExprList();
		if(e != null) out.add(e);
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}
	
	static ExprList ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = exprList(
				listContainsList,
				removeRepeats,
				alone
		);
	}

	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}

}
