package cas;

import java.util.ArrayList;
import java.util.Collections;

public class Or extends Expr{
	
	private static final long serialVersionUID = 5003710279364491787L;

	public Or(){
		commutative = true;
	}
	
	static Rule orContainsOr = new Rule("or contains or",Rule.VERY_EASY){
		@Override
		public void init(){
			example = "a|(b|c)=a|b|c";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			Expr original = null;
			for(int i = 0;i<or.size();i++){
				if(or.get(i) instanceof Or){
					if(original == null) original = e.copy();
					Or subOr = (Or)or.get(i);
					or.remove(i);
					i--;
					for(int j = 0;j<subOr.size();j++){
						or.add(subOr.get(j));
					}
				}
			}
			if(original != null){
				verboseMessage(original,or);
			}
			return or;
		}
		
	};
	
	static Rule nullRule = new Rule("null rule",Rule.EASY){
		@Override
		public void init(){
			example = "a|true=true";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			for(int i = 0;i<or.size();i++){
				if(or.get(i).equalStruct(BoolState.TRUE)){
					Expr result = bool(true);
					verboseMessage(e,result);
					return result;
				}
			}
			return or;
		}
	};
	
	static Rule removeFalses = new Rule("remove falses",Rule.VERY_EASY){
		@Override
		public void init(){
			example = "a|b|false=a&b";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			Expr original = null;
			for(int i = 0;i<or.size();i++){
				if(or.get(i).equalStruct(BoolState.FALSE)){
					if(original == null) original = e.copy(); 
					or.remove(i);
					i--;
				}
			}
			if(original != null){
				verboseMessage(original,or);
			}
			return or;
		}
	};
	
	static Rule complement = new Rule("has complement",Rule.TRICKY){
		String easyExample = "a|~a=true";
		String hardExample = "a&b|~a|~b=true";
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			for(int i = 0;i<or.size();i++){
				Expr current = or.get(i);
				Expr complement = not(current).simplify(settings);
				
				if(complement instanceof Or){//trickier case a&b|~a|~b=true
					boolean hasAll = true;
					for(int j = 0;j<complement.size();j++){
						boolean found = false;
						Expr toFind = complement.get(j);
						for(int k = 0;k<or.size();k++){
							if(k == i) continue;
							
							if(or.get(k).equalStruct(toFind)){
								found = true;
								break;
							}
							
						}
						if(!found){
							hasAll = false;
							break;
						}
					}
					if(hasAll){
						Expr result = bool(true);
						example = hardExample;
						verboseMessage(e,result);
						return result;
					}
				}else{
					for(int j = i+1;j<or.size();j++){
						Expr other = or.get(j);
						
						if(other.equalStruct(complement)){
							Expr result = bool(true);
							example = easyExample;
							verboseMessage(e,result);
							return result;
						}
					}
				}
				
			}
			return or;
		}
	};
	
	static Rule removeDuplicates = new Rule("remove duplicates",Rule.VERY_EASY){
		@Override
		public void init(){
			example = "a&a&b=a&b";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			Expr original = null;
			for(int i = 0;i<or.size();i++){
				Expr current = or.get(i);
				
				for(int j = i+1;j<or.size();j++){
					Expr other = or.get(j);
					
					if(other.equalStruct(current)){
						if(original == null) original = e.copy(); 
						or.remove(i);
						i--;
					}
				}
				
			}
			if(original != null){
				verboseMessage(original,or);
			}
			return or;
		}
	};
	
	static Rule absorb = new Rule("absorption",Rule.EASY){
		@Override
		public void init(){
			example = "x|x&y=x";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			
			for(int i = 0;i<or.size();i++){
				And current = And.cast(or.get(i));
				for(int j = 0;j<or.size();j++){
					if(j == i) continue;
					if(or.get(j) instanceof And){
						And other = (And)or.get(j);
						
						boolean hasAll = true;
						for(int k = 0;k<current.size();k++){
							boolean found = false;
							
							for(int l = k;l<other.size();l++){
								if(current.get(k).equalStruct(other.get(l))){
									found = true;
									break;
								}
							}
							
							if(!found){
								hasAll = false;
								break;
							}
						}
						
						if(hasAll){
							or.remove(j);
							j--;
						}
						
					}
				}
			}
			
			return or;
		}
	};
	
	static Rule aloneOr = new Rule("or has one element",Rule.VERY_EASY){
		String emptyOrExample = "alone or:=false";
		String aloneOrExample = "alone or:a=a";
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			Expr result = null;
			if(or.size() == 0){
				example = emptyOrExample;
				result = bool(false);
			}else if(or.size() == 1){
				example = aloneOrExample;
				result = or.get();
			}
			if(result != null){
				verboseMessage(e,result);
				return result;
			}
			return or;
		}
	};
	
	static Rule redundance = new Rule("redundant factors",Rule.TRICKY){
		@Override
		public void init(){
			example = "x&y|~x&z|~y&z|~x&q|~y&q=x&y|z|q";
		}
		int fastContains(Expr e,Expr a){//e is what your finding in a
			for(int i = 0;i<a.size();i++){
				if(a.get(i).equalStruct(e)){
					return i;
				}
			}
			return -1;
		}
		class BucketInfo{
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			ExprList flippedVars = new ExprList();
		}
		class Buckets{
			ExprList bucketVar = new ExprList();
			ArrayList<BucketInfo> buckets = new ArrayList<BucketInfo>();
			
			void place(Expr var,int index,Expr flippedPart){
				int i = fastContains(var,bucketVar);
				if(i == -1){
					bucketVar.add(var);
					buckets.add(new BucketInfo());
					i=bucketVar.size()-1;
				}
				
				BucketInfo bf = buckets.get(i);
				bf.indexes.add(index);
				bf.flippedVars.add(flippedPart);
				
			}
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){//this is very hard to explain
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			Expr original = e.copy();
			for(int i = 0;i < or.size();i++){
				And current = And.cast(or.get(i));
				ExprList flipped = new ExprList();//flipped terms
				
				for(int j = 0;j<current.size();j++){
					flipped.add( current.get(j) instanceof Not ? current.get(j).get() : not(current.get(j)) );
				}
				
				ArrayList<Integer> indexes = new ArrayList<Integer>();//need to keep track of indexes we may need to remove later
				ExprList strippedFactors = new ExprList();
				ExprList justFactor = new ExprList();
				for(int j = 0;j < or.size();j++){
					if(j == i || !(or.get(j) instanceof And)) continue;
					int count = 0;//see how many times a flipped factor shows up
					
					int lastIndex = -1;//keep track of the index of the factor
					for(int k = 0;k<flipped.size();k++){
						int temp = fastContains(flipped.get(k),or.get(j));//index of the factor
						lastIndex = temp == -1 ? lastIndex : temp;//only change it if it is not negative one
						if(temp != -1){
							count++;
						}
					}
					if(count == 1){//should only be one factor because DeMorgan "and" becomes "or", but then gets distributed so only one per term
						indexes.add(j);
						And strippedFactor = (And)or.get(j).copy();
						strippedFactor.remove(lastIndex);
						strippedFactors.add(strippedFactor);
						justFactor.add(or.get(j).get(lastIndex));
					}
				}
				
				//grouping into buckets
				
				Buckets buckets = new Buckets();
				
				for(int j = 0;j<justFactor.size();j++){
					buckets.place(strippedFactors.get(j), indexes.get(j), justFactor.get(j));
				}
							
				ArrayList<Integer> toBeRemoved = new ArrayList<Integer>();
				ArrayList<Expr> toBeAdded = new ArrayList<Expr>();
				
				for(int j = 0;j<buckets.bucketVar.size();j++){
					if(buckets.buckets.get(j).flippedVars.equalStruct(flipped)){
						toBeRemoved.addAll(buckets.buckets.get(j).indexes);
						toBeAdded.add(buckets.bucketVar.get(j));
					}
				}
				
				toBeRemoved.sort(Collections.reverseOrder());
				for(int j : toBeRemoved){
					or.remove(j);
				}
				for(Expr expr:toBeAdded){
					or.add(expr.simplify(settings));
				}
			}
			if(!original.equalStruct(or)){
				verboseMessage(original,or);
			}
			return or;
		}
	};
	
	static Rule[] ruleSequence = {
			orContainsOr,
			removeDuplicates,
			nullRule,
			removeFalses,
			complement,
			absorb,
			redundance,
			aloneOr,
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
		if(size() < 2) out+="alone or:";
		for(int i = 0;i<size();i++){
			boolean paren = get(i) instanceof Or;
			if(paren) out+="(";
			out+=get(i);
			if(paren) out+=")";
			if(i!=size()-1) out+="|";
		}
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		boolean state = false;
		for(int i = 0;i<size();i++){
			state |= Math.abs(get(i).convertToFloat(varDefs).real)>0.5;
		}
		double res = state ? 1.0 : 0.0;
		return new ComplexFloat(res,0);
	}
}
