package cas.primitive;
import java.math.BigInteger;

import cas.Cas;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.matrix.Mat;

public class Prod{
	
	public static Func.FuncLoader prodLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.commutative = true;
			owner.behavior.rule = new Rule(new Rule[] {
					factorSubSums,
					reduceTrigProd,
					trigExpandElements,
					combineWithDiv,
					prodContainsProd,
					expandIntBases,
					multConj,
					multiplyLikeTerms,
					compressRoots,
					expoIntoBase,
					multiplyIntBases,
					reSimplifyIntBasePowers,
					multiplyIntegers,
					zeroInProd,
					epsilonInfReduction,
					productWithMatrix,
					aloneProd
			},"main sequence");
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					String out = "";
					Expr prodCopy = owner.copy();
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
						
						if(i == 0 && e instanceof Num) if(((Num) e).getRealValue().equals(BigInteger.valueOf(-1))) {
							out+='-';
							continue;
						}
						
						if(e.typeName().equals("sum") || e.typeName().equals("prod")) paren = true;
						
						if(div) out+='/';
						if(paren) out+='(';
						out+=e.toString();
						if(paren) out+=')';
						
						
						if(i != prodCopy.size()-1) out+="*";
						
					}
					return out;
				}
			};
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					ComplexFloat total = new ComplexFloat(1,0);
					for(int i = 0;i<owner.size();i++) total=ComplexFloat.mult(total, owner.get(i).convertToFloat(varDefs));
					return total;
				}
			};
		}
	};
	
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
		if(e.typeName().equals("sin") || e.typeName().equals("tan") || e.typeName().equals("cos")) {
			return new TermInfo(e.get(),e.typeName(),Cas.num(1));
		}else if(e.typeName().equals("power")) {
			Func casted = (Func)e;
			if(Cas.isRealNum(casted.getExpo())) {
				e = casted.getBase();
				if(e.typeName().equals("sin") || e.typeName().equals("tan") || e.typeName().equals("cos")) {
					return new TermInfo(e.get(),e.typeName(),(Num)casted.getExpo());
				}
			}
		}
		return null;
	}
	
	static Rule epsilonInfReduction = new Rule("expressions with epsilon or infinity"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			boolean hasEpsilon = false;
			boolean hasInf = false;
			for(int i = 0;i<prod.size();i++){
				if(prod.get(i).equals(Var.EPSILON)){
					hasEpsilon = true;
				}else if(prod.get(i).equals(Var.INF)){
					hasInf = true;
				}
				if(hasInf && hasEpsilon) break;
			}
			if(hasEpsilon && !hasInf){
				if(prod.negative()){
					prod.clear();
					prod.add(Var.NEG_EPSILON);
				}else{
					prod.clear();
					prod.add(Var.EPSILON);
				}
			}else if(!hasEpsilon && hasInf){
				if(prod.negative()){
					prod.clear();
					prod.add(Var.NEG_INF);
				}else{
					prod.clear();
					prod.add(Var.INF);
				}
			}else if(hasEpsilon && hasInf){
				if(prod.negative()){
					prod.clear();
					prod.add(prod(num(-1),epsilon(),inf()));
				}else{
					prod.clear();
					prod.add(prod(epsilon(),inf()));
				}
			}
			
			return prod;
		}
	};
	
	//the following two functions are also used by div
	public static boolean foundProdInTrigInProd(Func prod){
		if(prod.containsType("sin")){
			for(int i = 0;i < prod.size();i++){
				if(prod.get(i).typeName().equals("sin") || prod.get(i).typeName().equals("cos") || prod.get(i).typeName().equals("tan")){
					if(prod.get(i).get().typeName().equals("prod")){
						return true;
					}
				}
			}
		}
		return false;
	}
	public static boolean foundNonProdInTrigInProd(Func prod){
		if(prod.containsType("sin")){
			for(int i = 0;i < prod.size();i++){
				if(prod.get(i).typeName().equals("sin") || prod.get(i).typeName().equals("cos") || prod.get(i).typeName().equals("tan")){
					if(!(prod.get(i).get().typeName().equals("prod"))){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	static Rule trigExpandElements = new Rule("trig expand elements Prod"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			if(foundProdInTrigInProd(prod) && foundNonProdInTrigInProd(prod)){
				
				for(int i = 0;i < prod.size();i++){
					prod.set(i, trigExpand(prod.get(i),casInfo));
				}
			}
			return prod;
		}
	};
	
	static Rule combineWithDiv = new Rule("combine products with division"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			int indexOfDiv = -1;
			for(int i = 0;i<prod.size();i++) {
				if(prod.get(i).typeName().equals("div")) {
					indexOfDiv = i;
					break;
				}
			}
			
			if(indexOfDiv != -1) {
				Func div = (Func)prod.get(indexOfDiv);
				prod.remove(indexOfDiv);
				
				Func prodNumer = Prod.cast(div.getNumer());
				Func prodDenom = Prod.cast(div.getDenom());
				
				for(int i = 0;i<prod.size();i++) {

					if(prod.get(i).typeName().equals("div")) {
						Func castedDiv = (Func)prod.get(i);
						
						prodNumer.add(castedDiv.getNumer());
						prodDenom.add(castedDiv.getDenom());
						
					}else {
						prodNumer.add(prod.get(i));
					}
					
				}
				
				return div(prodNumer,prodDenom).simplify(casInfo);
			}
			
			return prod;
		}
		
	};
	
	static Rule factorSubSums = new Rule("factor sum elements"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			for(int i = 0;i<prod.size();i++) if(prod.get(i).typeName().equals("sum")) prod.set(i,  factor(prod.get(i)).simplify(casInfo));
			return prod;
		}
	};
	
	static Rule reSimplifyIntBasePowers = new Rule("re-simplify int based powers"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				if(current.typeName().equals("power")) {
					Func currentPower = (Func)current;
					if(currentPower.getBase() instanceof Num) {
						
						prod.set(i, currentPower.simplify(casInfo));
						
					}
				}
			}
			return prod;
		}
		
	};
	
	static Rule prodContainsProd = new Rule("product contains a product"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				if(current.typeName().equals("prod")) {
					for(int j = 0;j<current.size();j++) prod.add(current.get(j));//add all the sub expressions into this product
					prod.remove(i);//remove the sub product
					i--;//shift back after deletion
				}
			}
			
			return prod;
		}
	};
	
	static Rule expoIntoBase = new Rule("power of integers with exponent being product"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				if(current.typeName().equals("power")) {
					Func currentPower = (Func)current;
					if(currentPower.getExpo().typeName().equals("prod") && currentPower.getBase() instanceof Num) {
						Func expoProd = (Func)currentPower.getExpo();
						
						for(int j = 0;j<expoProd.size();j++) {
							if(expoProd.get(j) instanceof Num) {
								Num num = (Num)expoProd.get(j);
								if(num.getRealValue().signum() == -1) continue;
								expoProd.remove(j);
								
								BigInteger newBase = ((Num)currentPower.getBase()).getRealValue().pow(num.getRealValue().intValue());
								
								currentPower.setBase( num(newBase) );
								currentPower.setExpo(expoProd.simplify(casInfo));
								
							}
						}
						
					}
				}
			}
			
			return prod;
			
		}
		
	};
	
	static Rule multiplyIntBases = new Rule("multiply int bases"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				
				if(current.typeName().equals("power")) {
					Func currentPower = (Func)current;
					
					if(currentPower.getBase() instanceof Num) {
						Num numBase = (Num)currentPower.getBase();
						
						for(int j = i+1; j< prod.size(); j++) {
							
							Expr other = prod.get(j);
							if(other.typeName().equals("power")) {
								Func otherPower = (Func)other;
								if(otherPower.getBase() instanceof Num && otherPower.getExpo().equals(currentPower.getExpo())) {
									
									numBase.setRealValue(numBase.getRealValue().multiply(((Num)otherPower.getBase()).getRealValue()));
									
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
	
	static Rule expandIntBases = new Rule("prime factor and expand int bases"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				
				if(current.typeName().equals("power")) {
					Func currentPower = (Func)current;
					
					if(isPositiveRealNum(currentPower.getBase()) && !(currentPower.getExpo() instanceof Num)) {
						Num numBase = (Num)currentPower.getBase();
						
						if(numBase.getRealValue().isProbablePrime(128) || numBase.getRealValue().equals(BigInteger.valueOf(-1))) continue;//skip primes
						
						Func primeFactorsProd = primeFactor(numBase);
						
						for(int j = 0;j<primeFactorsProd.size();j++) {
							Func factor = (Func)primeFactorsProd.get(j);
							if(((Num)factor.getExpo()).getRealValue().equals(BigInteger.ONE)) {
								prod.add(power(factor.getBase(),currentPower.getExpo().copy()));
							}else {
								if(currentPower.getExpo().typeName().equals("prod")) {
									currentPower.getExpo().add(factor.getExpo());
									prod.add(power(factor.getBase(),currentPower.getExpo().simplify(casInfo)));
								}else {
									prod.add(power(factor.getBase(),prod(currentPower.getExpo().copy(),factor.getExpo()).simplify(casInfo)));
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
	
	static Rule multiplyLikeTerms = new Rule("multiply like terms"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			for(int i = 0;i < prod.size();i++) {
				
				Expr current = prod.get(i);//get the current object in the array check
				
				Expr expoSum = sum();//create a sum object for the exponent
				
				if(current instanceof Num) continue;//ignore integers
				
				
				Func currentCasted = Power.cast(current);	
				current = currentCasted.getBase();//extract out the base and reassign current, current now represents the base of power
				expoSum.add(currentCasted.getExpo());//extract out the exponent
				
				boolean found = false;
				for(int j = i+1; j< prod.size();j++) {
					Expr other = prod.get(j);
					
					if(other instanceof Num) continue;//ignore integers
					
					Func otherCasted = Power.cast(other);
					if(otherCasted.getBase().equals(current)) {//if other has equal base
						expoSum.add(otherCasted.getExpo());
						prod.remove(j);
						j--;
						found = true;
					}
				}
				
				if(found) {
					Expr repl = power(current,expoSum);//replacement
					prod.set(i,repl.simplify(casInfo));//modify the element with the replacement
				}
				
			}
			
			return prod;
		}
	};
	
	static Rule zeroInProd = new Rule("zero in the product"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
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
	
	static Rule multiplyIntegers = new Rule("multiply integers"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
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
	
	static Rule reduceTrigProd = new Rule("reducing trig product") {//tan(x)*cos(x) -> sin(x)
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			Func newNumerProd = prod();
			
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
							BigInteger tanCountBI = sinCount.getRealValue().abs().min(cosCount.getRealValue().abs());
							BigInteger sumCount = sinCount.getRealValue().add(cosCount.getRealValue());
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
						newNumerProd.add(  Power.unCast(power(tan(var),tanCount))  );
					}
					if(!sinCount.equals(Num.ZERO)) {
						newNumerProd.add(  Power.unCast(power(sin(var),sinCount))  );
					}
					if(!cosCount.equals(Num.ZERO)) {
						newNumerProd.add(  Power.unCast(power(cos(var),cosCount))  );
					}
					
					
					
				}
				
			}
			
			Expr newExpr = Prod.combine(newNumerProd, prod);
			return newExpr;
		}
		
	};
	
	static Rule aloneProd = new Rule("product is alone"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			return Prod.unCast(e);
		}
	};
	
	static Rule multConj = new Rule("multiplying conjugates") {//(sqrt(2)+3)*(sqrt(2)-3) -> -7
		Expr sqrtObjExtended,sqrtObjExtended2;
		@Override
		public void init() {
			sqrtObjExtended = createExpr("a^(b/2)");
			sqrtObjExtended2 = createExpr("a^(b/2)*c");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			for(int i = 0;i<prod.size();i++) {
				Func currentCasted = Power.cast(prod.get(i));
				if( currentCasted.getBase().typeName().equals("sum") && currentCasted.getBase().size() == 2 ) {
					Func currentSum = (Func)currentCasted.getBase();
					Num num = null;
					Expr other = null;
					
					for(int j = 0;j<currentSum.size();j++) {
						if(currentSum.get(j) instanceof Num) num = (Num)currentSum.get(j);
						else if(Rule.fastSimilarExpr(sqrtObjExtended, currentSum.get(j)) || Rule.fastSimilarExpr(sqrtObjExtended2, currentSum.get(j))) {
							other = currentSum.get(j);
						}
					}
					
					if(!(num != null && other != null)) continue;
					
					Expr conj = Power.unCast( power(sum(other,num.negate()),currentCasted.getExpo()) );//Variant 1
					Expr conj2 = Power.unCast( power(sum(neg(other).simplify(casInfo),num),currentCasted.getExpo()) );//Variant 2
					
					for(int j = i+1;j<prod.size();j++) {
						Expr out = null;
						if(prod.get(j).equals(conj)) {
							out = power(factor(sub(power(other,num(2)),num.pow(BigInteger.TWO))),currentCasted.getExpo()).simplify(casInfo);
						}else if(prod.get(j).equals(conj2)) {
							out = power(factor(sub(num.pow(BigInteger.TWO),power(other,num(2)))),currentCasted.getExpo()).simplify(casInfo);
						}
						
						if(out != null) {
							prod.remove(j);
							prod.remove(i);
							if(out.typeName().equals("prod")) {
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
	
	static Rule productWithMatrix = new Rule("product with matrix") {//multiplication of each element
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			if(prod.containsType("mat")) {
				Func matriciesProd = prod();
				Func nonMatriciesProd = prod();
				
				for(int i = 0;i<prod.size();i++) {
					if(prod.get(i) instanceof Mat) {
						matriciesProd.add(prod.get(i));
					}else {
						nonMatriciesProd.add(prod.get(i));
					}
				}
				
				if(matriciesProd.size()>0) {
					Mat total = (Mat)matriciesProd.get(0);
					for(int i = 1;i<matriciesProd.size();i++) {
						Mat other = (Mat)matriciesProd.get(i);
						
						for(int row = 0;row<total.rows();row++) {
							for(int col = 0;col<total.cols();col++) {
								
								Func elprod = Prod.cast(total.getElement(row, col));
								elprod.add(other.getElement(row, col));
								
								total.setElement(row, col, elprod  );
								
							}
							
						}
					}
					
					for(int row = 0;row<total.rows();row++) {
						for(int col = 0;col<total.cols();col++) {
							
							Func elprod = Prod.cast(total.getElement(row, col));
							elprod.add(nonMatriciesProd);
							
							total.setElement(row, col, elprod  );
							
						}
						
					}
					
					return total.simplify(casInfo);
				}
			}
			return prod;
		}
		
	};
	
	static Rule compressRoots = new Rule("compress roots together"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func prod = (Func)e;
			
			for(int i = 0;i<prod.size();i++) {
				if(prod.get(i).typeName().equals("power") && ((Func)prod.get(i)).getExpo().typeName().equals("div") ) {
					Func current = (Func) prod.get(i);
					Func baseProd = Prod.cast(current.getBase());
					boolean changed = false;
					
					for(int j = i+1;j<prod.size();j++) {
						
						if(prod.get(j).typeName().equals("power") && ((Func)prod.get(j)).getExpo().typeName().equals("div") ) {
							Func other = (Func)prod.get(j);
							if(other.getExpo().equals(current.getExpo())) {
								
								if(other.getBase().typeName().equals("prod")) {
									for(int k = 0;k<other.getBase().size();k++) baseProd.add(other.getBase().get(k));
								}else {
									baseProd.add(other.getBase());
								}
								changed = true;
								prod.remove(j);
								j--;
							}
						}
						
					}
					if(changed) {
						current.setBase( Prod.unCast(baseProd));
						//System.out.println(current);
						
						prod.set(i, current.simplify(casInfo) );
					}
					
				}
			}
			
			return prod;
		}
		
	};
	
	public static Func cast(Expr e) {//converts it to a prod, returns prod
		if(e.typeName().equals("prod")) {
			return (Func)e;
		}
		Func outProd = Cas.prod();
		outProd.add(e);
		return outProd;
	}
	
	public static Func combineProds(Func aProd,Func bProd) {//creates a new object with a combine product, returns product
		Func outProd = Cas.prod();
		for(int i = 0;i<aProd.size();i++) {
			outProd.add(aProd.get(i).copy());
		}
		for(int i = 0;i<bProd.size();i++) {
			outProd.add(bProd.get(i).copy());
		}
		return outProd;
	}
	
	public static Func combine(Expr a,Expr b) {//like the prod(a,b) function but handles it better, avoids prods in prods, returns a prod
		Func aCastedProd = Prod.cast(a),bCasted = Prod.cast(b);
		return Prod.combineProds(aCastedProd, bCasted);
	}
	
	public static Expr unCast(Expr e) {//if it does not need to be a prod it will return back something else
		if(e.typeName().equals("prod")) {
			if(e.size()==0) {
				return Cas.num(1);
			}else if(e.size() == 1) {
				return e.get();
			}else {
				return e;
			}
		}
		return e;
	}
}
