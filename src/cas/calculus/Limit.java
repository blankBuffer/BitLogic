package cas.calculus;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.Settings;
import cas.SimpleFuncs;
import cas.StandardRules;
import cas.primitive.Div;
import cas.primitive.ExprList;
import cas.primitive.Ln;
import cas.primitive.Num;
import cas.primitive.Power;
import cas.primitive.Prod;
import cas.primitive.Sequence;
import cas.primitive.Sum;
import cas.primitive.Var;
import cas.special.Gamma;
import cas.trig.Cos;
import cas.trig.Sin;

public class Limit extends Expr{
	private static final long serialVersionUID = 3302019973998257065L;
	public static final short RIGHT = 1,LEFT = -1,NONE = 0;
	
	@Override
	public Var getVar(){
		return (Var) get(1);
	}
	
	public Expr getExpr(){
		return get();
	}
	
	public void setExpr(Expr e){
		set(0,e);
	}
	
	public Expr getValue(){
		return get(2);
	}
	
	public short getDirection(){
		return getDirection(getValue());
	}
	
	public static short flipDirection(short direction){
		return (short)(-direction);
	}
	
	public static short getDirection(Expr e){
		Var epsilon = epsilon();
		Expr negEpsilon = neg(epsilon());
		if(e instanceof Sum){
			Sum innerSum = (Sum)e;
			for(int i = 0;i<innerSum.size();i++){
				if(innerSum.get(i).equals(epsilon)){
					return RIGHT;
				}else if(innerSum.get(i).equals(negEpsilon)){
					return LEFT;
				}
			}
		}else if(e.equals(epsilon)){
			return RIGHT;
		}else if(e.equals(negEpsilon)){
			return LEFT;
		}
		return NONE;
	}
	
	public static Expr stripDirection(Expr e){//does not modify input
		Var epsilon = epsilon();
		Expr negEpsilon = neg(epsilon());
		if(e instanceof Sum){
			Sum innerSum = (Sum)e;
			for(int i = 0;i<innerSum.size();i++){
				if(innerSum.get(i).equals(epsilon)){
					Sum out = (Sum)e.copy();
					out.remove(i);
					return Sum.unCast(out);
				}else if(innerSum.get(i).equals(negEpsilon)){
					Sum out = (Sum)e.copy();
					out.remove(i);
					return Sum.unCast(out);
				}
			}
		}else if(e.equals(epsilon)){
			return num(0);
		}else if(e.equals(negEpsilon)){
			return num(0);
		}
		return e.copy();
	}
	
	public static Expr applyDirection(Expr e,short direction){//modifies input
		Expr epsilonAdder = direction == LEFT ? neg(epsilon()) : (  direction == RIGHT ? epsilon() : null);
		
		if(epsilonAdder != null){
			if(e instanceof Sum){
				e.add(epsilonAdder);
				return e;
			}
			return sum(epsilonAdder,e);
		}
		return e;
	}
	
	public static boolean zeroOrEpsilon(Expr e){
		return e.equals(Num.ZERO) || isEpsilon(e);
	}
	
	
	public static boolean isEpsilon(Expr e){
		return e.equals(epsilon()) || e.equals(neg(epsilon()));
	}
	public static boolean isInf(Expr e){
		return e.equals(inf()) || e.equals(neg(inf()));
	}
	
	public static class Bigger{
		
		private static boolean isBasicPoly(Expr e,Var v){
			return  (e instanceof Power && isPositiveRealNum(((Power)e).getExpo()) && ((Power)e).getBase().equals(v)) || e.equals(v);
		}
		
		private static boolean isBasicExponential(Expr e,Var v){
			return e instanceof Power && ((Power)e).getExpo().contains(v) && !((Power)e).getBase().contains(v);
		}
		
		private static boolean isSuperExp(Expr e,Var v){
			return e instanceof Power && ((Power)e).getExpo().contains(v) && ((Power)e).getBase().contains(v);
		}
		
		static Expr changeExponentialFuncs(Expr e,Var v){//e^(x+2) -> e^2*e^x
			if(e instanceof Power){
				Power casted = (Power)e;
				if(casted.getExpo() instanceof Sum && casted.getExpo().contains(v)){
					Prod outer = new Prod();
					Sum newExpo = new Sum();
					
					Sum currentExpo = (Sum)casted.getExpo();
					
					for(int i = 0;i<currentExpo.size();i++){
						if(currentExpo.get(i).contains(v)){
							newExpo.add( changeExponentialFuncs(currentExpo.get(i),v) );
						}else{
							outer.add(pow(casted.getBase().copy(),currentExpo.get(i).copy()));
						}
					}
					
					if(outer.size() > 0){
						outer.add(pow(casted.getBase().copy(),Sum.unCast(newExpo)));
						return outer;
					}
					return e.copy();
					
				}
			}
			Expr eCopy = e.copy();
			
			for(int i = 0;i<eCopy.size();i++){
				eCopy.set(i, changeExponentialFuncs(eCopy.get(i) ,v));
			}
			
			return eCopy;
		}
		
		private static final int CONST = 1,LOG = 2,POLY = 3,EXP = 4,GAMMA = 5,SUPER_EXP = 6,UNSURE = 0;
		
		private static int basicClassScore(Expr e,Var v){
			if(!e.contains(v)) return CONST;
			if(e instanceof Ln) return LOG;
			if(isBasicPoly(e,v)) return POLY;
			if(isBasicExponential(e,v)) return EXP;
			if(e instanceof Gamma) return GAMMA;
			if(isSuperExp(e,v)) return SUPER_EXP;
			
			if(e instanceof Div){
				Div casted = (Div)e;
				if(!casted.getNumer().contains(v)){
					return -basicClassScore(casted.getDenom(),v);
				}
			}
			
			return UNSURE;
		}
		
		private static void removeCommonItems(Expr a,Expr b){
			outer:for(int i = 0;i<a.size();i++){
				Expr current = a.get(i);
				
				for(int j = 0;j<b.size();j++){
					Expr other = b.get(j);
					if(current.equals(other)){
						b.remove(j);
						a.remove(i);
						i--;
						continue outer;
					}
				}
			}
		}
		
		private static int indexOfBiggestInSet(Expr e,Var v){
			int index = 0;
			for(int i = 0;i<e.size()-1;i++){
				int comp = biggerSimple(e.get(i),e.get(i+1),v);
				
				if(comp != -1) index = comp==0 ? i : i+1;
				else return -1;
			}
			return index;
		}
		
		private static int biggerSimple(Expr a,Expr b,Var v){//compares two functions to see which approaches infinity faster
			
			//System.out.println("comp: "+a+" "+b);
			
			if(!a.contains(v) && !b.contains(v)){
				
				double aFloat = a.convertToFloat(new ExprList()).real;
				double bFloat = b.convertToFloat(new ExprList()).real;
				
				if(aFloat>bFloat){
					return 0;
				}else if(aFloat<bFloat){
					return 1;
				}
				
				return -1;
			}
		
			Sequence aSep = seperateByVar(a,v);
			a=aSep.get(1);
			Expr aCoef = aSep.get(0);
			
			Sequence bSep = seperateByVar(b,v);
			b=bSep.get(1);
			Expr bCoef = bSep.get(0);
			
			int aScore = basicClassScore(a,v),bScore = basicClassScore(b,v);
			
			if(aScore != bScore && aScore != UNSURE && bScore != UNSURE) return aScore>bScore ? 0 : 1;
			
			if(a.equals(b)) return biggerSimple(aCoef,bCoef,v);
			int commonScore = aScore;
			
			if(commonScore == LOG){
				return biggerSimple(a.get(),b.get(),v);
			}
			
			if(commonScore == POLY){
				int comp = degree(a,v).compareTo(degree(b,v));
				if(comp == 1){
					return 0;
				}else if(comp == -1){
					return 1;
				}
				return -1;
			}
			
			if(commonScore == EXP){
				Power aCasted = (Power)a;
				Power bCasted = (Power)b;
				
				if(aCasted.getBase().equals(bCasted.getBase())) return biggerSimple(aCasted.getExpo(),bCasted.getExpo(),v);
				return biggerSimple(aCasted.getBase(),bCasted.getBase(),v);
			}
			
			if(commonScore == SUPER_EXP){
				Power aCasted = (Power)a;
				Power bCasted = (Power)b;
				
				if(aCasted.getBase().equals(bCasted.getBase())) return biggerSimple(aCasted.getExpo(),bCasted.getExpo(),v);
				return biggerSimple(aCasted.getBase(),bCasted.getBase(),v);
			}
			
			if(a instanceof Div || b instanceof Div || a instanceof Prod || b instanceof Prod){
				Div aDiv = Div.cast(a);
				Div bDiv = Div.cast(b);
				
				aDiv.setNumer(Prod.cast(aDiv.getNumer()));
				bDiv.setNumer(Prod.cast(bDiv.getNumer()));
				
				aDiv.setDenom(Prod.cast(aDiv.getDenom()));
				bDiv.setDenom(Prod.cast(bDiv.getDenom()));
				
				removeCommonItems(aDiv.getNumer(),bDiv.getNumer());
				removeCommonItems(aDiv.getDenom(),bDiv.getDenom());
				
				System.out.println(aDiv.getNumer()+"  "+aDiv.getDenom());
				int aDivComp = !aDiv.contains(v) ? 0 : biggerSimple(aDiv.getNumer(),aDiv.getDenom(),v);
				if(aDivComp == -1) return -1;
				int bDivComp = !bDiv.contains(v) ? 0 : biggerSimple(bDiv.getNumer(),bDiv.getDenom(),v);
				if(bDivComp == -1) return -1;
				
				if(aDivComp == 1 && bDivComp == 0) return 1;
				if(aDivComp == 0 && bDivComp == 1) return 0;
				
				
				if(aDiv.getNumer().size() == 0) aDiv.getNumer().add(num(1));
				if(aDiv.getDenom().size() == 0) aDiv.getDenom().add(num(1));
				
				if(bDiv.getNumer().size() == 0) bDiv.getNumer().add(num(1));
				if(bDiv.getDenom().size() == 0) bDiv.getDenom().add(num(1));
				
				
				
				int biggestInA = indexOfBiggestInSet(aDiv.get(aDivComp),v);
				if(biggestInA == -1) return -1;
				Expr biggestExprInA = aDiv.get(aDivComp).get(biggestInA);
				
				int biggestInB = indexOfBiggestInSet(bDiv.get(bDivComp),v);
				if(biggestInB == -1) return -1;
				Expr biggestExprInB = bDiv.get(bDivComp).get(biggestInB);
				
				return biggerSimple(biggestExprInA,biggestExprInB,v);
				
			}
			
			if(a instanceof Sum || b instanceof Sum){
				Sum aSum = Sum.cast(a);
				Sum bSum = Sum.cast(b);
				
				removeCommonItems(aSum,bSum);
				
				if(aSum.size() == 0) aSum.add(num(0));
				if(bSum.size() == 0) bSum.add(num(0));
				
				int biggestInA = indexOfBiggestInSet(aSum,v);
				if(biggestInA == -1) return -1;
				int biggestInB = indexOfBiggestInSet(bSum,v);
				if(biggestInB == -1) return -1;
				
				return biggerSimple(aSum.get(biggestInA),bSum.get(biggestInB),v);
				
			}
			
			return -1;
		}
		public static Expr bigger(Expr a,Expr b,Var v){
			if(a == null || b == null) return null;
			int biggerComp = biggerSimple(changeExponentialFuncs(a,v),changeExponentialFuncs(b,v),v);//pre-processing step with "changeExponentialFuncs"
			
			if(biggerComp == 0) return a;
			else if(biggerComp == 1) return b;
			
			return null;
		}
	}
	
	
	
	static Rule biggestInSumLimit = new Rule("biggest term in sum controls",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Limit lim = null;
			if(e instanceof Limit){
				lim = (Limit)e;
			}else{
				return e;
			}
			if(isInf(lim.getValue()) && lim.getExpr() instanceof Sum){
				Sum inner = (Sum)lim.getExpr();
				
				Expr biggest = null;
				for(int i = 0;i<inner.size()-1;i++){
					
					biggest = Bigger.bigger(inner.get(i),inner.get(i+1),lim.getVar());
					if(biggest == null) break;
				}
				if(biggest != null){
					return limit(biggest,lim.getVar(),lim.getValue());
				}
				
			}
			
			return lim;
		}
	};
	
	public Limit(){}//
	
	public Limit(Expr e,Var v,Expr value){
		add(e);
		add(v);
		add(value);
	}
	
	static Rule removeConstants = new Rule("limit product with constants",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Limit lim = null;
			if(e instanceof Limit){
				lim = (Limit)e;
			}else{
				return e;
			}
			Sequence sep = seperateByVar(lim.getExpr(),lim.getVar());
			
			if(!sep.get(0).equals(Num.ONE)){
				return prod(sep.get(0),limit(sep.get(1),lim.getVar(),lim.getValue())).simplify(settings);
			}
			
			return lim;
		}
	};
	
	static Rule divPolyLimit = new Rule("limit of dividing polynomials",Rule.CHALLENGING){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Limit lim = null;
			if(e instanceof Limit){
				lim = (Limit)e;
			}else{
				return e;
			}
			
			boolean positiveInf = lim.getValue().equals(inf());
			boolean hasInf = positiveInf || lim.getValue().equals(neg(inf()));
			if(lim.getExpr() instanceof Div){
				Div casted = (Div)lim.getExpr();
				if(isPolynomialUnstrict(casted.getNumer(),lim.getVar()) && isPolynomialUnstrict(casted.getDenom(),lim.getVar())){
					if(hasInf){
						casted.setNumer( SimpleFuncs.fullExpand.applyRuleToExpr(casted.getNumer(), settings) );
						casted.setDenom( SimpleFuncs.fullExpand.applyRuleToExpr(casted.getDenom(), settings) );
						
						Sequence numerPoly = polyExtract(casted.getNumer(),lim.getVar(),settings);
						Expr numerCoef = numerPoly.get(numerPoly.size()-1);
						
						Sequence denomPoly = polyExtract(casted.getDenom(),lim.getVar(),settings);
						Expr denomCoef = denomPoly.get(denomPoly.size()-1);
						
						Expr div = div(numerCoef,denomCoef).simplify(settings);
						
						int sign = 1;
						
						if(div.negative()){
							sign = -sign;
							div = div.strangeAbs(settings);
						}
						
						if(!positiveInf && (numerPoly.size()+denomPoly.size())%2==1){
							sign = -sign;
						}
						
						Expr out = null;
						if(numerPoly.size() == denomPoly.size()){
							out = prod(num(sign),div);
						}else if(numerPoly.size() > denomPoly.size()){
							out = prod(num(sign),inf());
						}else if(numerPoly.size() < denomPoly.size()){
							out = prod(num(sign),epsilon());
						}
						
						return out.simplify(settings);
							
					}
					
				}
			}
			
			return lim;
		}
	};
	
	static Rule directSubst = new Rule("direct substitution",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Limit lim = null;
			if(e instanceof Limit){
				lim = (Limit)e;
			}else{
				return e;
			}
			
			
			return lim.getExpr().replace(equ(lim.getVar(),lim.getValue())).simplify(settings);
		}
	};
	
	static Rule lhopitalsRule = new Rule("l'hopital's rule",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		boolean containsReducablePart(Expr e,Var v){
			Sum sum = Sum.cast(e);
			
			for(int i = 0;i<sum.size();i++){
				Prod prod = Prod.cast(sum.get(i));
				for(int j = 0;j<prod.size();j++){
					if(prod.get(j) instanceof Ln || prod.get(j) instanceof Sin || prod.get(j) instanceof Cos || isPolynomialUnstrict(prod.get(j),v)){
						return true;
					}
				}
			}
			return false;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Limit lim = null;
			if(e instanceof Limit){
				lim = (Limit)e;
			}else{
				return e;
			}
			
			if(lim.getExpr() instanceof Div){
				Div inner = (Div)lim.getExpr();
				
				if(containsReducablePart(inner.getNumer(),lim.getVar()) || containsReducablePart(inner.getDenom(),lim.getVar())){
					Div computed = div( limit(inner.getNumer(),lim.getVar(),lim.getValue()).simplify(settings) ,  limit(inner.getDenom(),lim.getVar(),lim.getValue()).simplify(settings) );
					
					if( (zeroOrEpsilon(computed.getNumer()) && zeroOrEpsilon(computed.getDenom())) || (isInf(computed.getNumer()) && isInf(computed.getDenom()))){
						Expr newInner = div( diff(inner.getNumer(),lim.getVar()), diff(inner.getDenom(),lim.getVar()) ).simplify(settings);
						lim.setExpr(newInner);
					}
				}
				
			}else if(lim.getExpr() instanceof Power){
				Power inner = (Power)lim.getExpr();
				
				if(containsReducablePart(inner.getBase(),lim.getVar()) || containsReducablePart(inner.getExpo(),lim.getVar())){
					Power computed = pow(limit(inner.getBase(),lim.getVar(),lim.getValue()).simplify(settings) ,  limit(inner.getExpo(),lim.getVar(),lim.getValue()).simplify(settings) );
					
					if( (zeroOrEpsilon(computed.getBase()) && zeroOrEpsilon(computed.getExpo())) || (stripDirection(computed.getBase()).equals(Num.ONE) && computed.getExpo().equals(inf()) ) ){
						Expr out = exp( limit( div( diff(ln(inner.getBase()),lim.getVar()) , diff(inv(inner.getExpo()),lim.getVar()) ) ,lim.getVar(),lim.getValue()) );
						return out.simplify(settings);
					}
					
				}
				
			}else if(lim.getExpr() instanceof Sum){
				Sum inner = (Sum)lim.getExpr();
				
				Sum finiteParts = new Sum();
				Sum posInfParts = new Sum();
				Sum negInfParts = new Sum();
				
				Expr inf = inf();
				Expr negInf = neg(inf());
				
				for(int i = 0;i<inner.size();i++){
					Expr currentComputed = limit(inner.get(i),lim.getVar(),lim.getValue()).simplify(settings);
					
					if(currentComputed.equals(inf)){
						posInfParts.add(inner.get(i));
					}else if(currentComputed.equals(negInf)){
						negInfParts.add(neg(inner.get(i)));
					}else{
						finiteParts.add(inner.get(i));
					}
					
				}
				if(negInfParts.size()>0 && posInfParts.size()>0){
					Expr out = limit(div(diff(  sub( inv(negInfParts),inv(posInfParts) )  ,lim.getVar()),diff(  inv(prod(posInfParts,negInfParts))  ,lim.getVar())).simplify(settings) ,lim.getVar(),lim.getValue());
					finiteParts.add(out);
					return finiteParts.simplify(settings);
				}
				
			}
			
			return lim;
		}
		
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				StandardRules.pullOutConstants,
				biggestInSumLimit,
				divPolyLimit,
				lhopitalsRule,
				directSubst
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat(varDefs);
	}

}
