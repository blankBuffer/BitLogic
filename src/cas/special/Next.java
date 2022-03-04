package cas.special;

import cas.*;
import cas.primitive.*;

/*
 * tries to predict the next element in a sequence
 */
public class Next extends Expr{
	
	private static final long serialVersionUID = 7861200724185548138L;
	
	public Next() {}//
	
	public Next(Sequence sequence,Num steps) {
		add(sequence);
		add(steps);
	}
	
	public Sequence getSequence() {
		return (Sequence)get();
	}
	
	public Num getNum() {//number of new terms we want to generate
		return (Num)get(1);
	}
	
	static Rule isFibonacci = new Rule("is the fibonacci sequence",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			if(s.size()>=4) {
				Expr first = s.get(0);
				Expr second = s.get(1);
				for(int i = 2;i<s.size();i++) {
					Expr expected = sum(first,second).simplify(settings);
					if(s.get(i).equals(expected)) {
						first = second;
						second = expected;
						
					}else return next;
				}
				Sequence newSeq = new Sequence();
				for(int i = 0;i<next.getNum().realValue.intValue();i++) {
					Expr expected = sum(first,second).simplify(settings);
					newSeq.add(expected);
					first = second;
					second = expected;
				}
				return newSeq;
			}
			return next;
		}
		
	};
	
	static boolean USED_GEO = false;
	static Rule geometric = new Rule("is a geometric series",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			if(USED_GEO) return e;
			Next next = (Next)e;
			Sequence s = next.getSequence();
			if(s.size()>=4) {
				Sequence ratioSequence = new Sequence();
				for(int i = 1;i<s.size();i++) {
					if(s.get(i-1).equals(Num.ZERO)) return next;//cant divide by zero
					
					Expr ratio = div(s.get(i),s.get(i-1)).simplify(settings);
					ratioSequence.add(ratio);
				}
				USED_GEO = true;
				Expr nextRatios = next(ratioSequence,next.getNum()).simplify(settings);
				USED_GEO = false;
				
				if(nextRatios instanceof Next) return next;
				
				Sequence newSeq = new Sequence();
				Expr last = s.get(s.size()-1);
				for(int i = 0;i<next.getNum().realValue.intValue();i++) {
					last = prod(last,nextRatios.get(i)).simplify(settings);
					newSeq.add(last);
				}
				return newSeq;
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
			
			Expr start = s.get(0);
			outer:for(int loopSize = 1;loopSize<s.size()/2+1;loopSize++) {
				if(s.get(loopSize).equals(start)) {
					for(int i = loopSize;i<s.size();i++) {
						if(!s.get(i).equals(s.get(i%loopSize))) continue outer;
					}
					Sequence newSeq = new Sequence();
					for(int i = 0;i<next.getNum().realValue.intValue();i++) newSeq.add(s.get( (s.size()+i)%loopSize ));
					return newSeq;
				}
			}	
			
			return next;
		}
	};
	
	static boolean USED_STEP = false;
	static Rule steppedPattern = new Rule("stepped/interlaced patterns",Rule.EASY) {//basically like sequences interlaced
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			if(USED_STEP || USED_GEO || USED_COEF) return e;
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			outer:for(int step = 2;step<=6 && s.size()/step>=3;step++) {//limit to 6 and number of cycles >= 3
				//System.out.println("str:"+step+" "+e);
				
				Sequence[] subSequences = new Sequence[step];
				for(int i = 0;i<subSequences.length;i++) subSequences[i] = new Sequence();
				
				for(int i = 0;i<s.size();i++) {
					subSequences[i%step].add(s.get(i));
				}
				
				//for(int i = 0;i<subSequences.length;i++) System.out.println(subSequences[i]);
				
				int startingPoint = s.size()%step;
				int remain = next.getNum().realValue.intValue();
				
				int maxChunkSize = remain/step;
				//System.out.println("max chunk size:"+maxChunkSize+" remain:"+remain);
				int[] chunkSizes = new int[step];
				
				for(int i = startingPoint;remain>=0;i++) {
					int j = i%step;
					
					if(remain>=maxChunkSize) {
						chunkSizes[j] += maxChunkSize;
						remain-=maxChunkSize;
					}
					else {
						chunkSizes[j]++;
						remain--;
					}
					
				}
				
				//for(int i = 0;i<step;i++) System.out.println("cz:"+chunkSizes[i]);
				
				Expr[] contSequences = new Expr[step];
				for(int i = 0;i<contSequences.length;i++) {
					USED_STEP = true;
					contSequences[i] = next(subSequences[i],num(chunkSizes[i])).simplify(settings);
					USED_STEP = false;
					if(contSequences[i] instanceof Next) continue outer;
				}
				
				remain = next.getNum().realValue.intValue();
				
				int[] indexTracker = new int[step];
				
				Sequence out = new Sequence();
				for(int i = startingPoint;i<startingPoint+remain;i++) {
					int j = i%step;
					if(chunkSizes[j]>0) {
						out.add(contSequences[j].get(indexTracker[j]));
						indexTracker[j]++;
						chunkSizes[j]--;
					}
				}
				System.out.println(step);
				return out;
				
			}
			
			return next;
		}
			
	};
	
	static boolean USED_COEF = false;
	static Rule coefPredict = new Rule("predict seperatly the coefficinet and the variable component",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			if(USED_COEF) return e;
			
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
			USED_COEF = true;
			Expr nextCoefs = next(coefs,next.getNum()).simplify(settings);
			Expr nextExprs = next(exprs,next.getNum()).simplify(settings);
			USED_COEF = false;
			
			if(nextCoefs instanceof Next || nextExprs instanceof Next) return next;
			
			Sequence newSeq = new Sequence();
			for(int i = 0;i<next.getNum().realValue.intValue();i++) {
				newSeq.add(prod(nextCoefs.get(i),nextExprs.get(i)).simplify(settings));
			}
			return newSeq;
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
				
				Expr nextNumers = next(numerS,next.getNum()).simplify(settings);
				Expr nextDenoms = next(denomS,next.getNum()).simplify(settings);
				
				if(nextNumers instanceof Next || nextDenoms instanceof Next) return next;
				
				Sequence newSeq = new Sequence();
				for(int i = 0;i<next.getNum().realValue.intValue();i++) {
					newSeq.add(div(nextNumers.get(i),nextDenoms.get(i)).simplify(settings));
				}
				return newSeq;
				
			}
			
			return next;
		}
	};
	
	static Rule powPredict = new Rule("predict next power",Rule.EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			if(s.get(0) instanceof Power || s.get(1) instanceof Power || s.get(s.size()-1) instanceof Power || s.get(s.size()-2) instanceof Power) {
				
				Sequence baseS = new Sequence();
				Sequence expoS = new Sequence();
				
				for(int i = 0;i<s.size();i++) {
					Power pow = Power.cast(s.get(i));
					
					baseS.add(pow.getBase());
					expoS.add(pow.getExpo());
				}
				
				Expr nextBases = next(baseS,next.getNum()).simplify(settings);
				Expr nextExpos = next(expoS,next.getNum()).simplify(settings);
				
				if(nextBases instanceof Next || nextExpos instanceof Next) return next;
				
				Sequence newSeq = new Sequence();
				for(int i = 0;i<next.getNum().realValue.intValue();i++) {
					newSeq.add(pow(nextBases.get(i),nextExpos.get(i)).simplify(settings));
				}
				return newSeq;
				
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
		
		Sequence getNext(Sequence s,Settings settings,int size,int num) {
			if(size == 0) return null;
			Sequence newSeq = new Sequence();
			if(allSame(s)) {
				for(int i = 0;i<num;i++) {
					newSeq.add(s.get(0));
				}
				return newSeq;
			}
			Sequence difference = getDifferenceSequence(s,settings);
			
			Sequence deltas = getNext(difference,settings,size-1,num);
			if(deltas == null) return null;
			
			newSeq.add( sum(s.get(s.size()-1),deltas.get(0)).simplify(settings) );
			for(int i = 1;newSeq.size()<num;i++) {
				newSeq.add( sum(newSeq.get(newSeq.size()-1),deltas.get(i)).simplify(settings) );
			}
			
			return newSeq;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			Expr expected = getNext(s,settings,s.size()/2+1,next.getNum().realValue.intValue());
			
			if(expected != null) {
				return expected;
			}
			
			return next;
		}
	};
	
	static Rule nothingCase = new Rule("nothing case for sequence prediction",Rule.VERY_EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			if(next.getNum().equals(Num.ZERO)) return new Sequence();
			else if(next.getSequence().size() == 1) {
				Sequence out = new Sequence();
				for(int i = 0;i<next.getNum().realValue.intValue();i++) {
					out.add(next.getSequence().get());
				}
				return out;
			}
			return next;
		}
	};
	
	static Rule funcPredict = new Rule("expression layout prediction",Rule.TRICKY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Next next = (Next)e;
			Sequence s = next.getSequence();
			
			String firstTypeName = s.get(0).typeName();
			if(SimpleFuncs.isFunc(firstTypeName) && s.get(0).size() == 1) {
				for(int i = 1;i<s.size();i++) if(!(s.get(i).typeName().equals(firstTypeName)&& s.get(i).size() == 1)) return next;
				Sequence subSequence = new Sequence();
				for(int i = 0;i<s.size();i++) subSequence.add(s.get(i).get());
				
				Expr expectedInner = next(subSequence,next.getNum()).simplify(settings);
				
				if(expectedInner instanceof Next) return next;
				
				for(int i = 0;i<expectedInner.size();i++) {
					expectedInner.set(i, SimpleFuncs.getFuncByName(firstTypeName, expectedInner.get(i)) );
				}
				return expectedInner.simplify(settings);
			}
			return next;
		}
	};
	
	static Sequence ruleSequence;
	
	public static void loadRules() {
		ruleSequence = sequence(
				nothingCase,
				polynomial,
				isFibonacci,
				looping,
				geometric,
				fracPredict,
				powPredict,
				funcPredict,
				coefPredict,
				steppedPattern
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