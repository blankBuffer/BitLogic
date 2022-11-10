package cas;

import java.io.Serializable;
import java.util.HashMap;

import cas.primitive.Becomes;
import cas.primitive.Equ;
import cas.primitive.Func;

public class Defs extends Cas implements Serializable{
	private static final long serialVersionUID = 4654953050013809971L;
	
	HashMap<String,Rule> functionsRule = new HashMap<String,Rule>();//stores the rule for a function name
	HashMap<String,Expr> varDefs = new HashMap<String,Expr>();
	
	public void defineVar(Func defEqu) {
		String varName = Equ.getLeftSide(defEqu).toString();
		if(!varDefs.containsKey(varName)) varDefs.put(varName, Equ.getRightSide(defEqu));
		else varDefs.replace(varName, Equ.getRightSide(defEqu));
	}
	
	public void addFuncRule(Func defBecomes) {
		String name = ((Func)Becomes.getLeftSide(defBecomes)).behavior.name;
		Rule r = new Rule(defBecomes, "function definition");
		r.init();
		if(!functionsRule.containsKey(name)) functionsRule.put(name, r);
		else functionsRule.replace(name, r);
		functionsRule.put(name, r);
	}
	
	public Rule getFuncRule(String name) {
		return functionsRule.get(name);
	}
	
	public Expr getVar(String s) {
		return varDefs.getOrDefault(s, var(s));
	}
	
	public void removeVar(String name) {
		varDefs.remove(name);
	}
	public void removeFunc(String name) {
		functionsRule.remove(name);
	}
	
	public void clear() {
		varDefs.clear();
		functionsRule.clear();
	}
	
	public static Defs blank = new Defs();//careful
	
}
