package cas.bool;

import java.util.ArrayList;

import cas.*;
import cas.primitive.*;

public class BoolCompress{
	
	
	public static Func.FuncLoader boolCompressLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			
			Rule group = new Rule("group the factorable components") {
				int termHasPart(Expr term,Expr part) {
					if(term.typeName().equals("and")) {
						for(int i = 0;i < term.size();i++) {
							if(term.get(i).equals(part)) return i;
						}
					}
					
					return -1;
				}
				
				void groupTermsWithSimilarPart(Func inOr,Expr part,CasInfo casInfo) {
					int count = 0;
					
					for(int i = 0;i < inOr.size();i++) {
						Expr term = inOr.get(i);
						if(termHasPart(term,part) != -1) count++;
						if(count == 2)break;
					}
					
					if(count != 2) return;//has enough to continue
					
					Expr portion = or();
					
					for(int i = 0;i < inOr.size();i++) {
						Expr term = inOr.get(i);
						int index = termHasPart(term,part);
						if(index != -1) {
							
							term.remove(index);
							portion.add(And.unCast(term));
							
							inOr.remove(i);
							i--;
						}
					}
					portion.flags.simple = true;
					portion = boolCompress(portion).simplify(casInfo);
					if(portion.typeName().equals("and")) portion.add(part);
					else portion = and(part,portion);
					inOr.add(portion);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Expr inner = e.get();
					
					if(inner.typeName().equals("or")) {
						ArrayList<VarCount> varcounts = new ArrayList<VarCount>();
						inner.countVars(varcounts);
						Func orInner = (Func)inner;
						
						for(int i = 0;i < varcounts.size();i++) {
							
							groupTermsWithSimilarPart(orInner,varcounts.get(i).v,casInfo);
							groupTermsWithSimilarPart(orInner,not(varcounts.get(i).v),casInfo);
							
						}
						
						inner = Or.unCast(inner);
						
					}
					
					if(inner.typeName().equals("and")) {
						int notCount = 0;
						for(int i = 0;i < inner.size();i++) {
							Expr term = inner.get(i);
							if(term.containsType("not")) {
								notCount++;
							}
						}
						if(inner.size()-notCount+1<notCount) {
							Func out = or();
							for(int i = 0;i < inner.size();i++) {
								out.add( not(inner.get(i)).simplify(casInfo) );
							}
							return not(out);
						}
					}
					
					e.set(0, inner);
					return e;
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
				group,
				StandardRules.becomeInner
			},"main sequence");
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return owner.get().convertToFloat(varDefs);
				}
			};
			
		}
		
	};

}
