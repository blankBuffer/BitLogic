package cas;
import java.math.BigInteger;
import java.util.Random;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.FunctionsLoader;
import cas.base.StandardRules;
import cas.bool.*;
import cas.primitive.*;
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
		
		Var.init();//initialize var specific stuff
		
		StandardRules.loadRules();//load additional shared rules
		
		Unit.init();//initialize unit conversion information
		
		FunctionsLoader.FUNCTION_UNLOCKED = true;//on the fly function generation now permitted since everything is loaded
		
		Expr.random = new Random(761234897);//initialize random variable
		
		Ask.loadBasicQuestions();//load Q and A file
		
		ALL_LOADED = true;
		System.out.println("Done loading CAS");
	}
	
	/*
	 * this file is for shortcuts
	 */
	
	public static final Expr nullExpr = null;
	
	public static Expr createExpr(String expr) {
		return Interpreter.createExpr(expr);
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
	public static Func comparison(Expr e) {
		Func out = (Func) FunctionsLoader.comparison.copy();
		out.add(e);
		return out;
	}
	
	public static Func getFunction(String name,Expr... params) {
		Func out = null;
		try {
			out = (Func) FunctionsLoader.getFuncByName(name, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}
}
