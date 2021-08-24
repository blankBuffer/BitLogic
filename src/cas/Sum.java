package cas;
import java.math.BigInteger;
import java.util.*;

public class Sum extends Expr{
	
	private static final long serialVersionUID = 2026808885890783719L;
	public Sum() {
	}
	public Sum(Expr first,Expr second) {
		add(first);
		add(second);
	}
	
	@Override
	public Expr simplify(Settings settings) {
		
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify all the sub expressions
		
		distrSubProds((Sum)toBeSimplified,settings);
		
		sumContainsSum((Sum)toBeSimplified);//sums contains a sum
		
		addLogs((Sum)toBeSimplified,settings);
		
		addLikeTerms((Sum)toBeSimplified,settings);//x+x = 2*x
		
		addIntegersAndFractions((Sum)toBeSimplified);//1+2 = 3
		
		toBeSimplified = alone((Sum)toBeSimplified);//alone sum is 0
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	void addLogs(Sum sum, Settings settings) {
		IndexSet indexSet = new IndexSet();
		IndexSet indexOfProdWithLog = new IndexSet();
		
		for(int i = 0;i < size();i++) {
			if(sum.get(i) instanceof Log) indexSet.ints.add(i);
			else if(sum.get(i) instanceof Prod) {
				Prod innerProd = (Prod)sum.get(i);
				int innerLogCount = 0;
				for(int j = 0;j<innerProd.size();j++) {
					if(innerProd.get(j) instanceof Log) {
						innerLogCount++;
					}
				}
				if(innerLogCount == 1) {
					indexSet.ints.add(i);
					indexOfProdWithLog.ints.add(i);
				}
			}
		}
		
		if(indexSet.ints.size() > 1) {//turn x*ln(y) -> ln(y^x)
			for(Integer index:indexOfProdWithLog.ints) {
				int i = index.intValue();
				Expr prod = (Prod) sum.get(i);
				Prod nonLog = new Prod();
				for(int j = 0;j < prod.size();j++) {
					if(!(prod.get(j) instanceof Log)) {
						nonLog.add(prod.get(j));
						prod.remove(j);
						j--;
					}
				}
				Expr log = prod.get(0);
				
				Expr newInnerPow = pow(log.get(),nonLog).simplify(settings);
				log.set(0, newInnerPow);
				sum.set(i,log);
				
			}
			//now merge
			Prod innerProd = new Prod();
			for(int j = indexSet.ints.size()-1;j >= 0;j--) {
				int i = indexSet.ints.get(j);
				Expr log = (Log)sum.get(i);
				innerProd.add(log.get());
				sum.remove(i);
			}
			sum.add(ln(innerProd).simplify(settings));
		}
		
		
		
	}
	
	void distrSubProds(Sum sum,Settings settings) {
		for(int i = 0;i<sum.size();i++) {
			if(sum.get(i) instanceof Prod || (settings.powExpandMode && sum.get(i) instanceof Power)) {
				sum.set(i,  distr(sum.get(i)).simplify(settings));
			}
		}
	}
	
	void sumContainsSum(Sum sum) {
		for(int i = 0;i<sum.size();i++) {
			Expr current = sum.get(i);
			if(current instanceof Sum) {
				for(int j = 0;j<current.size();j++) sum.add(current.get(j));
				sum.remove(i);//delete from list to remove duplicates
				i--;//shift back after deletion
			}
		}
	}
	
	void addLikeTerms(Sum sum,Settings settings) {//x+x -> 2*x
		for(int i = 0;i<sum.size();i++) {
			Expr current = sum.get(i).copy();//make sure its copy as we don't want to modify the real object
			BigInteger coef = BigInteger.ONE;//coefficient
			
			if(current instanceof Prod) {//if its a product
				for(int j = 0;j<current.size();j++) {//look at each part of product
					Expr partOfProd = current.get(j);
					if(partOfProd instanceof Num) {//if its a number
						coef = ((Num)partOfProd).value;
						current.remove(j);
						current = current.simplify(settings);//simplify so that it does not stay a product if it becomes alone
						break;
					}
				}
			}
			
			boolean foundSame = false;
			for(int j = i+1;j < sum.size();j++) {//the i+1 is more efficient than 0 
				
				Expr toComp = sum.get(j).copy();//expression to compare to
				BigInteger toCompCoef = BigInteger.ONE;
				
				if(toComp instanceof Prod) {
					for(int k = 0;k<toComp.size();k++) {//look at each part of product
						Expr partOfProd = toComp.get(k);
						if(partOfProd instanceof Num) {//if its a number
							toCompCoef = ((Num)partOfProd).value;
							toComp.remove(k);
							toComp = toComp.simplify(settings);//simplify so that it does not stay a product if it becomes alone
							break;
						}
					}
				}
				
				if(current.equalStruct(toComp)) {
					sum.remove(j);
					j--;
					foundSame = true;
					coef = coef.add(toCompCoef);
				}
				
			}
			if(foundSame) {
				if(current instanceof Prod) {//if its a product still just add the coefficient
					current.add(num(coef));
				}else {//if not just make a new product
					current = prod(current,num(coef));
				}
				current = current.simplify(settings);//this has to be done in case of 0*x must become zero
				sum.set(i, current);//replace the sum element with the new combine like term version
			}
			
		}
	}
	
	void addIntegersAndFractions(Sum sum) {
		
		Num[] total = new Num[] {num(0),num(1)};
		
		for(int i = 0;i < sum.size();i++) {
			Num[] frac = extractNormalFrac(sum.get(i));
			if(frac != null) {
				total[0].value = total[0].value.multiply(frac[1].value).add(frac[0].value.multiply(total[1].value));
				total[1].value = total[1].value.multiply(frac[1].value);
				sum.remove(i);
				i--;
			}
		}
		boolean negative = total[0].value.signum()*total[1].value.signum() == -1;
		total[0].value = total[0].value.abs();
		total[1].value = total[1].value.abs();
		BigInteger gcd = total[0].value.gcd(total[1].value);
		total[0].value = total[0].value.divide(gcd);
		total[1].value = total[1].value.divide(gcd);
		
		if(total[0].value.equals(BigInteger.ZERO)) return;
		
		if(total[1].value.equals(BigInteger.ONE)) {
			if(!total[0].value.equals(BigInteger.ONE)) {
				if(negative) total[0].value = total[0].value.negate();
				sum.add(total[0]);
				return;
			}
		}
		
		if(total[0].value.equals(BigInteger.ONE)) {
			if(!total[1].value.equals(BigInteger.ONE)) {
				if(negative) total[1].value = total[1].value.negate();
				sum.add(inv(total[1]));
				return;
			}
		}
		
		if(total[0].value.equals(BigInteger.ONE) && total[1].value.equals(BigInteger.ONE)) {
			if(negative) sum.add(num(-1));
			else sum.add(num(1));
			
			return;
		}
		
		if(negative) total[0].value = total[0].value.negate();
		sum.add(div(total[0],total[1]));
		
	}
	
	Expr alone(Sum sum) {
		if(sum.size() == 1) {//if a sum is only one element 
			return sum.get(0);
		}else if(sum.size() == 0) {//if the sum is empty return 0
			return num(0);
		}
		return sum;
	}

	@Override
	public String toString() {
		String out = "";
		if(size() < 2) out+="alone sum:";
		for(int i = 0;i < size();i++) {
			out+=get(i).toString();
			boolean useNothing = false;
			
			if(i!=size()-1) {
				Expr next = get(i+1);
				if(next instanceof Num) {
					Num numCatsed  = (Num)next;
					if(numCatsed.value.signum()==-1) useNothing = true;
				}else if(next instanceof Prod){
					Num numCasted = null;
					for(int j = 0;j<next.size();j++) {
						if(next.get(j) instanceof Num) {
							numCasted = (Num)next.get(j);
							break;
						}
					}
					if(numCasted != null) {
						if(numCasted.value.signum()==-1) useNothing = true;
					}
				}
			}
			
			if(i != size()-1) {
				if(!useNothing) out+='+';
			}
		}
		return out;
	}
	@Override
	public Expr copy() {
		Expr sumCopy = new Sum();
		for(int i = 0;i<size();i++) {
			sumCopy.add(get(i).copy());
		}
		sumCopy.flags.set(flags);
		return sumCopy;
	}
	@Override
	public boolean equalStruct(Expr other) {//x+y = y+x
		if(other instanceof Sum) {//make sure same type
			
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
		if(other instanceof Sum) {
			
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
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		Sum repl = new Sum();
		for(int i = 0;i<size();i++) repl.add(get(i).replace(equs));
		return repl;
	}
	@Override
	public long generateHash() {
		long sum = 1;
		for(int i = 0;i<size();i++) sum+=get(i).generateHash();//add all sub expressions hashes
		
		return sum-926481637408623462L;//again arbitrary digits
	}
	@Override
	public double convertToFloat(ExprList varDefs) {
		double total = 0;
		for(int i = 0;i<size();i++) total+=get(i).convertToFloat(varDefs);
		return total;
	}

}
