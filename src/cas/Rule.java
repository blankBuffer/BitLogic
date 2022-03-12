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
	
	public String name = null;//name or description of the rule
	private String patternStr = null;//pattern as a string before initialization
	private String conditionStr = null;//condition as a string before initialization
	
	
	/*
	 * the transformation description with a before and after state
	 * beforeState->afterState
	 */
	private Becomes pattern = null;
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
	private static ArrayList<Rule> allPatternBasedRules = new ArrayList<Rule>();
	
	public Rule(String pattern,String name){
		patternStr = pattern;
		this.name = name;
	}
	public Rule(String pattern,String condition,String name){
		patternStr = pattern;
		conditionStr = condition;
		this.name = name;
	}
	public Rule(Becomes pattern,String name){
		this.pattern = pattern;
		this.pattern.getLeftSide().sort();
		this.name = name;
	}
	public Rule(String name){
		this.name = name;
	}
	
	/*
	 * loads the rule from the 'patternStr' string to the 'pattern' expression
	 * loads the condition from the 'conditionStr' string to the 'condition' expression
	 */
	public void init(){
		if(patternStr != null) {
			pattern = (Becomes) createExpr(patternStr);
			pattern.getLeftSide().sort();//sort the patterns left side into a standardized order
			
			patternParts = generateTemplateParts(pattern.getLeftSide());
			matchingSetsForPattern = generateTemplateMatchingSets(patternParts);
			
			allPatternBasedRules.add(this);
		}
		if(conditionStr != null) {
			condition = createExpr(conditionStr);
		}
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
	
	private static ArrayList<IndexSet> generateTemplateMatchingSets(Sequence templateParts){
		ExprList usedVars = new ExprList();
		ArrayList<IndexSet> matcherIndexSets = new ArrayList<IndexSet>();
		for(int i = 0;i<templateParts.size();i++) {
			
			if(usedVars.contains(templateParts.get(i))) continue;
			
			IndexSet matchSet = new IndexSet();
			matchSet.ints.add(i);
			
			for(int j = i+1;j<templateParts.size();j++) {
				if(templateParts.get(i).equals(templateParts.get(j))) {
					matchSet.ints.add(j);
					
				}
			}
			
			if(matchSet.ints.size() != 1) matcherIndexSets.add(matchSet);
			
			usedVars.add(templateParts.get(i));
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
	
	private static boolean checkForMatches(Expr template,Expr expr) {
		Sequence exprs = sequence();
		
		Sequence templateParts = generateTemplateParts(template);
		ArrayList<IndexSet> matchingSets = generateTemplateMatchingSets(templateParts);
		
		return checkForMatches(matchingSets,exprs,template,expr);
	}
	
	private static boolean similarExpr(Expr template,Expr other,boolean checked){
		if(other == null) return false;
		if(template instanceof Var && ((Var) template).isGeneric()) return true;
		
		if(template.typeName().equals(other.typeName())) {
			if(template instanceof Num || template instanceof BoolState || template instanceof FloatExpr || template instanceof Var) return template.equals(other);
			if(template.size() != other.size()) return false;
			
			template.sort();
			other.sort();
			
			if(!checked) return Rule.checkForMatches(template,other);
			
			for(int i = 0;i<template.size();i++) {
				if(!fastSimilarExpr(template.get(i),other.get(i))) return false;
			}
			
			return true;
		}
		return false;
	}
	
	
	public static boolean fastSimilarExpr(Expr template,Expr other) {
		return similarExpr(template,other,true);
	}
	
	public static boolean strictSimilarExpr(Expr template,Expr other) {
		return similarExpr(template,other,false);
	}
	
	public static Expr getExprByName(ExprList equs,String name){
		for (int i = 0;i<equs.size();i++){
			Equ currentEqu = (Equ) equs.get(i);
			if(currentEqu.getLeftSide().toString().equals(name)){
				return currentEqu.getRightSide();
			}
		}
		return null;
	}
	
	
	static public ExprList getEqusFromTemplate(Expr template,Expr expr) {
		if(fastSimilarExpr(template,expr)) {
			Sequence exprParts = sequence();
			
			Sequence templateParts = generateTemplateParts(template);
			ArrayList<IndexSet> matchingSets = generateTemplateMatchingSets(templateParts);
			
			boolean match = checkForMatches(matchingSets,exprParts,template,expr);
			if(!match) {
				return null;
			}
			ExprList equs = new ExprList();
			for(int i = 0;i<exprParts.size();i++) equs.add(equ(templateParts.get(i),exprParts.get(i)));
			
			return equs;
			
		}
		return null;
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
		
		if(fastSimilarExpr(pattern.getLeftSide(),expr)) {
			Sequence parts = sequence();
			
			boolean match = checkForMatches(matchingSetsForPattern,parts,pattern.getLeftSide(),expr);
			if(!match) {
				return expr;
			}
			ExprList equs = new ExprList();
			for(int i = 0;i<parts.size();i++) equs.add(equ(patternParts.get(i),parts.get(i)));
			
			if(condition != null) {
				Expr condition = this.condition.replace(equs);
				condition = condition.simplify(casInfo);
				if(condition.simplify(casInfo).equals(BoolState.FALSE)) return expr;
			}
			
			Expr out = pattern.getRightSide().replace(equs);
			if(pattern.getLeftSide().getClass() == pattern.getRightSide().getClass()) {
				out.simplifyChildren(casInfo);
			}else {
				out = out.simplify(casInfo);
			}
			return out;
			
		}
		return expr;
	}
	
	private static boolean LOADED = false;
	public static boolean isLoaded() {
		return LOADED;
	}
	
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
		Range.loadRules();
		
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
		for(Rule r:allPatternBasedRules) {
			if(!r.pattern.getRightSide().containsType(r.pattern.getLeftSide().typeName())) {
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
