package cas;
import java.math.BigInteger;
import java.util.ArrayList;

public class Prod extends Expr{

	private static final long serialVersionUID = -6256457097575815230L;
	public Prod() {
	}
	public Prod(Expr first,Expr second) {
		add(first);
		add(second);
	}
	
	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		if(!settings.powExpandMode) factorSubSums((Prod)toBeSimplified,settings);//factor sums
		
		prodContainsProd((Prod)toBeSimplified);//product contains a product
		
		boolean hasManyIntBases = hasManyIntBases();//improves performance
			
		if(hasManyIntBases) expandIntBases((Prod)toBeSimplified,settings);//12^x*m -> 2^(2*x)*3^x*m
			
		
		multiplyLikeTerms((Prod)toBeSimplified,settings);//x*x = x^2
		
		if(!settings.powExpandMode) {	
			if(hasManyIntBases) expoIntoBase((Prod)toBeSimplified,settings);//10^(2*x)*a -> 100^x*a
			
			if(hasManyIntBases)multiplyIntBases((Prod)toBeSimplified);//2^x*10^x -> 20^x
			
			if(hasManyIntBases)simplifyIntBasePowers((Prod)toBeSimplified,settings);//re simplify powers with int bases
		}
		
		multiplyIntegersAndInverses((Prod)toBeSimplified);//2*3 = 6
		
		checkForZero((Prod)toBeSimplified);//x*0 -> 0
		
		toBeSimplified = alone((Prod)toBeSimplified);//if a product only has 1 element
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		return toBeSimplified;
	}
	
	boolean hasManyIntBases() {
		int count = 0;
		for(int i = 0;i<size();i++) {
			if(get(i) instanceof Power) {
				Power p = (Power)get(i);
				if(p.getBase() instanceof Num) count++;
				
			}
		}
		if(count > 1) return true;
		return false;
	}
	
	void factorSubSums(Prod prod,Settings settings) {
		for(int i = 0;i<prod.size();i++) if(prod.get(i) instanceof Sum) prod.set(i,  factor(prod.get(i)).simplify(settings));
	}
	
	
	void simplifyIntBasePowers(Prod prod,Settings settings) {
		for(int i = 0;i<prod.size();i++) {
			Expr current = prod.get(i);
			if(current instanceof Power) {
				Power currentPower = (Power)current;
				if(currentPower.getBase() instanceof Num) {
					
					prod.set(i, currentPower.simplify(settings));
					
				}
			}
		}
	}
	
	void prodContainsProd(Prod prod) {
		for(int i = 0;i<prod.size();i++) {
			Expr current = prod.get(i);
			if(current instanceof Prod) {
				for(int j = 0;j<current.size();j++) prod.add(current.get(j));//add all the sub expressions into this product
				prod.remove(i);//remove the sub product
				i--;//shift back after deletion
			}
		}
	}
	
	void expoIntoBase(Prod prod,Settings settings) {
		for(int i = 0;i<prod.size();i++) {
			Expr current = prod.get(i);
			if(current instanceof Power) {
				Power currentPower = (Power)current;
				if(currentPower.getExpo() instanceof Prod && currentPower.getBase() instanceof Num) {
					Prod expoProd = (Prod)currentPower.getExpo();
					
					for(int j = 0;j<expoProd.size();j++) {
						if(expoProd.get(j) instanceof Num) {
							Num num = (Num)expoProd.get(j);
							expoProd.remove(j);
							
							BigInteger newBase = ((Num)currentPower.getBase()).value.pow(num.value.intValue());
							
							currentPower.setBase( num(newBase) );
							currentPower.setExpo(expoProd.simplify(settings));
							
						}
					}
					
				}
			}
		}
	}
	
	void multiplyIntBases(Prod prod) {
		for(int i = 0;i<prod.size();i++) {
			Expr current = prod.get(i);
			
			if(current instanceof Power) {
				Power currentPower = (Power)current;
				if(currentPower.equalStruct(i())) continue;
				
				if(currentPower.getBase() instanceof Num) {
					Num numBase = (Num)currentPower.getBase();
					
					for(int j = i+1; j< prod.size(); j++) {
						
						Expr other = prod.get(j);
						if(other instanceof Power) {
							Power otherPower = (Power)other;
							if(otherPower.equalStruct(i())) continue;
							if(otherPower.getBase() instanceof Num && otherPower.getExpo().equalStruct(currentPower.getExpo())) {
								
								numBase.value = numBase.value.multiply(((Num)otherPower.getBase()).value);
								
								prod.remove(j);
								j--;
								
							}
							
						}
						
					}
					
				}
			}
			prod.set(i, current);//do not simplify because next step does that
		}
	}
	
	void expandIntBases(Prod prod,Settings settings) {
		
		for(int i = 0;i<prod.size();i++) {
			Expr current = prod.get(i);
			
			if(current instanceof Power) {
				Power currentPower = (Power)current;
				
				if(currentPower.getBase() instanceof Num && !(currentPower.getExpo() instanceof Num)) {
					Num numBase = (Num)currentPower.getBase();
					
					if(numBase.value.isProbablePrime(128) || numBase.value.equals(BigInteger.valueOf(-1))) continue;//skip primes
					
					Prod primeFactors = primeFactor(numBase);
					
					for(int j = 0;j<primeFactors.size();j++) {
						Power factor = (Power)primeFactors.get(j);
						if(((Num)factor.getExpo()).value.equals(BigInteger.ONE)) {
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
		
	}
	
	void multiplyLikeTerms(Prod prod,Settings settings) {//x*x -> x^2
		for(int i = 0;i < prod.size();i++) {
			
			Expr current = prod.get(i);//get the current object in the array check
			
			if(settings.powExpandMode && current instanceof Sum) continue; 
			
			Expr expo = new Sum();//create a sum object for the exponent
			
			if(current instanceof Num) continue;//ignore integers
			if(invObj.fastSimilarStruct(current)) {
				if(current.get() instanceof Num) continue;//ignore inverses
			}
			
			if(current instanceof Power) {
				Power currentCasted = (Power)current;
				current = currentCasted.getBase();//extract out the base and reassign current, current now represents the base of power
				expo.add(currentCasted.getExpo());//extract out the exponent
			}else expo.add(new Num(1));//if its not a power asume exponent to be 1
			
			if(settings.powExpandMode && current instanceof Sum) continue; 
			
			boolean found = false;
			for(int j = i+1; j< prod.size();j++) {
				Expr other = prod.get(j);
				
				if(other instanceof Num) continue;//ignore integers
				if(invObj.fastSimilarStruct(other)) {
					if(other.get() instanceof Num) continue;//ignore inverses
				}
				
				if(other.equalStruct(current)) {//if other has equal base
					expo.add(new Num(1L));
					prod.remove(j);
					j--;
					found = true;
				}else if(other instanceof Power) {
					Power otherCasted = (Power)other;
					if(otherCasted.getBase().equalStruct(current)) {//if other has equal base
						expo.add(otherCasted.getExpo());
						prod.remove(j);
						j--;
						found = true;
					}
				}
			}
			
			if(found) {
				Expr repl = new Power(current,expo);//replacement
				prod.set(i,repl.simplify(settings));//modify the element with the replacement
			}
			
		}
	}
	
	void checkForZero(Prod prod) {//x*0 -> 0
		boolean foundZero = false;
		for(int i = 0;i < prod.size();i++) {
			if(prod.get(i) instanceof Num) {
				Num casted = (Num)prod.get(i);
				if(casted.value.equals(BigInteger.ZERO)) {
					foundZero = true;
					break;
				}
			}
		}
		if(foundZero) {
			prod.clear();
			prod.add(new Num(0));
		}
	}
	
	void multiplyIntegersAndInverses(Prod prod) {
		
		int numCount = 0;
		for(int i = 0;i < prod.size();i++) {//look too see if there are integers
			if(prod.get(i) instanceof Num) numCount++;
			if(numCount > 0) break;
		}
		BigInteger total = BigInteger.ONE;
		if(numCount>=1) {//multiply regular integers
			
			for(int i = 0;i < prod.size();i++) {
				Expr expr = prod.get(i);
				if(expr instanceof Num) {
					Num exprCasted = (Num)expr;
					total = total.multiply(exprCasted.value);
					prod.remove(i);
					i--;
				}
			}
		}
		
		numCount = 0;
		for(int i = 0;i < prod.size();i++) {//check to see if there are inverses
			Expr current = prod.get(i);
			if(invObj.fastSimilarStruct(current)) {
				if(((Power)current).getBase() instanceof Num) numCount++;
			}
			if(numCount > 0) break;
		}
		BigInteger totalInv = BigInteger.ONE;
		if(numCount>=1) {//multiply inverses
		
			for(int i = 0;i < prod.size();i++) {
				Expr expr = prod.get(i);
				if(invObj.fastSimilarStruct(expr)) {
					if(((Power)expr).getBase() instanceof Num) {
						Num exprCasted = (Num)(((Power)expr).getBase());
						totalInv = totalInv.multiply(exprCasted.value);
						prod.remove(i);
						i--;
					}
				}
			}
		}
		
		if(!(total.equals(BigInteger.ONE) && totalInv.equals(BigInteger.ONE))) {//if can be reduced
			
			if(total.signum() == 1 && totalInv.signum() == -1) {//we prefer the numerator to be negative
				total = total.negate();
				totalInv = totalInv.negate();
			}
			
			BigInteger gcd = total.gcd(totalInv);//get the greatest common divisor
			
			total = total.divide(gcd);//reducing fraction
			totalInv = totalInv.divide(gcd);//reducing fraction
			
			if( totalInv.equals(BigInteger.valueOf(-1))) {//if 5/-1 -> -5/1
				total = total.negate();
				totalInv = BigInteger.ONE;
			}
			
			if( total.equals(BigInteger.valueOf(-1)) && !totalInv.equals(BigInteger.ONE) ) {// if -1/5 -> 1/-5
				totalInv = totalInv.negate();
				total = BigInteger.ONE;
			}
			
			if(total.signum() == -1 && totalInv.signum() == -1) {
				total = total.abs();
				totalInv = totalInv.abs();
			}
			
			if(!total.equals(BigInteger.ONE)) prod.add(num(total));
			if(!totalInv.equals(BigInteger.ONE)) prod.add(inv(num(totalInv)));
		}
	}
	
	Expr alone(Prod prod) {
		if(prod.size() == 1) {//if a sum is only one element 
			return prod.get(0);
		}else if(prod.size() == 0) {//if the sum is empty return 1
			return new Num(1);
		}
		return prod;
	}

	@Override
	public String toString() {
		String out = "";
		Expr prodCopy = (Prod)copy();
		if(prodCopy.size() < 2) out+="alone product:";
		int indexOfSwap = 0;
		if(prodCopy.size()>1) {//is starting with inverse try to swap with non inv so keep nice division sign
			if(invObj.fastSimilarStruct(prodCopy.get())) {
				for(int i = 1;i<prodCopy.size();i++) {
					if(!invObj.fastSimilarStruct(prodCopy.get(i))) {
						indexOfSwap = i;
						break;
					}
				}
			}
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
			
			if(i != 0 && invObj.fastSimilarStruct(e)) {
				Power casted = (Power)e;
				e = casted.getBase();
				div = true;
			}
			
			boolean nextIsDiv = false;
			if(i != prodCopy.size()-1) if(invObj.fastSimilarStruct(prodCopy.get(i+1))) nextIsDiv = true;
			
			if(i == 0 && !nextIsDiv && e instanceof Num) if(((Num) e).value.equals(BigInteger.valueOf(-1))) {
				out+='-';
				continue;
			}
			
			if(e instanceof Sum || e instanceof Prod) paren = true;
			
			if(div) out+='/';
			if(paren) out+='(';
			out+=e.toString();
			if(paren) out+=')';
			
			
			if(i != prodCopy.size()-1 && !nextIsDiv) out+="*";
			
		}
		return out;
	}
	@Override
	public Expr copy() {
		Expr prodCopy = new Prod();
		for(int i = 0;i<size();i++) {
			prodCopy.add(get(i).copy());
		}
		prodCopy.flags.set(flags);
		return prodCopy;
	}
	@Override
	public boolean equalStruct(Expr other) {//remember that x*y is the same as y*x
		if(other instanceof Prod) {//make sure same type
			
			if(other.size() == size()) {//make sure they are the same size
				
				boolean usedIndex[] = new boolean[size()];//keep track of what indices have been used
				int length = other.size();//length of the lists
				
				outer:for(int i = 0;i < length;i++) {
					for(int j = 0;j < length;j++) {
						if(usedIndex[j]) continue;
						if(get(i).equalStruct(other.get(j))) {
							usedIndex[j] = true;
							continue outer;
						}
					}
					return false;//the subExpr was never found 
				}
				
				return true;//they are the same as everything was found
				 
			}
		}
		return false;
	}
	@Override
	boolean similarStruct(Expr other,boolean checked) {
		if(other instanceof Prod) {
			
			sort();
			other.sort();
			
			if(!checked) if(checkForMatches(other) == false) return false;
			if(size() != other.size()) return false;
			
			boolean[] usedIndicies = new boolean[other.size()];
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Var) continue;//skip because they return true on anything
				boolean found = false;
				for(int j = 0;j<other.size();j++) {
					if(usedIndicies[j]) continue;
					else if(get(i).fastSimilarStruct(other.get(j))) {
						found = true;
						usedIndicies[j] = true;
						break;
					}
				}
				if(!found) return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public Expr replace(ArrayList<Equ> equs) {//substitute based on the given equations
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		Prod repl = new Prod();
		for(int i = 0;i < size();i++) repl.add(get(i).replace(equs));
		return repl;
	}
	@Override
	public long generateHash() {
		long sum = 1;
		for(int i = 0;i<size();i++) sum+=get(i).generateHash();//add all the sub expressions hashes
		
		return sum+2894621942862801234L;//random shift
	}
	@Override
	public double convertToFloat(ExprList varDefs) {
		double total = 1;
		for(int i = 0;i<size();i++) total*=get(i).convertToFloat(varDefs);
		return total;
	}
}
