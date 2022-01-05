package cas;

public class And extends Expr{

	private static final long serialVersionUID = 8729081482954093557L;
	
	public And(){
		commutative = true;
	}
	
	static Rule andContainsAnd = new Rule("and contains and",Rule.VERY_EASY){
		@Override
		public void init(){
			example = "a&(b&c)=a&b&c";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = null;
			if(e instanceof And){
				and = (And)e;
			}else{
				return e;
			}
			Expr original = null;
			for(int i = 0;i<and.size();i++){
				if(and.get(i) instanceof And){
					if(original == null) original = e.copy();
					And subAnd = (And)and.get(i);
					and.remove(i);
					i--;
					for(int j = 0;j<subAnd.size();j++){
						and.add(subAnd.get(j));
					}
				}
			}
			if(original != null){
				verboseMessage(original,and);
			}
			return and;
		}
		
	};
	
	static Rule nullRule = new Rule("null rule",Rule.EASY){
		@Override
		public void init(){
			example = "a&false=false";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = null;
			if(e instanceof And){
				and = (And)e;
			}else{
				return e;
			}
			for(int i = 0;i<and.size();i++){
				if(and.get(i).equalStruct(BoolState.FALSE)){
					Expr result = bool(false);
					verboseMessage(e,result);
					return result;
				}
			}
			return and;
		}
	};
	
	static Rule removeTrues = new Rule("remove trues",Rule.VERY_EASY){
		@Override
		public void init(){
			example = "a&b&true=a&b";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = null;
			if(e instanceof And){
				and = (And)e;
			}else{
				return e;
			}
			Expr original = null;
			for(int i = 0;i<and.size();i++){
				if(and.get(i).equalStruct(BoolState.TRUE)){
					if(original == null) original = e.copy(); 
					and.remove(i);
					i--;
				}
			}
			if(original != null){
				verboseMessage(original,and);
			}
			return and;
		}
	};
	
	static Rule complement = new Rule("has complement",Rule.TRICKY){
		@Override
		public void init(){
			example = "a&~a=false";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = null;
			if(e instanceof And){
				and = (And)e;
			}else{
				return e;
			}
			for(int i = 0;i<and.size();i++){
				Expr current = and.get(i);
				Expr complement = current instanceof Not ? current.get() : not(current);
				
				for(int j = i+1;j<and.size();j++){
					Expr other = and.get(j);
					
					if(other.equalStruct(complement)){
						Expr result = bool(false);
						verboseMessage(e,result);
						return result;
					}
				}
				
			}
			return and;
		}
	};
	
	static Rule removeDuplicates = new Rule("remove duplicates",Rule.VERY_EASY){
		@Override
		public void init(){
			example = "a&a&b=a&b";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = null;
			if(e instanceof And){
				and = (And)e;
			}else{
				return e;
			}
			Expr original = null;
			for(int i = 0;i<and.size();i++){
				Expr current = and.get(i);
				
				for(int j = i+1;j<and.size();j++){
					Expr other = and.get(j);
					
					if(other.equalStruct(current)){
						if(original == null) original = e.copy(); 
						and.remove(i);
						i--;
					}
				}
				
			}
			if(original != null){
				verboseMessage(original,and);
			}
			return and;
		}
	};
	
	static Rule distribute = new Rule("and contains or",Rule.EASY){
		@Override
		public void init(){
			example = "a&(b|c)=a&b|a&c";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = null;
			if(e instanceof And){
				and = (And)e;
			}else{
				return e;
			}
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
					verboseMessage(e,result);
					return result;
				}
			}
			return and;
		}
	};
	
	static Rule aloneAnd = new Rule("and has one element",Rule.VERY_EASY){
		String emptyAndExample = "alone and:=true";
		String aloneAndExample = "alone and:a=a";
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			And and = null;
			if(e instanceof And){
				and = (And)e;
			}else{
				return e;
			}
			Expr result = null;
			if(and.size() == 0){
				example = emptyAndExample;
				result = bool(true);
			}else if(and.size() == 1){
				example = aloneAndExample;
				result = and.get();
			}
			if(result != null){
				verboseMessage(e,result);
				return result;
			}
			return and;
		}
	};
	
	static Rule[] ruleSequence = {
			andContainsAnd,
			removeDuplicates,
			nullRule,
			removeTrues,
			complement,
			distribute,
			aloneAnd,
	};

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify all the sub expressions
		
		for (Rule r:ruleSequence){
			toBeSimplified = r.applyRuleToExpr(toBeSimplified, settings);
		}
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
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
