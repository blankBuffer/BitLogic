package cas;

public class And extends Expr{

	private static final long serialVersionUID = 8729081482954093557L;
	
	public And(){
		commutative = true;
	}
	
	static Rule andContainsAnd = new Rule("and contains and",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = (And)e;
			for(int i = 0;i<and.size();i++){
				if(and.get(i) instanceof And){
					And subAnd = (And)and.get(i);
					and.remove(i);
					i--;
					for(int j = 0;j<subAnd.size();j++){
						and.add(subAnd.get(j));
					}
				}
			}
			return and;
		}
		
	};
	
	static Rule nullRule = new Rule("null rule",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = (And)e;
			for(int i = 0;i<and.size();i++){
				if(and.get(i).equals(BoolState.FALSE)){
					Expr result = bool(false);
					return result;
				}
			}
			return and;
		}
	};
	
	static Rule removeTrues = new Rule("remove trues",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = (And)e;
			for(int i = 0;i<and.size();i++){
				if(and.get(i).equals(BoolState.TRUE)){
					and.remove(i);
					i--;
				}
			}
			return and;
		}
	};
	
	static Rule complement = new Rule("has complement",Rule.TRICKY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = (And)e;
			for(int i = 0;i<and.size();i++){
				Expr current = and.get(i);
				Expr complement = current instanceof Not ? current.get() : not(current);
				
				for(int j = i+1;j<and.size();j++){
					Expr other = and.get(j);
					
					if(other.equals(complement)){
						Expr result = bool(false);
						return result;
					}
				}
				
			}
			return and;
		}
	};
	
	static Rule removeDuplicates = new Rule("remove duplicates",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = (And)e;
			for(int i = 0;i<and.size();i++){
				Expr current = and.get(i);
				
				for(int j = i+1;j<and.size();j++){
					Expr other = and.get(j);
					
					if(other.equals(current)){
						and.remove(i);
						i--;
					}
				}
				
			}
			return and;
		}
	};
	
	static Rule distribute = new Rule("and contains or",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = (And)e;
			for(int i = 0;i<and.size();i++){
				if(and.get(i) instanceof Or){
					Or subOr = (Or)and.get(i).copy();
					And andRem = (And)and.copy();
					andRem.remove(i);
					
					for(int j = 0;j<subOr.size();j++){
						Expr subExpr = subOr.get(j);
						
						if(subExpr instanceof And){
							And andRemCopy = (And)andRem.copy();
							for(int k = 0;k<subExpr.size();k++){
								andRemCopy.add(subExpr.get(k));
							}
							subOr.set(j, andRemCopy);
						}else{
							And andRemCopy = (And)andRem.copy();
							andRemCopy.add(subExpr);
							subOr.set(j, andRemCopy);
						}
						
						
					}
					
					Expr result = subOr.simplify(settings);
					return result;
				}
			}
			return and;
		}
	};
	
	static Rule aloneAnd = new Rule("and has one element",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = (And)e;
			Expr result = null;
			if(and.size() == 0){
				result = bool(true);
			}else if(and.size() == 1){
				result = and.get();
			}
			if(result != null){
				return result;
			}
			return and;
		}
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				andContainsAnd,
				removeDuplicates,
				nullRule,
				removeTrues,
				complement,
				distribute,
				aloneAnd
		);
		Rule.initRules(ruleSequence);
	}

	@Override
	Sequence getRuleSequence(){
		return ruleSequence;
	}

	@Override
	public String toString() {
		String out = "";
		if(size() < 2) out+="alone and:";
		for(int i = 0;i<size();i++){
			boolean paren = get(i) instanceof Or || get(i) instanceof And;
			if(paren) out+="(";
			out+=get(i);
			if(paren) out+=")";
			if(i!=size()-1) out+="&";
		}
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		boolean state = true;
		for(int i = 0;i<size();i++){
			state &= Math.abs(get(i).convertToFloat(varDefs).real)>0.5;
		}
		double res = state ? 1.0 : 0.0;
		return new ComplexFloat(res,0);
	}
	
	public static And cast(Expr e){
		if(e instanceof And){
			return (And)e;
		}
		return and(e);
	}
}
