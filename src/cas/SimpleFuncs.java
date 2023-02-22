package cas;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import cas.lang.*;
import cas.primitive.*;
import cas.programming.Ternary;
import cas.special.*;
import cas.algebra.Distr;
import cas.algebra.Factor;
import cas.algebra.Gcd;
import cas.algebra.Solve;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.base.StandardRules;
import cas.bool.*;
import cas.calculus.*;

public class SimpleFuncs extends Cas{

	private static HashMap<String,Func> funcs = new HashMap<String,Func>();
	public static boolean FUNCTION_UNLOCKED = false;//ability to create new functions on the fly, must be turned off during loading of CAS to prevent spelling errors
	
	static Func tree = new Func("tree",1);
	static Func size = new Func("size",1);
	static Func get = new Func("get",2);
	static Func choose = new Func("choose",2);
	static Func primeFactor = new Func("primeFactor",1);
	
	static Func partialFrac = new Func("partialFrac",2);
	static Func polyDiv = new Func("polyDiv",2);
	
	static Func polyCoef = new Func("polyCoef",2);
	static Func degree = new Func("degree",2);
	static Func leadingCoef = new Func("leadingCoef",2);
	static Func conv = new Func("conv",3);
	static Func latex = new Func("latex",1);
	static Func subst = new Func("subst",2);
	static Func comparison = new Func("comparison",1);
	static Func taylor = new Func("taylor",3);
	static Func gui = new Func("gui",0);
	static Func nParams = new Func("nParams",1);
	static Func isType = new Func("isType",2);
	static Func contains = new Func("contains",2);
	static Func result = new Func("result",1);
	static Func allowAbs = new Func("allowAbs",0);
	static Func allowComplexNumbers = new Func("allowComplexNumbers",0);
	static Func singleSolutionMode = new Func("singleSolutionMode",0);
	static Func factorIrrationalRoots = new Func("factorIrrationalRoots",0);
	static Func relaxedPower = new Func("relaxedPower",0);
	static Func setAllowAbs = new Func("setAllowAbs",1);
	static Func setAllowComplexNumbers = new Func("setAllowComplexNumbers",1);
	static Func setSingleSolutionMode = new Func("setSingleSolutionMode",1);
	static Func setFactorIrrationalRoots = new Func("setFactorIrrationalRoots",1);
	static Func setRelaxedPower = new Func("setRelaxedPower",1);
	
	static Func sinh = new Func("sinh",1);
	static Func cosh = new Func("cosh",1);
	static Func tanh = new Func("tanh",1);
	
	static Func sec = new Func("sec",1);
	static Func csc = new Func("csc",1);
	static Func cot = new Func("cot",1);
	
	static Func extSeq = new Func("extSeq",2);
	static Func truncSeq = new Func("truncSeq",2);
	static Func subSeq = new Func("subSeq",3);
	static Func revSeq = new Func("revSeq",1);
	static Func sumSeq = new Func("sumSeq",1);
	
	static Func arcLen = new Func("arcLen",4);
	static Func repDiff = new Func("repDiff",3);
	
	static Func similar = new Func("similar",2);
	static Func fastSimilar = new Func("fastSimilar",2);
	
	static Func sortExpr = new Func("sortExpr",1);
	
	static Func delete = new Func("delete",1);
	static Func help = new Func("help",1);
	
	static Func fSolve = new Func("fSolve",2);
	
	static Func fastEquals = new Func("fastEquals",2);
	
	
	
	
	
	static Func power;
	static Func sin,cos,tan,asin,acos,atan;
	static Func and,or,not,boolCompress,boolTableToExpr;
	static Func approx;
	static Func becomes,equ;
	static Func exprSet;
	static Func sequence;
	static Func gcd;
	static Func sum;
	static Func div;
	static Func diff,integrate,limit;
	static Func ln;
	static Func lambertW;
	static Func distr,factor;
	static Func solve;
	static Func integrateOver;
	static Func gamma;
	static Func abs;
	static Func expand;
	static Func prod;
	static Func ternary;
	
	public static boolean functionsConstructed = false;
	
	public static void functionsConstructor() {
		if(functionsConstructed) return;
		System.out.println("constructing functions...");
		
		power = new Func("power",2,Power.loader);
		sin = new Func("sin",1,Trig.sinLoader);
		cos = new Func("cos",1,Trig.cosLoader);
		tan = new Func("tan",1,Trig.tanLoader);
		asin = new Func("asin",1,Trig.asinLoader);
		acos = new Func("acos",1,Trig.acosLoader);
		atan = new Func("atan",1,Trig.atanLoader);
		and = new Func("and",-1,And.andLoader);
		or = new Func("or",-1,Or.orLoader);
		not = new Func("not",1,Not.notLoader);
		boolCompress = new Func("boolCompress",1,BoolCompress.boolCompressLoader);
		boolTableToExpr = new Func("boolTableToExpr",2,BoolTableToExpr.boolTableToExprLoader);
		approx = new Func("approx",2,Approx.approxLoader);
		becomes = new Func("becomes",2,Becomes.becomesLoader);
		equ = new Func("equ",2,Equ.equLoader);
		exprSet = new Func("set",-1,ExprSet.exprSetLoader);
		sequence = new Func("sequence",-1,Sequence.sequenceLoader);
		gcd = new Func("gcd",-1,Gcd.gcdLoader);
		sum = new Func("sum",-1,Sum.sumLoader);
		div = new Func("div",2,Div.divLoader);
		diff = new Func("diff",2,Diff.diffLoader);
		limit = new Func("limit",2,Limit.limitLoader);
		ln = new Func("ln",1,Ln.lnLoader);
		integrate = new Func("integrate",2,Integrate.integrateLoader);
		lambertW = new Func("lambertW",1,LambertW.lambertwLoader);
		distr = new Func("distr",1,Distr.distrLoader);
		factor = new Func("factor",1,Factor.factorLoader);
		solve = new Func("solve",2,Solve.solveLoader);
		integrateOver = new Func("integrateOver",4,IntegrateOver.integrateOverLoader);
		gamma = new Func("gamma",1,Gamma.gammaLoader);
		abs = new Func("abs",1,Abs.absLoader);
		expand = new Func("expand",1,Distr.expandLoader);
		prod = new Func("prod",-1,Prod.prodLoader);
		ternary = new Func("ternary",3,Ternary.ternaryLoader);
		
		addFunc(power);
		addFunc(sin);
		addFunc(cos);
		addFunc(tan);
		addFunc(asin);
		addFunc(acos);
		addFunc(atan);
		addFunc(and);
		addFunc(or);
		addFunc(not);
		addFunc(boolCompress);
		addFunc(boolTableToExpr);
		addFunc(approx);
		addFunc(becomes);
		addFunc(equ);
		addFunc(gcd);
		addFunc(sum);
		addFunc(div);
		addFunc(diff);
		addFunc(ln);
		addFunc(limit);
		addFunc(integrate);
		addFunc(lambertW);
		addFunc(distr);
		addFunc(factor);
		addFunc(solve);
		addFunc(integrateOver);
		addFunc(gamma);
		addFunc(abs);
		addFunc(expand);
		addFunc(prod);
		addFunc(ternary);
		
		functionsConstructed = true;
		System.out.println("done constructing functions");
	}
	
	public static void loadRules(){
		
		//
		
		functionsConstructor();
		
		Cas.initExprTemp();
		
		//
		addFunc(choose);
		addFunc(get);
		addFunc(size);
		addFunc(tree);
		addFunc(primeFactor);
		
		addFunc(partialFrac);
		addFunc(polyDiv);
		
		addFunc(polyCoef);
		addFunc(degree);
		addFunc(leadingCoef);
		addFunc(conv);
		addFunc(latex);
		addFunc(subst);
		addFunc(comparison);
		addFunc(taylor);
		addFunc(gui);
		addFunc(nParams);
		addFunc(isType);
		addFunc(contains);
		addFunc(result);//override the simplify children bool
		addFunc(allowAbs);
		addFunc(singleSolutionMode);
		addFunc(allowComplexNumbers);
		addFunc(factorIrrationalRoots);
		addFunc(setAllowAbs);
		addFunc(setAllowComplexNumbers);
		addFunc(setSingleSolutionMode);
		addFunc(setFactorIrrationalRoots);
		addFunc(relaxedPower);
		addFunc(setRelaxedPower);
		
		addFunc(sinh);
		addFunc(cosh);
		addFunc(tanh);
		
		addFunc(sec);
		addFunc(csc);
		addFunc(cot);
		
		addFunc(extSeq);
		addFunc(truncSeq);
		addFunc(subSeq);
		addFunc(revSeq);
		addFunc(sumSeq);
		
		addFunc(arcLen);
		addFunc(repDiff);
		
		addFunc(similar);
		addFunc(fastSimilar);
		addFunc(sortExpr);
		
		addFunc(delete);
		addFunc(help);
		
		addFunc(fSolve);
		
		addFunc(fastEquals);
		
		addFunc(exprSet);
		addFunc(sequence);
		
		for(String s:funcs.keySet()) {
			functionNames.add(s);
		}
		//
		for(Func f:funcs.values()){
			if(f.loader != null) f.loader.load(f);
		}
		
		//
		
		tree.behavior.simplifyChildren = false;
		tree.behavior.rule = new Rule("show the tree of the expression"){
			

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				return var(f.get().toStringTree(0));
			}
		};
		
		size.behavior.simplifyChildren = false;
		size.behavior.rule = new Rule("get size of sub expression"){
			

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				
				return num(f.get().size());
			}
		};
		size.behavior.toFloat = new Func.FloatFunc() {
			
			
			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				return new ComplexFloat(owner.get().size(),0);
			}
		};
		
		get.behavior.simplifyChildren = false;
		get.behavior.rule = new Rule("get sub expression"){
			

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func f = (Func)e;
					int index = ((Num)f.get(1)).getRealValue().intValue();
					return f.get().get( index );
				}
		};
		get.behavior.toFloat = new Func.FloatFunc() {
			
			
			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				int index = ((Num)owner.get(1)).getRealValue().intValue();
				return owner.get().get(index).convertToFloat(varDefs);
			}
		};
		
		choose.behavior.rule = new Rule("choose formula"){
			

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				
				Expr n = f.get(0);
				Expr k = f.get(1);
				
				if(isPositiveRealNum(n) && isPositiveRealNum(k)){
					return num(choose( ((Num)n).getRealValue() , ((Num)k).getRealValue()));
				}
				return e;
			}
		};
		choose.behavior.toFloat = new Func.FloatFunc() {
			
			
			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				double n = owner.get(0).convertToFloat(varDefs).real;
				double k = owner.get(1).convertToFloat(varDefs).real;
				return new ComplexFloat( factorial(n)/(factorial(k)*factorial(n-k)) ,0);
			}
		};
		
		primeFactor.behavior.rule = new Rule("prime factor an integer"){
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				return primeFactor((Num)f.get());
			}
		};
		primeFactor.behavior.toFloat = Func.nothingFunc;
		
		partialFrac.behavior.rule = new Rule("break apart polynomial ratio into a sum of inverse linear terms"){
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				return partialFrac(inner,var,casInfo);
			}
		};
		partialFrac.behavior.toFloat = Func.nothingFunc;
		
		polyDiv.behavior.rule = new Rule("polynomial division") {
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Var v = (Var)f.get(1);
				return polyDiv(f.get(), v, casInfo);
			}
		};
		polyDiv.behavior.toFloat = Func.nothingFunc;
		
		polyCoef.behavior.rule = new Rule("get the coefficients of a polynomial as a list"){
			

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				Expr ans = polyExtract(inner,var,casInfo);
				if(ans == null) return error();
				return ans;
			}
		};
		
		degree.behavior.rule = new Rule("get the degree of a polynomial"){
			

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				Expr ans = num(degree(inner,var));
				if(ans.equals(Num.NEG_ONE)) return error();
				return ans;
			}
		};
		
		leadingCoef.behavior.rule = new Rule("get the leading coefficient of a polynomial"){
			

			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func f = (Func)e;
				Expr inner = f.get(0);
				Var var = (Var)f.get(1);
				Expr ans = getLeadingCoef(inner,var,casInfo);
				if(ans == null) return error();
				return ans;
			}
		};
		
		conv.behavior.simplifyChildren = false;
		conv.behavior.rule = new Rule("unit conversion") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				try {
					return approx(Unit.conv(f.get(0), Unit.getUnit(f.get(1).toString()), Unit.getUnit(f.get(2).toString())),exprSet()).simplify(casInfo);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				return var("error");
			}
		};
		
		latex.behavior.simplifyChildren = false;
		latex.behavior.rule = new Rule("LaTeX language conversion") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				return var(generateLatex(f.get()));
			}
		};
		
		subst.behavior.rule = new Rule("substitution") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				if(f.get(1).typeName().equals("set")) {
					return f.get().replace((Func)f.get(1)).simplify(casInfo);
				}
				return f.get().replace((Func)f.get(1)).simplify(casInfo);
			}
		};
		
		comparison.behavior.rule = new Rule("comparison") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				if(f.get().typeName().equals("equ")) {
					Func castedEqu = (Func)f.get();
					
					return bool(Equ.getLeftSide(castedEqu).exactlyEquals(Equ.getRightSide(castedEqu)));
				}else if(f.get() instanceof Less) {
					Less casted = (Less)f.get();
					
					boolean equal = casted.getLeftSide().exactlyEquals(casted.getRightSide());
					if(casted.containsVars()) return bool(!equal);
					
					return bool(!equal && casted.getLeftSide().convertToFloat(exprSet()).real < casted.getRightSide().convertToFloat(exprSet()).real );
				}else if(f.get() instanceof Greater) {
					Greater casted = (Greater)f.get();
					
					boolean equal = casted.getLeftSide().exactlyEquals(casted.getRightSide());
					if(casted.containsVars()) return bool(!equal);
					
					return bool(!equal && casted.getLeftSide().convertToFloat(exprSet()).real > casted.getRightSide().convertToFloat(exprSet()).real );
				}else if(f.get() instanceof BoolState) {
					return f.get();
				}
				
				return f;
			}
		};
		
		taylor.behavior.rule = new Rule("taylor series"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				Expr expr = f.get(0);
				
				Func equ = (Func)f.get(1);
				Var v = (Var)Equ.getLeftSide(equ);
				Num n = (Num)f.get(2);
				
				
				Func outSum = sum();
				
				for(int i = 0;i<n.getRealValue().intValue();i++) {
					
					outSum.add( div(prod(expr.replace(equ),power(sub(v,Equ.getRightSide(equ)),num(i))),num(factorial(BigInteger.valueOf(i)))));
					
					expr = diff(expr,v).simplify(casInfo);
				}
				
				return outSum.simplify(casInfo);
			}
		};
		
		gui.behavior.rule = new Rule("opens a new window") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				new ui.CalcWindow();
				
				return var("done");
			}
		};
		
		nParams.behavior.rule = new Rule("expected number of paramters") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				return num(getExpectectedParams(f.get().toString()));
			}
		};
		
		isType.behavior.simplifyChildren = false;
		isType.behavior.rule = new Rule("check type") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				return bool(f.get(0).typeName().equals(f.get(1).toString()));
			}
		};
		
		contains.behavior.simplifyChildren = false;
		contains.behavior.rule = new Rule("check if first argument contains the second argument"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func f = (Func)e;
				
				return bool(f.get(0).contains(f.get(1)));
			}
		};
		
		result.behavior.rule = StandardRules.becomeInner;
		
		allowAbs.behavior.rule = new Rule("are we allowing absolute values") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(casInfo.allowAbs());
			}
		};
		
		allowComplexNumbers.behavior.rule = new Rule("are we allowing absolute values") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(casInfo.allowComplexNumbers());
			}
		};
		singleSolutionMode.behavior.rule = new Rule("should solve only return one solution") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(casInfo.singleSolutionMode());
			}
		};
		factorIrrationalRoots.behavior.rule = new Rule("allow irrational roots in factoring") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(casInfo.factorIrrationalRoots());
			}
		};
		
		setAllowAbs.behavior.rule = new Rule("are we allowing absolute values") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				casInfo.setAllowAbs(e.get().equals(BoolState.TRUE));
				return bool(casInfo.allowAbs());
			}
		};
		
		setAllowComplexNumbers.behavior.rule = new Rule("are we allowing absolute values") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				casInfo.setAllowComplexNumbers( e.get().equals(BoolState.TRUE));
				return bool(casInfo.allowComplexNumbers());
			}
		};
		setSingleSolutionMode.behavior.rule = new Rule("set if solve should return one solution") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				casInfo.setSingleSolutionMode( e.get().equals(BoolState.TRUE));
				return bool(casInfo.singleSolutionMode());
			}
		};
		setFactorIrrationalRoots.behavior.rule = new Rule("should solve only return one solution") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				casInfo.setFactorIrrationalRoots( e.get().equals(BoolState.TRUE));
				return bool(casInfo.factorIrrationalRoots());
			}
		};
		relaxedPower.behavior.rule = new Rule("simplify (a^b)^c to a^(b*c) always") {
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(casInfo.relaxedPower());
			}
		};
		setRelaxedPower.behavior.rule = new Rule("simplify (a^b)^c to a^(b*c) always") {
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				casInfo.setRelaxedPower(e.get().equals(BoolState.TRUE));
				return bool(casInfo.relaxedPower());
			}
		};
		
		sinh.behavior.rule = new Rule("sinh function") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return div(sub(exp(prod(num(2),e.get())),num(1)),prod(num(2),exp(e.get()))).simplify(casInfo);
			}
		};
		sinh.behavior.toFloat = new Func.FloatFunc() {
			

			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				return ComplexFloat.mult(ComplexFloat.sub(ComplexFloat.exp(owner.get().convertToFloat(varDefs)),ComplexFloat.exp(ComplexFloat.neg(owner.get().convertToFloat(varDefs)))),new ComplexFloat(0.5,0));
			}
		};
		
		cosh.behavior.rule = new Rule("cosh function") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return div(sum(exp(prod(num(2),e.get())),num(1)),prod(num(2),exp(e.get()))).simplify(casInfo);
			}
		};
		cosh.behavior.toFloat = new Func.FloatFunc() {
			

			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				return ComplexFloat.mult(ComplexFloat.add(ComplexFloat.exp(owner.get().convertToFloat(varDefs)),ComplexFloat.exp(ComplexFloat.neg(owner.get().convertToFloat(varDefs)))),new ComplexFloat(0.5,0));
			}
		};
		
		tanh.behavior.rule = new Rule("tanh function") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return div( sub(exp(prod(num(2),e.get())),num(1)) , sum(exp(prod(num(2),e.get())),num(1)) ).simplify(casInfo);
			}
		};
		tanh.behavior.toFloat = new Func.FloatFunc() {
			

			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				ComplexFloat expSquared = ComplexFloat.exp( ComplexFloat.mult(ComplexFloat.TWO, owner.get().convertToFloat(varDefs) ) );
				return ComplexFloat.div( ComplexFloat.sub(expSquared, ComplexFloat.ONE) ,  ComplexFloat.add(expSquared, ComplexFloat.ONE) );
			}
		};
		
		sec.behavior.rule = new Rule("replace sec with one over cos"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return inv(cos(e.get())).simplify(casInfo);
			}
		};
		sec.behavior.toFloat = new Func.FloatFunc() {
			

			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.cos(owner.get().convertToFloat(varDefs)));
			}
		};
		
		csc.behavior.rule = new Rule("replace csc with one over sin"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return inv(sin(e.get())).simplify(casInfo);
			}
		};
		csc.behavior.toFloat = new Func.FloatFunc() {
			

			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.sin(owner.get().convertToFloat(varDefs)));
			}
		};
		
		cot.behavior.rule = new Rule("replace cot with one over tan"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return inv(tan(e.get())).simplify(casInfo);
			}
		};
		cot.behavior.toFloat = new Func.FloatFunc() {
			

			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				return ComplexFloat.div(ComplexFloat.ONE, ComplexFloat.tan(owner.get().convertToFloat(varDefs)));
			}
		};
		
		
		extSeq.behavior.rule = new Rule("extend the sequence"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				
				int needed = ((Num)e.get(1)).getRealValue().intValue()-e.get().size();
				
				Expr extended = next( (Func)e.get() , num(needed) ).simplify(casInfo);
				
				if(extended instanceof Next) {
					for(int i = 0;i<needed;i++) {
						e.get().add( e.get(0).get(i%e.get(0).size()) );
					}
				}else {
					for(int i = 0;i<needed;i++) {
						e.get().add(extended.get(i));
					}
				}
				
				return e.get();
			}
		};
		truncSeq.behavior.rule = new Rule("truncate the sequence") {
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				int newSize = ((Num)e.get(1)).getRealValue().intValue();
				
				Func truncatedSequence = sequence();
				for(int i = 0;i<newSize;i++) {
					truncatedSequence.add(e.get(0).get(i));
				}
				return truncatedSequence;
			}
		};
		subSeq.behavior.rule = new Rule("get the sub sequence from start to end, end is non inclusive") {
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				int start = ((Num)e.get(1)).getRealValue().intValue();
				int end = ((Num)e.get(2)).getRealValue().intValue();
				
				Func truncatedSequence = sequence();
				for(int i = start;i<end;i++) {
					truncatedSequence.add(e.get(0).get(i));
				}
				return truncatedSequence;
			}
		};
		revSeq.behavior.rule = new Rule("revserse the sequence") {
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func oldSequence = (Func)e.get(0);
				Func newSequence = sequence();
				
				for(int i = oldSequence.size()-1;i>=0;i--) {
					newSequence.add(oldSequence.get(i));
				}
				
				return newSequence;
			}
		};
		sumSeq.behavior.rule = new Rule("add elements of sequence"){
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func sequence = (Func)e.get();
				Func sum = sum();
				for(int i = 0;i<sequence.size();i++) {
					sum.add(sequence.get(i));
				}
				return sum.simplify(casInfo);
			}
		};
		
		
		
		arcLen.behavior.rule = new Rule("arc-length of a function"){
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Expr min = e.get(0),max = e.get(1);
				Expr expr = e.get(2);
				Var v = (Var) e.get(3);
				
				return integrateOver(min,max,sqrt(sum(num(1),power(diff(expr,v),num(2)))),v).simplify(casInfo);
			}
		};
		arcLen.behavior.toFloat = new Func.FloatFunc() {
			

			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				Expr min = owner.get(0),max = owner.get(1);
				Expr expr = owner.get(2);
				Var v = (Var) owner.get(3);
				
				return integrateOver(min,max,sqrt(sum(num(1),power(diff(expr,v),num(2)))),v).convertToFloat(varDefs);
			}
		};
		
		repDiff.behavior.rule = new Rule("repeated derivative"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				int amount = ((Num)e.get(2)).getRealValue().intValue();
				
				Expr expr = e.get(0);
				Var v = (Var)e.get(1);
				
				for(int i = 0;i<amount;i++) {
					expr = distr(diff(expr,v)).simplify(casInfo);
				}
				
				return expr;
			}
		};
		repDiff.behavior.toFloat = new Func.FloatFunc() {
			
			
			//this is not based on any mathematical rigor, but it seems to somewhat work
			//time complexity of 2^n so keep n small
			ComplexFloat calcDerivRec(Expr expr,Func varDefs,ComplexFloat var,int n,ComplexFloat delta) {
				if(n > 8) return new ComplexFloat(0,0);//does not work well beyond this point
				if(n == 0) return expr.convertToFloat(varDefs);
				
				ComplexFloat deltaOver2 = new ComplexFloat(delta.real/2,0);
				
				if(n==1) {
					
					var.set( ComplexFloat.sub(var, deltaOver2) );//subtract delta/2
					ComplexFloat y0 = expr.convertToFloat(varDefs);
					var.set( ComplexFloat.add(var, delta) );//add delta
					ComplexFloat y1 = expr.convertToFloat(varDefs);
					
					ComplexFloat slope = ComplexFloat.div((ComplexFloat.sub(y1, y0)),delta);
					return slope;
				}
				
				ComplexFloat originalVal = new ComplexFloat(var);
				ComplexFloat newDelta = new ComplexFloat(delta.real/2.0,0);//increase precision on lower order derivatives
				
				var.set( ComplexFloat.sub(var, deltaOver2) );
				ComplexFloat y0Der = calcDerivRec(expr,varDefs,var,n-1,newDelta);
				var.set( ComplexFloat.add(originalVal, deltaOver2 ) );//add delta/2 to the original x
				ComplexFloat y1Der = calcDerivRec(expr,varDefs,var,n-1,newDelta);
				
				ComplexFloat slope = ComplexFloat.div((ComplexFloat.sub(y1Der, y0Der)),delta);
				return slope;
				
			}

			@Override
			public ComplexFloat convertToFloat(Func varDefs, Func owner) {
				int amount = ((Num)owner.get(2)).getRealValue().intValue();
				Var var = (Var)owner.get(1);
				ComplexFloat equRightSideVal = null;
				Expr expr = owner.get(0);
				
				for(int i = 0;i < varDefs.size();i++) {//search for definition
					Func equ = (Func)varDefs.get(i);
					Var v = (Var)Equ.getLeftSide(equ);
					if(v.equals(var)) {
						equRightSideVal = ((FloatExpr)Equ.getRightSide(equ)).value;//found!
						break;
					}
				}
				
				if(equRightSideVal == null) return new ComplexFloat(0,0);
				
				ComplexFloat delta = new ComplexFloat( Math.pow( Math.abs(equRightSideVal.real)/Short.MAX_VALUE,1.0/amount ) ,0);//set original precision
				return calcDerivRec(expr,varDefs,equRightSideVal,amount,delta);
			}
		};
		
		similar.behavior.simplifyChildren = false;
		similar.behavior.rule = new Rule("expressions are similar"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(Rule.strictSimilarExpr(e.get(0), e.get(1)));
			}
		};
		fastSimilar.behavior.simplifyChildren = false;
		fastSimilar.behavior.rule = new Rule("expressions are similar computed quickly"){
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				return bool(Rule.fastSimilarExpr(e.get(0), e.get(1)));
			}
		};
		
		sortExpr.behavior.simplifyChildren = false;
		sortExpr.behavior.rule = new Rule("sort expression into cononical arangement") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				e.get().sort();
				System.out.println(e.get().flags.sorted);
				return e.get();
			}
		};
		
		delete.behavior.simplifyChildren = false;
		delete.behavior.rule = new Rule("delete variable or function") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Expr inner = e.get();
				
				if(inner instanceof Var)casInfo.definitions.removeVar(inner.toString());
				else if(inner instanceof Func) casInfo.definitions.removeFunc(inner.typeName());
				return Var.SUCCESS;
			}
		};
		
		help.behavior.simplifyChildren = false;
		help.behavior.rule = new Rule("help function") {
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				
				return var(e.get().help());
			}
		};
		
		fSolve.behavior.rule = new Rule("floating point solver") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				Func polySequence = polyExtract(e.get(0), (Var)e.get(1) ,casInfo);
				Func solutionsSet = exprSet();
				
				if(polySequence!=null) {
					ArrayList<Double> solutionsArrayList = Solve.polySolve(polySequence);
					for(double solution:solutionsArrayList) {
						solutionsSet.add(floatExpr(solution));
					}
				}
				
				return solutionsSet;
			}
		};
		
		fastEquals.behavior.simplifyChildren = false;
		fastEquals.behavior.rule = new Rule("faste comparison") {
			
			
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
				
				return bool(e.get(0).equals(e.get(1)));
			}
		};
		
		
		
		for(String funcName :funcs.keySet()) {
			Rule mainRule = funcs.get(funcName).getRule();
			Rule doneRule = funcs.get(funcName).getDoneRule();
			if(mainRule != null) mainRule.init();
			if(doneRule != null) doneRule.init();
		}
		
	}
	
	private static ArrayList<String> functionNames = new ArrayList<String>();
	static HashMap<String,Integer> numberOfParams = new HashMap<String,Integer>();
	
	public static void addFunc(Func f){
		funcs.put(f.behavior.name,f);
		
	}
	
	static{
		
	}
	
	static {
		
		numberOfParams.put("sqrt", 1);
		numberOfParams.put("cbrt", 1);
		numberOfParams.put("inv", 1);
		numberOfParams.put("exp", 1);
		numberOfParams.put("inv", 1);
		numberOfParams.put("neg", 1);
		
		numberOfParams.put("sinh", 1);
		numberOfParams.put("cosh", 1);
		numberOfParams.put("tanh", 1);
		
		numberOfParams.put("range", 4);
		numberOfParams.put("approx", 2);
		
		numberOfParams.put("limit", 2);
		numberOfParams.put("mat", 1);
		numberOfParams.put("transpose", 1);
		numberOfParams.put("next", 2);
		
		numberOfParams.put("boolTableToExpr", 2);
		
		for(String s:numberOfParams.keySet()) {
			functionNames.add(s);
		}
	}
	
	public static boolean isFunc(String name) {
		return functionNames.contains(name);
	}
	
	public static int getExpectectedParams(String funcName) {
		Func func = funcs.get(funcName);
		if(func != null) {
			return func.behavior.numOfParams;
		}
		
		Integer num = numberOfParams.get(funcName);
		if(num != null) {
			return num;
		}
		
		return 0;
	}
	
	public static Expr getFuncByName(String funcName,Expr... params) throws Exception {
		
		if(funcName.equals("approx")) {
			if(params.length == 1) {
				return approx(params[0],exprSet());
			}else if(params.length == 2) {
				return approx(params[0],(Func)params[1]);
			}else {
				throw new Exception("function: "+funcName+", requires: 1 or 2 parameters");
			}
		}
		
		Func func = funcs.get(funcName);
		if(func != null) {
			func = (Func)func.copy();
			for(Expr param:params) func.add(param);
			if(func.behavior.numOfParams != -1 && func.size() != func.behavior.numOfParams){
				throw new Exception("function: "+funcName+", requires: "+func.behavior.numOfParams+", parameter(s)");
			}
			return func;
		}
		
		if(funcName.equals("sqrt")) return sqrt(params[0]);
		if(funcName.equals("cbrt")) return cbrt(params[0]);
		if(funcName.equals("inv")) return inv(params[0]);
		if(funcName.equals("exp")) return exp(params[0]);
		if(funcName.equals("inv")) return inv(params[0]);
		if(funcName.equals("neg")) return neg(params[0]);
		if(funcName.equals("mat")) return mat((Func)params[0]);
		if(funcName.equals("transpose")) return transpose(params[0]);
		if(funcName.equals("next")) return next((Func)params[0],(Num)params[1]);
		
		if(funcName.equals("gcd")) return gcd(params);
		
		if(funcName.equals("solve") && params[0].typeName().equals("set")) return solve((Func)params[0],(Func)params[1]);
		if(funcName.equals("solve") && params[0] instanceof Greater) return solve((Greater)params[0],(Var)params[1]);
		if(funcName.equals("solve") && params[0] instanceof Less) return solve((Less)params[0],(Var)params[1]);
	
		if(funcName.equals("limit")) return limit(params[0],(Func)params[1]);
		if(funcName.equals("range")) return range(params[0],params[1],params[2],(Var)params[3]);
		
		if(funcName.equals("boolTableToExpr")) return boolTableToExpr((Func)params[0],(Func)params[1]);
		
		if(!FUNCTION_UNLOCKED) throw new Exception("no function by the name: "+funcName);//allow making new functions on the fly
		
		
		Func blankFunc = new Func(funcName);
		for(Expr param:params) blankFunc.add(param);
		blankFunc.behavior.numOfParams = params.length;
		
		return blankFunc;
	}
}
