package cas;
import java.math.BigInteger;

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
		
		factorSubSums((Prod)toBeSimplified,settings);//factor sums
		
		trigExpandElements((Prod)toBeSimplified, settings);
		
		toBeSimplified = combineWithDiv((Prod)toBeSimplified,settings);
				
		if(toBeSimplified instanceof Prod) {
			prodContainsProd((Prod)toBeSimplified);//product contains a product
		
			boolean hasManyIntBases = hasManyIntBases();//improves performance
				
			if(hasManyIntBases) expandIntBases((Prod)toBeSimplified,settings);//12^x*m -> 2^(2*x)*3^x*m
			
			multiplyLikeTerms((Prod)toBeSimplified,settings);//x*x = x^2
			
			if(hasManyIntBases) expoIntoBase((Prod)toBeSimplified,settings);//10^(2*x)*a -> 100^x*a
				
			if(hasManyIntBases)multiplyIntBases((Prod)toBeSimplified);//2^x*10^x -> 20^x
				
			if(hasManyIntBases)simplifyIntBasePowers((Prod)toBeSimplified,settings);//re simplify powers with int bases
			
			multiplyIntegersAndInverses((Prod)toBeSimplified);//2*3 = 6
			
			checkForZero((Prod)toBeSimplified);//x*0 -> 0
			
			toBeSimplified = Prod.unCast(toBeSimplified);//if a product only has 1 element
		}
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		return toBeSimplified;
	}
	
	static void trigExpandElements(Prod prod,Settings settings){//expands double angle to allow things to cancel out
		if(prod.containsType(Sin.class)){
			for(int i = 0;i < prod.size();i++){
				prod.set(i, trigExpand(prod.get(i),settings));
			}
		}
	}
	
	static Expr combineWithDiv(Prod prod,Settings settings) {//combines into a div if there is a div in the product
		
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
	
	static void factorSubSums(Prod prod,Settings settings) {
		for(int i = 0;i<prod.size();i++) if(prod.get(i) instanceof Sum) prod.set(i,  factor(prod.get(i)).simplify(settings));
	}
	
	
	static void simplifyIntBasePowers(Prod prod,Settings settings) {
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
	
	static void prodContainsProd(Prod prod) {
		for(int i = 0;i<prod.size();i++) {
			Expr current = prod.get(i);
			if(current instanceof Prod) {
				for(int j = 0;j<current.size();j++) prod.add(current.get(j));//add all the sub expressions into this product
				prod.remove(i);//remove the sub product
				i--;//shift back after deletion
			}
		}
	}
	
	static void expoIntoBase(Prod prod,Settings settings) {//10^(2*x)*a -> 100^x*a
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
	}
	
	static void multiplyIntBases(Prod prod) {
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
							if(otherPower.getBase() instanceof Num && otherPower.getExpo().equalStruct(currentPower.getExpo())) {
								
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
	}
	
	static void expandIntBases(Prod prod,Settings settings) {
		
		for(int i = 0;i<prod.size();i++) {
			Expr current = prod.get(i);
			
			if(current instanceof Power) {
				Power currentPower = (Power)current;
				
				if(currentPower.getBase() instanceof Num && !(currentPower.getExpo() instanceof Num)) {
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
		
	}
	
	static void multiplyLikeTerms(Prod prod,Settings settings) {//x*x -> x^2
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
				if(otherCasted.getBase().equalStruct(current)) {//if other has equal base
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
	}
	
	static void checkForZero(Prod prod) {//x*0 -> 0
		boolean foundZero = false;
		for(int i = 0;i < prod.size();i++) {
			if(prod.get(i) instanceof Num) {
				Num casted = (Num)prod.get(i);
				if(casted.equalStruct(Num.ZERO)) {
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
	
	static void multiplyIntegersAndInverses(Prod prod) {//incomplete
		
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
		
		if(!total.equalStruct(Num.ONE)) prod.add(total);
		
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
	public long generateHash() {
		long sum = 1;
		for(int i = 0;i<size();i++) sum+=get(i).generateHash();//add all the sub expressions hashes
		
		return sum+2894621942862801234L;//random shift
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
