package cas;

import java.io.Serializable;
import java.util.HashMap;

import cas.primitive.Becomes;
import cas.primitive.Equ;
import cas.primitive.Func;

public class Defs extends QuickMath implements Serializable{
	private static final long serialVersionUID = 4654953050013809971L;
	
	HashMap<String,Rule> functionsRule = new HashMap<String,Rule>();//stores the rule for a function name
	HashMap<String,Expr> varDefs = new HashMap<String,Expr>();
	
	public void defineVar(Equ def) {
		String varName = def.getLeftSide().toString();
		if(!varDefs.containsKey(varName)) varDefs.put(varName, def.getRightSide());
		else varDefs.replace(varName, def.getRightSide());
	}
	
	public void addFuncRule(Becomes def) {
		String name = ((Func)def.getLeftSide()).name;
		Rule r = new Rule(def, "function definition", Rule.EASY);
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
