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
			Expr numerParts = new Prod();
			Expr denomParts = new Prod();
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
			if(numerParts.size() == 1) numerParts = numerParts.get();
			if(denomParts.size() == 1) denomParts = denomParts.get();
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
	
	static Num[] extractNumFrac(Expr in){//numerical based fraction extraction, returns null if it does not meet criteria
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
	
	public static Expr partialFrac(Expr expr,Var v,Settings settings) {
		Settings yesFactor = new Settings();
		yesFactor.factor = true;
		Settings noFactor = new Settings();
		expr = expr.simplify(yesFactor);
		
		Expr[] frac = extractFrac(expr);
		
		if(!isPolynomial(frac[0],v)) {
			return expr;
		}
		
		Prod denom = null;
		if(frac[1] instanceof Prod) {
			denom = (Prod) frac[1];
		}else {
			denom = new Prod();
			denom.add(frac[1]);
		}
		
		ExprList denTerms = new ExprList();
		
		BigInteger denDegree = BigInteger.ZERO;
		
		for(int i = 0;i<denom.size();i++) {
			noFactor.factor = false;
			Expr e = inv(denom.get(i)).simplify(noFactor);
			
			if(e instanceof Power) {
				Power casted = (Power)e;
				if(casted.getExpo() instanceof Num && !casted.getExpo().negative()) {
					BigInteger d = degree(casted.getBase(),v);
					if(!d.equals(BigInteger.ONE)) return expr;
					denDegree = denDegree.add(((Num)casted.getExpo()).value);
				}else return expr;
			}else {
				BigInteger d = degree(e,v);
				if(!d.equals(BigInteger.ONE)) return expr;
				denDegree = denDegree.add(BigInteger.ONE);
			}
			
			denTerms.add(e);
		}
		
		if(denDegree.compareTo(BigInteger.TWO) == -1) return expr;
		
		if(degree(frac[0],v).compareTo(denDegree) != -1) return expr;
		//all the checks for if it fits the form are done by this point
		Expr out = new Sum();
		
		for(int i = 0;i<denTerms.size();i++) {
			Expr function = prod(expr,denTerms.get(i)).simplify(settings);
			
			
			if(!(denTerms.get(i) instanceof Power)) {
				ExprList poly = polyExtract(denTerms.get(i),v,settings);
				Expr solution = div(neg(poly.get(0)),poly.get(1)).simplify(settings);
				
				Expr numer = function.replace(equ(v,solution)).simplify(settings);
				
				out.add(div(numer,denTerms.get(i)).simplify(settings));
			}else {
				Power pw = (Power)denTerms.get(i);
				ExprList poly = polyExtract(pw.getBase(),v,settings);
				
				Expr solution = div(neg(poly.get(0)),poly.get(1)).simplify(settings);
				
				BigInteger currentExpo = ((Num)pw.getExpo()).value;
				
				BigInteger count = BigInteger.ZERO;
				BigInteger factorial = BigInteger.ONE;
				
				while(currentExpo.compareTo(BigInteger.ONE) == 1) {
					
					Expr numer = function.replace(equ(v,solution)).simplify(settings);
					
					Prod outProd = prod(numer,pow(pw.getBase(),num(currentExpo.negate())),inv(num(factorial)));
					outProd.add(pow(poly.get(1),num(count.negate())));
					out.add(outProd.simplify(settings));
					
					count = count.add(BigInteger.ONE);
					factorial = factorial.multiply(count);
					function = diff(function,v).simplify(settings);
					currentExpo = currentExpo.subtract(BigInteger.ONE);
				}
				
				Expr numer = function.replace(equ(v,solution)).simplify(settings);
				
				
				Prod outProd = prod(numer,pow(pw.getBase(),num(currentExpo.negate())),inv(num(factorial)));
				outProd.add(pow(poly.get(1),num(count.negate())));
				out.add(outProd.simplify(settings));
				
			}
			
		}
		out = out.simplify(noFactor);
		return out;
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
	
	public static BigInteger degree(Expr expr,Var v) {//returns -1 if it does not fit polynomial form
		
		if(!expr.contains(v)) return BigInteger.ZERO;
		if(expr instanceof Power) {
			Power casted = (Power)expr;
			if(casted.getBase().equalStruct(v) && casted.getExpo() instanceof Num && !casted.getExpo().negative()) {
				return ((Num)casted.getExpo()).value;
			}
		}else if(expr instanceof Sum) {
			BigInteger maxDegree = BigInteger.ZERO;
			
			for(int j = 0;j<expr.size();j++) {
				Expr inner = expr.get(j);
				if(!inner.contains(v)) continue;
				
				if(inner instanceof Power) {
					Power casted = (Power)inner;
					if(casted.getBase().equalStruct(v) && casted.getExpo() instanceof Num && !casted.getExpo().negative()) {
						maxDegree = maxDegree.max(  ((Num)casted.getExpo()).value  );
					}else {
						return BigInteger.valueOf(-1);
					}
				}else if(inner instanceof Prod) {
					
					int termsWithVar = 0;
					int indexOfVarThing = 0;
					for(int i = 0;i<inner.size();i++) {
						if(inner.get(i).contains(v)) {
							termsWithVar++;
							indexOfVarThing = i;
						}
					}
					
					if(termsWithVar == 1) {
						Expr e = inner.get(indexOfVarThing);
						if(e.equalStruct(v)) maxDegree = maxDegree.max(BigInteger.ONE);
						else if(e instanceof Power ) {
							Power casted = (Power)e;
							if(casted.getBase().equalStruct(v) && casted.getExpo() instanceof Num && !casted.getExpo().negative()) {
								maxDegree = maxDegree.max(  ((Num)casted.getExpo()).value  );
							}
						}else return BigInteger.valueOf(-1);
					}else {
						return BigInteger.valueOf(-1);
					}
					
					
				}else if(inner.equalStruct(v)) {
					maxDegree = maxDegree.max(BigInteger.ONE);
				}else {
					return BigInteger.valueOf(-1);
				}
				
				
			}
			return maxDegree;
			
		}else if(expr instanceof Prod) {
			int termsWithVar = 0;
			int indexOfVarThing = 0;
			for(int i = 0;i<expr.size();i++) {
				if(expr.get(i).contains(v)) {
					termsWithVar++;
					indexOfVarThing = i;
				}
			}
			
			if(termsWithVar == 1) {
				Expr e = expr.get(indexOfVarThing);
				if(e.equalStruct(v)) return BigInteger.ONE;
				else if(e instanceof Power ) {
					Power casted = (Power)e;
					if(casted.getBase().equalStruct(v) && casted.getExpo() instanceof Num && !casted.getExpo().negative()) {
						return ((Num)casted.getExpo()).value;
					}
				}
			}
		}else if(expr.equalStruct(v)) {
			return BigInteger.ONE;
		}
		
		
		
		return BigInteger.valueOf(-1);
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
		
		Prod p = new Prod();
		BigInteger n = num.value;
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
	
}
