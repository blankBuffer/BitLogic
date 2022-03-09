package cas;

import java.util.ArrayList;
/*
 * rules are used to describe any expression transformation
 * it can either be hard coded or use a pattern with the option of a condition
 * a rule with a pattern is described in a string "before_state->after_state" the arrow -> signifies the transformation
 * a condition can check weather some variables are in a range and you can have it in a boolean algebra statement
 * 
 * a typical rule used in this CAS can be written as follows 
 * 
 * Rule myRule = new Rule("x/x->1","~eval(x=0)","x divided by x",Rule.EASY);
 * 
 * the eval checks if a given statement is true or not eval(2=3) gives false but eval(2=2) gives true
 * any eval with a variable returns true
 * 
 * the main reason for specifying the difficulty is for filtering out stupid simple rules from a simplify dump. 
 * It's an arbitrary decision and you may not agree with my difficulty scoring but its not really important
 */
import java.util.Random;

import cas.bool.*;
import cas.calculus.*;
import cas.lang.Ask;
import cas.matrix.*;
import cas.primitive.*;
import cas.programming.*;
import cas.special.*;
import cas.trig.*;

public class Rule extends Expr{
	private static final long serialVersionUID = 5928512201641990677L;
	
	public static final int VERY_EASY = 0,EASY = 1,UNCOMMON = 2,TRICKY = 3,CHALLENGING = 4,DIFFICULT = 5,VERY_DIFFICULT = 6;//this is opinion based
	static final boolean VERBOSE_DEFAULT = false;
	static final int VERBOSE_DIFFICULTY = VERY_EASY;
	
	String name = null;
	public boolean verbose = VERBOSE_DEFAULT;
	int difficulty = 0;
	
	/*
	 * the similarStruct function checks if the other expression has the same layout as the template
	 * 
	 * there are two wrapped functions for this function namely fastSimilarStruct and strictSimilarStruct
	 * the main reason for having separate functions is because its not always necessary to check for variable matching pairs
	 * 
	 * similar is defined as having a same tree but the variables might be different or other has expressions where the variables would be
	 * 
	 * this is best understood by example
	 * 
	 * template=a^b other=2^3 , fastSimilar = true , strictSimilar = false
	 * template=a^a other=2^3 , fastSimilar = true , strictSimilar = false (because 2 not equal to 3 and the template has the same symbol for both so it fails the matching pairs test)
	 * 
	 * this=sin(x)^b other = a^b , not similar at all, why? because the template was expecting the base to be the sine function
	 * 
	 * note that with sums and products the length of sub expressions must be equal. It does its best to sort the expressions in the sum/product in a similar kind of way, but its not perfect
	 * 
	 */
	static boolean similarStruct(Expr template,Expr other,boolean checked){//returns if another expression is similar to this but any comparison with variables is returned true
		if(other == null) return false;
		if(template instanceof Var && ((Var) template).generic) return true;
		
		if(template.typeName().equals(other.typeName())) {
			
			if(template instanceof Num || template instanceof BoolState || template instanceof FloatExpr || template instanceof Var) return template.equals(other);
			
			if(template.size() != other.size()) return false;
			if(!checked) other.sort();
			
			if(!checked) return Rule.checkForMatches(template,other);//check for matches works like similarStuct at a fundamental level so no need to do anything more
			
			if(template instanceof Equ && ((Equ)template).type != ((Equ)other).type ) return false;
			
			for(int i = 0;i<template.size();i++) {
				if(!fastSimilarStruct(template.get(i),other.get(i))) return false;
			}
			
			return true;
		}
		return false;
	}
	
	/*
	 * letting the template expression=this we check if the other expression is similar to the template
	 * we DONT check for matches which is why its called fast
	 */
	public static boolean fastSimilarStruct(Expr template,Expr other) {
		return similarStruct(template,other,true);
	}
	/*
	 * a full similar comparisons we DO check for matches making it a 'near' guarantee
	 * note that with sums and products the length must be equal
	 */
	public static boolean strictSimilarStruct(Expr template,Expr other) {
		return similarStruct(template,other,false);
	}
	
	/*
	 * this takes a list of equations as an input and you can ask for a particular value from the set
	 * given [x=3,y=5,z=m] we can give the name "x" and it returns 3, if we ask for "z" it returns m
	 * this is useful for extracting out variables from a template and putting them into objects manually
	 */
	public static Expr getExprByName(ExprList equs,String name){
		for (int i = 0;i<equs.size();i++){
			Equ currentEqu = (Equ) equs.get(i);
			if(currentEqu.getLeftSide().toString().equals(name)){
				return currentEqu.getRightSide();
			}
		}
		return null;
	}
	/*
	 * you give it a template expression and the expression which is similar and it returns a list of equations for the values of each variable
	 * getEqusFromTemplate(a^b,2^3) returns [a=2,b=3] returns null if it can't be done
	 */
	static public ExprList getEqusFromTemplate(Expr template,Expr expr) {
		if(fastSimilarStruct(template,expr)) {//we use fast similar struct here because we don't want to call the getParts and later we check for matches anyway
			Sequence exampleParts = sequence();
			Sequence parts = sequence();
			boolean match = checkForMatches(exampleParts,parts,template,expr);//this effectively makes it a strictSimilarStruct anyway
			if(!match) {
				return null;
			}
			ExprList equs = new ExprList();
			for(int i = 0;i<parts.size();i++) equs.add(equ(exampleParts.get(i),parts.get(i)));
			
			return equs;
			
		}
		return null;
	}
	
	static boolean checkForMatches(Expr template,Expr expr) {//shortcut
		Sequence vars = sequence();
		Sequence exprs = sequence();
		
		return checkForMatches(vars,exprs,template,expr);
	}
	
	/*
	 * extracts the parts and variables from the template=this, and expr is the parts to be extracted 
	 * example, let template=diff(x^a,x) and expr=diff(m^(k*l),m)
	 * these expression break down into lists based on the format of the template tree
	 * template -> {x,a,x} the list is loaded into templateVarsOut
	 * expr -> {m,k*l,m} the list is loaded into exprsPartsOut
	 * 
	 * if this lists for some reason are not the same length it returns false saying that they can't be compared
	 * returning true is saying no problems were encountered
	 * 
	 * this is useful because we can later compare the list for matching sets,
	 * for example in this example the first and last element must be the same [ ->x ,a, ->x ],
	 * this is what the check for matches function does
	 * 
	 */
	static boolean getParts(Sequence templateVarsOut,Sequence exprsPartsOut,Expr template,Expr expr) {//template and this is input,vars and exprs are outputs
		if(template instanceof Var && ((Var)template).generic) {//template encountered a variable, time for extraction into the expr list
			templateVarsOut.add(template.copy());
			exprsPartsOut.add(expr.copy());
		}else{
			if(template.size()==expr.size() && template.typeName().equals(expr.typeName())) {
				for(int i = 0;i<template.size();i++) {
					if(!getParts(templateVarsOut,exprsPartsOut,template.get(i),expr.get(i))) return false;
				}
			}else {
				return false;//different size so variables will obviously not match
			}
		}
		return true;
	}
	/*
	 * given a template and the expr it checks if the sets match for the parts list and if they are similar, it also loads the parts into
	 * the templateVars and exprsParts list since they will likely be used later on to construct an equation set
	 */
	static boolean checkForMatches(Sequence templateVars,Sequence exprsParts,Expr template,Expr expr) {
		
		//part one, create index pairs
		
		ExprList usedVars = new ExprList();
		ArrayList<IndexSet> indexSets = new ArrayList<IndexSet>();
		
		template.sort();
		expr.sort();
		
		if(!getParts(templateVars,exprsParts,template,expr)) return false;//its possible for expressions to be un-strictly similar
		
		for(int i = 0;i<templateVars.size();i++) {
			
			if(usedVars.contains(templateVars.get(i))) continue;
			
			IndexSet set = new IndexSet();
			set.ints.add(i);
			
			for(int j = i+1;j<templateVars.size();j++) {
				if(templateVars.get(i).equals(templateVars.get(j))) {
					set.ints.add(j);
					
				}
			}
			
			if(set.ints.size() != 1) indexSets.add(set);
			
			usedVars.add(templateVars.get(i));
		}
		
		
		///parts two, check expr parts have the index set pairs
		
		for(IndexSet set:indexSets) {
			Expr e = exprsParts.get(set.ints.get(0));
			for(int i = 1;i<set.ints.size();i++) {
				if(set.ints.get(i) > exprsParts.size()) return false;
				
				Expr e2 =  exprsParts.get(set.ints.get(i));
				
				if(!e2.equals(e)) return false;
			}
			
		}
		
		
		return true;
	}
	
	String patternStr = null;
	String conditionStr = null;
	
	public Rule(String pattern,String name,int difficulty){
		patternStr = pattern;
		this.name = name;
		this.difficulty = difficulty;
	}
	public Rule(String pattern,String condition,String name,int difficulty){
		patternStr = pattern;
		conditionStr = condition;
		this.name = name;
		this.difficulty = difficulty;
	}
	public Rule(Becomes pattern,String name,int difficulty){
		this.pattern = pattern;
		this.pattern.getLeftSide().sort();
		this.name = name;
		this.difficulty = difficulty;
	}
	public Rule(String name,int difficulty){
		this.name = name;
		this.difficulty = difficulty;
	}
	
	private static ArrayList<Rule> allPatternBasedRules = new ArrayList<Rule>();
	
	public void init(){//compiling the pattern
		if(patternStr != null) {
			pattern = (Becomes) createExpr(patternStr);
			pattern.getLeftSide().sort();
			allPatternBasedRules.add(this);
		}
		if(conditionStr != null) {
			condition = createExpr(conditionStr);
		}
	}
	//calls init of each rule in the list of sequence
	public static void initRules(Sequence ruleSequence) {
		for(int i = 0;i<ruleSequence.size();i++) ((Rule)ruleSequence.get(i)).init();
	}
	public static void initRules(Rule[] ruleSequence) {
		for(int i = 0;i<ruleSequence.length;i++) ruleSequence[i].init();
	}
	
	public Becomes pattern = null;//the template expresion, its basically an example of the problem
	public Expr condition = null;//a condition for the variables, for example checking if a variable is positive
	/*
	 * uses either the pattern provided or a hard coded system
	 * just applies a transformation to an expression
	 * 
	 * NOTE if your using a pattern based rule make sure to call init before it is used
	 * The reason its not set in stone with the constructor is because some rules are statically defined and we don't want the interpreter to construct
	 * the tree if the interpreter is being upgraded otherwise as soon as it begins the run-time it will crash and tests can't be done
	 * 
	 * this can be override if you want to hard code a rule if it cant be done with patterns
	 */
	public Expr applyRuleToExpr(Expr expr,CasInfo casInfo){//note this may modify the original expression. The return is there so that if it changes expression type
		//System.out.println(pattern.getLeftSide()+" "+expr+" "+fastSimilarStruct(pattern.getLeftSide(),expr));
		if(fastSimilarStruct(pattern.getLeftSide(),expr)) {//we use fast similar struct here because we don't want to call the getParts function twice and its faster
			Sequence exampleParts = sequence();
			Sequence parts = sequence();
			boolean match = checkForMatches(exampleParts,parts,pattern.getLeftSide(),expr);
			if(!match) {
				return expr;
			}
			ExprList equs = new ExprList();
			for(int i = 0;i<parts.size();i++) equs.add(equ(exampleParts.get(i),parts.get(i)));
			
			if(condition != null) {
				Expr condition = this.condition.replace(equs);
				condition = condition.simplify(casInfo);
				if(condition.simplify(casInfo).equals(BoolState.FALSE)) return expr;
			}
			
			Expr out = pattern.getRightSide().replace(equs);
			if(pattern.getLeftSide().getClass() == pattern.getRightSide().getClass()) {
				out.simplifyChildren(casInfo);
			}else {
				out = out.simplify(casInfo);//no recursion since different class type
			}
			return out;
			
		}
		return expr;
	}
	@Override
	public String toString(){
		if(pattern != null && condition != null) {
			return name+" "+pattern+" "+condition;
		}
		if(pattern != null && condition == null) {
			return name+" "+pattern;
		}
		return name;
	}
	
	private static boolean LOADED = false;
	/*
	 * loads all the CAS rules into each type, this can take some time
	 * this is required to be run somewhere before the CAS can be used, maybe put it as close to first line of main method
	 */
	
	public static volatile float loadingPercent = 0;
	public static void loadRules(){
		if(LOADED) return;
		System.out.println("loading CAS rules...");
		long startTime = System.nanoTime();
		
		random = new Random(761234897);
		StandardRules.loadRules();
		
		loadingPercent = 10;
		
		Abs.loadRules();
		Acos.loadRules();
		And.loadRules();
		Approx.loadRules();
		
		loadingPercent = 15;
		
		Asin.loadRules();
		Atan.loadRules();
		Cos.loadRules();
		Define.loadRules();
		Diff.loadRules();
		
		loadingPercent = 20;
		
		Distr.loadRules();
		Div.loadRules();
		Dot.loadRules();
		ExprList.loadRules();
		
		loadingPercent = 25;
		
		Factor.loadRules();
		Gamma.loadRules();
		Integrate.loadRules();
		loadingPercent = 35;
		IntegrateOver.loadRules();
		
		loadingPercent = 40;
		
		LambertW.loadRules();
		Limit.loadRules();
		Ln.loadRules();
		Mat.loadRules();
		
		loadingPercent = 50;
		
		Next.loadRules();
		Not.loadRules();
		Or.loadRules();
		Power.loadRules();
		Prod.loadRules();
		
		loadingPercent = 60;
		
		Sin.loadRules();
		Solve.loadRules();
		Sum.loadRules();
		Tan.loadRules();
		Ternary.loadRules();
		Transpose.loadRules();
		
		loadingPercent = 70;
		
		SimpleFuncs.loadRules();
		
		float incr = 20.0f/allPatternBasedRules.size();
		for(Rule r:allPatternBasedRules) {//simplify rules
			if(!r.pattern.getRightSide().containsType(r.pattern.getLeftSide().typeName())) {//non recursive simplification
				r.pattern.setRightSide(r.pattern.getRightSide().simplify(CasInfo.normal));
			}
			loadingPercent+=incr;
		}
		
		loadingPercent = 90;
		
		Ask.loadBasicQuestions();
		
		SimpleFuncs.FUNCTION_UNLOCKED = true;
		
		long endTime = System.nanoTime();
		System.out.println("done loading Rules! took "+((endTime-startTime)/1000000.0)+" ms");
		
		loadingPercent = 100;
		LOADED = true;
	}
	
	@Override
	public Sequence getRuleSequence() {
		return null;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return null;
	}
	
	@Override
	public String typeName() {
		return "rule";
	}
}
