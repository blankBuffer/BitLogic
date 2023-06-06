package cas.base;

import java.util.ArrayList;

import cas.Algorithms;
import cas.bool.*;
import cas.primitive.*;

import static cas.Cas.*;

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
	
	public String name = null;//name or description of the rule
	private String patternStr = null;//pattern as a string before initialization
	private String conditionStr = null;//condition as a string before initialization
	
	public Rule[] cases = null;
	
	/*
	 * the transformation description with a before and after state
	 * beforeState->afterState
	 */
	public Func pattern = null;
	private Expr condition = null;//conditions that apply to the rule
	
	
	/*
	 * the list of variables in the left side of the pattern used for comparison
	 * if the left side of the pattern was
	 * sin(2*k)*a^b
	 * it would generate a part set of [k,a,b] assuming the expression is sorted
	 */
	private Func patternPartsSequence = null;
	
	
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
	private ArrayList<Algorithms.IndexSet> matchingSetsForPattern = null;
	
	public Rule(String pattern,String name){
		patternStr = pattern;
		this.name = name;
	}
	public Rule(String pattern,String condition,String name){
		patternStr = pattern;
		conditionStr = condition;
		this.name = name;
	}
	public Rule(Func patternBecomes,String name){
		this.pattern = patternBecomes;
		Becomes.getLeftSide(this.pattern).sort();
		this.name = name;
	}
	public Rule(Rule[] cases,String name) {
		this.name = name;
		this.cases = cases;
	}
	public Rule(String name){
		this.name = name;
	}
	
	
	/*
	 * loads the rule from the 'patternStr' string to the 'pattern' expression
	 * loads the condition from the 'conditionStr' string to the 'condition' expression
	 */
	public void init(){
		
		//System.out.println(this);
		
		if(patternStr != null) {
			pattern = (Func) createExpr(patternStr);
			if(!pattern.isType("becomes")) throw new RuntimeException("expected a becomes for a pattern!");
		}
		
		if(pattern != null) {
			Becomes.getLeftSide(pattern).sort();//sort the patterns left side into a standardized order
			
			patternPartsSequence = generateTemplateParts(Becomes.getLeftSide(pattern));
			matchingSetsForPattern = generateTemplateMatchingSets(patternPartsSequence);
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
	private static boolean getPartsBasedOnTemplate(Func exprsPartsOutSequence,Expr template,Expr expr) {
		if(template instanceof Var && ((Var)template).isGeneric()) {
			exprsPartsOutSequence.add(expr.copy());
		}else{
			if(template.size()==expr.size() && template.isType(expr.typeName())) {
				for(int i = 0;i<template.size();i++) {
					if(!getPartsBasedOnTemplate(exprsPartsOutSequence,template.get(i),expr.get(i))) return false;
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
	private static ArrayList<Algorithms.IndexSet> generateTemplateMatchingSets(Func templatePartsSequence){
		Func usedVarsSet = exprSet();
		ArrayList<Algorithms.IndexSet> matcherIndexSets = new ArrayList<Algorithms.IndexSet>();
		for(int i = 0;i<templatePartsSequence.size();i++) {
			
			if(usedVarsSet.contains(templatePartsSequence.get(i))) continue;
			
			Algorithms.IndexSet matchSet = new Algorithms.IndexSet();
			matchSet.ints.add(i);
			
			for(int j = i+1;j<templatePartsSequence.size();j++) {
				if(templatePartsSequence.get(i).equals(templatePartsSequence.get(j))) {
					matchSet.ints.add(j);
					
				}
			}
			
			if(matchSet.ints.size() != 1) matcherIndexSets.add(matchSet);
			
			usedVarsSet.add(templatePartsSequence.get(i));
		}
		return matcherIndexSets;
	}
	
	private static Func generateTemplateParts(Expr template) {//wrapper function, returns sequence
		Func templateVarsOutSequence = sequence();
		generateTemplatePartsRec(templateVarsOutSequence,template);
		return templateVarsOutSequence;
	}
	private static void generateTemplatePartsRec(Func templateVarsOutSequence,Expr template) {
		if(template instanceof Var && ((Var)template).isGeneric()) {
			templateVarsOutSequence.add(template.copy());
		}else{
			for(int i = 0;i<template.size();i++) {
				generateTemplatePartsRec(templateVarsOutSequence,template.get(i));
			}
		}
	}
	
	private static Func makeEqusFromParts(Func templatePartsSequence,Func otherPartsSequence) {//return set of equations
		Func out = exprSet();
		for(int i = 0;i<templatePartsSequence.size();i++) {
			out.add(equ(templatePartsSequence.get(i),otherPartsSequence.get(i)));
		}
		return out;
	}
	
	private static boolean similarExpr(Expr template,Expr other,Expr condition,boolean checked){
		if(other == null) return false;
		if(template instanceof Var && ((Var) template).isGeneric()) return true;
		
		if(template.isType(other.typeName())) {
			if(template instanceof Num || template instanceof BoolState || template instanceof FloatExpr || template instanceof Var) return template.equals(other);
			if(template.size() != other.size()) return false;
			
			template.sort();
			other.sort();
			
			if(!checked) {
				Func otherPartsSequence = sequence();
				
				Func templatePartsSequence = generateTemplateParts(template);
				ArrayList<Algorithms.IndexSet> matchingSets = generateTemplateMatchingSets(templatePartsSequence);
				
				boolean partsMatch = checkForMatches(matchingSets,otherPartsSequence,template,other);
				if(condition == null) return partsMatch;
				if(!partsMatch) return false;
				
				Func equsSet = makeEqusFromParts(templatePartsSequence,otherPartsSequence);
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
			Func exprPartsSequence = sequence();
			
			Func templatePartsSequence = generateTemplateParts(template);
			ArrayList<Algorithms.IndexSet> matchingSets = generateTemplateMatchingSets(templatePartsSequence);
			
			boolean match = checkForMatches(matchingSets,exprPartsSequence,template,expr);
			if(!match) {
				return null;
			}
			Func equsSet = makeEqusFromParts(templatePartsSequence,exprPartsSequence);
			
			if(condition != null && !condition.replace(equsSet).simplify(CasInfo.normal).equals(BoolState.TRUE)) return null;
			
			return equsSet;
			
		}
		return null;
	}
	
	static public Func getEqusFromTemplate(Expr template,Expr expr) {//returns set of equations
		return getEqusFromTemplate(template,null,expr);
	}
	
	private static boolean checkForMatches(ArrayList<Algorithms.IndexSet> matchingSets,Func exprsPartsSequence,Expr template,Expr expr) {
		
		template.sort();
		expr.sort();
		
		if(!getPartsBasedOnTemplate(exprsPartsSequence,template,expr)) return false;
		
		for(Algorithms.IndexSet set:matchingSets) {
			Expr e = exprsPartsSequence.get(set.ints.get(0));
			for(int i = 1;i<set.ints.size();i++) {
				
				Expr e2 =  exprsPartsSequence.get(set.ints.get(i));
				
				if(!e2.equals(e)) return false;
			}
			
		}
		
		return true;
	}
	
	public static void initRules(Func ruleSequence) {
		for(int i = 0;i<ruleSequence.size();i++) ((Rule)ruleSequence.get(i)).init();
	}
	public static void initRules(Rule[] ruleSequence) {
		for(int i = 0;i<ruleSequence.length;i++) ruleSequence[i].init();
	}
	
	public Expr applyRuleToExpr(Expr expr,CasInfo casInfo){
		
		if(pattern == null && cases == null) throw new RuntimeException("pattern is missing : "+name);
		
		if(cases == null) {
			if(fastSimilarExpr(Becomes.getLeftSide(pattern),expr)) {
				Func partsSequence = sequence();
				
				boolean match = checkForMatches(matchingSetsForPattern,partsSequence,Becomes.getLeftSide(pattern),expr);
				if(!match) {
					return expr;
				}
				Func equsSet = makeEqusFromParts(patternPartsSequence,partsSequence);
				
				if(condition != null) {
					Expr condition = this.condition.replace(equsSet);
					condition = condition.simplify(casInfo);
					if(!condition.simplify(casInfo).equals(BoolState.TRUE)) return expr;
				}
				
				Expr out = Becomes.getRightSide(pattern).replace(equsSet);
				if(Becomes.getLeftSide(pattern).isType(Becomes.getRightSide(pattern).typeName())) {
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
