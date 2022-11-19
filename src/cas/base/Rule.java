package cas.base;

import java.util.ArrayList;
import java.util.Random;

import cas.SimpleFuncs;
import cas.bool.*;
import cas.calculus.*;
import cas.lang.Ask;
import cas.matrix.*;
import cas.primitive.*;
import cas.programming.*;
import cas.special.*;


/*
 * rules are used to describe any expression transformation
 * it can either be hard coded or use a pattern with the option of a condition
 * a rule with a pattern is described in a string "before_state->after_state" the arrow -> signifies the transformation
 * a condition can check weather some variables are in a range and you can have it in a boolean algebra statement
 * 
 * a typical rule used in this CAS can be written as follows 
 * 
 * Rule myRule = new Rule("x/x->1","~eval(x=0)","x divided by x");
 * 
 * the eval checks if a given statement is true or not eval(2=3) gives false but eval(2=2) gives true
 * any eval with a variable returns true
 * 
 */

public class Rule extends Expr{
	
	private static int ruleCount = 0;
	
	public String name = null;//name or description of the rule
	private String patternStr = null;//pattern as a string before initialization
	private String conditionStr = null;//condition as a string before initialization
	
	public Rule[] cases = null;
	
	/*
	 * the transformation description with a before and after state
	 * beforeState->afterState
	 */
	private Func pattern = null;
	private Expr condition = null;//conditions that apply to the rule
	
	
	/*
	 * the list of variables in the left side of the pattern used for comparison
	 * if the left side of the pattern was
	 * sin(2*k)*a^b
	 * it would generate a part set of [k,a,b] assuming the expression is sorted
	 */
	private Sequence patternParts = null;
	
	
	/*
	 * the sets of matching variables in the patterns parts
	 * if the left side of the pattern has variables used more than once it keeps track of the
	 * matching indexes from the patternParts variable
	 * for example with the left side of a pattern being
	 * sin(c*b)*a^b+a
	 * it would generate a part set of [c,b,a,b,a] assuming the expression is sorted
	 * the variables 'a' and 'b' show up more than once so when comparing with the pattern we need
	 * the same locations to be equal
	 * the 	matchingSetsForPattern' would therefore store this as
	 * {{1,3},{2,4}}
	 * indicating the indexes of the matching variables in the parts list
	 */
	private ArrayList<IndexSet> matchingSetsForPattern = null;
	
	/*
	 * list of all pattern based rules in the CAS
	 * we store it so that we can simplify the patterns before being used
	 */
	private static Sequence allPatternBasedRules = sequence();
	
	public Rule(String pattern,String name){
		patternStr = pattern;
		this.name = name;
		ruleCount++;
	}
	public Rule(String pattern,String condition,String name){
		patternStr = pattern;
		conditionStr = condition;
		this.name = name;
		ruleCount++;
	}
	public Rule(Func patternBecomes,String name){
		this.pattern = patternBecomes;
		Becomes.getLeftSide(this.pattern).sort();
		this.name = name;
		ruleCount++;
	}
	public Rule(Rule[] cases,String name) {
		this.name = name;
		this.cases = cases;
		ruleCount++;
	}
	public Rule(String name){
		this.name = name;
		ruleCount++;
	}
	
	
	/*
	 * loads the rule from the 'patternStr' string to the 'pattern' expression
	 * loads the condition from the 'conditionStr' string to the 'condition' expression
	 */
	public void init(){
		
		//System.out.println(this);
		
		if(patternStr != null) {
			pattern = (Func) createExpr(patternStr);
			if(!pattern.typeName().equals("becomes")) throw new RuntimeException("expected a becomes for a pattern!");
		}
		
		if(pattern != null) {
			Becomes.getLeftSide(pattern).sort();//sort the patterns left side into a standardized order
			
			patternParts = generateTemplateParts(Becomes.getLeftSide(pattern));
			matchingSetsForPattern = generateTemplateMatchingSets(patternParts);
			
			allPatternBasedRules.add(this);
		}
		
		if(conditionStr != null) condition = createExpr(conditionStr);
		if(cases != null) Rule.initRules(cases);
		
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
	
	/*
	 * loads the parts based on the template expression layout
	 * basically whenever you run into a variable in the template tree load the same relative expression part
	 * into the 'exprsPartsOut' list
	 * 
	 * say the template is sin(a^b)+k and the expression is sin(2^(k+1))+cos(x)
	 * it would generate the parts [2,k+1,cos(x)]
	 * 
	 * the function returns false if it cannot accomplish the task because the expression is not similar to the template
	 */
	private static boolean getPartsBasedOnTemplate(Sequence exprsPartsOut,Expr template,Expr expr) {
		if(template instanceof Var && ((Var)template).isGeneric()) {
			exprsPartsOut.add(expr.copy());
		}else{
			if(template.size()==expr.size() && template.typeName().equals(expr.typeName())) {
				for(int i = 0;i<template.size();i++) {
					if(!getPartsBasedOnTemplate(exprsPartsOut,template.get(i),expr.get(i))) return false;
				}
			}else {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * creates the matching array sequences to describe where variable names are re-used
	 * this is described above where sin(c*b)*a^b+a maps to {{1,3},{2,4}}
	 */
	private static ArrayList<IndexSet> generateTemplateMatchingSets(Sequence templateParts){
		Func usedVarsSet = exprSet();
		ArrayList<IndexSet> matcherIndexSets = new ArrayList<IndexSet>();
		for(int i = 0;i<templateParts.size();i++) {
			
			if(usedVarsSet.contains(templateParts.get(i))) continue;
			
			IndexSet matchSet = new IndexSet();
			matchSet.ints.add(i);
			
			for(int j = i+1;j<templateParts.size();j++) {
				if(templateParts.get(i).equals(templateParts.get(j))) {
					matchSet.ints.add(j);
					
				}
			}
			
			if(matchSet.ints.size() != 1) matcherIndexSets.add(matchSet);
			
			usedVarsSet.add(templateParts.get(i));
		}
		return matcherIndexSets;
	}
	
	private static Sequence generateTemplateParts(Expr template) {//wrapper function
		Sequence templateVarsOut = sequence();
		generateTemplatePartsRec(templateVarsOut,template);
		return templateVarsOut;
	}
	private static void generateTemplatePartsRec(Sequence templateVarsOut,Expr template) {
		if(template instanceof Var && ((Var)template).isGeneric()) {
			templateVarsOut.add(template.copy());
		}else{
			for(int i = 0;i<template.size();i++) {
				generateTemplatePartsRec(templateVarsOut,template.get(i));
			}
		}
	}
	
	private static Func makeEqusFromParts(Sequence templateParts,Sequence otherParts) {//return set of equations
		Func out = exprSet();
		for(int i = 0;i<templateParts.size();i++) {
			out.add(equ(templateParts.get(i),otherParts.get(i)));
		}
		return out;
	}
	
	private static boolean similarExpr(Expr template,Expr other,Expr condition,boolean checked){
		if(other == null) return false;
		if(template instanceof Var && ((Var) template).isGeneric()) return true;
		
		if(template.typeName().equals(other.typeName())) {
			if(template instanceof Num || template instanceof BoolState || template instanceof FloatExpr || template instanceof Var) return template.equals(other);
			if(template.size() != other.size()) return false;
			
			template.sort();
			other.sort();
			
			if(!checked) {
				Sequence otherParts = sequence();
				
				Sequence templateParts = generateTemplateParts(template);
				ArrayList<IndexSet> matchingSets = generateTemplateMatchingSets(templateParts);
				
				boolean partsMatch = checkForMatches(matchingSets,otherParts,template,other);
				if(condition == null) return partsMatch;
				if(!partsMatch) return false;
				
				Func equsSet = makeEqusFromParts(templateParts,otherParts);
				return condition.replace(equsSet).simplify(CasInfo.normal).equals(BoolState.TRUE);
			}
			
			for(int i = 0;i<template.size();i++) {
				if(!fastSimilarExpr(template.get(i),other.get(i))) return false;
			}
			
			return true;
		}
		return false;
	}
	
	
	public static boolean fastSimilarExpr(Expr template,Expr other) {
		return similarExpr(template,other,null,true);
	}
	
	public static boolean strictSimilarExpr(Expr template,Expr other) {
		return similarExpr(template,other,null,false);
	}
	
	public static boolean similarWithCondition(Expr template,Expr other,Expr condition) {
		return similarExpr(template,other,condition,false);
	}
	
	public static Expr getExprByName(Func equsSet,String name){
		for (int i = 0;i<equsSet.size();i++){
			Func currentEqu = (Func) equsSet.get(i);
			if(Equ.getLeftSide(currentEqu).toString().equals(name)){
				return Equ.getRightSide(currentEqu);
			}
		}
		return null;
	}
	
	
	
	static public Func getEqusFromTemplate(Expr template,Expr condition,Expr expr) {//returns set of equations
		if(fastSimilarExpr(template,expr)) {
			Sequence exprParts = sequence();
			
			Sequence templateParts = generateTemplateParts(template);
			ArrayList<IndexSet> matchingSets = generateTemplateMatchingSets(templateParts);
			
			boolean match = checkForMatches(matchingSets,exprParts,template,expr);
			if(!match) {
				return null;
			}
			Func equsSet = makeEqusFromParts(templateParts,exprParts);
			
			if(condition != null && !condition.replace(equsSet).simplify(CasInfo.normal).equals(BoolState.TRUE)) return null;
			
			return equsSet;
			
		}
		return null;
	}
	
	static public Func getEqusFromTemplate(Expr template,Expr expr) {//returns set of equations
		return getEqusFromTemplate(template,null,expr);
	}
	
	private static boolean checkForMatches(ArrayList<IndexSet> matchingSets,Sequence exprsParts,Expr template,Expr expr) {
		
		template.sort();
		expr.sort();
		
		if(!getPartsBasedOnTemplate(exprsParts,template,expr)) return false;
		
		for(IndexSet set:matchingSets) {
			Expr e = exprsParts.get(set.ints.get(0));
			for(int i = 1;i<set.ints.size();i++) {
				
				Expr e2 =  exprsParts.get(set.ints.get(i));
				
				if(!e2.equals(e)) return false;
			}
			
		}
		
		return true;
	}
	
	public static void initRules(Sequence ruleSequence) {
		for(int i = 0;i<ruleSequence.size();i++) ((Rule)ruleSequence.get(i)).init();
	}
	public static void initRules(Rule[] ruleSequence) {
		for(int i = 0;i<ruleSequence.length;i++) ruleSequence[i].init();
	}
	
	public Expr applyRuleToExpr(Expr expr,CasInfo casInfo){
		
		if(pattern == null && cases == null) throw new RuntimeException("pattern is missing : "+name);
		
		if(cases == null) {
			if(fastSimilarExpr(Becomes.getLeftSide(pattern),expr)) {
				Sequence parts = sequence();
				
				boolean match = checkForMatches(matchingSetsForPattern,parts,Becomes.getLeftSide(pattern),expr);
				if(!match) {
					return expr;
				}
				Func equsSet = makeEqusFromParts(patternParts,parts);
				
				if(condition != null) {
					Expr condition = this.condition.replace(equsSet);
					condition = condition.simplify(casInfo);
					if(!condition.simplify(casInfo).equals(BoolState.TRUE)) return expr;
				}
				
				Expr out = Becomes.getRightSide(pattern).replace(equsSet);
				if(Becomes.getLeftSide(pattern).typeName().equals(Becomes.getRightSide(pattern).typeName())) {
					out.simplifyChildren(casInfo);
				}else {
					out = out.simplify(casInfo);
				}
				return out;
				
			}
		}else {
			String startType = expr.typeName();
			for(Rule r:cases){
				//r.println();
				//expr.println();
				expr = r.applyRuleToExpr(expr, casInfo);
				String currentType = expr.typeName();
				if(!currentType.equals(startType)) break;
			}
		}
		return expr;
	}
	
	
	
	private static boolean ALL_LOADED = false;
	private static boolean RULES_LOADED = false;
	public static boolean isAllLoaded() {
		return ALL_LOADED;
	}
	public static boolean rulesLoaded() {
		return RULES_LOADED;
	}
	
	
	
	private static volatile float loadingPercent = 0;
	public static float getLoadingPercent() {
		return loadingPercent;
	}
	
	private static void loadTypeRules() {
		SimpleFuncs.loadRules();
		StandardRules.loadRules();
		
		Define.loadRules();
		
		Dot.loadRules();
		
		Limit.loadRules();
		Mat.loadRules();
		
		Next.loadRules();
		Range.loadRules();
		
		Ternary.loadRules();
		Transpose.loadRules();
		
		loadingPercent = 70;
		
		System.out.println("rules loaded");
		RULES_LOADED = true;
	}
	
	static ArrayList<String> bannedPreSimplifyFunctions = new ArrayList<String>();
	private static void loadBannedPreSimplifyFunctions() {
		bannedPreSimplifyFunctions.add("allowAbs");
		bannedPreSimplifyFunctions.add("allowComplexNumbers");
		bannedPreSimplifyFunctions.add("singleSolutionMode");
		bannedPreSimplifyFunctions.add("factorIrrationalRoots");
		
		bannedPreSimplifyFunctions.add("size");
		bannedPreSimplifyFunctions.add("get");
		bannedPreSimplifyFunctions.add("degree");
		bannedPreSimplifyFunctions.add("comparison");
		bannedPreSimplifyFunctions.add("expand");
		bannedPreSimplifyFunctions.add("distr");
		bannedPreSimplifyFunctions.add("factor");
		bannedPreSimplifyFunctions.add("isType");
		bannedPreSimplifyFunctions.add("contains");
		bannedPreSimplifyFunctions.add("result");
		bannedPreSimplifyFunctions.add("similar");
		bannedPreSimplifyFunctions.add("fastEquals");
		
	}
	
	public static void loadCompileSimplifyRules(){//loads and simpoifies everything, faster runtime, ,much slower runtime
		if(ALL_LOADED) return;
		System.out.println("loading CAS rules...");
		long startTime = System.nanoTime();
		
		random = new Random(761234897);
		
		loadTypeRules();
		
		float incr = 20.0f/allPatternBasedRules.size();
		
		loadBannedPreSimplifyFunctions();
		for(int i = 0;i<allPatternBasedRules.size();i++) {//simplify
			Rule r = (Rule)allPatternBasedRules.get(i);
			boolean doNotSimplify = true;
			
			for(String bannedType:bannedPreSimplifyFunctions) {
				if(Becomes.getRightSide(r.pattern).containsType(bannedType)) {
					doNotSimplify = true;
					break;
				}
			}
			
			if(!doNotSimplify) {
				if(!Becomes.getRightSide(r.pattern).containsType(Becomes.getLeftSide(r.pattern).typeName())) {//avoid recursion
					Becomes.setRightSide(r.pattern,Becomes.getRightSide(r.pattern).simplify(CasInfo.normal));
				}else {
					Becomes.getRightSide(r.pattern).simplifyChildren(CasInfo.normal, Becomes.getLeftSide(r.pattern).typeName());
				}
			}
			loadingPercent+=incr;
		}
		
		
		loadingPercent = 90;
		
		Ask.loadBasicQuestions();
		
		SimpleFuncs.FUNCTION_UNLOCKED = true;
		
		long endTime = System.nanoTime();
		System.out.println("done loading and simplifying "+ruleCount+" Rules! took "+((endTime-startTime)/1000000.0)+" ms");
		
		loadingPercent = 100;
		ALL_LOADED = true;
	}
	
	@Override
	public Rule getRule() {
		return null;
	}
	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return null;
	}
	
	@Override
	public String typeName() {
		return "rule";
	}
	@Override
	public String help() {
		return "rule expression is a transformation";
	}
}