package cas.special;

import cas.*;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.primitive.*;

/*
 * tries to predict the next elements in a sequence based on the pattern provided
 * it is based on a few common patterns
 */
public class Next extends Expr{
	
	public Next() {}//
	
	public Next(Func sequence,Num steps) {
		add(sequence);
		add(steps);
	}
	
	public Func getSequence() {
		return (Func)get();
	}
	
	public Num getNum() {//number of new terms we want to generate
		return (Num)get(1);
	}
	
	//is the sequence the standard fibonacci
	static Rule isFibonacci = new Rule("is the fibonacci sequence") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Next next = (Next)e;
			Func seq = next.getSequence();
			
			if(seq.size()>=4) {
				Expr first = seq.get(0);
				Expr second = seq.get(1);
				for(int i = 2;i<seq.size();i++) {
					Expr expected = sum(first,second).simplify(casInfo);
					if(seq.get(i).equals(expected)) {
						first = second;
						second = expected;
						
					}else return next;
				}
				Func newSeq = sequence();
				for(int i = 0;i<next.getNum().getRealValue().intValue();i++) {
					Expr expected = sum(first,second).simplify(casInfo);
					newSeq.add(expected);
					first = second;
					second = expected;
				}
				return newSeq;
			}
			return next;
		}
		
	};
	
	/*
	 * finds the pattern by checking the ratio pattern
	 * for example if the sequence was 2,4,8,16,32 it would calculate the ration sequence to be
	 * 2,2,2,2 and then predict the next ratios to continue the sequence
	 * 
	 * this can also predict factorial sequence
	 */
	static boolean USED_GEO = false;
	static Rule geometric = new Rule("is a geometric series") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(USED_GEO) return e;
			Next next = (Next)e;
			Func seq = next.getSequence();
			if(seq.size()>=4) {
				Func ratioSequence = sequence();
				for(int i = 1;i<seq.size();i++) {
					if(seq.get(i-1).equals(Num.ZERO)) return next;//cant divide by zero
					
					Expr ratio = div(seq.get(i),seq.get(i-1)).simplify(casInfo);
					ratioSequence.add(ratio);
				}
				USED_GEO = true;
				Expr nextRatios = next(ratioSequence,next.getNum()).simplify(casInfo);
				USED_GEO = false;
				
				if(nextRatios instanceof Next) return next;
				
				Func newSeq = sequence();
				Expr last = seq.get(seq.size()-1);
				for(int i = 0;i<next.getNum().getRealValue().intValue();i++) {
					last = prod(last,nextRatios.get(i)).simplify(casInfo);
					newSeq.add(last);
				}
				return newSeq;
			}
			return next;
		}
	};
	
	/*
	 * sequence loops like 1,2,3,4,1,2,3,4,1,2,3,4 etc
	 */
	static Rule looping = new Rule("sequence is a loop") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Next next = (Next)e;
			Func seq = next.getSequence();
			
			Expr start = seq.get(0);
			outer:for(int loopSize = 1;loopSize<seq.size()/2+1;loopSize++) {
				if(seq.get(loopSize).equals(start)) {
					for(int i = loopSize;i<seq.size();i++) {
						if(!seq.get(i).equals(seq.get(i%loopSize))) continue outer;
					}
					Func newSeq = sequence();
					for(int i = 0;i<next.getNum().getRealValue().intValue();i++) newSeq.add(seq.get( (seq.size()+i)%loopSize ));
					return newSeq;
				}
			}	
			
			return next;
		}
	};
	
	/*
	 * patterns are combine together
	 * for example with the sequence 10,1,9,2,8,3,7,4 has a step of 2 for each sub pattern
	 * the pattern in the even indexes gives 10,9,8,7 and in the odd 1,2,3,4
	 * it can detect up to 6 sequences 'interlaced' or a step of 6.
	 */
	static boolean USED_STEP = false;
	static Rule steppedPattern = new Rule("stepped/interlaced patterns") {//basically like sequences interlaced
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(USED_STEP || USED_GEO || USED_COEF) return e;
			Next next = (Next)e;
			Func seq = next.getSequence();
			
			outer:for(int step = 2;step<=6 && seq.size()/step>=3;step++) {//limit to 6 and number of cycles >= 3
				//System.out.println("str:"+step+" "+e);
				
				Func[] subSequences = new Func[step];
				for(int i = 0;i<subSequences.length;i++) subSequences[i] = sequence();
				
				for(int i = 0;i<seq.size();i++) {
					subSequences[i%step].add(seq.get(i));
				}
				
				//for(int i = 0;i<subSequences.length;i++) System.out.println(subSequences[i]);
				
				int startingPoint = seq.size()%step;
				int remain = next.getNum().getRealValue().intValue();
				
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
					contSequences[i] = next(subSequences[i],num(chunkSizes[i])).simplify(casInfo);
					USED_STEP = false;
					if(contSequences[i] instanceof Next) continue outer;
				}
				
				remain = next.getNum().getRealValue().intValue();
				
				int[] indexTracker = new int[step];
				
				Func outSeq = sequence();
				for(int i = startingPoint;i<startingPoint+remain;i++) {
					int j = i%step;
					if(chunkSizes[j]>0) {
						outSeq.add(contSequences[j].get(indexTracker[j]));
						indexTracker[j]++;
						chunkSizes[j]--;
					}
				}
				System.out.println(step);
				return outSeq;
				
			}
			
			return next;
		}
			
	};
	
	/*
	 * looks for patterns in the coefficients of the pattern
	 */
	static boolean USED_COEF = false;
	static Rule coefPredict = new Rule("predict seperatly the coefficinet and the variable component") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(USED_COEF) return e;
			
			Next next = (Next)e;
			Func seq = next.getSequence();
			
			if(!seq.containsVars()) return next;
			
			Func coefsSeq = sequence();
			Func exprsSeq = sequence();
			
			for(int i = 0;i<seq.size();i++) {
				Func partsSeq = seperateCoef(seq.get(i));
				
				coefsSeq.add(partsSeq.get(0));
				exprsSeq.add(partsSeq.get(1));
			}
			USED_COEF = true;
			Expr nextCoefs = next(coefsSeq,next.getNum()).simplify(casInfo);
			Expr nextExprs = next(exprsSeq,next.getNum()).simplify(casInfo);
			USED_COEF = false;
			
			if(nextCoefs instanceof Next || nextExprs instanceof Next) return next;
			
			Func newSeq = sequence();
			for(int i = 0;i<next.getNum().getRealValue().intValue();i++) {
				newSeq.add(prod(nextCoefs.get(i),nextExprs.get(i)).simplify(casInfo));
			}
			return newSeq;
		}
	};
	//predicts the next fraction in a sequence. Looks at the numerator and denominator patterns separately to predict the next elements
	static Rule fracPredict = new Rule("predict next fraction") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Next next = (Next)e;
			Func seq = next.getSequence();
			
			if(seq.get(0).typeName().equals("div") || seq.get(1).typeName().equals("div")) {
				
				Func numerSeq = sequence();
				Func denomSeq = sequence();
				
				for(int i = 0;i<seq.size();i++) {
					Func div = Div.cast(seq.get(i));
					
					numerSeq.add(div.getNumer());
					denomSeq.add(div.getDenom());
				}
				
				Expr nextNumers = next(numerSeq,next.getNum()).simplify(casInfo);
				Expr nextDenoms = next(denomSeq,next.getNum()).simplify(casInfo);
				
				if(nextNumers instanceof Next || nextDenoms instanceof Next) return next;
				
				Func newSeq = sequence();
				for(int i = 0;i<next.getNum().getRealValue().intValue();i++) {
					newSeq.add(div(nextNumers.get(i),nextDenoms.get(i)).simplify(casInfo));
				}
				return newSeq;
				
			}
			
			return next;
		}
	};
	
	/*
	 * predicts the next power in a sequence by looking at the base and exponent as seperate sequences
	 */
	static Rule powPredict = new Rule("predict next power") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Next next = (Next)e;
			Func seq = next.getSequence();
			
			if(seq.get(0).typeName().equals("power") || seq.get(1).typeName().equals("power") || seq.get(seq.size()-1).typeName().equals("power") || seq.get(seq.size()-2).typeName().equals("power")) {
				
				Func baseSeq = sequence();
				Func expoSeq = sequence();
				
				for(int i = 0;i<seq.size();i++) {
					Func pow = Power.cast(seq.get(i));
					
					baseSeq.add(pow.getBase());
					expoSeq.add(pow.getExpo());
				}
				
				Expr nextBases = next(baseSeq,next.getNum()).simplify(casInfo);
				Expr nextExpos = next(expoSeq,next.getNum()).simplify(casInfo);
				
				if(nextBases instanceof Next || nextExpos instanceof Next) return next;
				
				Func newSeq = sequence();
				for(int i = 0;i<next.getNum().getRealValue().intValue();i++) {
					newSeq.add(power(nextBases.get(i),nextExpos.get(i)).simplify(casInfo));
				}
				return newSeq;
				
			}
			
			return next;
		}
	};
	
	/*
	 * standard way of predicting the next number in a sequences
	 */
	static Rule polynomial = new Rule("is a polynomial sequence") {
		boolean allSame(Func seq) {
			for(int i = 1;i<seq.size();i++) {
				if(!seq.get(i).equals(seq.get(0))) return false;
			}
			return true;
		}
		
		//returns sequence
		Func getDifferenceSequence(Func originalSequence,CasInfo casInfo) {
			Func newSequence = sequence();
			for(int i = 0;i<originalSequence.size()-1;i++) {
				newSequence.add( sub(originalSequence.get(i+1),originalSequence.get(i)).simplify(casInfo) );
			}
			return newSequence;
		}
		
		//returns sequence
		Func getNext(Func sequence,CasInfo casInfo,int size,int num) {
			if(size == 0) return null;
			Func newSeq = sequence();
			if(allSame(sequence)) {
				for(int i = 0;i<num;i++) {
					newSeq.add(sequence.get(0));
				}
				return newSeq;
			}
			Func differenceSequence = getDifferenceSequence(sequence,casInfo);
			
			Func deltasSequence = getNext(differenceSequence,casInfo,size-1,num);
			if(deltasSequence == null) return null;
			
			newSeq.add( sum(sequence.get(sequence.size()-1),deltasSequence.get(0)).simplify(casInfo) );
			for(int i = 1;newSeq.size()<num;i++) {
				newSeq.add( sum(newSeq.get(newSeq.size()-1),deltasSequence.get(i)).simplify(casInfo) );
			}
			
			return newSeq;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Next next = (Next)e;
			Func sequence = next.getSequence();
			Expr expected = getNext(sequence,casInfo,sequence.size()/2+1,next.getNum().getRealValue().intValue());
			
			if(expected != null) {
				return expected;
			}
			
			return next;
		}
	};
	
	//sequence is only 1 or 0 elements
	static Rule nothingCase = new Rule("nothing case for sequence prediction") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Next next = (Next)e;
			if(next.getNum().equals(Num.ZERO)) return sequence();
			else if(next.getSequence().size() == 1) {
				Func outSequence = sequence();
				for(int i = 0;i<next.getNum().getRealValue().intValue();i++) {
					outSequence.add(next.getSequence().get());
				}
				return outSequence;
			}
			return next;
		}
	};
	
	/*
	 * looks what's inside the function and predicts the next inner
	 */
	static Rule funcPredict = new Rule("expression layout prediction") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Next next = (Next)e;
			Func sequence = next.getSequence();
			
			String firstTypeName = sequence.get(0).typeName();
			if(SimpleFuncs.isFunc(firstTypeName) && sequence.get(0).size() == 1) {
				for(int i = 1;i<sequence.size();i++) if(!(sequence.get(i).typeName().equals(firstTypeName)&& sequence.get(i).size() == 1)) return next;
				Func subSequence = sequence();
				for(int i = 0;i<sequence.size();i++) subSequence.add(sequence.get(i).get());
				
				Expr expectedInner = next(subSequence,next.getNum()).simplify(casInfo);
				
				if(expectedInner instanceof Next) return next;
				
				for(int i = 0;i<expectedInner.size();i++) {
					try {
						expectedInner.set(i, SimpleFuncs.getFuncByName(firstTypeName, expectedInner.get(i)) );
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				return expectedInner.simplify(casInfo);
			}
			return next;
		}
	};
	
	static Rule mainSequenceRule = null;
	
	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
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
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
	}

	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return ComplexFloat.ZERO;
	}

	@Override
	public String typeName() {
		return "next";
	}

	@Override
	public String help() {
		return "next(sequence) the sequence predictor\n"
				+ "examples\n"
				+ "next({1,1,2,3,5},4)->{8,13,21,34}\n"
				+ "next({x/2,x^2/4,x^3/8,x^4/16},4)->{x^5/32,x^6/64,x^7/128,x^8/256}";
	}
}
