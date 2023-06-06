package cas.bool;

import java.util.ArrayList;
import java.util.Collections;

import cas.*;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

import static cas.Cas.*;


public class Or{
	
	public static Func.FuncLoader orLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			owner.behavior.helpMessage = "Boolean algebraic or.\n"
					+ "Example x|x&y returns x\n"
					+ "Example ~a&c|~b&c|a&b|d returns a&b|c|d";
			
			Rule orContainsOr = new Rule("or contains or"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					for(int i = 0;i<or.size();i++){
						if(or.get(i).isType("or")){
							Func subOr = (Func)or.get(i);
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
			
			Rule nullRule = new Rule("null rule"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					for(int i = 0;i<or.size();i++){
						if(or.get(i).equals(BoolState.TRUE)){
							Expr result = bool(true);
							return result;
						}
					}
					return or;
				}
			};
			
			Rule removeFalses = new Rule("remove falses"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					for(int i = 0;i<or.size();i++){
						if(or.get(i).equals(BoolState.FALSE)){
							or.remove(i);
							i--;
						}
					}
					return or;
				}
			};
			
			Rule complement = new Rule("has complement"){//a|~a -> true , a&b|~a|~b -> true
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					for(int i = 0;i<or.size();i++){
						Expr current = or.get(i);
						Expr complement = not(current).simplify(casInfo);
						
						if(complement.isType("or")){//trickier case a&b|~a|~b=true
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
			
			Rule removeDuplicates = new Rule("remove duplicates"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					for(int i = 0;i<or.size();i++){
						Expr current = or.get(i);
						
						for(int j = i+1;j<or.size();j++){
							Expr other = or.get(j);
							
							if(other.equals(current)){
								or.remove(j);
								j--;
							}
						}
						
					}
					return or;
				}
			};
			
			Rule absorb = new Rule("absorption"){// x|x&y -> x
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					for(int i = 0;i<or.size();i++){
						Func currentAnd = And.cast(or.get(i));
						for(int j = 0;j<or.size();j++){
							if(j == i) continue;
							if(or.get(j).isType("and")){
								Func otherAnd = (Func)or.get(j);
								
								boolean hasAll = true;
								for(int k = 0;k<currentAnd.size();k++){
									boolean found = false;
									
									for(int l = k;l<otherAnd.size();l++){
										if(currentAnd.get(k).equals(otherAnd.get(l))){
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
			
			Rule aloneOr = new Rule("or has one element"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
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
			
			Rule consensus = new Rule("consensus rule"){//x&y | y&z | ~x&z -> x&y | ~x&z
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					if(or.size()>=3){
						
						outer:for(int i = 0;i<or.size();i++){
							if(!(or.get(i).isType("and") && or.get(i).size()==2)) continue;
							
							Func currentAnd = (Func)or.get(i);
							Expr a = currentAnd.get(0),b = currentAnd.get(1);
							Expr notA = a.isType("not") ? a.get() : not(a),notB = b.isType("not") ? b.get() : not(b);
							
							for(int j = i+1;j < or.size();j++){
								if(!(or.get(j).isType("and") && or.get(j).size()==2)) continue;
								Func otherAnd = (Func)or.get(j);
								int test = 1-fastContains(notA,otherAnd);
								if(test != 2){
									Expr redundant = and(b,otherAnd.get(test));
									for(int k = 0;k<or.size();k++){
										if(i == k | j==k) continue;
										if(or.get(k).equals(redundant)){
											or.remove(k);
											i--;
											continue outer;
										}
									}
								}
								test = 1-fastContains(notB,otherAnd);
								if(test != 2){
									Expr redundant = and(a,otherAnd.get(test));
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
			
			/*
			 * 
			 * a|~a&b -> a|b , ~a&c|~b&c|a&b|d -> a&b|c|d
			 * the reasoning for the second example is because ~a&c|~b&c|a&b|d = c&(~a|~b)|a&b|d = c&~(a&b)|a&b|d = (c&~(a&b)|a&b)|d = (c|a&b)|d = a&b|c|d
			 * 
			 * more complex examples
			 * 	 d&k&~(a&b&c)|a&b&c expands to ~a&d&k|~b&d&k|~c&d&k|a&b&c which simplifies down to d&k|a&b&c
			 * 
			 * 	d&~(a&b&c)|a&b&c expands to ~a&d|~b&d|~c&d|a&b&c which simplifies down to d|a&b&c
			 * 
			 * this algorithm is not very efficient
			 * 
			 */
			Rule redundance = new Rule("redundance"){
				
				Func negElems(Expr orTerm){//returns expr set
					Func negatedElemsSet = exprSet();
					if(orTerm.isType("and")){
						for(int j = 0;j<orTerm.size();j++){
							negatedElemsSet.add(not(orTerm.get(j)).simplify(CasInfo.normal));
						}
					}else negatedElemsSet.add(not(orTerm).simplify(CasInfo.normal));
					return negatedElemsSet;
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					
					outer:for(int i = 0;i<or.size();i++){
						Expr orTerm = or.get(i);
						Func negatedElemsSet = negElems(orTerm);//negate all elements of that term
						
						for(int j = 0;j<or.size();j++){
							if(j == i) continue;
							
							Func castedTermAnd = And.cast(or.get(j));
							
							int indexOfFactor = fastContains(negatedElemsSet.get(0),castedTermAnd);//find a factor
							if(indexOfFactor == -1) continue;
							Func strippedAnd = (Func)castedTermAnd.copy();
							strippedAnd.remove(indexOfFactor);
							
							Func negatedElemsSetCopy = (Func) negatedElemsSet.copy();
							negatedElemsSetCopy.remove(0);
							
							if(negatedElemsSetCopy.size() == 0){//simple case
								or.set(j,strippedAnd.simplify(casInfo));
								if(j < i) i = -1;//changed sequence needs restart
								continue outer;
							}else{//find the rest of them
								ArrayList<Integer> toBeRemoved = new ArrayList<Integer>();
								toBeRemoved.add(j);
								while(negatedElemsSetCopy.size() > 0){
									Expr searchFor = negatedElemsSetCopy.get(0);
									
									boolean found = false;
									for(int k = 0;k<or.size();k++){
										if(k == i || k == j) continue;
										
										Func castedTerm2And = And.cast(or.get(k));
										
										indexOfFactor = fastContains(searchFor,castedTerm2And);//find a factor
										
										if(indexOfFactor != -1){
											Func otherStrippedAnd = (Func) castedTerm2And.copy();
											otherStrippedAnd.remove(indexOfFactor);
											if(otherStrippedAnd.equals(strippedAnd)){
												toBeRemoved.add(k);
												found = true;
												break;
											}
											
										}
										
										
									}
									if(found) negatedElemsSetCopy.remove(0);
									else continue outer;	
								}
								
								Collections.sort(toBeRemoved);
								
								boolean needsReset = false;
								for(int k = toBeRemoved.size()-1;k>=0;k--){
									or.remove(toBeRemoved.get(k));
									if(k < i) needsReset = true;
								}
								
								or.add(strippedAnd.simplify(casInfo));
								if(needsReset) i = -1;//changed sequence needs restart
								continue outer;
								
								
							}
							
							
						}
						
						
					}
					
					
					return or;
				}
			};
			
			/*
			 * go through each variable
			 */
			Rule factorGroupVar = new Rule("factor each variable"){
				void extractVars(Expr e,Func outSet){
					if(e.isType("var") && fastContains(e,outSet) == -1){
						outSet.add(e);
					}else if(e.isType("not") && e.get().isType("var") && fastContains(e,outSet) == -1){
						outSet.add(e);
					}else{
						for(int i = 0;i < e.size();i++){
							extractVars(e.get(i),outSet);
						}
					}
					
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func or = (Func)e;
					
					//capture all possible variables
					Func varsSet = exprSet();
					extractVars(or,varsSet);
					//
					
					for(int i = 0;i < varsSet.size();i++){
						Expr var = varsSet.get(i);
						
						Expr recursiveOr = or();
						Algorithms.IndexSet toBeRemoved = new Algorithms.IndexSet();
						for(int j = or.size()-1;j >= 0;j--){
							Expr term = or.get(j);
							if(term.isType("and")){
								int varIndex = fastContains(var,term);
								if(varIndex != -1){
									Expr termCopy = term.copy();
									termCopy.remove(varIndex);
									toBeRemoved.ints.add(j);
									
									recursiveOr.add(termCopy);
								}
							}
						}
						
						if(recursiveOr.size() > 1){
							for(int j = toBeRemoved.ints.size()-1;j>=0;j--){
								or.remove(j);
							}
							
							recursiveOr = recursiveOr.simplify(casInfo);
							if(recursiveOr.isType("or")){
								for(int j = 0;j<recursiveOr.size();j++){
									or.add(and(recursiveOr.get(j),var).simplify(casInfo));
								}
							}else{
								or.add(and(recursiveOr,var).simplify(casInfo));
							}
						}
						
						
					}
					
					return or;
				}
			};
			
			Rule subSequence = new Rule(new Rule[]{orContainsOr,// (x|y)|z -> x|y|z
					complement,// a|~a -> true , a&b|~a|~b -> true
					redundance,// a|~a&b -> a|b , ~a&c|~b&c|a&b|d -> a&b|c|d
					removeDuplicates,// x|x -> x
					nullRule,// x|true -> true
					removeFalses,// false|x -> x
					absorb,// x|x&y -> x
					consensus,// x&y | y&z | ~x&z -> x&y | ~x&z
					aloneOr,
			},"sub sequence");
			
			owner.behavior.commutative = true;
			owner.behavior.rule = new Rule(new Rule[]{
				subSequence,
				factorGroupVar,
				//again to ensure no more steps
				subSequence,
			},"main sequence");
			
			owner.behavior.toStringMethod = new Func.ToString() {
				@Override
				public String generateString(Func owner) {
					String out = "";
					if(owner.size() < 2) out+="alone or:";
					for(int i = 0;i<owner.size();i++){
						boolean paren = owner.get(i).isType("or");
						if(paren) out+="(";
						out+=owner.get(i);
						if(paren) out+=")";
						if(i!=owner.size()-1) out+="|";
					}
					return out;
				}
			};
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					boolean state = false;
					for(int i = 0;i<owner.size();i++){
						state |= Math.abs(owner.get(i).convertToFloat(varDefs).real)>0.5;
					}
					double res = state ? 1.0 : 0.0;
					return new ComplexFloat(res,0);
				}
			};
			
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
	
	public static Func cast(Expr e){
		if(e.isType("or")){
			return (Func)e;
		}
		return Cas.or(e);
	}//returns or

	public static Expr unCast(Expr e) {
		if(e.isType("or") && e.size() == 1) {
			return e.get();
		}
		return e;
	}
}
