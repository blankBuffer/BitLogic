package cas.bool;

import java.util.ArrayList;
import java.util.Collections;

import cas.*;
import cas.primitive.*;


public class Or extends Expr{
	
	private static final long serialVersionUID = 5003710279364491787L;

	public Or(){
		commutative = true;
	}
	
	static Rule orContainsOr = new Rule("or contains or",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			for(int i = 0;i<or.size();i++){
				if(or.get(i) instanceof Or){
					Or subOr = (Or)or.get(i);
					or.remove(i);
					i--;
					for(int j = 0;j<subOr.size();j++){
						or.add(subOr.get(j));
					}
				}
			}
			return or;
		}
		
	};
	
	static Rule nullRule = new Rule("null rule",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			for(int i = 0;i<or.size();i++){
				if(or.get(i).equals(BoolState.TRUE)){
					Expr result = bool(true);
					return result;
				}
			}
			return or;
		}
	};
	
	static Rule removeFalses = new Rule("remove falses",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			for(int i = 0;i<or.size();i++){
				if(or.get(i).equals(BoolState.FALSE)){
					or.remove(i);
					i--;
				}
			}
			return or;
		}
	};
	
	static Rule complement = new Rule("has complement",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			for(int i = 0;i<or.size();i++){
				Expr current = or.get(i);
				Expr complement = not(current).simplify(casInfo);
				
				if(complement instanceof Or){//trickier case a&b|~a|~b=true
					boolean hasAll = true;
					for(int j = 0;j<complement.size();j++){
						boolean found = false;
						Expr toFind = complement.get(j);
						for(int k = 0;k<or.size();k++){
							if(k == i) continue;
							
							if(or.get(k).equals(toFind)){
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
						return result;
					}
				}else{
					for(int j = i+1;j<or.size();j++){
						Expr other = or.get(j);
						
						if(other.equals(complement)){
							Expr result = bool(true);
							return result;
						}
					}
				}
				
			}
			return or;
		}
	};
	
	static Rule removeDuplicates = new Rule("remove duplicates",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			for(int i = 0;i<or.size();i++){
				Expr current = or.get(i);
				
				for(int j = i+1;j<or.size();j++){
					Expr other = or.get(j);
					
					if(other.equals(current)){
						or.remove(i);
						i--;
					}
				}
				
			}
			return or;
		}
	};
	
	static Rule absorb = new Rule("absorption",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
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
								if(current.get(k).equals(other.get(l))){
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
							if(j<i) i--;
							j--;
						}
						
					}
				}
			}
			return or;
		}
	};
	
	static Rule aloneOr = new Rule("or has one element",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			Expr result = null;
			if(or.size() == 0){
				result = bool(false);
			}else if(or.size() == 1){
				result = or.get();
			}
			if(result != null){
				return result;
			}
			return or;
		}
	};
	
	private static int fastContains(Expr e,Expr a){//e is what your finding in a
		for(int i = 0;i<a.size();i++){
			if(a.get(i).equals(e)){
				return i;
			}
		}
		return -1;
	}
	static Rule redundance = new Rule("redundant factors",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

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
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){//this is very hard to explain
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
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
					if(buckets.buckets.get(j).flippedVars.equals(flipped)){
						toBeRemoved.addAll(buckets.buckets.get(j).indexes);
						toBeAdded.add(buckets.bucketVar.get(j));
					}
				}
				
				toBeRemoved.sort(Collections.reverseOrder());
				for(int j : toBeRemoved){
					or.remove(j);
				}
				for(Expr expr:toBeAdded){
					or.add(expr.simplify(casInfo));
				}
			}
			return or;
		}
	};
	
	static Rule consensus = new Rule("consensus rule",Rule.CHALLENGING){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Or or = null;
			if(e instanceof Or){
				or = (Or)e;
			}else{
				return e;
			}
			if(or.size()>=3){
				
				outer:for(int i = 0;i<or.size();i++){
					if(!(or.get(i) instanceof And && or.get(i).size()==2)) continue;
					
					And current = (And)or.get(i);
					Expr a = current.get(0),b = current.get(1);
					Expr notA = a instanceof Not ? a.get() : not(a),notB = b instanceof Not ? b.get() : not(b);
					
					for(int j = i+1;j < or.size();j++){
						if(!(or.get(j) instanceof And && or.get(j).size()==2)) continue;
						And other = (And)or.get(j);
						int test = 1-fastContains(notA,other);
						if(test != 2){
							Expr redundant = and(b,other.get(test));
							for(int k = 0;k<or.size();k++){
								if(i == k | j==k) continue;
								if(or.get(k).equals(redundant)){
									or.remove(k);
									i--;
									continue outer;
								}
							}
						}
						test = 1-fastContains(notB,other);
						if(test != 2){
							Expr redundant = and(a,other.get(test));
							for(int k = 0;k<or.size();k++){
								if(i == k | j==k) continue;
								if(or.get(k).equals(redundant)){
									or.remove(k);
									i--;
									continue outer;
								}
							}
						}
						
					}
					
					
				}
				
			}
			return or;
		}
	};
	
	static Rule ruleCombination = new Rule("rule set",Rule.CHALLENGING){
		private static final long serialVersionUID = 1L;

		ExprList allFactorableVars(Or or){
			ArrayList<VarCount> varcounts = new ArrayList<VarCount>();
			or.countVars(varcounts);
			ExprList allFactorableVars = new ExprList();
			for(int i = 0;i<varcounts.size();i++){
				VarCount vc = varcounts.get(i);
				if(vc.count>=2){
					allFactorableVars.add(vc.v);
					allFactorableVars.add(not(vc.v));
				}
			}
			return allFactorableVars;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			//now try with factored components
			
			if(e instanceof Or){
				Or or = (Or)e;
				ExprList allFactorableVars = allFactorableVars(or);
				ExprList used = new ExprList();
				for(int i = 0;i<allFactorableVars.size();i++){
					Expr var = allFactorableVars.get(i);
					if(used.contains(var)) continue;
					used.add(var);
					
					Or factoredOutOr = new Or();
					for(int j = 0;j<or.size();j++){
						if(!(or.get(j) instanceof And)) continue;
						And currentAnd = (And)or.get(j);
						
						
						int index = fastContains(var,currentAnd);
						
						if(index != -1){
							And currentAndFactoredOut = (And)currentAnd.copy();
							currentAndFactoredOut.remove(index);
							
							or.remove(j);
							j--;
							
							factoredOutOr.add(currentAndFactoredOut.simplify(casInfo));
							
						}
						
					}
					Or factoredOutOrNew = Or.cast(this.applyRuleToExpr(factoredOutOr.copy(), casInfo) );
					if(!factoredOutOrNew.equals(factoredOutOr)){//re calculate to waste lest time
						i=-1;
						allFactorableVars = allFactorableVars(or);
					}
					for(int j = 0;j<factoredOutOrNew.size();j++){
						And current = And.cast(factoredOutOrNew.get(j));
						current.add(var);
						or.add(current.simplify(casInfo));
					}
				}
				
			}
			
			{//apply plain
				e = complement.applyRuleToExpr(e, casInfo);
				e = redundance.applyRuleToExpr(e, casInfo);
				e = consensus.applyRuleToExpr(e, casInfo);
			}
			
			return e;
		}
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				orContainsOr,
				removeDuplicates,
				nullRule,
				removeFalses,
				absorb,
				ruleCombination,
				aloneOr	
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
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
	
	public static Or cast(Expr e){
		if(e instanceof Or){
			return (Or)e;
		}
		return or(e);
	}
	
	@Override
	public String typeName() {
		return "or";
	}
}
