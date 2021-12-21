package cas;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;


public class QuickMath {
	/*
	 * this file is for shortcuts and general algorithms used everywhere
	 */
	
	public static final Expr nullExpr = null;
	
	public static Expr createExpr(String expr) {
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
		public String toString(){
			return v.toString()+": "+count;
		}
		
		@Override
		public int compareTo(VarCount other) {
			return -Integer.compare(count, other.count);
		}
	}
	
	//
	public static Expr sqrtObj = sqrt(var("x"));//used for comparing to
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
	public static Num num(long r,long i) {
		return new Num(r,i);
	}
	public static Num num(BigInteger i) {
		return new Num(i);
	}
	public static Num num(String s) {
		return new Num(s);
	}
	public static Num num(BigInteger real,BigInteger imag) {
		return new Num(real,imag);
	}
	public static FloatExpr floatExpr(ComplexFloat complexFloat) {
		return new FloatExpr(complexFloat);
	}
	public static FloatExpr floatExpr(double d) {
		return new FloatExpr(d);
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
	public static Div inv(Expr a) {
		return div(num(1),a);
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
	public static Div div(Expr a,Expr b) {
		return new Div(a,b);
	}
	public static E e() {
		return new E();
	}
	public static Pi pi() {
		return new Pi();
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
	public static Div sinh(Expr expr) {
		return div(sub(exp(expr),exp(neg(expr))),num(2));
	}
	public static Div cosh(Expr expr) {
		return div(sum(exp(expr),exp(neg(expr))),num(2));
	}
	public static Div tanh(Expr expr) {
		return div(sub(exp(expr),exp(neg(expr))),sum(exp(expr),exp(neg(expr))));
	}
	public static Atan atan(Expr expr) {
		return new Atan(expr);
	}
	public static Asin asin(Expr expr) {
		return new Asin(expr);
	}
	public static Acos acos(Expr expr) {
		return new Acos(expr);
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
	public static Gamma gamma(Expr expr) {
		return new Gamma(expr);
	}
	public static LambertW lambertW(Expr expr){
		return new LambertW(expr);
	}
	public static Func func(String name,ExprList vars,Expr expr) {
		return new Func(name,vars,expr);
	}
	public static Func func(String name,Equ v,Expr expr) {
		return new Func(name,v,expr);
	}
	//
	/*
	public static boolean allLinearTerms(Expr e) {
		if(e instanceof Prod) {
			
		}else {
			
		}
	}
	*/
	public static Expr partialFrac(Expr expr,Var v,Settings settings) {//being re written, currently disabled
		if(expr instanceof Div) {
			BigInteger negOne = BigInteger.valueOf(-1);
			Div frac = (Div)expr;
			BigInteger numerDegree = degree( frac.getNumer() ,v);
			BigInteger denomDegree = degree( frac.getDenom() ,v);
			
			if(numerDegree.equals(negOne) || denomDegree.equals(negOne)) return expr;//not polynomials
			if(denomDegree.compareTo(numerDegree) != 1) return expr;//denominator needs a greater degree
			Prod denomFactored = Prod.cast(factor(frac.getDenom()).simplify(settings));
			//if(!allLinearTerms( stripNonVarPartsFromProd(denomFactored,v) )) return expr;
			//all checks done
			System.out.println(denomFactored);
			
		}
		return expr;
	}
	
	public static Expr polyDiv(Expr expr,Var v,Settings settings) {
		if(expr instanceof Div) {
			Div frac = (Div)expr.copy();
			ExprList numPoly = polyExtract(distr(frac.getNumer()).simplify(settings),v,settings);
			ExprList denPoly = polyExtract(distr(frac.getDenom()).simplify(settings),v,settings);
			if(numPoly != null && denPoly != null && numPoly.size()>=denPoly.size()) {
				ExprList[] result = polyDiv(numPoly,denPoly,settings);
				Expr outPart =  exprListToPoly(result[0],v,settings);
				Expr remainPart =  div(exprListToPoly(result[1],v,settings),frac.getDenom());
				expr = sum(outPart,remainPart);
			}
		}
		return expr;
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
	
	public static BigInteger degree(Expr expr,Var v) {//returns -1 if it is not possible
		BigInteger negOne = BigInteger.valueOf(-1);
		Sum exprSum = Sum.cast(expr);
		BigInteger maxDegree = BigInteger.ZERO;
		for(int i = 0;i<exprSum.size();i++) {
			Expr term = exprSum.get(i);
			Expr stripped = stripNonVarPartsFromProd(term,v);
			if(stripped.equalStruct(v)) maxDegree = maxDegree.max(BigInteger.ONE);
			else if(stripped instanceof Power) {
				Power casted = (Power)stripped;
				if(isPositiveRealNum(casted.getExpo())) {
					if(casted.getBase().equalStruct(v)) {
						maxDegree = maxDegree.max(((Num)casted.getExpo()).realValue);
					}else if(isPolynomial(casted.getBase(),v)) {
						maxDegree = maxDegree.max( degree(casted.getBase(),v).multiply(((Num)casted.getExpo()).realValue) );
					}else return negOne;
				}else return negOne;
			}else if(stripped instanceof Prod) {
				BigInteger subDegreeSum = BigInteger.ZERO;
				for(int j = 0;j<stripped.size();j++) {
					BigInteger childDegree = degree(stripped.get(j),v);
					if(childDegree.equals(negOne)) return negOne;
					subDegreeSum = subDegreeSum.add(childDegree);
				}
				maxDegree = maxDegree.max(subDegreeSum);
			}else if(stripped instanceof Sum) {
				BigInteger childDegree = degree(stripped,v);
				if(childDegree.equals(negOne)) return negOne;
				maxDegree = maxDegree.max(childDegree);
			}else if(stripped instanceof Num) continue;
			else return negOne;
		}
		return maxDegree;
	}
	
	public static boolean isPolynomial(Expr expr,Var v) {
		Sum exprSum = Sum.cast(expr);
		for(int i = 0;i<exprSum.size();i++) {
			Expr term = exprSum.get(i);
			Expr stripped = stripNonVarPartsFromProd(term,v);
			if(stripped.equalStruct(v)) continue;
			else if(stripped instanceof Power) {
				Power casted = (Power)stripped;
				if(casted.getBase().equalStruct(v) && isPositiveRealNum(casted.getExpo())) continue;
				return false;
			}else if(stripped instanceof Num) continue;
			else return false;
		}
		return true;
	}
	public static boolean isRealNum(Expr e) {
		return e instanceof Num && !((Num)e).isComplex();
	}
	public static boolean isPositiveRealNum(Expr e) {
		return isRealNum(e) && !e.negative();
	}
	
	public static ExprList polyExtract(Expr expr,Var v,Settings settings) {
		BigInteger maxDegree = BigInteger.valueOf(16);
		ExprList coef = new ExprList();
		Sum sum = Sum.cast(expr);
		for(int i = 0;i<sum.size();i++) {
			Expr e = sum.get(i);
			BigInteger degree = BigInteger.ZERO;
			Expr contents = null;
			if(e.contains(v)) {
				ExprList parts = seperateByVar(e,v);
				if(parts.get(1).equalStruct(v)) {
					contents = parts.get(0);
					degree = BigInteger.ONE;
				}else if(parts.get(1) instanceof Power) {
					Power casted = (Power)parts.get(1);
					if(casted.getBase().equalStruct(v) && isPositiveRealNum(casted.getExpo())) {
						degree = ((Num)casted.getExpo()).realValue;
					}else return null;
					contents = parts.get(0);
				}else return null;
			}else contents = e;
			if(degree.compareTo(maxDegree) == 1) return null;//don't want too big of a polynomial
			while(BigInteger.valueOf(coef.size()).compareTo(degree) <= 0) coef.add(num(0));//resize coef length to fit degree size
			int degreeInt = degree.intValue();
			coef.set(degreeInt,sum(coef.get(degreeInt),contents));
		}
		coef.simplifyChildren(settings);
		return coef;
	}
	
	public static Expr stripNonVarPartsFromProd(Expr e,Expr v) {//a*x^2/c -> x^2, its like the opposite of getting the coefficient
		if(e.contains(v)) {
			if(e instanceof Prod) {
				Prod eCasted = (Prod)e.copy();
				for(int i = 0;i<eCasted.size();i++) {
					if(!eCasted.get(i).contains(v)) {
						eCasted.remove(i);
						i--;
					}
				}
				return Prod.unCast(eCasted);
			}else if(e instanceof Div) {
				Div eCasted = (Div)e;
				Expr numer = stripNonVarPartsFromProd(eCasted.getNumer(),v);
				Expr denom = stripNonVarPartsFromProd(eCasted.getDenom(),v);
				return Div.unCast(div(numer,denom));
			}
			return e.copy();
		}
		return num(1);
	}
	
	public static ExprList seperateByVar(Expr e,Expr v) {//returns [coef,var parts]
		ExprList out = new ExprList();
		if(!e.contains(v)) {
			out.add(e.copy());
			out.add(num(1));
		}else if(e instanceof Prod) {
			Expr outCopy = e.copy();
			Expr coef = new Prod();
			
			for(int i = 0;i < outCopy.size();i++) {
				if(!outCopy.get(i).contains(v)) {
					coef.add(outCopy.get(i));
					outCopy.remove(i);
					i--;
				}
			}
			
			out.add(Prod.unCast(coef));
			out.add(Prod.unCast(outCopy));
			
		}else if(e instanceof Div) {
			Div casted = (Div)e;
			ExprList numer = seperateByVar(casted.getNumer(),v);
			ExprList denom = seperateByVar(casted.getDenom(),v);
			
			Expr coef = Div.unCast(div(numer.get(0),denom.get(0)));
			Expr newExpr = Div.unCast(div(numer.get(1),denom.get(1)));
			
			out.add(coef);
			out.add(newExpr);
			
		}else {
			out.add(num(1));
			out.add(e.copy());
		}
		return out;
	}
	
	public static ExprList seperateCoef(Expr e) {//returns [coef,remain]
		ExprList out = new ExprList();
		if(e instanceof Prod) {
			Prod prodCopy = (Prod)e.copy();
			for(int i = 0;i<prodCopy.size();i++) {
				if(prodCopy.get(i) instanceof Num) {
					out.add(prodCopy.get(i));
					prodCopy.remove(i);
					i--;
				}
			}
			if(out.size() == 0) {
				out.add(num(1));
			}
			out.add(Prod.unCast(prodCopy));
		}else if(e instanceof Div) {
			Div casted = (Div)e;
			ExprList numer = seperateCoef(casted.getNumer());
			ExprList denom = seperateCoef(casted.getDenom());
			Expr newCoef = Div.unCast(div(numer.get(0),denom.get(0)));
			Expr newExpr = Div.unCast(div(numer.get(1),denom.get(1)));
			out.add(newCoef);
			out.add(newExpr);
		}else if(e instanceof Num) {
			out.add(e.copy());
			out.add(num(1));
		}else {
			out.add(num(1));
			out.add(e.copy());
		}
		
		return out;
	}
	
	static Expr exprListToPoly(ExprList poly,Var v,Settings settings){
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
	
	public static Power perfectPower(Num n) {//basically tries to do a big root with all possible exponents
		if(n.realValue.compareTo(BigInteger.TWO) == -1 || n.isComplex()) return pow(n.copy(),num(1));//make it accept any number without errors
		int currentExpo = n.realValue.bitLength();//maximum exponent is log base 2 of that number. This is because 2 is the smallest base possible
		while(currentExpo != 1) {
			BigInteger possibleBase = bigRoot(n.realValue,BigInteger.valueOf(currentExpo));
			if(possibleBase.pow(currentExpo).equals(n.realValue)) {//testing if re exponentiating it gives the same result
				return pow(num(possibleBase),num(currentExpo));
			}
			currentExpo--;
		}
		return pow(n.copy(),num(1));
	}
	
	BigInteger gcm(BigInteger a,BigInteger b) {
		return a.multiply(b).divide(a.gcd(b));
	}
	
	private static class IntFactor {//uses a mix of rho and wheel factorization
		
		static boolean initializedWheel = false;
		static long[] initialSet = new long[]{2,3,5,7};
		static ArrayList<BigInteger> wheelSet = new ArrayList<BigInteger>();
		static BigInteger increase = BigInteger.ONE;
		static BigInteger maxCheck = BigInteger.TWO.pow(24);//maximum check for wheel factor
		static int limit = (int)Math.pow(2, 15);//maximum loops for rho
		
		static boolean isPartOfWheelSet(long l){
			for(long i:initialSet){
				if(l%i == 0)return false;
			}
			return true;
		}
		static void initWheel(){
			if(initializedWheel) return;
			
			initializedWheel = true;
			for(Long l:initialSet) increase = increase.multiply(BigInteger.valueOf(l));
			int increaseInt = increase.intValue();
			for(int i = 2;i<increaseInt+2;i++){
				if(isPartOfWheelSet(i)){
					wheelSet.add(BigInteger.valueOf(i));
				}
			}
		}
		static ArrayList<BigInteger> wheelFactor(BigInteger l){
			ArrayList<BigInteger> factors = new ArrayList<BigInteger>();
			if(l.isProbablePrime(1)){
				factors.add(l);
				return factors;
			}
			if(l.compareTo(BigInteger.TWO) == -1) return factors;
			
			BigInteger sqrtVal = l.sqrt();
			for(int i = 0;i<initialSet.length;i++){
				BigInteger test = BigInteger.valueOf(initialSet[i]);
				if(test.compareTo(sqrtVal)==1){
					if(!l.equals(BigInteger.ONE)) factors.add(l);
					return factors;
				}
				boolean worked = false;
				while(l.mod(test).equals(BigInteger.ZERO)){
					l = l.divide(test);
					factors.add(test);
					worked = true;
				}
				if(worked) sqrtVal = l.sqrt();
			}
			
			if(l.isProbablePrime(1)){
				factors.add(l);
				return factors;
			}
			BigInteger i = BigInteger.ZERO;
			while(true){
				for(BigInteger test:wheelSet){
					test = test.add(i.multiply(increase));
					if(test.compareTo(sqrtVal)==1 || test.compareTo(maxCheck)==1){
						if(!l.equals(BigInteger.ONE)) factors.add(l);
						return factors;
					}
					boolean worked = false;
					while(l.mod(test).equals(BigInteger.ZERO)){
						l = l.divide(test);
						factors.add(test);
						worked = true;
					}
					if(worked){
						
						if(l.isProbablePrime(1)){
							factors.add(l);
							return factors;
						}
						
						sqrtVal = l.sqrt();
					}
				}
				i = i.add(BigInteger.ONE);
			}
		}
		
		
		static BigInteger rhoFunc(BigInteger in,BigInteger n){
			return in.pow(2).add(BigInteger.ONE).mod(n);
		}
		
		static ArrayList<BigInteger> cachedBigPrimes = new ArrayList<BigInteger>();
		static BigInteger big = BigInteger.valueOf(Short.MAX_VALUE);
		static void addToCache(BigInteger i){
			if(i.compareTo(big)==1 ){
				if(!cachedBigPrimes.contains(i)) cachedBigPrimes.add(i);
			}
		}
		static void resizeCache(){
			while(cachedBigPrimes.size() > 1024){
				cachedBigPrimes.remove(0);
			}
		}
		
		static ArrayList<BigInteger> rhoFactor(BigInteger n){
			ArrayList<BigInteger> factors = new ArrayList<BigInteger>();
			if(n.compareTo(BigInteger.TWO) == -1) return factors;
			
			if(n.isProbablePrime(1)){
				factors.add(n);
				return factors;
			}
			initWheel();
			resizeCache();
			
			///cached primes
			if(n.compareTo(big) == 1){
				boolean usedCache = false;
				for(BigInteger i:cachedBigPrimes){
					if(n.mod(i).equals(BigInteger.ZERO)){
						usedCache = true;
						n = n.divide(i);
						factors.add(i);
					}
				}
				if(usedCache){
					if(n.isProbablePrime(1)){
						addToCache(n);
						factors.add(n);
						return factors;
					}
					if(n.equals(BigInteger.ONE)){
						return factors;
					}
				}
			}
			///
			
			while (true) {
				BigInteger x = BigInteger.TWO,y=x,d = BigInteger.ONE;
				long counter = 0;
				while (d.equals(BigInteger.ONE)) {
					if(counter>limit){
						addToCache(n);
						factors.add(n);
						return factors;
					}
					x = rhoFunc(x,n);
					y = rhoFunc(rhoFunc(y,n),n);
					d = x.subtract(y).gcd(n);
					counter++;
				}
				ArrayList<BigInteger> subList = wheelFactor(d);
				if(subList.size() > 1){
					for(BigInteger d2:subList){
						addToCache(d2);
						factors.add(d2);
						n = n.divide(d2);
					}
				
				}else{
					addToCache(d);
					factors.add(d);
					n = n.divide(d);
					while(n.mod(d).equals(BigInteger.ZERO)){
						factors.add(d);
						n = n.divide(d);
					}
				}
				if(n.isProbablePrime(1)){
					addToCache(n);
					factors.add(n);
					break;
				}
				if(n.equals(BigInteger.ONE)){
					break;
				}
			}
			return factors;
		}
	}
	
	public static Prod primeFactor(Num num) {
		
		if(num.isComplex()) {
			System.err.println("prime factor function recieved a complex number.");
			return null;
		}
		Prod p = new Prod();
		BigInteger n = num.realValue;
		if(n.signum()==-1) {
			n = n.abs();
			p.add(pow(num(-1),num(1)));
		}
		
		ArrayList<BigInteger> factors = IntFactor.rhoFactor(n);
		for(int i = 0;i<factors.size();i++) {
			BigInteger currentVal = factors.get(i);
			int count = 1;
			for(int j = i+1;j<factors.size();j++) {
				if(currentVal.equals(factors.get(j))) {
					factors.remove(j);
					count++;
					j--;
				}
			}
			p.add(new Power(num(currentVal),num(count)));
		}
		
		
		return p;
	}
	
	public static double factorial(double x) {//using https://journalofinequalitiesandapplications.springeropen.com/articles/10.1186/s13660-018-1646-6
		int focusLevel = 12;
		x+=focusLevel;
		//getting (x+8)!
		double stirling = Math.sqrt(2*Math.PI*x)*Math.pow(x/Math.E, x);
		double concentrate = Math.pow(x*Math.sinh(1.0/x),x/2.0);
		double concentratePart2 = Math.exp(7.0/(324.0*x*x*x*(35.0*x*x+33.0)));
		
		double xPlus8Fact = stirling*concentrate*concentratePart2;
		
		x-=focusLevel;
		//the shift then division is a hacky way of getting more precision from the factorial
		//x!=x*(x-1)! -> x!/x=(x-1)! shifting x by 1 -> (x+1)!/(x+1)=x!, this increases the precision because the sterling approximation
		//increases in correct significant digits as x approaches infinity. This is why (x+1)!/(x+1) would increase precision because we are
		//now evaluating at a larger value of x with the (x+1)!
		double denomProd = 1.0;
		for(int i = 1;i<=focusLevel;i++) {
			denomProd*=(x+i);
		}
		
		return xPlus8Fact/denomProd;
	}
	
	public static BigInteger factorial(BigInteger x) {
		BigInteger prod = BigInteger.ONE;
		
		if(x.compareTo(BigInteger.ONE) == 1) {
			for(BigInteger i = x;i.compareTo(BigInteger.ONE) == 1;i = i.subtract(BigInteger.ONE)) {
				prod = prod.multiply(i);
			}
		}
		
		return prod;
	}
	
	public static BigInteger choose(BigInteger n,BigInteger k) {
		BigInteger outNumer = BigInteger.ONE;
		BigInteger outDenom = BigInteger.ONE;
		
		for(BigInteger i = BigInteger.ZERO; i.compareTo(k) == -1;i = i.add(BigInteger.ONE)) {
			outNumer = outNumer.multiply( n.subtract(i) );
			outDenom = outDenom.multiply(i.add(BigInteger.ONE));
			
			BigInteger gcd = outNumer.gcd(outDenom);
			outNumer = outNumer.divide(gcd);
			outDenom = outDenom.divide(gcd);
			
		}
		return outNumer.divide(outDenom);
	}
	
	static Equ isSin2x = (Equ)createExpr("sin(x)*cos(x)=sin(2*x)/2");
	static Equ isSin2x_2 = (Equ)createExpr("a*sin(x)*cos(x)=a*sin(2*x)/2");
	static Equ isCos2x = (Equ)createExpr("2*cos(x)^2-1=cos(2*x)");
	static Equ isTan2x = (Equ)createExpr("2*tan(x)/(1-tan(x)^2)=tan(2*x)");
	
	public static Expr trigCompress(Expr e,Settings settings) {
		e = e.modifyFromExample(isSin2x, settings);
		e = e.modifyFromExample(isSin2x_2, settings);
		e = e.modifyFromExample(isCos2x, settings);
		e = e.modifyFromExample(isTan2x, settings);
		return e;
	}
	
	public static Expr trigExpand(Expr e,Settings settings){
		if(e.containsType(Sin.class)){
			Expr trigPart = null;
			Prod coef = new Prod();
			if(e instanceof Prod){
				for(int j = 0;j<e.size();j++){
					if(e.get(j) instanceof Sin){
						if(trigPart == null){
							trigPart = e.get(j);
						}else{
							return e;
						}
					}else{
						coef.add(e.get(j));
					}
				}
			}else if(e instanceof Sin){
				trigPart = e;
			}
			
			if(trigPart != null){
				Expr innerPart = trigPart.get();
				Expr halfInner = div(innerPart,num(2)).simplify(settings);
				
				if(!(halfInner instanceof Div)){
					coef.add(sin(halfInner));
					coef.add(cos(halfInner));
					coef.add(num(2));
					return coef.simplify(settings);
				}
				return e;
			}
			
		}
		return e;
	}
	
	public static Expr binomial(Expr left,Expr right,BigInteger expo,Settings settings) {//returns the binomial expansion
		Sum out = new Sum();
		BigInteger expoPlusOne = expo.add(BigInteger.ONE);
		for(BigInteger i = BigInteger.ZERO;i.compareTo(expoPlusOne)==-1 ;i = i.add(BigInteger.ONE)) {
			Num coef = num(choose(expo,i));
			out.add( prod(coef,pow(left,num(i)),pow(right,num( expo.subtract(i) ))  ).simplify(settings) );
		}
				
		return Sum.unCast(out);
	}
	
	public static long[] toFraction(double num) {//from https://begriffs.com/pdf/dec2frac.pdf
		if( ((long)num)-num == 0.0) return new long[] {(long)num,1};
		double z = num;
		long d_old = 0,d = 1,new_d;
		long n = Math.round(num);
		double delta = 0;
		double zero = 0.000000001;
		for(int i = 0;i<11;i++) {
			delta = z-(long)z;
			if(Math.abs(delta)<zero) return new long[] {n,d};
			z = 1.0/(delta);
			new_d = d*((long)z)+d_old;
			d_old = d;
			d = new_d;
			
			n = Math.round(num*d);
			if( ((double)n/d)-num == 0.0) {
				return new long[] {n,d};
			}
			
			
		}
		return new long[] {n,d};
	}
	
	static HashMap<String,String> BLToMathMLFunctionNameMap = null;
	private static void initBLToMathMLFunctionNameMap() {
		BLToMathMLFunctionNameMap = new HashMap<String,String>();
		//based on class names
		BLToMathMLFunctionNameMap.put("log","ln");
		BLToMathMLFunctionNameMap.put("asin","arcsin");
		BLToMathMLFunctionNameMap.put("acos","arccos");
		BLToMathMLFunctionNameMap.put("atan","arctan");
	}
	public static String generateMathML(Expr e) {
		if(BLToMathMLFunctionNameMap == null) initBLToMathMLFunctionNameMap();
		String out = "";
		String leftParen = "\\left( ";
		String rightParen = "\\right) ";
		if(e instanceof Pi) {
			out+="\\pi ";
		}else if(e instanceof Var || e instanceof Num || e instanceof E) {
			out += e.toString();
		}else if(e instanceof Prod) {
			for(int i = 0;i<e.size();i++) {
				boolean paren = e.get(i) instanceof Sum;
				if(paren) out+=leftParen;
				out+=generateMathML(e.get(i));
				if(paren) out+=rightParen;
				if(i!=e.size()-1) out+="\\cdot ";
			}
		}else if(e instanceof Sum) {
			for(int i = 0;i<e.size();i++) {
				if(i!=0)out+=generateMathML(e.get(i).abs(Settings.normal));
				else out+=generateMathML(e.get(i));
				if(i!=e.size()-1) {
					if(e.get(i+1).negative()) out+="-";
					else out+="+";
				}
			}
		}else if(e instanceof Div) {
			out+="\\frac{";
			out+=generateMathML(((Div)e).getNumer());
			out+="}{";
			out+=generateMathML(((Div)e).getDenom());
			out+="}";
		}else if(e instanceof Power) {
			Power casted = (Power)e;
			out+="{";
			boolean parenBase = false;
			if(casted.getBase() instanceof Sum || casted.getBase() instanceof Prod || casted.getBase() instanceof Power || (casted.getBase() instanceof Num && casted.getBase().negative())) parenBase = true;
			if(parenBase) out+=leftParen;
			out+=generateMathML(casted.getBase());
			if(parenBase) out+=rightParen;
			out+="}^{";
			boolean parenExpo= false;
			if(casted.getExpo() instanceof Sum || casted.getExpo() instanceof Prod || casted.getExpo() instanceof Power) parenExpo = true;
			if(parenExpo) out+=leftParen;
			out+=generateMathML(casted.getExpo());
			if(parenExpo) out+=rightParen;
			out+="}";
		}else if(e instanceof Equ) {
			Equ casted = (Equ)e;
			out+=generateMathML( casted.getLeftSide() );
			out+="=";
			out+=generateMathML( casted.getRightSide() );
		}else if(e instanceof IntegrateOver) {
			IntegrateOver casted = (IntegrateOver)e;
			out+="\\int_{";
			out+=generateMathML(casted.getMin());
			out+="}^{";
			out+=generateMathML(casted.getMax());
			out+="}{";
			out+=generateMathML(casted.getExpr());
			out+=" d"+generateMathML(casted.getVar());
			out+="}";
		}else if(e instanceof Diff) {
			Diff casted = (Diff)e;
			out+="\\frac{d}{d";
			out+=casted.getVar().toString();
			out+="}"+leftParen;
			out+=generateMathML(casted.get());
			out+=rightParen;
		}else{
			String BLfunctionName = e.getClass().getSimpleName().toLowerCase();
			String repl = BLToMathMLFunctionNameMap.get(BLfunctionName);
			if(repl == null) repl = BLfunctionName;
			
			out+="\\"+repl+leftParen;
			out+=generateMathML(e.get());
			out+=rightParen;
		}
		
		return out;
	}
	
}
