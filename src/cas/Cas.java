package cas;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.FunctionsLoader;
import cas.base.StandardRules;
import cas.bool.*;
import cas.primitive.*;
import cas.calculus.*;
import cas.lang.*;

public class Cas {
	
	private volatile static boolean ALL_LOADED = false;
	public static boolean isAllLoaded() {
		return ALL_LOADED;
	}
	
	public static void load(){
		if(ALL_LOADED) return;
		System.out.println("Loading BitLogic CAS...");
		MetaLang.init();//load the meta language
		Interpreter.init();//load bit logic standard syntax
		
		FunctionsLoader.load();//load functions into memory
		StandardRules.loadRules();//load additional shared rules
		
		FunctionsLoader.FUNCTION_UNLOCKED = true;//on the fly function generation now permitted since everything is loaded
		
		Expr.random = new Random(761234897);//initialize random variable
		
		Ask.loadBasicQuestions();//load Q and A file
		
		ALL_LOADED = true;
		System.out.println("Done loading CAS");
	}
	
	/*
	 * this file is for shortcuts and general algorithms used everywhere
	 */
	
	public static final Expr nullExpr = null;
	
	public static Expr createExpr(String expr) {
		return Interpreter.createExpr(expr);
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
		public VarCount(Var v,int c){
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
	
	public static boolean isSqrt(Expr e) {
		if(!e.isType("power")) return false;
		
		Func casted = (Func)e;
			
		if(!casted.getExpo().isType("div")) return false;
		
		Func expo = (Func)casted.getExpo();
		
		if(!(expo.getDenom().equals(Num.TWO) && expo.getNumer().equals(Num.ONE))) return false;
		
		return true;
	}
	
	public static boolean isCbrt(Expr e) {
		if(!e.isType("power")) return false;
		
		Func casted = (Func)e;
			
		if(!casted.getExpo().isType("div")) return false;
		
		Func expo = (Func)casted.getExpo();
		
		if(!(expo.getDenom().equals(num(3)) && expo.getNumer().equals(Num.ONE))) return false;
		
		return true;
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
		Func out = (Func) FunctionsLoader.power.copy();
		out.add(a);
		out.add(b);
		return out;
	}
	public static Func sum(Expr... exprs) {
		Func out = (Func) FunctionsLoader.sum.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	public static Func prod(Expr... exprs) {
		Func out = (Func) FunctionsLoader.prod.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	public static Func dot(Expr... exprs) {
		Func out = (Func) FunctionsLoader.dot.copy();
		for(Expr e:exprs){
			out.add(e);
		}
		return out;
	}
	public static Func and(Expr... exprs) {
		Func out = (Func) FunctionsLoader.and.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	public static Func or(Expr... exprs) {
		Func out = (Func) FunctionsLoader.or.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	public static Func exprSet(Expr... exprs) {
		Func out = (Func) FunctionsLoader.exprSet.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	public static Func gcd(Expr... exprs) {
		Func out = (Func) FunctionsLoader.gcd.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	
	public static Func limit(Expr e,Func becomes){
		Func out = (Func) FunctionsLoader.limit.copy();
		out.add(e);
		out.add(becomes);
		return out;
	}
	public static Func not(Expr expr) {
		Func out = (Func) FunctionsLoader.not.copy();
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
	public static Func equ(Expr a,Expr b) {
		Func out = (Func) FunctionsLoader.equ.copy();
		out.add(a);
		out.add(b);
		return out;
	}
	public static Func equGreater(Expr a,Expr b) {
		Func out = (Func) FunctionsLoader.greater.copy();
		out.add(a);
		out.add(b);
		return out;
	}
	public static Func equLess(Expr a,Expr b) {
		Func out = (Func) FunctionsLoader.less.copy();
		out.add(a);
		out.add(b);
		return out;
	}
	public static BoolState bool(boolean b) {
		return new BoolState(b);
	}
	public static Func ln(Expr expr) {
		Func out = (Func) FunctionsLoader.ln.copy();
		out.add(expr);
		return out;
	}
	public static Func sub(Expr a,Expr b) {
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
	public static Func neg(Expr a) {
		return prod(num(-1),a);
	}
	public static Func div(Expr a,Expr b) {
		Func out = (Func) FunctionsLoader.div.copy();
		out.add(a);
		out.add(b);
		return out;
	}
	
	public static Func diff(Expr e,Var v) {
		Func out = (Func) FunctionsLoader.diff.copy();
		out.add(e);
		out.add(v);
		return out;
	}
	public static Func integrate(Expr e,Var v) {
		Func out = (Func) FunctionsLoader.integrate.copy();
		out.add(e);
		out.add(v);
		return out;
	}
	public static Func integrateOver(Expr min,Expr max,Expr e,Var v) {
		Func out = (Func) FunctionsLoader.integrateOver.copy();
		out.add(min);
		out.add(max);
		out.add(e);
		out.add(v);
		return out;
	}
	public static Func solve(Expr e,Expr v) {
		Func out = (Func) FunctionsLoader.solve.copy();
		out.add(e);
		out.add(v);
		return out;
	}
	
	public static Func exp(Expr expr) {
		return power(e(),expr);
	}
	public static Func sin(Expr expr) {
		Func out = (Func) FunctionsLoader.sin.copy();
		out.add(expr);
		return out;
	}
	public static Func cos(Expr expr) {
		Func out = (Func) FunctionsLoader.cos.copy();
		out.add(expr);
		return out;
	}
	public static Func tan(Expr expr) {
		Func out = (Func) FunctionsLoader.tan.copy();
		out.add(expr);
		return out;
	}
	public static Func atan(Expr expr) {
		Func out = (Func) FunctionsLoader.atan.copy();
		out.add(expr);
		return out;
	}
	public static Func asin(Expr expr) {
		Func out = (Func) FunctionsLoader.asin.copy();
		out.add(expr);
		return out;
	}
	public static Func acos(Expr expr) {
		Func out = (Func) FunctionsLoader.acos.copy();
		out.add(expr);
		return out;
	}
	public static Func approx(Expr expr,Func defsSet) {
		Func out = (Func) FunctionsLoader.approx.copy();
		out.add(expr);
		out.add(defsSet);
		return out;
	}
	public static Func factor(Expr expr) {
		Func out = (Func) FunctionsLoader.factor.copy();
		out.add(expr);
		return out;
	}
	public static Func distr(Expr expr) {
		Func out = (Func) FunctionsLoader.distr.copy();
		out.add(expr);
		return out;
	}
	public static Func gamma(Expr expr) {
		Func out = (Func) FunctionsLoader.gamma.copy();
		out.add(expr);
		return out;
	}
	public static Func lambertW(Expr expr){
		Func out = (Func)FunctionsLoader.lambertW.copy();
		out.add(expr);
		return out;
	}
	public static Func abs(Expr expr) {
		Func out = (Func) FunctionsLoader.abs.copy();
		out.add(expr);
		return out;
	}
	public static Func mat(Expr... exprs) {
		Func out = (Func) FunctionsLoader.mat.copy();
		for(Expr e:exprs) out.add(e);
		return out;
	}
	public static Func func(String name,Func vEqu,Expr expr) {
		return new Func(name,vEqu,expr);
	}
	
	
	public static Func becomes(Expr left,Expr right) {
		Func out = (Func)FunctionsLoader.becomes.copy();
		out.add(left);
		out.add(right);
		return out;
	}
	public static Func transpose(Expr e) {
		Func out = (Func)FunctionsLoader.transpose.copy();
		out.add(e);
		return out;
	}
	public static Func next(Func sequence,Num num) {
		Func out = (Func)FunctionsLoader.next.copy();
		out.add(sequence);
		out.add(num);
		return out;
	}
	public static Func define(Expr left,Expr right) {
		Func out = (Func) FunctionsLoader.define.copy();
		out.add(left);
		out.add(right);
		return out;
	}
	public static Func ternary(Expr toBeEvaled,Expr ifTrue,Expr ifFalse) {
		Func out = (Func)FunctionsLoader.ternary.copy();
		out.add(toBeEvaled);
		out.add(ifTrue);
		out.add(ifFalse);
		return out;
	}
	public static Func range(Expr min,Expr max,Expr e,Var v) {
		Func out = (Func)FunctionsLoader.range.copy();
		out.add(min);
		out.add(max);
		out.add(e);
		out.add(v);
		return out;
	}
	public static Func boolCompress(Expr expr) {
		Func out = (Func) FunctionsLoader.boolCompress.copy();
		out.add(expr);
		return out;
	}
	public static Func boolTableToExpr(Func tableSet,Func varsSet) {
		Func out = (Func) FunctionsLoader.boolTableToExpr.copy();
		out.add(tableSet);
		out.add(varsSet);
		return out;
	}
	public static Func sequence(Expr... exprs) {
		Func out = (Func) FunctionsLoader.sequence.copy();
		for(Expr expr:exprs) out.add(expr);
		return out;
	}
	public static Func comparison(Func equ) {
		Func out = (Func) FunctionsLoader.comparison.copy();
		out.add(equ);
		return out;
	}
	public static Func expand(Expr e) {
		Func out = (Func) FunctionsLoader.expand.copy();
		out.add(e);
		return out;
	}
	//
	
	public static boolean allLinearTerms(Expr e,Var v) {
		Func prodCast = (Func)e;
		for(int i = 0;i<prodCast.size();i++){
			Func current = Power.cast(prodCast.get(i));
			if(!(isPositiveRealNum(current.getExpo()) && (current.getBase().isType("sum") || current.getBase() instanceof Var) && degree(current.getBase(),v) == BigInteger.ONE)){
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
		if(expr.isType("div")) {
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
			
			Func partsSequence = seperateByVar(denomFactored,v);
			
			Expr denomCoef = partsSequence.get(0);
			denomFactored = Prod.cast(partsSequence.get(1));
			
			if(!allLinearTerms(denomFactored,v)) return expr;//make sure its a product of linear terms
			frac.setDenom(denomFactored);
			
			Func outSum = sum();
			
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
				
				Func polySequence = polyExtract(currentTerm.getBase(),v,casInfo);
				Expr linearTermCoef = polySequence.get(1);
				Func solutionEqu = (Func)ExprSet.cast(solve(equ(currentTerm,Num.ZERO),v).simplify(casInfo)).get();
				
				
				BigInteger expo = ((Num)currentTerm.getExpo()).getRealValue();
				BigInteger expoMinusOne = expo.subtract(BigInteger.ONE);
				
				for(BigInteger j = BigInteger.ZERO;j.compareTo( expo ) == -1;j = j.add(BigInteger.ONE)){
					BigInteger currentExpo = expo.subtract(j);
					
					Expr functionOut = currentFunction.replace(solutionEqu);
					
					chara.add( exprSet(num(outSum.size()),polySequence.get(0),polySequence.get(1),num(currentExpo)) );//index , constant , linear coeff, exponent
					
					Expr numer = div(functionOut,prod(denomCoef,power(linearTermCoef,num(j)), num(factorial(j))));
					Expr newTerm = div(numer,  power(currentTerm.getBase(),num(currentExpo))  );
					
					outSum.add(newTerm);
					
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
					Func firstDiv = (Func) outSum.get(i);
					Func secondDiv = (Func) outSum.get(j);
					
					Expr combinedDenom = power(distr( prod(((Func)firstDiv.getDenom()).getBase(),((Func)secondDiv.getDenom()).getBase())  ),((Func)firstDiv.getDenom()).getExpo());
					
					Expr combinedNumer = sum( prod(firstDiv.getNumer(),secondDiv.getDenom()) ,  prod(firstDiv.getDenom(),secondDiv.getNumer()) );
					
					outSum.set(i, div(combinedNumer,combinedDenom));
					outSum.set(j, null);//don't want resize of array
				}
				
				for(int i = outSum.size()-1;i>=0;i--) {
					if(outSum.get(i) == null) outSum.remove(i);
				}
				
			}
			return outSum.simplify(casInfo);
			
		}
		return expr;
	}
	
	public static Expr polyDiv(Expr expr,Var v,CasInfo casInfo) {
		if(expr.isType("div")) {
			Func frac = (Func)expr.copy();
			Func numPolySequence = polyExtract(distr(frac.getNumer()).simplify(casInfo),v,casInfo);
			Func denPolySequence = polyExtract(distr(frac.getDenom()).simplify(casInfo),v,casInfo);
			if(numPolySequence != null && denPolySequence != null && numPolySequence.size()>=denPolySequence.size()) {
				Func[] result = polyDiv(numPolySequence,denPolySequence,casInfo);
				Expr outPart =  exprListToPoly(result[0],v,casInfo);
				Expr remainPart =  div(exprListToPoly(result[1],v,casInfo),frac.getDenom());
				expr = sum(outPart,remainPart);
			}
		}
		return expr;
	}
	
	public static Func[] polyDiv(Func numeratorSequence,Func denominatorSequence,CasInfo casInfo) {//returns [output] + [remainder]
		Func remainSequence = (Func)numeratorSequence.copy();
		Num zero = num(0);
		
		Func outSequence = sequence();
		
		while(remainSequence.size() >= denominatorSequence.size()) {
			if(denominatorSequence.get(denominatorSequence.size()-1).equals(zero)) {//avoid divide by zero situation
				outSequence.add(0, zero.copy());
				remainSequence.remove(remainSequence.size()-1);//pop last element
				continue;
			}
			
			
			Expr coef = div(remainSequence.get(remainSequence.size()-1).copy(),denominatorSequence.get(denominatorSequence.size()-1).copy()).simplify(casInfo);
			outSequence.add(0, coef);
			coef = neg(coef);
			
			
			for(int i = remainSequence.size()-denominatorSequence.size();i<remainSequence.size()-1;i++) {//we can skip last one since we know it will be deleted
				remainSequence.set(i, sum( prod(denominatorSequence.get(i-(remainSequence.size()-denominatorSequence.size()) ),coef) ,remainSequence.get(i)).simplify(casInfo) );
			}
			remainSequence.remove(remainSequence.size()-1);//pop last element
			
			
		}
		while(remainSequence.size()>0 && remainSequence.get(remainSequence.size()-1).equals(zero)) {//clean zeros off end
			remainSequence.remove(remainSequence.size()-1);
		}
		
		return new Func[] { outSequence,remainSequence };
	}
	
	public static BigInteger degree(Expr expr,Var v) {//returns -1 if it is not possible
		BigInteger negOne = BigInteger.valueOf(-1);
		Func exprSum = Sum.cast(expr);
		BigInteger maxDegree = BigInteger.ZERO;
		for(int i = 0;i<exprSum.size();i++) {
			Expr term = exprSum.get(i);
			Expr stripped = stripNonVarPartsFromProd(term,v);
			if(stripped.equals(v)) maxDegree = maxDegree.max(BigInteger.ONE);
			else if(stripped.isType("power")) {
				Func casted = (Func)stripped;
				if(isPositiveRealNum(casted.getExpo())) {
					if(casted.getBase().equals(v)) {
						maxDegree = maxDegree.max(((Num)casted.getExpo()).getRealValue());
					}else if(isPolynomialUnstrict(casted.getBase(),v)) {
						maxDegree = maxDegree.max( degree(casted.getBase(),v).multiply(((Num)casted.getExpo()).getRealValue()) );
					}else return negOne;
				}else return negOne;
			}else if(stripped.isType("prod")) {
				BigInteger subDegreeSum = BigInteger.ZERO;
				for(int j = 0;j<stripped.size();j++) {
					BigInteger childDegree = degree(stripped.get(j),v);
					if(childDegree.equals(negOne)) return negOne;
					subDegreeSum = subDegreeSum.add(childDegree);
				}
				maxDegree = maxDegree.max(subDegreeSum);
			}else if(stripped.isType("sum")) {
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
		if(expr.isType("sum") || expr.isType("prod")){
			for(int i = 0;i<expr.size();i++){
				if(!isPolynomialUnstrict(expr.get(i),v)) return false;
			}
			return true;
		}else if(expr.isType("power")){
			Func casted = (Func)expr;
			return isPositiveRealNum(casted.getExpo()) && isPolynomialUnstrict(casted.getBase(),v);
		}else if(expr.isType("div")){
			Func castedDiv = (Func)expr;
			return !castedDiv.getDenom().contains(v) && isPolynomialUnstrict(castedDiv.getNumer(),v);
		}
		return false;
	}
	
	public static boolean isPlainPolynomial(Expr expr,Var v) {//basic polynomial
		Func exprSum = Sum.cast(expr);
		for(int i = 0;i<exprSum.size();i++) {
			Expr term = exprSum.get(i);
			Expr stripped = stripNonVarPartsFromProd(term,v);
			if(stripped.equals(v)) continue;
			else if(stripped.isType("power")) {
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
			}else if(expr.isType("div")){
				Func castedDiv = (Func)expr;
				return div(getLeadingCoef(castedDiv.getNumer(),v,casInfo),castedDiv.getDenom()).simplify(casInfo);
			}else if(expr.isType("prod")){
				Func outProd = prod();
				for(int i = 0;i<expr.size();i++){
					outProd.add(getLeadingCoef(expr.get(i),v,casInfo));
				}
				return outProd.simplify(casInfo);
			}else if(expr.isType("power")){
				Func casted = (Func)expr;
				return power(getLeadingCoef(casted.getBase(),v,casInfo),casted.getExpo()).simplify(casInfo);
			}else if(expr.isType("sum")){
				BigInteger[] degrees = new BigInteger[expr.size()];
				for(int i = 0;i<expr.size();i++){
					degrees[i] = degree(expr.get(i),v);
				}
				
				BigInteger maxDegree = BigInteger.ZERO;
				
				for(int i = 0;i<degrees.length;i++){
					BigInteger currentDegree = degrees[i];
					maxDegree = maxDegree.max(currentDegree);
				}
				
				Func outSum = sum();
				for(int i = 0;i<expr.size();i++){
					if(degrees[i].equals(maxDegree)){
						outSum.add( getLeadingCoef(expr.get(i) ,v,casInfo) );
					}
				}
				return outSum.simplify(casInfo);
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
	
	/*
	 * extracts the coefficients from an expression in the standard polynomial format
	 * example polyExtract(1+x^2+a*x^4,x) -> [1,0,1,0,a]
	 * 
	 */
	public static Func polyExtract(Expr expr,Var v,CasInfo casInfo) {//returns sequence
		BigInteger maxDegree = BigInteger.valueOf(16);
		Func coefSequence = sequence();
		Func sum = Sum.cast(expr);
		for(int i = 0;i<sum.size();i++) {
			Expr e = sum.get(i);
			BigInteger degree = BigInteger.ZERO;
			Expr contents = null;
			if(e.contains(v)) {
				Func partsSequence = seperateByVar(e,v);
				if(partsSequence.get(1).equals(v)) {
					contents = partsSequence.get(0);
					degree = BigInteger.ONE;
				}else if(partsSequence.get(1).isType("power")) {
					Func casted = (Func)partsSequence.get(1);
					if(casted.getBase().equals(v) && isPositiveRealNum(casted.getExpo())) {
						degree = ((Num)casted.getExpo()).getRealValue();
					}else return null;
					contents = partsSequence.get(0);
				}else return null;
			}else contents = e;
			if(degree.compareTo(maxDegree) == 1) return null;//don't want too big of a polynomial
			while(BigInteger.valueOf(coefSequence.size()).compareTo(degree) <= 0) coefSequence.add(num(0));//resize coef length to fit degree size
			int degreeInt = degree.intValue();
			coefSequence.set(degreeInt,sum(coefSequence.get(degreeInt),contents));
		}
		coefSequence.simplifyChildren(casInfo);
		return coefSequence;
	}
	
	public static Expr stripNonVarPartsFromProd(Expr e,Expr v) {//a*x^2/c -> x^2, its like the opposite of getting the coefficient
		if(e.contains(v)) {
			if(e.isType("prod")) {
				Func eCastedProd = (Func)e.copy();
				for(int i = 0;i<eCastedProd.size();i++) {
					if(!eCastedProd.get(i).contains(v)) {
						eCastedProd.remove(i);
						i--;
					}
				}
				return Prod.unCast(eCastedProd);
			}else if(e.isType("div")) {
				Func eCastedDiv = (Func)e;
				Expr numer = stripNonVarPartsFromProd(eCastedDiv.getNumer(),v);
				Expr denom = stripNonVarPartsFromProd(eCastedDiv.getDenom(),v);
				return Div.unCast(div(numer,denom));
			}
			return e.copy();
		}
		return num(1);
	}
	
	public static Func seperateByVar(Expr e,Expr v) {//returns [coef,var parts]
		Func outSequence = sequence();
		if(!e.contains(v)) {
			outSequence.add(e.copy());
			outSequence.add(num(1));
		}else if(e.isType("prod")) {
			Expr outCopy = e.copy();
			Expr coefProd = prod();
			
			for(int i = 0;i < outCopy.size();i++) {
				if(!outCopy.get(i).contains(v)) {
					coefProd.add(outCopy.get(i));
					outCopy.remove(i);
					i--;
				}
			}
			
			outSequence.add(Prod.unCast(coefProd));
			outSequence.add(Prod.unCast(outCopy));
			
		}else if(e.isType("div")) {
			Func castedDiv = (Func)e;
			Func numerSequence = seperateByVar(castedDiv.getNumer(),v);
			Func denomSequence = seperateByVar(castedDiv.getDenom(),v);
			
			Expr coef = Div.unCast(div(numerSequence.get(0),denomSequence.get(0)));
			Expr newExpr = Div.unCast(div(numerSequence.get(1),denomSequence.get(1)));
			
			outSequence.add(coef);
			outSequence.add(newExpr);
			
		}else {
			outSequence.add(num(1));
			outSequence.add(e.copy());
		}
		return outSequence;
	}
	
	public static Func seperateCoef(Expr e) {//returns [coef,remain]
		Func outSeq = sequence();
		if(e.isType("prod")) {
			Func prodCopy = (Func)e.copy();
			for(int i = 0;i<prodCopy.size();i++) {
				if(prodCopy.get(i) instanceof Num) {
					outSeq.add(prodCopy.get(i));
					prodCopy.remove(i);
					i--;
				}
			}
			if(outSeq.size() == 0) {
				outSeq.add(num(1));
			}
			outSeq.add(Prod.unCast(prodCopy));
		}else if(e.isType("div")) {
			Func castedDiv = (Func)e;
			Func numerSeq = seperateCoef(castedDiv.getNumer());
			Func denomSeq = seperateCoef(castedDiv.getDenom());
			Expr newCoef = Div.unCast(div(numerSeq.get(0),denomSeq.get(0)));
			Expr newExpr = Div.unCast(div(numerSeq.get(1),denomSeq.get(1)));
			outSeq.add(newCoef);
			outSeq.add(newExpr);
		}else if(e instanceof Num) {
			outSeq.add(e.copy());
			outSeq.add(num(1));
		}else {
			outSeq.add(num(1));
			outSeq.add(e.copy());
		}
		
		return outSeq;
	}
	
	protected static Expr exprListToPoly(Func polySequence,Var v,CasInfo casInfo){
		if(polySequence.size()==0) return num(0);
		Func outSum = sum();
		for(int i = 0;i<polySequence.size();i++) {
			if(i == 0) {
				outSum.add(polySequence.get(i));
			}else if(i == 1){
				outSum.add(prod(polySequence.get(i),v));
			}else {
				outSum.add(prod(polySequence.get(i),power(v,num(i))));
			}
		}
		
		return outSum.simplify(casInfo);
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
	
	//does obvious separation of real and imaginary components
	//returns a sequence
	public static Func basicRealAndImagComponents(Expr e,CasInfo casInfo) {
		if(e instanceof Num) {
			Num n = (Num)e;
			return sequence(num(n.getRealValue()),num(n.getImagValue()));
		}
		
		if(e.isType("prod")) {
			Func eCopyProd = (Func)e.copy();
			for(int i = 0;i<e.size();i++) {
				if(e.get(i) instanceof Num) {
					Num num = (Num)eCopyProd.get(i);
					
					if(num.getImagValue().equals(BigInteger.ZERO)) {
						return sequence(eCopyProd,num(0));
					}else if(num.getRealValue().equals(BigInteger.ZERO)) {
						num.setRealValue(num.getImagValue());
						num.setImagValue(BigInteger.ZERO);
						eCopyProd.flags.simple = false;
						return (Func) sequence(num(0),eCopyProd).simplify(casInfo);
					}else {
						
						Func imagCopyProd = (Func)e.copy();
						imagCopyProd.set(i, num(num.getImagValue()));
						num.setImagValue(BigInteger.ZERO);
						eCopyProd.flags.simple = false;
						return sequence(eCopyProd.simplify(casInfo),imagCopyProd.simplify(casInfo));
					}
					
				}
			}
			return sequence(eCopyProd,num(0));
		}
		
		if(e.isType("sum")) {
			Func outSequence = sequence(sum(),sum());
			for(int i = 0;i<e.size();i++) {
				Func seperatedElSequence = basicRealAndImagComponents(e.get(i),casInfo);
				
				outSequence.get(0).add(seperatedElSequence.get(0));
				outSequence.get(1).add(seperatedElSequence.get(1));
				
			}
			
			return (Func)outSequence.simplify(casInfo);
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
	
	public static Func primeFactor(Num num) {
		
		if(num.isComplex()) {
			System.err.println("prime factor function recieved a complex number.");
			return null;
		}
		Func prod = prod();
		BigInteger n = num.getRealValue();
		if(n.signum()==-1) {
			n = n.abs();
			prod.add(power(num(-1),num(1)));
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
			prod.add(power(num(currentVal),num(count)));
		}
		
		
		return prod;
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
			Func coefProd = prod();
			if(e.isType("prod")){
				for(int j = 0;j<e.size();j++){
					if(e.get(j).isType("sin")){
						if(trigPart == null){
							trigPart = e.get(j);
						}else{
							return e;
						}
					}else{
						coefProd.add(e.get(j));
					}
				}
			}else if(e.isType("sin")){
				trigPart = e;
			}
			
			if(trigPart != null){
				Expr innerPart = trigPart.get();
				Expr halfInner = div(innerPart,num(2)).simplify(casInfo);
				
				if(!(halfInner.isType("div"))){
					coefProd.add(sin(halfInner));
					coefProd.add(cos(halfInner));
					coefProd.add(num(2));
					return coefProd.simplify(casInfo);
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
		Func termsSum = sum();
		ArrayList<BigInteger[]> exponentSets = possiblePartitions(expo.getRealValue(),BigInteger.valueOf(baseSum.size()),null,0,null);
		//System.out.println(exponentSets);
		
		for(BigInteger[] set : exponentSets){
			Func termProd = prod();
			BigInteger coef = BigInteger.ONE;
			BigInteger rem = expo.getRealValue();
			
			for(int i = 0;i<baseSum.size();i++){
				coef = coef.multiply(choose(rem,set[i]));
				rem = rem.subtract(set[i]);
				termProd.add(power(baseSum.get(i),num(set[i])));
			}
			
			termProd.add(num(coef));
			termsSum.add(termProd.simplify(casInfo));
		}
		
		return termsSum;
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
		if(e.isType("equ")) {
			return Equ.getLeftSide((Func)e);
		}
		if(e.isType("less")) {
			return Less.getLeftSide((Func)e);
		}
		if(e.isType("greater")) {
			return Greater.getLeftSide((Func)e);
		}
		
		return null;
	}
	
	public static Expr getRightSideGeneric(Expr e) {
		if(e.isType("equ")) {
			return Equ.getRightSide((Func)e);
		}
		if(e.isType("less")) {
			return Less.getRightSide((Func)e);
		}
		if(e.isType("greater")) {
			return Greater.getRightSide((Func)e);
		}
		
		return null;
	}
	
	public static void setLeftSideGeneric(Expr expr,Expr ls) {
		if(expr.isType("equ")) {
			Equ.setLeftSide(((Func)expr),ls);
		}
		if(expr.isType("less")) {
			Less.setLeftSide((Func)expr,ls);
		}
		if(expr.isType("greater")) {
			Greater.setLeftSide((Func)expr,ls);
		}
	}
	
	public static void setRightSideGeneric(Expr expr,Expr rs) {
		if(expr.isType("equ")) {
			Equ.setRightSide(((Func)expr),rs);
		}
		if(expr.isType("less")) {
			Less.setRightSide(((Func)expr),rs);
		}
		if(expr.isType("greater")) {
			Greater.setRightSide((Func)expr,rs);
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
		}else if(e.isType("prod")) {
			e = (Func)e.copy();//need to bring num to front
			
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
				boolean paren = e.get(i).isType("sum");
				if(paren) out+=leftParen;
				out+=generateLatex(e.get(i));
				if(paren) out+=rightParen;
				if(i!=e.size()-1) out+=" \\cdot ";
			}
		}else if(e.isType("sum")) {
			for(int i = 0;i<e.size();i++) {
				if(i!=0)out+=generateLatex(e.get(i).strangeAbs(CasInfo.normal));
				else out+=generateLatex(e.get(i));
				if(i!=e.size()-1) {
					if(e.get(i+1).negative()) out+="-";
					else out+="+";
				}
			}
		}else if(e.isType("div")) {
			out+="\\frac{";
			out+=generateLatex(((Func)e).getNumer());
			out+="}{";
			out+=generateLatex(((Func)e).getDenom());
			out+="}";
		}else if(e.isType("power")) {
			Func casted = (Func)e;
			
			if(isSqrt(e)){
				out += "\\sqrt{";
				out+= generateLatex(casted.getBase());
				out += "}";
			}else{
				out+="{";
				boolean parenBase = false;
				if(casted.getBase().isType("sum") || casted.getBase().isType("prod") || casted.getBase().isType("power") || (casted.getBase() instanceof Num && casted.getBase().negative())) parenBase = true;
				if(parenBase) out+=leftParen;
				out+=generateLatex(casted.getBase());
				if(parenBase) out+=rightParen;
				out+="}^{";
				boolean parenExpo= false;
				if(casted.getExpo().isType("sum") || casted.getExpo().isType("prod") || casted.getExpo().isType("power")) parenExpo = true;
				if(parenExpo) out+=leftParen;
				out+=generateLatex(casted.getExpo());
				if(parenExpo) out+=rightParen;
				out+="}";
			}
		}else if(e.isType("equ")) {
			Func castedEqu = (Func)e;
			out+=generateLatex( Equ.getLeftSide(castedEqu) );
			out+="=";
			out+=generateLatex( Equ.getRightSide(castedEqu) );
		}else if(e.isType("integrateOver")) {
			Func castedDefInt = (Func)e;
			out+="\\int_{";
			out+=generateLatex(IntegrateOver.getMin(castedDefInt));
			out+="}^{";
			out+=generateLatex(IntegrateOver.getMax(castedDefInt));
			out+="}{";
			out+=generateLatex(IntegrateOver.getExpr(castedDefInt));
			out+=" d"+generateLatex(castedDefInt.getVar());
			out+="}";
		}else if(e.isType("diff")) {
			Func casted = (Func)e;
			out+="\\frac{d}{d";
			out+=casted.getVar().toString();
			out+="}"+leftParen;
			out+=generateLatex(casted.get());
			out+=rightParen;
		}else if(e.isType("abs")){
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
