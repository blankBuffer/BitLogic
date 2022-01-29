package cas;

import java.util.ArrayList;

public class Rule extends Expr{
	private static final long serialVersionUID = 5928512201641990677L;
	
	static final int VERY_EASY = 0,EASY = 1,UNCOMMON = 2,TRICKY = 3,CHALLENGING = 4,DIFFICULT = 5,VERY_DIFFICULT = 6;//this is opinion based
	static final boolean VERBOSE_DEFAULT = false;
	static final int VERBOSE_DIFFICULTY = VERY_EASY;
	
	String name = null;
	public boolean verbose = VERBOSE_DEFAULT;
	int difficulty = 0;
	
	//the checked variable simply keeps track weather chechForMatches has been checked
	static boolean similarStruct(Expr template,Expr other,boolean checked){//returns if another expression is similar to this but any comparison with variables is returned true
		if(other == null) return false;
		if(template instanceof Var && ((Var) template).generic) return true;
		
		if(other.getClass().equals(template.getClass())) {
			
			if(template instanceof Num || template instanceof BoolState || template instanceof FloatExpr || template instanceof Var) return template.equals(other);
			
			if(!checked) {
				template.sort();
				other.sort();
			}
			if(template.size() != other.size()) return false;
			
			if(!checked) if(Rule.checkForMatches(template,other) == false) return false;
			
			if(template.commutative){
				
				boolean[] usedIndicies = new boolean[other.size()];
				for(int i = 0;i<template.size();i++) {
					if(template.get(i) instanceof Var && ((Var)template.get(i)).generic) continue;//skip because they return true on anything
					boolean found = false;
					for(int j = 0;j<other.size();j++) {
						if(usedIndicies[j]) continue;
						else if(fastSimilarStruct(template.get(i),other.get(j))) {
							found = true;
							usedIndicies[j] = true;
							break;
						}
					}
					if(!found) return false;
				}
				return true;
			}//else not commutative
			
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
	 * 
	 * similar is defined as having a same tree but the variables might be different or other has expressions where the varibales would be
	 * 
	 * this is best understood by example
	 * 
	 * this=a^b other=2^3 , fastSimilar = true , strictSimilar = false
	 * this=a^a other=2^3 , fastSimilar = true , strictSimilar = fasle (because 2 not equal to 3)
	 * 
	 * this=sin(x)^b other = a^b , not similar at all, why? because the template was expecting the base to be the sine function
	 * 
	 * note that with sums and products the length of sub expressions must be equal. It does its best to sort the expressions in the sum/product in a similar kind of way, but its not perfect
	 * 
	 */
	public static boolean fastSimilarStruct(Expr template,Expr other) {
		return similarStruct(template,other,true);
	}
	/*
	 * a full similar comparisons we DO check for matches making it a guarantee
	 * 
	 * note that with sums and products the length must be equal
	 * 
	 */
	public static boolean strictSimilarStruct(Expr template,Expr other) {
		return similarStruct(template,other,false);
	}
	
	static class ModifyFromExampleResult{
		Expr expr;
		boolean success;
		ModifyFromExampleResult(Expr expr,boolean success ){
			this.expr = expr;
			this.success = success;
		}
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
		if(fastSimilarStruct(template,expr)) {//we use fast similar struct here because we don't want to call the getParts function twice and its faster
			ExprList exampleParts = new ExprList();
			ExprList parts = new ExprList();
			boolean match = checkForMatches(exampleParts,parts,template,expr);
			if(!match) {
				return null;
			}
			ExprList equs = new ExprList();
			for(int i = 0;i<parts.size();i++) equs.add(new Equ(exampleParts.get(i),parts.get(i)));
			
			return equs;
			
		}
		return null;
	}
	
	static boolean checkForMatches(Expr template,Expr expr) {//shortcut
		ExprList vars = new ExprList();
		ExprList exprs = new ExprList();
		
		return checkForMatches(vars,exprs,template,expr);
	}
	
	/*
	 * extracts the parts and variables from the template=this, and expr is the parts to be extracted 
	 * example, let this=diff(x^a,x) and expr=diff(m^(k*l),m)
	 * these expression break down into lists
	 * this -> [x,a,x]
	 * expr -> [m,k*l,m]
	 * 
	 * if this lists for some reason are not the same length it returns false saying that they can't be compared
	 * returning true is saying no problems were encountered
	 * 
	 * this is useful because we can later compare the list for matching sets,
	 * for example in this example the first and last element must be the same [ ->x ,a, ->x ],
	 * this is what the check for matches function does
	 * 
	 */
	static boolean getParts(ExprList templateVarsOut,ExprList exprsPartsOut,Expr template,Expr expr) {//template and this is input,vars and exprs are outputs
		if(template instanceof Var && ((Var)template).generic) {//template encountered a variable, time for extraction into the expr list
			templateVarsOut.add(template.copy());
			exprsPartsOut.add(expr.copy());
		}else{
			if(template.size()==expr.size()) {
				for(int i = 0;i<template.size();i++) {
					if(!getParts(templateVarsOut,exprsPartsOut,template.get(i),expr.get(i))) return false;
				}
			}else {
				return false;//different size so variables will obviously not match
			}
		}
		return true;
	}
	
	static boolean checkForMatches(ExprList templateVars,ExprList exprsParts,Expr template,Expr expr) {
		
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
		
		
		///parts two, check other has matches
		
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
	
	
	public Rule(String pattern,String name,int difficulty){
		this.pattern = (Equ) createExpr(pattern);
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public Rule(String pattern,String condition,String name,int difficulty){
		this.pattern = (Equ) createExpr(pattern);
		this.condition = createExpr(condition);
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public Rule(Equ pattern,String name,int difficulty){
		this.pattern = pattern;
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public Rule(String name,int difficulty){
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public void init(){
		
	}
	
	public Equ pattern = null;
	public Expr condition = null;
	Expr applyRuleToExpr(Expr expr,Settings settings){//note this may modify the original expression. The return is there so that if it changes expression type
		if(fastSimilarStruct(pattern.getLeftSide(),expr)) {//we use fast similar struct here because we don't want to call the getParts function twice and its faster
			
			ExprList exampleParts = new ExprList();
			ExprList parts = new ExprList();
			boolean match = checkForMatches(exampleParts,parts,pattern.getLeftSide(),expr);
			if(!match) {
				return expr;
			}
			ExprList equs = new ExprList();
			for(int i = 0;i<parts.size();i++) equs.add(new Equ(exampleParts.get(i),parts.get(i)));
			
			if(condition != null) {
				Expr conditionResult = condition.replace(equs);
				if(conditionResult.equals(BoolState.FALSE)) return expr;
			}
			
			Expr out = pattern.getRightSide().replace(equs);
			if(pattern.getLeftSide().getClass() == pattern.getRightSide().getClass()) {
				out.simplifyChildren(settings);
			}else {
				out = out.simplify(settings);//no recursion since different class type
			}
			return out;
			
		}
		return expr;
	}
	@Override
	public String toString(){
		return name;
	}
	
	public static void loadRules(){
		System.out.println("loading CAS rules...");
		Acos.loadRules();
		And.loadRules();
		Approx.loadRules();
		Asin.loadRules();
		Atan.loadRules();
		Cos.loadRules();
		Diff.loadRules();
		Distr.loadRules();
		Div.loadRules();
		ExprList.loadRules();
		
		Factor.loadRules();
		Gamma.loadRules();
		Integrate.loadRules();
		IntegrateOver.loadRules();
		LambertW.loadRules();
		Limit.loadRules();
		
		Ln.loadRules();
		Not.loadRules();
		Or.loadRules();
		Power.loadRules();
		Prod.loadRules();
		
		Sin.loadRules();
		
		SimpleFuncs.loadRules();
		Sum.loadRules();
		Tan.loadRules();
		System.out.println("done loading Rules!");
	}
	
	@Override
	ExprList getRuleSequence() {
		return null;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return null;
	}
}
