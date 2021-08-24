package cas;
import java.math.BigInteger;
import java.util.ArrayList;


public class QuickMath {
	/*
	 * this file is for shortcuts and general algorithms used everywhere
	 */
	
	static Expr createExpr(String expr) {
		return Interpreter.createExpr(expr);
	}
	
	static class IndexSet{
		ArrayList<Integer> ints = new ArrayList<Integer>();
		void print() {
			for(int i:ints) System.out.print(i+",");
		}
		void println() {
			print();
			System.out.println();
		}
	}
	static class VarCount implements Comparable<VarCount>{
		Var v;
		int count = 0;
		VarCount(Var v,int c){
			this.v = v;
			this.count = c;
		}
		void print() {
			System.out.println(v.toString());
			System.out.print(": "+count);
		}
		void println() {
			print();
			System.out.println();
		}
		@Override
		public int compareTo(VarCount other) {
			return -Integer.compare(count, other.count);
		}
	}
	
	//
	public static Expr sqrtObj = sqrt(var("x"));//used for comparing to
	public static Expr invObj = inv(var("x"));//used for comparing to
	public static Expr cbrtObj = cbrt(var("x"));//used for comparing to
	
	//
	public static Power pow(Expr a,Expr b) {
		return new Power(a,b);
	}
	public static Sum sum(Expr a,Expr b) {
		return new Sum(a,b);
	}
	public static Sum sum(Expr a,Expr b,Expr c) {
		Sum out = new Sum();
		out.add(a);
		out.add(b);
		out.add(c);
		return out;
	}
	public static Prod prod(Expr a,Expr b) {
		return new Prod(a,b);
	}
	public static Prod prod(Expr a,Expr b,Expr c) {
		Prod out = new Prod();
		out.add(a);
		out.add(b);
		out.add(c);
		return out;
	}
	public static Var var(String s) {
		return new Var(s);
	}
	public static Num num(long i) {
		return new Num(i);
	}
	public static Num num(BigInteger i) {
		return new Num(i);
	}
	public static Num num(String s) {
		return new Num(s);
	}
	public static FloatExpr floatExpr(double v) {
		return new FloatExpr(v);
	}
	public static FloatExpr floatExpr(String s) {
		return new FloatExpr(s);
	}
	public static Equ equ(Expr a,Expr b) {
		return new Equ(a,b);
	}
	public static BoolState bool(boolean b) {
		return new BoolState(b);
	}
	public static Log ln(Expr a) {
		return new Log(a);
	}
	public static Sum sub(Expr a,Expr b) {
		return sum(a,prod(num(-1),b));
	}
	public static Power inv(Expr a) {
		return pow(a,num(-1));
	}
	public static Power sqrt(Expr a) {
		return pow(a,inv(num(2)));
	}
	public static Power cbrt(Expr a) {
		return pow(a,inv(num(3)));
	}
	public static Prod neg(Expr a) {
		return prod(num(-1),a);
	}
	public static Prod div(Expr a,Expr b) {
		return prod(a,inv(b));
	}
	public static E e() {
		return new E();
	}
	public static Pi pi() {
		return new Pi();
	}
	public static Power i() {
		return sqrt(num(-1));
	}
	public static Diff diff(Expr e,Var v) {
		return new Diff(e,v);
	}
	public static Integrate integrate(Expr e,Var v) {
		return new Integrate(e,v);
	}
	public static IntegrateOver integrateOver(Expr min,Expr max,Expr e,Var v) {
		return new IntegrateOver(min,max,e,v);
	}
	public static Solve solve(Equ e,Var v) {
		return new Solve(e,v);
	}
	public static Power exp(Expr expr) {
		return pow(e(),expr);
	}
	public static Sin sin(Expr expr) {
		return new Sin(expr);
	}
	public static Cos cos(Expr expr) {
		return new Cos(expr);
	}
	public static Tan tan(Expr expr) {
		return new Tan(expr);
	}
	public static Atan atan(Expr expr) {
		return new Atan(expr);
	}
	public static Approx approx(Expr expr,ExprList defs) {
		return new Approx(expr,defs);
	}
	public static Factor factor(Expr expr) {
		return new Factor(expr);
	}
	public static Distr distr(Expr expr) {
		return new Distr(expr);
	}
	//
	
	static Expr[] extractFrac(Expr in) {//general fraction extraction
		if(in instanceof Prod) {
			Prod prod = (Prod)in;
			Prod numerParts = new Prod();
			Prod denomParts = new Prod();
			for(int j = 0;j<prod.size();j++) {
				if(prod.get(j) instanceof Power) {
					Power pow = (Power)prod.get(j);
					
					if(pow.getExpo().negative()) {
						denomParts.add(pow.copy());
					}else {
						numerParts.add(pow.copy());
					}
					
				}else {
					numerParts.add(prod.get(j).copy());
				}
				
			}
			return new Expr[] {numerParts,denomParts};
		}else if(in instanceof Power) {
			Power pow = (Power)in;
			
			if(pow.getExpo().negative()) {
				return new Expr[] {num(1),pow.copy()};
			}else {
				return new Expr[] {pow.copy(),num(1)};
			}
		}
		return new Expr[] {in.copy(),num(1)};
	}
	
	static Num[] extractNormalFrac(Expr in){//numerical based fraction extraction, returns null if it does not meet criteria
		Num num = null,den = null;
		if(in instanceof Num) {
			num = (Num)in.copy();
			den = num(1);
			return new Num[] {num,den};
		}else if(invObj.fastSimilarStruct(in)) {
			if(in.get() instanceof Num) {
				den = (Num)in.get().copy();
				if(den.value.signum() == -1) {
					num = num(-1);
					den.value = den.value.abs();
				}else num = num(1);
				
				return new Num[]{num,den};
			}
		}else if(in instanceof Prod && in.size() == 2) {
			for(int i = 0;i<2;i++) {
				if(in.get(i) instanceof Num) {
					num = (Num)in.get(i).copy();
				}else if(invObj.fastSimilarStruct(in.get(i))) {
					Power inv = (Power)in.get(i);
					if(inv.get() instanceof Num) {
						den = (Num)inv.get().copy();
					}
				}
				
			}
			if(num!=null && den !=null) {
				if(den.value.signum() == -1) {
					den.value = den.value.abs();
					num.value = num.value.negate();
				}
				return new Num[]{num,den};
			}
		}
		return null;
	}
	
	public static ExprList[] polyDiv(ExprList num,ExprList den,Settings settings) {//returns output + remainder
		ExprList remain = (ExprList)num.copy();
		Num zero = num(0);
		
		ExprList out = new ExprList();
		
		while(remain.size() >= den.size()) {
			if(den.get(den.size()-1).equalStruct(zero)) {//avoid divide by zero situation
				out.add(0, zero.copy());
				remain.remove(remain.size()-1);//pop last element
				continue;
			}
			
			
			Expr coef = div(remain.get(remain.size()-1).copy(),den.get(den.size()-1).copy()).simplify(settings);
			out.add(0, coef);
			coef = neg(coef);
			
			
			for(int i = remain.size()-den.size();i<remain.size()-1;i++) {//we can skip last one since we know it will be deleted
				remain.set(i, sum( prod(den.get(i-(remain.size()-den.size()) ),coef) ,remain.get(i)).simplify(settings) );
			}
			remain.remove(remain.size()-1);//pop last element
			
			
		}
		while(remain.size()>0 && remain.get(remain.size()-1).equalStruct(zero)) {//clean zeros off end
			remain.remove(remain.size()-1);
		}
		
		return new ExprList[] { out,remain };
	}
	
	public static BigInteger degree(Expr expr,Var v) {
		if(expr instanceof Sum) {
			
		}else if(expr instanceof Power) {
			Power casted = (Power)expr;
			if(casted.getBase().equalStruct(v)) {
				
			}
		}
		
		
		
		return BigInteger.ZERO;
	}
	
	public static boolean isPolynomial(Expr expr,Var v) {
		
		Sum sum = null;
		if(expr instanceof Sum) {
			sum = (Sum)expr;
		}else {
			sum = new Sum();
			sum.add(expr);
		}
		
		for(int i = 0;i<sum.size();i++) {
			Expr e = sum.get(i);
			Expr contents = null;
			
			if(e.contains(v)) {
				if(e instanceof Prod) {
					int indexOfVarThing = 0;
					
					int countCheck = 0;
					for(int j = 0;j < e.size();j++) {
						if(e.get(j).contains(v)) {
							countCheck++;
							indexOfVarThing = j;
						}
					}
					
					if(countCheck != 1) return false;
					
					contents = (Prod)e.copy();
					e = contents.get(indexOfVarThing);
					contents.remove(indexOfVarThing);
					
					if(e.equalStruct(v)) {
						continue;
					}else if(e instanceof Power) {
						Power casted = (Power)e;
						if(casted.getExpo() instanceof Num && casted.getBase().equalStruct(v)) {
							Num num = (Num)casted.getExpo();
							if(num.value.compareTo(BigInteger.ZERO) != 1) return false;
						}else return false;
					}else return false;
					
				}else if(e.equalStruct(v)) {
					continue;
				}else if(e instanceof Power) {
					Power casted = (Power)e;
					if(casted.getExpo() instanceof Num && casted.getBase().equalStruct(v)) {
						Num num = (Num)casted.getExpo();
						if(num.value.compareTo(BigInteger.ZERO) != 1) return false;
					}else return false;
				}else return false;
			}
			
			
		}
		
		return true;
	}
	
	public static ExprList polyExtract(Expr expr,Var v,Settings settings) {
		BigInteger maxDegree = BigInteger.valueOf(16);
		ExprList coef = new ExprList();
		
		Sum sum = null;
		if(expr instanceof Sum) {
			sum = (Sum)expr;
		}else {
			sum = new Sum();
			sum.add(expr);
		}
		
		for(int i = 0;i<sum.size();i++) {
			Expr e = sum.get(i);
			BigInteger degree = BigInteger.ZERO;
			
			boolean prod = false;
			Expr contents = null;
			
			if(e.contains(v)) {
				if(e instanceof Prod) {
					prod = true;
					int indexOfVarThing = 0;
					
					int countCheck = 0;
					for(int j = 0;j < e.size();j++) {
						if(e.get(j).contains(v)) {
							countCheck++;
							indexOfVarThing = j;
						}
					}
					
					if(countCheck != 1) return null;
					
					contents = (Prod)e.copy();
					e = contents.get(indexOfVarThing);
					contents.remove(indexOfVarThing);
					
					if(e.equalStruct(v)) {
						degree = BigInteger.ONE;
					}else if(e instanceof Power) {
						Power casted = (Power)e;
						if(casted.getExpo() instanceof Num && casted.getBase().equalStruct(v)) {
							Num num = (Num)casted.getExpo();
							if(num.value.compareTo(BigInteger.ZERO) == 1) degree = num.value;
							else return null;
						}else return null;
					}else return null;
					
				}else if(e.equalStruct(v)) {
					degree = BigInteger.ONE;
				}else if(e instanceof Power) {
					Power casted = (Power)e;
					if(casted.getExpo() instanceof Num && casted.getBase().equalStruct(v)) {
						Num num = (Num)casted.getExpo();
						if(num.value.compareTo(BigInteger.ZERO) == 1) degree = num.value;
						else return null;
					}else return null;
				}else return null;
			}
			if(degree.compareTo(maxDegree) == 1) {
				return null;
			}
			while(BigInteger.valueOf(coef.size()).compareTo(degree) <= 0) {//resize coef length to fit degree size
				coef.add(num(0));
			}
			
			int degreeInt = degree.intValue();
			if(degreeInt == 0) {
				coef.set(0,sum(coef.get(0), e ));
				continue;
			}
			
			if(prod) {
				coef.set(degreeInt,sum(coef.get(degreeInt),contents));
			}else {
				coef.set(degreeInt,sum(coef.get(degreeInt),num(1)));
			}
			
		}
		
		coef.simplifyChildren(settings);
		
		return coef;
	}
	
	Expr exprListToPoly(ExprList poly,Var v,Settings settings){
		if(poly.size()==0) return num(0);
		Sum out = new Sum();
		for(int i = 0;i<poly.size();i++) {
			if(i == 0) {
				out.add(poly.get(i));
			}else if(i == 1){
				out.add(prod(poly.get(i),v));
			}else {
				out.add(prod(poly.get(i),pow(v,num(i))));
			}
		}
		
		return out.simplify(settings);
	}
	
	public static BigInteger bigRoot(BigInteger n,BigInteger root) {
		BigInteger x = BigInteger.ZERO.setBit(n.bitLength() / root.intValue() + 1);//set minimum guess
		while (true) {
			BigInteger y = x.multiply(root.subtract(BigInteger.ONE)).add(n.divide(x.pow(root.intValue()-1))).divide(root);//newton's method of convergence
			if (y.compareTo(x) >= 0) break;//converged
			x = y;
		}
		return x;
	}
	
	public static Power perfectPower(Num n) {
		if(n.value.compareTo(BigInteger.TWO) == -1) return pow(n.copy(),num(1));//make it accept any number without errors
		int currentExpo = n.value.bitLength();//maximum exponent is log base 2 of that number
		while(currentExpo != 1) {
			BigInteger possibleBase = bigRoot(n.value,BigInteger.valueOf(currentExpo));
			if(possibleBase.pow(currentExpo).equals(n.value)) {
				return pow(num(possibleBase),num(currentExpo));
			}
			currentExpo--;
		}
		return pow(n.copy(),num(1));
	}
	
	static ArrayList<Integer> smallPrimes = new ArrayList<Integer>();//small prime cache
	static ArrayList<BigInteger> recentBigPrimes = new ArrayList<BigInteger>();//recent big primes
	static boolean madePrimeCache = false;
	
	static int smallPrimeIterations = 8192;
	
	static void generatePrimeCache() {
		smallPrimes.add(2);
		smallPrimes.add(3);
		
		for(int n = 1;n<smallPrimeIterations;n++) {
			int possiblePrime1 = 6*n-1;
			int possiblePrime2 = 6*n+1;
			
			boolean found = false;
			for(int i:smallPrimes) {
				if(possiblePrime1%i==0) {
					found = true;
					break;
				}
			}
			if(!found) smallPrimes.add(possiblePrime1);
			
			found = false;
			for(int i:smallPrimes) {
				if(possiblePrime2%i==0) {
					found = true;
					break;
				}
			}
			if(!found) smallPrimes.add(possiblePrime2);
		}
		madePrimeCache = true;
	}
	
	public static Prod primeFactor(Num n) {
		while(recentBigPrimes.size()>1024) {
			recentBigPrimes.remove(0);//remove old
		}
			
		if(!madePrimeCache) generatePrimeCache();
		Prod p = new Prod();
		BigInteger whatsLeft = n.value;
		if(whatsLeft.signum()==-1) {
			whatsLeft = whatsLeft.abs();
			p.add(pow(num(-1),num(1)));
		}
		
		//check big prime cache
		
		for(BigInteger i:recentBigPrimes) {
			
			if(whatsLeft.mod(i).equals(BigInteger.ZERO)) {
				int expoCount = 1;
				whatsLeft = whatsLeft.divide(i);
				while(whatsLeft.mod(i).equals(BigInteger.ZERO)) {
					whatsLeft = whatsLeft.divide(i);
					expoCount++;
				}
				p.add(pow(num(i),num(expoCount)));
			}
		}
					
		if(whatsLeft.equals(BigInteger.ONE)) return p;
		if(whatsLeft.isProbablePrime(128)) {
			p.add(pow(num(whatsLeft),num(1)));
			return p;
		}
		
		int counter = 0;
		int checkSpacing = 32;
		
		
		//go through small primes
		for(int i:smallPrimes) {
			counter++;
			BigInteger bigI = BigInteger.valueOf(i);
			if(counter%checkSpacing == 0) {
				if(bigI.compareTo(whatsLeft.sqrt()) == 1) break;
				if(whatsLeft.isProbablePrime(128)) {
					p.add(pow(num(whatsLeft),num(1)));
					return p;
				}
			}
			if(whatsLeft.mod(bigI).equals(BigInteger.ZERO)) {
				int expoCount = 1;
				whatsLeft = whatsLeft.divide(bigI);
				while(whatsLeft.mod(bigI).equals(BigInteger.ZERO)) {
					whatsLeft = whatsLeft.divide(bigI);
					expoCount++;
				}
				p.add(pow(num(i),num(expoCount)));
			}
		}
			
		
		
		if(whatsLeft.equals(BigInteger.ONE)) return p;
		if(whatsLeft.isProbablePrime(128)) {
			p.add(pow(num(whatsLeft),num(1)));
			return p;
		}
		
		
		//look for new big primes
		BigInteger currentPrime = BigInteger.valueOf(smallPrimeIterations*6+1).nextProbablePrime();
		while(currentPrime.compareTo(BigInteger.valueOf(1000000)) == -1) {
			counter++;
			if(counter%checkSpacing == 0) {
				if(currentPrime.compareTo(whatsLeft.sqrt()) == 1) break;
				if(whatsLeft.isProbablePrime(128)) {
					p.add(pow(num(whatsLeft),num(1)));
					return p;
				}
			}
				
			if(whatsLeft.mod(currentPrime).equals(BigInteger.ZERO)) {
				if(!recentBigPrimes.contains(currentPrime)) recentBigPrimes.add(currentPrime);
				int expoCount = 1;
				whatsLeft = whatsLeft.divide(currentPrime);
				while(whatsLeft.mod(currentPrime).equals(BigInteger.ZERO)) {
					whatsLeft = whatsLeft.divide(currentPrime);
					expoCount++;
				}
				p.add(pow(num(currentPrime),num(expoCount)));
			}
			currentPrime = currentPrime.nextProbablePrime();
		}
			
		
		
		if(!whatsLeft.equals(BigInteger.ONE)) {
			recentBigPrimes.add(whatsLeft);//we pretend that what's left is prime
			p.add(pow(num(whatsLeft),num(1)));
		}
		
		return p;
	}
	
}
