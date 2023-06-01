package cas.base;

import java.util.ArrayList;
import java.util.HashMap;

import cas.Cas;
import cas.SimpleFuncs;
import cas.Trig;
import cas.algebra.Distr;
import cas.algebra.Factor;
import cas.algebra.Gcd;
import cas.algebra.Solve;
import cas.bool.And;
import cas.bool.BoolCompress;
import cas.bool.BoolTableToExpr;
import cas.bool.Not;
import cas.bool.Or;
import cas.calculus.Diff;
import cas.calculus.Integrate;
import cas.calculus.IntegrateOver;
import cas.calculus.Limit;
import cas.calculus.Range;
import cas.matrix.Dot;
import cas.matrix.Mat;
import cas.matrix.Transpose;
import cas.primitive.Abs;
import cas.primitive.Approx;
import cas.primitive.Becomes;
import cas.primitive.Div;
import cas.primitive.Equ;
import cas.primitive.ExprSet;
import cas.primitive.Greater;
import cas.primitive.Less;
import cas.primitive.Ln;
import cas.primitive.Power;
import cas.primitive.Prod;
import cas.primitive.Sequence;
import cas.primitive.Sum;
import cas.programming.Define;
import cas.programming.Ternary;
import cas.special.Gamma;
import cas.special.LambertW;
import cas.special.Next;

public class FunctionsLoader {//this class simply loads all the functions into memory
	//CONSTANTS
	public static final int N_PARAMETERS = -1;//arbitrary number of parameters
	//
	
	
	public static HashMap<String,Func> funcs = new HashMap<String,Func>();
	public static ArrayList<String> functionNames = new ArrayList<String>();
	public static HashMap<String,Integer> numberOfParams = new HashMap<String,Integer>();
	
	public static boolean FUNCTION_UNLOCKED = false;//ability to create new functions on the fly, must be turned off during loading of CAS to prevent spelling errors
	
	public static boolean isFunc(String name) {
		return functionNames.contains(name);
	}
	
	public static int getExpectedParams(String name) {
		return numberOfParams.get(name);
	}
	
	public static void addFunc(Func f){
		funcs.put(f.behavior.name,f);
	}
	
public static Expr getFuncByName(String funcName,Expr... params) throws Exception {
		
		Func func = FunctionsLoader.funcs.get(funcName);
		if(func != null) {
			func = (Func)func.copy();
			for(Expr param:params) func.add(param);
			if(func.behavior.numOfParams != FunctionsLoader.N_PARAMETERS && func.size() != func.behavior.numOfParams){
				throw new Exception("function: "+funcName+", requires: "+func.behavior.numOfParams+", parameter(s)");
			}
			return func;
		}
		
		if(funcName.equals("sqrt")) return Cas.sqrt(params[0]);
		if(funcName.equals("cbrt")) return Cas.cbrt(params[0]);
		if(funcName.equals("inv")) return Cas.inv(params[0]);
		if(funcName.equals("exp")) return Cas.exp(params[0]);
		if(funcName.equals("inv")) return Cas.inv(params[0]);
		if(funcName.equals("neg")) return Cas.neg(params[0]);
		
		if(!FUNCTION_UNLOCKED) throw new Exception("no function by the name: "+funcName);//allow making new functions on the fly
		
		
		Func blankFunc = new Func(funcName);
		for(Expr param:params) blankFunc.add(param);
		blankFunc.behavior.numOfParams = params.length;
		
		return blankFunc;
	}
	
	public static boolean functionsLoaded;
	//---------------------LOADERS FOR THESE ARE IN SEPERATE FILES----------------
	public static Func power,prod,sum,div;
	public static Func lambertW,ln,abs;
	public static Func sin,cos,tan,asin,acos,atan;
	public static Func diff,integrate,integrateOver,limit,range;
	public static Func and,or,not,boolCompress,boolTableToExpr;
	public static Func solve,distr,factor,gcd;
	public static Func becomes,equ;
	public static Func exprSet,sequence;
	public static Func approx;
	public static Func gamma;
	public static Func expand;
	public static Func ternary;
	public static Func define;
	public static Func dot,mat,transpose;
	public static Func next;
	public static Func greater,less;
	
	public static void specializedLoaders() {
		power = new Func("power",2,Power.loader);
		addFunc(power);
		sin = new Func("sin",1,Trig.sinLoader);
		addFunc(sin);
		cos = new Func("cos",1,Trig.cosLoader);
		addFunc(cos);
		tan = new Func("tan",1,Trig.tanLoader);
		addFunc(tan);
		asin = new Func("asin",1,Trig.asinLoader);
		addFunc(asin);
		acos = new Func("acos",1,Trig.acosLoader);
		addFunc(acos);
		atan = new Func("atan",1,Trig.atanLoader);
		addFunc(atan);
		and = new Func("and",N_PARAMETERS,And.andLoader);
		addFunc(and);
		or = new Func("or",N_PARAMETERS,Or.orLoader);
		addFunc(or);
		not = new Func("not",1,Not.notLoader);
		addFunc(not);
		boolCompress = new Func("boolCompress",1,BoolCompress.boolCompressLoader);
		addFunc(boolCompress);
		boolTableToExpr = new Func("boolTableToExpr",2,BoolTableToExpr.boolTableToExprLoader);
		addFunc(boolTableToExpr);
		approx = new Func("approx",2,Approx.approxLoader);
		addFunc(approx);
		becomes = new Func("becomes",2,Becomes.becomesLoader);
		addFunc(becomes);
		equ = new Func("equ",2,Equ.equLoader);
		addFunc(equ);
		exprSet = new Func("set",N_PARAMETERS,ExprSet.exprSetLoader);
		addFunc(exprSet);
		sequence = new Func("sequence",N_PARAMETERS,Sequence.sequenceLoader);
		addFunc(sequence);
		gcd = new Func("gcd",N_PARAMETERS,Gcd.gcdLoader);
		addFunc(gcd);
		sum = new Func("sum",N_PARAMETERS,Sum.sumLoader);
		addFunc(sum);
		div = new Func("div",2,Div.divLoader);
		addFunc(div);
		diff = new Func("diff",2,Diff.diffLoader);
		addFunc(diff);
		limit = new Func("limit",2,Limit.limitLoader);
		addFunc(limit);
		range = new Func("range",4,Range.rangeLoader);
		addFunc(range);
		ln = new Func("ln",1,Ln.lnLoader);
		addFunc(ln);
		integrate = new Func("integrate",2,Integrate.integrateLoader);
		addFunc(integrate);
		lambertW = new Func("lambertW",1,LambertW.lambertwLoader);
		addFunc(lambertW);
		distr = new Func("distr",1,Distr.distrLoader);
		addFunc(distr);
		factor = new Func("factor",1,Factor.factorLoader);
		addFunc(factor);
		solve = new Func("solve",2,Solve.solveLoader);
		addFunc(solve);
		integrateOver = new Func("integrateOver",4,IntegrateOver.integrateOverLoader);
		addFunc(integrateOver);
		gamma = new Func("gamma",1,Gamma.gammaLoader);
		addFunc(gamma);
		abs = new Func("abs",1,Abs.absLoader);
		addFunc(abs);
		expand = new Func("expand",1,Distr.expandLoader);
		addFunc(expand);
		prod = new Func("prod",N_PARAMETERS,Prod.prodLoader);
		addFunc(prod);
		ternary = new Func("ternary",3,Ternary.ternaryLoader);
		addFunc(ternary);
		define = new Func("define",2,Define.defineLoader);
		addFunc(define);
		dot = new Func("dot",N_PARAMETERS,Dot.dotLoader);
		addFunc(dot);
		mat = new Func("mat",N_PARAMETERS,Mat.matLoader);
		addFunc(mat);
		transpose = new Func("transpose",1,Transpose.transposeLoader);
		addFunc(transpose);
		next = new Func("next",2,Next.nextLoader);
		addFunc(next);
		greater = new Func("greater",2,Greater.greaterLoader);
		addFunc(greater);
		less = new Func("less",2,Less.lessLoader);
		addFunc(less);
	}
	//---------------------LOADERS FOR THESE ARE IN SEPERATE FILES----------------
	
	
	
	
	//---------------------LOADERS FOR THESE ARE IN SimpleFuncs.java----------------
	public static Func tree;
	public static Func size,get;
	public static Func choose;
	public static Func primeFactor;
	
	public static Func partialFrac;
	public static Func polyDiv;
	
	public static Func polyCoef;
	public static Func degree;
	public static Func leadingCoef;
	public static Func conv;
	public static Func latex;
	public static Func subst;
	public static Func comparison;
	public static Func taylor;
	public static Func gui;
	public static Func nParams;
	public static Func isType;
	public static Func contains;
	public static Func result;
	public static Func allowAbs;
	public static Func allowComplexNumbers;
	public static Func singleSolutionMode;
	public static Func factorIrrationalRoots;
	public static Func relaxedPower;
	public static Func setAllowAbs;
	public static Func setAllowComplexNumbers;
	public static Func setSingleSolutionMode;
	public static Func setFactorIrrationalRoots;
	public static Func setRelaxedPower;
	public static Func sinh,cosh,tanh,sec,csc,cot;
	public static Func extSeq,truncSeq,subSeq,revSeq;
	public static Func sumSeq;
	public static Func arcLen;
	public static Func repDiff;
	public static Func similar,fastSimilar;
	public static Func sortExpr;
	public static Func deleteVar,deleteFunc;
	public static Func help;
	public static Func fSolve;
	public static Func fastEquals;
	
	public static void imbededLoaders() {
		tree = new Func("tree",1,SimpleFuncs.treeLoader);
		addFunc(tree);
		size = new Func("size",1,SimpleFuncs.sizeLoader);
		addFunc(size);
		get = new Func("get",2,SimpleFuncs.getLoader);
		addFunc(get);
		choose = new Func("choose",2,SimpleFuncs.chooseLoader);
		addFunc(choose);
		primeFactor = new Func("primeFactor",1,SimpleFuncs.primeFactorLoader);
		addFunc(primeFactor);
		partialFrac = new Func("partialFrac",2,SimpleFuncs.partialFracLoader);
		addFunc(partialFrac);
		polyDiv = new Func("polyDiv",2,SimpleFuncs.polyDivLoader);
		addFunc(polyDiv);
		polyCoef = new Func("polyCoef",2,SimpleFuncs.polyCoefLoader);
		addFunc(polyCoef);
		degree = new Func("degree",2,SimpleFuncs.degreeLoader);
		addFunc(degree);
		leadingCoef = new Func("leadingCoef",2,SimpleFuncs.leadingCoefLoader);
		addFunc(leadingCoef);
		conv = new Func("conv",3,SimpleFuncs.convLoader);
		addFunc(conv);
		latex = new Func("latex",1,SimpleFuncs.latexLoader);
		addFunc(latex);
		subst = new Func("subst",2,SimpleFuncs.substLoader);
		addFunc(subst);
		comparison = new Func("comparison",1,SimpleFuncs.comparisonLoader);
		addFunc(comparison);
		taylor = new Func("taylor",3,SimpleFuncs.taylorLoader);
		addFunc(taylor);
		gui = new Func("gui",0,SimpleFuncs.guiLoader);
		addFunc(gui);
		nParams = new Func("nParams",1,SimpleFuncs.nParamsLoader);
		addFunc(nParams);
		isType = new Func("isType",2,SimpleFuncs.isTypeLoader);
		addFunc(isType);
		contains = new Func("contains",2,SimpleFuncs.containsLoader);
		addFunc(contains);
		result = new Func("result",1,SimpleFuncs.resultLoader);
		addFunc(result);
		allowAbs = new Func("allowAbs",0,SimpleFuncs.allowAbsLoader);
		addFunc(allowAbs);
		allowComplexNumbers = new Func("allowComplexNumbers",0,SimpleFuncs.allowComplexNumbersLoader);
		addFunc(allowComplexNumbers);
		singleSolutionMode = new Func("singleSolutionMode",0,SimpleFuncs.singleSolutionModeLoader);
		addFunc(singleSolutionMode);
		factorIrrationalRoots = new Func("factorIrrationalRoots",0,SimpleFuncs.factorIrrationalRootsLoader);
		addFunc(factorIrrationalRoots);
		setAllowAbs = new Func("setAllowAbs",1,SimpleFuncs.setAllowAbsLoader);
		addFunc(setAllowAbs);
		setAllowComplexNumbers = new Func("setAllowComplexNumbers",1,SimpleFuncs.setAllowComplexNumbersLoader);
		addFunc(setAllowComplexNumbers);
		setSingleSolutionMode = new Func("setSingleSolutionMode",1,SimpleFuncs.setSingleSolutionModeLoader);
		addFunc(setSingleSolutionMode);
		setFactorIrrationalRoots = new Func("setFactorIrrationalRoots",1,SimpleFuncs.setFactorIrrationalRootsLoader);
		addFunc(setFactorIrrationalRoots);
		relaxedPower = new Func("relaxedPower",0,SimpleFuncs.relaxedPowerLoader);
		addFunc(relaxedPower);
		setRelaxedPower = new Func("setRelaxedPower",1,SimpleFuncs.setRelaxedPowerLoader);
		addFunc(setRelaxedPower);
		sinh = new Func("sinh",1,SimpleFuncs.sinhLoader);
		addFunc(sinh);
		cosh = new Func("cosh",1,SimpleFuncs.coshLoader);
		addFunc(cosh);
		tanh = new Func("tanh",1,SimpleFuncs.tanhLoader);
		addFunc(tanh);
		sec = new Func("sec",1,SimpleFuncs.secLoader);
		addFunc(sec);
		csc = new Func("csc",1,SimpleFuncs.cscLoader);
		addFunc(csc);
		cot = new Func("cot",1,SimpleFuncs.cotLoader);
		addFunc(cot);
		extSeq = new Func("extSeq",2,SimpleFuncs.extSeqLoader);
		addFunc(extSeq);
		truncSeq = new Func("truncSeq",2,SimpleFuncs.truncSeqLoader);
		addFunc(truncSeq);
		subSeq = new Func("subSeq",3,SimpleFuncs.subSeqLoader);
		addFunc(subSeq);
		revSeq = new Func("revSeq",1,SimpleFuncs.revSeqLoader);
		addFunc(revSeq);
		sumSeq = new Func("sumSeq",1,SimpleFuncs.sumSeqLoader);
		addFunc(sumSeq);
		arcLen = new Func("arcLen",4,SimpleFuncs.arcLenLoader);
		addFunc(arcLen);
		repDiff = new Func("repDiff",3,SimpleFuncs.repDiffLoader);
		addFunc(repDiff);
		similar = new Func("similar",2,SimpleFuncs.similarLoader);
		addFunc(similar);
		fastSimilar = new Func("fastSimilar",2,SimpleFuncs.fastSimilarLoader);
		addFunc(fastSimilar);
		sortExpr = new Func("sortExpr",1,SimpleFuncs.sortExprLoader);
		addFunc(sortExpr);
		deleteVar = new Func("deleteVar",1,SimpleFuncs.deleteVarLoader);
		addFunc(deleteVar);
		deleteFunc = new Func("deleteFunc",1,SimpleFuncs.deleteFuncLoader);
		addFunc(deleteFunc);
		help = new Func("help",1,SimpleFuncs.helpLoader);
		addFunc(help);
		fSolve = new Func("fSolve",2,SimpleFuncs.fSolveLoader);
		addFunc(fSolve);
		fastEquals = new Func("fastEquals",2,SimpleFuncs.fastEqualsLoader);
		addFunc(fastEquals);
	}
	//---------------------LOADERS FOR THESE ARE IN SimpleFuncs.java----------------
	
	private static void initFuncs() {
		System.out.println("- Running Loaders...");
		//call loaders
		for(Func f:funcs.values()) {
			if(f.loader != null) f.loader.load(f);
		}
		System.out.println("- Done running Loaders!");
		
		System.out.println("- Initializing rules...");
		//initialize rules
		for(String funcName :FunctionsLoader.funcs.keySet()) {
			Rule mainRule = FunctionsLoader.funcs.get(funcName).getRule();
			Rule doneRule = FunctionsLoader.funcs.get(funcName).getDoneRule();
			if(mainRule != null) mainRule.init();
			if(doneRule != null) doneRule.init();
		}
		System.out.println("- Done initializing rules!");
	}
	
	private static void loadFunctionParameterCounts() {
		numberOfParams.put("sqrt", 1);
		numberOfParams.put("cbrt", 1);
		numberOfParams.put("inv", 1);
		numberOfParams.put("exp", 1);
		numberOfParams.put("inv", 1);
		numberOfParams.put("neg", 1);
		
		for(Func f:funcs.values()) {
			numberOfParams.put(f.typeName(), f.behavior.numOfParams);
		}
	}
	
	private static void loadFunctionNames() {
		for(String s:numberOfParams.keySet()) {
			FunctionsLoader.functionNames.add(s);
		}
	}
	
	public static void load() {
		if(functionsLoaded) return;
		System.out.println("- Loading functions into memory...");
		
		specializedLoaders();
		imbededLoaders();
		
		loadFunctionParameterCounts();
		
		loadFunctionNames();
		
		initFuncs();
		
		functionsLoaded = true;
		System.out.println("- Done loading functions into memory!");
	}
}
