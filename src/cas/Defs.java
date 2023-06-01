package cas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cas.base.Expr;
import cas.base.Func;
import cas.base.FunctionsLoader;
import cas.base.Rule;
import cas.primitive.Becomes;
import cas.primitive.Equ;

public class Defs extends Cas implements Serializable{
	private static final long serialVersionUID = 4654953050013809971L;
	
	HashMap<String,Expr> varDefs = new HashMap<String,Expr>();
	ArrayList<String> addedFunctionNames = new ArrayList<String>();
	
	public void defineVar(Func defEqu) {
		String varName = Equ.getLeftSide(defEqu).toString();
		if(!varDefs.containsKey(varName)) varDefs.put(varName, Equ.getRightSide(defEqu));
		else varDefs.replace(varName, Equ.getRightSide(defEqu));
	}
	
	public void defineFunc(String name,Rule rule) {//rule is assumed to be initialized
		if(addedFunctionNames.contains(name)) {//delete old
			FunctionsLoader.funcs.remove(name);
		}else {
			addedFunctionNames.add(name);
		}
		
		int parameters = Becomes.getLeftSide(rule.pattern).size();
		Func f = new Func(name,parameters);
		
		f.behavior.rule = rule;
		
		FunctionsLoader.addFunc(f);
	}
	
	public Expr getVar(String s) {
		return varDefs.getOrDefault(s, var(s));
	}
	
	public void removeVar(String name) {
		varDefs.remove(name);
	}
	
	public void removeFunc(String name) {
		if(addedFunctionNames.contains(name)) {
			FunctionsLoader.funcs.remove(name);
			addedFunctionNames.remove(name);
		}
	}
	
	public void clear() {
		varDefs.clear();
	}
	
	public static Defs blank = new Defs();//careful
	
}
