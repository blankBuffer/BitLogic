package cas.bool;

import java.util.ArrayList;

import cas.*;
import cas.primitive.*;

public class BoolCompress extends Expr{
	private static final long serialVersionUID = 7339019468812566197L;
	
	public BoolCompress() {}//
	public BoolCompress(Expr e) {
		add(e);
	}
	
	static Rule group = new Rule("group the factorable components") {
		private static final long serialVersionUID = 1L;
		
		int termHasPart(Expr term,Expr part) {
			if(term instanceof And) {
				for(int i = 0;i < term.size();i++) {
					if(term.get(i).equals(part)) return i;
				}
			}
			
			return -1;
		}
		
		void groupTermsWithSimilarPart(Or in,Expr part,CasInfo casInfo) {
			int count = 0;
			
			for(int i = 0;i < in.size();i++) {
				Expr term = in.get(i);
				if(termHasPart(term,part) != -1) count++;
				if(count == 2)break;
			}
			
			if(count != 2) return;//has enough to continue
			
			Expr portion = new Or();
			
			for(int i = 0;i < in.size();i++) {
				Expr term = in.get(i);
				int index = termHasPart(term,part);
				if(index != -1) {
					
					term.remove(index);
					portion.add(And.unCast(term));
					
					in.remove(i);
					i--;
				}
			}
			portion.flags.simple = true;
			portion = boolCompress(portion).simplify(casInfo);
			if(portion instanceof And) portion.add(part);
			else portion = and(part,portion);
			in.add(portion);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Expr inner = e.get();
			
			if(inner instanceof Or) {
				ArrayList<VarCount> varcounts = new ArrayList<VarCount>();
				inner.countVars(varcounts);
				
				for(int i = 0;i < varcounts.size();i++) {
					
					groupTermsWithSimilarPart((Or)inner,varcounts.get(i).v,casInfo);
					groupTermsWithSimilarPart((Or)inner,not(varcounts.get(i).v),casInfo);
					
				}
				
				inner = Or.unCast(inner);
				
			}
			
			if(inner instanceof And) {
				int notCount = 0;
				for(int i = 0;i < inner.size();i++) {
					Expr term = inner.get(i);
					if(term.containsType("not")) {
						notCount++;
					}
				}
				if(inner.size()-notCount+1<notCount) {
					Or out = new Or();
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

	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				group,
				StandardRules.becomeInner
		);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public String help() {
		return "boolCompress(x) is the boolean expression compressor\n"
				+ "examples\n"
				+ "boolCompress(x&a|x&b)->x&(a|b)\n"
				+ "boolCompress(~x&~y&~r&w|~x&~r&~y&k)->~(y|~w&~k|r|x)";
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat(varDefs);
	}

	@Override
	public String typeName() {
		return "boolCompress";
	}

}
