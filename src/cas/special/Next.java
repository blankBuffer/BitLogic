package cas.special;

import cas.*;
import cas.primitive.*;
import cas.bool.*;

/*
 * tries to predict the next element in a sequence
 */
public class Next extends Expr{
	
	private static final long serialVersionUID = 7861200724185548138L;
	
	public Next() {}//
	
	public Next(Sequence sequence) {
		add(sequence);
	}
	
	public Sequence getSequence() {
		return (Sequence)get();
	}
	
	static Rule isFibonacci = new Rule("is the fibonacci sequence",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			if(s.size()>=3) {
				Expr first = s.get(0);
				Expr second = s.get(1);
				for(int i = 2;i<s.size();i++) {
					Expr expected = sum(first,second).simplify(settings);
					if(s.get(i).equals(expected)) {
						first = second;
						second = s.get(i);
						
					}else return next;
				}	
				return sum(first,second).simplify(settings);
			}
			return next;
		}
		
	};
	
	static Rule geometric = new Rule("is a geometric series",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			if(s.size()>=3) {
				Expr ratio = div(s.get(1),s.get(0)).simplify(settings);
				
				for(int i = 1;i<s.size();i++) {
					Expr otherRatio = div(s.get(i),s.get(i-1)).simplify(settings);
					
					if(!otherRatio.equals(ratio)) return next;
					
				}
				return prod(s.get(s.size()-1),ratio).simplify(settings);
			}
			return next;
		}
	};
	
	static Rule looping = new Rule("sequence is a loop",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			if(s.size()>1) {
				Expr start = s.get(0);
				
				outer:for(int loopSize = 1;loopSize<s.size();loopSize++) {
					if(s.get(loopSize).equals(start)) {
						
						for(int i = loopSize;i<s.size();i++) {
							if(!s.get(i).equals(s.get(i%loopSize))) continue outer;
						}
						return s.get( s.size()%loopSize );
						
					}
				}
				
			}
			
			return next;
		}
	};
	
	static Rule occilation2 = new Rule("occilation of size 2",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			if(s.size()>=3) {
				boolean firstComp = ((BoolState)eval(equLess(s.get(0),s.get(1))).simplify(settings)).state;
				boolean secondComp = ((BoolState)eval(equLess(s.get(1),s.get(2))).simplify(settings)).state;
				
				if(firstComp != secondComp) {
					if(s.size()%2 == 0) {
						Sequence oddTerms = new Sequence();
						for(int i = 0;i<s.size();i+=2) oddTerms.add(s.get(i));
						
						return next(oddTerms).simplify(settings);
						
					}//else
					
					Sequence evenTerms = new Sequence();
					for(int i = 1;i<s.size();i+=2) evenTerms.add(s.get(i));
					
					return next(evenTerms).simplify(settings);
				}
				
			}
			
			return next;
		}
			
	};
	
	static Rule coefPredict = new Rule("predict seperatly the coefficinet and the variable component",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			if(!s.containsVars()) return next;
			
			Sequence coefs = new Sequence();
			Sequence exprs = new Sequence();
			
			for(int i = 0;i<s.size();i++) {
				Sequence part = seperateCoef(s.get(i));
				
				coefs.add(part.get(0));
				exprs.add(part.get(1));
			}
			
			Expr nextCoef = next(coefs).simplify(settings);
			Expr nextExpr = next(exprs).simplify(settings);
			
			return prod(nextCoef,nextExpr).simplify(settings);
		}
	};
	
	static Rule fracPredict = new Rule("predict next fraction",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			if(s.get(0) instanceof Div || s.get(1) instanceof Div) {
				
				Sequence numerS = new Sequence();
				Sequence denomS = new Sequence();
				
				for(int i = 0;i<s.size();i++) {
					Div div = Div.cast(s.get(i));
					
					numerS.add(div.getNumer());
					denomS.add(div.getDenom());
				}
				
				Expr nextNumer = next(numerS).simplify(settings);
				Expr nextDenom = next(denomS).simplify(settings);
				
				return div(nextNumer,nextDenom).simplify(settings);
				
			}
			
			return next;
		}
	};
	
	static Rule polynomial = new Rule("is a polynomial sequence",Rule.TRICKY) {
		private static final long serialVersionUID = 1L;
		
		boolean allSame(Sequence s) {
			for(int i = 1;i<s.size();i++) {
				if(!s.get(i).equals(s.get(0))) return false;
			}
			return true;
		}
		
		Sequence getDifferenceSequence(Sequence original,Settings settings) {
			Sequence newSequence = new Sequence();
			for(int i = 0;i<original.size()-1;i++) {
				newSequence.add( sub(original.get(i+1),original.get(i)).simplify(settings) );
			}
			return newSequence;
		}
		
		Expr getNext(Sequence s,Settings settings) {
			if(allSame(s)) {
				return s.get(0);
			}
			
			Sequence difference = getDifferenceSequence(s,settings);
			
			Expr n = getNext(difference,settings);
			
			
			return sum(s.get(s.size()-1),n).simplify(settings);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			return getNext(s,settings);
		}
	};
	
	static Sequence ruleSequence;
	
	public static void loadRules() {
		ruleSequence = sequence(
				isFibonacci,
				geometric,
				looping,
				occilation2,
				coefPredict,
				fracPredict,
				polynomial
		);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

}
