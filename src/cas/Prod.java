package cas;
import java.math.BigInteger;

public class Prod extends Expr{

	private static final long serialVersionUID = -6256457097575815230L;
	
	static class TermInfo{
		Expr var = null;
		String typeName = null;
		Num expo = null;
		TermInfo(Expr var,String typeName,Num expo){
			this.var = var;
			this.typeName = typeName;
			this.expo = expo;
		}
	}
	static TermInfo getTermInfo(Expr e) {
		if(e instanceof Sin || e instanceof Tan || e instanceof Cos) {
			return new TermInfo(e.get(),e.typeName(),num(1));
		}else if(e instanceof Power) {
			Power casted = (Power)e;
			if(isRealNum(casted.getExpo())) {
				e = casted.getBase();
				if(e instanceof Sin || e instanceof Tan || e instanceof Cos) {
					return new TermInfo(e.get(),e.typeName(),(Num)casted.getExpo());
				}
			}
		}
		return null;
	}
	
	public Prod() {
		commutative = true;
	}
	static Rule epsilonInfReduction = new Rule("expressions with epsilon or infinity",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			Expr epsilon = epsilon();
			Expr inf = inf();
			boolean hasEpsilon = false;
			boolean hasInf = false;
			for(int i = 0;i<prod.size();i++){
				if(prod.get(i).equals(epsilon)){
					hasEpsilon = true;
				}else if(prod.get(i).equals(inf)){
					hasInf = true;
				}
				if(hasInf && hasEpsilon) break;
			}
			if(hasEpsilon && !hasInf){
				if(prod.negative()){
					prod.clear();
					prod.add(neg(epsilon));
				}else{
					prod.clear();
					prod.add(epsilon);
				}
			}else if(!hasEpsilon && hasInf){
				if(prod.negative()){
					prod.clear();
					prod.add(neg(inf));
				}else{
					prod.clear();
					prod.add(inf);
				}
			}else if(hasEpsilon && hasInf){
				if(prod.negative()){
					prod.clear();
					prod.add(prod(num(-1),epsilon,inf));
				}else{
					prod.clear();
					prod.add(prod(epsilon,inf));
				}
			}
			
			return prod;
		}
	};
	
	//the following two functions are also used by div
	static boolean foundProdInTrigInProd(Prod prod){
		if(prod.containsType(Sin.class)){
			for(int i = 0;i < prod.size();i++){
				if(prod.get(i) instanceof Sin || prod.get(i) instanceof Cos || prod.get(i) instanceof Tan){
					if(prod.get(i).get() instanceof Prod){
						return true;
					}
				}
			}
		}
		return false;
	}
	static boolean foundNonProdInTrigInProd(Prod prod){
		if(prod.containsType(Sin.class)){
			for(int i = 0;i < prod.size();i++){
				if(prod.get(i) instanceof Sin || prod.get(i) instanceof Cos || prod.get(i) instanceof Tan){
					if(!(prod.get(i).get() instanceof Prod)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	static Rule trigExpandElements = new Rule("trig expand elements Prod",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			if(foundProdInTrigInProd(prod) && foundNonProdInTrigInProd(prod)){
				
				for(int i = 0;i < prod.size();i++){
					prod.set(i, trigExpand(prod.get(i),settings));
				}
			}
			return prod;
		}
	};
	
	static Rule combineWithDiv = new Rule("combine products with division",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			int indexOfDiv = -1;
			for(int i = 0;i<prod.size();i++) {
				if(prod.get(i) instanceof Div) {
					indexOfDiv = i;
					break;
				}
			}
			
			if(indexOfDiv != -1) {
				Div div = (Div)prod.get(indexOfDiv);
				prod.remove(indexOfDiv);
				
				Prod prodNumer = Prod.cast(div.getNumer());
				Prod prodDenom = Prod.cast(div.getDenom());
				
				for(int i = 0;i<prod.size();i++) {

					if(prod.get(i) instanceof Div) {
						Div castedDiv = (Div)prod.get(i);
						
						prodNumer.add(castedDiv.getNumer());
						prodDenom.add(castedDiv.getDenom());
						
					}else {
						prodNumer.add(prod.get(i));
					}
					
				}
				
				return div(prodNumer,prodDenom).simplify(settings);
			}
			
			return prod;
		}
		
	};
	
	static Rule factorSubSums = new Rule("factor sum elements",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			for(int i = 0;i<prod.size();i++) if(prod.get(i) instanceof Sum) prod.set(i,  factor(prod.get(i)).simplify(settings));
			return prod;
		}
	};
	
	static Rule reSimplifyIntBasePowers = new Rule("re-simplify int based powers",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				if(current instanceof Power) {
					Power currentPower = (Power)current;
					if(currentPower.getBase() instanceof Num) {
						
						prod.set(i, currentPower.simplify(settings));
						
					}
				}
			}
			return prod;
		}
		
	};
	
	static Rule prodContainsProd = new Rule("product contains a product",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				if(current instanceof Prod) {
					for(int j = 0;j<current.size();j++) prod.add(current.get(j));//add all the sub expressions into this product
					prod.remove(i);//remove the sub product
					i--;//shift back after deletion
				}
			}
			
			return prod;
		}
	};
	
	static Rule expoIntoBase = new Rule("power of integers with exponent being product",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				if(current instanceof Power) {
					Power currentPower = (Power)current;
					if(currentPower.getExpo() instanceof Prod && currentPower.getBase() instanceof Num) {
						Prod expoProd = (Prod)currentPower.getExpo();
						
						for(int j = 0;j<expoProd.size();j++) {
							if(expoProd.get(j) instanceof Num) {
								Num num = (Num)expoProd.get(j);
								if(num.realValue.signum() == -1) continue;
								expoProd.remove(j);
								
								BigInteger newBase = ((Num)currentPower.getBase()).realValue.pow(num.realValue.intValue());
								
								currentPower.setBase( num(newBase) );
								currentPower.setExpo(expoProd.simplify(settings));
								
							}
						}
						
					}
				}
			}
			
			return prod;
			
		}
		
	};
	
	static Rule multiplyIntBases = new Rule("multiply int bases",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				
				if(current instanceof Power) {
					Power currentPower = (Power)current;
					
					if(currentPower.getBase() instanceof Num) {
						Num numBase = (Num)currentPower.getBase();
						
						for(int j = i+1; j< prod.size(); j++) {
							
							Expr other = prod.get(j);
							if(other instanceof Power) {
								Power otherPower = (Power)other;
								if(otherPower.getBase() instanceof Num && otherPower.getExpo().equals(currentPower.getExpo())) {
									
									numBase.realValue = numBase.realValue.multiply(((Num)otherPower.getBase()).realValue);
									
									prod.remove(j);
									j--;
									
								}
								
							}
							
						}
						
					}
				}
				prod.set(i, current);//do not simplify because next step does that
			}
			
			return prod;
		}
	};
	
	static Rule expandIntBases = new Rule("prime factor and expand int bases",Rule.TRICKY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				
				if(current instanceof Power) {
					Power currentPower = (Power)current;
					
					if(isPositiveRealNum(currentPower.getBase()) && !(currentPower.getExpo() instanceof Num)) {
						Num numBase = (Num)currentPower.getBase();
						
						if(numBase.realValue.isProbablePrime(128) || numBase.realValue.equals(BigInteger.valueOf(-1))) continue;//skip primes
						
						Prod primeFactors = primeFactor(numBase);
						
						for(int j = 0;j<primeFactors.size();j++) {
							Power factor = (Power)primeFactors.get(j);
							if(((Num)factor.getExpo()).realValue.equals(BigInteger.ONE)) {
								prod.add(pow(factor.getBase(),currentPower.getExpo().copy()));
							}else {
								if(currentPower.getExpo() instanceof Prod) {
									currentPower.getExpo().add(factor.getExpo());
									prod.add(pow(factor.getBase(),currentPower.getExpo().simplify(settings)));
								}else {
									prod.add(pow(factor.getBase(),prod(currentPower.getExpo().copy(),factor.getExpo()).simplify(settings)));
								}
								
							}
						}
						
						prod.remove(i);
						i--;
						
					}
					
				}
				
			}
			
			return prod;
		}
	};
	
	static Rule multiplyLikeTerms = new Rule("multiply like terms",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			for(int i = 0;i < prod.size();i++) {
				
				Expr current = prod.get(i);//get the current object in the array check
				
				Expr expo = new Sum();//create a sum object for the exponent
				
				if(current instanceof Num) continue;//ignore integers
				
				
				Power currentCasted = Power.cast(current);	
				current = currentCasted.getBase();//extract out the base and reassign current, current now represents the base of power
				expo.add(currentCasted.getExpo());//extract out the exponent
				
				boolean found = false;
				for(int j = i+1; j< prod.size();j++) {
					Expr other = prod.get(j);
					
					if(other instanceof Num) continue;//ignore integers
					
					Power otherCasted = Power.cast(other);
					if(otherCasted.getBase().equals(current)) {//if other has equal base
						expo.add(otherCasted.getExpo());
						prod.remove(j);
						j--;
						found = true;
					}
				}
				
				if(found) {
					Expr repl = new Power(current,expo);//replacement
					prod.set(i,repl.simplify(settings));//modify the element with the replacement
				}
				
			}
			
			return prod;
		}
	};
	
	static Rule zeroInProd = new Rule("zero in the product",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			boolean foundZero = false;
			for(int i = 0;i < prod.size();i++) {
				if(prod.get(i) instanceof Num) {
					Num casted = (Num)prod.get(i);
					if(casted.equals(Num.ZERO)) {
						foundZero = true;
						break;
					}
				}
			}
			if(foundZero) {
				prod.clear();
				prod.add(new Num(0));
			}
			return prod;
		}
	};
	
	static Rule multiplyIntegers = new Rule("multiply integers",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			Num total = Num.ONE;
			
			
			for(int i = 0;i < prod.size();i++) {
				Expr expr = prod.get(i);
				if(expr instanceof Num) {
					Num exprCasted = (Num)expr;
					total = total.multNum(exprCasted);
					prod.remove(i);
					i--;
				}
			}
			
			if(!total.equals(Num.ONE)) prod.add(total);
			
			return prod;
		}
	};
	
	static Rule reduceTrigProd = new Rule("reducing trig product",Rule.EASY) {//tan(x)*cos(x) -> sin(x)
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			Prod newNumerProd = new Prod();
			
			for(int i = 0;i < prod.size();i++) {
				TermInfo termInfo = getTermInfo(prod.get(i));
				if(termInfo != null) {
					Expr var = termInfo.var;
					Num sinCount = num(0);
					Num cosCount = num(0);
					
					if(termInfo.typeName.equals("sin")) {
						sinCount = sinCount.addNum(termInfo.expo);
					}else if(termInfo.typeName.equals("cos")) {
						cosCount = cosCount.addNum(termInfo.expo);
					}else if(termInfo.typeName.equals("tan")) {
						sinCount = sinCount.addNum(termInfo.expo);
						cosCount = cosCount.subNum(termInfo.expo);
					}
					
					prod.remove(i);
					i--;
					
					for(int j = 0;j < prod.size();j++) {
						Prod.TermInfo otherTermInfo = Prod.getTermInfo(prod.get(j));
						if(otherTermInfo != null && otherTermInfo.var.equals(var)) {
							
							if(otherTermInfo.typeName.equals("sin")) {
								sinCount = sinCount.addNum(otherTermInfo.expo);
							}else if(otherTermInfo.typeName.equals("cos")) {
								cosCount = cosCount.addNum(otherTermInfo.expo);
							}else if(otherTermInfo.typeName.equals("tan")) {
								sinCount = sinCount.addNum(otherTermInfo.expo);
								cosCount = cosCount.subNum(otherTermInfo.expo);
							}
							
							prod.remove(j);
							j--;
						}
					}
					
					//move around calculation
					Num tanCount = num(0);
					if(!sinCount.equals(Num.ZERO) && !cosCount.equals(Num.ZERO)) {
						if(sinCount.signum() == 1 ^ cosCount.signum() == 1) {
							BigInteger tanCountBI = sinCount.realValue.abs().min(cosCount.realValue.abs());
							BigInteger sumCount = sinCount.realValue.add(cosCount.realValue);
							if(sinCount.negative()) {
								tanCountBI = tanCountBI.negate();
							}
							
							if(sumCount.signum() == 1 ^ sinCount.negative()) {
								sinCount = num(sumCount);
								cosCount = num(0);
							}else {
								sinCount = num(0);
								cosCount = num(sumCount);
							}
							
							tanCount = num(tanCountBI);
						}
					}
					//re add back
					if(!tanCount.equals(Num.ZERO)) {
						newNumerProd.add(  Power.unCast(pow(tan(var),tanCount))  );
					}
					if(!sinCount.equals(Num.ZERO)) {
						newNumerProd.add(  Power.unCast(pow(sin(var),sinCount))  );
					}
					if(!cosCount.equals(Num.ZERO)) {
						newNumerProd.add(  Power.unCast(pow(cos(var),cosCount))  );
					}
					
					
					
				}
				
			}
			
			Expr newExpr = Prod.combine(newNumerProd, prod);
			return newExpr;
		}
		
	};
	
	static Rule aloneProd = new Rule("product is alone",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			return Prod.unCast(e);
		}
	};
	
	static Rule multConj = new Rule("multiplying conjugates",Rule.TRICKY) {//(sqrt(2)+3)*(sqrt(2)-3) -> -7
		private static final long serialVersionUID = 1L;
		
		Expr sqrtObjExtended,sqrtObjExtended2;
		@Override
		public void init() {
			sqrtObjExtended = createExpr("a^(b/2)");
			sqrtObjExtended2 = createExpr("a^(b/2)*c");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Prod prod = (Prod)e;
			
			for(int i = 0;i<prod.size();i++) {
				if(prod.get(i) instanceof Sum && prod.get(i).size() == 2) {
					Sum current = (Sum)prod.get(i);
					Num num = null;
					Expr other = null;
					
					for(int j = 0;j<current.size();j++) {
						if(current.get(j) instanceof Num) num = (Num)current.get(j);
						else if(Rule.fastSimilarStruct(sqrtObjExtended, current.get(j)) || Rule.fastSimilarStruct(sqrtObjExtended2, current.get(j))) {
							other = current.get(j);
						}
					}
					
					if(!(num != null && other != null)) continue;
					
					Sum conj = sum(other,num.negate());//Variant 1
					Expr conj2 = sum(neg(other).simplify(settings),num);//Variant 2
					
					for(int j = i+1;j<prod.size();j++) {
						Expr out = null;
						if(prod.get(j).equals(conj)) {
							out = factor(sub(pow(other,num(2)),num.pow(BigInteger.TWO))).simplify(settings);
						}else if(prod.get(j).equals(conj2)) {
							out = factor(sub(num.pow(BigInteger.TWO),pow(other,num(2)))).simplify(settings);
						}
						
						if(out != null) {
							prod.remove(j);
							prod.remove(i);
							if(out instanceof Prod) {
								for(int k = 0;k<out.size();k++) prod.add(out.get(k));
							}else prod.add(out);
							
							i--;
							continue;
						}
					}
					
				}
			}
			
			return prod;
		}
	};
	
	static ExprList ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = exprList(
				factorSubSums,
				reduceTrigProd,
				trigExpandElements,
				combineWithDiv,
				prodContainsProd,
				expandIntBases,
				multConj,
				multiplyLikeTerms,
				expoIntoBase,
				multiplyIntBases,
				reSimplifyIntBasePowers,
				multiplyIntegers,
				zeroInProd,
				epsilonInfReduction,
				aloneProd
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}
	
	@Override
	public String toString() {
		String out = "";
		Expr prodCopy = copy();
		if(prodCopy.size() < 2) out+="alone product:";
		int indexOfSwap = 0;
		if(prodCopy.size()>1) {
			if(!(prodCopy.get() instanceof Num)) {//bring number to front
				for(int i = 0;i<prodCopy.size();i++) {
					if(prodCopy.get(i) instanceof Num) {
						indexOfSwap = i;
						break;
					}
				}
			}
			//swap
			Expr temp = prodCopy.get(indexOfSwap);
			prodCopy.set(indexOfSwap,prodCopy.get());
			prodCopy.set(0,temp);
		}
		
		for(int i = 0;i < prodCopy.size();i++) {
			boolean paren = false,div = false;
			Expr e = prodCopy.get(i);
			
			if(i == 0 && e instanceof Num) if(((Num) e).realValue.equals(BigInteger.valueOf(-1))) {
				out+='-';
				continue;
			}
			
			if(e instanceof Sum || e instanceof Prod) paren = true;
			
			if(div) out+='/';
			if(paren) out+='(';
			out+=e.toString();
			if(paren) out+=')';
			
			
			if(i != prodCopy.size()-1) out+="*";
			
		}
		return out;
	}
	
	public static Prod cast(Expr e) {//converts it to a prod
		if(e instanceof Prod) {
			return (Prod)e;
		}
		Prod out = new Prod();
		out.add(e);
		return out;
	}
	
	public static Prod combineProds(Prod a,Prod b) {//creates a new object with a combine product
		Prod out = new Prod();
		for(int i = 0;i<a.size();i++) {
			out.add(a.get(i).copy());
		}
		for(int i = 0;i<b.size();i++) {
			out.add(b.get(i).copy());
		}
		return out;
	}
	
	public static Prod combine(Expr a,Expr b) {//like the prod(a,b) function but handles it better, avoids prods in prods
		Prod aCasted = Prod.cast(a),bCasted = Prod.cast(b);
		return Prod.combineProds(aCasted, bCasted);
	}
	
	public static Expr unCast(Expr e) {//if it does not need to be a prod it will return back something else
		if(e instanceof Prod) {
			if(e.size()==0) {
				return num(1);
			}else if(e.size() == 1) {
				return e.get();
			}else {
				return e;
			}
		}
		return e;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		ComplexFloat total = new ComplexFloat(1,0);
		for(int i = 0;i<size();i++) total=ComplexFloat.mult(total, get(i).convertToFloat(varDefs));
		return total;
	}
}
