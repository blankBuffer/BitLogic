package cas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cas.primitive.Equ;
import cas.primitive.ExprList;
import cas.primitive.Func;

public class Defs extends QuickMath implements Serializable{
	private static final long serialVersionUID = 4654953050013809971L;
	public ArrayList<Func> funcsArrayList = new ArrayList<Func>();//just for printing purposes
	public ArrayList<Equ> varsArrayList = new ArrayList<Equ>();//just for printing purposes
	
	
	HashMap<String,Func> functions = new HashMap<String,Func>();
	HashMap<String,Equ> varDefs = new HashMap<String,Equ>();
	
	public ExprList getVars() {
		ExprList list = new ExprList();
		for(int i = 0;i<varsArrayList.size();i++) {
			list.add(varsArrayList.get(i));
		}
		return list;
	}
	
	public void addVar(Equ def) {
		String varName = def.getLeftSide().toString();
		removeVar(varName);
		varDefs.put(varName, def);
		varsArrayList.add(def);
	}
	
	public void addFunc(Func f) {
		String name = f.name;
		removeFunc(name);
		functions.put(name, f);
		funcsArrayList.add(f);
	}
	
	public Func getFunc(String name) {
		Func out = functions.get(name);
		if(out != null) return out;
		return null;
	}
	
	public Expr getVar(String s) {
		Equ out = varDefs.get(s);
		if(out != null) return out.getRightSide().copy();
		return null;
	}
	
	public void removeVar(String name) {
		Equ equ = varDefs.get(name);
		varDefs.remove(name);
		varsArrayList.remove(equ);
	}
	public void removeFunc(String name) {
		Func f = functions.get(name);
		functions.remove(name);
		funcsArrayList.remove(f);
	}
	public void clear() {
		varDefs.clear();
		functions.clear();
		funcsArrayList.clear();
		varsArrayList.clear();
	}
	
	public static Defs blank = new Defs();
	
	@Override
	public String toString() {
		String out = "";
		out+="funcs: "+funcsArrayList;
		out+="vars: "+varsArrayList;
		return out;
	}
	
}
