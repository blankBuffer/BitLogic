package cas;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import cas.bool.*;
import cas.special.*;
import cas.primitive.*;
import cas.calculus.*;
import cas.lang.*;
import cas.matrix.*;
import cas.programming.*;


public class Cas {
	/*
	 * this file is for shortcuts and general algorithms used everywhere
	 */
	
	public static final Expr nullExpr = null;
	
	public static Expr createExpr(String expr) {
		return Interpreter.createExpr(expr);
	}
	
	public static void load(){
		Rule.loadCompileSimplifyRules();
	}
	
	public static class IndexSet{
		public ArrayList<Integer> ints = new ArrayList<Integer>();
		void print() {
			for(int i:ints) System.out.print(i+",");
		}
		void println() {
			print();
			System.out.println();
		}
	}
	
	public static class VarCount implements Comparable<VarCount>{
		public Var v;
		public int count = 0;
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
			int comparison = -Integer.compare(count, other.count);
			if(comparison == 0) return Integer.compare(v.hashCode(), other.v.hashCode());//if two variabls have the same count use the hash to sort instead
			return comparison;
		}
	}
	
	//
	public static Expr sqrtObj = null;//used for comparing to
	public static Expr cbrtObj = null;//used for comparing to
	
	public static void initExprTemp(){
		sqrtObj = sqrt(var("x"));
		cbrtObj = cbrt(var("x"));
	}
	
	public static Var e() {
		return var("e");
	}
	public static Var pi() {
		return var("pi");
	}
	public static Var inf(){
		return var("inf");
	}
	public static Var epsilon(){
		return var("epsilon");
	}
	
	public static Var error(){
		return var("error");
	}
	
	//
	public static Func power(Expr a,Expr b) {
		Func out = (Func) SimpleFuncs.power.copy();
		out.add(a);
		out.add(b);
		return out;
	}
	public static Sum sum(Expr... exprs) {
		Sum out = new Sum();
		for(Expr e:exprs){
			out.add(e);
		}
		return out;
	}
	public static Prod prod(Expr... exprs) {
		Prod out = new Prod();
		for(Expr e:exprs){
			out.add(e);
		}
		return out;
	}
	public static Dot dot(Expr... exprs) {
		Dot out = new Dot();
		for(Expr e:exprs){
			out.add(e);
		}
		return out;
	}
	public static Func and(Expr... exprs) {
		Func out = (Func) SimpleFuncs.and.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	public static Func or(Expr... exprs) {
		Func out = (Func) SimpleFuncs.or.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	public static ExprList exprList(Expr... exprs) {
		ExprList out = new ExprList();
		for(Expr e:exprs){
			out.add(e);
		}
		return out;
	}
	public static Gcd gcd(Expr... exprs) {
		Gcd out = new Gcd();
		for(Expr e:exprs){
			out.add(e);
		}
		return out;
	}
	
	public static Limit limit(Expr e,Becomes becomes){
		return new Limit(e,becomes);
	}
	public static Func not(Expr expr) {
		Func out = (Func) SimpleFuncs.not.copy();
		out.add(expr);
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
	public static Greater equGreater(Expr a,Expr b) {
		return new Greater(a,b);
	}
	public static Less equLess(Expr a,Expr b) {
		return new Less(a,b);
	}
	public static BoolState bool(boolean b) {
		return new BoolState(b);
	}
	public static Func ln(Expr expr) {
		try {
			return (Func)SimpleFuncs.getFuncByName("ln", expr);
		}catch(Exception e) {}
		return null;
	}
	public static Sum sub(Expr a,Expr b) {
		return sum(a,prod(num(-1),b));
	}
	public static Func inv(Expr a) {
		return div(num(1),a);
	}
	public static Func sqrt(Expr a) {
		return power(a,inv(num(2)));
	}
	public static Func cbrt(Expr a) {
		return power(a,inv(num(3)));
	}
	public static Prod neg(Expr a) {
		return prod(num(-1),a);
	}
	public static Func div(Expr a,Expr b) {
		try {
			return (Func)SimpleFuncs.getFuncByName("div",a,b);
		}catch(Exception e2) {}
		return null;
	}
	
	public static Func diff(Expr e,Var v) {
		try {
			return (Func)SimpleFuncs.getFuncByName("diff",e,v);
		}catch(Exception e2) {}
		return null;
	}
	public static Func integrate(Expr e,Var v) {
		try {
			return (Func)SimpleFuncs.getFuncByName("integrate",e,v);
		}catch(Exception e2) {}
		return null;
	}
	public static Func integrateOver(Expr min,Expr max,Expr e,Var v) {
		try {
			return (Func)SimpleFuncs.getFuncByName("integrateOver", min,max,e,v);
		}catch(Exception e2) {}
		return null;
	}
	public static Func solve(Expr e,Expr v) {
		try {
			return (Func)SimpleFuncs.getFuncByName("solve", e,v);
		}catch(Exception e2) {}
		return null;
	}
	
	public static Func exp(Expr expr) {
		return power(e(),expr);
	}
	public static Func sin(Expr expr) {
		Func out = (Func) SimpleFuncs.sin.copy();
		out.add(expr);
		return out;
	}
	public static Func cos(Expr expr) {
		Func out = (Func) SimpleFuncs.cos.copy();
		out.add(expr);
		return out;
	}
	public static Func tan(Expr expr) {
		Func out = (Func) SimpleFuncs.tan.copy();
		out.add(expr);
		return out;
	}
	public static Func atan(Expr expr) {
		Func out = (Func) SimpleFuncs.atan.copy();
		out.add(expr);
		return out;
	}
	public static Func asin(Expr expr) {
		Func out = (Func) SimpleFuncs.asin.copy();
		out.add(expr);
		return out;
	}
	public static Func acos(Expr expr) {
		Func out = (Func) SimpleFuncs.acos.copy();
		out.add(expr);
		return out;
	}
	public static Func approx(Expr expr,ExprList defs) {
		Func out = (Func) SimpleFuncs.approx.copy();
		out.add(expr);
		out.add(defs);
		return out;
	}
	public static Func factor(Expr expr) {
		try {
			return (Func)SimpleFuncs.getFuncByName("factor", expr);
		}catch(Exception e) {}
		return null;
	}
	public static Func distr(Expr expr) {
		try {
			return (Func)SimpleFuncs.getFuncByName("distr", expr);
		}catch(Exception e) {}
		return null;
	}
	public static Func gamma(Expr expr) {
		try {
			return (Func)SimpleFuncs.getFuncByName("gamma", expr);
		}catch(Exception e) {}
		return null;
	}
	public static Func lambertW(Expr expr){
		try {
			return (Func)SimpleFuncs.getFuncByName("lambertW", expr);
		}catch(Exception e) {}
		return null;
	}
	public static Func abs(Expr expr) {
		try {
			return (Func)SimpleFuncs.getFuncByName("abs", expr);
		}catch(Exception e) {}
		return null;
	}
	public static Mat mat(Sequence e) {
		return new Mat(e);
	}
	public static Mat mat(int rows,int cols) {
		return new Mat(rows,cols);
	}
	public static Func func(String name,ExprList vars,Expr expr) {
		return new Func(name,vars,expr);
	}
	public static Func func(String name,Equ v,Expr expr) {
		return new Func(name,v,expr);
	}
	public static Becomes becomes(Expr left,Expr right) {
		return new Becomes(left,right);
	}
	public static Transpose transpose(Expr e) {
		return new Transpose(e);
	}
	public static Next next(Sequence s,Num num) {
		return new Next(s,num);
	}
	public static Define define(Expr left,Expr right) {
		return new Define(left,right);
	}
	public static Ternary ternary(Expr toBeEvaled,Expr ifTrue,Expr ifFalse) {
		return new Ternary(toBeEvaled,ifTrue,ifFalse);
	}
	public static Range range(Expr min,Expr max,Expr e,Var v) {
		return new Range(min,max,e,v);
	}
	public static Func boolCompress(Expr expr) {
		Func out = (Func) SimpleFuncs.boolCompress.copy();
		out.add(expr);
		return out;
	}
	public static Func boolTableToExpr(ExprList table, ExprList vars) {
		Func out = (Func) SimpleFuncs.boolTableToExpr.copy();
		out.add(table);
		out.add(vars);
		return out;
	}
	public static Sequence sequence(Expr... exprs) {
		Sequence out = new Sequence();
		for(Expr e:exprs) {
			out.add(e);
		}
		return out;
	}
	public static Func comparison(Expr equ) {
		try {
			return (Func)SimpleFuncs.getFuncByName("comparison", equ);
		}catch(Exception e) {}
		return null;
	}
	public static Func expand(Expr e) {
		try {
			return (Func)SimpleFuncs.getFuncByName("expand", e);
		}catch(Exception e2) {}
		return null;
	}
	//
	
	public static boolean allLinearTerms(Expr e,Var v) {
		Prod prodCast = (Prod)e;
		for(int i = 0;i<prodCast.size();i++){
			Func current = Power.cast(prodCast.get(i));
			if(!(isPositiveRealNum(current.getExpo()) && (current.getBase() instanceof Sum || current.getBase() instanceof Var) && degree(current.getBase(),v) == BigInteger.ONE)){
				return false;
			}
		}
		return true;
	}
	
	public static boolean containsComplexNumbers(Expr e) {
		if(e instanceof Num) return ((Num)e).isComplex();
		
		for(int i = 0;i < e.size();i++) {
			if(containsComplexNumbers(e.get(i))) return true;
		}
		return false;
	}
	
	public static Expr partialFrac(Expr expr,Var v,CasInfo casInfo) {
		if(expr.typeName().equals("div")) {
			BigInteger negOne = BigInteger.valueOf(-1);
			Func frac = (Func)expr;
			BigInteger numerDegree = degree( frac.getNumer() ,v);
			BigInteger denomDegree = degree( frac.getDenom() ,v);
			
			if(numerDegree.equals(negOne) || denomDegree.equals(negOne)) return expr;//not polynomials
			if(denomDegree.compareTo(numerDegree) != 1) return expr;//denominator needs a greater degree
			
			CasInfo factorIrrationalAndComplexRoots = new CasInfo(casInfo);
			factorIrrationalAndComplexRoots.setFactorIrrationalRoots(true);
			factorIrrationalAndComplexRoots.setAllowComplexNumbers(true);
			
			
			frac.getDenom().fullFlagReset();//assume to not be simplified so that full factoring can occur
			Expr denomFactored = factor(frac.getDenom()).simplify(factorIrrationalAndComplexRoots);//factor denominator into hopefully linear terms
			
			boolean needsCombination = containsComplexNumbers(denomFactored);
			
			Sequence parts = seperateByVar(denomFactored,v);
			
			Expr denomCoef = parts.get(0);
			denomFactored = Prod.cast(parts.get(1));
			
			if(!allLinearTerms(denomFactored,v)) return expr;//make sure its a product of linear terms
			frac.setDenom(denomFactored);
			
			Sum out = new Sum();
			
			ArrayList<Expr> chara = new ArrayList<Expr>();//a measurement which is useful when combining conjugate denominators
			
			for(int i = 0;i<denomFactored.size();i++){//create fraction terms using extended heaviside partial fractions algorithm
				/*
				the algorithm works as follows
				n(x) is a polynomial of lesser degree then the denominator
				
				f(x)=n(x)/((x+1)*(a*x+b)^3) this is the expression which will have partial fractions applied
				
				let k1(x)=f(x)*(x+1)=n(x)/(a*x+b)^3  which simplifies to all the other terms in the denominator
				let k2(x)=f(x)*(a*x+b)^3=n(x)/(x+1)
				
				let r1 be the solution to the first denominator term namely the root of (x+1)=0 so r1=-1
				let r2 be the solution to the second denominator term namely the root of (a*x+b) so r2=-b/a
				
				the result partial fraction ends up being
				
				k1(r1)/(x+1)  +   (k2(r2)/(a^0*0!))/(a*x+b)^3 + (k2'(r2)/(a^1*1!))/(a*x+b)^2 + (k2''(r2)/(a^2*2!))/(a*x+b)^1
				
				the pattern works much like the regular cover up method but works for denominators with powers
				*/
				
				Expr currentFunction = frac.copy();
				((Func)currentFunction).getDenom().remove(i);
				
				Func currentTerm = Power.cast(denomFactored.get(i));
				
				Sequence poly = polyExtract(currentTerm.getBase(),v,casInfo);
				Expr linearTermCoef = poly.get(1);
				Equ solution = (Equ)ExprList.cast(solve(equ(currentTerm,Num.ZERO),v).simplify(casInfo)).get();
				
				
				BigInteger expo = ((Num)currentTerm.getExpo()).getRealValue();
				BigInteger expoMinusOne = expo.subtract(BigInteger.ONE);
				
				for(BigInteger j = BigInteger.ZERO;j.compareTo( expo ) == -1;j = j.add(BigInteger.ONE)){
					BigInteger currentExpo = expo.subtract(j);
					
					Expr functionOut = currentFunction.replace(solution);
					
					chara.add( exprList(num(out.size()),poly.get(0),poly.get(1),num(currentExpo)) );//index , constant , linear coeff, exponent
					
					Expr numer = div(functionOut,prod(denomCoef,power(linearTermCoef,num(j)), num(factorial(j))));
					Expr newTerm = div(numer,  power(currentTerm.getBase(),num(currentExpo))  );
					
					out.add(newTerm);
					
					if(!j.equals(expoMinusOne)){
						currentFunction = diff(currentFunction,v).simplify(casInfo);
					}
					
				}
				
			}
			
			
			if(!casInfo.allowComplexNumbers() && needsCombination){//combine complex conjugates
				HashMap<Integer,Integer> pairs = new HashMap<Integer,Integer>();
				
				outer:for(int i = 0;i<chara.size();i++) {
					Expr current = chara.get(i);
					
					for(int j = i+1;j<chara.size();j++) {
						Expr other = chara.get(j);
						
						if( !containsComplexNumbers(other.get(1)) ) {
							j--;
							chara.remove(j);
							continue;
						}
						
						if(current.get(2).equals(other.get(2)) && current.get(3).equals(other.get(3)) && !current.get(1).equals(other.get(1))) {//exponent and variable coefficient same but constant different means it can be combined because they are conjugates
							pairs.put( ((Num)current.get(0)).getRealValue().intValue() , ((Num)other.get(0)).getRealValue().intValue());
							chara.remove(j);
							continue outer;
							
						}
					}
				}
				
				for(int key:pairs.keySet()) {
					int i = key;
					int j = pairs.get(key);
					Func firstDiv = (Func) out.get(i);
					Func secondDiv = (Func) out.get(j);
					
					Expr combinedDenom = power(distr( prod(((Func)firstDiv.getDenom()).getBase(),((Func)secondDiv.getDenom()).getBase())  ),((Func)firstDiv.getDenom()).getExpo());
					
					Expr combinedNumer = sum( prod(firstDiv.getNumer(),secondDiv.getDenom()) ,  prod(firstDiv.getDenom(),secondDiv.getNumer()) );
					
					out.set(i, div(combinedNumer,combinedDenom));
					out.set(j, null);//don't want resize of array
				}
				
				for(int i = out.size()-1;i>=0;i--) {
					if(out.get(i) == null) out.remove(i);
				}
				
			}
			return out.simplify(casInfo);
			
		}
		return expr;
	}
	
	public static Expr polyDiv(Expr expr,Var v,CasInfo casInfo) {
		if(expr.typeName().equals("div")) {
			Func frac = (Func)expr.copy();
			Sequence numPoly = polyExtract(distr(frac.getNumer()).simplify(casInfo),v,casInfo);
			Sequence denPoly = polyExtract(distr(frac.getDenom()).simplify(casInfo),v,casInfo);
			if(numPoly != null && denPoly != null && numPoly.size()>=denPoly.size()) {
				Sequence[] result = polyDiv(numPoly,denPoly,casInfo);
				Expr outPart =  exprListToPoly(result[0],v,casInfo);
				Expr remainPart =  div(exprListToPoly(result[1],v,casInfo),frac.getDenom());
				expr = sum(outPart,remainPart);
			}
		}
		return expr;
	}
	
	public static Sequence[] polyDiv(Sequence num,Sequence den,CasInfo casInfo) {//returns output + remainder
		Sequence remain = (Sequence)num.copy();
		Num zero = num(0);
		
		Sequence out = sequence();
		
		while(remain.size() >= den.size()) {
			if(den.get(den.size()-1).equals(zero)) {//avoid divide by zero situation
				out.add(0, zero.copy());
				remain.remove(remain.size()-1);//pop last element
				continue;
			}
			
			
			Expr coef = div(remain.get(remain.size()-1).copy(),den.get(den.size()-1).copy()).simplify(casInfo);
			out.add(0, coef);
			coef = neg(coef);
			
			
			for(int i = remain.size()-den.size();i<remain.size()-1;i++) {//we can skip last one since we know it will be deleted
				remain.set(i, sum( prod(den.get(i-(remain.size()-den.size()) ),coef) ,remain.get(i)).simplify(casInfo) );
			}
			remain.remove(remain.size()-1);//pop last element
			
			
		}
		while(remain.size()>0 && remain.get(remain.size()-1).equals(zero)) {//clean zeros off end
			remain.remove(remain.size()-1);
		}
		
		return new Sequence[] { out,remain };
	}
	
	public static BigInteger degree(Expr expr,Var v) {//returns -1 if it is not possible
		BigInteger negOne = BigInteger.valueOf(-1);
		Sum exprSum = Sum.cast(expr);
		BigInteger maxDegree = BigInteger.ZERO;
		for(int i = 0;i<exprSum.size();i++) {
			Expr term = exprSum.get(i);
			Expr stripped = stripNonVarPartsFromProd(term,v);
			if(stripped.equals(v)) maxDegree = maxDegree.max(BigInteger.ONE);
			else if(stripped.typeName().equals("power")) {
				Func casted = (Func)stripped;
				if(isPositiveRealNum(casted.getExpo())) {
					if(casted.getBase().equals(v)) {
						maxDegree = maxDegree.max(((Num)casted.getExpo()).getRealValue());
					}else if(isPolynomialUnstrict(casted.getBase(),v)) {
						maxDegree = maxDegree.max( degree(casted.getBase(),v).multiply(((Num)casted.getExpo()).getRealValue()) );
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
	
	public static boolean isPolynomialUnstrict(Expr expr,Var v){//is a polynomial in any form
		if(!expr.contains(v) || expr.equals(v)) return true;
		if(expr instanceof Sum || expr instanceof Prod){
			for(int i = 0;i<expr.size();i++){
				if(!isPolynomialUnstrict(expr.get(i),v)) return false;
			}
			return true;
		}else if(expr.typeName().equals("power")){
			Func casted = (Func)expr;
			return isPositiveRealNum(casted.getExpo()) && isPolynomialUnstrict(casted.getBase(),v);
		}else if(expr.typeName().equals("div")){
			Func castedDiv = (Func)expr;
			return !castedDiv.getDenom().contains(v) && isPolynomialUnstrict(castedDiv.getNumer(),v);
		}
		return false;
	}
	
	public static boolean isPlainPolynomial(Expr expr,Var v) {//basic polynomial
		Sum exprSum = Sum.cast(expr);
		for(int i = 0;i<exprSum.size();i++) {
			Expr term = exprSum.get(i);
			Expr stripped = stripNonVarPartsFromProd(term,v);
			if(stripped.equals(v)) continue;
			else if(stripped.typeName().equals("power")) {
				Func casted = (Func)stripped;
				if(casted.getBase().equals(v) && isPositiveRealNum(casted.getExpo())) continue;
				return false;
			}else if(stripped instanceof Num) continue;
			else return false;
		}
		return true;
	}
	public static Expr getLeadingCoef(Expr expr,Var v,CasInfo casInfo){
		if(isPolynomialUnstrict(expr,v)){
			if(!expr.contains(v)){
				return expr;
			}else if(expr.typeName().equals("div")){
				Func castedDiv = (Func)expr;
				return div(getLeadingCoef(castedDiv.getNumer(),v,casInfo),castedDiv.getDenom()).simplify(casInfo);
			}else if(expr instanceof Prod){
				Prod out = new Prod();
				for(int i = 0;i<expr.size();i++){
					out.add(getLeadingCoef(expr.get(i),v,casInfo));
				}
				return out.simplify(casInfo);
			}else if(expr.typeName().equals("power")){
				Func casted = (Func)expr;
				return power(getLeadingCoef(casted.getBase(),v,casInfo),casted.getExpo()).simplify(casInfo);
			}else if(expr instanceof Sum){
				BigInteger[] degrees = new BigInteger[expr.size()];
				for(int i = 0;i<expr.size();i++){
					degrees[i] = degree(expr.get(i),v);
				}
				
				BigInteger maxDegree = BigInteger.ZERO;
				
				for(int i = 0;i<degrees.length;i++){
					BigInteger currentDegree = degrees[i];
					maxDegree = maxDegree.max(currentDegree);
				}
				
				Sum out = new Sum();
				for(int i = 0;i<expr.size();i++){
					if(degrees[i].equals(maxDegree)){
						out.add( getLeadingCoef(expr.get(i) ,v,casInfo) );
					}
				}
				return out.simplify(casInfo);
			}else if(expr.equals(v)){
				return num(1);
			}
		}
		return null;
	}
	
	public static boolean isRealNum(Expr e) {
		return e instanceof Num && !((Num)e).isComplex();
	}
	public static boolean isPositiveRealNum(Expr e) {
		return isRealNum(e) && !e.negative();
	}
	public static boolean isNegativeRealNum(Expr e) {
		return isRealNum(e) && e.negative();
	}
	
	public static Sequence polyExtract(Expr expr,Var v,CasInfo casInfo) {
		BigInteger maxDegree = BigInteger.valueOf(16);
		Sequence coef = sequence();
		Sum sum = Sum.cast(expr);
		for(int i = 0;i<sum.size();i++) {
			Expr e = sum.get(i);
			BigInteger degree = BigInteger.ZERO;
			Expr contents = null;
			if(e.contains(v)) {
				Sequence parts = seperateByVar(e,v);
				if(parts.get(1).equals(v)) {
					contents = parts.get(0);
					degree = BigInteger.ONE;
				}else if(parts.get(1).typeName().equals("power")) {
					Func casted = (Func)parts.get(1);
					if(casted.getBase().equals(v) && isPositiveRealNum(casted.getExpo())) {
						degree = ((Num)casted.getExpo()).getRealValue();
					}else return null;
					contents = parts.get(0);
				}else return null;
			}else contents = e;
			if(degree.compareTo(maxDegree) == 1) return null;//don't want too big of a polynomial
			while(BigInteger.valueOf(coef.size()).compareTo(degree) <= 0) coef.add(num(0));//resize coef length to fit degree size
			int degreeInt = degree.intValue();
			coef.set(degreeInt,sum(coef.get(degreeInt),contents));
		}
		coef.simplifyChildren(casInfo);
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
			}else if(e.typeName().equals("div")) {
				Func eCastedDiv = (Func)e;
				Expr numer = stripNonVarPartsFromProd(eCastedDiv.getNumer(),v);
				Expr denom = stripNonVarPartsFromProd(eCastedDiv.getDenom(),v);
				return Div.unCast(div(numer,denom));
			}
			return e.copy();
		}
		return num(1);
	}
	
	public static Sequence seperateByVar(Expr e,Expr v) {//returns {coef,var parts}
		Sequence out = sequence();
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
			
		}else if(e.typeName().equals("div")) {
			Func castedDiv = (Func)e;
			Sequence numer = seperateByVar(castedDiv.getNumer(),v);
			Sequence denom = seperateByVar(castedDiv.getDenom(),v);
			
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
	
	public static Sequence seperateCoef(Expr e) {//returns [coef,remain]
		Sequence out = sequence();
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
		}else if(e.typeName().equals("div")) {
			Func castedDiv = (Func)e;
			Sequence numer = seperateCoef(castedDiv.getNumer());
			Sequence denom = seperateCoef(castedDiv.getDenom());
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
	
	protected static Expr exprListToPoly(Sequence poly,Var v,CasInfo casInfo){
		if(poly.size()==0) return num(0);
		Sum out = new Sum();
		for(int i = 0;i<poly.size();i++) {
			if(i == 0) {
				out.add(poly.get(i));
			}else if(i == 1){
				out.add(prod(poly.get(i),v));
			}else {
				out.add(prod(poly.get(i),power(v,num(i))));
			}
		}
		
		return out.simplify(casInfo);
	}
	
	public static BigInteger bigRoot(BigInteger n,BigInteger root) {//answer may need validation
		boolean neg = n.signum() == -1;
		n = n.abs();
		BigInteger x = BigInteger.ZERO.setBit(n.bitLength() / root.intValue() + 1);//set minimum guess
		int rootInt = root.intValue();
		while (true) {
			BigInteger y = x.multiply(root.subtract(BigInteger.ONE)).add(n.divide(x.pow(rootInt-1))).divide(root);//newton's method of convergence
			if (y.compareTo(x) >= 0) break;//converged
			x = y;
		}
		return neg? x.negate():x;
	}
	
	public static BigInteger divisibleRoot(BigInteger n,BigInteger root) {//returns a factor that n can split into example 50,2 -> 25 and 12,2 -> 4 and 54,3 -> 27
		boolean neg = n.signum() == -1;
		n = n.abs();
		
		int rootInt = root.intValue();
		BigInteger product = BigInteger.ONE;
		
		BigInteger i = BigInteger.TWO;//base incremental value
		BigInteger currentPower = i.pow(Math.abs(root.intValue()));//first test
		
		while(n.shiftRight(1).compareTo(currentPower) != -1  ) {//a number can only be divisible by a perfect square that is half its size(simply a conjecture). example 50 is divisible by 25 which is half its size
			
			
			while(n.mod(currentPower).equals(BigInteger.ZERO)) {
				n = n.divide(currentPower);
				product = product.multiply(currentPower);//Accumulative product that is root-able
			}
			
			i = i.add(BigInteger.ONE);//increment
			if(i.intValue()>0x10000) break;//give up
			currentPower = i.pow(rootInt);//update currentPower value
		}
		return neg? product.negate():product;
	}
	
	public static Func perfectPower(Num n) {//basically tries to do a big root with all possible exponents
		if(n.getRealValue().compareTo(BigInteger.TWO) == -1 || n.isComplex()) return power(n.copy(),num(1));//make it accept any number without errors
		int currentExpo = n.getRealValue().bitLength();//maximum exponent is log base 2 of that number. This is because 2 is the smallest base possible
		while(currentExpo != 1) {
			BigInteger possibleBase = bigRoot(n.getRealValue(),BigInteger.valueOf(currentExpo));
			if(possibleBase.pow(currentExpo).equals(n.getRealValue())) {//testing if re exponentiating it gives the same result
				return power(num(possibleBase),num(currentExpo));
			}
			currentExpo--;
		}
		return power(n.copy(),num(1));
	}
	
	public static Sequence basicRealAndImagComponents(Expr e,CasInfo casInfo) {//does obvious separation of real and imaginary components
		if(e instanceof Num) {
			Num n = (Num)e;
			return sequence(num(n.getRealValue()),num(n.getImagValue()));
		}
		
		if(e instanceof Prod) {
			Prod eCopy = (Prod)e.copy();
			for(int i = 0;i<e.size();i++) {
				if(e.get(i) instanceof Num) {
					Num num = (Num)eCopy.get(i);
					
					if(num.getImagValue().equals(BigInteger.ZERO)) {
						return sequence(eCopy,num(0));
					}else if(num.getRealValue().equals(BigInteger.ZERO)) {
						num.setRealValue(num.getImagValue());
						num.setImagValue(BigInteger.ZERO);
						eCopy.flags.simple = false;
						return (Sequence) sequence(num(0),eCopy).simplify(casInfo);
					}else {
						
						Prod imagCopy = (Prod)e.copy();
						imagCopy.set(i, num(num.getImagValue()));
						num.setImagValue(BigInteger.ZERO);
						eCopy.flags.simple = false;
						return sequence(eCopy.simplify(casInfo),imagCopy.simplify(casInfo));
					}
					
				}
			}
			return sequence(eCopy,num(0));
		}
		
		if(e instanceof Sum) {
			Sequence out = sequence(sum(),sum());
			for(int i = 0;i<e.size();i++) {
				Sequence seperatedEl = basicRealAndImagComponents(e.get(i),casInfo);
				
				out.get(0).add(seperatedEl.get(0));
				out.get(1).add(seperatedEl.get(1));
				
			}
			
			return (Sequence)out.simplify(casInfo);
		}
		
		return sequence(e.copy(),num(0));
	}
	
	public static BigInteger gcm(BigInteger a,BigInteger b) {
		return a.multiply(b).divide(a.gcd(b));
	}
	
	private static class IntFactor {//uses a mix of rho and wheel factorization
		
		static long[] initialSet = new long[]{2,3,5,7};//the wheel initial set that we use to create our base
		static ArrayList<BigInteger> wheelSet = new ArrayList<BigInteger>();//the full wheel sieve set
		static BigInteger increase = BigInteger.ONE;
		static BigInteger maxCheck = BigInteger.TWO.pow(24);//maximum check for wheel factor
		static int limit = (int)Math.pow(2, 15);//maximum loops for rho
		
		static final boolean VERBOSE = false;
		
		static boolean isPartOfWheelSet(long l){
			for(long i:initialSet){
				if(l%i == 0)return false;
			}
			return true;
		}
		static {
			initWheel();
		}
		static void initWheel(){//make first ring
			wheelSet.add(BigInteger.ONE);
			for(Long l:initialSet) {
				BigInteger bigL = BigInteger.valueOf(l);
				wheelSet.add(bigL);
				increase = increase.multiply(bigL);//product of initial set makes the size of the original ring
			}
			//to get to the next ring we simply add the increase
			int increaseInt = increase.intValue();
			
			for(int i = 2;i<increaseInt;i++){
				if(isPartOfWheelSet(i)){
					wheelSet.add(BigInteger.valueOf(i));
				}
			}
		}
		
		static ArrayList<BigInteger> wheelFactor(BigInteger l){
			ArrayList<BigInteger> factors = new ArrayList<BigInteger>();
			if(l.isProbablePrime(1)){//already prime so add self to list
				if(VERBOSE) System.out.println("alreadyPrime: "+l);
				factors.add(l);
				return factors;
			}
			if(l.compareTo(BigInteger.TWO) == -1) return factors;//if number less than 2 nothing to do
			
			BigInteger sqrtVal = l.sqrt();
			
			BigInteger i = BigInteger.ZERO;
			while(!Thread.currentThread().isInterrupted()){
				for(BigInteger test:wheelSet){//test is the current wheel set value
					test = test.add(i.multiply(increase));
					if(test.equals(BigInteger.ONE)) continue;//can't be 1 but might be increase*n+1
					if(test.compareTo(sqrtVal)==1 || test.compareTo(maxCheck)==1){//done
						if(!l.equals(BigInteger.ONE)) {
							if(VERBOSE) System.out.println("wheel set fake prime: "+l);
							System.err.println("warning could not factor "+l);
							factors.add(l);
						}
						return factors;
					}
					boolean worked = false;
					while(l.mod(test).equals(BigInteger.ZERO)){
						l = l.divide(test);
						if(VERBOSE) System.out.println("wheel factor: "+test);
						factors.add(test);
						worked = true;
					}
					if(worked && !l.equals(BigInteger.ONE)){
						
						if(l.isProbablePrime(1)){
							if(VERBOSE) System.out.println("already prime: "+l);
							factors.add(l);
							return factors;
						}
						
						sqrtVal = l.sqrt();
					}
				}
				i = i.add(BigInteger.ONE);
			}
			return null;
		}
		
		
		static BigInteger rhoFunc(BigInteger in,BigInteger n){
			return in.pow(2).add(BigInteger.ONE).mod(n);
		}
		
		static ArrayList<BigInteger> cachedBigPrimes = new ArrayList<BigInteger>();
		static BigInteger big = BigInteger.valueOf(Short.MAX_VALUE);
		static void addToCache(BigInteger i){//add bug number to cache
			if(i.compareTo(big)==1 ){
				if(!cachedBigPrimes.contains(i)) cachedBigPrimes.add(i);
			}
		}
		static void resizeCache(){//shrink cache if it has too many numbers
			while(cachedBigPrimes.size() > 1024){
				cachedBigPrimes.remove(0);
			}
		}
		
		static ArrayList<BigInteger> rhoFactor(BigInteger n){
			ArrayList<BigInteger> factors = new ArrayList<BigInteger>();
			if(n.compareTo(BigInteger.TWO) == -1) return factors;
			
			if(n.isProbablePrime(1)){//already prime
				if(VERBOSE) System.out.println("already prime: "+n);
				factors.add(n);
				return factors;
			}
			resizeCache();//shrink cache if it has too many numbers
			
			///cached primes
			if(n.compareTo(big) == 1){
				boolean usedCache = false;
				for(BigInteger i:cachedBigPrimes){
					if(n.mod(i).equals(BigInteger.ZERO)){
						usedCache = true;
						n = n.divide(i);
						if(VERBOSE) System.out.println("cached prime: "+i);
						factors.add(i);
					}
				}
				if(usedCache){
					if(n.isProbablePrime(1)){
						addToCache(n);
						if(VERBOSE) System.out.println("already prime: "+n);
						factors.add(n);
						return factors;
					}
					if(n.equals(BigInteger.ONE)){
						return factors;
					}
				}
			}
			///
			
			while (!Thread.currentThread().isInterrupted()) {
				BigInteger x = BigInteger.TWO,y=x,d = BigInteger.ONE;
				long counter = 0;
				while (d.equals(BigInteger.ONE)) {
					if(counter>limit){
						addToCache(n);
						if(VERBOSE) System.out.println("rho factor fake prime: "+n);
						System.err.println("warning could not factor "+n);
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
						if(VERBOSE) System.out.println("adding from wheel factor: "+d2);
						factors.add(d2);
						n = n.divide(d2);
					}
				
				}else{
					addToCache(d);
					if(VERBOSE) System.out.println("adding rho factor: "+d);
					factors.add(d);
					n = n.divide(d);
					while(n.mod(d).equals(BigInteger.ZERO)){
						if(VERBOSE) System.out.println("adding rho factor: "+d);
						factors.add(d);
						n = n.divide(d);
					}
				}
				if(n.isProbablePrime(1)){
					addToCache(n);
					if(VERBOSE) System.out.println("already prime: "+n);
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
		BigInteger n = num.getRealValue();
		if(n.signum()==-1) {
			n = n.abs();
			p.add(power(num(-1),num(1)));
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
			p.add(power(num(currentVal),num(count)));
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
		BigInteger outDenom = factorial(k);
		for (BigInteger i = n;i.compareTo(n.subtract(k))==1;i = i.subtract(BigInteger.ONE)){
			outNumer = outNumer.multiply(i);
		}
		return outNumer.divide(outDenom);
	}
	
	public static Var mostCommonVar(Expr e){
		if(e.containsVars()){
			ArrayList<VarCount> varcounts = null;
			varcounts = new ArrayList<VarCount>();
			e.countVars(varcounts);
			return Collections.max(varcounts).v;
		}
		return null;
	}
	
	public static Expr trigExpand(Expr e,CasInfo casInfo){
		if(e.containsType("sin")){
			Expr trigPart = null;
			Prod coef = new Prod();
			if(e instanceof Prod){
				for(int j = 0;j<e.size();j++){
					if(e.get(j).typeName().equals("sin")){
						if(trigPart == null){
							trigPart = e.get(j);
						}else{
							return e;
						}
					}else{
						coef.add(e.get(j));
					}
				}
			}else if(e.typeName().equals("sin")){
				trigPart = e;
			}
			
			if(trigPart != null){
				Expr innerPart = trigPart.get();
				Expr halfInner = div(innerPart,num(2)).simplify(casInfo);
				
				if(!(halfInner.typeName().equals("div"))){
					coef.add(sin(halfInner));
					coef.add(cos(halfInner));
					coef.add(num(2));
					return coef.simplify(casInfo);
				}
				return e;
			}
			
		}
		return e;
	}
	
	
	/*
	 * 
	 * this describes all the possible ways to add up to the 'size' variable
	 * 
	 */
	public static ArrayList<BigInteger[]> possiblePartitions(BigInteger size,BigInteger groups,BigInteger[] l_current,int currentIndex, ArrayList<BigInteger[]> out){
		if(out == null) out = new ArrayList<BigInteger[]>();
		if(l_current == null) l_current = new BigInteger[groups.intValue()];
		if(groups.equals(BigInteger.ONE)){
			BigInteger[] l_new = l_current.clone();
			l_new[currentIndex] = size;
			out.add(l_new);
			return out;
		}
		for (BigInteger i = BigInteger.ZERO;i.compareTo(size) != 1;i = i.add(BigInteger.ONE)){
			BigInteger[] l_new = l_current.clone();
			l_new[currentIndex] = i;
			possiblePartitions(size.subtract(i),groups.subtract(BigInteger.ONE),l_new,currentIndex+1,out);
		}
		return out;
	}
	public static Expr multinomial(Expr baseSum,Num expo,CasInfo casInfo) {//returns the binomial expansion
		Sum terms = new Sum();
		ArrayList<BigInteger[]> exponentSets = possiblePartitions(expo.getRealValue(),BigInteger.valueOf(baseSum.size()),null,0,null);
		//System.out.println(exponentSets);
		
		for(BigInteger[] set : exponentSets){
			Prod term = new Prod();
			BigInteger coef = BigInteger.ONE;
			BigInteger rem = expo.getRealValue();
			
			for(int i = 0;i<baseSum.size();i++){
				coef = coef.multiply(choose(rem,set[i]));
				rem = rem.subtract(set[i]);
				term.add(power(baseSum.get(i),num(set[i])));
			}
			
			term.add(num(coef));
			terms.add(term.simplify(casInfo));
		}
		
		return terms;
	}
	
	private static String[] trigTypes = new String[] {"sin","cos","tan","asin","acos","atan"};
	public static boolean containsTrig(Expr e) {
		for(String type:trigTypes) {
			if(e.containsType(type)) return true;
		}
		return false;
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
	
	public static Expr getLeftSideGeneric(Expr e) {
		if(e instanceof Equ) {
			return ((Equ)e).getLeftSide();
		}
		if(e instanceof Less) {
			return ((Less)e).getLeftSide();
		}
		if(e instanceof Greater) {
			return ((Greater)e).getLeftSide();
		}
		
		return null;
	}
	
	public static Expr getRightSideGeneric(Expr e) {
		if(e instanceof Equ) {
			return ((Equ)e).getRightSide();
		}
		if(e instanceof Less) {
			return ((Less)e).getRightSide();
		}
		if(e instanceof Greater) {
			return ((Greater)e).getRightSide();
		}
		
		return null;
	}
	
	public static void setLeftSideGeneric(Expr expr,Expr ls) {
		if(expr instanceof Equ) {
			((Equ)expr).setLeftSide(ls);
		}
		if(expr instanceof Less) {
			((Less)expr).setLeftSide(ls);
		}
		if(expr instanceof Greater) {
			((Greater)expr).setLeftSide(ls);
		}
	}
	
	public static void setRightSideGeneric(Expr expr,Expr rs) {
		if(expr instanceof Equ) {
			((Equ)expr).setRightSide(rs);
		}
		if(expr instanceof Less) {
			((Less)expr).setRightSide(rs);
		}
		if(expr instanceof Greater) {
			((Greater)expr).setRightSide(rs);
		}
	}
	
	static HashMap<String,String> BLToLatexFunctionNameMap = null;
	private static void initBLToMathMLFunctionNameMap() {
		BLToLatexFunctionNameMap = new HashMap<String,String>();
		//based on class names
		BLToLatexFunctionNameMap.put("asin","arcsin");
		BLToLatexFunctionNameMap.put("acos","arccos");
		BLToLatexFunctionNameMap.put("atan","arctan");
	}
	public static String generateLatex(Expr e) {
		if(BLToLatexFunctionNameMap == null) initBLToMathMLFunctionNameMap();
		String out = "";
		String leftParen = "\\left( ";
		String rightParen = "\\right) ";
		if(e.equals(Var.PI)) {
			out+="\\pi ";
		}else if(e instanceof Var || e instanceof Num || e.equals(Var.E)) {
			out += e.toString();
		}else if(e instanceof Prod) {
			e = (Prod)e.copy();//need to bring num to front
			
			for(int i = 0;i<e.size();i++){
				if(e.get(i) instanceof Num){
					Expr temp = e.get(0);
					e.set(0,e.get(i));
					e.set(i, temp);
					break;
				}
			}
			
			if(e.get(0).equals(Num.NEG_ONE)){
				e.remove(0);
				out+="-";
			}
				
			for(int i = 0;i<e.size();i++) {
				boolean paren = e.get(i) instanceof Sum;
				if(paren) out+=leftParen;
				out+=generateLatex(e.get(i));
				if(paren) out+=rightParen;
				if(i!=e.size()-1) out+=" \\cdot ";
			}
		}else if(e instanceof Sum) {
			for(int i = 0;i<e.size();i++) {
				if(i!=0)out+=generateLatex(e.get(i).strangeAbs(CasInfo.normal));
				else out+=generateLatex(e.get(i));
				if(i!=e.size()-1) {
					if(e.get(i+1).negative()) out+="-";
					else out+="+";
				}
			}
		}else if(e.typeName().equals("div")) {
			out+="\\frac{";
			out+=generateLatex(((Func)e).getNumer());
			out+="}{";
			out+=generateLatex(((Func)e).getDenom());
			out+="}";
		}else if(e.typeName().equals("power")) {
			Func casted = (Func)e;
			
			if(Rule.fastSimilarExpr(sqrtObj,e)){
				out += "\\sqrt{";
				out+= generateLatex(casted.getBase());
				out += "}";
			}else{
				out+="{";
				boolean parenBase = false;
				if(casted.getBase() instanceof Sum || casted.getBase() instanceof Prod || casted.getBase().typeName().equals("power") || (casted.getBase() instanceof Num && casted.getBase().negative())) parenBase = true;
				if(parenBase) out+=leftParen;
				out+=generateLatex(casted.getBase());
				if(parenBase) out+=rightParen;
				out+="}^{";
				boolean parenExpo= false;
				if(casted.getExpo() instanceof Sum || casted.getExpo() instanceof Prod || casted.getExpo().typeName().equals("power")) parenExpo = true;
				if(parenExpo) out+=leftParen;
				out+=generateLatex(casted.getExpo());
				if(parenExpo) out+=rightParen;
				out+="}";
			}
		}else if(e instanceof Equ) {
			Equ casted = (Equ)e;
			out+=generateLatex( casted.getLeftSide() );
			out+="=";
			out+=generateLatex( casted.getRightSide() );
		}else if(e.typeName().equals("integrateOver")) {
			Func castedDefInt = (Func)e;
			out+="\\int_{";
			out+=generateLatex(IntegrateOver.getMin(castedDefInt));
			out+="}^{";
			out+=generateLatex(IntegrateOver.getMax(castedDefInt));
			out+="}{";
			out+=generateLatex(IntegrateOver.getExpr(castedDefInt));
			out+=" d"+generateLatex(castedDefInt.getVar());
			out+="}";
		}else if(e.typeName().equals("diff")) {
			Func casted = (Func)e;
			out+="\\frac{d}{d";
			out+=casted.getVar().toString();
			out+="}"+leftParen;
			out+=generateLatex(casted.get());
			out+=rightParen;
		}else if(e.typeName().equals("abs")){
			out+="\\left| ";
			out+=generateLatex(e.get());
			out+="\\right| ";
		}else{
			String BLfunctionName = e.getClass().getSimpleName().toLowerCase();
			String repl = BLToLatexFunctionNameMap.get(BLfunctionName);
			if(repl == null) repl = BLfunctionName;
			
			out+="\\"+repl+leftParen;
			out+=generateLatex(e.get());
			out+=rightParen;
		}
		
		return out;
	}
	
}
